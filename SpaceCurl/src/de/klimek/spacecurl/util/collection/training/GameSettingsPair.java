
package de.klimek.spacecurl.util.collection.training;

import android.os.Bundle;
import de.klimek.spacecurl.game.GameFragment;

public class GameSettingsPair {
    private final Class<? extends GameFragment> mGameClass;
    private final Bundle mSettingsBundle;

    public GameSettingsPair(Class<? extends GameFragment> c) {
        mGameClass = c;
        mSettingsBundle = new Bundle();
    }

    public GameSettingsPair(Class<? extends GameFragment> c, Bundle settings) {
        mGameClass = c;
        mSettingsBundle = settings;
    }

    public Class<? extends GameFragment> getGameClass() {
        return mGameClass;
    }

    public Bundle getSettings() {
        return mSettingsBundle;
    }
}
