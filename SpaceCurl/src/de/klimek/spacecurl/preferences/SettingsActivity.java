
package de.klimek.spacecurl.preferences;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import de.klimek.spacecurl.Database;
import de.klimek.spacecurl.R;

/**
 * Activity showing general settings and headers leading to game settings. <br>
 * All settings are defined in XML in <a
 * href="../../../../res/xml/preferences.xml" >res/xml/preferences.xml</a>.
 * 
 * @author Mike Klimek
 * @see <a href ="http://developer.android.com/guide/topics/ui/settings.html">
 *      Settings Guide</a>
 */
public class SettingsActivity extends FragmentActivity {
    private Database mDatabase = Database.getInstance(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (mDatabase.isOrientationLandscape()) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
        // enable "up-caret" to navigate back to FreePlayActivity
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getFragmentManager()
                .beginTransaction()
                .replace(android.R.id.content, createSettingFragment(R.xml.preferences))
                .commit();
    }

    /**
     * A factory for creating a {@link SettingsFragment}.
     * 
     * @param preferencesResId XML resource to load preferences from
     * @return The created {@link SettingsFragment}
     */
    private static SettingsFragment createSettingFragment(int preferencesResId) {
        Bundle args = new Bundle();
        args.putInt(SettingsFragment.ARG_SETTINGS_RESOURCE, preferencesResId);
        SettingsFragment settingsFragment = new SettingsFragment();
        settingsFragment.setArguments(args);
        return settingsFragment;
    }

}
