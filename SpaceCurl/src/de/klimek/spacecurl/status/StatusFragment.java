
package de.klimek.spacecurl.status;

import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.internal.CardArrayAdapter;
import it.gmariotti.cardslib.library.view.CardListView;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import de.klimek.spacecurl.R;
import de.klimek.spacecurl.util.cards.StatusCard;
import de.klimek.spacecurl.util.collection.Database;
import de.klimek.spacecurl.util.collection.Status;

public class StatusFragment extends Fragment {
    private Database mDatabase = Database.getInstance();
    private CardListView mCardListView;
    private CardArrayAdapter mCardArrayAdapter;
    private List<Card> mCards = new ArrayList<Card>();

    // Empty constructor required for fragment subclasses
    public StatusFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater,
            ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_status, container, false);

        mCardArrayAdapter = new CardArrayAdapter(getActivity(), mCards);
        mCardListView = (CardListView) rootView.findViewById(R.id.card_list);
        if (mCardListView != null) {
            mCardListView.setAdapter(mCardArrayAdapter);
        }
        invalidateCardList();
        return rootView;
    }

    @Override
    public void onAttach(Activity activity) {
        // TODO Auto-generated method stub
        super.onAttach(activity);
    }

    private void invalidateCardList() {
        mCards.clear();
        for (Status curStatus : mDatabase.getStatuses()) {
            mCards.add(createStatusCard(curStatus));
        }
        if (mCardArrayAdapter != null) {
            mCardArrayAdapter.notifyDataSetChanged();
        }
    }

    private Card createStatusCard(Status status) {
        StatusCard card = new StatusCard(getActivity(), status);
        return card;
    }

    public void addStatus(Status status) {
        mDatabase.getStatuses().add(status);
        if (isAdded()) {
            mCards.add(createStatusCard(status));
            invalidateCardList();
        }
    }
}
