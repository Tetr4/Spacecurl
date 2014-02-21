
package de.klimek.spacecurl.game.universal;

import java.util.Random;

import android.content.Context;
import android.graphics.Canvas;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import de.klimek.spacecurl.game.GameFragment;

// TODO replace with 3d render, similar to http://code.google.com/p/stardroid/
public class Universal extends GameFragment {
    // TODO more complex shape. ellipse?
    public static final String ARG_TARGET_RADIUS = "ARG_TARGET_RADIUS";
    public static final String ARG_HOLDING_TIME = "ARG_HOLDING_TIME";
    public static final String ARG_RESET_HOLDING_TIME_IF_LEFT = "ARG_RESET_HOLDING_TIME_IF_LEFT";

    // private int score;
    private GameUniversalView mGame;
    private Effect[] mEffects;
    private FreeAxisCount mFreeAxisCount;

    private UniversalSettings mSettings;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mSettings = (UniversalSettings) getSettings();
        mGame = new GameUniversalView(getActivity());
        if (mSettings.getTargets().isEmpty()) {
            Random random = new Random();
            mGame.mTarget = new Target(random.nextFloat(), random.nextFloat(),
                    random.nextFloat() / 10.0f + 0.02f, 0L, false);
        } else {
            mGame.mTarget = mSettings.getTargets().get(0);
        }
        resumeGame();
        return mGame;
    }

    @Override
    public void pauseGame() {
        mGame.pause();
    }

    @Override
    public void resumeGame() {
        mGame.resume();

    }

    @Override
    public FreeAxisCount getFreeAxisCount() {
        mFreeAxisCount = FreeAxisCount.One;
        return mFreeAxisCount;
    }

    @Override
    public Effect[] getEffects() {
        return mEffects;
    }

    private class GameUniversalView extends View {
        private AsyncTask<Void, Void, Void> _logicThread = new LogicThread();

        private int mViewWidthMax;
        private int mViewHeightMax;
        private int mCenterX;
        private int mCenterY;
        private int mMinBorder;
        private boolean mSizeChanged = false;

        private Player mPlayer;
        private Target mTarget;
        private CenteredCircles mCircles;

        private float mStatus;

        private boolean mResetIfLeft;
        private Random mRandom = new Random();

        public GameUniversalView(Context context) {
            super(context);
        }

        public void pause() {
            _logicThread.cancel(true);

        }

        public void resume() {
            if (!_logicThread.getStatus().equals(AsyncTask.Status.RUNNING)) {
                _logicThread = new LogicThread();
                _logicThread.execute();
            }

        }

        // Called back when the view is first created or its size changes.
        @Override
        public void onSizeChanged(int w, int h, int oldW, int oldH) {
            // Set the movement bounds for the ball
            mViewWidthMax = w - 1;
            mViewHeightMax = h - 1;
            mCenterX = mViewWidthMax / 2;
            mCenterY = mViewHeightMax / 2;
            mMinBorder = mViewWidthMax <= mViewHeightMax ? mViewWidthMax : mViewHeightMax;

            mPlayer = new Player(0.1f);
            // mPlayer.mPositionX = mCenterX;
            // mPlayer.mPositionY = mCenterY;

            int circleCount = 6;
            mCircles = new CenteredCircles(circleCount, mMinBorder / (circleCount * 2), mCenterX,
                    mCenterY);
            mSizeChanged = true;
        }

        @Override
        protected void onDraw(Canvas canvas) {
            mCircles.draw(canvas);
            mTarget.draw(canvas);
            if (hasOrientation())
                mPlayer.draw(canvas);
        }

        private class LogicThread extends AsyncTask<Void, Void, Void> {
            private long _lastTime = System.currentTimeMillis();
            private long _startTime;
            private long _deltaTime;
            private float _pitch;
            private float _roll;
            private float _distance;
            private float _innerBorder = Float.NaN;
            private float _outerBorder = Float.NaN;
            private boolean _finished = false;

            @Override
            protected Void doInBackground(Void... params) {
                while (!isCancelled()) {
                    _startTime = System.currentTimeMillis();
                    _deltaTime = _startTime - _lastTime;
                    _lastTime = _startTime;
                    if (mSizeChanged && hasOrientation()) {
                        updatePlayer();
                        checkFinished();
                        updateStatus();
                        publishProgress();
                    }
                    // Delay
                    try {
                        Thread.sleep(1000 / 30); // 30 FPS
                    } catch (InterruptedException e) {
                        // e.printStackTrace();
                    }
                }
                return null;
            }

            private void updatePlayer() {
                _pitch = getScaledOrientation()[1];
                _roll = getScaledOrientation()[2];
                mPlayer.mPositionX = (_roll + 1.0f) / 2.0f;
                mPlayer.mPositionY = (_pitch + 1.0f) / 2.0f;
            }

            private void checkFinished() {
                if (mPlayer.intersects(mTarget)) {
                    mTarget.mCurHoldingTime -= _deltaTime;
                    if (mTarget.mCurHoldingTime < 0)
                        _finished = true;
                } else if (mResetIfLeft) {
                    mTarget.mCurHoldingTime = mTarget.mHoldingTime;
                }
            }

            private void updateStatus() {
                if (hasOrientation()) {
                    _distance = mPlayer.distanceTo(mTarget);
                    if (_innerBorder == Float.NaN) {
                        _innerBorder = _distance * 1.5f;
                        _outerBorder = _innerBorder * 1.5f;
                    } else {
                        _innerBorder -= 0; // TODO SPEED
                        _outerBorder = _innerBorder * 1.5f;
                        mStatus = (_distance - _innerBorder) / (_outerBorder - _innerBorder);
                        // Cutoff values between 0.0f and 1.0f
                        mStatus = Math.min(1.0f, Math.max(mStatus, 0.0f));
                    }
                }
            }

            @Override
            protected void onProgressUpdate(Void... values) {
                if (_finished) {
                    notifyFinished();
                    mTarget = new Target(mRandom.nextFloat(), mRandom.nextFloat(),
                            mRandom.nextFloat() / 10.0f + 0.02f, 0L, false);
                    mTarget.mCurHoldingTime = mTarget.mHoldingTime;
                    _innerBorder = Float.NaN;
                    _finished = false;
                }
                notifyStatusChanged(mStatus);
                invalidate();
            }

            @Override
            protected void onCancelled(Void result) {
                Log.v(TAG, "Thread: Cancelled");
                super.onCancelled(result);
            }
        }

    }
}
