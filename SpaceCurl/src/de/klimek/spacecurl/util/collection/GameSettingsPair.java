
package de.klimek.spacecurl.util.collection;

import android.os.Bundle;

public class GameSettingsPair {
    private final String mGameClassName;
    private final Bundle mSettingsBundle;

    public GameSettingsPair(String className, Bundle settings) {
        this.mGameClassName = className;
        this.mSettingsBundle = settings;
    }

    public String getGameClassName() {
        return mGameClassName;
    }

    public Bundle getSettings() {
        return mSettingsBundle;
    }
}
