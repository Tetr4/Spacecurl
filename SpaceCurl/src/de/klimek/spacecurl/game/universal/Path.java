
package de.klimek.spacecurl.game.universal;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;

/**
 * Path
 */
public class Path extends Drawable {
    private float mPositionX1;
    private float mPositionY1;
    private float mPositionX2;
    private float mPositionY2;
    private float mWidth;
    private float mTrackLength;
    private float mRemainingTrackLength;
    private Paint mPaint;
    private int mMinBorder;

    private android.graphics.Path mPath = new android.graphics.Path();

    public Path(float x1, float y1, float x2, float y2, float width, float trackLength) {
        mPositionX1 = x1;
        mPositionY1 = y1;
        mPositionX2 = x2;
        mPositionY2 = y2;
        mWidth = width;
        mTrackLength = trackLength;
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(Color.GREEN);
        mPaint.setAntiAlias(true);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
    }

    @Override
    public void draw(Canvas canvas) {
        mMinBorder = canvas.getWidth() <= canvas.getHeight() ? canvas.getWidth() : canvas
                .getHeight();
        mPaint.setStrokeWidth(mWidth * mMinBorder);
        mPath.moveTo(mPositionX1 * mMinBorder,
                mPositionY1 * mMinBorder);
        mPath.lineTo(mPositionX2 * mMinBorder,
                mPositionY2 * mMinBorder);
        canvas.drawPath(mPath, mPaint);
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
