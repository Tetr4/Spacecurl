
package de.klimek.spacecurl.game.sensor;

import de.klimek.spacecurl.game.GameFragment;
import de.klimek.spacecurl.game.GameSettings;

public class SensorSettings extends GameSettings {

    @Override
    public GameFragment getFragment() {
        Sensor fragment = new Sensor();
        fragment.setSettings(this);
        return fragment;
    }

}
