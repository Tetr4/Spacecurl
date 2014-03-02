
package de.klimek.spacecurl.game.universal;

import java.util.ArrayList;

import de.klimek.spacecurl.game.GameFragment;
import de.klimek.spacecurl.game.GameSettings;

public class UniversalSettings extends GameSettings {
    private ArrayList<Target> mTargets = new ArrayList<Target>();
    private ArrayList<Path> mPaths = new ArrayList<Path>();

    public void addTarget(float x, float y, float radius) {
        mTargets.add(new Target(x, y, radius));
    }

    public void addTarget(Target target) {
        mTargets.add(target);
    }

    public void addPath(float x1, float y1, float x2, float y2, float width) {
        mPaths.add(new Path(x1, y1, x2, y2, width, 200));
    }

    public void addPath(Path path) {
        mPaths.add(path);
    }

    public ArrayList<Path> getPaths() {
        return mPaths;
    }

    public ArrayList<Target> getTargets() {
        return mTargets;
    }

    @Override
    public GameFragment getFragment() {
        Universal fragment = new Universal();
        fragment.setSettings(this);
        return fragment;
    }
}
