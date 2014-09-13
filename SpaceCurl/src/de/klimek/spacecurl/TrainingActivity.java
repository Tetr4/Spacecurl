
package de.klimek.spacecurl;

import android.app.ActionBar;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View.OnClickListener;
import android.widget.Toast;
import de.klimek.spacecurl.game.GameCallBackListener;
import de.klimek.spacecurl.training.TrainingSelectActivity;
import de.klimek.spacecurl.util.collection.training.Training;

/**
 * This program is an App for the Android OS 4.4, intended to provide
 * information and visual support while training on the SpaceCurl.
 * 
 * @author Mike Klimek
 * @see <a href="http://developer.android.com/reference/packages.html">Android
 *      API</a>
 */
public class TrainingActivity extends BasicTrainingActivity implements OnClickListener,
        GameCallBackListener {
    private static final String TAG = BasicTrainingActivity.class.getSimpleName();
    protected static final String STATE_CURRENT_TRAINING = "STATE_CURRENT_TRAINING";
    protected static final String STATE_CURRENT_GAME = "STATE_CURRENT_GAME";
    public final static String EXTRA_TRAINING_INDEX = "EXTRA_TRAINING";

    private ActionBar mActionBar;
    private String mTitle = "";

    private Training mTraining;
    private int mCurGameDescriptionIndex = -1;
    private boolean mEnded = false;

    /**
     * Called by OS when the activity is first created.
     * 
     * @param savedInstanceState a {@link Bundle} containing key-Value Pairs if
     *            the activity is being re-initialized after previously being
     *            destroyed to free memory
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        int trainingNr = getIntent().getIntExtra(EXTRA_TRAINING_INDEX, -1);
        Database database = Database.getInstance(this);
        mTraining = database.getTrainings().get(trainingNr);
        loadTraining(mTraining);

        // enable status panel
        useStatus(trainingNr);

        setupActionbar();
        nextGame();
    }

    private void setupActionbar() {
        mActionBar = getActionBar();
        mActionBar.setTitle(mTitle);
        mActionBar.setDisplayShowTitleEnabled(true);
    }

    /**
     * Game has finished, or skipped
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
            mEnded = true;
            invalidateOptionsMenu();
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

    private void updateActionbar() {
        mTitle = mTraining.getTitle();
        mActionBar.setTitle(
                mTitle + " (" + (mCurGameDescriptionIndex + 1) + "/" + mTraining.size() + ")"
                );
        invalidateOptionsMenu();
    }

    /**
     * Called by OS the first time the options menu (icons and overflow menu
     * [three vertical dots] in the ActionBar) is displayed.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.training, menu);
        MenuItem itemShowStatus = menu.findItem(R.id.action_show_status);
        itemShowStatus.setVisible(!mEnded);
        MenuItem itemNext = menu.findItem(R.id.action_next_game);
        itemNext.setVisible(mCurGameDescriptionIndex + 1 < mTraining.size() && !mEnded);
        MenuItem itemPrevious = menu.findItem(R.id.action_previous_game);
        itemPrevious.setVisible(mCurGameDescriptionIndex > 0 && !mEnded);
        return true;
    }

    /**
     * Called by OS when an item in the ActionBar is selected
     * 
     * @param item the selected MenuItem
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_new_training:
                startActivity(new Intent(this, TrainingSelectActivity.class));
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
