
package de.klimek.spacecurl.game.lights;

import java.util.Random;

import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import de.klimek.spacecurl.R;
import de.klimek.spacecurl.game.GameFragment;

public class Lights extends GameFragment {
    private AsyncTask<Void, Void, Void> _logicThread = new LogicThread();
    private static final int FPS = 30;
    private int mGoalDistance = 60000;
    private volatile float mDistance = 0;
    private volatile long mRemainingStageTime = new Random().nextInt(20000 - 10000) + 10000;;
    private volatile long mTotalTime = 0;
    private volatile boolean mBonus = false;

    private float mStatus = 1.0f;
    private float mFilteredStatus = mStatus;
    private float mFilterWeight = 0.05f;

    private Stage mStage = Stage.Go;

    private static enum Stage {
        Go, Stop, Result
    }

    private String mMessageGo;
    private String mMessageStop;
    private int mColorGo;
    private int mColorStop;

    private LinearLayout mLayoutStopAndGo;
    private TextView mTextViewMessage;
    private TextView mTextViewRemainingTime;
    private TextView mTextViewTotalTime;
    private TextView mTextViewBonus;
    private ProgressBar mProgressBar;

    private LinearLayout mLayoutResult;
    private TextView mTextViewFinalTime;
    private TextView mTextViewContinueTime;

    // private StatusBundle mStatusBundle;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.game_lights, container, false);
        Resources res = getResources();

        mMessageGo = res.getString(R.string.game_lights_message_go);
        mMessageStop = res.getString(R.string.game_lights_message_stop);
        mColorGo = res.getColor(R.color.game_lights_color_go);
        mColorStop = res.getColor(R.color.game_lights_color_stop);

        mLayoutStopAndGo = (LinearLayout) rootView
                .findViewById(R.id.game_lights_layout_stop_and_go);
        mLayoutResult = (LinearLayout) rootView.findViewById(R.id.game_result_layout);
        mTextViewMessage = (TextView) rootView.findViewById(R.id.game_lights_message);
        mTextViewRemainingTime = (TextView) rootView.findViewById(R.id.game_lights_remaining_time);
        mTextViewTotalTime = (TextView) rootView.findViewById(R.id.game_lights_total_time);
        mTextViewBonus = (TextView) rootView.findViewById(R.id.game_lights_bonus);
        mProgressBar = (ProgressBar) rootView.findViewById(R.id.game_lights_progress);

        mTextViewFinalTime = (TextView) rootView.findViewById(R.id.game_result_score);
        mTextViewContinueTime = (TextView) rootView.findViewById(R.id.game_result_continue_time);
        go();
        return rootView;
    }

    @Override
    public void doPauseGame() {
        _logicThread.cancel(true);
    }

    @Override
    public void doResumeGame() {
        if (!_logicThread.getStatus().equals(AsyncTask.Status.RUNNING)) {
            _logicThread = new LogicThread();
            _logicThread.execute();
        }
    }

    @Override
    public FreeAxisCount getFreeAxisCount() {
        return FreeAxisCount.Three;
    }

    @Override
    public Effect[] getEffects() {
        Effect[] e = {};
        return e;
    }

    private void go() {
        mLayoutStopAndGo.setVisibility(View.VISIBLE);
        mLayoutResult.setVisibility(View.INVISIBLE);
        mLayoutStopAndGo.setBackgroundColor(mColorGo);
        mTextViewMessage.setText(mMessageGo);
        mTextViewRemainingTime.setText(timeToString(mRemainingStageTime, false));
        mTextViewTotalTime.setText(timeToString(mTotalTime, true));
        mTextViewBonus.setText(mBonus ? "BONUS!" : "");
        mProgressBar.setProgress((int) ((mDistance / mGoalDistance) * 100));
    }

    private void stop() {
        mLayoutStopAndGo.setVisibility(View.VISIBLE);
        mLayoutResult.setVisibility(View.INVISIBLE);
        mLayoutStopAndGo.setBackgroundColor(mColorStop);
        mTextViewMessage.setText(mMessageStop);
        mTextViewRemainingTime.setText(timeToString(mRemainingStageTime, false));
        mTextViewTotalTime.setText(timeToString(mTotalTime, true));
        mTextViewBonus.setText("");
        mProgressBar.setProgress((int) ((mDistance / mGoalDistance) * 100));
    }

    private void result() {
        mLayoutStopAndGo.setVisibility(View.INVISIBLE);
        mLayoutResult.setVisibility(View.VISIBLE);
        mTextViewContinueTime.setText(timeToString(mRemainingStageTime, false));
        mTextViewFinalTime.setText(timeToString(mTotalTime, true));
    }

    private String timeToString(long time, boolean showMillis) {
        if (showMillis) {
            return "" + time / 1000
                    + ":" + time % 1000;
        } else {
            return "" + time / 1000;
        }
    }

    private class LogicThread extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            long _lastTime = System.currentTimeMillis();
            long _startTime;
            long _deltaTime;
            long _timeEnd;
            long _timeSleep;
            float _rotation;
            float _rotationSpeed;
            while (!isCancelled()) {
                _startTime = System.currentTimeMillis();
                _deltaTime = _startTime - _lastTime;
                _lastTime = _startTime;
                mRemainingStageTime -= _deltaTime;
                // Stage time remaining
                if (mRemainingStageTime > 1000) {
                    // TODO set mBonus
                    _rotationSpeed = getRotationSpeed();
                    _rotation = _rotationSpeed * _deltaTime;
                    switch (mStage) {
                        case Go:
                            // prevent potential overflow
                            if (Long.MAX_VALUE - _deltaTime < mTotalTime) {
                                mStage = Stage.Result;
                                mRemainingStageTime = 7000;
                            }
                            mTotalTime += _deltaTime;

                            mDistance += _rotation;
                            if (mDistance >= mGoalDistance) { // reached goal
                                mStage = Stage.Result;
                                mRemainingStageTime = 7000;
                            }

                            // update status
                            mStatus = _rotationSpeed / 2.0f;
                            // Cutoff values between 0.0f and 1.0f
                            mStatus = Math.min(1.0f, Math.max(mStatus, 0.0f));
                            // filter
                            mFilteredStatus += mFilterWeight * (mStatus -
                                    mFilteredStatus);
                            break;

                        case Stop:
                            mDistance -= _rotation * 2;
                            if (mDistance < 0)
                                mDistance = 0;
                            // update status
                            Log.d(TAG, Float.toString(_rotationSpeed));
                            mStatus = 1.0f - (_rotationSpeed);
                            // Cutoff values between 0.0f and 1.0f
                            mStatus = Math.min(1.0f, Math.max(mStatus, 0.0f));
                            // filter
                            mFilteredStatus += mFilterWeight * (mStatus -
                                    mFilteredStatus);
                            break;

                        case Result:
                            break;
                    }

                } else { // Stage time ran out
                    switch (mStage) {
                        case Go:
                            mStage = Stage.Stop;
                            mRemainingStageTime = new Random().nextInt(10000 - 5000) + 5000;
                            break;
                        case Stop:
                            mStage = Stage.Go;
                            mRemainingStageTime = new Random().nextInt(20000 - 10000) + 10000;
                            break;
                        case Result:
                            mStage = Stage.Go;
                            mTotalTime = 0;
                            mDistance = 0;
                            mRemainingStageTime = new Random().nextInt(20000 - 10000) + 10000;
                            break;
                    }
                }
                _timeEnd = System.currentTimeMillis();
                _timeSleep = (1000 / FPS) - (_timeEnd - _startTime);
                publishProgress();

                // Delay
                try {
                    Thread.sleep(_timeSleep < 0 ? 0 : _timeSleep);
                } catch (InterruptedException e) {
                    // e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            switch (mStage) {
                case Go:
                    go();
                    notifyStatusChanged(mFilteredStatus);
                    break;
                case Stop:
                    stop();
                    notifyStatusChanged(mFilteredStatus);
                    break;
                case Result:
                    boolean handled = notifyFinished("Gesamtzeit: "
                            + timeToString(mTotalTime, true));
                    if (!handled) {
                        result();
                    }
                    break;
            }
        }

        @Override
        protected void onCancelled(Void value) {
            Log.v(TAG, "Thread: Cancelled");
        }
    }
}
