
package de.klimek.spacecurl.game.tunnel;

import de.klimek.spacecurl.game.GameDescription;
import de.klimek.spacecurl.game.GameFragment;

public class TunnelDescription extends GameDescription {
    private int mLives;
    private boolean mShowLives = true;

    public TunnelDescription(String title, int lives) {
        super(title);
        mLives = lives;
        setFreeAxisCount(1);
        Effect[] effects = {
                Effect.Accuracy,
                Effect.Speed
        };
        setEffects(effects);
    }

    public void setTunnelHeight(float height) {
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

    @Override
    protected GameFragment newFragment() {
        return new Tunnel();
    }

}
