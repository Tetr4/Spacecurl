
package de.klimek.spacecurl.util.collection;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import de.klimek.spacecurl.game.GameDescription;

/**
 * List Collection which represents a Training description. Contains a
 * {@link GameDescription} for every game and a title. Also contains a List of
 * saved {@link TrainingStatus TrainingStatuses} for every execution of the
 * Training.
 * 
 * @author Mike Klimek
 */
public class Training extends ArrayList<GameDescription> {
    private static final long serialVersionUID = -4346310670231953747L;
    private String mTitle;

    private List<TrainingStatus> mSavedTrainingStatuses = new LinkedList<TrainingStatus>();

    public Training(String title) {
        setTitle(title);
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String title) {
        mTitle = title;
    }

    public TrainingStatus createTrainingStatus() {
        TrainingStatus trainingStatus = new TrainingStatus();
        mSavedTrainingStatuses.add(trainingStatus);
        return trainingStatus;
    }

}
