
package de.klimek.spacecurl;

import java.util.ArrayList;
import java.util.List;

import android.app.ActionBar;
import android.app.ActionBar.OnNavigationListener;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import de.klimek.spacecurl.game.GameDescription;
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
public class FreePlayActivity extends BasicTrainingActivity implements OnClickListener {
    public static final String TAG = FreePlayActivity.class.getName();
    protected static final String STATE_ACTIONBAR_SELECTED_ITEM = "STATE_ACTIONBAR_SELECTED_ITEM";

    private ActionBar mActionBar;
    private Training mFreeplayGames;

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

        Database database = Database.getInstance(this);
        mFreeplayGames = database.getFreeplayGames();

        loadTraining(mFreeplayGames);
        hideSlidingPane();

        setupActionbar(savedInstanceState);

    }

    /**
     * Creates the {@link ActionBar} and the {@link Spinner} and enables its
     * functionality
     * 
     * @see <a
     *      href="http://developer.android.com/design/patterns/actionbar.html">
     *      ActionBar Design</a>
     * @see <a
     *      href="http://developer.android.com/design/building-blocks/spinners.html">
     *      Spinner Design</a>
     */
    private void setupActionbar(Bundle savedInstanceState) {
        mActionBar = getActionBar();
        mActionBar.setDisplayShowTitleEnabled(false);
        mActionBar.setDisplayShowHomeEnabled(true); // "up-caret"
        mActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);

        // Adapter to fill spinner with items
        List<String> gameTitles = new ArrayList<String>();
        for (GameDescription curGame : mFreeplayGames) {
            gameTitles.add(curGame.getTitle());
        }
        SpinnerAdapter spinnerAdapter = new ArrayAdapter<String>(this, R.layout.list_item_spinner,
                gameTitles);
        // Listener which calls selectSpinnerItem(pos) when an item is selected
        OnNavigationListener onNavigationListener = new OnNavigationListener() {
            @Override
            public boolean onNavigationItemSelected(int position, long itemId) {
                switchToGame(position);
                return true;
            }
        };
        mActionBar.setListNavigationCallbacks(spinnerAdapter,
                onNavigationListener);

        if (savedInstanceState == null) {
            // Select first game on new start
            mActionBar.setSelectedNavigationItem(0);
        }
        else {
            // select saved game on restart
            mActionBar.setSelectedNavigationItem(
                    savedInstanceState.getInt(STATE_ACTIONBAR_SELECTED_ITEM));
        }
    }

    /**
     * Called by OS the first time the options menu (icons and overflow menu
     * [three vertical dots] in the ActionBar) is displayed.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.freeplay, menu);
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
        // case R.id.action_settings:
        // startActivity(new Intent(this, SettingsActivity.class));
        // break;
        // case R.id.action_about:
        // Toast.makeText(this, R.string.action_about,
        // Toast.LENGTH_LONG)
        // .show();
        // break;
            case R.id.action_new_training:
                startActivity(new Intent(this, TrainingSelectActivity.class));
                break;
            // case R.id.action_help:
            // break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    /**
     * Called by OS when the App will be destroyed to (e.g. to free Memory), but
     * may be opened again.
     * 
     * @param outState a {@link Bundle} containing key-Value Pairs to be saved
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt(STATE_ACTIONBAR_SELECTED_ITEM,
                mActionBar.getSelectedNavigationIndex());
    }
}
