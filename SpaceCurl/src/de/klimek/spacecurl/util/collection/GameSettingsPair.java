
package de.klimek.spacecurl.util.collection;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

public class GameSettingsPair implements Parcelable {
    private final String mGameClassName;
    private final Bundle mSettingsBundle;

    public GameSettingsPair(String game, Bundle settings) {
        this.mGameClassName = game;
        this.mSettingsBundle = settings;
    }

    private GameSettingsPair(Parcel in) {
        mGameClassName = in.readString();
        mSettingsBundle = in.readBundle();
    }

    public String getGameClassName() {
        return mGameClassName;
    }

    public Bundle getSettings() {
        return mSettingsBundle;
    }

    @Override
    public int describeContents() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mGameClassName);
        dest.writeBundle(mSettingsBundle);
    }

    public static final Parcelable.Creator<GameSettingsPair> CREATOR = new Parcelable.Creator<GameSettingsPair>() {
        public GameSettingsPair createFromParcel(Parcel in) {
            return new GameSettingsPair(in);
        }

        public GameSettingsPair[] newArray(int size) {
            return new GameSettingsPair[size];
        }
    };

}
