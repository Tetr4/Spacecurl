
package de.klimek.spacecurl;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.preference.PreferenceManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import de.klimek.spacecurl.game.lights.LightsDescription;
import de.klimek.spacecurl.game.pong.PongDescription;
import de.klimek.spacecurl.game.tunnel.TunnelDescription;
import de.klimek.spacecurl.game.universal.Target;
import de.klimek.spacecurl.game.universal.UniversalDescription;
import de.klimek.spacecurl.game.universal3D.Universal3DDescription;
import de.klimek.spacecurl.util.collection.Training;

/**
 * Singleton database access
 * 
 * @author Mike Klimek
 */
public class Database {
    private List<Training> mTrainings = new ArrayList<Training>();
    private Training mFreeplayGames = new Training("Free Play");
    private Training mSelectableGames = new Training("Selectable Games");

    private SharedPreferences mSharedPreferences;
    private Boolean mOrientationLandscape;
    private float mPhoneInclination;
    private float mPitchMultiplier;
    private float mRollMultiplier;
    private boolean mControlInversed = true;

    private static Database sInstance;

    public static Database getInstance(Context context) {
        // TODO dependency injection instead of singleton?
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

    public void loadDatabase(Context context) {
        // TODO SQL instead of directly from code

        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

        loadSelectableGames(context);

        addFreeplayGames(context);
        addMixedTraining(context);
        // addBalanceTraining(context);
        // addCoordTraining(context);
        for (int i = 0; i <= 10; i++) {
            loadRandomTraining(context, "Zufallstraining " + Integer.toString(i));
        }

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

    /**
     * Games which are available in the TrainingBuilder
     */
    private void loadSelectableGames(Context context) {
        Resources resources = context.getResources();
        mSelectableGames = new Training("TrainingGames");
        mSelectableGames.add(new UniversalDescription("Universal"));
        mSelectableGames.add(new LightsDescription(resources.getString(R.string.game_lights)));
        mSelectableGames.add(new PongDescription(resources.getString(R.string.game_pong), 1));
        mSelectableGames.add(new TunnelDescription("Tunnel", 1));
    }

    /**
     * Freeplay games
     */
    private void addFreeplayGames(Context context) {
        Resources resources = context.getResources();

        TunnelDescription settingsTunnel = new TunnelDescription("Tunnel", 1);
        settingsTunnel.setShowLives(false);
        mFreeplayGames.add(settingsTunnel);

        PongDescription settingsPong = new PongDescription(resources.getString(R.string.game_pong),
                1);
        settingsPong.setShowLives(false);
        mFreeplayGames.add(settingsPong);

        // MazeSettings settingsMaze = new
        // MazeSettings(resources.getString(R.string.game_maze));
        // mFreeplayGames.add(settingsMaze);

        LightsDescription settingsLights = new LightsDescription(
                resources.getString(R.string.game_lights));
        mFreeplayGames.add(settingsLights);

        // UniversalSettings settingsUniversal = new UniversalSettings(
        // resources.getString(R.string.game_universal));
        // mFreeplayGames.add(settingsUniversal);

        Universal3DDescription settingsUniversal3D = new Universal3DDescription("Virtual Reality",
                R.drawable.photoshpere_island2);
        mFreeplayGames.add(settingsUniversal3D);

        // SensorSettings settingsSensor = new SensorSettings("Sensor Test");
        // mFreeplayGames.add(settingsSensor);
    }

    /**
     * Balancetraining
     */
    private void addBalanceTraining(Context context) {

        Training balanceTraining = new Training("Balancetraining");
        // Lotrecht
        UniversalDescription settingsLotrecht = new UniversalDescription("Lotrecht");
        settingsLotrecht.addTarget(new Target(0.5f, 0.5f, 0.05f, 6000));
        settingsLotrecht
                .setInstructions("Begib dich in die lotrechte Position (stehe kerzengerade)");
        balanceTraining.add(settingsLotrecht);

        // Lotrecht 2 Achsen, Augenbinde
        UniversalDescription settingsLotrecht2 = new UniversalDescription("Lotrecht 2 Achsen");
        settingsLotrecht2.addTarget(new Target(0.5f, 0.5f, 0.05f, 6000));
        settingsLotrecht2.setInstructions("Begib dich in die lotrechte Position");
        balanceTraining.add(settingsLotrecht2);

        // Sagittal
        UniversalDescription settingsSagittal = new UniversalDescription("Sagittal");
        for (int i = 0; i < 5; i++) {
            settingsSagittal.addTarget(new Target(0.5f, 0.75f, 0.1f));
            settingsSagittal.addTarget(new Target(0.5f, 0.25f, 0.1f));
        }
        settingsSagittal
                .setInstructions("Bewege dich um die Sagittalachse (nach vorne und hinten)");
        balanceTraining.add(settingsSagittal);

        // Bauch- und Rueckenlage
        UniversalDescription settingsBauchRueck = new UniversalDescription("Bauch- und Rückenlage");
        settingsBauchRueck.addTarget(new Target(0.5f, 0.8f, 0.07f, 6000));
        settingsBauchRueck.addTarget(new Target(0.5f, 0.2f, 0.07f, 6000));
        settingsBauchRueck
                .setInstructions("Begib dich in die Bauchlage und dann in die Rückenlage");
        balanceTraining.add(settingsBauchRueck);

        // Abschluss
        UniversalDescription settingsAchsen = new UniversalDescription("Achsen");
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
        balanceTraining.add(settingsAchsen);

        // Viereckiger Grundrahmen
        UniversalDescription settingsViereck = new UniversalDescription("Viereck");
        for (int i = 0; i < 4; i++) {
            settingsViereck.addTarget(new Target(0.25f, 0.25f, 0.1f));
            settingsViereck.addTarget(new Target(0.75f, 0.25f, 0.1f));
            settingsViereck.addTarget(new Target(0.75f, 0.75f, 0.1f));
            settingsViereck.addTarget(new Target(0.25f, 0.75f, 0.1f));
        }
        settingsViereck.setInstructions("Bewege dich um den viereckigen Grundrahmen");
        balanceTraining.add(settingsViereck);

        // Achterkreis
        UniversalDescription settingsAcht = new UniversalDescription("Acht");
        for (int i = 0; i < 4; i++) {
            settingsAcht.addTarget(new Target(0.5f, 0.5f, 0.1f));
            settingsAcht.addTarget(new Target(0.25f, 0.3f, 0.1f));
            settingsAcht.addTarget(new Target(0.5f, 0.2f, 0.1f));
            settingsAcht.addTarget(new Target(0.75f, 0.3f, 0.1f));
            settingsAcht.addTarget(new Target(0.5f, 0.5f, 0.1f));
            settingsAcht.addTarget(new Target(0.25f, 0.7f, 0.1f));
            settingsAcht.addTarget(new Target(0.5f, 0.8f, 0.1f));
            settingsAcht.addTarget(new Target(0.75f, 0.7f, 0.1f));
        }
        settingsAcht.setInstructions("Bewege dich in einer 8");
        balanceTraining.add(settingsAcht);

        mTrainings.add(balanceTraining);
    }

    /**
     * Grobkoordination
     */
    private void addCoordTraining(Context context) {
        Resources resources = context.getResources();

        Training grobKoordinationsTraining = new Training("Grobkoordination");
        // Bremsen Handgriffe
        LightsDescription settingsBremsen = new LightsDescription("Abbremsen");
        settingsBremsen
                .setInstructions("Um den Balken zu füllen, bewege dich in der grünen Phase, und stoppe in der roten Phase\n\nHalte dich dabei an den Handgriffe fest");
        grobKoordinationsTraining.add(settingsBremsen);

        // Bremsen Handgriffe
        LightsDescription settingsBremsen2 = new LightsDescription("Abbremsen");
        settingsBremsen2
                .setInstructions("Um den Balken zu füllen, bewege dich in der grünen Phase, und stoppe in der roten Phase\n\nDiemsal ohne Handgriffe");
        grobKoordinationsTraining.add(settingsBremsen2);

        // Rolle
        UniversalDescription settingsRolle = new UniversalDescription("Rolle");
        settingsRolle.addTarget(new Target(0.5f, 0.05f, 0.15f));
        settingsRolle.addTarget(new Target(0.5f, 0.95f, 0.15f));
        settingsRolle.setInstructions("Mache eine Vorwärts- oder Rückwärtsrolle");
        grobKoordinationsTraining.add(settingsRolle);

        mTrainings.add(grobKoordinationsTraining);

        // Viereckiger Grundrahmen
        Training training0 = new Training("Viereck");
        UniversalDescription settingsUniversal = new UniversalDescription("Viereck");
        settingsUniversal.addTarget(new Target(0.25f, 0.25f, 0.1f));
        settingsUniversal.addTarget(new Target(0.75f, 0.25f, 0.15f));
        settingsUniversal.addTarget(new Target(0.75f, 0.75f, 0.2f));
        settingsUniversal.addTarget(new Target(0.25f, 0.75f));
        settingsUniversal.setTitle(resources.getString(R.string.game_universal));
        training0.add(settingsUniversal);

        // UniversalSettings settingsUniversal2 = new
        // UniversalSettings("Viereck");
        // settingsUniversal2.addPath(new Path(0.0f, 1.0f, 1.0f, 0.0f, 0.1f,
        // 200));
        // settingsUniversal2.setTitle(resources.getString(R.string.game_universal));
        // training0.add(settingsUniversal2);

        mTrainings.add(training0);
    }

    /**
     * Balancetraining + Games
     */
    private void addMixedTraining(Context context) {
        Training balanceTraining2 = new Training("Balancetraining");
        // Lotrecht
        UniversalDescription settingsLotrecht = new UniversalDescription("Lotrecht");
        settingsLotrecht.addTarget(new Target(0.5f, 0.5f, 0.05f, 6000));
        settingsLotrecht.setInstructions("Halte deine Position lotrecht (stehe kerzengerade)");
        balanceTraining2.add(settingsLotrecht);

        // Sagittal
        UniversalDescription settingsSagittal = new UniversalDescription("Sagittal");
        for (int i = 0; i < 5; i++) {
            settingsSagittal.addTarget(new Target(0.5f, 0.75f, 0.1f));
            settingsSagittal.addTarget(new Target(0.5f, 0.25f, 0.1f));
        }
        settingsSagittal
                .setInstructions("Bewege dich nach vorne und hinten");
        balanceTraining2.add(settingsSagittal);

        // Bauch- und Rueckenlage
        UniversalDescription settingsBauchRueck = new UniversalDescription("Bauchlage");
        settingsBauchRueck.addTarget(new Target(0.5f, 0.9f, 0.07f, 6000));
        settingsBauchRueck.addTarget(new Target(0.5f, 0.1f, 0.07f, 6000));
        settingsBauchRueck
                .setInstructions("Halte deine Position in der Bauchlage und dann in der Rückenlage");
        balanceTraining2.add(settingsBauchRueck);

        // Tunnel
        TunnelDescription settingsTunnel = new TunnelDescription("Tunnel", 3);
        settingsTunnel
                .setInstructions("Verlagere dein Gewicht, damit die Rakete nicht an den Rand stößt, und versuche eine möglichst weite Strecke zu fliegen");
        balanceTraining2.add(settingsTunnel);

        // Abschluss
        UniversalDescription settingsAchsen = new UniversalDescription("Achsen");
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
        settingsAchsen.setInstructions("Bewege zu den dargestellten Punkten");
        balanceTraining2.add(settingsAchsen);

        // Viereckiger Grundrahmen
        UniversalDescription settingsViereck = new UniversalDescription("Viereck");
        for (int i = 0; i < 4; i++) {
            settingsViereck.addTarget(new Target(0.4f, 0.4f, 0.1f));
            settingsViereck.addTarget(new Target(0.6f, 0.4f, 0.1f));
            settingsViereck.addTarget(new Target(0.6f, 0.6f, 0.1f));
            settingsViereck.addTarget(new Target(0.4f, 0.6f, 0.1f));
        }
        settingsViereck.setInstructions("Bewege dich im Viereck");
        balanceTraining2.add(settingsViereck);

        // Pong
        PongDescription settingsPong = new PongDescription("Pong", 3);
        settingsPong.setInstructions("Versuche den Ball möglichst oft zu blocken.");
        balanceTraining2.add(settingsPong);

        // Abbremsen
        LightsDescription settingsBremsen = new LightsDescription("Abbremsen");
        settingsBremsen
                .setInstructions("Um den Balken zu füllen, bewege dich in der grünen Phase, und stoppe in der roten Phase\n\nHalte dich dabei an den Handgriffen fest");
        balanceTraining2.add(settingsBremsen);

        mTrainings.add(balanceTraining2);
    }

    /**
     * Random training
     */
    private void loadRandomTraining(Context context, String title) {
        Random random = new Random();
        Training trainingLoop = new Training(title);

        for (int j = 1; j <= 10; j++) {
            UniversalDescription settingsRandom = new UniversalDescription("Zufallsübung "
                    + Integer.toString(j));

            int nrOfTargets = random.nextInt(3) + 2;
            for (int k = 1; k <= nrOfTargets; k++) {
                boolean resetIfLeft = random.nextBoolean();
                float targetPositionX = random.nextFloat();
                float targetPositionY = random.nextFloat();
                float targetRadius = random.nextFloat() / 15.0f + 0.04f;
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

    public List<Training> getTrainings() {
        return mTrainings;
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

    public boolean isControlInversed() {
        return mControlInversed;
    }

    public Training getSelectableGames() {
        return mSelectableGames;
    }
}
