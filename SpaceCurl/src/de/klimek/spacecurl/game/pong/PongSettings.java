
package de.klimek.spacecurl.game.pong;

import de.klimek.spacecurl.game.GameFragment;
import de.klimek.spacecurl.game.GameSettings;

public class PongSettings extends GameSettings {
    private int mLives;
    private boolean mShowLives = true;

    public PongSettings(String title, int lives) {
        super(title);
        mLives = lives;
    }

    @Override
    public GameFragment getFragment() {
        Pong fragment = new Pong();
        fragment.setSettings(this);
        return fragment;
    }

    public void setPaddleWidth(float width) {
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
