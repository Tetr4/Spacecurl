
package de.klimek.spacecurl.game.pong;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import de.klimek.spacecurl.R;

/**
 * Ball for Pong
 */
public class Ball extends Drawable {
    private int mRadius = 40; // Ball's radius
    private int mPositionX = mRadius + 400; // Ball's center (x,y)
    private int mPositionY = mRadius + 400;
    private int mSpeedX = 4; // Ball's speed (x,y)
    private int mSpeedY = 2;
    private Paint mPaint = new Paint();
    private Bitmap mSmileyNormalScaled;
    private Bitmap mSmileyFrowningScaled;
    private Bitmap mSmileySmilingScaled;
    private Rect mSource = new Rect();
    private Rect mDest = new Rect();
    private FaceState mState = FaceState.Normal;
    private long mRemainingFaceTime = 500;

    public static enum FaceState {
        Normal, Smiling, Frowning,
    }

    public Ball(Resources res) {
        mPaint.setColor(Color.RED);
        mSource.set(0,
                0,
                mRadius * 2,
                mRadius * 2);
        Bitmap smileyNormal = BitmapFactory.decodeResource(res,
                R.drawable.smiley_normal);
        Bitmap smileyFrowning = BitmapFactory.decodeResource(res,
                R.drawable.smiley_frowning);
        Bitmap smileySmiling = BitmapFactory.decodeResource(res,
                R.drawable.smiley_smiling);
        mSmileyNormalScaled = Bitmap.createScaledBitmap(smileyNormal, mRadius * 2,
                mRadius * 2,
                true);
        mSmileyFrowningScaled = Bitmap.createScaledBitmap(smileyFrowning, mRadius * 2,
                mRadius * 2,
                true);
        mSmileySmilingScaled = Bitmap.createScaledBitmap(smileySmiling, mRadius * 2,
                mRadius * 2,
                true);
    }

    @Override
    public void draw(Canvas canvas) {
        mDest.set(mPositionX - mRadius,
                mPositionY - mRadius,
                mPositionX + mRadius,
                mPositionY + mRadius);
        switch (mState) {
            case Normal:
                canvas.drawBitmap(mSmileyNormalScaled, mSource, mDest, null);
                return;
            case Frowning:
                canvas.drawBitmap(mSmileyFrowningScaled, mSource, mDest, null);
                return;
            case Smiling:
                canvas.drawBitmap(mSmileySmilingScaled, mSource, mDest, null);
                return;
        }

        // canvas.drawCircle(mPositionX, mPositionY, mRadius, mPaint);
    }

    public boolean collidesWithWall(int left, int top, int right, int bottom) {
        if (mPositionX + mRadius > right)
            return true;
        if (mPositionX - mRadius < left)
            return true;
        if (mPositionY + mRadius > bottom)
            return true;
        if (mPositionY - mRadius < top)
            return true;
        return false;
    }

    public boolean collidesWithPaddle(Paddle paddle) {
        switch (paddle.mSide) {
            case Left:
                if (mSpeedX > 0)
                    return false;
                break;
            case Top:
                if (mSpeedY > 0)
                    return false;
                break;
            case Right:
                if (mSpeedX < 0)
                    return false;
                break;
            case Bottom:
                if (mSpeedY < 0)
                    return false;
                break;
        }
        int distanceX = (int) Math.abs(mPositionX - paddle.mOnScreenCenterX);
        int distanceY = (int) Math.abs(mPositionY - paddle.mOnScreenCenterY);

        // Circle completely outside
        if (distanceX > (paddle.mOnScreenWidth / 2.0f + mRadius))
            return false;
        if (distanceY > (paddle.mOnScreenHeight / 2.0f + mRadius))
            return false;

        // Circlecenter inside
        if (distanceX <= (paddle.mOnScreenWidth / 2.0f))
            return true;
        if (distanceY <= (paddle.mOnScreenHeight / 2.0f))
            return true;

        // Corner
        int cornerDistance_square = (distanceX - paddle.mOnScreenWidth /
                2) ^ 2 +
                (distanceY - paddle.mOnScreenHeight / 2) ^ 2;
        return (cornerDistance_square <= (mRadius ^ 2));
    }

    @Override
    public int getOpacity() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public void setAlpha(int alpha) {
        // TODO Auto-generated method stub

    }

    @Override
    public void setColorFilter(ColorFilter cf) {
        // TODO Auto-generated method stub

    }

    public FaceState getFaceState() {
        return mState;
    }

    public void setFaceState(FaceState state) {
        mState = state;
    }

    public int getPositionX() {
        return mPositionX;
    }

    public void setPositionX(int mPositionX) {
        this.mPositionX = mPositionX;
    }

    public int getSpeedX() {
        return mSpeedX;
    }

    public void setSpeedX(int mSpeedX) {
        this.mSpeedX = mSpeedX;
    }

    public int getPositionY() {
        return mPositionY;
    }

    public void setPositionY(int mPositionY) {
        this.mPositionY = mPositionY;
    }

    public int getSpeedY() {
        return mSpeedY;
    }

    public void setSpeedY(int mSpeedY) {
        this.mSpeedY = mSpeedY;
    }

    public long getRemainingFaceTime() {
        return mRemainingFaceTime;
    }

    public void setRemainingFaceTime(long mRemainingFaceTime) {
        this.mRemainingFaceTime = mRemainingFaceTime;
    }
}
