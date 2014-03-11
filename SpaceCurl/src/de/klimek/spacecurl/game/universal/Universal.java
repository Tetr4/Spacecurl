
package de.klimek.spacecurl.game.universal;

import java.util.ArrayList;
import java.util.Random;

import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import de.klimek.spacecurl.R;
import de.klimek.spacecurl.game.GameFragment;

public class Universal extends GameFragment {
    private Effect[] mEffects;
    private FreeAxisCount mFreeAxisCount;
    private Mode mMode;

    public static enum Mode {
        Targets, Paths
    }

    private Random mRandom = new Random();
    private AsyncTask<Void, Void, Void> _logicThread;

    private UniversalSettings mSettings;
    private View mGameView;

    private Player mPlayer = new Player(0.1f);
    private CenteredCircles mCircles = new CenteredCircles(6);
    private ArrayList<Target> mTargets = new ArrayList<Target>();
    private ArrayList<Path> mPaths = new ArrayList<Path>();

    private int mCurPathIndex;
    private Path mCurPath;
    // private ArrayList<Path> mPaths = new ArrayList<Path>();
    private int mCurTargetIndex;
    private Drawable mTargetDrawable;
    private Target mCurTarget;
    private Target mNextTarget;

    private float mInnerBorder;
    private float mInnerBorderShrinkStep = 0.007f;
    private float mOuterBorder;
    private float mOuterBorderShrinkStep = 0.005f;
    private boolean mBordersSet = false;
    private boolean mFinished = false;

    private float mStatus = 1.0f;
    private float mHighscore = 1.0f;
    private float mHighscoreFactor = 1.0f;
    private float mFilteredStatus = 1.0f;
    private float mFilterWeight = 0.05f;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setupSettings();
        setupView();
        // resumeGame();
        return mGameView;
    }

    private void setupSettings() {
        mSettings = (UniversalSettings) getSettings();
        mTargetDrawable = getResources().getDrawable(R.drawable.target);
        if (!mSettings.getTargets().isEmpty()) { // TARGET
            mMode = Mode.Targets;

            mTargets.addAll(mSettings.getTargets());
            mCurTargetIndex = 0;
            mCurTarget = mTargets.get(mCurTargetIndex);
            mCurTarget.mRemainingHoldingTime = mCurTarget.mHoldingTime;
            if (mCurTargetIndex + 1 < mTargets.size()) {
                mNextTarget = mTargets.get(mCurTargetIndex + 1);
            }

            mCurTarget.setDrawable(mTargetDrawable);
        }
        else if (!mSettings.getPaths().isEmpty()) { // PATH
            mMode = Mode.Paths;
            mPaths = mSettings.getPaths();
            mCurPathIndex = 0;
            mCurPath = mPaths.get(mCurPathIndex);
        }
        // else if (false) { // AXIS
        // }
        else { // WARM UP
            mMode = Mode.Targets;

            mTargets.add(new Target(mRandom.nextFloat(), mRandom.nextFloat(),
                    mRandom.nextFloat() / 10.0f + 0.02f, 0L, false));
            mCurTargetIndex = 0;
            mCurTarget = mTargets.get(mCurTargetIndex);

            mTargetDrawable = getResources().getDrawable(R.drawable.target);
            // mCurTarget.setDrawable(mTargetDrawable);
        }
    }

    private void setupView() {
        // mGameView = new UniversalView(getActivity());
        // mGameView.setTarget(mCurTarget);
        // mGameView.setPlayer(mPlayer);
        mGameView = new View(getActivity()) {
            @Override
            protected void onDraw(Canvas canvas) {
                mCircles.draw(canvas);
                if (mCurTarget != null) {
                    mCurTarget.draw(canvas);
                    if (mNextTarget != null) {
                        mNextTarget.draw(canvas, true);
                    }
                }
                if (mCurPath != null) {
                    mCurPath.draw(canvas);
                }
                if (hasOrientation()) {
                    mPlayer.draw(canvas);
                }
            }
        };
    }

    @Override
    public void doPauseGame() {
        if (_logicThread != null) {
            _logicThread.cancel(true);
        }
    }

    @Override
    public void doResumeGame() {
        if (_logicThread == null || !_logicThread.getStatus().equals(AsyncTask.Status.RUNNING)) {
            _logicThread = new LogicThread();
            _logicThread.execute();
        }
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

    private class LogicThread extends AsyncTask<Void, Void, Void> {
        private long _lastTime = System.currentTimeMillis();
        private long _startTime;
        private long _deltaTime;
        private float _pitch;
        private float _roll;
        private float _distance;

        @Override
        protected Void doInBackground(Void... params) {
            while (!isCancelled()) {
                _startTime = System.currentTimeMillis();
                _deltaTime = _startTime - _lastTime;
                _lastTime = _startTime;
                if (hasOrientation()) {
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
            // _pitch = getScaledOrientation()[1];
            // _roll = getScaledOrientation()[2];
            _pitch = getOrientation()[1];
            _roll = getOrientation()[2];
            mPlayer.mPositionX = (_roll + 1.0f) / 2.0f;
            mPlayer.mPositionY = (_pitch + 1.0f) / 2.0f;
        }

        private void checkFinished() {
            switch (mMode) {
                case Targets:
                    if (mPlayer.intersects(mCurTarget)) {
                        mCurTarget.mRemainingHoldingTime -= _deltaTime;
                        if (mCurTarget.mRemainingHoldingTime < 0)
                            mFinished = true;
                    } else if (mCurTarget.mResetIfLeft) {
                        mCurTarget.mRemainingHoldingTime = mCurTarget.mHoldingTime;
                    }
                    break;
                case Paths:
                    // FIXME
                    break;
            }

        }

        private void updateStatus() {
            // Distance
            switch (mMode) {
                case Targets:
                    _distance = mPlayer.distanceTo(mCurTarget);
                    break;
                case Paths:
                    _distance = mPlayer.distanceTo(mCurPath);
                    break;
            }

            // Borders
            if (!mBordersSet) {
                mInnerBorder = _distance > mCurTarget.mRadius
                        ? _distance * 1.15f : mCurTarget.mRadius;
                mOuterBorder = mInnerBorder * 2.0f;
                mBordersSet = true;
            }
            else {
                if ((mInnerBorder - mInnerBorderShrinkStep) > mCurTarget.mRadius) {
                    mInnerBorder -= mInnerBorderShrinkStep;
                } else {
                    mInnerBorder = mCurTarget.mRadius;
                }
                if ((mOuterBorder - mOuterBorderShrinkStep) > mCurTarget.mRadius * 2.0f) {
                    mOuterBorder -= mOuterBorderShrinkStep;
                } else {
                    mOuterBorder = mCurTarget.mRadius * 2.0f;
                }
            }

            // Status
            mStatus = 1.0f + -(_distance - mInnerBorder) / (mOuterBorder - mInnerBorder);
            // Cutoff values between 0.0f and 1.0f
            mStatus = Math.min(1.0f, Math.max(mStatus, 0.0f));
            // filter
            mFilteredStatus += mFilterWeight * (mStatus - mFilteredStatus);
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            if (mFinished) {
                switch (mMode) {
                    case Targets:
                        if (mCurTargetIndex >= (mTargets.size() - 1)) {
                            notifyFinished(String
                                    .format("Fehlerfreiheit: %.0f %%", mHighscore * 100));
                            // mTargets.add(new Target(mRandom.nextFloat(),
                            // mRandom.nextFloat(),
                            // mRandom.nextFloat() / 10.0f + 0.02f, 0L, false));
                            // ++mCurIndex;
                            // mCurTarget = mTargets.get(mCurIndex);
                            // mInnerBorder = Float.MIN_VALUE;
                            // mFinished = false;
                        } else {
                            ++mCurTargetIndex;
                            mCurTarget = mTargets.get(mCurTargetIndex);
                            mCurTarget.mRemainingHoldingTime = mCurTarget.mHoldingTime;
                            mCurTarget.setDrawable(mTargetDrawable);
                            ;
                            if (mCurTargetIndex + 1 < mTargets.size()) {
                                mNextTarget = mTargets.get(mCurTargetIndex + 1);
                            } else {
                                mNextTarget = null;
                            }
                            mBordersSet = false;
                            mFinished = false;
                        }
                        break;
                    case Paths:
                        if (mCurPathIndex >= (mPaths.size() - 1)) {
                            notifyFinished(String
                                    .format("Fehlerfreiheit: %.0f %", mHighscore * 100));
                        } else {
                            ++mCurPathIndex;
                            mCurPath = mPaths.get(mCurPathIndex);
                            mBordersSet = false;
                            mFinished = false;
                        }
                        break;
                }
            }
            notifyStatusChanged(mFilteredStatus);
            // running average (e.g. after 5 Status calculations:
            // mHighscore * 4/5 + mStatus * 1/5)
            mHighscore = (mHighscore * (mHighscoreFactor - 1) / mHighscoreFactor) + mFilteredStatus
                    * (1 / mHighscoreFactor);
            mHighscoreFactor++;
            mGameView.invalidate();
        }

        @Override
        protected void onCancelled(Void result) {
            Log.v(Universal.TAG, "Thread: Cancelled");
            super.onCancelled(result);
        }
    }
}
