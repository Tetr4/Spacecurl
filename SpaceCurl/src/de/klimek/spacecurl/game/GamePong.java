
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
import de.klimek.spacecurl.util.collection.Database;

public class GamePong extends GameFragment {
    public static final int DEFAULT_TITLE_RESOURCE_ID = R.string.game_pong;

    private GamePongView mGame;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mGame = new GamePongView(getActivity());
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

    private class GamePongView extends View {
        private static final String TAG = "GamePong";
        private static final int FPS = 30;
        private AsyncTask<Void, Void, Void> _logicThread = new LogicThread();

        private int mViewWidthMin = 0; // This view's bounds
        private int mViewWidthMax = 0;
        private int mViewHeightMin = 0;
        private int mViewHeightMax = 0;

        private Ball mBall;
        private Paddle mPaddleUp;
        private Paddle mPaddleDown;
        private Paddle mPaddleLeft;
        private Paddle mPaddleRight;
        private int mPaddlePadding = 12;

        private float mPitch;
        private float mRoll;
        private float mInclinationRangeFactor = 2.0f;
        // TODO Calibrate
        // Phone is not attached straight for better visibilty of the screen
        private float mPhoneInclination;

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
            mPhoneInclination = Database.getInstance().getPhoneInclination();
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

        // Called back to draw the view. Also called by invalidate().
        @Override
        protected void onDraw(Canvas canvas) {
            mBall.draw(canvas);
            mPaddleUp.draw(canvas);
            mPaddleDown.draw(canvas);
            mPaddleLeft.draw(canvas);
            mPaddleRight.draw(canvas);
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
                long timeStart;
                long timeEnd;
                long timeSleep;
                while (!isCancelled()) {
                    timeStart = System.currentTimeMillis();

                    updatePaddles();
                    updateBall();
                    checkCollisions();
                    publishProgress();

                    timeEnd = System.currentTimeMillis();
                    timeSleep = (1000 / FPS) - (timeEnd - timeStart);
                    // Delay
                    try {
                        Thread.sleep(timeSleep < 0 ? 0 : timeSleep);
                    } catch (InterruptedException e) {
                        // e.printStackTrace();
                    }
                }
                return null;
            }

            @Override
            protected void onProgressUpdate(Void... values) {
                invalidate();
                // range 0.0 - 1.0
                mPitch = ((getOrientation()[1] / (float) Math.PI) * mInclinationRangeFactor)
                        + .5f;
                mRoll = ((getOrientation()[2] / (float) Math.PI) * mInclinationRangeFactor)
                        + mPhoneInclination;
            }

            @Override
            protected void onCancelled(Void result) {
                Log.v(TAG, "Thread: Cancelled");
                super.onCancelled(result);
            }

            private void updatePaddles() {
                mPaddleUp.mPositionX = (int) (mPitch * mViewWidthMax);
                mPaddleUp.mPositionY = mPaddlePadding;
                mPaddleDown.mPositionX = (int) (mPitch * mViewWidthMax);
                mPaddleDown.mPositionY = mViewHeightMax - mPaddlePadding;
                mPaddleLeft.mPositionX = mViewWidthMin + mPaddlePadding;
                mPaddleLeft.mPositionY = (int) (mRoll * mViewHeightMax);
                mPaddleRight.mPositionX = mViewWidthMax - mPaddlePadding;
                mPaddleRight.mPositionY = (int) (mRoll * mViewHeightMax);
            }

            private void updateBall() {
                // Get new (x,y) position
                mBall.mPositionX += mBall.mSpeedX;
                mBall.mPositionY += mBall.mSpeedY;
            }

            private void checkCollisions() {
                // Check paddle collisions and react
                if (mBall.collidesWithPaddle(mPaddleUp)) {
                    mBall.mSpeedY = Math.abs(mBall.mSpeedY);
                }
                if (mBall.collidesWithPaddle(mPaddleDown)) {
                    mBall.mSpeedY = -Math.abs(mBall.mSpeedY);
                }
                if (mBall.collidesWithPaddle(mPaddleLeft)) {
                    mBall.mSpeedX = Math.abs(mBall.mSpeedX);
                }
                if (mBall.collidesWithPaddle(mPaddleRight)) {
                    mBall.mSpeedX = -Math.abs(mBall.mSpeedX);
                }
                // // Detect wall collisions and react
                // if (mBall.mPositionX + mBall.mRadius > mViewWidthMax) {
                // mBall.mSpeedX = -mBall.mSpeedX;
                // mBall.mPositionX = mViewWidthMax - mBall.mRadius;
                // } else if (mBall.mPositionX - mBall.mRadius < mViewWidthMin)
                // {
                // mBall.mSpeedX = -mBall.mSpeedX;
                // mBall.mPositionX = mViewWidthMin + mBall.mRadius;
                // }
                // if (mBall.mPositionY + mBall.mRadius > mViewHeightMax) {
                // mBall.mSpeedY = -mBall.mSpeedY;
                // mBall.mPositionY = mViewHeightMax - mBall.mRadius;
                // } else if (mBall.mPositionY - mBall.mRadius < mViewHeightMin)
                // {
                // mBall.mSpeedY = -mBall.mSpeedY;
                // mBall.mPositionY = mViewHeightMin + mBall.mRadius;
                // }
                if (mBall.collidesWithWall())
                    serveBall();
            }

            private void serveBall() {
                mBall.mPositionX = mViewWidthMax / 2;
                mBall.mPositionY = mViewHeightMax / 2;
                // mBall.mSpeed += mBallSpeedModifier;
                // mBall.randomAngle();
            }
        }

        /**
         * Ball
         */
        private class Ball {
            private int mRadius = 40; // Ball's radius
            private int mPositionX = mRadius + 20; // Ball's center (x,y)
            private int mPositionY = mRadius + 40;
            private int mSpeedX = 10; // Ball's speed (x,y)
            private int mSpeedY = 7;
            private Paint mPaint = new Paint();

            protected void draw(Canvas canvas) {
                mPaint.setColor(Color.WHITE);
                canvas.drawCircle(mPositionX, mPositionY, mRadius, mPaint);
            }

            private boolean collidesWithWall() {
                if (mPositionX + mRadius > mViewWidthMax)
                    return true;
                if (mPositionX - mRadius < mViewWidthMin)
                    return true;
                if (mPositionY + mRadius > mViewHeightMax)
                    return true;
                if (mPositionY - mRadius < mViewHeightMin)
                    return true;
                return false;
            }

            private boolean collidesWithPaddle(Paddle paddle) {
                int paddleScreenWidth = paddle.getScreenWidth();
                int paddleScreenHeight = paddle.getScreenHeight();
                int distanceX = (int) Math.abs(mPositionX - paddle.mPositionX);
                int distanceY = (int) Math.abs(mPositionY - paddle.mPositionY);

                // Circle completely outside
                if (distanceX > (paddleScreenWidth / 2.0f + mRadius))
                    return false;
                if (distanceY > (paddleScreenHeight / 2.0f + mRadius))
                    return false;

                // Circlecenter inside
                if (distanceX <= (paddleScreenWidth / 2.0f))
                    return true;
                if (distanceY <= (paddleScreenHeight / 2.0f))
                    return true;

                // Corner
                int cornerDistance_square = (distanceX - paddleScreenWidth /
                        2) ^ 2 +
                        (distanceY - paddleScreenHeight / 2) ^ 2;
                return (cornerDistance_square <= (mRadius ^ 2));
            }
        }

        /**
         * Paddle
         */
        private class Paddle {
            private int mWidth = 196;
            private int mHeight = 50;
            private int mPositionX;
            private int mPositionY;
            private Paint mPaint = new Paint();
            private Rect mRect = new Rect();
            private boolean isHorizontal = true;

            public Paddle() {
                mPaint.setColor(Color.WHITE);
                mRect.set(0,
                        0,
                        getScreenWidth(),
                        getScreenHeight());
            }

            private int getScreenWidth() {
                return isHorizontal ? mWidth : mHeight;
            }

            private int getScreenHeight() {
                return isHorizontal ? mHeight : mWidth;
            }

            protected void draw(Canvas canvas) {
                int screenWidth = getScreenWidth();
                int screenHeight = getScreenHeight();
                mRect.set(0,
                        0,
                        screenWidth,
                        screenHeight);
                mRect.offsetTo(mPositionX - screenWidth / 2
                        , mPositionY - screenHeight / 2);
                canvas.drawRect(mRect, mPaint);
            }
        }
    }
}
