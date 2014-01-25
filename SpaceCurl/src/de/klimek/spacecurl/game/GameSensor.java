
package de.klimek.spacecurl.game;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import de.klimek.spacecurl.R;

public class GameSensor extends GameFragment {
    public static final int DEFAULT_TITLE_RESOURCE_ID = -1;

    private AsyncTask<Void, Void, Void> _logicThread = new LogicThread();

    private TextView orientXValue;
    private TextView orientYValue;
    private TextView orientZValue;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.game_sensor, container, false);

        orientXValue = (TextView) rootView.findViewById(R.id.orient_x_value);
        orientYValue = (TextView) rootView.findViewById(R.id.orient_y_value);
        orientZValue = (TextView) rootView.findViewById(R.id.orient_z_value);

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
            _logicThread.execute();
        }
    }

    @Override
    public FreeAxisCount getFreeAxisCount() {
        return FreeAxisCount.One;
    }

    @Override
    public Effect[] getEffects() {
        Effect[] e = {};
        return e;
    }

    private class LogicThread extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            while (!isCancelled()) {
                publishProgress();

                // Delay
                try {
                    Thread.sleep(1000 / 30); // 30 FPS
                } catch (InterruptedException e) {
                    // e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            // float[] orientation = getOrientation();
            float[] orientation = getScaledOrientation();

            orientXValue.setText(String.format(" %.2f", orientation[0]));
            orientYValue.setText(String.format(" %.2f", orientation[1]));
            orientZValue.setText(String.format(" %.2f", orientation[2]));
        }

        @Override
        protected void onCancelled(Void result) {
            Log.v(TAG, "Thread: Cancelled");
            super.onCancelled(result);
        }
    }

}
