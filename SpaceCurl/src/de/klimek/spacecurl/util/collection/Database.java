
package de.klimek.spacecurl.util.collection;

import java.util.ArrayList;

import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import de.klimek.spacecurl.R;
import de.klimek.spacecurl.game.GameFragment;
import de.klimek.spacecurl.game.GameLights;
import de.klimek.spacecurl.game.GameMaze;
import de.klimek.spacecurl.game.GamePong;
import de.klimek.spacecurl.game.GameSensor;
import de.klimek.spacecurl.game.GameTunnel;
import de.klimek.spacecurl.game.GameUniversal;

/**
 * Singleton
 * 
 * @author Mike
 */
public class Database {
    private ArrayList<Training> mTrainings = new ArrayList<Training>();
    private ArrayList<Status> mStatuses = new ArrayList<Status>();
    private Training mFreeplayGames = new Training("Freeplay");
    private float mPhoneInclination = 0.1f;
    // TODO mInclinationFaktor
    private Resources mResources;
    private Orientation mOrientation = Orientation.Portrait;

    private static enum Orientation {
        Landscape, Portrait
    }

    private static Database sInstance;

    public static Database getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new Database(context);
        }
        return sInstance;
    }

    public static Database getInstance() {
        return sInstance;
    }

    private Database(Context context) {
        mResources = context.getResources();
        fillTrainings();
        fillFreeplayGames();
    }

    private void fillFreeplayGames() {
        // Add games to Spinner
        Bundle settingsTunnel = new Bundle();
        settingsTunnel.putString(GameFragment.ARG_TITLE, "Tunnel");
        GameSettingsPair gspTunnel = new GameSettingsPair(GameTunnel.class.getName(),
                settingsTunnel);
        getFreeplayGames().add(gspTunnel);

        Bundle settingsPong = new Bundle();
        settingsPong.putString(GameFragment.ARG_TITLE, mResources.getString(R.string.game_pong));
        GameSettingsPair gspPong = new GameSettingsPair(GamePong.class.getName(), settingsPong);
        getFreeplayGames().add(gspPong);

        // Bundle settingsMaze = new Bundle();
        // settingsMaze.putString(GameFragment.ARG_TITLE,
        // mResources.getString(R.string.game_maze));
        // GameSettingsPair gspMaze = new
        // GameSettingsPair(GameMaze.class.getName(),
        // settings);
        // mFreeplayGames.add(gspMaze);

        Bundle settingsLights = new Bundle();
        settingsLights
                .putString(GameFragment.ARG_TITLE, mResources.getString(R.string.game_lights));
        GameSettingsPair gspLights = new GameSettingsPair(GameLights.class.getName(),
                settingsLights);
        getFreeplayGames().add(gspLights);

        Bundle settingsUniversal = new Bundle();
        settingsUniversal.putString(GameFragment.ARG_TITLE,
                mResources.getString(R.string.game_universal));
        GameSettingsPair gspUniversal = new GameSettingsPair(GameUniversal.class.getName(),
                settingsUniversal);
        getFreeplayGames().add(gspUniversal);

        Bundle settingsSensor = new Bundle();
        settingsSensor.putString(GameFragment.ARG_TITLE, "Sensor Test");
        GameSettingsPair gspSensor = new GameSettingsPair(GameSensor.class.getName(),
                settingsSensor);
        getFreeplayGames().add(gspSensor);
    }

    private void fillTrainings() {
        // TODO SQL

        // 8
        Training training0 = new Training("8");
        // upperLeft
        Bundle upperLeft = new Bundle();
        upperLeft.putFloat(GameUniversal.ARG_TARGET_POSITION_X, 0.33f);
        upperLeft.putFloat(GameUniversal.ARG_TARGET_POSITION_Y, 0.33f);
        training0.add(new GameSettingsPair(GameUniversal.class.getName(), upperLeft));
        // upperRight
        Bundle upperRight = new Bundle();
        upperRight.putFloat(GameUniversal.ARG_TARGET_POSITION_X, 0.66f);
        upperRight.putFloat(GameUniversal.ARG_TARGET_POSITION_Y, 0.33f);
        training0.add(new GameSettingsPair(GameUniversal.class.getName(), upperRight));
        // middle
        Bundle middle = new Bundle();
        middle.putFloat(GameUniversal.ARG_TARGET_POSITION_X, 0.5f);
        middle.putFloat(GameUniversal.ARG_TARGET_POSITION_Y, 0.5f);
        training0.add(new GameSettingsPair(GameUniversal.class.getName(), middle));
        // lowerLeft
        Bundle lowerLeft = new Bundle();
        lowerLeft.putFloat(GameUniversal.ARG_TARGET_POSITION_X, 0.33f);
        lowerLeft.putFloat(GameUniversal.ARG_TARGET_POSITION_Y, 0.66f);
        training0.add(new GameSettingsPair(GameUniversal.class.getName(), lowerLeft));
        // lowerRight
        Bundle lowerRight = new Bundle();
        lowerRight.putFloat(GameUniversal.ARG_TARGET_POSITION_X, 0.66f);
        lowerRight.putFloat(GameUniversal.ARG_TARGET_POSITION_Y, 0.66f);
        training0.add(new GameSettingsPair(GameUniversal.class.getName(), lowerRight));
        // middle
        training0.add(new GameSettingsPair(GameUniversal.class.getName(),
                (Bundle) middle.clone()));
        mTrainings.add(training0);

        // Test training
        Training training1 = new Training("Test Training");
        training1.add(new GameSettingsPair(GamePong.class.getName(), new Bundle()));
        training1.add(new GameSettingsPair(GameUniversal.class.getName(), new Bundle()));
        training1.add(new GameSettingsPair(GameLights.class.getName(), new Bundle()));
        training1.add(new GameSettingsPair(GameMaze.class.getName(), new Bundle()));
        mTrainings.add(training1);

        Training training2 = new Training("Test Training 2");
        training2.add(new GameSettingsPair(GameUniversal.class.getName(), new Bundle()));
        training2.add(new GameSettingsPair(GamePong.class.getName(), new Bundle()));
        training2.add(new GameSettingsPair(GameUniversal.class.getName(), new Bundle()));
        training2.add(new GameSettingsPair(GameUniversal.class.getName(), new Bundle()));
        training2.add(new GameSettingsPair(GameLights.class.getName(), new Bundle()));
        mTrainings.add(training2);

        Training training3 = new Training("Noch ein Training");
        training3.add(new GameSettingsPair(GameUniversal.class.getName(), new Bundle()));
        training3.add(new GameSettingsPair(GamePong.class.getName(), new Bundle()));
        training3.add(new GameSettingsPair(GameUniversal.class.getName(), new Bundle()));
        training3.add(new GameSettingsPair(GameUniversal.class.getName(), new Bundle()));
        training3.add(new GameSettingsPair(GameLights.class.getName(), new Bundle()));
        mTrainings.add(training3);

        for (int i = 0; i < 10; i++) {
            Training training5 = new Training("Training" + i);
            training5.add(new GameSettingsPair(GameUniversal.class.getName(), new Bundle()));
            mTrainings.add(training5);
        }

    }

    public void saveDatabase() {
        // TODO SQL
    }

    public ArrayList<Training> getTrainings() {
        return mTrainings;
    }

    public ArrayList<Status> getStatuses() {
        return mStatuses;
    }

    public Training getFreeplayGames() {
        return mFreeplayGames;
    }

    public Orientation getOrientation() {
        return mOrientation;
    }

    public void setOrientation(Orientation orientation) {
        mOrientation = orientation;
    }

    public float getPhoneInclination() {
        return mPhoneInclination;
    }

    public void setPhoneInclination(float mPhoneInclination) {
        this.mPhoneInclination = mPhoneInclination;
    }

}
