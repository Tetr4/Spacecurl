
package de.klimek.spacecurl.game.sensor;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GraphView.GraphViewData;
import com.jjoe64.graphview.GraphViewSeries;
import com.jjoe64.graphview.LineGraphView;

import de.klimek.spacecurl.R;
import de.klimek.spacecurl.game.GameFragment;

/**
 * Live sensor data output for debugging, display, etc
 * 
 * @author mike
 */
public class Sensor extends GameFragment {
    public static final int MAX_DATA_COUNT = 200;

    private AsyncTask<Void, Void, Void> mLogicThread = new LogicThread();

    private TextView mOrientXValue;
    private TextView mOrientYValue;
    private TextView mOrientZValue;
    private LinearLayout mLayoutGraph1;
    private LinearLayout mLayoutGraph2;
    private GraphView mGraph1;
    private GraphView mGraph2;
    private GraphViewSeries mGraphViewSeries1;
    private GraphViewSeries mGraphViewSeries2;
    private int mValueX = 0;
    private float mStatus = 1.0f;
    private float mFilteredStatus = 1.0f;
    private float mFilterWeight = 0.027f;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.game_sensor, container, false);

        mOrientXValue = (TextView) rootView.findViewById(R.id.orient_x_value);
        mOrientYValue = (TextView) rootView.findViewById(R.id.orient_y_value);
        mOrientZValue = (TextView) rootView.findViewById(R.id.orient_z_value);

        mLayoutGraph1 = (LinearLayout) rootView.findViewById(R.id.graph1);
        mLayoutGraph2 = (LinearLayout) rootView.findViewById(R.id.graph2);

        mGraphViewSeries1 = new GraphViewSeries(new
                GraphViewData[] {
                        new GraphViewData(mValueX, 1.0f)
                });

        mGraphViewSeries2 = new GraphViewSeries(new
                GraphViewData[] {
                        new GraphViewData(mValueX, 1.0f)
                });

        mGraph1 = new LineGraphView(getActivity(), "");
        mGraph1.addSeries(mGraphViewSeries1);
        mGraph1.setManualYAxisBounds(1.0f, 0.0f);
        mGraph1.setViewPort(0.0f, 200.0f);
        mGraph1.setScrollable(true);
        mGraph1.setDisableTouch(true);
        mGraph1.getGraphViewStyle().setNumHorizontalLabels(1);
        mLayoutGraph1.addView(mGraph1);

        mGraph2 = new LineGraphView(getActivity(), "");
        mGraph2.addSeries(mGraphViewSeries2);
        mGraph2.setManualYAxisBounds(1.0f, 0.0f);
        mGraph2.setViewPort(0.0f, 200.0f);
        mGraph2.setScrollable(true);
        mGraph2.setDisableTouch(true);
        mGraph2.getGraphViewStyle().setNumHorizontalLabels(1);
        mLayoutGraph2.addView(mGraph2);

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
            mStatus = (orientation[1] + 1f) / 2.0f;
            mFilteredStatus += mFilterWeight * (mStatus - mFilteredStatus);

            mOrientXValue.setText(String.format(" %.2f", orientation[0]));
            mOrientYValue.setText(String.format(" %.2f", orientation[1]));
            mOrientZValue.setText(String.format(" %.2f", orientation[2]));
            mGraphViewSeries1.appendData(new GraphViewData(mValueX, mStatus), true, MAX_DATA_COUNT);
            mGraphViewSeries2.appendData(new GraphViewData(mValueX, mFilteredStatus), true,
                    MAX_DATA_COUNT);
            ++mValueX;
        }

        @Override
        protected void onCancelled(Void result) {
            Log.v(TAG, "Thread: Cancelled");
            super.onCancelled(result);
        }
    }

}
