
package de.klimek.spacecurl;

import android.app.ActionBar;
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
import android.widget.FrameLayout;
import android.widget.Spinner;

import com.jjoe64.graphview.GraphView.GraphViewData;
import com.jjoe64.graphview.GraphViewSeries;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;
import com.sothree.slidinguppanel.SlidingUpPanelLayout.PanelSlideListener;

import de.klimek.spacecurl.MainActivity.State;
import de.klimek.spacecurl.game.GameCallBackListener;
import de.klimek.spacecurl.game.GameFragment;
import de.klimek.spacecurl.status.StatusFragment;
import de.klimek.spacecurl.training.TrainingSelectActivity;
import de.klimek.spacecurl.util.collection.Database;
import de.klimek.spacecurl.util.collection.GameSettingsPair;
import de.klimek.spacecurl.util.collection.Status;
import de.klimek.spacecurl.util.collection.Training;

/**
 * This program is an App for the Android OS 4.4, intended to provide
 * information and visual support while training on the SpaceCurl.
 * 
 * @author Mike Klimek
 * @see <a href="http://developer.android.com/reference/packages.html">Android
 *      API</a>
 */
public class TrainingActivity extends FragmentActivity implements OnClickListener,
        GameCallBackListener {
    private static final String TAG = "TrainingActivity"; // Used for log output
    protected static final String STATE_CURRENT_TRAINING = "STATE_CURRENT_TRAINING";
    protected static final String STATE_CURRENT_GAME = "STATE_CURRENT_GAME";
    public final static String EXTRA_TRAINING_KEY = "EXTRA_TRAINING";

    private ActionBar mActionBar;
    private StatusFragment mStatusFragment;
    private GameFragment mGameFragment;
    private FrameLayout mGameFrame;
    private SlidingUpPanelLayout mSlidingUpPanel;
    private PauseView mPauseView;

    private State mState = State.Paused;

    private Training mTraining;
    private int mTrainingIndex = -1;
    private GameSettingsPair mGameSettingsPair;
    private String mTitle = "";

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
        int key = getIntent().getIntExtra(EXTRA_TRAINING_KEY, 0);
        mTraining = Database.getInstance().getTrainings().get(key);
        setupSettings();
        setupStatusFragment();
        setupPauseView();
        setupActionbar();
        nextGame();
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
    }

    private void setupStatusFragment() {
        mStatusFragment = new StatusFragment();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.status_frame, mStatusFragment).commit();
        // Set SildingUpPanel Height to only show upper card
        final int cardHeight = (int) (getResources()
                .getDimension(R.dimen.card_height));
        final int padding = 12;
        mSlidingUpPanel = (SlidingUpPanelLayout) findViewById(R.id.content_frame);
        mSlidingUpPanel.setPanelHeight(cardHeight + padding);
        mSlidingUpPanel.setPanelSlideListener(new PanelSlideListener() {
            @Override
            public void onPanelSlide(View panel, float slideOffset) {
                Log.i(TAG, "onPanelSlide, offset " + slideOffset);
                if (slideOffset < 0.2) {
                    if (mActionBar.isShowing()) {
                        mActionBar.hide();
                    }
                } else {
                    if (!mActionBar.isShowing()) {
                        mActionBar.show();
                    }
                }
            }

            @Override
            public void onPanelCollapsed(View panel) {
            }

            @Override
            public void onPanelExpanded(View panel) {
            }

            @Override
            public void onPanelAnchored(View panel) {
            }
        });
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
        mActionBar.setTitle(mTitle);
        mActionBar.setDisplayShowTitleEnabled(true);
    }

    private void previousGame() {
        Log.v(TAG, "previousGame");
        if (mTrainingIndex - 1 >= 0) {
            mGameSettingsPair = mTraining.get(--mTrainingIndex);
            switchToGame(mGameSettingsPair);
            // TODO remove StatusCard and use previous
        }
    }

    private void nextGame() {
        Log.v(TAG, "nextGame");
        if (mTrainingIndex + 1 < mTraining.size()) {
            mGameSettingsPair = mTraining.get(++mTrainingIndex);
            switchToGame(mGameSettingsPair);
            // TODO add statuscard
        } else {
            // Arrived at end of training
            mSlidingUpPanel.expandPane();
        }
    }

    @Override
    public void onGameFinished() {
        nextGame();
    }

    @Override
    public void onStatusChanged(Status status) {
    }

    /**
     * Switches the GameFragment and registers the StatusFragment as an
     * onStatusChangedListener. Hides status depending on
     * GameFragment.usesStatus()
     * 
     * @param position position of the item in the spinner
     */
    private void switchToGame(GameSettingsPair pair) {
        if (mGameFragment != null) {
            mGameFragment.setState(State.Paused);
        }
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
        // TODO functionality
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

        // Transaction
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.game_frame, newGameFragment).commit();

        // previous GameFragment will be garbage collected
        mGameFragment = newGameFragment;
        mState = State.Running;
        resumeGame();

        // Actionbar title and icons
        mTitle = mGameSettingsPair.getSettings().getString(GameFragment.ARG_TITLE);
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
            case R.id.action_freeplay:
                startActivity(new Intent(this, MainActivity.class));
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
        // outState.putInt(STATE_ACTIONBAR_SELECTED_ITEM,
        // mActionBar.getSelectedNavigationIndex());
    }

    @Override
    protected void onPause() {
        super.onPause();
        mState = State.Paused;
        pauseGame();
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
            // just paused in onUserInteraction, dont resume
            mState = State.Paused;
        }
    }

    private void pauseGame() {
        Log.v(TAG, "Paused");
        if (mGameFragment != null) {
            mGameFragment.setState(State.Paused);
        }
        // Show pause symbol and grey out screen
        mPauseView.setVisibility(View.VISIBLE);
        mPauseView.bringToFront();
    }

    private void resumeGame() {
        Log.v(TAG, "Resumed");
        // hide navigation bar.
        // XFIXME flags reset when actionbar spinner or overflow window opens
        // View decorView = getWindow().getDecorView();
        // int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
        // // | View.SYSTEM_UI_FLAG_FULLSCREEN
        // // |View.SYSTEM_UI_FLAG_LOW_PROFILE
        // | View.SYSTEM_UI_FLAG_IMMERSIVE;
        // decorView.setSystemUiVisibility(uiOptions);
        mPauseView.setVisibility(View.INVISIBLE);
        mGameFragment.setState(State.Running);
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
