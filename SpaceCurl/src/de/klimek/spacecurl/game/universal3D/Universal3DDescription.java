
package de.klimek.spacecurl.game.universal3D;

import java.util.ArrayList;

import de.klimek.spacecurl.game.GameDescription;
import de.klimek.spacecurl.game.GameFragment;

public class Universal3DDescription extends GameDescription {
    private int mDrawableResId;
    private ArrayList<Target> mTargets = new ArrayList<Target>();

    public Universal3DDescription(String title, int drawableResId) {
        super(title);
        mDrawableResId = drawableResId;
        setFreeAxisCount(3);
        Effect[] effects = {
                Effect.Accuracy,
                Effect.Endurance
        };
        setEffects(effects);
    }

    public void addTarget(Target target) {
        mTargets.add(target);
    }

    public ArrayList<Target> getTargets() {
        return mTargets;
    }

    public int getDrawableResId() {
        return mDrawableResId;
    }

    public void setDrawableResId(int drawableResId) {
        mDrawableResId = drawableResId;
    }

    @Override
    protected GameFragment createFragment() {
        return new Universal3D();
    }

}
