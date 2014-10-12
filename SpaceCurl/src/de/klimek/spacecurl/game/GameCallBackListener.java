
package de.klimek.spacecurl.game;

/**
 * Used for Callbacks between a GameFragment and its Activity
 * 
 * @author Mike Klimek
 */
public interface GameCallBackListener {
    public void onGameFinished(String highScore);

    public void onStatusChanged(float status);
}
