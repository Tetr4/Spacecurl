
package de.klimek.spacecurl.util;

import it.gmariotti.cardslib.library.internal.Card;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.LineGraphView;

import de.klimek.spacecurl.R;
import de.klimek.spacecurl.util.collection.status.GameStatus;

public class StatusCard extends Card {
    // private int mRatingColor = 0xFFFF0000;
    private GraphView mGraphView;
    private GameStatus mStatus;

    public StatusCard(Context context, GameStatus status) {
        super(context, R.layout.card_status);
        mStatus = status;
        setTitle(mStatus.getTitle());
    }

    @Override
    public void setupInnerViewElements(ViewGroup parent, View view) {
        mGraphView = new LineGraphView(getContext(), mTitle);
        mGraphView.addSeries(mStatus.getGraphViewSeries());
        mGraphView.setManualYAxisBounds(1.0f, 0.0f);
        mGraphView.setViewPort(0.0f, 200.0f);
        mGraphView.setScrollable(true);
        mGraphView.setDisableTouch(true);
        mGraphView.getGraphViewStyle().setNumHorizontalLabels(1);
        LinearLayout graphLayout = (LinearLayout) parent.findViewById(
                R.id.card_status_graph);
        if (graphLayout != null) {
            // graphLayout.removeAllViews();
            graphLayout.addView(mGraphView);
        }
    }

    public GameStatus getStatus() {
        return mStatus;
    }

    public void setStatus(GameStatus status) {
        mStatus = status;
    }

}
