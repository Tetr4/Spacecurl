
package de.klimek.spacecurl.game;

import java.util.Random;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import de.klimek.spacecurl.R;

// TODO replace with 3d render, similar to http://code.google.com/p/stardroid/
public class GameUniversal extends GameFragment {
    public static final int DEFAULT_TITLE_RESOURCE_ID = R.string.game_universal;
    public static final String ARG_TARGET_POSITION_X = "ARG_TARGET_POSITION_X";
    public static final String ARG_TARGET_POSITION_Y = "ARG_TARGET_POSITION_Y";
    // TODO more complex shape. ellipse?
    public static final String ARG_TARGET_RADIUS = "ARG_TARGET_RADIUS";
    public static final String ARG_HOLDING_TIME = "ARG_HOLDING_TIME";
    public static final String ARG_RESET_HOLDING_TIME_IF_LEFT = "ARG_RESET_HOLDING_TIME_IF_LEFT";

    // private int score;
    private GameUniversalView mGame;
    private Effect[] mEffects;
    private FreeAxisCount mFreeAxisCount;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Random random = new Random();
        mGame = new GameUniversalView(getActivity());
        mGame.mResetIfLeft = getArguments().getBoolean(ARG_RESET_HOLDING_TIME_IF_LEFT, true);
        float targetPositionX = getArguments().getFloat(ARG_TARGET_POSITION_X,
                random.nextFloat());
        float targetPositionY = getArguments().getFloat(ARG_TARGET_POSITION_Y,
                random.nextFloat());
        float targetRadius = getArguments().getFloat(ARG_TARGET_RADIUS,
                random.nextFloat() / 10.0f + 0.02f);
        long holdingTime = getArguments().getLong(ARG_HOLDING_TIME, 0);
        mGame.mTarget = new Target(targetPositionX, targetPositionY, targetRadius, holdingTime);
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

        private float mPitch;
        private float mRoll;

        private boolean mResetIfLeft;
        private boolean mFinished = false;
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
            long _lastTime = System.currentTimeMillis();
            long _startTime;
            long _deltaTime;

            @Override
            protected Void doInBackground(Void... params) {
                while (!isCancelled()) {
                    _startTime = System.currentTimeMillis();
                    _deltaTime = _startTime - _lastTime;
                    _lastTime = _startTime;
                    if (mSizeChanged && hasOrientation()) {
                        updatePlayer();
                        checkFinished();
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
                mPitch = getScaledOrientation()[1];
                mRoll = getScaledOrientation()[2];
                mPlayer.mPositionX = (mRoll + 1.0f) / 2.0f;
                mPlayer.mPositionY = (mPitch + 1.0f) / 2.0f;
            }

            private void checkFinished() {
                if (mPlayer.intersects(mTarget)) {
                    mTarget.mCurHoldingTime -= _deltaTime;
                    if (mTarget.mCurHoldingTime < 0)
                        mFinished = true;
                } else if (mResetIfLeft) {
                    mTarget.mCurHoldingTime = mTarget.mHoldingTime;
                }

            }

            @Override
            protected void onProgressUpdate(Void... values) {
                if (mFinished) {
                    notifyFinished();
                    mTarget = new Target(mRandom.nextFloat(), mRandom.nextFloat(),
                            mRandom.nextFloat() / 10.0f + 0.02f, 0L);
                    mTarget.mCurHoldingTime = mTarget.mHoldingTime;
                    mFinished = false;
                }
                invalidate();
            }

            @Override
            protected void onCancelled(Void result) {
                Log.v(TAG, "Thread: Cancelled");
                super.onCancelled(result);
            }
        }

    }

    /**
     * Target
     */
    private static class Target {
        private float mPositionX;
        private float mPositionY;
        private float mRadius;
        private Paint mPaint;
        private long mHoldingTime;
        private long mCurHoldingTime;
        private int mMinBorder;
        private int mOnScreenPositionX;
        private int mOnScreenPositionY;
        private int mOnScreenRadius;

        private Target(float positionX, float positionY, float radius, long holdingTime) {
            mPositionX = positionX;
            mPositionY = positionY;
            mRadius = radius;
            mHoldingTime = holdingTime;
            mCurHoldingTime = mHoldingTime;
            mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            mPaint.setStyle(Paint.Style.FILL);
            mPaint.setColor(Color.RED);
        }

        private void draw(Canvas canvas) {
            mMinBorder = canvas.getWidth() <= canvas.getHeight() ? canvas.getWidth() : canvas
                    .getHeight();
            mOnScreenPositionX = (int) (mPositionX * mMinBorder - (mMinBorder - canvas.getWidth()) / 2);
            mOnScreenPositionY = (int) (mPositionY * mMinBorder - (mMinBorder - canvas.getHeight()) / 2);
            mOnScreenRadius = (int) (mRadius * mMinBorder);
            RectF r = new RectF(mOnScreenPositionX - mOnScreenRadius,
                    mOnScreenPositionY - mOnScreenRadius,
                    mOnScreenPositionX + mOnScreenRadius,
                    mOnScreenPositionY + mOnScreenRadius);
            canvas.drawOval(r, mPaint);
        }
    }

    /**
     * Player
     */
    private static class Player {
        private float mAxisLength;
        private float mPositionX;
        private float mPositionY;
        private Paint mPaint;
        private int mMinBorder;
        private int mOnScreenPositionX;
        private int mOnScreenPositionY;
        private int mOnScreenAxisLength;

        private Player(float axisLength) {
            mAxisLength = axisLength;
            mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            mPaint.setStyle(Paint.Style.STROKE);
            mPaint.setStrokeWidth(4);
            mPaint.setColor(Color.GREEN);
        }

        public boolean intersects(Target mTarget) {
            return (Math.pow((mPositionX - mTarget.mPositionX), 2)
                    + Math.pow((mPositionY - mTarget.mPositionY), 2)
                    < Math.pow(mTarget.mRadius, 2));
        }

        private void draw(Canvas canvas) {
            mMinBorder = canvas.getWidth() <= canvas.getHeight() ? canvas.getWidth() : canvas
                    .getHeight();
            mOnScreenPositionX = (int) (mPositionX * mMinBorder - (mMinBorder - canvas.getWidth()) / 2);
            mOnScreenPositionY = (int) (mPositionY * mMinBorder - (mMinBorder - canvas.getHeight()) / 2);
            mOnScreenAxisLength = (int) (mAxisLength * mMinBorder);
            canvas.drawLine(mOnScreenPositionX,
                    mOnScreenPositionY - mOnScreenAxisLength / 2,
                    mOnScreenPositionX,
                    mOnScreenPositionY + mOnScreenAxisLength / 2,
                    mPaint);
            canvas.drawLine(mOnScreenPositionX - mOnScreenAxisLength / 2,
                    mOnScreenPositionY,
                    mOnScreenPositionX + mOnScreenAxisLength / 2,
                    mOnScreenPositionY,
                    mPaint);
        }
    }

    /**
     * CenteredCircles
     */
    private static class CenteredCircles {
        private int mPositionX;
        private int mPositionY;
        private int mCircleCount = 6;
        private int mRadiusIncrement;
        private Paint mPaint;

        private CenteredCircles(int circleCount, int radiusIncrement, int positionX,
                int positionY) {
            mCircleCount = circleCount;
            // mMinBorder / (mCircleCount * 2)
            mRadiusIncrement = radiusIncrement;
            mPositionX = positionX;
            mPositionY = positionY;
            mPaint = new Paint();
            mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            mPaint.setStyle(Paint.Style.STROKE);
            mPaint.setStrokeWidth(4);
            mPaint.setColor(Color.MAGENTA);
        }

        private void draw(Canvas canvas) {
            for (int i = 1; i < mCircleCount; i++) {
                int radius = mRadiusIncrement * i;
                RectF oval = new RectF(
                        mPositionX - radius,
                        mPositionY - radius,
                        mPositionX + radius,
                        mPositionY + radius);
                canvas.drawArc(oval, 0, 360, false, mPaint);
                radius += mRadiusIncrement;
            }
        }
    }
}
