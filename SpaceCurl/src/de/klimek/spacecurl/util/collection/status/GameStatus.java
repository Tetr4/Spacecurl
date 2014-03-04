
package de.klimek.spacecurl.util.collection.status;

import com.jjoe64.graphview.GraphView.GraphViewData;
import com.jjoe64.graphview.GraphViewSeries;

public class GameStatus {
    private String mTitle = "";
    private GraphViewSeries mGraphViewSeries;
    private int mValueX = 0;
    private static final int MAX_DATA_COUNT = 200;

    public GameStatus(String title) {
        mTitle = title;
        mGraphViewSeries = new GraphViewSeries(new
                GraphViewData[] {
                        new GraphViewData(mValueX++, 1.0f)
                });
    }

    public void addStatus(float status) {
        // FIXME Triggers garbage collection too often -> stutter
        // TODO Compress graph data?
        mGraphViewSeries.appendData(new GraphViewData(mValueX++, status), true, MAX_DATA_COUNT);
    }

    public GraphViewSeries getGraphViewSeries() {
        return mGraphViewSeries;
    }

    public String getTitle() {
        return mTitle;
    }

    public void reset() {
        mGraphViewSeries.resetData(new
                GraphViewData[] {
                        new GraphViewData(mValueX++, 0)
                });
    }
}
