
package de.klimek.spacecurl.activities;

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
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.sothree.slidinguppanel.SlidingUpPanelLayout;
import com.sothree.slidinguppanel.SlidingUpPanelLayout.PanelSlideListener;

import de.klimek.spacecurl.Database;
import de.klimek.spacecurl.R;
import de.klimek.spacecurl.R.dimen;
import de.klimek.spacecurl.R.drawable;
import de.klimek.spacecurl.R.id;
import de.klimek.spacecurl.R.layout;
import de.klimek.spacecurl.game.GameCallBackListener;
import de.klimek.spacecurl.game.GameDescription;
import de.klimek.spacecurl.game.GameFragment;
import de.klimek.spacecurl.util.ColorGradient;
import de.klimek.spacecurl.util.cards.StatusCard;
import de.klimek.spacecurl.util.collection.GameStatus;
import de.klimek.spacecurl.util.collection.Training;
import de.klimek.spacecurl.util.collection.TrainingStatus;

/**
 * This program is an App for the Android OS 4.4, intended to provide
 * information and visual support while training on the SpaceCurl. <br>
 * This abstract class loads a training from the database and provides
 * functionality to subclasses, e. g. usage of the status panel, switching
 * games, and showing a pause screen. The training's number is given as an extra
 * (EXTRA_TRAINING_NR) when starting the activity. <br>
 * 
 * @author Mike Klimek
 * @see <a href="http://developer.android.com/reference/packages.html">Android
 *      API</a>
 */
public abstract class BasicTrainingActivity extends FragmentActivity implements OnClickListener {
    // Used for log output
    private static final String TAG = BasicTrainingActivity.class.getName();

    private Database mDatabase;

    // Status panel
    private boolean mUsesStatus = false;
    private TrainingStatus mTrainingStatus;
    private GameStatus mCurGameStatus;
    private int mStatusColor;
    private ColorGradient mStatusGradient = new ColorGradient(Color.RED, Color.YELLOW, Color.GREEN);
    private FrameLayout mStatusIndicator;
    private ImageButton mSlidingToggleButton;
    private boolean mButtonImageIsExpand = true;
    private SlidingUpPanelLayout mSlidingUpPanel;
    private CardListView mCardListView;
    private CardArrayAdapter mCardArrayAdapter;
    private List<Card> mCards = new ArrayList<Card>();

    private Training mTraining;
    private GameFragment mGameFragment;
    private FrameLayout mGameFrame;

    // Pause Frame
    private FrameLayout mPauseFrame;
    private LinearLayout mInstructionLayout;
    private ImageView mResumeButton;
    private TextView mInstructionsTextView;
    private String mInstructions = "";
    private int mShortAnimationDuration;
    private State mState = State.Paused;

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
        setContentView(R.layout.activity_main);

        mStatusIndicator = (FrameLayout) findViewById(R.id.status_indicator);
        mSlidingToggleButton = (ImageButton) findViewById(R.id.panel_button);
        mSlidingUpPanel = (SlidingUpPanelLayout) findViewById(R.id.content_frame);
        hideSlidingPane(); // initially hidden

        // setupPauseView (Notification window with instructions for the
        // current exercise or pause/play icon)
        mPauseFrame = (FrameLayout) findViewById(R.id.pause_layout);
        mPauseFrame.setAlpha(0.0f);
        mPauseFrame.setVisibility(View.GONE);
        mInstructionLayout = (LinearLayout) findViewById(R.id.instruction_layout);
        mResumeButton = (ImageView) findViewById(R.id.resume_button);
        mInstructionsTextView = (TextView) findViewById(R.id.instructions_textview);
        mShortAnimationDuration = getResources().getInteger(
                android.R.integer.config_shortAnimTime);
        mGameFrame = (FrameLayout) findViewById(R.id.game_frame);
        mGameFrame.setOnClickListener(this);
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
     * Load training from database. The training's number is given as an extra
     * (EXTRA_TRAINING_NR) when starting the activity. Otherwise a blank
     * Training will be created.
     */
    protected void loadTraining(Training training) {
        mTraining = training;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_new_training:
                startActivity(new Intent(this, TrainingSelectActivity.class));
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    /**
     * subclasses can use this methode to enable the statuspanel.
     */
    protected final void useStatus(int trainingIndex) {
        mUsesStatus = true;
        mTrainingStatus = new TrainingStatus(trainingIndex);
        mDatabase.getStatuses().append(trainingIndex, mTrainingStatus);
        showSlidingPane();

        mSlidingUpPanel.setPanelSlideListener(new PanelSlideListener() {

            @Override
            public void onPanelSlide(View panel, float slideOffset) {
                if (slideOffset > 0.5f && mButtonImageIsExpand) {
                    mSlidingToggleButton.setImageResource(R.drawable.ic_collapse);
                    mButtonImageIsExpand = false;
                } else if (slideOffset < 0.5f && !mButtonImageIsExpand) {
                    mSlidingToggleButton.setImageResource(R.drawable.ic_expand);
                    mButtonImageIsExpand = true;
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

            @Override
            public void onPanelHidden(View panel) {
            }
        });
        int statusIndicatorHeight = (int) (getResources()
                .getDimension(R.dimen.status_indicator_height));
        mSlidingUpPanel.setPanelHeight(statusIndicatorHeight);
        // delegate clicks to underlying panel
        mSlidingToggleButton.setClickable(false);

        // setup cardlist
        // FIXME bug in viewholder pattern
        mCardArrayAdapter = new CardArrayAdapter(this, mCards);
        mCardListView = (CardListView) findViewById(R.id.card_list);
        mCardListView.setAdapter(mCardArrayAdapter);
    }

    protected final void updateCurGameStatus(final float status) {
        // graph
        mCurGameStatus.addStatus(status);
        // indicator
        mStatusColor = mStatusGradient.getColorForFraction(status);
        mStatusIndicator.setBackgroundColor(mStatusColor);
        // TODO threaded?
    }

    protected final void expandSlidingPane() {
        if (mState == State.Running) {
            pause();
            mState = State.Paused;
        }
        mSlidingUpPanel.expandPanel();
    }

    protected final void collapseSlidingPane() {
        mSlidingUpPanel.collapsePanel();
    }

    protected final void showSlidingPane() {
        mSlidingUpPanel.showPanel();
        return;
    }

    protected final void hideSlidingPane() {
        mSlidingUpPanel.hidePanel();
        return;
    }

    protected final void showStatusIndicator() {
        mStatusIndicator.setVisibility(View.VISIBLE);
    }

    protected final void hideStatusIndicator() {
        mStatusIndicator.setVisibility(View.GONE);
    }

    protected final void lockSlidingPane() {
        mSlidingToggleButton.setVisibility(View.GONE);
    }

    protected final void unlockSlidingPane() {
        mSlidingToggleButton.setVisibility(View.VISIBLE);
    }

    /**
     * Creates a new GameFragment from a gameDescriptionIndex and displays it.
     * Switches to the associated gameStatus, e.g. to reuse previous gamecards
     * 
     * @param gameDescriptionIndex the game to start
     * @param enterAnimation how the fragment should enter
     * @param exitAnimation how the previous fragment should be removed
     */
    protected final void switchToGame(int gameDescriptionIndex, int enterAnimation,
            int exitAnimation) {
        mState = State.Paused;
        pause();

        // GameFragment:
        GameDescription newGameDescription = mTraining.get(gameDescriptionIndex);
        mGameFragment = newGameDescription.getFragment();

        // Transaction
        getSupportFragmentManager().beginTransaction()
                .setCustomAnimations(enterAnimation, exitAnimation)
                .replace(R.id.game_frame, mGameFragment)
                .commit();

        // enable callback on certain subclasses
        if (this instanceof GameCallBackListener) {
            mGameFragment.registerGameCallBackListener((GameCallBackListener) this);
        }

        // update pause view
        mInstructions = newGameDescription.getInstructions();
        if (mInstructions == null || mInstructions.equals("")) {
            // only show pause/play icon
            mInstructionLayout.setVisibility(View.GONE);
            mResumeButton.setVisibility(View.VISIBLE);
        } else {
            // only show instructions
            mResumeButton.setVisibility(View.GONE);
            mInstructionLayout.setVisibility(View.VISIBLE);
            mInstructionsTextView.setText(mInstructions);
        }

        if (mUsesStatus) {
            // switch to status associated with current game
            mCurGameStatus = mTrainingStatus.get(gameDescriptionIndex);
            if (mCurGameStatus == null) {
                // create new
                mCurGameStatus = new GameStatus(newGameDescription.getTitle());
                mTrainingStatus.append(gameDescriptionIndex, mCurGameStatus);
                mCards.add(new StatusCard(this, mCurGameStatus));
                mCardArrayAdapter.notifyDataSetChanged();
            } else {
                // reset existing
                mCurGameStatus.reset();
                mCardArrayAdapter.notifyDataSetChanged();
            }
        }
    }

    /**
     * Creates a new GameFragment from a gameDescriptionIndex and displays it
     * using fade_in/fade_out animations.
     * 
     * @param gameDescription the game to start
     */
    protected final void switchToGame(int gameDescriptionIndex) {
        switchToGame(gameDescriptionIndex, android.R.anim.fade_in, android.R.anim.fade_out);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mState == State.Running) {
            pause();
        }
        mState = State.Paused;
    }

    @Override
    public void onUserInteraction() {
        // Pause on every user interaction
        if (mState == State.Running) {
            mState = State.Pausing;
            pause();
        } else if (mState == State.Pausing) {
            // Previously clicked outside of gameframe and now possibly on
            // gameframe again to resume
            mState = State.Paused;
        }
        super.onUserInteraction();
    }

    /**
     * Called when clicking inside the game frame (after onUserInteraction)
     */
    @Override
    public void onClick(View v) {
        if (mState == State.Paused) {
            // Resume when paused
            resume();
            mState = State.Running;
        } else if (mState == State.Pausing) {
            // We have just paused in onUserInteraction -> don't resume
            mState = State.Paused;
        }
    }

    private void pause() {
        Log.v(TAG, "Paused");

        // pause game
        if (mGameFragment != null) {
            mGameFragment.onPauseGame();
        }

        // Show pause view and grey out screen
        mPauseFrame.setVisibility(View.VISIBLE);
        mPauseFrame.animate()
                .alpha(1f)
                .setDuration(mShortAnimationDuration)
                .setListener(null);

        // Show navigation bar
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
    }

    private void resume() {
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
