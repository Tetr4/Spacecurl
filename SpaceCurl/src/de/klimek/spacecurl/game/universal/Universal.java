
package de.klimek.spacecurl.game.universal;

import java.util.ArrayList;
import java.util.Random;

import android.graphics.Canvas;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import de.klimek.spacecurl.game.GameFragment;

public class Universal extends GameFragment {
    private Effect[] mEffects;
    private FreeAxisCount mFreeAxisCount;

    private Random mRandom = new Random();

    private AsyncTask<Void, Void, Void> _logicThread = new LogicThread();

    private UniversalSettings mSettings;
    private View mGameView;

    private Player mPlayer = new Player(0.1f);
    private CenteredCircles mCircles = new CenteredCircles(6);
    private ArrayList<Target> mTargets = new ArrayList<Target>();
    // private ArrayList<Path> mPaths = new ArrayList<Path>();
    private int mCurIndex;
    private Target mCurTarget;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setupSettings();
        setupView();
        resumeGame();
        return mGameView;
    }

    private void setupSettings() {
        mSettings = (UniversalSettings) getSettings();
        if (!mSettings.getTargets().isEmpty()) { // TARGET
            mTargets = mSettings.getTargets();
            mCurIndex = 0;
            mCurTarget = mTargets.get(mCurIndex);
        }
        // else if (!mSettings.getPaths().isEmpty()) { // PATH
        // mPaths = mSettings.getPaths();
        // }
        // else if (false) { // AXIS
        // }
        else { // WARM UP
            mTargets.add(new Target(mRandom.nextFloat(), mRandom.nextFloat(),
                    mRandom.nextFloat() / 10.0f + 0.02f, 0L, false));
            mCurIndex = 0;
            mCurTarget = mTargets.get(mCurIndex);
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
                }
                if (hasOrientation()) {
                    mPlayer.draw(canvas);
                }
            }
        };
    }

    @Override
    public void pauseGame() {
        _logicThread.cancel(true);
    }

    @Override
    public void resumeGame() {
        if (!_logicThread.getStatus().equals(AsyncTask.Status.RUNNING)) {
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
        private float _innerBorder;
        private float _innerBorderShrinkStep = 0.005f;
        private float _outerBorder;
        private float _outerBorderShrinkStep = 0.002f;
        private float _outerBorderWidth = 0.4f;
        private boolean _bordersSet = false;
        private boolean _finished = false;
        private float _status;

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
            _pitch = getScaledOrientation()[1];
            _roll = getScaledOrientation()[2];
            mPlayer.mPositionX = (_roll + 1.0f) / 2.0f;
            mPlayer.mPositionY = (_pitch + 1.0f) / 2.0f;
        }

        private void checkFinished() {
            if (mPlayer.intersects(mCurTarget)) {
                mCurTarget.mRemainingHoldingTime -= _deltaTime;
                if (mCurTarget.mRemainingHoldingTime < 0)
                    _finished = true;
            } else if (mCurTarget.mResetIfLeft) {
                mCurTarget.mRemainingHoldingTime = mCurTarget.mHoldingTime;
            }
        }

        private void updateStatus() {
            _distance = mPlayer.distanceTo(mCurTarget);
            if (!_bordersSet) {
                _innerBorder = _distance * 1.05f;
                _outerBorder = _innerBorder + _outerBorderWidth;
                _bordersSet = true;
            }
            else {
                _innerBorder -= _innerBorderShrinkStep;
                _outerBorder -= _outerBorderShrinkStep;
            }
            _outerBorder = _innerBorder + _outerBorderWidth;
            _status = 1 + -(_distance - _innerBorder) / (_outerBorder - _innerBorder);
            // Cutoff values between 0.0f and 1.0f
            _status = Math.min(1.0f, Math.max(_status, 0.0f));

        }

        @Override
        protected void onProgressUpdate(Void... values) {
            if (_finished) {
                if (mCurIndex >= (mTargets.size() - 1)) {
                    notifyFinished();
                    // mTargets.add(new Target(mRandom.nextFloat(),
                    // mRandom.nextFloat(),
                    // mRandom.nextFloat() / 10.0f + 0.02f, 0L, false));
                    // ++mCurIndex;
                    // mCurTarget = mTargets.get(mCurIndex);
                    // _innerBorder = Float.MIN_VALUE;
                    // _finished = false;
                } else {
                    ++mCurIndex;
                    mCurTarget = mTargets.get(mCurIndex);
                    _bordersSet = false;
                    _finished = false;
                }
            }
            notifyStatusChanged(_status);
            mGameView.invalidate();
        }

        @Override
        protected void onCancelled(Void result) {
            Log.v(Universal.TAG, "Thread: Cancelled");
            super.onCancelled(result);
        }
    }
}
