
package de.klimek.spacecurl.game.universal3D;

import java.util.ArrayList;

import de.klimek.spacecurl.game.GameFragment;
import de.klimek.spacecurl.game.GameSettings;

public class Universal3DSettings extends GameSettings {
    private int mDrawableResId;
    private ArrayList<Target> mTargets = new ArrayList<Target>();

    public Universal3DSettings(String title, int drawableResId) {
        super(title);
        mDrawableResId = drawableResId;
    }

    public void addTarget(Target target) {
        mTargets.add(target);
    }

    public ArrayList<Target> getTargets() {
        return mTargets;
    }

    @Override
    public GameFragment getFragment() {
        Universal3D fragment = new Universal3D();
        fragment.setSettings(this);
        return fragment;
    }

    public int getDrawableResId() {
        return mDrawableResId;
    }

    public void setDrawableResId(int drawableResId) {
        mDrawableResId = drawableResId;
    }

}
