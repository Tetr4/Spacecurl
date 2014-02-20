
package de.klimek.spacecurl.game.lights;

import de.klimek.spacecurl.game.GameFragment;
import de.klimek.spacecurl.game.GameSettings;

public class LightsSettings extends GameSettings {
    @Override
    public Class<? extends GameFragment> getGameClass() {
        return Lights.class;
    }

    public void setRequiredDistance() {

    }

}
