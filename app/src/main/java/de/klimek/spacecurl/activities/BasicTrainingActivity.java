
package de.klimek.spacecurl.activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.sothree.slidinguppanel.SlidingUpPanelLayout;
import com.sothree.slidinguppanel.SlidingUpPanelLayout.PanelSlideListener;

import java.util.ArrayList;
import java.util.List;

import de.klimek.spacecurl.Database;
import de.klimek.spacecurl.R;
import de.klimek.spacecurl.game.GameCallBackListener;
import de.klimek.spacecurl.game.GameDescription;
import de.klimek.spacecurl.game.GameFragment;
import de.klimek.spacecurl.util.ColorGradient;
import de.klimek.spacecurl.util.cards.StatusCard;
import de.klimek.spacecurl.util.collection.GameStatus;
import de.klimek.spacecurl.util.collection.Training;
import de.klimek.spacecurl.util.collection.TrainingStatus;
import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.internal.CardArrayAdapter;
import it.gmariotti.cardslib.library.view.CardListView;
import it.gmariotti.cardslib.library.view.CardView;

/**
 * This abstract class loads a training and provides functionality to
 * subclasses, e. g. usage of the status panel, switching games, and showing a
 * pause screen. <br/>
 * A Training must be loaded with {@link #loadTraining(Training)}. In order to
 * use the status-panel {@link #useStatusPanel()} has to be called.
 * 
 * @author Mike Klimek
 * @see <a href="http://developer.android.com/reference/packages.html">Android
 *      API</a>
 */
public abstract class BasicTrainingActivity extends FragmentActivity implements OnClickListener,
        GameCallBackListener {
    public static final String TAG = BasicTrainingActivity.class.getName();

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

    private GameFragment mGameFragment;
    private FrameLayout mGameFrame;

    private Training mTraining;
    private Database mDatabase;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDatabase = Database.getInstance(this);
        setContentView(R.layout.activity_base);

        setupPauseView();
    }

    /**
     * Dialog with instructions for the current game or pause/play icon. Shows
     * when the user interacts with the App.
     */
    private void setupPauseView() {
        mGameFrame = (FrameLayout) findViewById(R.id.game_frame);
        mGameFrame.setOnClickListener(this);
        // hide initially
        mPauseFrame = (FrameLayout) findViewById(R.id.pause_layout);
        mPauseFrame.setAlpha(0.0f);
        mPauseFrame.setVisibility(View.GONE);
        mInstructionLayout = (LinearLayout) findViewById(R.id.instruction_layout);
        mResumeButton = (ImageView) findViewById(R.id.resume_button);
        mInstructionsTextView = (TextView) findViewById(R.id.instructions_textview);
        mShortAnimationDuration = getResources().getInteger(
                android.R.integer.config_shortAnimTime);
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
     * Subclasses must load a Training with this method.
     * 
     * @param training
     */
    protected final void loadTraining(Training training) {
        mTraining = training;
    }

    /**
     * Subclasses can use this method to enable the status-panel.
     */
    protected final void useStatusPanel() {
        mTrainingStatus = mTraining.createTrainingStatus();
        mUsesStatus = true;

        mStatusIndicator = (FrameLayout) findViewById(R.id.status_indicator);
        mSlidingToggleButton = (ImageButton) findViewById(R.id.panel_button);

        mSlidingUpPanel = (SlidingUpPanelLayout) findViewById(R.id.content_frame);
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
        mSlidingUpPanel.setEnabled(true);

        // setup cardlist
        mCardArrayAdapter = new FixedCardArrayAdapter(this, mCards);
        mCardListView = (CardListView) findViewById(R.id.card_list);
        mCardListView.setAdapter(mCardArrayAdapter);
    }

    protected final void updateCurGameStatus(final float status) {
        // graph
        mCurGameStatus.addStatus(status);
        // indicator color
        mStatusColor = mStatusGradient.getColorForFraction(status);
        mStatusIndicator.setBackgroundColor(mStatusColor);
    }

    protected final void expandSlidingPane() {
        if (mState == State.Running) {
            pause();
            mState = State.Paused;
        }
        mSlidingUpPanel.setPanelState(SlidingUpPanelLayout.PanelState.EXPANDED);
    }

    protected final void collapseSlidingPane() {
        mSlidingUpPanel.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
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
     * Switches to the associated gameStatus from a previous game (overwriting
     * it) or creates a new one.
     * 
     * @param gameDescriptionIndex the game to start
     * @param enterAnimation how the fragment should enter
     * @param exitAnimation how the previous fragment should be removed
     */
    protected final void switchToGame(int gameDescriptionIndex, int enterAnimation,
            int exitAnimation) {
        mState = State.Paused;
        pause();

        // get Fragment
        GameDescription newGameDescription = mTraining.get(gameDescriptionIndex);
        mGameFragment = newGameDescription.createFragment();

        // Transaction
        getSupportFragmentManager().beginTransaction()
                .setCustomAnimations(enterAnimation, exitAnimation)
                .replace(R.id.game_frame, mGameFragment)
                .commit();

        // enable callback
        mGameFragment.registerGameCallBackListener(this);

        // update pause view
        mInstructions = newGameDescription.getInstructions();
        if (mInstructions == null || mInstructions.isEmpty()) {
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
                mCards.add(gameDescriptionIndex, new StatusCard(this, mCurGameStatus));
                mCardArrayAdapter.notifyDataSetChanged();
            } else {
                // reset existing
                mCurGameStatus.reset();
                mCardArrayAdapter.notifyDataSetChanged();
            }
        }
    }

    /**
     * Convenience method. Same as
     * {@link #switchToGame(int gameDescriptionIndex, int enterAnimation, int exitAnimation)}
     * but uses standard fade_in/fade_out animations.
     * 
     * @param gameDescriptionIndex the game to start
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

    /**
     * Fixes bug in {@link CardArrayAdapter CardArrayAdapter's} Viewholder
     * pattern. Otherwise the cards innerViewElements would not be replaced
     * (Title and Graph from previous Graph is shown).
     * 
     * @author Mike Klimek
     */
    private class FixedCardArrayAdapter extends CardArrayAdapter {

        public FixedCardArrayAdapter(Context context, List<Card> cards) {
            super(context, cards);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Card card = (Card) getItem(position);
            if (convertView == null) {
                LayoutInflater layoutInflater = LayoutInflater.from(getContext());
                convertView = layoutInflater.inflate(R.layout.list_card_layout, parent, false);
            }
            CardView view = (CardView) convertView.findViewById(R.id.list_cardId);
            view.setForceReplaceInnerLayout(true);
            view.setCard(card);
            return convertView;
        }
    }
}
