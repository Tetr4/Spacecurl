
package de.klimek.spacecurl.game;

import java.util.LinkedList;

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

        private int mViewWidthMin = 0; // This view's bounds
        private int mViewWidthMax;
        private int mViewHeightMin = 0;
        private int mViewHeightMax;

        private Bitmap mBitmap;
        private Canvas mBufferCanvas;
        private int mWidthCounter = 0;

        private int mSpeed = 10;

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

            canvas.drawBitmap(mBitmap, mWidthCounter, 0, null);
            canvas.drawBitmap(mBitmap, mWidthCounter + mViewWidthMax, 0, null);

            mPlayer.draw(canvas);
        }

        // Called back when the view is first created or its size changes.
        @Override
        public void onSizeChanged(int w, int h, int oldW, int oldH) {
            // Set the movement bounds for the ball
            mViewWidthMax = w - 1;
            mViewHeightMax = h - 1;

            for (int i = 0; i < mViewWidthMax; i++) {
                Wall wall = new Wall(200 + i, 500 + i);
                mTunnel.add(wall);
            }

            mBitmap = createBitmap();

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

        private Bitmap createBitmap() {
            Bitmap bitmap = Bitmap.createBitmap((int) (mViewWidthMax), mViewHeightMax,
                    Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            canvas.drawColor(Color.BLACK);
            for (int i = 0; i < mViewWidthMax; i++) {
                drawTunnel(canvas, mTunnel.get(i), i);
            }
            return bitmap;
        }

        private void switchBuffer(Bitmap bitmap) {
            mBitmap = bitmap;
        }

        private void drawTunnel(Canvas canvas, Wall wall, int positionX) {
            Paint paintTunnel = new Paint();
            paintTunnel.setColor(Color.WHITE);
            Paint paintWall = new Paint();
            paintWall.setColor(Color.BLACK);
            canvas.drawLine(positionX,
                    mViewHeightMin,
                    positionX,
                    mViewHeightMax,
                    paintWall);
            canvas.drawLine(positionX,
                    wall.top,
                    positionX,
                    wall.bottom,
                    paintTunnel);
        }

        private void updatePlayer() {
            mPlayer.mPositionX = mPadding;
            mPlayer.mPositionY = (int) (mRoll * mViewWidthMax);
        }

        private void updateTunnel() {
            // int mPositionY;
            // int mUpperSpike;
            // int mGap;
            // int mLowerSpike;
            // float rndNr = new Random().nextFloat() - 2;
            // int mTop;
            // int mBottom;
            for (int i = 0; i < mSpeed; i++) {
                Wall wall = new Wall(400, 800);
                mTunnel.removeFirst();
                mTunnel.addLast(wall);
            }
        }

        private double interpolate(double a, double b, double x) {
            double ft = x * 3.1415927;
            double f = (1 - Math.cos(ft)) * 0.5;
            return a * (1 - f) + b * f;
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
                    updateTunnel();
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
                mWidthCounter -= mSpeed;
                if (mWidthCounter <= -mViewWidthMax) {
                    mWidthCounter = 0;
                    // switchBuffer();
                }
                mRoll = ((getOrientation()[2] / (float) Math.PI) * mInclinationRangeFactor)
                        + mPhoneInclination;
            }

            @Override
            protected void onCancelled(Void result) {
                Log.v(TAG, "Thread: Cancelled");
                super.onCancelled(result);
            }
        }

        private class Wall {
            private int top;
            private int bottom;

            private Wall(int top, int bottom) {
                this.top = top;
                this.bottom = bottom;
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
