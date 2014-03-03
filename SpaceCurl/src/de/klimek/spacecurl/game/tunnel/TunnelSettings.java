
package de.klimek.spacecurl.game.tunnel;

import de.klimek.spacecurl.game.GameFragment;
import de.klimek.spacecurl.game.GameSettings;

public class TunnelSettings extends GameSettings {
    private int mLives;
    private boolean mShowLives = true;

    public TunnelSettings(String title, int lives) {
        super(title);
        mLives = lives;
    }

    public void setTunnelHeight(float height) {
    }

    @Override
    public GameFragment getFragment() {
        Tunnel fragment = new Tunnel();
        fragment.setSettings(this);
        return fragment;
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
