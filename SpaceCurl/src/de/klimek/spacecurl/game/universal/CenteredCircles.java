package de.klimek.spacecurl.game.universal;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;

/**
 * CenteredCircles
 */
class CenteredCircles {
    private int mPositionX;
    private int mPositionY;
    private int mCircleCount = 6;
    private int mRadiusIncrement;
    private Paint mPaint;

    CenteredCircles(int circleCount, int radiusIncrement, int positionX,
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

    void draw(Canvas canvas) {
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