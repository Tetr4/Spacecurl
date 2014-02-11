
package de.klimek.spacecurl;

import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.internal.CardArrayAdapter;
import it.gmariotti.cardslib.library.view.CardListView;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.FrameLayout;

import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import de.klimek.spacecurl.game.GameFragment;
import de.klimek.spacecurl.training.TrainingSelectActivity;
import de.klimek.spacecurl.util.StatusCard;
import de.klimek.spacecurl.util.collection.Database;
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
public abstract class MainActivityPrototype extends FragmentActivity implements OnClickListener {
    // Used for log output
    private static final String TAG = MainActivityPrototype.class.getName();

    public final static String EXTRA_TRAINING_KEY = "EXTRA_TRAINING";

    private Database mDatabase;

    private CardListView mCardListView;
    private SlidingUpPanelLayout mSlidingUpPanel;
    private CardArrayAdapter mCardArrayAdapter;
    private List<Card> mCards = new ArrayList<Card>();

    private FrameLayout mGameFrame;
    private GameFragment mGameFragment;

    private PauseView mPauseView;
    private State mState = State.Running;

    public static enum State {
        Paused, Pausing, Running
    }

    protected abstract boolean usesStatus();

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
        setupStatus(usesStatus());
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
        mGameFrame = (FrameLayout) findViewById(R.id.game_frame);
        mPauseView = new PauseView(this);
        mPauseView.setVisibility(View.INVISIBLE);
        mGameFrame.addView(mPauseView);
        mGameFrame.setOnClickListener(this);
    }

    private void setupStatus(boolean show) {
        mSlidingUpPanel = (SlidingUpPanelLayout) findViewById(R.id.content_frame);
        if (show) {
            int cardHeight = (int) (getResources()
                    .getDimension(R.dimen.card_height));
            int padding = 12;
            mSlidingUpPanel.setPanelHeight(cardHeight + padding);
        }
        else {
            mSlidingUpPanel.setPanelHeight(-1);
            return;
        }

        mCardArrayAdapter = new CardArrayAdapter(this, mCards);
        mCardListView = (CardListView) findViewById(R.id.card_list);
        if (mCardListView != null) {
            mCardListView.setAdapter(mCardArrayAdapter);
        }
        // Set SildingUpPanel Height to only show upper card

        // mSlidingUpPanel.setEnableDragViewTouchEvents(true);
        // mSlidingUpPanel.setPanelSlideListener(new PanelSlideListener() {
        // @Override
        // public void onPanelSlide(View panel, float slideOffset) {
        // Log.i(TAG, "onPanelSlide, offset " + slideOffset);
        // Log.d(TAG,
        // Boolean.toString(mSlidingUpPanel.canScrollVertically(1)));
        // if (slideOffset < 0.2) {
        // if (getActionBar().isShowing()) {
        // getActionBar().hide();
        // }
        // } else {
        // if (!getActionBar().isShowing()) {
        // getActionBar().show();
        // }
        // }
        // }
        //
        // @Override
        // public void onPanelCollapsed(View panel) {
        // // TODO Auto-generated method stub
        //
        // }
        //
        // @Override
        // public void onPanelExpanded(View panel) {
        // // TODO Auto-generated method stub
        //
        // }
        //
        // @Override
        // public void onPanelAnchored(View panel) {
        // // TODO Auto-generated method stub
        //
        // }
        // });
    }

    protected void expandSlidingPane() {
        mSlidingUpPanel.expandPane();
    }

    protected void addStatus(Status status) {
        if (!usesStatus()) {
            // Exception
            return;
        }
        mDatabase.getStatuses().add(status);
        mCards.add(new StatusCard(this, status));
        mCardArrayAdapter.notifyDataSetChanged();
    }

    /**
     * Switches the GameFragment and registers the StatusFragment as an
     * onStatusChangedListener. Hides status depending on
     * GameFragment.usesStatus()
     * 
     * @param position position of the item in the spinner
     */
    protected void switchToGame(GameSettingsPair pair) {
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

        onGameSwitched();

    }

    protected abstract void onGameSwitched();

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
