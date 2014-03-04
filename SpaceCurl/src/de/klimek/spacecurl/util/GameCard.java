
package de.klimek.spacecurl.util;

import it.gmariotti.cardslib.library.internal.Card;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import de.klimek.spacecurl.R;
import de.klimek.spacecurl.game.GameSettings;

public class GameCard extends Card {
    private TextView mGameTitleTextView;
    private String mGameTitle;
    private GameSettings mGameSettings;

    public GameCard(Context context, GameSettings gameSettings) {
        super(context, R.layout.card_game);
        setGameSettings(gameSettings);
        mGameTitle = gameSettings.getTitle();
    }

    @Override
    public void setupInnerViewElements(ViewGroup parent, View view) {
        mGameTitleTextView = (TextView) parent.findViewById(
                R.id.card_game_name);
        mGameTitleTextView.setText(mGameTitle);
    }

    public GameSettings getGameSettings() {
        return mGameSettings;
    }

    public void setGameSettings(GameSettings gameSettings) {
        mGameSettings = gameSettings;
    }

}
