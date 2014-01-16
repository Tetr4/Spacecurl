
package de.klimek.spacecurl.util.collection;

import com.jjoe64.graphview.GraphViewSeries;

public class Status {
    public String mTitle = "";
    public int mScore = 0;
    public String mScoreLabel = "";
    public GraphViewSeries mGraphViewSeries;
    public static final int MAX_DATA_COUNT = 200;
    public String mXLabel = "";
    public String mYLabel = "";
    public int mRating = 1;
    public static final int MIN_RATING = 1;
    public static final int MAX_RATING = 10;

}
