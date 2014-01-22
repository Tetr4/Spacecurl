
package de.klimek.spacecurl.game;

import java.util.Random;

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
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import de.klimek.spacecurl.R;
import de.klimek.spacecurl.util.collection.Database;

public class GamePong extends GameFragment {
    public static final int DEFAULT_TITLE_RESOURCE_ID = R.string.game_pong;
    private FrameLayout mPongLayout;
    private TextView mPongScore;
    private LinearLayout mResultLayout;
    private TextView mResultScore;
    private TextView mResultContinueTime;

    private static enum PaddleSide {
        Left, Top, Right, Bottom
    }

    private GamePongView mGame;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // ViewSwitcher
        View rootView = inflater.inflate(R.layout.game_pong, container, false);
        mPongLayout = (FrameLayout) rootView.findViewById(R.id.game_pong_layout);
        mPongScore = (TextView) mPongLayout.findViewById(R.id.game_pong_score);
        mResultLayout = (LinearLayout)
                rootView.findViewById(R.id.game_result_layout);
        mResultScore = (TextView)
                rootView.findViewById(R.id.game_result_score);
        mResultContinueTime = (TextView)
                rootView.findViewById(R.id.game_result_continue_time);
        mGame = new GamePongView(getActivity());
        mPongLayout.addView(mGame);
        return rootView;
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

    public class GamePongView extends View {
        private static final String TAG = "GamePong";
        private static final int FPS = 30;
        private AsyncTask<Void, Void, Void> _logicThread = new LogicThread();

        private int mViewWidthMin = 0; // This view's bounds
        private int mViewWidthMax;
        private int mViewHeightMin = 0;
        private int mViewHeightMax;

        private Ball mBall;
        private Paddle mPaddleTop;
        private Paddle mPaddleBottom;
        private Paddle mPaddleLeft;
        private Paddle mPaddleRight;

        private float mPitch;
        private float mRoll;
        private float mInclinationRangeFactor = 2.0f;
        // TODO Calibrate
        // Phone is not attached straight for better visibilty of the screen
        private float mPhoneInclination;

        private int mBallContacts;

        // Constructor
        public GamePongView(Context context) {
            super(context);
            mBall = new Ball();
            mPaddleLeft = new Paddle(PaddleSide.Left);
            mPaddleTop = new Paddle(PaddleSide.Top);
            mPaddleRight = new Paddle(PaddleSide.Right);
            mPaddleBottom = new Paddle(PaddleSide.Bottom);
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
            if (!hasOrientation())
                return;
            mPaddleTop.draw(canvas);
            mPaddleBottom.draw(canvas);
            mPaddleLeft.draw(canvas);
            mPaddleRight.draw(canvas);
            mBall.draw(canvas);
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

            private void updatePaddles() {
                // range 0.0 - 1.0
                mPitch = ((getOrientation()[1] / (float) Math.PI) * mInclinationRangeFactor)
                        + .5f;
                mRoll = ((getOrientation()[2] / (float) Math.PI) * mInclinationRangeFactor)
                        + mPhoneInclination;
                mPaddleLeft.mPosition = mRoll;
                mPaddleTop.mPosition = mPitch;
                mPaddleRight.mPosition = mRoll;
                mPaddleBottom.mPosition = mPitch;
            }

            private void updateBall() {
                // Get new (x,y) position
                mBall.mPositionX += mBall.mSpeedX;
                mBall.mPositionY += mBall.mSpeedY;
            }

            private void checkCollisions() {
                // Check paddle collisions and react
                if (mBall.collidesWithPaddle(mPaddleTop)) {
                    mBall.mSpeedY = Math.abs(mBall.mSpeedY);
                    ++mBallContacts;
                }
                if (mBall.collidesWithPaddle(mPaddleBottom)) {
                    mBall.mSpeedY = -Math.abs(mBall.mSpeedY);
                    ++mBallContacts;
                }
                if (mBall.collidesWithPaddle(mPaddleLeft)) {
                    mBall.mSpeedX = Math.abs(mBall.mSpeedX);
                    ++mBallContacts;
                }
                if (mBall.collidesWithPaddle(mPaddleRight)) {
                    mBall.mSpeedX = -Math.abs(mBall.mSpeedX);
                    ++mBallContacts;
                }
                if (mBall.collidesWithWall()) {
                    serveBall();
                    mBallContacts = 0;
                }
            }

            private void serveBall() {
                mBall.mPositionX = mViewWidthMax / 2;
                mBall.mPositionY = mViewHeightMax / 2;
                mBall.mSpeedX = new Random().nextBoolean() ? mBall.mSpeedX : -mBall.mSpeedX;
                mBall.mSpeedY = new Random().nextBoolean() ? mBall.mSpeedY : -mBall.mSpeedY;
                // mBall.mSpeed += mBallSpeedModifier;
                // mBall.randomAngle();
            }

            @Override
            protected void onProgressUpdate(Void... values) {
                mPongScore.setText(Integer.toString(mBallContacts));
                invalidate();
            }

            @Override
            protected void onCancelled(Void result) {
                Log.v(TAG, "Thread: Cancelled");
                super.onCancelled(result);
            }
        }

        /**
         * Ball
         */
        private class Ball {
            private int mRadius = 40; // Ball's radius
            private int mPositionX = mRadius + 20; // Ball's center (x,y)
            private int mPositionY = mRadius + 40;
            private int mSpeedX = 7; // Ball's speed (x,y)
            private int mSpeedY = 3;
            private Paint mPaint = new Paint();

            protected void draw(Canvas canvas) {
                mPaint.setColor(Color.RED);
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
                switch (paddle.mSide) {
                    case Left:
                        if (mSpeedX > 0)
                            return false;
                        break;
                    case Top:
                        if (mSpeedY > 0)
                            return false;
                        break;
                    case Right:
                        if (mSpeedX < 0)
                            return false;
                        break;
                    case Bottom:
                        if (mSpeedY < 0)
                            return false;
                        break;
                }
                int distanceX = (int) Math.abs(mPositionX - paddle.mOnScreenCenterX);
                int distanceY = (int) Math.abs(mPositionY - paddle.mOnScreenCenterY);

                // Circle completely outside
                if (distanceX > (paddle.mOnScreenWidth / 2.0f + mRadius))
                    return false;
                if (distanceY > (paddle.mOnScreenHeight / 2.0f + mRadius))
                    return false;

                // Circlecenter inside
                if (distanceX <= (paddle.mOnScreenWidth / 2.0f))
                    return true;
                if (distanceY <= (paddle.mOnScreenHeight / 2.0f))
                    return true;

                // Corner
                int cornerDistance_square = (distanceX - paddle.mOnScreenWidth /
                        2) ^ 2 +
                        (distanceY - paddle.mOnScreenHeight / 2) ^ 2;
                return (cornerDistance_square <= (mRadius ^ 2));
            }
        }

        /**
         * Paddle
         */
        private class Paddle {
            private int mWidth = 196;
            private int mHeight = 50;
            private int mPadding = 12;
            private volatile float mPosition;
            private Paint mPaint = new Paint();
            private Rect mRect = new Rect();
            private PaddleSide mSide;
            private int mOnScreenCenterX;
            private int mOnScreenCenterY;
            private int mOnScreenWidth;
            private int mOnScreenHeight;

            public Paddle(PaddleSide side) {
                mSide = side;
                mPaint.setColor(Color.WHITE);
                // mPaint.setShadowLayer(10.0f, 0.0f, 2.0f, 0xFF000000);
                // setLayerType(LAYER_TYPE_SOFTWARE, mPaint);
                if (side == PaddleSide.Left || side == PaddleSide.Right) {
                    mOnScreenWidth = mHeight;
                    mOnScreenHeight = mWidth;
                } else {
                    mOnScreenWidth = mWidth;
                    mOnScreenHeight = mHeight;
                }
                mRect.set(-mOnScreenWidth / 2,
                        -mOnScreenHeight / 2,
                        mOnScreenWidth / 2,
                        mOnScreenHeight / 2);
            }

            protected void draw(Canvas canvas) {
                switch (mSide) {
                    case Left:
                        mOnScreenCenterX = mPadding + mHeight / 2;
                        mOnScreenCenterY = (int) (mPosition * canvas.getHeight());
                        break;
                    case Top:
                        mOnScreenCenterX = (int) (mPosition * canvas.getWidth());
                        mOnScreenCenterY = mPadding + mHeight / 2;
                        break;
                    case Right:
                        mOnScreenCenterX = canvas.getWidth() - mPadding - mHeight / 2;
                        mOnScreenCenterY = (int) (mPosition * canvas.getHeight());
                        break;
                    case Bottom:
                        mOnScreenCenterX = (int) (mPosition * canvas.getWidth());
                        mOnScreenCenterY = canvas.getHeight() - mPadding - mHeight / 2;
                        break;
                }
                mRect.offsetTo(mOnScreenCenterX - mOnScreenWidth / 2, mOnScreenCenterY
                        - mOnScreenHeight / 2);
                canvas.drawRect(mRect, mPaint);
            }
        }
    }
}
