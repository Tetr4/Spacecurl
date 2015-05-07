
package de.klimek.spacecurl.game.pong;

import de.klimek.spacecurl.game.GameDescription;
import de.klimek.spacecurl.game.GameFragment;

public class PongDescription extends GameDescription {
    private int mLives;
    private boolean mShowLives = true;

    public PongDescription(String title, int lives) {
        super(title);
        mLives = lives;
        setFreeAxisCount(2);
        Effect[] effects = {
                Effect.Accuracy,
                Effect.Speed
        };
        setEffects(effects);
    }

    @Override
    public GameFragment newFragment() {
        return new Pong();
    }

    public void setPaddleWidth(float width) {
        // TODO implement
    }

    public int getLives() {
        return mLives;
    }

    public void setLives(int lives) {
        mLives = lives;
    }

    public boolean showLives() {
        return mShowLives;
    }

    public void setShowLives(boolean showLives) {
        mShowLives = showLives;
    }

}
