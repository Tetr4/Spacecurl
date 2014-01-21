
package de.klimek.spacecurl.game;

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
import de.klimek.spacecurl.util.collection.Database;

public class GameUniversal extends GameFragment {
    public static final int DEFAULT_TITLE_RESOURCE_ID = R.string.game_universal;
    public static final String ARG_TARGET_POSITION = "ARG_TARGET_POSITION";
    public static final String ARG_TOLERANCE = "ARG_TOLERANCE";
    public static final String ARG_HOLDING_TIME = "ARG_HOLDING_TIME";
    public static final String ARG_RESET_IF_LEFT = "ARG_RESET_IF_LEFT";

    // private int score;
    private GameUniversalView mGame;
    private Effect[] mEffects;
    private FreeAxisCount mFreeAxisCount;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // TODO get & set Target Position
        // getArguments().get...(ARG_TARGET_POSITION);
        mGame = new GameUniversalView(getActivity());
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

        private boolean mFinished = false;

        private Player mPlayer;
        private Target mTarget;
        private CenteredCircles mCircles;

        private float mPitch;
        private float mRoll;
        private float mInclinationRangeFactor = 2.0f;
        // TODO Calibrate
        // Phone is not attached straight for better visibilty of the screen
        private float mPhoneInclination;

        public GameUniversalView(Context context) {
            super(context);
            mPhoneInclination = Database.getInstance().getPhoneInclination();
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

            mPlayer = new Player(mMinBorder / 10);
            mPlayer.mPositionX = mCenterX;
            mPlayer.mPositionY = mCenterY;

            mTarget = new Target(mMinBorder / 24, mCenterX - mMinBorder / 6, mCenterY + mMinBorder
                    / 4);

            int circleCount = 6;
            mCircles = new CenteredCircles(circleCount, mMinBorder / (circleCount * 2), mCenterX,
                    mCenterY);
            mSizeChanged = true;
        }

        @Override
        protected void onDraw(Canvas canvas) {
            mTarget.draw(canvas);
            mPlayer.draw(canvas);
            mCircles.draw(canvas);
        }

        private class LogicThread extends AsyncTask<Void, Void, Void> {
            @Override
            protected Void doInBackground(Void... params) {
                while (!isCancelled()) {
                    if (mSizeChanged) {
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
                mPlayer.mPositionX = (int) (mPitch * mMinBorder);
                mPlayer.mPositionY = (int) (mRoll * mMinBorder);

            }

            private void checkFinished() {
                if (mPlayer.intersects(mTarget)) {
                    mFinished = true;
                }

            }

            @Override
            protected void onProgressUpdate(Void... values) {
                if (mFinished) {
                    notifyFinished();
                    this.cancel(true);
                }
                mPitch = ((getOrientation()[1] / (float) Math.PI) * mInclinationRangeFactor)
                        + .5f;
                mRoll = ((getOrientation()[2] / (float) Math.PI) * mInclinationRangeFactor)
                        + mPhoneInclination;
                invalidate();
            }

            @Override
            protected void onCancelled(Void result) {
                Log.v(TAG, "Thread: Cancelled");
                super.onCancelled(result);
            }
        }

        /**
         * Target
         */
        private class Target {
            private int mRadius;
            private int mPositionX;
            private int mPositionY;
            private Paint mPaint;

            private Target(int radius, int positionX, int positionY) {
                mRadius = radius;
                mPositionX = positionX;
                mPositionY = positionY;
                mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
                mPaint.setStyle(Paint.Style.FILL);
                mPaint.setColor(Color.RED);
            }

            private void draw(Canvas canvas) {
                RectF r = new RectF(mPositionX - mRadius,
                        mPositionY - mRadius,
                        mPositionX + mRadius,
                        mPositionY + mRadius);
                canvas.drawOval(r, mPaint);
            }
        }

        /**
         * Player
         */
        private class Player {
            private int mAxisLength;
            private int mPositionX;
            private int mPositionY;
            private Paint mPaint;

            private Player(int axisLength) {
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
                canvas.drawLine(mPositionX,
                        mPositionY - mAxisLength / 2,
                        mPositionX,
                        mPositionY + mAxisLength / 2,
                        mPaint);
                canvas.drawLine(mPositionX - mAxisLength / 2,
                        mPositionY,
                        mPositionX + mAxisLength / 2,
                        mPositionY,
                        mPaint);
            }
        }

        /**
         * CenteredCircles
         */
        private class CenteredCircles {
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
}
