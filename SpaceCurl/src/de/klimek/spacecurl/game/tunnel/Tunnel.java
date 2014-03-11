
package de.klimek.spacecurl.game.tunnel;

import java.util.LinkedList;
import java.util.Random;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import de.klimek.spacecurl.R;
import de.klimek.spacecurl.game.GameFragment;

public class Tunnel extends GameFragment {
    private static final String TAG = "Tunnel";
    private static final int FPS = 30;
    private AsyncTask<Void, Void, Void> _logicThread = new LogicThread();

    private View mGame;
    private FrameLayout mTunnelBar;
    private FrameLayout mTunnelLayout;
    private LinearLayout mResultLayout;
    private TextView mResultScore;
    private TextView mResultContinueTime;
    private TextView mTunnelScore;
    private TunnelSettings mSettings;

    // Drawing variables
    private int mViewWidthMax;
    private int mViewHeightMax;
    private Bitmap mBitmap;
    private int mBitmapWidth;
    private int mBufferColumns = 10;
    private int mViewPortOffset = 0;
    private Canvas mBufferCanvas;
    private AnimatedSprite mExplosion;
    private Bitmap mExplosionBitmap;
    private View mLivesView;

    // Game difficulty parameters
    private int mCurveWidth = 450;
    private int mSpeed = 3;
    private int mMinTunnelHeight = 190;
    private int mPadding = 12;
    private Player mPlayer;
    private int mTotalLives;
    private Lives mLives;
    private float mStatusStepSize = 0.001f;

    // Tunnel variables
    private LinkedList<Wall> mTunnel = new LinkedList<Wall>();
    private float mTunnelHeight;
    private int mCurvePrevious;
    private int mCurveNext;
    private int mCurveStep = 0;

    // Sensor variables
    private float mPitch;

    // Logic variables
    private float mDistance = 0;
    private int mRemainingTimeUntilContinue = 6000;
    private Stage mStage = Stage.SizeNotSet;
    private boolean mExplode = false;
    private boolean mShowLives;

    // status variables
    private float mStatus = 1.0f;
    private float mHighscore = 0;

    private static enum Stage {
        SizeNotSet, Running, GameOver, Result, Restart
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.game_tunnel, container, false);

        // Settings
        mSettings = (TunnelSettings) getSettings();
        mTotalLives = mSettings.getLives();
        mShowLives = mSettings.showLives();

        // Objects
        mPlayer = new Player(96, 59, getResources());
        mLives = new Lives(mTotalLives, (int) (mPlayer.getWidth() * 0.75f),
                (int) (mPlayer.getHeight() * 0.75f), getResources());
        mExplosionBitmap = BitmapFactory.decodeStream(getResources().openRawResource(
                R.drawable.explosion));

        // result screen
        mResultLayout = (LinearLayout) rootView.findViewById(R.id.game_result_layout);
        mTunnelScore = (TextView)
                rootView.findViewById(R.id.game_tunnel_score);
        mResultScore = (TextView)
                rootView.findViewById(R.id.game_result_score);
        mResultContinueTime = (TextView)
                rootView.findViewById(R.id.game_result_continue_time);

        // main layout
        mTunnelLayout = (FrameLayout) rootView.findViewById(R.id.game_tunnel_layout);
        mGame = new View(getActivity()) {
            // Called back to draw the view. Also called by invalidate().
            @Override
            protected void onDraw(Canvas canvas) {
                canvas.drawBitmap(mBitmap, -mViewPortOffset, 0, null);
                canvas.drawBitmap(mBitmap, -mViewPortOffset + mBitmapWidth, 0, null);
                if (hasOrientation()) {
                    mPlayer.draw(canvas);
                }
                if (mExplode) {
                    mExplosion.draw(canvas);
                }
            }

            // Called back when the view is first created or its size changes.
            @Override
            public void onSizeChanged(int w, int h, int oldW, int oldH) {
                // Screen height/width dependent variables
                mViewWidthMax = w - 1;
                mViewHeightMax = h - 1;
                mTunnelHeight = mViewHeightMax * 1.5f;
                mCurvePrevious = mViewHeightMax / 2;
                mCurveNext = mCurvePrevious;

                // bitmap and Canvas
                mBitmapWidth = (int) (mViewWidthMax + mBufferColumns);
                mBitmap = Bitmap.createBitmap(mBitmapWidth, mViewHeightMax,
                        Bitmap.Config.ARGB_8888);
                mBufferCanvas = new Canvas(mBitmap);
                mBufferCanvas.clipRect(0, 0, mBitmapWidth, mViewHeightMax);
                mBufferCanvas.drawColor(Color.BLACK);

                // fill bitmap
                mTunnel.clear();
                for (int i = 0; i < mBitmapWidth; i++) {
                    Wall wall = generateRightmostWall();
                    // add to list
                    mTunnel.add(wall);
                    // draw
                    wall.draw(mBufferCanvas, i);
                }
                mStage = Stage.Running;
                invalidate();
            }
        };
        mTunnelLayout.addView(mGame);

        // Top bar with score and lives
        mTunnelBar = (FrameLayout) rootView.findViewById(R.id.tunnel_bar);
        mLivesView = new View(getActivity()) {
            @Override
            protected void onDraw(Canvas canvas) {
                if (mShowLives) {
                    mLives.draw(canvas);
                }
            }
        };
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        params.gravity = Gravity.CENTER;
        mTunnelBar.addView(mLivesView, params);

        return rootView;
    }

    @Override
    public void doPauseGame() {
        _logicThread.cancel(true);
    }

    @Override
    public void doResumeGame() {
        if (!_logicThread.getStatus().equals(AsyncTask.Status.RUNNING)) {
            _logicThread = new LogicThread();
            _logicThread.execute();
        }
    }

    @Override
    public FreeAxisCount getFreeAxisCount() {
        return FreeAxisCount.One;
    }

    @Override
    public Effect[] getEffects() {
        Effect[] e = {
                Effect.Accuracy,
                Effect.Speed
        };
        return e;
    }

    private static float interpolate(int y1, int y2, float mu) {
        float mu2 = (float) ((1 - Math.cos(mu * Math.PI)) / 2.0f);
        return (y1 * (1 - mu2) + y2 * mu2);
    }

    private static String timeToString(long time, boolean showMillis) {
        if (showMillis) {
            return "" + time / 1000
                    + ":" + time % 1000;
        } else {
            return "" + time / 1000;
        }
    }

    private Wall generateRightmostWall() { // warning: lots of side effects
        // update tunnel position
        int tunnelPosY = (int) interpolate(mCurvePrevious, mCurveNext,
                (float) mCurveStep / mCurveWidth);

        double deltaY = interpolate(mCurvePrevious, mCurveNext,
                (float) (mCurveStep + 1) / mCurveWidth)
                - interpolate(mCurvePrevious, mCurveNext,
                        (float) (mCurveStep) / mCurveWidth);
        int deltaX = 1;
        double alpha = Math.atan((Math.abs((float) (deltaY / deltaX))));
        double beta = 180 - 90 - Math.toDegrees(alpha);
        float c = (float) (mTunnelHeight / Math.sin(Math.toRadians(beta)));

        mCurveStep = (mCurveStep + 1) % mCurveWidth;
        if (mCurveStep == 0) {
            mCurvePrevious = mCurveNext;
            mCurveNext = new Random().nextInt(mViewHeightMax);
        }

        // update tunnelHeight
        mTunnelHeight = ((mTunnelHeight - mMinTunnelHeight) * 0.999f)
                + mMinTunnelHeight;

        // generate wall
        return new Wall((int) (tunnelPosY - c / 2.0f),
                (int) (tunnelPosY + c / 2.0f));
    }

    private class LogicThread extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            long _lastTime = System.currentTimeMillis();
            long _startTime;
            long _deltaTime;
            long _timeEnd;
            long _timeSleep;
            while (!isCancelled()) {
                _startTime = System.currentTimeMillis();
                _deltaTime = _startTime - _lastTime;
                _lastTime = _startTime;
                switch (mStage) {
                    case SizeNotSet:
                        // Do nothing
                        break;

                    case Running:
                        for (int i = 0; i < mSpeed; i++) {
                            updateTunnel();
                        }
                        updatePlayer();
                        checkCollisions();
                        mDistance += 0.1;
                        mHighscore = mDistance > mHighscore ? mDistance : mHighscore;
                        break;

                    case GameOver:
                        if (mExplode) {
                            updateExplosion();
                        }
                        break;

                    case Restart:
                        reset();
                        mStage = Stage.Running;
                        break;

                    case Result:
                        mRemainingTimeUntilContinue -= _deltaTime;
                        if (mRemainingTimeUntilContinue < 1000) {
                            mStage = Stage.Restart;
                        }
                        break;
                }
                publishProgress();

                _timeEnd = System.currentTimeMillis();
                _timeSleep = (1000 / FPS) - (_timeEnd - _startTime);
                // Delay
                try {
                    Thread.sleep(_timeSleep < 0 ? 0 : _timeSleep);
                } catch (InterruptedException e) {
                    // e.printStackTrace();
                }
            }
            return null;
        }

        private void updateExplosion() {
            long now = System.currentTimeMillis();
            mExplosion.Update(now);
            if (mExplosion.dispose) {
                mExplode = false;
                mExplosion = null;
            }
        }

        private void updatePlayer() {
            mPitch = getScaledOrientation()[1];
            mPlayer.setPositionX(mPadding + mPlayer.getWidth() / 2);
            mPlayer.setPositionY((int) ((mPitch + 1.0f) / 2.0f
                    * (mViewHeightMax - (mPlayer.getWidth() + mPadding * 2))
                    + mPlayer.getWidth() / 2 + mPadding));
        }

        private void updateTunnel() {
            Wall wall = generateRightmostWall();
            // add to list, keep size
            mTunnel.removeFirst();
            mTunnel.addLast(wall);
            // draw offscreen
            wall.draw(mBufferCanvas, (mViewPortOffset + mViewWidthMax + (mBufferColumns / 2))
                    % mBitmapWidth);
            mViewPortOffset = (mViewPortOffset + 1) % mBitmapWidth;
        }

        private void checkCollisions() {
            // check for every x coordinate along hitbox
            for (int i = mPlayer.getPositionX() - mPlayer.getWidth() / 2; i <= mPlayer
                    .getPositionX()
                    + mPlayer.getWidth() / 2; i++) {
                // check collision with top or bottom wall at cur. x coordinate
                Wall wall = mTunnel.get(i);
                if (wall.getTop() > mPlayer.getPositionY() - mPlayer.getHeight() / 3
                        || wall.getBottom() < mPlayer.getPositionY() + mPlayer.getHeight() / 3) {
                    mStage = Stage.GameOver;
                    // lost life
                    if (mLives.getLives() > 0) {
                        mLives.setLives(mLives.getLives() - 1);
                    }
                    // update status
                    if (mDistance <= 20f) { // bad
                        mStatus -= 0.5f;
                    } else if (mDistance < 70f) { // okay
                        // 30 -> .4
                        // 40 -> .3
                        // 50 -> .2
                        // 60 -> .1
                        mStatus -= 0.5 - ((mDistance - 20) / 10f);
                    }

                    // Begin explosion
                    mExplosion = new AnimatedSprite();
                    mExplosion.Initialize(mExplosionBitmap, 120, 160, FPS, 20, false);
                    mExplosion.setXPos(mPlayer.getPositionX());
                    mExplosion.setYPos(mPlayer.getPositionY());
                    mExplode = true;
                    return;
                }
            }
            // no collision
            mStatus += mStatusStepSize;
            mStatus = Math.min(mStatus, 1.0f);
        }

        private void reset() {
            mDistance = 0;
            mRemainingTimeUntilContinue = 8000;
            mViewPortOffset = 0;
            mCurveStep = 0;
            mCurvePrevious = mViewHeightMax / 2;
            mCurveNext = mCurvePrevious;
            mTunnelHeight = mViewHeightMax * 1.5f;
            mTunnel.clear();
            for (int i = 0; i < mBitmapWidth; i++) {
                Wall wall = generateRightmostWall();
                // add to list
                mTunnel.add(wall);
                // draw
                wall.draw(mBufferCanvas, i);
            }
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            switch (mStage) {
                case GameOver:
                    if (mLives.getLives() <= 0 && !mExplode) {
                        boolean handled = notifyFinished(String.format(
                                "Längste geschaffte Strecke: %.0f m", mHighscore));
                        if (!handled) {
                            // mResultLayout.startAnimation(AnimationUtils.loadAnimation(
                            // getActivity(),
                            // R.anim.result_anim));
                            // mResultLayout.setVisibility(View.VISIBLE);
                            mLives.setLives(mTotalLives);
                            mStage = Stage.Restart;
                        }
                    } else if (!mExplode) {
                        mStage = Stage.Restart;
                    }
                    break;

                case Result:
                    mResultContinueTime
                            .setText(timeToString(mRemainingTimeUntilContinue, false));
                    mResultScore.setText(String.format("%.1f m", mDistance));
                    break;

                case Running:
                    mResultLayout.setVisibility(View.INVISIBLE);
                    mTunnelScore.setText(String.format("%.0f m", mDistance));
                    break;

                case SizeNotSet:
                    break;

                case Restart:
                    break;

            }

            // notifyStatusChanged(mStatus); // FIXME laggy. more threads?
            mGame.invalidate();
            mLivesView.invalidate();
        }

        @Override
        protected void onCancelled(Void result) {
            Log.v(TAG, "Thread: Cancelled");
            super.onCancelled(result);
        }
    }

}
