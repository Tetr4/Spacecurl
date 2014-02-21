
package de.klimek.spacecurl.util.collection.status;

import java.util.ArrayList;

public class TrainingStatus extends ArrayList<GameStatus> {
    private static final long serialVersionUID = -4374659462874669024L;
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
