
package de.klimek.spacecurl.util.cards;

import it.gmariotti.cardslib.library.internal.Card;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import de.klimek.spacecurl.R;
import de.klimek.spacecurl.game.GameDescription;

public class GameCard extends Card {
    private TextView mGameTitleTextView;
    private String mGameTitle;
    private GameDescription mGameDescription;

    public GameCard(Context context, GameDescription gameDescription) {
        super(context, R.layout.card_game);
        setGameDescription(gameDescription);
        mGameTitle = gameDescription.getTitle();
    }

    @Override
    public void setupInnerViewElements(ViewGroup parent, View view) {
        mGameTitleTextView = (TextView) parent.findViewById(
                R.id.card_game_name);
        mGameTitleTextView.setText(mGameTitle);
    }

    public GameDescription getGameDescription() {
        return mGameDescription;
    }

    public void setGameDescription(GameDescription gameDescription) {
        mGameDescription = gameDescription;
    }

}
