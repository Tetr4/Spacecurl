
package de.klimek.spacecurl.util.cards;

import it.gmariotti.cardslib.library.internal.Card;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import de.klimek.spacecurl.R;
import de.klimek.spacecurl.util.collection.Training;

public class TrainingCard extends Card {
    private TextView mTrainingTitleTextView;
    private String mTrainingTitle;
    private Training mTraining;

    public TrainingCard(Context context, Training training) {
        super(context, R.layout.list_item_training);
        setTraining(training);
    }

    @Override
    public void setupInnerViewElements(ViewGroup parent, View view) {
        mTrainingTitleTextView = (TextView) parent.findViewById(
                R.id.training_name);
        mTrainingTitleTextView.setText(mTrainingTitle);
    }

    public Training getTraining() {
        return mTraining;
    }

    public void setTraining(Training training) {
        mTraining = training;
    }

}
