
package de.klimek.spacecurl.game.universal;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;

/**
 * Player
 */
public class Player extends Drawable {
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

    public boolean intersects(Path path) {
        return false; // FIXME
    }

    public float distanceTo(Target target) {
        if (intersects(target)) {
            return 0.0f;
        } else {
            return (float) Math.sqrt(
                    Math.pow((mPositionX - target.mPositionX), 2)
                            + Math.pow((mPositionY - target.mPositionY), 2)
                    ) - target.mRadius;
        }
    }

    public float distanceTo(Path path) {
        if (intersects(path)) {
            return 0.0f;
        } else {
            return 2.0f; // FIXME
        }
    }

    @Override
    public void draw(Canvas canvas) {
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
