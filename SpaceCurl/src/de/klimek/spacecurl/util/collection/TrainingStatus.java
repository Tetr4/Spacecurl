
package de.klimek.spacecurl.util.collection;

import android.util.SparseArray;

/**
 * Represents a <b>started</b> training's status. Contains {@link GameStatus
 * GameStatus'} which represent a status for every game in a running training.
 * Also contains a overall score for the training. Is a {@link SparseArray}
 * (allows gaps in indices, similar to {@code HashMap<Integer, GameStatus>}) for
 * creation of GameStatus in arbitrary order, since Games can be started out of
 * order.
 * 
 * @author Mike Klimek
 */
public class TrainingStatus extends SparseArray<GameStatus> {
    private float mScore = 0.0f;

    public TrainingStatus() {
    }

    public float getScore() {
        return mScore;
    }

    public void setScore(float score) {
        mScore = score;
    }

}
