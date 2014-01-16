
package de.klimek.spacecurl.training;

import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.internal.CardArrayAdapter;
import it.gmariotti.cardslib.library.view.CardListView;

import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import de.klimek.spacecurl.R;
import de.klimek.spacecurl.util.cards.GameCard;
import de.klimek.spacecurl.util.collection.Database;
import de.klimek.spacecurl.util.collection.GameSettingsPair;
import de.klimek.spacecurl.util.collection.Training;

public class TrainingBuilderActivity extends FragmentActivity {
    private Database mDatabase = Database.getInstance();
    private CardListView mCardListView;
    private CardArrayAdapter mCardArrayAdapter;
    private List<Card> mCards = new ArrayList<Card>();
    private Training mTraining;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_training_builder);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        setupTraining();
        setupCards();
    }

    private void setupTraining() {
        mTraining = mDatabase.getTrainings().get(0);
    }

    private void setupCards() {
        invalidateCardList();
        mCardArrayAdapter = new CardArrayAdapter(this, mCards);
        mCardListView = (CardListView) findViewById(R.id.game_list);
        if (mCardListView != null) {
            mCardListView.setAdapter(mCardArrayAdapter);
        }
    }

    private Card createAddCard() {
        Card card = new Card(this) {
            @Override
            public void setupInnerViewElements(ViewGroup parent, View view) {
                ImageView imageView = new ImageView(getContext());
                imageView.setImageResource(R.drawable.ic_new);
                parent.addView(imageView);
            }
        };
        return card;
    }

    private void invalidateCardList() {
        mCards.clear();
        for (GameSettingsPair curGame : mTraining) {
            mCards.add(createGameCard(curGame));
        }
        mCards.add(createAddCard());
        if (mCardArrayAdapter != null) {
            mCardArrayAdapter.notifyDataSetChanged();
        }
    }

    private GameCard createGameCard(GameSettingsPair pair) {
        GameCard card = new GameCard(this, pair);
        return card;
    }

    public void addGame(GameSettingsPair pair) {
        mTraining.add(pair);
        invalidateCardList();
    }

}
