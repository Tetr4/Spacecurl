
package de.klimek.spacecurl.util.collection;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GraphViewSeries;

/**
 * Represents the status for a single game. Has a title and a
 * {@link GraphViewSeries} for storing graph data.
 * 
 * @author Mike Klimek
 */
public class GameStatus {
    private String mTitle;
    private GraphViewSeries mGraphViewSeries;
    private int mValueX = 0;
    private static final int MAX_DATA_COUNT = 200;

    public GameStatus(String title) {
        mTitle = title;
        mGraphViewSeries = new GraphViewSeries(new
                GraphView.GraphViewData[] {
                        new GraphView.GraphViewData(mValueX++, 1.0f)
                });
    }

    public void addStatus(float status) {
        // FIXME High MAX_DATA_COUNT triggers garbage coll. too often -> stutter
        // TODO Compress data? reuse old graphviewdatas (like viewholder)?
        mGraphViewSeries.appendData(new GraphView.GraphViewData(mValueX++, status), true, MAX_DATA_COUNT);
    }

    public GraphViewSeries getGraphViewSeries() {
        return mGraphViewSeries;
    }

    public String getTitle() {
        return mTitle;
    }

    public void reset() {
        mGraphViewSeries.resetData(new
                GraphView.GraphViewData[] {
                        new GraphView.GraphViewData(mValueX++, 0)
                });
    }
}
