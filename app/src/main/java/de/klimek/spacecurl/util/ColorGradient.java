
package de.klimek.spacecurl.util;

import android.graphics.Color;

/**
 * Provides linear interpolation over multiple colors, e.g. to create gradients.
 * 
 * @author Mike Klimek
 */
public class ColorGradient {
    private int[] mColors;

    public ColorGradient(int... gradientColors) {
        mColors = gradientColors;
    }

    /**
     * @param fraction from 0.0f to 1.0f
     * @return color corresponding to the fraction
     */
    public int getColorForFraction(float fraction) {
        if (mColors.length == 1) {
            return mColors[0];
        }

        int index = (int) (fraction * (mColors.length - 1));
        if (index < 0) {
            return mColors[0];
        }
        if (index >= mColors.length - 1) {
            return mColors[mColors.length - 1];
        }
        float adjustFraction = (fraction - (float) index
                / (mColors.length - 1)) * (mColors.length - 1);
        return interpolateColor(mColors[index], mColors[index + 1], adjustFraction);
    }

    public static int interpolateColor(int color1, int color2, float fraction) {
        fraction = Math.min(fraction, 1.0f);
        fraction = Math.max(fraction, 0.0f);

        int deltaAlpha = Color.alpha(color2) - Color.alpha(color1);
        int deltaRed = Color.red(color2) - Color.red(color1);
        int deltaGreen = Color.green(color2) - Color.green(color1);
        int deltaBlue = Color.blue(color2) - Color.blue(color1);

        int resultAlpha = (int) (Color.alpha(color1) + (deltaAlpha * fraction));
        int resultRed = (int) (Color.red(color1) + (deltaRed * fraction));
        int resultGreen = (int) (Color.green(color1) + (deltaGreen * fraction));
        int resultBlue = (int) (Color.blue(color1) + (deltaBlue * fraction));

        resultAlpha = Math.max(Math.min(resultAlpha, 255), 0);
        resultRed = Math.max(Math.min(resultRed, 255), 0);
        resultGreen = Math.max(Math.min(resultGreen, 255), 0);
        resultBlue = Math.max(Math.min(resultBlue, 255), 0);

        return Color.argb(resultAlpha, resultRed, resultGreen, resultBlue);
    }

}
