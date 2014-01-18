
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

    private class GameTunnelView extends View {
        private static final String TAG = "GameTunnel";
        private static final int FPS = 30;
        private AsyncTask<Void, Void, Void> _logicThread = new LogicThread();

        private int mViewWidthMax;
        private int mViewHeightMax;

        private Bitmap mBitmap;
        private Canvas mBufferCanvas;
        private int mBufferWidth;
        private int mBufferColumns = 10;
        private int mViewPortOffset = 0;
        private int mSpikeWidth = 400;
        private int mSpikeStep = 0;
        private float mTunnelHeight;
        private int mMinTunnelHeight = 200;
        private int mSpikeLeft;
        private int mSpikeRight;

        private int mSpeed = 5;

        private Player mPlayer;
        private int mPadding = 12;
        private LinkedList<Wall> mTunnel = new LinkedList<Wall>();

        private float mRoll;
        private float mInclinationRangeFactor = 2.0f;
        // TODO Calibrate
        // Phone is not attached straight for better visibilty of the screen
        private float mPhoneInclination = 0.1f;

        // Constructor
        public GameTunnelView(Context context) {
            super(context);
            setDrawingCacheEnabled(true);
            mPlayer = new Player();
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
            canvas.drawBitmap(mBitmap, -mViewPortOffset + mBufferWidth, 0, null);
            mPlayer.draw(canvas);
        }

        // Called back when the view is first created or its size changes.
        @Override
        public void onSizeChanged(int w, int h, int oldW, int oldH) {
            // Set the movement bounds for the ball
            mViewWidthMax = w - 1;
            mViewHeightMax = h - 1;

            mTunnelHeight = mViewHeightMax;
            mBufferWidth = (int) (mViewWidthMax + mBufferColumns);
            mSpikeRight = new Random().nextInt(mViewHeightMax);

            mBitmap = Bitmap.createBitmap(mBufferWidth, mViewHeightMax,
                    Bitmap.Config.ARGB_8888);
            mBufferCanvas = new Canvas(mBitmap);
            mBufferCanvas.clipRect(0, 0, mBufferWidth, mViewHeightMax);
            mBufferCanvas.drawColor(Color.BLACK);
            mTunnel.clear();
            for (int i = 0; i < mBufferWidth; i++) {
                if (mSpikeStep == 0) {
                    mSpikeLeft = mSpikeRight;
                    mSpikeRight = new Random().nextInt(mViewHeightMax);
                }
                int pos = interpolate(mSpikeLeft, mSpikeRight, (float) mSpikeStep / mSpikeWidth);
                mSpikeStep = (mSpikeStep + 1) % mSpikeWidth;
                Wall wall;
                if (i < mViewWidthMax / 2) {
                    wall = new Wall(0, mViewHeightMax);
                } else {
                    mTunnelHeight = ((mTunnelHeight - mMinTunnelHeight) * 0.999f)
                            + mMinTunnelHeight;
                    wall = new Wall((int) (pos - mTunnelHeight / 2.0f),
                            (int) (pos + mTunnelHeight / 2.0f));
                }
                mTunnel.add(wall);
                wall.draw(mBufferCanvas, i);
            }

            // Paint p = new Paint(Paint.ANTI_ALIAS_FLAG |
            // Paint.FILTER_BITMAP_FLAG);
            // p.setColor(0xff800000);
            // p.setShader(new LinearGradient(0, 0, w, 0, 0xffffffff,
            // 0xff555555,
            // Shader.TileMode.CLAMP));
            // Path pth = new Path();
            // pth.moveTo(w * 0.15f, 0);
            // pth.lineTo(w * 0.56f, 0);
            // pth.lineTo(w * 0.92f, h);
            // pth.lineTo(w * 0.08f, h);
            // pth.lineTo(w * 0.27f, 0);
            // mBufferCanvas.drawPath(pth, p);
            invalidate();
        }

        private void updatePlayer() {
            mPlayer.mPositionX = mPadding;
            mPlayer.mPositionY = (int) (mRoll * mViewHeightMax);
        }

        private void updateTunnel() {
            if (mSpikeStep == 0) {
                mSpikeLeft = mSpikeRight;
                mSpikeRight = new Random().nextInt(mViewHeightMax);
            }
            int pos = interpolate(mSpikeLeft, mSpikeRight, (float) mSpikeStep / mSpikeWidth);
            mSpikeStep = (mSpikeStep + 1) % mSpikeWidth;
            // int mPositionY;
            // int mUpperSpike;
            // int mGap;
            // int mLowerSpike;
            // float rndNr = new Random().nextFloat() - 2;
            // int mTop;
            // int mBottom;
            mTunnelHeight = ((mTunnelHeight - mMinTunnelHeight) * 0.999f)
                    + mMinTunnelHeight;
            Wall wall = new Wall((int) (pos - mTunnelHeight / 2.0f),
                    (int) (pos + mTunnelHeight / 2.0f));

            mTunnel.removeFirst();
            mTunnel.addLast(wall);
            // draw offscreen
            wall.draw(mBufferCanvas, (mViewPortOffset + mViewWidthMax + (mBufferColumns / 2))
                    % mBufferWidth);
            mViewPortOffset = (mViewPortOffset + 1) % mBufferWidth;
        }

        private int interpolate(int y1, int y2, double mu) {
            double mu2 = (1 - Math.cos(mu * Math.PI)) / 2;
            return (int) (y1 * (1 - mu2) + y2 * mu2);
        }

        private void checkCollisions() {
            synchronized (mTunnel) {
                for (int i = mPlayer.mPositionX; i <= mPlayer.mWidth; i++) {
                    Wall wall = mTunnel.get(i);
                    if (wall.top > mPlayer.mPositionY || wall.bottom < mPlayer.mPositionY) {
                        gameOver();
                        return;
                    }
                }
            }
        }

        private void gameOver() {
            // TODO complete
            Log.d(TAG, "Game Over");
        }

        private class LogicThread extends AsyncTask<Void, Void, Void> {

            @Override
            protected Void doInBackground(Void... params) {
                long timeStart;
                long timeEnd;
                long timeSleep;
                while (!isCancelled()) {
                    timeStart = System.currentTimeMillis();

                    updatePlayer();
                    for (int i = 0; i < mSpeed; i++) {
                        updateTunnel();
                    }
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
         * 
         * @author Mike
         */
        private class Player {
            private int mWidth = 120;
            private int mHeight = 50;
            private int mPositionX = mViewWidthMax / 2;
            private int mPositionY = mViewHeightMax / 2;
            private Paint mPaint = new Paint();
            private Rect mRect = new Rect();

            public Player() {
                mPaint.setColor(Color.RED);
                mRect.set(0,
                        0,
                        mWidth,
                        mHeight);
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
