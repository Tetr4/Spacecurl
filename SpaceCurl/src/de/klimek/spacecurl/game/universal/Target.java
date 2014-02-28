
package de.klimek.spacecurl.game.universal;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;

/**
 * Target
 */
public class Target extends Drawable {
    float mPositionX;
    float mPositionY;
    float mRadius;
    private Paint mPaint;
    long mHoldingTime;
    long mRemainingHoldingTime;
    boolean mResetIfLeft;
    private RectF mRect = new RectF();
    private Drawable mDrawable;
    private int mMinBorder;
    private int mOnScreenPositionX;
    private int mOnScreenPositionY;
    private int mOnScreenRadius;

    public Target(float positionX, float positionY) {
        this(positionX, positionY, 0.07f);
    }

    public Target(float positionX, float positionY, float radius) {
        this(positionX, positionY, radius, 0, false);
    }

    public Target(float positionX, float positionY, long holdingTime) {
        this(positionX, positionY, 0.07f, holdingTime, true);
    }

    public Target(float positionX, float positionY, float radius, long holdingTime) {
        this(positionX, positionY, radius, holdingTime, true);
    }

    public Target(float positionX, float positionY, long holdingTime, boolean resetIfLeft) {
        this(positionX, positionY, 0.07f, holdingTime, resetIfLeft);
    }

    public Target(float positionX, float positionY, float radius, long holdingTime,
            boolean resetIfLeft) {
        mPositionX = positionX;
        mPositionY = positionY;
        mRadius = radius;
        mHoldingTime = holdingTime;
        mRemainingHoldingTime = mHoldingTime;
        mResetIfLeft = resetIfLeft;
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(Color.RED);
    }

    public void setDrawable(Drawable drawable) {
        mDrawable = drawable;
    }

    @Override
    public void draw(Canvas canvas) {
        draw(canvas, false);
    }

    public void draw(Canvas canvas, Boolean translucent) {
        mMinBorder = canvas.getWidth() <= canvas.getHeight() ? canvas.getWidth() : canvas
                .getHeight();
        mOnScreenPositionX = (int) (mPositionX * mMinBorder - (mMinBorder - canvas.getWidth()) / 2);
        mOnScreenPositionY = (int) (mPositionY * mMinBorder - (mMinBorder - canvas.getHeight()) / 2);
        mOnScreenRadius = (int) (mRadius * mMinBorder);
        mRect.set(mOnScreenPositionX - mOnScreenRadius,
                mOnScreenPositionY - mOnScreenRadius,
                mOnScreenPositionX + mOnScreenRadius,
                mOnScreenPositionY + mOnScreenRadius);
        if (translucent) {
            mPaint.setAlpha(96);
            mPaint.setStyle(Paint.Style.STROKE);
            mPaint.setStrokeWidth((float) (mRadius * 0.1 * mMinBorder));
            canvas.drawArc(mRect, 0, 360, false, mPaint);
        } else {
            mPaint.setAlpha(255);
            mPaint.setStyle(Paint.Style.FILL);
            if (mDrawable != null) {
                mDrawable.setBounds((int) mRect.left, (int) mRect.top, (int) mRect.right,
                        (int) mRect.bottom);
                mDrawable.draw(canvas);
            } else {
                canvas.drawOval(mRect, mPaint);
            }
        }
    }

    @Override
    public int getOpacity() {
        // TODO Auto-generated method stub
        return PixelFormat.TRANSPARENT;
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
