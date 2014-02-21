
package de.klimek.spacecurl.training;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import de.klimek.spacecurl.R;
import de.klimek.spacecurl.TrainingActivity;
import de.klimek.spacecurl.util.collection.Database;
import de.klimek.spacecurl.util.collection.training.Training;

public class TrainingSelectActivity extends FragmentActivity {
    private Database mDatabase;
    private ListView mListView;
    private TrainingArrayAdapter mArrayAdapter;
    private List<Training> mTrainings = new ArrayList<Training>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDatabase = Database.getInstance(this);
        if (mDatabase.isOrientationLandscape()) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
        setContentView(R.layout.activity_training_select);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        setupAddButton();
        mTrainings = Database.getInstance().getTrainings();
        setupListView();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        if (mArrayAdapter != null) {
            mArrayAdapter.notifyDataSetChanged();
        }
    }

    private void setupAddButton() {
        ImageButton btn = (ImageButton) findViewById(R.id.training_add_button);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addTraining();
            }
        });
    }

    private void setupListView() {
        mListView = (ListView) findViewById(R.id.training_list);
        // add footer (so nothing is behind add_button_bar when scrolled down)
        View footerItem = getLayoutInflater()
                .inflate(R.layout.list_item_training_footer, mListView, false);
        mListView.addFooterView(footerItem, null, false);

        mListView.setClickable(true);
        mListView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                    int position, long id) {
                editTraining(mTrainings.get(position));
            }
        });
        mArrayAdapter = new TrainingArrayAdapter(this, R.layout.list_item_training, mTrainings);
        mListView.setAdapter(mArrayAdapter);
    }

    private void startTraining(Training training) {
        int key = mTrainings.indexOf(training);
        Intent intent = new Intent(this, TrainingActivity.class);
        intent.putExtra(TrainingActivity.EXTRA_TRAINING_KEY, key);
        startActivity(intent);
    }

    private void editTraining(Training training) {
        int key = mTrainings.indexOf(training);
        Intent intent = new Intent(this, TrainingBuilderActivity.class);
        intent.putExtra(TrainingActivity.EXTRA_TRAINING_KEY, key);
        startActivity(intent);
    }

    private void addTraining() {
        Intent intent = new Intent(this, TrainingBuilderActivity.class);
        intent.putExtra(TrainingActivity.EXTRA_TRAINING_KEY, TrainingBuilderActivity.NEW_TRAINING);
        startActivity(intent);
    }

    public class TrainingArrayAdapter extends ArrayAdapter<Training> {
        private final Context context;
        private final List<Training> trainings;

        public TrainingArrayAdapter(Context context, int layoutRessource, List<Training> trainings) {
            super(context, layoutRessource, trainings);
            this.context = context;
            this.trainings = trainings;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // get training
            final Training training = trainings.get(position);

            // create view from training
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View listItemView = inflater.inflate(R.layout.list_item_training, parent, false);
            // listItemView.setOnClickListener(new OnClickListener() {
            // @Override
            // public void onClick(View v) {
            // editTraining(training);
            // }
            // });
            TextView trainingName = (TextView) listItemView.findViewById(R.id.training_name);
            ImageButton playButton = (ImageButton) listItemView
                    .findViewById(R.id.training_play_button);
            playButton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    startTraining(training);
                }
            });
            trainingName.setText(training.getTitle());
            return listItemView;
        }

    }
}
