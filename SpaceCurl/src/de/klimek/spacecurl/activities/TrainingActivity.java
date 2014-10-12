
package de.klimek.spacecurl.activities;

import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View.OnClickListener;
import android.widget.Toast;
import de.klimek.spacecurl.Database;
import de.klimek.spacecurl.R;
import de.klimek.spacecurl.util.collection.Training;

/**
 * Automatically switched to next game after the previous has finished. Enables
 * skipping to previous/next game with actionbar icons. After the training has
 * finished the status panel is expanded and locked.The training's database
 * index must be given as an extra (EXTRA_TRAINING_INDEX) when starting the
 * activity.
 * 
 * @author Mike Klimek
 */
public class TrainingActivity extends BasicTrainingActivity implements OnClickListener {
    private static final String TAG = TrainingActivity.class.getSimpleName();
    public final static String EXTRA_TRAINING_INDEX = "EXTRA_TRAINING_INDEX";

    private Training mTraining;
    private int mCurGameDescriptionIndex = -1;
    private boolean mTrainingHasEnded = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Database database = Database.getInstance(this);
        int trainingIndex = getIntent().getIntExtra(EXTRA_TRAINING_INDEX, -1);
        mTraining = database.getTrainings().get(trainingIndex);
        loadTraining(mTraining);

        useStatusPanel();

        nextGame();
    }

    /**
     * Called e.g. when game has finished, or is skipped by user
     */
    private void nextGame() {
        Log.i(TAG, "nextGame");
        // check if next Game exists
        if (mCurGameDescriptionIndex + 1 < mTraining.size()) {
            ++mCurGameDescriptionIndex;
            switchToGame(mCurGameDescriptionIndex, R.anim.slide_in_right, R.anim.slide_out_left);
            updateActionbar();
        } else {
            // training finished
            expandSlidingPane();
            lockSlidingPane();
            hideStatusIndicator();
            mTrainingHasEnded = true;
            updateActionbar();
        }
    }

    /**
     * User skips back to previous game
     */
    private void previousGame() {
        Log.i(TAG, "previousGame");
        if (mCurGameDescriptionIndex - 1 >= 0) {
            --mCurGameDescriptionIndex;
            switchToGame(mCurGameDescriptionIndex, android.R.anim.slide_in_left,
                    android.R.anim.slide_out_right);
            updateActionbar();
        }
    }

    @Override
    public void onStatusChanged(float status) {
        updateCurGameStatus(status);
    }

    @Override
    public void onGameFinished(String highScore) {
        postHighScore(highScore);
        nextGame();
    }

    private void postHighScore(String highScore) {
        // TODO Bigger Notification
        for (int i = 0; i < 2; i++) { // double duration
            Toast toast = Toast.makeText(getApplicationContext(), highScore,
                    Toast.LENGTH_LONG);
            toast.setGravity(Gravity.TOP, 0, getActionBar().getHeight() + 32);
            toast.show();
        }
    }

    /**
     * Displays the training's name, number of the current game and total games.
     * Also updates the previous/next icon.
     */
    private void updateActionbar() {
        String title = mTraining.getTitle();
        // "Balance (2/8)"
        getActionBar().setTitle(
                title + " (" + (mCurGameDescriptionIndex + 1) + "/" + mTraining.size() + ")"
                );
        invalidateOptionsMenu();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.training, menu);
        MenuItem itemShowStatus = menu.findItem(R.id.action_show_status);
        itemShowStatus.setVisible(!mTrainingHasEnded); // hide if ended
        MenuItem itemNext = menu.findItem(R.id.action_next_game);
        itemNext.setVisible(mCurGameDescriptionIndex + 1 < mTraining.size() && !mTrainingHasEnded);
        MenuItem itemPrevious = menu.findItem(R.id.action_previous_game);
        itemPrevious.setVisible(mCurGameDescriptionIndex > 0 && !mTrainingHasEnded);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_new_training:
                finish();
                break;
            case R.id.action_previous_game:
                previousGame();
                break;
            case R.id.action_next_game:
                nextGame();
                break;
            case R.id.action_show_status:
                expandSlidingPane();
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

}
