
package de.klimek.spacecurl.game.pong;

import java.util.Random;

import android.content.res.Resources;
import android.graphics.Canvas;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;
import de.klimek.spacecurl.R;
import de.klimek.spacecurl.game.GameFragment;

public class Pong extends GameFragment {
    private FrameLayout mPongLayout;
    private TextView mPongScore;

    private static final String TAG = "Pong";
    private static final int FPS = 30;
    private AsyncTask<Void, Void, Void> _logicThread = new LogicThread();

    private int mViewWidthMin = 0; // This view's bounds
    private int mViewWidthMax;
    private int mViewHeightMin = 0;
    private int mViewHeightMax;
    private boolean mHaveSize = false;

    private Ball mBall;
    private Paddle mPaddleTop;
    private Paddle mPaddleBottom;
    private Paddle mPaddleLeft;
    private Paddle mPaddleRight;

    private float mPitch;
    private float mRoll;

    private int mTotalLives;
    private Lives mLives;
    private boolean mShowLives;
    private int mBallContacts;
    private boolean mFinished = false;
    private int mHighscore = 0;
    private boolean mStatusUpdate = true;
    public float mStatus = 1.0f;
    public float mStatusStepSize = 0.1f;

    private View mGame;
    private PongSettings mSettings;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mSettings = (PongSettings) getSettings();

        mBall = new Ball(getResources());
        Resources res = getResources();
        mShowLives = mSettings.showLives();
        mTotalLives = mSettings.getLives();

        mLives = new Lives(mTotalLives, res);
        mPaddleLeft = new Paddle(Paddle.PaddleSide.Left, res);
        mPaddleTop = new Paddle(Paddle.PaddleSide.Top, res);
        mPaddleRight = new Paddle(Paddle.PaddleSide.Right, res);
        mPaddleBottom = new Paddle(Paddle.PaddleSide.Bottom, res);

        View rootView = inflater.inflate(R.layout.game_pong, container, false);
        mPongLayout = (FrameLayout) rootView.findViewById(R.id.game_pong_layout);
        mPongScore = (TextView) mPongLayout.findViewById(R.id.game_pong_score);
        mGame = new View(getActivity()) { // Called back to draw the view. Also
            @Override
            protected void onDraw(Canvas canvas) {
                if (mShowLives) {
                    mLives.draw(canvas);
                }
                if (hasOrientation()) {
                    mPaddleTop.draw(canvas);
                    mPaddleBottom.draw(canvas);
                    mPaddleLeft.draw(canvas);
                    mPaddleRight.draw(canvas);
                }
                mBall.draw(canvas);
            }

            // Called back when the view is first created or its size changes.
            @Override
            public void onSizeChanged(int w, int h, int oldW, int oldH) {
                // Set the movement bounds for the ball
                mViewWidthMax = w - 1;
                mViewHeightMax = h - 1;
                mHaveSize = true;
            }
        };
        mPongLayout.addView(mGame);
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
        return FreeAxisCount.Two;
    }

    @Override
    public Effect[] getEffects() {
        Effect[] e = {
                Effect.Accuracy,
                Effect.Speed
        };
        return e;
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
                if (mHaveSize) {
                    updatePaddles();
                    updateBall();
                    checkCollisions(_deltaTime);
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

        private void updatePaddles() {
            mPitch = getScaledOrientation()[1];
            mRoll = getScaledOrientation()[2];
            mPaddleLeft.mPosition = mPitch;
            mPaddleTop.mPosition = -mRoll;
            mPaddleRight.mPosition = mPitch;
            mPaddleBottom.mPosition = mRoll;
        }

        private void updateBall() {
            if (mBall.getFaceState() != Ball.FaceState.Frowning) {
                // Get new (x,y) position
                mBall.setPositionX(mBall.getPositionX() + mBall.getSpeedX());
                mBall.setPositionY(mBall.getPositionY() + mBall.getSpeedY());
            }
        }

        private void checkCollisions(long deltaTime) {
            // ball has collided with wall
            if (mBall.getFaceState() == Ball.FaceState.Frowning) {
                if (mBall.getRemainingFaceTime() > 0) {
                    mBall.setRemainingFaceTime(mBall.getRemainingFaceTime() - deltaTime);
                } else if (mLives.getLives() <= 0) {
                    mFinished = true;
                } else {
                    // reset
                    mBallContacts = 0;
                    mBall.setFaceState(Ball.FaceState.Normal);
                    mBall.setPositionX(mViewWidthMax / 2);
                    mBall.setPositionY(mViewHeightMax / 2);
                    mBall.setSpeedX(new Random().nextBoolean() ? mBall.getSpeedX() : -mBall
                            .getSpeedX());
                    mBall.setSpeedY(new Random().nextBoolean() ? mBall.getSpeedY() : -mBall
                            .getSpeedY());
                    // mBall.mSpeed += mBallSpeedModifier;
                    // mBall.randomAngle();
                }
                return; // returns instead of "else if" for readability
            }

            // Check paddle collisions and react
            // Top
            if (mBall.collidesWithPaddle(mPaddleTop)) {
                mBall.setSpeedY(Math.abs(mBall.getSpeedY()));
                doAfterCollision();
                return;
            }
            // Bottom
            if (mBall.collidesWithPaddle(mPaddleBottom)) {
                mBall.setSpeedY(-Math.abs(mBall.getSpeedY()));
                doAfterCollision();
                return;
            }
            // Left
            if (mBall.collidesWithPaddle(mPaddleLeft)) {
                mBall.setSpeedX(Math.abs(mBall.getSpeedX()));
                doAfterCollision();
                return;
            }
            // Right
            if (mBall.collidesWithPaddle(mPaddleRight)) {
                mBall.setSpeedX(-Math.abs(mBall.getSpeedX()));
                doAfterCollision();
                return;
            }

            // Wall collision
            if (mBall.collidesWithWall(mViewWidthMin, mViewHeightMin, mViewWidthMax,
                    mViewHeightMax)) {
                mBall.setRemainingFaceTime(500);
                mBall.setFaceState(Ball.FaceState.Frowning);

                // update status
                if (mBallContacts <= 0) { // bad
                    mStatus -= 0.5f;
                } else if (mBallContacts < 5) { // okay
                    // 1 -> .4
                    // 2 -> .3
                    // 3 -> .2
                    // 4 -> .1
                    mStatus -= 0.5 - (mBallContacts / 10f);
                }
                mStatusUpdate = true;

                // lost a life
                if (mLives.getLives() > 0) {
                    mLives.setLives(mLives.getLives() - 1);
                }
                return;
            }

            // no collision and
            mBall.setRemainingFaceTime(mBall.getRemainingFaceTime() - deltaTime);
            if (mBall.getRemainingFaceTime() <= 0) {
                mBall.setFaceState(Ball.FaceState.Normal);
            }
        }

        private void doAfterCollision() {
            ++mBallContacts;
            mHighscore = mBallContacts > mHighscore ? mBallContacts : mHighscore;
            mStatus += mStatusStepSize;
            mStatus = Math.min(mStatus, 1.0f);
            mStatusUpdate = true;
            mBall.setRemainingFaceTime(500);
            mBall.setFaceState(Ball.FaceState.Smiling);
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            mPongScore.setText(Integer.toString(mBallContacts));
            if (mStatusUpdate) {
                notifyStatusChanged(mStatus);
                mStatusUpdate = false;
            }

            if (mFinished) {
                boolean handled = notifyFinished(String.format("Beste Score: %d", mHighscore));
                if (!handled) {
                    mLives.setLives(mTotalLives);
                    mFinished = false;
                    mHighscore = 0;
                }
            }

            mGame.invalidate();
        }

        @Override
        protected void onCancelled(Void result) {
            Log.v(TAG, "Thread: Cancelled");
            super.onCancelled(result);
        }
    }

}
