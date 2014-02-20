
package de.klimek.spacecurl.game.pong;

import de.klimek.spacecurl.game.GameFragment;
import de.klimek.spacecurl.game.GameSettings;

public class PongSettings extends GameSettings {

    @Override
    public Class<? extends GameFragment> getGameClass() {
        return Pong.class;
    }

    public void setPaddleWidth(float width) {
    }

}
