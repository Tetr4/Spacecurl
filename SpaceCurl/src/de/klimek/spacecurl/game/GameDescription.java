
package de.klimek.spacecurl.game;

/**
 * Abstract class which contains game infos and settings, like title,
 * instructions, effects, ... <br>
 * Subclasses can add additional infos/settings (like number of lives,
 * difficulty, ...) and have to provide a GameFragment. Instances can be created
 * by the user with the TrainingBuilder or at start by querying the database.
 * Fragments can be created from an instance to play the game.
 * 
 * @author mike
 */
public abstract class GameDescription {
    private String mTitle;
    private String mInstructions = "";
    private Effect[] mEffects = {};
    private int mFreeAxisCount = 1;

    public static enum Effect {
        Accuracy, Speed, Strength, Endurance
    }

    protected abstract GameFragment createFragment();

    public GameFragment getFragment() {
        GameFragment fragment = createFragment();
        fragment.setGameDescription(this);
        return fragment;
    }

    public GameDescription(String title) {
        mTitle = title;
    }

    public void setTitle(String title) {
        mTitle = title;
    };

    public String getTitle() {
        return mTitle;
    };

    public void setInstructions(String instructions) {
        mInstructions = instructions;
    }

    public String getInstructions() {
        return mInstructions;
    }

    public void setEffects(Effect[] effects) {
        mEffects = effects;
    }

    public Effect[] getEffects() {
        return mEffects;
    }

    public void setFreeAxisCount(int freeAxisCount) {
        mFreeAxisCount = freeAxisCount;
    }

    public int getFreeAxisCount() {
        return mFreeAxisCount;
    }
}
