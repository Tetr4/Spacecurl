
package de.klimek.spacecurl;

import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.internal.CardArrayAdapter;
import it.gmariotti.cardslib.library.view.CardListView;

import java.util.ArrayList;
import java.util.List;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.FrameLayout;

import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import de.klimek.spacecurl.game.GameFragment;
import de.klimek.spacecurl.game.GameSettings;
import de.klimek.spacecurl.training.TrainingSelectActivity;
import de.klimek.spacecurl.util.StatusCard;
import de.klimek.spacecurl.util.collection.Database;
import de.klimek.spacecurl.util.collection.status.GameStatus;
import de.klimek.spacecurl.util.collection.status.TrainingStatus;
import de.klimek.spacecurl.util.collection.training.Training;

/**
 * This program is an App for the Android OS 4.4, intended to provide
 * information and visual support while training on the SpaceCurl.
 * 
 * @author Mike Klimek
 * @see <a href="http://developer.android.com/reference/packages.html">Android
 *      API</a>
 */
public abstract class MainActivityPrototype extends FragmentActivity implements OnClickListener {
    // Used for log output
    private static final String TAG = MainActivityPrototype.class.getName();

    public final static String EXTRA_TRAINING_KEY = "EXTRA_TRAINING";

    private Database mDatabase;

    private FrameLayout mStatusIndicator;
    private SlidingUpPanelLayout mSlidingUpPanel;
    private CardListView mCardListView;
    private CardArrayAdapter mCardArrayAdapter;

    private Training mTraining;
    private List<Card> mCards = new ArrayList<Card>();
    private TrainingStatus mStatus;
    private GameStatus mCurGameStatus;

    private GameFragment mGameFragment;

    private FrameLayout mPauseFrame;
    private State mState = State.Running;

    public static enum State {
        Paused, Pausing, Running
    }

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
        // Log.d(TAG, mDatabase.getTrainings().get(0).getTitle());
        setContentView(R.layout.activity_main);
        setupSettings();
        setupPauseView();
    }

    /**
     * Initialises Settings
     * 
     * @see <a href
     *      ="http://developer.android.com/guide/topics/ui/settings.html">Settings
     *      Guide</a>
     */
    private void setupSettings() {
        // Default setting values on first startup
        // PreferenceManager.setDefaultValues(this, R.xml.prefersences, false);
        // SharedPreferences sharedPref =
        // PreferenceManager.getDefaultSharedPreferences(this);
        if (mDatabase.isOrientationLandscape()) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
    }

    private void setupPauseView() {
        mPauseFrame = (FrameLayout) findViewById(R.id.pause_layout);
        FrameLayout gameFrame = (FrameLayout) findViewById(R.id.game_frame);
        gameFrame.setOnClickListener(this);
    }

    protected void expandSlidingPane() {
        if (mSlidingUpPanel != null) {
            pauseGame();
            mSlidingUpPanel.expandPane();
        }
    }

    protected void hideSlidingPane() {
        if (mSlidingUpPanel == null) {
            mSlidingUpPanel = (SlidingUpPanelLayout) findViewById(R.id.content_frame);
        }
        mSlidingUpPanel.setPanelHeight(-1);
        return;
    }

    protected void useStatusForTraining(int key) {
        mTraining = mDatabase.getTrainings().get(key);
        mStatus = new TrainingStatus(key);
        mDatabase.getStatuses().add(mStatus);

        // setup panel
        mSlidingUpPanel = (SlidingUpPanelLayout) findViewById(R.id.content_frame);
        int statusIndicatorHeight = (int) (getResources()
                .getDimension(R.dimen.status_indicator_height));
        int padding = 0;
        mSlidingUpPanel.setPanelHeight(statusIndicatorHeight + padding);
        mSlidingUpPanel.setShadowDrawable(getResources().getDrawable(R.drawable.above_shadow));

        // setup indicator
        mStatusIndicator = (FrameLayout) findViewById(R.id.status_indicator);

        // setup cardlist
        mCardArrayAdapter = new CardArrayAdapter(this, mCards);
        mCardListView = (CardListView) findViewById(R.id.card_list);
        mCardListView.setAdapter(mCardArrayAdapter);
    }

    protected void switchStatus(int index) {
        if (index >= mStatus.size()) {
            mCurGameStatus = new GameStatus(mTraining.get(index).getTitle());
            mStatus.add(mCurGameStatus);
        } else {
            mCurGameStatus = mStatus.get(index);
        }
        if (mStatus != null) {
            mCards.clear();
            for (GameStatus gameStatus : mStatus) {
                mCards.add(new StatusCard(this, gameStatus));
            }
            mCardArrayAdapter.notifyDataSetChanged();
        }
    }

    protected void onStatusChanged(float status) {
        mCurGameStatus.addStatus(status);
        int statusColor;
        if (status <= 0.5f) {
            statusColor = interpolateColor(Color.RED, Color.YELLOW, status * 2.0f);
        } else {
            statusColor = interpolateColor(Color.YELLOW, Color.GREEN, (status - 0.5f) * 2.0f);
        }
        Log.d(TAG, Integer.toString(statusColor, 16));
        mStatusIndicator.setBackgroundColor(statusColor);
    }

    private static int interpolateColor(int color1, int color2, float fraction) {
        fraction = Math.min(fraction, 1.0f);
        fraction = Math.max(fraction, 0.0f);

        int deltaAlpha = Color.alpha(color2) - Color.alpha(color1);
        int deltaRed = Color.red(color2) - Color.red(color1);
        int deltaGreen = Color.green(color2) - Color.green(color1);
        int deltaBlue = Color.blue(color2) - Color.blue(color1);

        int resultAlpha = (int) (Color.alpha(color1) + (deltaAlpha * fraction));
        int resultRed = (int) (Color.red(color1) + (deltaRed * fraction));
        int resultGreen = (int) (Color.green(color1) + (deltaGreen * fraction));
        int resultBlue = (int) (Color.blue(color1) + (deltaBlue * fraction));

        resultAlpha = Math.max(Math.min(resultAlpha, 255), 0);
        resultRed = Math.max(Math.min(resultRed, 255), 0);
        resultGreen = Math.max(Math.min(resultGreen, 255), 0);
        resultBlue = Math.max(Math.min(resultBlue, 255), 0);

        return Color.argb(resultAlpha, resultRed, resultGreen, resultBlue);
    }

    /**
     * Switches the GameFragment and registers the StatusFragment as an
     * onStatusChangedListener. Hides status depending on
     * GameFragment.usesStatus()
     * 
     * @param position position of the item in the spinner
     */
    protected void switchToGame(GameSettings settings) {
        if (mGameFragment != null) {
            mGameFragment.setState(State.Paused);
        }
        // GameFragment:
        GameFragment newGameFragment;
        try {
            // instantiate Fragment from Class
            newGameFragment = (GameFragment) settings.getGameClass().newInstance();
            newGameFragment.setSettings(settings);
        }
        // no multicatch in dalvik (~ java 1.6)
        catch (Exception e) {
            // App not operable
            throw new RuntimeException(e.getMessage());
        }

        // Transaction
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.game_frame, newGameFragment).commit();

        // previous GameFragment will be garbage collected
        mGameFragment = newGameFragment;
        mState = State.Running;
        resumeGame();

        // Actionbar title and icons
        // mTitle = mGameSettingsPair.getSettings().getString(
        // GameFragment.ARG_TITLE);

        onGameSwitched(mGameFragment);
    }

    protected abstract void onGameSwitched(GameFragment gameFragment);

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

                // NotificationManager nm =
                // (NotificationManager)getSystemService( NOTIFICATION_SERVICE);
                // Notification notif = new Notification();
                // notif.ledARGB = 0xFFff0000;
                // notif.flags = Notification.FLAG_SHOW_LIGHTS;
                // notif.ledOnMS = 100;
                // notif.ledOffMS = 100;
                // nm.notify(123421234, notif);
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mState == State.Running) {
            mState = State.Paused;
            pauseGame();
        }
    }

    @Override
    public void onUserInteraction() {
        if (mState == State.Running) {
            mState = State.Pausing;
            pauseGame();
        } else if (mState == State.Pausing) {
            mState = State.Paused;
        }
        super.onUserInteraction();
    }

    /**
     * Called after onUserInteraction
     */
    @Override
    public void onClick(View v) {
        if (mState == State.Paused) {
            resumeGame();
            mState = State.Running;
        } else if (mState == State.Pausing) {
            // we have just paused in onUserInteraction, don't resume
            mState = State.Paused;
        }
    }

    private void pauseGame() {
        Log.v(TAG, "Paused");
        if (mGameFragment != null) {
            mGameFragment.setState(State.Paused);
        }
        // Show pause symbol and grey out screen
        mPauseFrame.setVisibility(View.VISIBLE);
        mPauseFrame.bringToFront();
        // Show navigation bar
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
    }

    private void resumeGame() {
        Log.v(TAG, "Resumed");
        mPauseFrame.setVisibility(View.INVISIBLE);
        if (mGameFragment != null) {
            mGameFragment.setState(State.Running);
        }
        // Grey out navigation bar
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);
    }

}
