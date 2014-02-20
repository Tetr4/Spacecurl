
package de.klimek.spacecurl.game;

public abstract class GameSettings {
    private String mTitle;

    // public GameSettings(String title) {
    // mTitle = title;
    // }

    public abstract Class<? extends GameFragment> getGameClass();

    public void setTitle(String title) {
        mTitle = title;
    };

    public String getTitle() {
        return mTitle;
    };
}
