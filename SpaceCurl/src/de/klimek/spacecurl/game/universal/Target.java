
package de.klimek.spacecurl.game.universal;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import de.klimek.spacecurl.util.ColorGradient;

/**
 * Target
 */
public class Target extends Drawable {
    float mPositionX;
    float mPositionY;
    float mRadius;
    private Paint mTargetPaint;
    private RectF mTargetRect = new RectF();
    private Paint mTranslucentPaint;
    private RectF mTranslucentTargetRect = new RectF();
    private static final float TRANSLUCENT_RADIUS = 0.03f;
    private Drawable mDrawable;

    long mHoldingTime;
    long mRemainingHoldingTime;
    boolean mResetIfLeft;
    private RectF mCompletionRect = new RectF();
    private static final float COMPLETION_RADIUS_RATIO = 1.1f;
    private float mCompletionFraction;
    private ColorGradient mCompletionGradient = new ColorGradient(Color.RED, Color.YELLOW,
            Color.GREEN);
    private int mCompletionColor;
    private Paint mCompletionPaint;

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
        mTargetPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mTargetPaint.setStyle(Paint.Style.FILL);
        mTargetPaint.setColor(Color.RED);
        mCompletionPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mCompletionPaint.setStyle(Paint.Style.STROKE);
        mCompletionPaint.setStrokeWidth(12);
        mTranslucentPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mTranslucentPaint.setColor(Color.GRAY);
        mTranslucentPaint.setStyle(Paint.Style.FILL);
        mTranslucentPaint.setAlpha(200);
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

        if (translucent) { // next Target
            mTranslucentTargetRect.set(mOnScreenPositionX - TRANSLUCENT_RADIUS * mMinBorder,
                    mOnScreenPositionY - TRANSLUCENT_RADIUS * mMinBorder,
                    mOnScreenPositionX + TRANSLUCENT_RADIUS * mMinBorder,
                    mOnScreenPositionY + TRANSLUCENT_RADIUS * mMinBorder);
            canvas.drawArc(mTranslucentTargetRect, 0, 360, false, mTranslucentPaint);
        } else { // current Target
            // Progress circle for holding time
            if (mHoldingTime > 0) {
                mCompletionRect.set(mOnScreenPositionX - mOnScreenRadius * COMPLETION_RADIUS_RATIO,
                        mOnScreenPositionY - mOnScreenRadius * COMPLETION_RADIUS_RATIO,
                        mOnScreenPositionX + mOnScreenRadius * COMPLETION_RADIUS_RATIO,
                        mOnScreenPositionY + mOnScreenRadius * COMPLETION_RADIUS_RATIO);
                // Grey ring
                if (mRemainingHoldingTime != mHoldingTime) {
                    mCompletionPaint.setColor(Color.GRAY);
                    mCompletionPaint.setAlpha(200);
                    canvas.drawArc(mCompletionRect, 0,
                            360, false,
                            mCompletionPaint);
                }
                // Gradient
                mCompletionFraction = ((float) (mHoldingTime - mRemainingHoldingTime) / mHoldingTime);
                mCompletionColor = mCompletionGradient.getColorForFraction(mCompletionFraction);
                mCompletionPaint.setAlpha(255);
                mCompletionPaint.setColor(mCompletionColor);
                canvas.drawArc(mCompletionRect, 0,
                        mCompletionFraction * 360, false,
                        mCompletionPaint);
            }

            mTargetRect.set(mOnScreenPositionX - mOnScreenRadius,
                    mOnScreenPositionY - mOnScreenRadius,
                    mOnScreenPositionX + mOnScreenRadius,
                    mOnScreenPositionY + mOnScreenRadius);

            // drawable or just a circle
            if (mDrawable != null) {
                mDrawable.setBounds((int) mTargetRect.left, (int) mTargetRect.top,
                        (int) mTargetRect.right,
                        (int) mTargetRect.bottom);
                mDrawable.draw(canvas);
            } else {
                mTargetPaint.setAlpha(255);
                mTargetPaint.setStyle(Paint.Style.FILL);
                canvas.drawOval(mTargetRect, mTargetPaint);
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
