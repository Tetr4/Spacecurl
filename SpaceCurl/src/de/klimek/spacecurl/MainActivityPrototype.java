
package de.klimek.spacecurl;

import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.internal.CardArrayAdapter;
import it.gmariotti.cardslib.library.view.CardListView;

import java.util.ArrayList;
import java.util.List;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import de.klimek.spacecurl.game.GameCallBackListener;
import de.klimek.spacecurl.game.GameFragment;
import de.klimek.spacecurl.game.GameSettings;
import de.klimek.spacecurl.training.TrainingSelectActivity;
import de.klimek.spacecurl.util.ColorGradient;
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
    private ImageButton mSlidingToggleButton;
    private boolean mButtonImageIsExpand = true;
    private SlidingUpPanelLayout mSlidingUpPanel;
    private CardListView mCardListView;
    private CardArrayAdapter mCardArrayAdapter;

    private Training mTraining;
    private List<Card> mCards = new ArrayList<Card>();
    private TrainingStatus mStatus;
    private GameStatus mCurGameStatus;
    private int mStatusColor;

    private GameFragment mGameFragment;
    private FrameLayout mGameFrame;

    private FrameLayout mPauseFrame;
    private LinearLayout mInstructionLayout;
    private TextView mInstructionsTextView;
    private String mHighScore;
    private String mInstructions = "";
    private int mShortAnimationDuration;
    private State mState = State.Paused;

    private ColorGradient mGradient = new ColorGradient(Color.RED, Color.YELLOW, Color.GREEN);

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

    @Override
    protected void onResume() {
        super.onResume();
        if (mDatabase.isOrientationLandscape()) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
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
        // PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        // SharedPreferences sharedPref =
        // PreferenceManager.getDefaultSharedPreferences(this);
    }

    private void setupPauseView() {
        mPauseFrame = (FrameLayout) findViewById(R.id.pause_layout);
        mPauseFrame.setAlpha(0.0f);
        mPauseFrame.setVisibility(View.GONE);
        mInstructionLayout = (LinearLayout) findViewById(R.id.instruction_layout);
        mInstructionsTextView = (TextView) findViewById(R.id.instructions_textview);
        mShortAnimationDuration = getResources().getInteger(
                android.R.integer.config_shortAnimTime);
        mGameFrame = (FrameLayout) findViewById(R.id.game_frame);
        mGameFrame.setOnClickListener(this);
    }

    protected void expandSlidingPane() {
        if (mSlidingUpPanel != null) {
            pause();
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

    protected final void useStatusForTraining(int key) {
        mTraining = mDatabase.getTrainings().get(key);
        mStatus = new TrainingStatus(key);
        mDatabase.getStatuses().add(mStatus);

        // setup indicator
        mStatusIndicator = (FrameLayout) findViewById(R.id.status_indicator);
        mSlidingToggleButton = (ImageButton) findViewById(R.id.panel_button);
        OnClickListener expandsListener = new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (mButtonImageIsExpand) {
                    expandSlidingPane();
                    mSlidingToggleButton.setImageResource(R.drawable.ic_collapse);
                    mButtonImageIsExpand = false;
                } else {
                    if (mSlidingUpPanel != null) {
                        mSlidingUpPanel.collapsePane();
                    }
                    mSlidingToggleButton.setImageResource(R.drawable.ic_expand);
                    mButtonImageIsExpand = true;
                }
            }
        };
        mSlidingToggleButton.setOnClickListener(expandsListener);
        mStatusIndicator.setOnClickListener(expandsListener);

        // setup panel
        mSlidingUpPanel = (SlidingUpPanelLayout) findViewById(R.id.content_frame);
        mSlidingUpPanel.setSlidingEnabled(false);
        int statusIndicatorHeight = (int) (getResources()
                .getDimension(R.dimen.status_indicator_height));
        mSlidingUpPanel.setPanelHeight(statusIndicatorHeight);
        mSlidingUpPanel.setShadowDrawable(getResources().getDrawable(R.drawable.above_shadow));

        // setup cardlist
        mCardArrayAdapter = new CardArrayAdapter(this, mCards);
        mCardListView = (CardListView) findViewById(R.id.card_list);
        mCardListView.setAdapter(mCardArrayAdapter);
    }

    protected final void switchStatus(int index) {
        if (index >= mStatus.size()) {
            mCurGameStatus = new GameStatus(mTraining.get(index).getTitle());
            mStatus.add(mCurGameStatus);
            mCards.add(new StatusCard(this, mCurGameStatus));
            mCardArrayAdapter.notifyDataSetChanged();
        } else {
            mCurGameStatus = mStatus.get(index);
            mCurGameStatus.reset();
        }
    }

    protected final void doStatusChanged(final float status) {
        // graph
        mCurGameStatus.addStatus(status);
        // indicator
        mStatusColor = mGradient.getColorForFraction(status);
        mStatusIndicator.setBackgroundColor(mStatusColor);
        // new Handler().post(new Runnable() {
        // @Override
        // public void run() {
        // // graph
        // mCurGameStatus.addStatus(status);
        // // indicator
        // mStatusColor = mGradient.getColorForFraction(status);
        // mStatusIndicator.setBackgroundColor(mStatusColor);
        // }
        // });
    }

    protected final void doPostHighScore(String highScore) { // TODO Notifcation
        mHighScore = highScore;
        // Toast toast2 = new Toast(this);
        // toast2.setGravity(Gravity.TOP, 0, getActionBar().getHeight() + 32);
        // toast2.setDuration(Toast.LENGTH_LONG);
        // TextView tv = new TextView(this);
        // tv.setText(highScore);
        // toast2.setView(tv);
        // toast2.show();
        Toast toast = Toast.makeText(getApplicationContext(), mHighScore,
                Toast.LENGTH_LONG);
        toast.setGravity(Gravity.TOP, 0, getActionBar().getHeight() + 32);
        toast.show();
    }

    /**
     * Switches the GameFragment and registers the StatusFragment as an
     * onStatusChangedListener. Hides status depending on
     * GameFragment.usesStatus()
     * 
     * @param position position of the item in the spinner
     */
    protected final void switchToGame(GameSettings settings, int enterAnimation, int exitAnimation) {
        mState = State.Paused;
        pause();
        // GameFragment:
        GameFragment newGameFragment = settings.getFragment();

        // Transaction
        getSupportFragmentManager().beginTransaction()
                .setCustomAnimations(enterAnimation, exitAnimation)
                .replace(R.id.game_frame, newGameFragment)
                .commit();

        // previous GameFragment will be garbage collected
        mGameFragment = newGameFragment;
        if (this instanceof GameCallBackListener) {
            mGameFragment.registerGameCallBackListener((GameCallBackListener) this);
        }

        // mState = State.Paused;
        // pauseGame();

        // Actionbar title and icons
        // mTitle = mGameSettingsPair.getSettings().getString(
        // GameFragment.ARG_TITLE);

        mInstructions = settings.getInstructions();
        if (mInstructions == null || mInstructions.equals("")) {
            mInstructionLayout.setVisibility(View.GONE);
        } else {
            mInstructionLayout.setVisibility(View.VISIBLE);
            mInstructionsTextView.setText(mInstructions);
        }
        onGameSwitched(mGameFragment);
    }

    protected final void switchToGame(GameSettings settings) {
        switchToGame(settings, android.R.anim.fade_in, android.R.anim.fade_out);
    }

    protected void onGameSwitched(GameFragment gameFragment) {
        return;
    };

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
            pause();
        }
    }

    @Override
    public void onUserInteraction() {
        if (mState == State.Running) {
            mState = State.Pausing;
            pause();
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
            resume();
            mState = State.Running;
        } else if (mState == State.Pausing) {
            // we have just paused in onUserInteraction, don't resume
            mState = State.Paused;
        }
    }

    private final void pause() {
        // pause game
        Log.v(TAG, "Paused");
        if (mGameFragment != null) {
            mGameFragment.onPauseGame();
        }

        // Show pause symbol and grey out screen
        mPauseFrame.setVisibility(View.VISIBLE);
        // mPauseFrame.bringToFront();
        mPauseFrame.animate()
                .alpha(1f)
                .setDuration(mShortAnimationDuration)
                .setListener(null);

        // Show navigation bar
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
    }

    private final void resume() {
        Log.v(TAG, "Resumed");

        // hide pause view
        mPauseFrame.animate()
                .alpha(0f)
                .setDuration(mShortAnimationDuration)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        mPauseFrame.setVisibility(View.GONE);
                    }
                });

        // resume game
        if (mGameFragment != null) {
            mGameFragment.onResumeGame();
        }
        // Grey out navigation bar
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);
    }

}
