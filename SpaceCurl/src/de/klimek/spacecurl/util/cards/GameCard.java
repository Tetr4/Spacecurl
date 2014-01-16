
package de.klimek.spacecurl.util.cards;

import it.gmariotti.cardslib.library.internal.Card;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import de.klimek.spacecurl.R;
import de.klimek.spacecurl.util.collection.GameSettingsPair;

public class GameCard extends Card {
    private TextView mGameTitleTextView;
    private String mGameTitle;
    private GameSettingsPair mGameSettingsPair;

    public GameCard(Context context, GameSettingsPair gameSettingsPair) {
        super(context, R.layout.card_game);
        setGameSettingsPair(gameSettingsPair);
        mGameTitle = "asdf";
    }

    @Override
    public void setupInnerViewElements(ViewGroup parent, View view) {
        mGameTitleTextView = (TextView) parent.findViewById(
                R.id.card_game_name);
        mGameTitleTextView.setText(mGameTitle);
    }

    public GameSettingsPair getmGameSettingsPair() {
        return mGameSettingsPair;
    }

    public void setGameSettingsPair(GameSettingsPair gameSettingsPair) {
        mGameSettingsPair = gameSettingsPair;
    }

}
