
package de.klimek.spacecurl.game.sensor;

import de.klimek.spacecurl.game.GameDescription;
import de.klimek.spacecurl.game.GameFragment;

public class SensorDescription extends GameDescription {

    public SensorDescription(String title) {
        super(title);
    }

    @Override
    protected GameFragment newFragment() {
        return new Sensor();
    }

}
