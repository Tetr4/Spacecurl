
package de.klimek.spacecurl.game;

import java.util.LinkedList;
import java.util.Random;

import android.content.Context;
import android.graphics.Bitmap;
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

public class GameTunnel extends GameFragment {
    public static final int DEFAULT_TITLE_RESOURCE_ID = R.string.game_pong;

    private GameTunnelView mGame;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mGame = new GameTunnelView(getActivity());
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

    private static float interpolate(int y1, int y2, float mu) {
        float mu2 = (float) ((1 - Math.cos(mu * Math.PI)) / 2.0f);
        return (y1 * (1 - mu2) + y2 * mu2);
    }

    private class GameTunnelView extends View {
        private static final String TAG = "GameTunnel";
        private static final int FPS = 30;
        private AsyncTask<Void, Void, Void> _logicThread = new LogicThread();

        // Game Parameters
        private int mCurveWidth = 200;
        private int mSpeed = 3;
        private int mMinTunnelHeight = 160;
        private int mPadding = 12;
        private Player mPlayer = new Player(60, 60);;

        private volatile boolean mSizeSet = false;
        private int mViewWidthMax;
        private int mViewHeightMax;
        private Bitmap mBitmap;
        private int mBitmapWidth;
        private int mBufferColumns = 10;
        private int mViewPortOffset = 0;
        private Canvas mBufferCanvas;

        private LinkedList<Wall> mTunnel = new LinkedList<Wall>();
        private float mTunnelHeight;
        private int mCurvePrevious;
        private int mCurveNext;
        private int mCurveStep = 0;

        private float mRoll;
        private float mInclinationRangeFactor = 2.0f;
        // TODO Calibrate
        // Phone is not attached straight for better visibilty of the screen
        private float mPhoneInclination;

        // Constructor
        public GameTunnelView(Context context) {
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

        // Called back to draw the view. Also called by invalidate().
        @Override
        protected void onDraw(Canvas canvas) {
            canvas.drawBitmap(mBitmap, -mViewPortOffset, 0, null);
            canvas.drawBitmap(mBitmap, -mViewPortOffset + mBitmapWidth, 0, null);
            mPlayer.draw(canvas);
        }

        // Called back when the view is first created or its size changes.
        @Override
        public void onSizeChanged(int w, int h, int oldW, int oldH) {
            mViewWidthMax = w - 1;
            mViewHeightMax = h - 1;
            mTunnelHeight = mViewHeightMax * 1.5f;
            mCurvePrevious = mViewHeightMax / 2;
            mCurveNext = mCurvePrevious;

            mBitmapWidth = (int) (mViewWidthMax + mBufferColumns);
            mBitmap = Bitmap.createBitmap(mBitmapWidth, mViewHeightMax,
                    Bitmap.Config.ARGB_8888);
            mBufferCanvas = new Canvas(mBitmap);
            mBufferCanvas.clipRect(0, 0, mBitmapWidth, mViewHeightMax);
            mBufferCanvas.drawColor(Color.BLACK);

            mTunnel.clear();
            int tunnelPosY;
            for (int i = 0; i < mBitmapWidth; i++) {
                // update tunnel position
                tunnelPosY = (int) interpolate(mCurvePrevious, mCurveNext, (float) mCurveStep
                        / mCurveWidth);

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
                Wall wall = new Wall((int) (tunnelPosY - c / 2.0f),
                        (int) (tunnelPosY + c / 2.0f));
                mTunnel.add(wall);

                // draw
                wall.draw(mBufferCanvas, i);
            }
            mSizeSet = true;

            invalidate();
        }

        private class LogicThread extends AsyncTask<Void, Void, Void> {

            @Override
            protected Void doInBackground(Void... params) {
                long timeStart;
                long timeEnd;
                long timeSleep;
                while (!isCancelled()) {
                    timeStart = System.currentTimeMillis();
                    if (mSizeSet) {
                        updatePlayer();
                        for (int i = 0; i < mSpeed; i++) {
                            updateTunnel();
                        }
                        checkCollisions();
                        publishProgress();
                    }
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
                mRoll = ((getOrientation()[2] / (float) Math.PI) * mInclinationRangeFactor)
                        + mPhoneInclination;
                invalidate();
            }

            @Override
            protected void onCancelled(Void result) {
                Log.v(TAG, "Thread: Cancelled");
                super.onCancelled(result);
            }

            private void updatePlayer() {
                mPlayer.mPositionX = mPadding + mPlayer.mWidth / 2;
                mPlayer.mPositionY = (int) (mRoll * mViewHeightMax);
            }

            private void updateTunnel() {
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
                Wall wall = new Wall((int) (tunnelPosY - c / 2.0f),
                        (int) (tunnelPosY + c / 2.0f));
                mTunnel.removeFirst();
                mTunnel.addLast(wall);
                // draw offscreen
                wall.draw(mBufferCanvas, (mViewPortOffset + mViewWidthMax + (mBufferColumns / 2))
                        % mBitmapWidth);
                mViewPortOffset = (mViewPortOffset + 1) % mBitmapWidth;
            }

            private void checkCollisions() {
                for (int i = mPlayer.mPositionX; i <= mPlayer.mWidth; i++) {
                    Wall wall = mTunnel.get(i);
                    if (wall.top > mPlayer.mPositionY || wall.bottom < mPlayer.mPositionY) {
                        gameOver();
                        return;
                    }
                }
            }

            private void gameOver() {
                // TODO complete
                Log.d(TAG, "Game Over");
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
            private Rect mRect = new Rect();

            public Player(int width, int height) {
                mPaint.setColor(Color.RED);
                mWidth = width;
                mHeight = height;
                mPositionX = mPadding + mWidth / 2;
                mPositionY = mViewHeightMax / 2;
            }

            protected void draw(Canvas canvas) {
                mRect.set(0,
                        0,
                        mWidth,
                        mHeight);
                mRect.offsetTo(mPositionX - mWidth / 2
                        , mPositionY - mHeight / 2);
                canvas.drawRect(mRect, mPaint);
            }
        }
    }
}
