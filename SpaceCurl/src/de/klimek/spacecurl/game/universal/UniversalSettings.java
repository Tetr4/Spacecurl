
package de.klimek.spacecurl.game.universal;

import java.util.ArrayList;

import de.klimek.spacecurl.game.GameFragment;
import de.klimek.spacecurl.game.GameSettings;

public class UniversalSettings extends GameSettings {
    private ArrayList<Target> mTargets = new ArrayList<Target>();

    @Override
    public Class<? extends GameFragment> getGameClass() {
        return Universal.class;
    }

    // public void setMode()
    public void addTarget(Target target) {

    }

    public ArrayList<Target> getTargets() {
        return mTargets;
    }
}
