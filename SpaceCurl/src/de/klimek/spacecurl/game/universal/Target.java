
package de.klimek.spacecurl.game.universal;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;

/**
 * Target
 */
public class Target {
    float mPositionX;
    float mPositionY;
    float mRadius;
    private Paint mPaint;
    long mHoldingTime;
    long mCurHoldingTime;
    boolean mResetIfLeft;
    private RectF mRect;
    private int mMinBorder;
    private int mOnScreenPositionX;
    private int mOnScreenPositionY;
    private int mOnScreenRadius;

    public Target(float positionX, float positionY, float radius, long holdingTime,
            boolean resetIfLeft) {
        mPositionX = positionX;
        mPositionY = positionY;
        mRadius = radius;
        mHoldingTime = holdingTime;
        mCurHoldingTime = mHoldingTime;
        mResetIfLeft = resetIfLeft;
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(Color.RED);
        mRect = new RectF();
    }

    public void draw(Canvas canvas) {
        mMinBorder = canvas.getWidth() <= canvas.getHeight() ? canvas.getWidth() : canvas
                .getHeight();
        mOnScreenPositionX = (int) (mPositionX * mMinBorder - (mMinBorder - canvas.getWidth()) / 2);
        mOnScreenPositionY = (int) (mPositionY * mMinBorder - (mMinBorder - canvas.getHeight()) / 2);
        mOnScreenRadius = (int) (mRadius * mMinBorder);
        mRect = new RectF(mOnScreenPositionX - mOnScreenRadius,
                mOnScreenPositionY - mOnScreenRadius,
                mOnScreenPositionX + mOnScreenRadius,
                mOnScreenPositionY + mOnScreenRadius);
        canvas.drawOval(mRect, mPaint);
    }
}
