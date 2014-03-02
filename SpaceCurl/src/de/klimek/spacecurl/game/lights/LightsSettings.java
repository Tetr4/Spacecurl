
package de.klimek.spacecurl.game.lights;

import de.klimek.spacecurl.game.GameFragment;
import de.klimek.spacecurl.game.GameSettings;

public class LightsSettings extends GameSettings {
    @Override
    public GameFragment getFragment() {
        Lights fragment = new Lights();
        fragment.setSettings(this);
        return fragment;
    }

    public void setRequiredDistance() {

    }

}
