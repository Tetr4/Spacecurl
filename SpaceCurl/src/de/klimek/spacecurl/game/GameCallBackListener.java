
package de.klimek.spacecurl.game;

import de.klimek.spacecurl.util.collection.Status;

public interface GameCallBackListener {
    public void onGameFinished();

    public void onStatusChanged(Status status);
}
