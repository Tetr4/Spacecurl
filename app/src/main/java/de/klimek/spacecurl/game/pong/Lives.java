
package de.klimek.spacecurl.game.pong;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;

import de.klimek.spacecurl.R;

/**
 * Multiple icons for remaining lives
 */
public class Lives {
    private static final int RADIUS = 32;
    private static final int PADDING_TOP = 8;
    private static final int PADDING_SIDE = 8;
    private int mLiveCount;
    private Bitmap mSmileyBitmap;

    public Lives(int lives, Resources res) {
        mLiveCount = lives;
        // mSource.set(0,
        // 0,
        // RADIUS * 2,
        // RADIUS * 2);
        Bitmap smiley = BitmapFactory.decodeResource(res,
                R.drawable.smiley_normal);
        mSmileyBitmap = Bitmap.createScaledBitmap(smiley, RADIUS * 2,
                RADIUS * 2,
                true);
    }

    public void setLives(int lives) {
        mLiveCount = lives;
    }

    public int getLives() {
        return mLiveCount;
    }

    protected void draw(Canvas canvas) {
        for (int i = 0; i < mLiveCount; i++) {
            canvas.drawBitmap(mSmileyBitmap, i * RADIUS * 2 + PADDING_SIDE * (i + 1), PADDING_TOP,
                    null);
        }
    }
}
