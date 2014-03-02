
package de.klimek.spacecurl.game.universal3D;

import de.klimek.spacecurl.game.GameFragment;
import de.klimek.spacecurl.game.GameSettings;

public class Universal3DSettings extends GameSettings {

    // public void setMode()
    public void setTargetX() {

    }

    public void setTargetY() {

    }

    @Override
    public GameFragment getFragment() {
        Universal3D fragment = new Universal3D();
        fragment.setSettings(this);
        return fragment;
    }

}
