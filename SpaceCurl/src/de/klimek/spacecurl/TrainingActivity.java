
package de.klimek.spacecurl;

import android.app.ActionBar;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View.OnClickListener;
import de.klimek.spacecurl.game.GameCallBackListener;
import de.klimek.spacecurl.game.GameFragment;
import de.klimek.spacecurl.game.GameSettings;
import de.klimek.spacecurl.training.TrainingSelectActivity;
import de.klimek.spacecurl.util.collection.Database;
import de.klimek.spacecurl.util.collection.training.Training;

/**
 * This program is an App for the Android OS 4.4, intended to provide
 * information and visual support while training on the SpaceCurl.
 * 
 * @author Mike Klimek
 * @see <a href="http://developer.android.com/reference/packages.html">Android
 *      API</a>
 */
public class TrainingActivity extends MainActivityPrototype implements OnClickListener,
        GameCallBackListener {
    private static final String TAG = "TrainingActivity"; // Used for log output
    protected static final String STATE_CURRENT_TRAINING = "STATE_CURRENT_TRAINING";
    protected static final String STATE_CURRENT_GAME = "STATE_CURRENT_GAME";
    public final static String EXTRA_TRAINING_KEY = "EXTRA_TRAINING";

    private Database mDatabase;
    private ActionBar mActionBar;
    private String mTitle = "";

    private Training mTraining;
    private int mTrainingIndex = -1;
    private GameSettings mGameSettings;

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
        mDatabase = Database.getInstance(this);
        int key = getIntent().getIntExtra(EXTRA_TRAINING_KEY, 0);

        mTraining = mDatabase.getTrainings().get(key);

        useStatusForTraining(key);

        setupActionbar();
        nextGame();
    }

    private void setupActionbar() {
        mActionBar = getActionBar();
        mActionBar.setTitle(mTitle);
        mActionBar.setDisplayShowTitleEnabled(true);
    }

    private void previousGame() {
        Log.v(TAG, "previousGame");
        if (mTrainingIndex - 1 >= 0) {
            --mTrainingIndex;
            mGameSettings = mTraining.get(mTrainingIndex);
            switchStatus(mTrainingIndex);
            switchToGame(mGameSettings);
        }
    }

    private void nextGame() {
        Log.v(TAG, "nextGame");
        if (mTrainingIndex + 1 < mTraining.size()) {
            ++mTrainingIndex;
            mGameSettings = mTraining.get(mTrainingIndex);
            switchStatus(mTrainingIndex);
            switchToGame(mGameSettings);
        } else {
            expandSlidingPane();
        }
    }

    @Override
    public void onStatusChanged(float status) {
        super.doStatusChanged(status);
    }

    @Override
    public void onGameFinished() {
        nextGame();
    }

    @Override
    protected void onGameSwitched(GameFragment gameFragment) {
        mTitle = mTraining.getTitle();
        mActionBar.setTitle(
                mTitle + " (" + (mTrainingIndex + 1) + "/" + mTraining.size() + ")"
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
        MenuItem itemNext = menu.findItem(R.id.action_next_game);
        itemNext.setVisible(mTrainingIndex + 1 < mTraining.size());
        MenuItem itemPrevious = menu.findItem(R.id.action_previous_game);
        itemPrevious.setVisible(mTrainingIndex > 0);
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
