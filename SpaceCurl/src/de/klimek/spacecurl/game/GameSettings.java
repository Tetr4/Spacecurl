
package de.klimek.spacecurl.game;

public abstract class GameSettings {
    private String mTitle;
    private String mInstructions = "";

    public abstract GameFragment getFragment();

    public GameSettings(String title) {
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
}
