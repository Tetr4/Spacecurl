
package de.klimek.spacecurl.game.lights;

import de.klimek.spacecurl.game.GameDescription;
import de.klimek.spacecurl.game.GameFragment;

public class LightsDescription extends GameDescription {
    private int mRequiredDistance = 25000;

    public LightsDescription(String title) {
        super(title);
        setFreeAxisCount(3);
    }

    public void setRequiredDistance(int distance) {
        mRequiredDistance = distance;
    }

    public int getRequiredDistance() {
        return mRequiredDistance;
    }

    @Override
    protected GameFragment newFragment() {
        return new Lights();
    }

}
