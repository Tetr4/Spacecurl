
package de.klimek.spacecurl.game.tunnel;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;

public class AnimatedSprite {
    private Bitmap animation;
    private int xPos;
    private int yPos;
    private Rect sRectangle;
    private Rect mDest;
    private int fps;
    private int numFrames;
    private int currentFrame;
    private long frameTimer;
    private int spriteHeight;
    private int spriteWidth;
    private boolean loop;
    public boolean dispose;

    public AnimatedSprite() {
        sRectangle = new Rect(0, 0, 0, 0);
        frameTimer = 0;
        currentFrame = 0;
        xPos = 80;
        yPos = 200;
        dispose = false;
    }

    public void Initialize(Bitmap bitmap, int height, int width, int fps, int frameCount,
            boolean loop) {
        this.animation = bitmap;
        this.spriteHeight = height;
        this.spriteWidth = width;
        this.sRectangle.top = 0;
        this.sRectangle.bottom = spriteHeight;
        this.sRectangle.left = 0;
        this.sRectangle.right = spriteWidth;
        this.fps = 1000 / fps;
        this.numFrames = frameCount;
        this.loop = loop;
    }

    public int getXPos() {
        return xPos;
    }

    public int getYPos() {
        return yPos;
    }

    public void setXPos(int value) {
        xPos = value - (spriteWidth / 2);
    }

    public void setYPos(int value) {
        yPos = value - (spriteHeight / 2);
    }

    public void Update(long gameTime) {
        if (gameTime > frameTimer + fps) {
            frameTimer = gameTime;
            currentFrame += 1;

            if (currentFrame >= numFrames) {
                currentFrame = 0;

                if (!loop)
                    dispose = true;
            }

            sRectangle.left = currentFrame * spriteWidth;
            sRectangle.right = sRectangle.left + spriteWidth;
        }
    }

    public void draw(Canvas canvas) {
        mDest = new Rect(getXPos(), getYPos(), getXPos() + spriteWidth,
                getYPos() + spriteHeight);
        canvas.drawBitmap(animation, sRectangle, mDest, null);
    }
}
