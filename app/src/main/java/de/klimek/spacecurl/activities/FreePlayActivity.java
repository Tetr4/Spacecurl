
package de.klimek.spacecurl.activities;

import android.app.ActionBar;
import android.app.ActionBar.OnNavigationListener;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;

import java.util.ArrayList;
import java.util.List;

import de.klimek.spacecurl.Database;
import de.klimek.spacecurl.R;
import de.klimek.spacecurl.game.GameDescription;
import de.klimek.spacecurl.util.collection.Training;

/**
 * Enables free selection of games with a navigation {@link Spinner}.
 * 
 * @author Mike Klimek
 */
public class FreePlayActivity extends BasicTrainingActivity implements OnClickListener {
    public static final String TAG = FreePlayActivity.class.getName();
    protected static final String STATE_ACTIONBAR_SELECTED_ITEM = "STATE_ACTIONBAR_SELECTED_ITEM";

    private ActionBar mActionBar;
    private Training mFreeplayGames;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Database database = Database.getInstance(this);
        mFreeplayGames = database.getFreeplayGames();
        loadTraining(mFreeplayGames);

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
        mActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);

        // Adapter to fill spinner with items
        List<String> gameTitles = new ArrayList<String>();
        for (GameDescription curGame : mFreeplayGames) {
            gameTitles.add(curGame.getTitle());
        }
        SpinnerAdapter spinnerAdapter = new ArrayAdapter<String>(this, R.layout.list_item_spinner,
                gameTitles);

        // Listener which calls switchToGame(position) when an item is selected
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.freeplay, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_new_training:
                finish();
                break;

            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt(STATE_ACTIONBAR_SELECTED_ITEM,
                mActionBar.getSelectedNavigationIndex());
    }

    @Override
    public void onGameFinished(String highScore) {
        // do nothing
    }

    @Override
    public void onStatusChanged(float status) {
        // do nothing
    }
}
