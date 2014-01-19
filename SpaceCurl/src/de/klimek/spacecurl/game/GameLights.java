
package de.klimek.spacecurl.game;

import java.util.Random;

import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import de.klimek.spacecurl.R;

public class GameLights extends GameFragment {
    public static final int DEFAULT_TITLE_RESOURCE_ID = R.string.game_lights;
    private AsyncTask<Integer, String, Integer> _logicThread = new LogicThread();
    private int mAllotedTime = 30000;
    private volatile int mRemainingTime = mAllotedTime;
    private static final int FPS = 30;

    private int mScore;
    private String mMessageGo;
    private String mMessageStop;
    private int mColorGo;
    private int mColorStop;
    private int mColorResult;

    private State mState = State.Stop;

    private static enum State {
        Go, Stop, Result
    }

    private LinearLayout mLayout;
    private TextView mTextViewTime;
    private TextView mTextViewScore;
    private TextView mTextViewMessage;

    // private StatusBundle mStatusBundle;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.game_lights, container, false);
        Resources res = getResources();
        mMessageGo = res.getString(R.string.game_lights_message_go);
        mMessageStop = res.getString(R.string.game_lights_message_stop);
        mColorGo = res.getColor(R.color.game_lights_color_go);
        mColorStop = res.getColor(R.color.game_lights_color_stop);
        mColorResult = res.getColor(R.color.game_lights_color_result);

        mLayout = (LinearLayout) rootView.findViewById(R.id.game_lights_layout);
        mTextViewTime = (TextView) rootView.findViewById(R.id.game_lights_time);
        mTextViewScore = (TextView) rootView.findViewById(R.id.game_lights_score);
        mTextViewMessage = (TextView) rootView.findViewById(R.id.game_lights_message);
        stop();
        return rootView;
    }

    @Override
    public void pauseGame() {
        _logicThread.cancel(true);
    }

    @Override
    public void resumeGame() {
        if (!_logicThread.getStatus().equals(AsyncTask.Status.RUNNING)) {
            _logicThread = new LogicThread();
            _logicThread.execute(mRemainingTime);
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
        mState = State.Go;
        mLayout.setBackgroundColor(mColorGo);
        mTextViewMessage.setText(mMessageGo);
        int time = new Random().nextInt(10000 - 3000) + 3000;
        _logicThread = new LogicThread();
        _logicThread.execute(mAllotedTime);
    }

    private void stop() {
        mState = State.Stop;
        mLayout.setBackgroundColor(mColorStop);
        mTextViewMessage.setText(mMessageStop);
        int time = new Random().nextInt(10000 - 3000) + 3000;
        _logicThread = new LogicThread();
        _logicThread.execute(mAllotedTime);
    }

    private void result() {
        mState = State.Result;
        mLayout.setBackgroundColor(mColorResult);
        // mTextViewMessage.setText(mMessageStop);
        mRemainingTime = mAllotedTime;
        int time = 3000;
        _logicThread = new LogicThread();
        _logicThread.execute(time);
    }

    private class LogicThread extends AsyncTask<Integer, String, Integer> {

        @Override
        protected Integer doInBackground(Integer... params) {
            long startTime = System.currentTimeMillis();
            int totalTime = params[0];
            int remainingTime = totalTime;
            String remainingTimeString;

            while (!isCancelled()) {
                remainingTime = (int) (totalTime - (System.currentTimeMillis() - startTime));
                remainingTimeString = "" + remainingTime / 1000
                        + ":" + remainingTime % 1000;
                publishProgress(remainingTimeString);
                if (remainingTime < 0)
                    break;

                // Delay
                try {
                    Thread.sleep(1000 / FPS);
                } catch (InterruptedException e) {
                    // e.printStackTrace();
                }
            }
            return remainingTime;
        }

        @Override
        protected void onProgressUpdate(String... values) {
            // TODO: get angular speed
            mTextViewTime.setText(values[0]);
            // mTextViewScore.setText(values[1]);
        }

        @Override
        protected void onPostExecute(Integer value) {
            if (mState == State.Stop) {
                go();
            } else
                stop();
        }

        @Override
        protected void onCancelled(Integer value) {
            Log.v(TAG, "Thread: Cancelled");

            mRemainingTime = value;
        }
    }
}
