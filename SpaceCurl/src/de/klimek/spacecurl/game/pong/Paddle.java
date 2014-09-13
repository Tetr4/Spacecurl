
package de.klimek.spacecurl.game.pong;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import de.klimek.spacecurl.R;

/**
 * Paddle for Pong
 */
public class Paddle {
    int mWidth = 196;
    int mHeight = 50;
    int mPadding = 12;
    float mPosition;
    private Paint mPaint = new Paint();
    private Rect mSource = new Rect();
    private Rect mDest = new Rect();
    PaddleSide mSide;
    int mOnScreenCenterX;
    int mOnScreenCenterY;
    int mOnScreenWidth;
    int mOnScreenHeight;
    private Bitmap mPaddleBitmapScaled;

    static enum PaddleSide {
        Left, Top, Right, Bottom
    }

    public Paddle(PaddleSide side, Resources res) {
        mSide = side;
        mPaint.setColor(Color.WHITE);
        // mPaint.setShadowLayer(10.0f, 0.0f, 2.0f, 0xFF000000);
        // setLayerType(LAYER_TYPE_SOFTWARE, mPaint);
        if (side == PaddleSide.Left || side == PaddleSide.Right) {
            mOnScreenWidth = mHeight;
            mOnScreenHeight = mWidth;
        } else {
            mOnScreenWidth = mWidth;
            mOnScreenHeight = mHeight;
        }
        mSource.set(0,
                0,
                mOnScreenWidth,
                mOnScreenHeight);
        Bitmap paddleBitmap = BitmapFactory.decodeResource(res,
                R.drawable.cloud);
        mPaddleBitmapScaled = Bitmap.createScaledBitmap(paddleBitmap, mOnScreenWidth,
                mOnScreenHeight, true);
    }

    protected void draw(Canvas canvas) {
        switch (mSide) {
            case Left:
                mOnScreenCenterX = mPadding + mHeight / 2;
                mOnScreenCenterY = (int) ((mPosition + 1.0f) / 2.0f
                        * (canvas.getHeight() - (mWidth + mPadding * 2)) + mWidth / 2 + mPadding);
                break;
            case Top:
                mOnScreenCenterX = (int) ((mPosition + 1.0f) / 2.0f
                        * (canvas.getWidth() - (mWidth + mPadding * 2)) + mWidth / 2 + mPadding);
                mOnScreenCenterY = mPadding + mHeight / 2;
                break;
            case Right:
                mOnScreenCenterX = canvas.getWidth() - mPadding - mHeight /
                        2;
                mOnScreenCenterY = (int) ((mPosition + 1.0f) / 2.0f
                        * (canvas.getHeight() - (mWidth + mPadding * 2)) + mWidth / 2 + mPadding);
                break;
            case Bottom:
                mOnScreenCenterX = (int) ((mPosition + 1.0f) / 2.0f
                        * (canvas.getWidth() - (mWidth + mPadding * 2)) + mWidth / 2 + mPadding);
                mOnScreenCenterY = canvas.getHeight() - mPadding - mHeight /
                        2;
                break;
        }

        mDest.set(mOnScreenCenterX - mOnScreenWidth / 2, mOnScreenCenterY
                - mOnScreenHeight / 2, mOnScreenCenterX + mOnScreenWidth / 2,
                mOnScreenCenterY + mOnScreenHeight / 2);
        canvas.drawBitmap(mPaddleBitmapScaled, mSource, mDest, null);
        // canvas.drawRect(mRect, mPaint);
    }
}
