
package de.klimek.spacecurl.util.collection;

import java.util.ArrayList;
import java.util.Random;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.preference.PreferenceManager;
import de.klimek.spacecurl.R;
import de.klimek.spacecurl.game.GameSettings;
import de.klimek.spacecurl.game.lights.LightsSettings;
import de.klimek.spacecurl.game.pong.PongSettings;
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
        TunnelSettings settingsTunnel = new TunnelSettings("Tunnel", 1);
        settingsTunnel.setShowLives(false);
        mFreeplayGames.add(settingsTunnel);

        PongSettings settingsPong = new PongSettings(resources.getString(R.string.game_pong), 1);
        settingsPong.setShowLives(false);
        mFreeplayGames.add(settingsPong);

        // MazeSettings settingsMaze = new
        // MazeSettings(resources.getString(R.string.game_maze));
        // mFreeplayGames.add(settingsMaze);

        LightsSettings settingsLights = new LightsSettings(
                resources.getString(R.string.game_lights));
        mFreeplayGames.add(settingsLights);

        // UniversalSettings settingsUniversal = new UniversalSettings(
        // resources.getString(R.string.game_universal));
        // mFreeplayGames.add(settingsUniversal);

        Universal3DSettings settingsUniversal3D = new Universal3DSettings("Virtual Reality",
                R.drawable.photoshpere_island2);
        mFreeplayGames.add(settingsUniversal3D);

        // SensorSettings settingsSensor = new SensorSettings("Sensor Test");
        // mFreeplayGames.add(settingsSensor);

        TrainingStatus freeplayStatus = new TrainingStatus(FREEPLAY_STATUS_KEY);
        for (GameSettings game : mFreeplayGames) {
            freeplayStatus.add(new GameStatus(game.getTitle()));
        }
    }

    private void fillTrainings(Context context) {
        Resources resources = context.getResources();
        mTrainingGames = (Training) mFreeplayGames.clone();

        // /*
        // * BALANCE
        // */
        // Training balanceTraining = new Training("Balance");
        // // Lotrecht
        // UniversalSettings settingsLotrecht = new
        // UniversalSettings("Lotrecht");
        // settingsLotrecht.addTarget(new Target(0.5f, 0.5f, 0.05f, 6000));
        // settingsLotrecht
        // .setInstructions("Begib dich in die lotrechte Position (stehe kerzengerade)");
        // balanceTraining.add(settingsLotrecht);
        //
        // // Lotrecht 2 Achsen, Augenbinde
        // UniversalSettings settingsLotrecht2 = new
        // UniversalSettings("Lotrecht 2 Achsen");
        // settingsLotrecht2.addTarget(new Target(0.5f, 0.5f, 0.05f, 6000));
        // settingsLotrecht2.setInstructions("Begib dich in die lotrechte Position");
        // balanceTraining.add(settingsLotrecht2);
        //
        // // Sagittal
        // UniversalSettings settingsSagittal = new
        // UniversalSettings("Sagittal");
        // for (int i = 0; i < 5; i++) {
        // settingsSagittal.addTarget(new Target(0.5f, 0.75f, 0.1f));
        // settingsSagittal.addTarget(new Target(0.5f, 0.25f, 0.1f));
        // }
        // settingsSagittal
        // .setInstructions("Bewege dich um die Sagittalachse (nach vorne und hinten)");
        // balanceTraining.add(settingsSagittal);
        //
        // // Bauch- und Rueckenlage
        // UniversalSettings settingsBauchRueck = new
        // UniversalSettings("Bauch- und Rückenlage");
        // settingsBauchRueck.addTarget(new Target(0.5f, 0.8f, 0.07f, 6000));
        // settingsBauchRueck.addTarget(new Target(0.5f, 0.2f, 0.07f, 6000));
        // settingsBauchRueck
        // .setInstructions("Begib dich in die Bauchlage und dann in die Rückenlage");
        // balanceTraining.add(settingsBauchRueck);
        //
        // // Abschluss
        // UniversalSettings settingsAchsen = new UniversalSettings("Achsen");
        // settingsAchsen.addTarget(new Target(0.5f, 0.5f, 0.1f));
        // for (int i = 0; i < 3; i++) { // transversal
        // settingsAchsen.addTarget(new Target(0.25f, 0.5f, 0.1f));
        // settingsAchsen.addTarget(new Target(0.75f, 0.5f, 0.1f));
        // }
        // settingsAchsen.addTarget(new Target(0.5f, 0.5f, 0.1f));
        // for (int i = 0; i < 3; i++) { // sagittal
        // settingsAchsen.addTarget(new Target(0.5f, 0.75f, 0.1f));
        // settingsAchsen.addTarget(new Target(0.5f, 0.25f, 0.1f));
        // }
        // settingsAchsen.addTarget(new Target(0.5f, 0.5f, 0.1f));
        // for (int i = 0; i < 3; i++) { // diagonal
        // settingsAchsen.addTarget(new Target(0.25f, 0.25f, 0.1f));
        // settingsAchsen.addTarget(new Target(0.75f, 0.75f, 0.1f));
        // }
        // settingsAchsen.setInstructions("Bewege dich um die dargestellten Achsen");
        // balanceTraining.add(settingsAchsen);
        //
        // // Viereckiger Grundrahmen
        // UniversalSettings settingsViereck = new UniversalSettings("Viereck");
        // for (int i = 0; i < 4; i++) {
        // settingsViereck.addTarget(new Target(0.25f, 0.25f, 0.1f));
        // settingsViereck.addTarget(new Target(0.75f, 0.25f, 0.1f));
        // settingsViereck.addTarget(new Target(0.75f, 0.75f, 0.1f));
        // settingsViereck.addTarget(new Target(0.25f, 0.75f, 0.1f));
        // }
        // settingsViereck.setInstructions("Bewege dich um den viereckigen Grundrahmen");
        // balanceTraining.add(settingsViereck);
        //
        // // Achterkreis
        // UniversalSettings settingsAcht = new UniversalSettings("Acht");
        // for (int i = 0; i < 4; i++) {
        // settingsAcht.addTarget(new Target(0.5f, 0.5f, 0.1f));
        // settingsAcht.addTarget(new Target(0.25f, 0.3f, 0.1f));
        // settingsAcht.addTarget(new Target(0.5f, 0.2f, 0.1f));
        // settingsAcht.addTarget(new Target(0.75f, 0.3f, 0.1f));
        // settingsAcht.addTarget(new Target(0.5f, 0.5f, 0.1f));
        // settingsAcht.addTarget(new Target(0.25f, 0.7f, 0.1f));
        // settingsAcht.addTarget(new Target(0.5f, 0.8f, 0.1f));
        // settingsAcht.addTarget(new Target(0.75f, 0.7f, 0.1f));
        // }
        // settingsAcht.setInstructions("Bewege dich in einer 8");
        // balanceTraining.add(settingsAcht);
        //
        // mTrainings.add(balanceTraining);

        /*
         * BALANCE MIT GAMES
         */
        Training balanceTraining2 = new Training("Balance");
        // Lotrecht
        UniversalSettings settingsLotrecht = new UniversalSettings("Lotrecht");
        settingsLotrecht.addTarget(new Target(0.5f, 0.5f, 0.05f, 6000));
        settingsLotrecht
                .setInstructions("Halte deine Position lotrecht (stehe kerzengerade)");
        balanceTraining2.add(settingsLotrecht);

        // Sagittal
        UniversalSettings settingsSagittal = new UniversalSettings("Sagittal");
        for (int i = 0; i < 5; i++) {
            settingsSagittal.addTarget(new Target(0.5f, 0.75f, 0.1f));
            settingsSagittal.addTarget(new Target(0.5f, 0.25f, 0.1f));
        }
        settingsSagittal
                .setInstructions("Bewege dich nach vorne und hinten");
        balanceTraining2.add(settingsSagittal);

        // Bauch- und Rueckenlage
        UniversalSettings settingsBauchRueck = new UniversalSettings("Bauch- und Rückenlage");
        settingsBauchRueck.addTarget(new Target(0.5f, 0.8f, 0.07f, 6000));
        settingsBauchRueck.addTarget(new Target(0.5f, 0.2f, 0.07f, 6000));
        settingsBauchRueck
                .setInstructions("Halte deine Position in der Bauchlage und dann in der Rückenlage");
        balanceTraining2.add(settingsBauchRueck);

        // Tunnel
        TunnelSettings settingsTunnel = new TunnelSettings("Tunnel", 3);
        settingsTunnel
                .setInstructions("Verlagere dein Gewicht, damit die Rakete nicht an den Rand stößt, und versuche eine möglichst weite Strecke zu fliegen");
        balanceTraining2.add(settingsTunnel);

        // Abschluss
        UniversalSettings settingsAchsen = new UniversalSettings("Achsen");
        settingsAchsen.addTarget(new Target(0.5f, 0.5f, 0.1f));
        for (int i = 0; i < 3; i++) { // transversal
            settingsAchsen.addTarget(new Target(0.25f, 0.5f, 0.1f));
            settingsAchsen.addTarget(new Target(0.75f, 0.5f, 0.1f));
        }
        settingsAchsen.addTarget(new Target(0.5f, 0.5f, 0.1f));
        for (int i = 0; i < 3; i++) { // sagittal
            settingsAchsen.addTarget(new Target(0.5f, 0.75f, 0.1f));
            settingsAchsen.addTarget(new Target(0.5f, 0.25f, 0.1f));
        }
        settingsAchsen.addTarget(new Target(0.5f, 0.5f, 0.1f));
        for (int i = 0; i < 3; i++) { // diagonal
            settingsAchsen.addTarget(new Target(0.25f, 0.25f, 0.1f));
            settingsAchsen.addTarget(new Target(0.75f, 0.75f, 0.1f));
        }
        settingsAchsen.setInstructions("Bewege dich um die dargestellten Achsen");
        balanceTraining2.add(settingsAchsen);

        // Viereckiger Grundrahmen
        UniversalSettings settingsViereck = new UniversalSettings("Viereck");
        for (int i = 0; i < 4; i++) {
            settingsViereck.addTarget(new Target(0.25f, 0.25f, 0.1f));
            settingsViereck.addTarget(new Target(0.75f, 0.25f, 0.1f));
            settingsViereck.addTarget(new Target(0.75f, 0.75f, 0.1f));
            settingsViereck.addTarget(new Target(0.25f, 0.75f, 0.1f));
        }
        settingsViereck.setInstructions("Bewege dich im Viereck");
        balanceTraining2.add(settingsViereck);

        // Pong
        PongSettings settingsPong = new PongSettings("Pong", 3);
        settingsPong.setInstructions("Versuche den Ball möglichst oft zu blocken.");
        balanceTraining2.add(settingsPong);

        // Abbremsen
        LightsSettings settingsBremsen = new LightsSettings("Abbremsen");
        settingsBremsen
                .setInstructions("Um den Balken zu füllen, bewege dich in der grünen Phase, und stoppe in der roten Phase\n\nHalte dich dabei an den Handgriffen fest");
        balanceTraining2.add(settingsBremsen);

        mTrainings.add(balanceTraining2);

        // /*
        // * GROBKOORDINATION
        // */
        // Training grobKoordinationsTraining = new
        // Training("Grobkoordination");
        // // Bremsen Handgriffe
        // LightsSettings settingsBremsen = new LightsSettings("Abbremsen");
        // settingsBremsen
        // .setInstructions("Um den Balken zu füllen, bewege dich in der grünen Phase, und stoppe in der roten Phase\n\nHalte dich dabei an den Handgriffe fest");
        // grobKoordinationsTraining.add(settingsBremsen);
        //
        // // Bremsen Handgriffe
        // LightsSettings settingsBremsen2 = new LightsSettings("Abbremsen");
        // settingsBremsen2
        // .setInstructions("Um den Balken zu füllen, bewege dich in der grünen Phase, und stoppe in der roten Phase\n\nDiemsal ohne Handgriffe");
        // grobKoordinationsTraining.add(settingsBremsen2);
        //
        // // Rolle
        // UniversalSettings settingsRolle = new UniversalSettings("Rolle");
        // settingsRolle.addTarget(new Target(0.5f, 0.05f, 0.15f));
        // settingsRolle.addTarget(new Target(0.5f, 0.95f, 0.15f));
        // settingsRolle.setInstructions("Mache eine Vorwärts- oder Rückwärtsrolle");
        // grobKoordinationsTraining.add(settingsRolle);
        //
        // mTrainings.add(grobKoordinationsTraining);
        //
        // // Viereckiger Grundrahmen
        // Training training0 = new Training("Viereck");
        // UniversalSettings settingsUniversal = new
        // UniversalSettings("Viereck");
        // settingsUniversal.addTarget(new Target(0.25f, 0.25f, 0.1f));
        // settingsUniversal.addTarget(new Target(0.75f, 0.25f, 0.15f));
        // settingsUniversal.addTarget(new Target(0.75f, 0.75f, 0.2f));
        // settingsUniversal.addTarget(new Target(0.25f, 0.75f));
        // settingsUniversal.setTitle(resources.getString(R.string.game_universal));
        // training0.add(settingsUniversal);

        // UniversalSettings settingsUniversal2 = new UniversalSettings();
        // settingsUniversal2.addPath(new Path(0.0f, 1.0f, 1.0f, 0.0f, 0.1f,
        // 200));
        // settingsUniversal2.setTitle(resources.getString(R.string.game_universal));
        // training0.add(settingsUniversal2);

        // mTrainings.add(training0);

        Random random = new Random();
        for (int i = 1; i <= 10; i++) {
            Training trainingLoop = new Training("Zufalls Training" + Integer.toString(i));

            for (int j = 1; j <= 10; j++) {
                UniversalSettings settingsRandom = new UniversalSettings("Zufallsübung"
                        + Integer.toString(j));

                int nrOfTargets = random.nextInt(3) + 1;
                for (int k = 1; k <= nrOfTargets; k++) {
                    boolean resetIfLeft = random.nextBoolean();
                    float targetPositionX = random.nextFloat();
                    float targetPositionY = random.nextFloat();
                    float targetRadius = random.nextFloat() / 10.0f + 0.02f;
                    long holdingTime = 0;
                    if (random.nextBoolean()) {
                        holdingTime = (long) (random.nextFloat() * 4500f) + 500;
                    }

                    settingsRandom.addTarget(new Target(targetPositionX, targetPositionY,
                            targetRadius,
                            holdingTime, resetIfLeft));
                }
                trainingLoop.add(settingsRandom);
            }
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
