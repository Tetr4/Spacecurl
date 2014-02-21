
package de.klimek.spacecurl.util.collection.status;

import com.jjoe64.graphview.GraphView.GraphViewData;
import com.jjoe64.graphview.GraphViewSeries;

public class GameStatus {
    private String mTitle = "";
    private GraphViewSeries mGraphViewSeries;
    private int valueX = 0;
    private static final int MAX_DATA_COUNT = 200;

    public GameStatus(String title) {
        mTitle = title;
        mGraphViewSeries = new GraphViewSeries(new
                GraphViewData[] {
                        new GraphViewData(valueX++, 0),
                        new GraphViewData(valueX++, 0),
                        new GraphViewData(valueX++, 0),
                        new GraphViewData(valueX++, 0),
                        new GraphViewData(valueX++, 0)
                });
    }

    public void addStatus(float status) {
        mGraphViewSeries.appendData(new GraphViewData(valueX++, status), true, MAX_DATA_COUNT);
    }

    public GraphViewSeries getGraphViewSeries() {
        return mGraphViewSeries;
    }

    public String getTitle() {
        return mTitle;
    }
}
