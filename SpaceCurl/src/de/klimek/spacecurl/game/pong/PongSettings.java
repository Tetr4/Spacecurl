
package de.klimek.spacecurl.game.pong;

import de.klimek.spacecurl.game.GameFragment;
import de.klimek.spacecurl.game.GameSettings;

public class PongSettings extends GameSettings {
    private int mLifes;

    public PongSettings(String title, int lifes) {
        super(title);
        mLifes = lifes;
    }

    @Override
    public GameFragment getFragment() {
        Pong fragment = new Pong();
        fragment.setSettings(this);
        return fragment;
    }

    public void setPaddleWidth(float width) {
    }

    public int getLifes() {
        return mLifes;
    }

    public void setLifes(int lifes) {
        mLifes = lifes;
    }

}
