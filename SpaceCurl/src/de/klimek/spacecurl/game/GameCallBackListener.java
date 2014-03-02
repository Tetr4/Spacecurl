
package de.klimek.spacecurl.game;

public interface GameCallBackListener {
    public void onGameFinished(String highScore);

    public void onStatusChanged(float status);
}
