
package de.klimek.spacecurl.game.pong;

import java.util.Random;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import de.klimek.spacecurl.game.GameFragment;
import de.klimek.spacecurl.util.collection.Database;

public class Pong extends GameFragment {
    private FrameLayout mPongLayout;
    private TextView mPongScore;
    private LinearLayout mResultLayout;
    private TextView mResultScore;
    private TextView mResultContinueTime;

    private static enum State {
        Normal, Smiling, Frowning,
    }

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
        private static final String TAG = "Pong";
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
        private boolean mHaveSize = false;;

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
                mPaddleTop.mPosition = mRoll;
                mPaddleRight.mPosition = mPitch;
                mPaddleBottom.mPosition = mRoll;
            }

            private void updateBall() {
                if (mBall.mState != State.Frowning) {
                    // Get new (x,y) position
                    mBall.mPositionX += mBall.mSpeedX;
                    mBall.mPositionY += mBall.mSpeedY;
                }
            }

            private void checkCollisions(long deltaTime) {
                // ball has collided with wall
                if (mBall.mState == State.Frowning) {
                    if (mBall.mRemainingFaceTime > 0) {
                        mBall.mRemainingFaceTime -= deltaTime;
                    } else {
                        mBall.mState = State.Normal;
                        mBall.mPositionX = mViewWidthMax / 2;
                        mBall.mPositionY = mViewHeightMax / 2;
                        mBall.mSpeedX = new Random().nextBoolean() ? mBall.mSpeedX : -mBall.mSpeedX;
                        mBall.mSpeedY = new Random().nextBoolean() ? mBall.mSpeedY : -mBall.mSpeedY;
                        // mBall.mSpeed += mBallSpeedModifier;
                        // mBall.randomAngle();
                    }
                }
                // Check paddle collisions and react
                else if (mBall.collidesWithPaddle(mPaddleTop)) {
                    mBall.mSpeedY = Math.abs(mBall.mSpeedY);
                    ++mBallContacts;
                    mBall.mRemainingFaceTime = 500;
                    mBall.mState = State.Smiling;
                } else if (mBall.collidesWithPaddle(mPaddleBottom)) {
                    mBall.mSpeedY = -Math.abs(mBall.mSpeedY);
                    ++mBallContacts;
                    mBall.mRemainingFaceTime = 500;
                    mBall.mState = State.Smiling;
                } else if (mBall.collidesWithPaddle(mPaddleLeft)) {
                    mBall.mSpeedX = Math.abs(mBall.mSpeedX);
                    ++mBallContacts;
                    mBall.mRemainingFaceTime = 500;
                    mBall.mState = State.Smiling;
                } else if (mBall.collidesWithPaddle(mPaddleRight)) {
                    mBall.mSpeedX = -Math.abs(mBall.mSpeedX);
                    ++mBallContacts;
                    mBall.mRemainingFaceTime = 500;
                    mBall.mState = State.Smiling;
                } else if (mBall.collidesWithWall()) {
                    mBallContacts = 0;
                    mBall.mRemainingFaceTime = 500;
                    mBall.mState = State.Frowning;
                } else {
                    mBall.mRemainingFaceTime -= deltaTime;
                    if (mBall.mRemainingFaceTime <= 0) {
                        mBall.mState = State.Normal;
                    }
                }
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
            private int mPositionX = mRadius + 400; // Ball's center (x,y)
            private int mPositionY = mRadius + 400;
            private int mSpeedX = 7; // Ball's speed (x,y)
            private int mSpeedY = 3;
            private Paint mPaint = new Paint();
            private Bitmap mSmileyNormalScaled;
            private Bitmap mSmileyFrowningScaled;
            private Bitmap mSmileySmilingScaled;
            private Rect mSource = new Rect();
            private Rect mDest = new Rect();
            private State mState = State.Normal;
            private long mRemainingFaceTime = 500;

            private Ball() {
                mPaint.setColor(Color.RED);
                mSource.set(0,
                        0,
                        mRadius * 2,
                        mRadius * 2);
                Bitmap smileyNormal = BitmapFactory.decodeResource(getResources(),
                        R.drawable.smiley_normal);
                Bitmap smileyFrowning = BitmapFactory.decodeResource(getResources(),
                        R.drawable.smiley_frowning);
                Bitmap smileySmiling = BitmapFactory.decodeResource(getResources(),
                        R.drawable.smiley_smiling);
                mSmileyNormalScaled = Bitmap.createScaledBitmap(smileyNormal, mRadius * 2,
                        mRadius * 2,
                        true);
                mSmileyFrowningScaled = Bitmap.createScaledBitmap(smileyFrowning, mRadius * 2,
                        mRadius * 2,
                        true);
                mSmileySmilingScaled = Bitmap.createScaledBitmap(smileySmiling, mRadius * 2,
                        mRadius * 2,
                        true);
            }

            protected void draw(Canvas canvas) {
                mDest.set(mPositionX - mRadius,
                        mPositionY - mRadius,
                        mPositionX + mRadius,
                        mPositionY + mRadius);
                switch (mState) {
                    case Normal:
                        canvas.drawBitmap(mSmileyNormalScaled, mSource, mDest, null);
                        return;
                    case Frowning:
                        canvas.drawBitmap(mSmileyFrowningScaled, mSource, mDest, null);
                        return;
                    case Smiling:
                        canvas.drawBitmap(mSmileySmilingScaled, mSource, mDest, null);
                        return;
                }

                // canvas.drawCircle(mPositionX, mPositionY, mRadius, mPaint);
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
            private Rect mSource = new Rect();
            private Rect mDest = new Rect();
            private PaddleSide mSide;
            private int mOnScreenCenterX;
            private int mOnScreenCenterY;
            private int mOnScreenWidth;
            private int mOnScreenHeight;
            private Bitmap mPaddleBitmapScaled;

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
                mSource.set(0,
                        0,
                        mOnScreenWidth,
                        mOnScreenHeight);
                Bitmap paddleBitmap = BitmapFactory.decodeResource(getResources(),
                        R.drawable.cloud);
                mPaddleBitmapScaled = Bitmap.createScaledBitmap(paddleBitmap, mOnScreenWidth,
                        mOnScreenHeight, true);
            }

            protected void draw(Canvas canvas) {
                switch (mSide) {
                    case Left:
                        mOnScreenCenterX = mPadding + mHeight / 2;
                        mOnScreenCenterY = (int) ((mPosition + 1.0f) / 2.0f
                                * (canvas.getHeight() - (mWidth + mPadding * 2)) + mWidth / 2 + mPadding);
                        break;
                    case Top:
                        mOnScreenCenterX = (int) ((mPosition + 1.0f) / 2.0f
                                * (canvas.getWidth() - (mWidth + mPadding * 2)) + mWidth / 2 + mPadding);
                        mOnScreenCenterY = mPadding + mHeight / 2;
                        break;
                    case Right:
                        mOnScreenCenterX = canvas.getWidth() - mPadding - mHeight /
                                2;
                        mOnScreenCenterY = (int) ((mPosition + 1.0f) / 2.0f
                                * (canvas.getHeight() - (mWidth + mPadding * 2)) + mWidth / 2 + mPadding);
                        break;
                    case Bottom:
                        mOnScreenCenterX = (int) ((mPosition + 1.0f) / 2.0f
                                * (canvas.getWidth() - (mWidth + mPadding * 2)) + mWidth / 2 + mPadding);
                        mOnScreenCenterY = canvas.getHeight() - mPadding - mHeight /
                                2;
                        break;
                }

                mDest.set(mOnScreenCenterX - mOnScreenWidth / 2, mOnScreenCenterY
                        - mOnScreenHeight / 2, mOnScreenCenterX + mOnScreenWidth / 2,
                        mOnScreenCenterY + mOnScreenHeight / 2);
                canvas.drawBitmap(mPaddleBitmapScaled, mSource, mDest, null);
                // canvas.drawRect(mRect, mPaint);
            }
        }
    }
}
