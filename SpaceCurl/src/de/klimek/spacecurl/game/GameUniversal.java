
package de.klimek.spacecurl.game;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import de.klimek.spacecurl.R;

public class GameUniversal extends GameFragment {
    public static final int DEFAULT_TITLE_RESOURCE_ID = R.string.game_universal;
    public static final String ARG_TARGET_POSITION = "ARG_TARGET_POSITION";
    // public static final String ARG_HOLDING_TIME
    // public static final String ARG_ZEITVORGABE
    // public static final String ARG_ABWEICHUNG
    // public static final String ARG_TOLERANCE

    // private int score;
    private GameUniversalView mGame;
    private Effect[] mEffects;
    private FreeAxisCount mFreeAxisCount;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mGame = new GameUniversalView(getActivity());
        return mGame;
    }

    @Override
    public void pauseGame() {
        // TODO Auto-generated method stub
    }

    @Override
    public void resumeGame() {
        // TODO Auto-generated method stub

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
        private int mViewWidthMax;
        private int mViewHeightMax;
        private int mCenterX;
        private int mCenterY;
        private int mMinBorder;

        public GameUniversalView(Context context) {
            super(context);

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
        }

        @Override
        protected void onDraw(Canvas canvas) {
            int circleCount = 6;
            int radiusIncrement = mMinBorder / (circleCount * 2);
            for (int i = 1; i < circleCount; i++) {
                int radius = radiusIncrement * i;
                drawCenteredCircle(canvas, radius, Color.MAGENTA);
                radius += radiusIncrement;
            }
            drawCross(canvas, mCenterX, mCenterY, mMinBorder / 10,
                    Color.GREEN);
            drawTarget(canvas, mCenterX - mMinBorder / 6, mCenterY + mMinBorder / 4,
                    mMinBorder / 12, Color.RED);
        }

        private void drawTarget(Canvas canvas, int positionX, int positionY, int size, int color) {
            RectF r = new RectF(positionX - size / 2,
                    positionY - size / 2,
                    positionX + size / 2,
                    positionY + size / 2);
            Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
            paint.setColor(color);
            paint.setStyle(Paint.Style.FILL);
            canvas.drawOval(r, paint);

        }

        private void drawCross(Canvas canvas, int positionX, int positionY, int axisLength,
                int color) {
            Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
            paint.setColor(color);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(4);
            canvas.drawLine(positionX,
                    positionY - axisLength / 2,
                    positionX,
                    positionY + axisLength / 2,
                    paint);
            canvas.drawLine(positionX - axisLength / 2,
                    positionY,
                    positionX + axisLength / 2,
                    positionY,
                    paint);

        }

        private void drawCenteredCircle(Canvas canvas, int radius, int color) {
            Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
            paint.setColor(color);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(4);
            RectF oval = new RectF(
                    mCenterX - radius,
                    mCenterY - radius,
                    mCenterX + radius,
                    mCenterY + radius);
            canvas.drawArc(oval, 0, 360, false, paint);
        }

    }

}
