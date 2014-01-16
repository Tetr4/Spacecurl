
package de.klimek.spacecurl.util.collection;

import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.internal.CardArrayAdapter;
import it.gmariotti.cardslib.library.view.CardListView;

import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public abstract class CardListFragment<COLLECTION extends ArrayList<UNIT>, UNIT, CARD extends Card>
        extends Fragment {
    private CardListView mCardListView;
    private CardArrayAdapter mCardArrayAdapter;
    private List<Card> mCards = new ArrayList<Card>();
    private COLLECTION mList;

    // Empty constructor required for fragment subclasses
    public CardListFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater,
            ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(getLayoutID(), container, false);

        mCardArrayAdapter = new CardArrayAdapter(getActivity(), mCards);
        mCardListView = (CardListView) rootView.findViewById(getCardListID());
        if (mCardListView != null) {
            mCardListView.setAdapter(mCardArrayAdapter);
        }
        return rootView;
    }

    private void invalidateCardList() {
        mCards.clear();
        for (UNIT curUnit : mList) {
            mCards.add(createCard(curUnit));
        }
        if (mCardArrayAdapter != null) {
            mCardArrayAdapter.notifyDataSetChanged();
        }
    }

    protected abstract CARD createCard(UNIT unit);

    protected abstract int getLayoutID();

    protected abstract int getCardListID();

    public void addStatus(UNIT unit) {
        mList.add(unit);
        invalidateCardList();
    }

}
