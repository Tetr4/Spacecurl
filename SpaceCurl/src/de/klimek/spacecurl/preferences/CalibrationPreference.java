
package de.klimek.spacecurl.preferences;

import android.content.Context;
import android.preference.DialogPreference;
import android.util.AttributeSet;

public class CalibrationPreference extends DialogPreference {
    public CalibrationPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        // setDialogLayoutResource(R.layout.numberpicker_dialog);
        // setDialogIcon(null);
    }
    // @Override
    // protected void onDialogClosed(boolean positiveResult) {
    // // When the user selects "OK", persist the new value
    // if (positiveResult) {
    // persistInt(1);
    // }
    // }

}
