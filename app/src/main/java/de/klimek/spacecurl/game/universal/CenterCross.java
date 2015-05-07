
package de.klimek.spacecurl.game.universal;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;

/**
 * Cross dividing the screen into Quadrants to help the user with
 * movements/navigation.
 */
public class CenterCross extends Drawable {
    private Paint mPaint;
    private int mMinBorder;
    private int mOnScreenPositionX;
    private int mOnScreenPositionY;

    public CenterCross() {
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
    }

    @Override
    public int getOpacity() {
        return PixelFormat.OPAQUE;
    }

    @Override
    public void setAlpha(int alpha) {

    }

    @Override
    public void setColorFilter(ColorFilter cf) {

    }
}
