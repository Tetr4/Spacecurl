package de.klimek.spacecurl.game.universal;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

/**
 * Player
 */
class Player {
    private float mAxisLength;
    float mPositionX;
    float mPositionY;
    private Paint mPaint;
    private int mMinBorder;
    private int mOnScreenPositionX;
    private int mOnScreenPositionY;
    private int mOnScreenAxisLength;

    Player(float axisLength) {
        mAxisLength = axisLength;
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(4);
        mPaint.setColor(Color.GREEN);
    }

    public boolean intersects(Target mTarget) {
        return (Math.pow((mPositionX - mTarget.mPositionX), 2)
                + Math.pow((mPositionY - mTarget.mPositionY), 2)
                < Math.pow(mTarget.mRadius, 2));
    }

    float distanceTo(Target mTarget) {
        if (intersects(mTarget)) {
            return 0.0f;
        } else {
            return (float) (Math.pow((mPositionX - mTarget.mPositionX), 2)
                    + Math.pow((mPositionY - mTarget.mPositionY), 2)
                    - Math.pow(mTarget.mRadius, 2));
        }
    }

    void draw(Canvas canvas) {
        mMinBorder = canvas.getWidth() <= canvas.getHeight() ? canvas.getWidth() : canvas
                .getHeight();
        mOnScreenPositionX = (int) (mPositionX * mMinBorder - (mMinBorder - canvas.getWidth()) / 2);
        mOnScreenPositionY = (int) (mPositionY * mMinBorder - (mMinBorder - canvas.getHeight()) / 2);
        mOnScreenAxisLength = (int) (mAxisLength * mMinBorder);
        canvas.drawLine(mOnScreenPositionX,
                mOnScreenPositionY - mOnScreenAxisLength / 2,
                mOnScreenPositionX,
                mOnScreenPositionY + mOnScreenAxisLength / 2,
                mPaint);
        canvas.drawLine(mOnScreenPositionX - mOnScreenAxisLength / 2,
                mOnScreenPositionY,
                mOnScreenPositionX + mOnScreenAxisLength / 2,
                mOnScreenPositionY,
                mPaint);
    }
}