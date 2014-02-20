
package de.klimek.spacecurl.game.maze;

import de.klimek.spacecurl.game.GameFragment;
import de.klimek.spacecurl.game.GameSettings;

public class MazeSettings extends GameSettings {

    @Override
    public Class<? extends GameFragment> getGameClass() {
        return Maze.class;
    }

}
