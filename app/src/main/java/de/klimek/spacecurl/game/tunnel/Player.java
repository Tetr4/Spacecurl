
package de.klimek.spacecurl.game.tunnel;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;

import de.klimek.spacecurl.R;

/**
 * Player
 */
public class Player extends Drawable {
    private int mWidth;
    private int mHeight;
    private int mPositionX = 0;
    private int mPositionY = 0;
    private Paint mPaint = new Paint();
    private Bitmap mRocket;
    private Bitmap mRocketScaled;
    private Rect mSource = new Rect();
    private Rect mDest = new Rect();

    public Player(int width, int height, Resources res) {
        // mPaint.setColor(Color.RED);
        mPaint.setAlpha(100);
        mWidth = width;
        mHeight = height;
        mRocket = BitmapFactory.decodeResource(res,
                R.drawable.rocket);
        mSource.set(0,
                0,
                mWidth,
                mHeight);
        mRocketScaled = Bitmap.createScaledBitmap(mRocket, mWidth, mHeight, true);
    }

    @Override
    public void draw(Canvas canvas) {
        mDest.set(mPositionX - mWidth / 2,
                mPositionY - mHeight / 2,
                mPositionX + mWidth / 2,
                mPositionY + mHeight / 2);
        canvas.drawBitmap(mRocketScaled, mSource, mDest, null);
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

    public int getPositionX() {
        return mPositionX;
    }

    public void setPositionX(int positionX) {
        mPositionX = positionX;
    }

    public int getPositionY() {
        return mPositionY;
    }

    public void setPositionY(int positionY) {
        mPositionY = positionY;
    }

    public int getWidth() {
        return mWidth;
    }

    public void setWidth(int mWidth) {
        this.mWidth = mWidth;
    }

    public int getHeight() {
        return mHeight;
    }

    public void setHeight(int mHeight) {
        this.mHeight = mHeight;
    }
}
