
package de.klimek.spacecurl.game.tunnel;

import de.klimek.spacecurl.game.GameFragment;
import de.klimek.spacecurl.game.GameSettings;

public class TunnelSettings extends GameSettings {

    @Override
    public Class<? extends GameFragment> getGameClass() {
        return Tunnel.class;
    }

    public void setTunnelHeight(float height) {
    }

}
