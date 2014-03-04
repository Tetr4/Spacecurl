
package de.klimek.spacecurl.game.tunnel;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import de.klimek.spacecurl.R;

/**
 * Lives
 */
public class Lives {
    private static final int PADDING_SIDE = 8;
    private int mLives;
    private int mWidth;
    private int mHeight;
    private Bitmap mRocketBitmap;

    public Lives(int lives, int width, int height, Resources res) {
        mLives = lives;
        mWidth = width;
        mHeight = height;
        Bitmap smiley = BitmapFactory.decodeResource(res,
                R.drawable.rocket);
        mRocketBitmap = Bitmap.createScaledBitmap(smiley, mWidth,
                mHeight,
                true);
    }

    public void setLives(int lives) {
        mLives = lives;
    }

    public int getLives() {
        return mLives;
    }

    protected void draw(Canvas canvas) {
        for (int i = 0; i < mLives; i++) {
            canvas.drawBitmap(mRocketBitmap, mWidth * i + PADDING_SIDE * (i + 1),
                    canvas.getHeight() / 2f - mHeight / 2f,
                    null);
        }
    }
}
