
package de.klimek.spacecurl.preferences;

import android.app.Dialog;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import de.klimek.spacecurl.R;
import de.klimek.spacecurl.util.collection.Database;

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

    /**
     * Inner class which displays settings in a Fragment. <br>
     * Settings are loaded from an XML resource. Static for system access
     * (restoring)
     */
    public static class SettingsFragment extends PreferenceFragment implements
            OnSharedPreferenceChangeListener {
        public static final String ARG_SETTINGS_RESOURCE = "ARG_SETTINGS_RESOURCE";
        private Database mDatabase = Database.getInstance();
        public SharedPreferences mSharedPrefs;
        private EditTextPreference mInclinationPreference;
        private EditTextPreference mPitchMultPreference;
        private EditTextPreference mRollMultPreference;

        // Empty constructor required for fragment subclasses
        public SettingsFragment() {

        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            int preferencesResId = getArguments().getInt(ARG_SETTINGS_RESOURCE);
            addPreferencesFromResource(preferencesResId);
            mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
            mInclinationPreference = (EditTextPreference) findPreference("pref_inclination");
            mInclinationPreference.setSummary(mSharedPrefs.getString("pref_inclination", "0.0"));

            mPitchMultPreference = (EditTextPreference) findPreference("pref_pitch_multiplier");
            mPitchMultPreference.setSummary(mSharedPrefs.getString("pref_pitch_multiplier", "1.0"));

            mRollMultPreference = (EditTextPreference) findPreference("pref_roll_multiplier");
            mRollMultPreference.setSummary(mSharedPrefs.getString("pref_roll_multiplier", "1.0"));
        }

        @Override
        public void onPause() {
            mSharedPrefs.unregisterOnSharedPreferenceChangeListener(this);
            super.onPause();
        }

        @Override
        public void onResume() {
            mSharedPrefs.registerOnSharedPreferenceChangeListener(this);
            super.onResume();
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            if (key.equals("pref_landscape")) {
                if (sharedPreferences.getBoolean(key, false)) {
                    mDatabase.setOrientationLandscape(true);
                    getActivity()
                            .setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                } else {
                    mDatabase.setOrientationLandscape(false);
                    getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                }
            }
            else if (key.equals("pref_inclination")) {
                String value = sharedPreferences.getString(key, "0.0");
                mInclinationPreference.setSummary(value);
                mDatabase.setPhoneInclination(Float.parseFloat(value));
            }
            else if (key.equals("pref_pitch_multiplier")) {
                String value = sharedPreferences.getString(key, "1.0");
                mPitchMultPreference.setSummary(value);
                mDatabase.setPitchMultiplier(Float.parseFloat(value));
            }
            else if (key.equals("pref_roll_multiplier")) {
                String value = sharedPreferences.getString(key, "1.0");
                mRollMultPreference.setSummary(value);
                mDatabase.setRollMultiplier(Float.parseFloat(value));
            }

        }

        @Override
        public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
                Preference preference) {
            super.onPreferenceTreeClick(preferenceScreen, preference);
            // If the user has clicked on a preference screen, set up the
            // ActionBar
            if (preference instanceof PreferenceScreen) {
                initializeActionBar((PreferenceScreen) preference);
            }

            return false;
        }

        /**
         * Sets up the action bar for a {@link PreferenceScreen}. <br>
         * Some "hacky programming" required, to enable up navigation with
         * "up-caret" in nested PreferenceScreens.
         * 
         * @see <a
         *      href="http://stackoverflow.com/questions/16374820/action-bar-home-button-not-functional-with-nested-preferencescreen/16800527#16800527">
         *      Solution for Enabling up navigation in nested PreferenceScreens
         *      on StackOverFlow</a>
         */
        private static void initializeActionBar(PreferenceScreen preferenceScreen) {
            final Dialog dialog = preferenceScreen.getDialog();
            if (dialog != null) {
                // Inialize the action bar
                dialog.getActionBar().setDisplayHomeAsUpEnabled(true);
                /*
                 * Apply custom home button area click listener to close the
                 * PreferenceScreen because PreferenceScreens are dialogs which
                 * swallow events instead of passing to the activity. Related
                 * Issue:
                 * https://code.google.com/p/android/issues/detail?id=4611
                 */
                View homeBtn = dialog.findViewById(android.R.id.home);
                if (homeBtn == null) {
                    return;
                }
                OnClickListener dismissDialogClickListener = new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                };
                // Prepare yourselves for some hacky programming
                ViewParent homeBtnContainer = homeBtn.getParent();
                // The home button is an ImageView inside a FrameLayout
                if (homeBtnContainer instanceof FrameLayout) {
                    ViewGroup containerParent = (ViewGroup) homeBtnContainer.getParent();
                    if (containerParent instanceof LinearLayout) {
                        // This view also contains the title text, set the
                        // whole view as clickable
                        ((LinearLayout) containerParent)
                                .setOnClickListener(dismissDialogClickListener);
                    } else {
                        // Just set it on the home button
                        ((FrameLayout) homeBtnContainer)
                                .setOnClickListener(dismissDialogClickListener);
                    }
                } else {
                    // The 'If all else fails' default case
                    homeBtn.setOnClickListener(dismissDialogClickListener);
                }
            }
        }
    }

}
