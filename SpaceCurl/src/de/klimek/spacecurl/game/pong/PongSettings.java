
package de.klimek.spacecurl.game.pong;

import de.klimek.spacecurl.game.GameFragment;
import de.klimek.spacecurl.game.GameSettings;

public class PongSettings extends GameSettings {

    @Override
    public GameFragment getFragment() {
        Pong fragment = new Pong();
        fragment.setSettings(this);
        return fragment;
    }

    public void setPaddleWidth(float width) {
    }

}
