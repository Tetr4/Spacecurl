
package de.klimek.spacecurl.game.tunnel;

import de.klimek.spacecurl.game.GameFragment;
import de.klimek.spacecurl.game.GameSettings;

public class TunnelSettings extends GameSettings {

    public void setTunnelHeight(float height) {
    }

    @Override
    public GameFragment getFragment() {
        Tunnel fragment = new Tunnel();
        fragment.setSettings(this);
        return fragment;
    }

}
