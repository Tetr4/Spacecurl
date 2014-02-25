
package de.klimek.spacecurl.game.tunnel;

import java.util.LinkedList;
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
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import de.klimek.spacecurl.R;
import de.klimek.spacecurl.game.GameFragment;

public class Tunnel extends GameFragment {
    private GameTunnelView mGame;
    private FrameLayout mTunnelLayout;
    private LinearLayout mResultLayout;
    private TextView mResultScore;
    private TextView mResultContinueTime;

    private static enum Stage {
        SizeNotSet, Running, GameOver, Result
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.game_tunnel, container, false);
        mTunnelLayout = (FrameLayout) rootView.findViewById(R.id.game_tunnel_layout);
        mResultLayout = (LinearLayout) rootView.findViewById(R.id.game_result_layout);
        mResultScore = (TextView)
                rootView.findViewById(R.id.game_result_score);
        mResultContinueTime = (TextView)
                rootView.findViewById(R.id.game_result_continue_time);
        mGame = new GameTunnelView(getActivity());
        mTunnelLayout.addView(mGame);
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

    public class GameTunnelView extends View {
        private static final String TAG = "Tunnel";
        private static final int FPS = 30;
        private AsyncTask<Void, Void, Void> _logicThread = new LogicThread();

        // Game difficulty parameters
        private int mCurveWidth = 300;
        private int mSpeed = 3;
        private int mMinTunnelHeight = 190;
        private int mPadding = 12;
        private Player mPlayer = new Player(96, 59);;

        // Drawing variables
        private int mViewWidthMax;
        private int mViewHeightMax;
        private Bitmap mBitmap;
        private int mBitmapWidth;
        private int mBufferColumns = 10;
        private int mViewPortOffset = 0;
        private Canvas mBufferCanvas;

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
        private int mRemainingTimeUntilContinue = 8000;
        private Stage mStage = Stage.SizeNotSet;

        // Constructor
        public GameTunnelView(Context context) {
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

        // Called back to draw the view. Also called by invalidate().
        @Override
        protected void onDraw(Canvas canvas) {
            canvas.drawBitmap(mBitmap, -mViewPortOffset, 0, null);
            canvas.drawBitmap(mBitmap, -mViewPortOffset + mBitmapWidth, 0, null);
            if (hasOrientation())
                mPlayer.draw(canvas);
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
                            break;

                        case GameOver:
                            // Do nothing
                            break;

                        case Result:
                            mRemainingTimeUntilContinue -= _deltaTime;
                            if (mRemainingTimeUntilContinue < 1000) {
                                reset();
                                mStage = Stage.Running;
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

            private void updatePlayer() {
                mPitch = getScaledOrientation()[1];
                mPlayer.mPositionX = mPadding + mPlayer.mWidth / 2;
                mPlayer.mPositionY = (int) ((mPitch + 1.0f) / 2.0f
                        * (mViewHeightMax - (mPlayer.mWidth + mPadding * 2)) + mPlayer.mWidth / 2 + mPadding);
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
                for (int i = mPlayer.mPositionX - mPlayer.mWidth / 2; i <= mPlayer.mPositionX
                        + mPlayer.mWidth / 2; i++) {
                    Wall wall = mTunnel.get(i);
                    if (wall.top > mPlayer.mPositionY - mPlayer.mHeight / 2
                            || wall.bottom < mPlayer.mPositionY + mPlayer.mHeight / 2) {
                        mStage = Stage.GameOver;
                        return;
                    }
                }
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
                        mResultLayout.startAnimation(AnimationUtils.loadAnimation(getActivity(),
                                R.anim.result_anim));
                        mResultLayout.setVisibility(View.VISIBLE);
                        mStage = Stage.Result;
                        break;
                    case Result:
                        mResultContinueTime
                                .setText(timeToString(mRemainingTimeUntilContinue, false));
                        mResultScore.setText(String.format("%.1f m", mDistance));
                        break;
                    case Running:
                        mResultLayout.setVisibility(View.INVISIBLE);
                        break;
                    case SizeNotSet:
                        break;
                    default:
                        break;
                }

                invalidate();
            }

            @Override
            protected void onCancelled(Void result) {
                Log.v(TAG, "Thread: Cancelled");
                super.onCancelled(result);
            }
        }

        /**
         * Wall
         */
        private class Wall {
            private final int top;
            private final int bottom;

            private Wall(int top, int bottom) {
                this.top = top;
                this.bottom = bottom;
            }

            protected void draw(Canvas canvas, int positionX) {
                Paint paintTunnel = new Paint();
                paintTunnel.setColor(Color.WHITE);
                Paint paintWall = new Paint();
                paintWall.setColor(Color.BLACK);
                canvas.drawLine(positionX,
                        0,
                        positionX,
                        mViewHeightMax,
                        paintWall);
                canvas.drawLine(positionX,
                        top,
                        positionX,
                        bottom,
                        paintTunnel);
            }
        }

        /**
         * Player
         */
        private class Player {
            private int mWidth;
            private int mHeight;
            private int mPositionX;
            private int mPositionY;
            private Paint mPaint = new Paint();
            private Bitmap mRocket = BitmapFactory.decodeResource(getResources(),
                    R.drawable.rocket);
            private Bitmap mRocketScaled;
            private Rect mSource = new Rect();
            private Rect mDest = new Rect();

            public Player(int width, int height) {
                // mPaint.setColor(Color.RED);
                mPaint.setAlpha(100);
                mWidth = width;
                mHeight = height;
                mPositionX = mPadding + mWidth / 2;
                mPositionY = mViewHeightMax / 2;
                mSource.set(0,
                        0,
                        mWidth,
                        mHeight);
                mRocketScaled = Bitmap.createScaledBitmap(mRocket, mWidth, mHeight, true);
            }

            protected void draw(Canvas canvas) {
                mDest.set(mPositionX - mWidth / 2,
                        mPositionY - mHeight / 2,
                        mPositionX + mWidth / 2,
                        mPositionY + mHeight / 2);
                canvas.drawBitmap(mRocketScaled, mSource, mDest, null);
            }
        }
    }
}
