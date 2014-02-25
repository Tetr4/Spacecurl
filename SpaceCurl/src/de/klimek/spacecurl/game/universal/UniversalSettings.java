
package de.klimek.spacecurl.game.universal;

import java.util.ArrayList;

import de.klimek.spacecurl.game.GameFragment;
import de.klimek.spacecurl.game.GameSettings;

public class UniversalSettings extends GameSettings {
    private ArrayList<Target> mTargets = new ArrayList<Target>();
    private ArrayList<Path> mPaths = new ArrayList<Path>();

    @Override
    public Class<? extends GameFragment> getGameClass() {
        return Universal.class;
    }

    public void addTarget(Target target) {
        mTargets.add(target);
    }

    // public void addPath(Target[] targets) {
    // mPaths.add(new Path(targets, 500));
    // }

    // public void addAxis() {
    //
    // }

    // public ArrayList<Path> getPaths() {
    // return mPaths;
    // }

    public ArrayList<Target> getTargets() {
        return mTargets;
    }
}
