
package de.klimek.spacecurl.game.tunnel;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;

/**
 * Wall
 */
public class Wall extends Drawable {
    private final int top;
    private final int bottom;

    public Wall(int top, int bottom) {
        this.top = top;
        this.bottom = bottom;
    }

    protected void draw(Canvas canvas, int positionX) {
        Paint paintTunnel = new Paint();
        paintTunnel.setColor(Color.WHITE);
        Paint paintWall = new Paint();
        paintWall.setColor(Color.BLACK);
        canvas.drawLine(positionX,
                0,
                positionX,
                canvas.getHeight(),
                paintWall);
        canvas.drawLine(positionX,
                top,
                positionX,
                getBottom(),
                paintTunnel);
    }

    @Override
    public void draw(Canvas canvas) {
        // TODO Auto-generated method stub

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

    public int getBottom() {
        return bottom;
    }

    public int getTop() {
        return top;
    }
}
