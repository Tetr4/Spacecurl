
package de.klimek.spacecurl.game.universal;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;

/**
 * CenteredCircles
 */
public class CenteredCircles extends Drawable {
    private int mCircleCount = 6;
    private Paint mPaint;
    private int mMinBorder;
    private int mOnScreenPositionX;
    private int mOnScreenPositionY;
    private int mCurRadius;
    private int mRadiusIncrement;
    private RectF mRect = new RectF();

    public CenteredCircles(int circleCount) {
        mCircleCount = circleCount;
        // mMinBorder / (mCircleCount * 2)
        mPaint = new Paint();
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(4);
        mPaint.setColor(Color.DKGRAY);
    }

    @Override
    public void draw(Canvas canvas) {
        mMinBorder = canvas.getWidth() <= canvas.getHeight() ? canvas.getWidth() : canvas
                .getHeight();
        mOnScreenPositionX = canvas.getWidth() / 2;
        mOnScreenPositionY = canvas.getHeight() / 2;
        canvas.drawLine(mOnScreenPositionX, mOnScreenPositionY + mMinBorder * 0.45f,
                mOnScreenPositionX,
                mOnScreenPositionY - mMinBorder * 0.45f, mPaint);
        canvas.drawLine(mOnScreenPositionX + mMinBorder * 0.45f, mOnScreenPositionY,
                mOnScreenPositionX - mMinBorder * 0.45f,
                mOnScreenPositionY, mPaint);
        // mRadiusIncrement = mMinBorder / (mCircleCount * 2);
        // for (int i = 1; i < mCircleCount; i++) {
        // mCurRadius = mRadiusIncrement * i;
        // mRect.set(mOnScreenPositionX - mCurRadius,
        // mOnScreenPositionY - mCurRadius,
        // mOnScreenPositionX + mCurRadius,
        // mOnScreenPositionY + mCurRadius);
        // canvas.drawArc(mRect, 0, 360, false, mPaint);
        // mCurRadius += mRadiusIncrement;
        // }

    }

    @Override
    public int getOpacity() {
        // TODO Auto-generated method stub
        return PixelFormat.OPAQUE;
    }

    @Override
    public void setAlpha(int alpha) {
        // TODO Auto-generated method stub

    }

    @Override
    public void setColorFilter(ColorFilter cf) {
        // TODO Auto-generated method stub

    }
}
