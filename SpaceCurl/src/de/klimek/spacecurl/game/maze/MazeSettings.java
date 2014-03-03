
package de.klimek.spacecurl.game.maze;

import de.klimek.spacecurl.game.GameFragment;
import de.klimek.spacecurl.game.GameSettings;

public class MazeSettings extends GameSettings {

    public MazeSettings(String title) {
        super(title);
    }

    @Override
    public GameFragment getFragment() {
        Maze fragment = new Maze();
        fragment.setSettings(this);
        return fragment;
    }

}
