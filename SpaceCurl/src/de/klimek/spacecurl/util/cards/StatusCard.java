
package de.klimek.spacecurl.util.cards;

import it.gmariotti.cardslib.library.internal.Card;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.LineGraphView;

import de.klimek.spacecurl.R;
import de.klimek.spacecurl.util.collection.Status;

public class StatusCard extends Card {
    // private int mRatingColor = 0xFFFF0000;
    private GraphView mGraphView;
    private TextView mScoreTextView;
    private FrameLayout mRatingLayout;
    private Status mStatus;

    public StatusCard(Context context, Status status) {
        super(context, R.layout.card_status);
        setStatus(status);
    }

    @Override
    public void setupInnerViewElements(ViewGroup parent, View view) {
        mGraphView = new LineGraphView(getContext(), mTitle);
        mGraphView.setScrollable(true);
        mGraphView.addSeries(mStatus.mGraphViewSeries);
        LinearLayout graphLayout = (LinearLayout) parent.findViewById(
                R.id.card_status_graph);
        if (graphLayout != null) {
            // graphLayout.removeAllViews();
            graphLayout.addView(mGraphView);
        }
        mScoreTextView = (TextView) parent.findViewById(
                R.id.card_status_score);
        if (mScoreTextView != null) {
            // mScoreTextView.setText(Integer.toString(mScore));
        }

        mRatingLayout = (FrameLayout) parent.findViewById(
                R.id.card_status_rating);
        if (mRatingLayout != null) {
            // mRatingLayout.setBackgroundColor(mRatingColor);
        }
    }

    public Status getStatus() {
        return mStatus;
    }

    public void setStatus(Status status) {
        this.mStatus = status;
    }

}
