
package de.klimek.spacecurl;

import java.util.ArrayList;
import java.util.List;

import android.app.ActionBar;
import android.app.ActionBar.OnNavigationListener;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.Toast;

import com.jjoe64.graphview.GraphView.GraphViewData;
import com.jjoe64.graphview.GraphViewSeries;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import de.klimek.spacecurl.game.GameFragment;
import de.klimek.spacecurl.game.GameLights;
import de.klimek.spacecurl.game.GameMaze;
import de.klimek.spacecurl.game.GamePong;
import de.klimek.spacecurl.game.GameSensor;
import de.klimek.spacecurl.game.GameUniversal;
import de.klimek.spacecurl.status.StatusFragment;
import de.klimek.spacecurl.training.TrainingSelectActivity;
import de.klimek.spacecurl.util.collection.GameSettingsPair;
import de.klimek.spacecurl.util.collection.Status;

/**
 * This program is an App for the Android OS 4.4, intended to provide
 * information and visual support while training on the SpaceCurl.
 * 
 * @author Mike Klimek
 * @see <a href="http://developer.android.com/reference/packages.html">Android
 *      API</a>
 */
public class MainActivity extends FragmentActivity implements OnClickListener {
    private static final String TAG = "MainActivity"; // Used for log output
    protected static final String STATE_ACTIONBAR_SELECTED_ITEM = "STATE_ACTIONBAR_SELECTED_ITEM";
    // protected static final String STATE_PAUSED = "STATE_PAUSED";

    private ActionBar mActionBar;
    private StatusFragment mStatusFragment;
    private GameFragment mGameFragment;

    // List of pairs. Each pair contains a
    // Class object and a Bundle for its Settings.
    private List<GameSettingsPair> mGames = new ArrayList<GameSettingsPair>();
    private List<String> mGameTitles = new ArrayList<String>();;

    private FrameLayout mGameFrame;
    private PauseView mPauseView;
    private boolean mIsPaused = false;
    private boolean mIsNowPausing = false;

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
        setContentView(R.layout.activity_main);

        // Add games to Spinner
        addGame(GamePong.class, R.string.game_pong);
        addGame(GameMaze.class, R.string.game_maze);
        addGame(GameLights.class, R.string.game_lights);
        addGame(GameUniversal.class, R.string.game_universal);

        Bundle sensorSettings = new Bundle();
        sensorSettings.putString(GameFragment.ARG_TITLE, "Sensor Test");
        addGame(GameSensor.class, sensorSettings);

        setupSettings();
        setupStatusFragment();
        setupPauseView();
        setupActionbar();
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

    private void addGame(Class<? extends GameFragment> gameClass, int titleResourceId) {
        Bundle settings = new Bundle();
        settings.putString(GameFragment.ARG_TITLE, getResources().getString(titleResourceId));
        addGame(gameClass, settings);
    }

    private void addGame(Class<? extends GameFragment> gameClass, Bundle settings) {
        mGames.add(new GameSettingsPair(gameClass.getName(), settings));
        String title = settings.getString(GameFragment.ARG_TITLE);
        mGameTitles.add(title);
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
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        // SharedPreferences sharedPref =
        // PreferenceManager.getDefaultSharedPreferences(this);
        // Boolean orientation = sharedPref.getBoolean("orientation", false);
        // if (orientation) {
        // setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        // }
    }

    private void setupPauseView() {
        mGameFrame = (FrameLayout) findViewById(R.id.game_frame);
        mGameFrame.setOnClickListener(this);
        mPauseView = new PauseView(this);
        mPauseView.setVisibility(View.INVISIBLE);
        mGameFrame.addView(mPauseView);
        pauseGame();
    }

    private void setupStatusFragment() {
        mStatusFragment = new StatusFragment();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.status_frame, mStatusFragment).commit();
        // Set SildingUpPanel Height to only show upper card
        int cardHeight = (int) (getResources()
                .getDimension(R.dimen.card_height));
        int padding = 12;
        SlidingUpPanelLayout panel = (SlidingUpPanelLayout) findViewById(R.id.content_frame);
        panel.setPanelHeight(cardHeight + padding);
    }

    /**
     * Creates the {@link ActionBar} and the {@link Spinner} and enables its
     * Functionality
     * 
     * @see <a
     *      href="http://developer.android.com/design/patterns/actionbar.html">
     *      ActionBar Design</a>
     * @see <a
     *      href="http://developer.android.com/design/building-blocks/spinners.html">
     *      Spinner Design</a>
     */
    private void setupActionbar() {
        mActionBar = getActionBar();
        mActionBar.setDisplayShowTitleEnabled(false);
        // mActionbar.setDisplayShowHomeEnabled(false); // no "up-caret"
        mActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
        // Adapter to fill spinner with items
        SpinnerAdapter spinnerAdapter = new ArrayAdapter<String>(this, R.layout.list_item_spinner,
                mGameTitles);
        // Listener which calls selectSpinnerItem(position) when an item is
        // selected
        OnNavigationListener onNavigationListener = new OnNavigationListener() {
            @Override
            public boolean onNavigationItemSelected(int position, long itemId) {
                switchToGame(mGames.get(position));
                return true;
            }
        };
        mActionBar.setListNavigationCallbacks(spinnerAdapter,
                onNavigationListener);
    }

    /**
     * Switches the GameFragment and registers the StatusFragment as an
     * onStatusChangedListener. Hides status depending on
     * GameFragment.usesStatus()
     * 
     * @param position position of the item in the spinner
     */
    private void switchToGame(GameSettingsPair pair) {
        // GameFragment:
        GameFragment newGameFragment;
        try {
            // instantiate Fragment from Class
            Class<?> c = (Class<?>) Class.forName(pair.getGameClassName());
            newGameFragment = (GameFragment) c.newInstance();
            newGameFragment.setArguments(pair.getSettings());
        }
        // no multicatch in dalvik (~ java 1.6)
        catch (Exception e) {
            // App not operable
            throw new RuntimeException(e.getMessage());
        }

        // StatusFragment:
        Status status = new Status();
        GraphViewSeries graphViewSeries = new GraphViewSeries(new
                GraphViewData[] {
                        new GraphViewData(0, 0),
                        new GraphViewData(1, 3),
                        new GraphViewData(2, 5),
                        new GraphViewData(3, 2),
                        new GraphViewData(4, 6)
                });
        status.mGraphViewSeries = graphViewSeries;
        int score = (int) (Math.random() * 9) + 1;
        status.mScore = score;
        // card.addGraphData(new GraphViewData(5, score));
        mStatusFragment.addStatus(status);
        newGameFragment.setStatus(status);

        // Transaction
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.game_frame, newGameFragment).commit();
        // previous GameFragment will be garbage collected
        mGameFragment = newGameFragment;
    }

    /**
     * Called by OS the first time the options menu (icons and overflow menu
     * [three vertical dots] in the ActionBar) is displayed.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
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
            case R.id.action_settings:
                // myIntent.putExtra("key", value); //Optional parameters
                startActivity(new Intent(this, SettingsActivity.class));
                break;
            case R.id.action_about:
                Toast.makeText(this, R.string.action_about, Toast.LENGTH_LONG)
                        .show();
                break;
            case R.id.action_new_training:
                startActivity(new Intent(this, TrainingSelectActivity.class));
                break;
            case R.id.action_help:
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

    @Override
    protected void onPause() {
        super.onPause();
        pauseGame();
    }

    @Override
    public void onUserInteraction() {
        if (!mIsPaused) {
            mIsNowPausing = true;
        }
        else {
            mIsNowPausing = false;
        }
        pauseGame();
        super.onUserInteraction();
    }

    /**
     * Called after onUserInteraction -> needs boolean lock variable, to prevent
     * resuming immediately after pausing
     */
    @Override
    public void onClick(View v) {
        if (!mIsNowPausing)
            resumeGame();
        mIsNowPausing = false;
    }

    private void pauseGame() {
        if (!mIsPaused) {
            Log.d(TAG, "Paused");
            if (mGameFragment != null) {
                mGameFragment.pauseGame();
            }
            // Show pause symbol and grey out screen
            // mGameFrame.addView(mPauseView);
            mPauseView.bringToFront();
            mPauseView.setVisibility(View.VISIBLE);
            mIsPaused = true;
        }
    }

    private void resumeGame() {
        if (mIsPaused) {
            Log.d(TAG, "Resumed");
            // hide navigation bar.
            // FIXME flags reset when actionbar spinner or overflow window opens
            // View decorView = getWindow().getDecorView();
            // int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
            // // | View.SYSTEM_UI_FLAG_FULLSCREEN
            // // |View.SYSTEM_UI_FLAG_LOW_PROFILE
            // | View.SYSTEM_UI_FLAG_IMMERSIVE;
            // decorView.setSystemUiVisibility(uiOptions);
            // remove pausescreen and resume game
            // mGameFrame.removeView(mPauseView);
            mPauseView.setVisibility(View.INVISIBLE);
            mGameFragment.resumeGame();
            mIsPaused = false;
        }
    }

    private class PauseView extends View {
        private Bitmap mPlaySign;
        private int mViewWidthMax;
        private int mViewHeightMax;
        private int mCenterX;
        private int mCenterY;
        private int mMinBorder;

        public PauseView(Context context) {
            super(context);
            Resources res = context.getResources();
            mPlaySign = BitmapFactory.decodeResource(res, R.drawable.ic_play);
        }

        // Called back when the view is first created or its size changes.
        @Override
        public void onSizeChanged(int w, int h, int oldW, int oldH) {
            mViewWidthMax = w - 1;
            mViewHeightMax = h - 1;
            mCenterX = mViewWidthMax / 2;
            mCenterY = mViewHeightMax / 2;
            mMinBorder = mViewWidthMax <= mViewHeightMax ? mViewWidthMax : mViewHeightMax;
        }

        @Override
        protected void onDraw(Canvas canvas) {
            canvas.drawARGB(160, 10, 10, 10);
            drawPlaySign(canvas, mMinBorder / 6);
        }

        private void drawPlaySign(Canvas canvas, int size) {
            // TODO Selber zeichnen
            Rect r = new Rect(mCenterX - size,
                    mCenterY - size,
                    mCenterX + size,
                    mCenterY + size);
            canvas.drawBitmap(mPlaySign, null, r, null);
        }
    }

}
