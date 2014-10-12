
package de.klimek.spacecurl.game.universal;

import java.util.ArrayList;

import de.klimek.spacecurl.game.GameDescription;
import de.klimek.spacecurl.game.GameFragment;

public class UniversalDescription extends GameDescription {
    public UniversalDescription(String title) {
        super(title);
    }

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
    protected GameFragment newFragment() {
        return new Universal();
    }
}
