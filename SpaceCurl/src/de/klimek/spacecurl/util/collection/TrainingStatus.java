
package de.klimek.spacecurl.util.collection;

import android.util.SparseArray;

public class TrainingStatus extends SparseArray<GameStatus> {
    private String mTitle = "";
    private float mScore = 0.0f;
    private int mTrainingKey;

    public TrainingStatus(int trainingKey) {
        mTrainingKey = trainingKey;
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String title) {
        mTitle = title;
    }

    public float getScore() {
        return mScore;
    }

    public void setScore(float score) {
        mScore = score;
    }

    public int getTrainingKey() {
        return mTrainingKey;
    }
}
