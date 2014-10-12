
package de.klimek.spacecurl.util.cards;

import it.gmariotti.cardslib.library.internal.Card;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.LineGraphView;

import de.klimek.spacecurl.R;
import de.klimek.spacecurl.activities.BasicTrainingActivity;
import de.klimek.spacecurl.util.collection.GameStatus;

/**
 * Represents a {@link GameStatus} in the status-panel of the
 * {@link BasicTrainingActivity}. Contains a title and a {@link GraphView} which
 * displays the GameStatus' graph data.
 * 
 * @author Mike Klimek
 */
public class StatusCard extends Card {
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
        LinearLayout graphLayout = (LinearLayout) view.findViewById(
                R.id.card_status_graph);
        if (graphLayout != null) {
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
