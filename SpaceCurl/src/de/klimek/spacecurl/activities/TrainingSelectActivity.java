
package de.klimek.spacecurl.activities;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
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
import android.widget.Toast;
import de.klimek.spacecurl.Database;
import de.klimek.spacecurl.R;
import de.klimek.spacecurl.preferences.SettingsActivity;
import de.klimek.spacecurl.util.collection.Training;

public class TrainingSelectActivity extends FragmentActivity {
    private boolean mDoubleBackToExitPressedOnce;
    private Database mDatabase;
    private ListView mListView;
    private TrainingArrayAdapter mArrayAdapter;
    private List<Training> mTrainings = new ArrayList<Training>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDatabase = Database.getInstance(this);
        setContentView(R.layout.activity_training_select);
        // getActionBar().setDisplayHomeAsUpEnabled(true);
        mTrainings = mDatabase.getTrainings();
        setupListView();
        setupAddButton();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mDatabase.isOrientationLandscape()) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
    }

    // Intent intent = new Intent(TrainingSelectActivity.this,
    // SettingsActivity.class);

    @Override
    protected void onRestart() {
        super.onRestart();
        if (mArrayAdapter != null) {
            mArrayAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onBackPressed() {
        if (mDoubleBackToExitPressedOnce) {
            super.onBackPressed();
            return;
        }
        mDoubleBackToExitPressedOnce = true;
        Toast.makeText(this, getResources().getString(R.string.click_back_again),
                Toast.LENGTH_SHORT)
                .show();
        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                mDoubleBackToExitPressedOnce = false;

            }
        }, 2000);
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

        // Freeplay item
        View freePlayItem = getLayoutInflater()
                .inflate(R.layout.list_item_freeplay, mListView, false);
        freePlayItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // start Freeplay
                Intent intent = new Intent(TrainingSelectActivity.this,
                        FreePlayActivity.class);
                startActivity(intent);
            }
        });
        // FIXME Settings for Freeplay instead of general settings
        View editbutton =
                freePlayItem.findViewById(R.id.freeplay_edit_button);
        editbutton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(TrainingSelectActivity.this,
                        SettingsActivity.class);
                startActivity(intent);
            }
        });
        mListView.addHeaderView(freePlayItem, null, false);

        // trainings
        mArrayAdapter = new TrainingArrayAdapter(this, R.layout.list_item_training, mTrainings);
        mListView.setAdapter(mArrayAdapter);

        // list interaction
        mListView.setClickable(true);
        mListView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                    int position, long id) {
                startTraining(mTrainings.get(position - 1));
            }
        });
    }

    private void startTraining(Training training) {
        int key = mTrainings.indexOf(training);
        Intent intent = new Intent(this, TrainingActivity.class);
        intent.putExtra(TrainingActivity.EXTRA_TRAINING_INDEX, key);
        startActivity(intent);
    }

    private void editTraining(Training training) {
        int key = mTrainings.indexOf(training);
        Intent intent = new Intent(this, TrainingBuilderActivity.class);
        intent.putExtra(TrainingActivity.EXTRA_TRAINING_INDEX, key);
        startActivity(intent);
    }

    private void addTraining() {
        Intent intent = new Intent(this, TrainingBuilderActivity.class);
        intent.putExtra(TrainingActivity.EXTRA_TRAINING_INDEX, TrainingBuilderActivity.NEW_TRAINING);
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

            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) context
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.list_item_training, parent, false);
            }
            // create view from training
            TextView trainingNameTextView = (TextView) convertView.findViewById(R.id.training_name);
            ImageButton editButton = (ImageButton) convertView
                    .findViewById(R.id.training_edit_button);
            editButton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    editTraining(training);
                }
            });
            trainingNameTextView.setText(training.getTitle());
            return convertView;
        }
    }
}
