
package de.klimek.spacecurl.game.lights;

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

import java.util.Random;

import de.klimek.spacecurl.R;
import de.klimek.spacecurl.game.GameFragment;

/**
 * A game, where the user has to move during a "GO"-stage in order to earn
 * points and stop during a "STOP"-stage so he doesn't lose points.
 * 
 * @author Mike Klimek
 */
public class Lights extends GameFragment {
    private static final String TAG = "Lights";

    private AsyncTask<Void, Void, Void> mLogicThread = new LogicThread();
    private static final int FPS = 30;

    private int mGoalDistance = 25000;
    private float mDistance = 0;
    private long mRemainingStageTime = new Random().nextInt(20000 - 10000) + 10000;;
    private long mTotalTime = 0;
    private boolean mBonus = false;

    private float mStatus = 1.0f;
    private float mFilteredStatus = mStatus;
    private float mFilterWeight = 0.02f;

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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.game_lights, container, false);
        Resources res = getResources();

        LightsDescription lightsDescription = (LightsDescription) getGameDescription();
        mGoalDistance = lightsDescription.getRequiredDistance();

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
        mLogicThread.cancel(true);
    }

    @Override
    public void doResumeGame() {
        if (!mLogicThread.getStatus().equals(AsyncTask.Status.RUNNING)) {
            mLogicThread = new LogicThread();
            mLogicThread.execute();
        }
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

    private static String timeToString(long time, boolean showMillis) {
        if (showMillis) {
            return "" + time / 1000
                    + ":" + time % 1000;
        } else {
            return "" + (1 + time / 1000);
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
                if (mRemainingStageTime > 0) {
                    // TODO set overhead mBonus
                    _rotationSpeed = getRotationSpeed();
                    _rotation = _rotationSpeed * _deltaTime;
                    switch (mStage) {
                        case Go:
                            // prevent potential overflow
                            if (mTotalTime > (Long.MAX_VALUE - _deltaTime)) {
                                mStage = Stage.Result;
                                mRemainingStageTime = 7000;
                            } else {
                                mTotalTime += _deltaTime;
                            }

                            mDistance += _rotation;
                            if (mDistance >= mGoalDistance) { // reached goal
                                mStage = Stage.Result;
                                mRemainingStageTime = 7000;
                            }

                            // update status
                            mStatus = _rotationSpeed;
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
                    Log.w(TAG, "LogicThread sleep interrupted");
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
                    // TODO resource string
                    boolean handled = notifyFinished("Gesamtzeit: "
                            + timeToString(mTotalTime, true));
                    if (!handled) {
                        // restart
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
