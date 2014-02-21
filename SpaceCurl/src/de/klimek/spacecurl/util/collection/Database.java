
package de.klimek.spacecurl.util.collection;

import java.util.ArrayList;
import java.util.Random;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.preference.PreferenceManager;
import de.klimek.spacecurl.R;
import de.klimek.spacecurl.game.GameSettings;
import de.klimek.spacecurl.game.maze.MazeSettings;
import de.klimek.spacecurl.game.pong.PongSettings;
import de.klimek.spacecurl.game.sensor.SensorSettings;
import de.klimek.spacecurl.game.tunnel.TunnelSettings;
import de.klimek.spacecurl.game.universal.Target;
import de.klimek.spacecurl.game.universal.UniversalSettings;
import de.klimek.spacecurl.game.universal3D.Universal3DSettings;
import de.klimek.spacecurl.util.collection.status.GameStatus;
import de.klimek.spacecurl.util.collection.status.TrainingStatus;
import de.klimek.spacecurl.util.collection.training.Training;

/**
 * Singleton
 * 
 * @author Mike
 */
public class Database {
    private ArrayList<Training> mTrainings = new ArrayList<Training>();
    private ArrayList<TrainingStatus> mStatuses = new ArrayList<TrainingStatus>();
    private Training mTrainingGames = new Training("Trainings");

    private Training mFreeplayGames = new Training("Freeplay");
    private static final int FREEPLAY_STATUS_KEY = -1;

    private SharedPreferences mSharedPreferences;
    private Boolean mOrientationLandscape;
    private float mPhoneInclination;
    private float mPitchMultiplier;
    private float mRollMultiplier;
    private boolean mControlInversed = true;

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
        loadDatabase(context);
    }

    private void fillFreeplayGames(Context context) {
        Resources resources = context.getResources();
        // Add games to Spinner
        TunnelSettings settingsTunnel = new TunnelSettings();
        settingsTunnel.setTitle("Tunnel");
        mFreeplayGames.add(settingsTunnel);

        PongSettings settingsPong = new PongSettings();
        settingsPong.setTitle(resources.getString(R.string.game_pong));
        mFreeplayGames.add(settingsPong);

        MazeSettings settingsMaze = new MazeSettings();
        settingsMaze.setTitle(resources.getString(R.string.game_maze));
        mFreeplayGames.add(settingsMaze);

        MazeSettings settingsLights = new MazeSettings();
        settingsLights.setTitle(resources.getString(R.string.game_lights));
        mFreeplayGames.add(settingsLights);

        UniversalSettings settingsUniversal = new UniversalSettings();
        settingsUniversal.setTitle(resources.getString(R.string.game_universal));
        mFreeplayGames.add(settingsUniversal);

        Universal3DSettings settingsUniversal3D = new Universal3DSettings();
        settingsUniversal3D.setTitle(resources.getString(R.string.game_universal3d));
        mFreeplayGames.add(settingsUniversal3D);

        SensorSettings settingsSensor = new SensorSettings();
        settingsSensor.setTitle("Sensor Test");
        mFreeplayGames.add(settingsSensor);

        TrainingStatus freeplayStatus = new TrainingStatus(FREEPLAY_STATUS_KEY);
        for (GameSettings game : mFreeplayGames) {
            freeplayStatus.add(new GameStatus(game.getTitle()));
        }
    }

    private void fillTrainings(Context context) {
        Resources resources = context.getResources();
        mTrainingGames = (Training) mFreeplayGames.clone();

        // 8
        Training training0 = new Training("8");
        UniversalSettings settingsUniversal = new UniversalSettings();
        settingsUniversal.setTitle(resources.getString(R.string.game_universal));
        training0.add(settingsUniversal);

        PongSettings settingsPong = new PongSettings();
        settingsPong.setTitle(resources.getString(R.string.game_pong));
        training0.add(settingsPong);

        mTrainings.add(training0);

        // Test training
        Training training1 = new Training("Test Training");
        training1.add(new PongSettings());
        training1.add(new MazeSettings());
        training1.add(new Universal3DSettings());
        training1.add(new UniversalSettings());
        mTrainings.add(training1);

        Training training2 = new Training("Noch ein Training");
        training2.add(new TunnelSettings());
        training2.add(new TunnelSettings());
        training2.add(new UniversalSettings());
        training2.add(new UniversalSettings());
        training2.add(new UniversalSettings());
        training2.add(new TunnelSettings());
        mTrainings.add(training2);

        Random random = new Random();
        for (int i = 0; i < 10; i++) { // TODO inner loop
            boolean resetIfLeft = true;
            float targetPositionX = random.nextFloat();
            float targetPositionY = random.nextFloat();
            float targetRadius = random.nextFloat() / 10.0f + 0.02f;
            long holdingTime = 0;

            Training trainingLoop = new Training("Training" + i);
            UniversalSettings settings = new UniversalSettings();
            settings.addTarget(new Target(targetPositionX, targetPositionY, targetRadius,
                    holdingTime, resetIfLeft));
            trainingLoop.add(new UniversalSettings());
            mTrainings.add(trainingLoop);
        }

    }

    public void loadDatabase(Context context) {
        // TODO SQL instead of directly from code
        mSharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(context);
        fillFreeplayGames(context);
        fillTrainings(context);
        mOrientationLandscape = mSharedPreferences.getBoolean("pref_landscape", false);
        mPhoneInclination = Float.parseFloat(
                mSharedPreferences.getString("pref_inclination", "0.0"));
        mPitchMultiplier = Float.parseFloat(
                mSharedPreferences.getString("pref_pitch_multiplier", "1.0"));
        mRollMultiplier = Float.parseFloat(
                mSharedPreferences.getString("pref_roll_multiplier", "1.0"));
    }

    public void saveDatabase() {
        // TODO SQL
    }

    public ArrayList<Training> getTrainings() {
        return mTrainings;
    }

    public ArrayList<TrainingStatus> getStatuses() {
        return mStatuses;
    }

    public Training getFreeplayGames() {
        return mFreeplayGames;
    }

    public boolean isOrientationLandscape() {
        return mOrientationLandscape;
    }

    public void setOrientationLandscape(Boolean orientationLandscape) {
        mOrientationLandscape = orientationLandscape;
    }

    public float getPhoneInclination() {
        return mPhoneInclination;
    }

    public void setPhoneInclination(float phoneInclination) {
        mPhoneInclination = phoneInclination;
    }

    public float getPitchMultiplier() {
        return mPitchMultiplier;
    }

    public void setPitchMultiplier(float pitchMultiplier) {
        mPitchMultiplier = pitchMultiplier;
    }

    public float getRollMultiplier() {
        return mRollMultiplier;
    }

    public void setRollMultiplier(float rollMultiplier) {
        mRollMultiplier = rollMultiplier;
    }

    public Training getTrainingGames() {
        return mTrainingGames;
    }

    public void setTrainingGames(Training trainingGames) {
        mTrainingGames = trainingGames;
    }

    public boolean isControlInversed() {
        return mControlInversed;
    }

}
