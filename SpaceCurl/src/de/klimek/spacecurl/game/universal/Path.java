
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
    private Paint mPaint;
    private int mTrackLength;
    private int mRemainingTrackLength;
    private int mMinBorder;
    private Target[] mTargets;
    private android.graphics.Path mPath = new android.graphics.Path();

    public Path(Target[] targets, int trackLength) {
        mTargets = targets;
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
        mPath.moveTo(mTargets[0].mPositionX * mMinBorder,
                mTargets[0].mPositionY * mMinBorder);
        for (int i = 1; i < mTargets.length; i++) {
            mPath.lineTo(mTargets[0].mPositionX * mMinBorder,
                    mTargets[0].mPositionY * mMinBorder);
        }
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
