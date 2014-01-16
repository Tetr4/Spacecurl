
package de.klimek.spacecurl.game;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import de.klimek.spacecurl.R;
import de.klimek.spacecurl.util.sensor.SensorFilterListener;

public class GamePong extends GameFragment implements SensorFilterListener {
    public static final int TITLE_RESOURCE_ID = R.string.game_pong;

    private static enum GameMode {
        SingleAxis, MultiAxis
    }

    private GamePongView mGame;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mGame = new GamePongView(getActivity());
        return mGame;
    }

    @Override
    public void onSensorFilterChanged(float[] filteredOrientation) {
        mGame.onSensorFilterChanged(filteredOrientation);
    }

    @Override
    public void pauseGame() {
        mGame.pause();
    }

    @Override
    public void resumeGame() {
        mGame.resume();
    }

    private class GamePongView extends View implements SensorFilterListener {
        private static final String TAG = "GamePong";
        private static final int FPS = 30;
        private float[] mFilteredOrientation = new float[9];
        private AsyncTask<Void, Void, Void> _logicThread = new LogicThread();

        private int mViewWidthMin = 0; // This view's bounds
        private int mViewWidthMax;
        private int mViewHeightMin = 0;
        private int mViewHeightMax;

        private Ball mBall;
        private Paddle mPaddleUp;
        private Paddle mPaddleDown;
        private Paddle mPaddleLeft;
        private Paddle mPaddleRight;

        private int mPaddlePadding = 12;

        // Constructor
        public GamePongView(Context context) {
            super(context);
            mBall = new Ball();
            mPaddleUp = new Paddle();
            mPaddleDown = new Paddle();
            mPaddleLeft = new Paddle();
            mPaddleLeft.isHorizontal = false;
            mPaddleRight = new Paddle();
            mPaddleRight.isHorizontal = false;

        }

        // @Override
        // protected void onAttachedToWindow() {
        // super.onAttachedToWindow();
        // Log.v(TAG, "Thread: Started");
        // _logicThread = new LogicThread();
        // _logicThread.execute();
        // }
        //
        // @Override
        // protected void onDetachedFromWindow() {
        // super.onDetachedFromWindow();
        // _logicThread.cancel(true);
        // }

        public void pause() {
            _logicThread.cancel(true);
        }

        public void resume() {
            if (!_logicThread.getStatus().equals(AsyncTask.Status.RUNNING)) {
                _logicThread = new LogicThread();
                _logicThread.execute();
            }
        }

        @Override
        public void onSensorFilterChanged(float[] filteredOrientation) {
            synchronized (mFilteredOrientation) {
                mFilteredOrientation = filteredOrientation;
            }
        }

        // Called back to draw the view. Also called by invalidate().
        @Override
        protected void onDraw(Canvas canvas) {
            mBall.draw(canvas);
            mPaddleUp.draw(canvas);
            mPaddleDown.draw(canvas);
            mPaddleLeft.draw(canvas);
            mPaddleRight.draw(canvas);
        }

        private void updatePaddles() {
            mPaddleUp.mPositionX = 300;
            mPaddleUp.mPositionY = mPaddlePadding;
            mPaddleDown.mPositionX += 1;
            mPaddleDown.mPositionY = mViewHeightMax - mPaddlePadding;
            mPaddleLeft.mPositionX = mViewWidthMin + mPaddlePadding;
            mPaddleLeft.mPositionY += 2;
            mPaddleRight.mPositionX = mViewWidthMax - mPaddlePadding;
            mPaddleRight.mPositionY = 400;
        }

        private void updateBall() {
            // Get new (x,y) position
            mBall.mPositionX += mBall.mSpeedX;
            mBall.mPositionY += mBall.mSpeedY;

        }

        private void checkCollisions() {
            // Detect collision and react
            if (mBall.mPositionX + mBall.mRadius > mViewWidthMax) {
                mBall.mSpeedX = -mBall.mSpeedX;
                mBall.mPositionX = mViewWidthMax - mBall.mRadius;
            } else if (mBall.mPositionX - mBall.mRadius < mViewWidthMin) {
                mBall.mSpeedX = -mBall.mSpeedX;
                mBall.mPositionX = mViewWidthMin + mBall.mRadius;
            }
            if (mBall.mPositionY + mBall.mRadius > mViewHeightMax) {
                mBall.mSpeedY = -mBall.mSpeedY;
                mBall.mPositionY = mViewHeightMax - mBall.mRadius;
            } else if (mBall.mPositionY - mBall.mRadius < mViewHeightMin) {
                mBall.mSpeedY = -mBall.mSpeedY;
                mBall.mPositionY = mViewHeightMin + mBall.mRadius;
            }

            // TODO Check for paddle collision
            // serveBall()
            // increaseDifficulty
        }

        // Called back when the view is first created or its size changes.
        @Override
        public void onSizeChanged(int w, int h, int oldW, int oldH) {
            // Set the movement bounds for the ball
            mViewWidthMax = w - 1;
            mViewHeightMax = h - 1;
        }

        private class LogicThread extends AsyncTask<Void, Void, Void> {
            @Override
            protected Void doInBackground(Void... params) {
                while (!isCancelled()) {
                    updatePaddles();
                    updateBall();
                    checkCollisions();
                    publishProgress();
                    // Delay
                    try {
                        Thread.sleep(1000 / FPS); // 30 FPS
                    } catch (InterruptedException e) {
                        // e.printStackTrace();
                    }
                }
                return null;
            }

            @Override
            protected void onProgressUpdate(Void... values) {
                invalidate();
                synchronized (mFilteredOrientation) {
                    // Log.d(GamePongView.TAG,
                    // Arrays.toString(mFilteredOrientation));
                }
            }

            @Override
            protected void onCancelled(Void result) {
                Log.v(TAG, "Thread: Cancelled");
                super.onCancelled(result);
            }
        }

        private class Ball {
            private float mRadius = 40; // Ball's radius
            private float mPositionX = mRadius + 20; // Ball's center (x,y)
            private float mPositionY = mRadius + 40;
            private float mSpeedX = 10; // Ball's speed (x,y)
            private float mSpeedY = 7;
            private Paint mPaint = new Paint();

            protected void draw(Canvas canvas) {
                mPaint.setColor(Color.WHITE);
                canvas.drawCircle(mPositionX, mPositionY, mRadius, mPaint);
            }
        }

        private class Paddle {
            private int mWidth = 196;
            private int mHeight = 20;
            private int mPositionX = 0;
            private int mPositionY = 0;
            private Paint mPaint = new Paint();
            private Rect mRect = new Rect();
            private boolean isHorizontal = true;

            public Paddle() {
                mPaint.setColor(Color.WHITE);
                mRect.set(0,
                        0,
                        isHorizontal ? mWidth : mHeight,
                        isHorizontal ? mHeight : mWidth);
            }

            protected void draw(Canvas canvas) {
                int corWidth = isHorizontal ? mWidth : mHeight;
                int corHeight = isHorizontal ? mHeight : mWidth;
                mRect.set(0,
                        0,
                        corWidth,
                        corHeight);
                mRect.offsetTo(mPositionX - corWidth / 2
                        , mPositionY - corHeight / 2);
                canvas.drawRect(mRect, mPaint);
            }
        }
    }

}
