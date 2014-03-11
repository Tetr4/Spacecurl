
package de.klimek.spacecurl.game;

import java.util.ArrayList;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.Display;
import android.view.Surface;
import android.view.WindowManager;
import de.klimek.spacecurl.util.collection.Database;

/**
 * Prototype Class for GameFragments. <br>
 * Extend Class
 * 
 * @author Mike Klimek
 */
public abstract class GameFragment extends Fragment implements SensorEventListener {
    public static final String TAG = "GameFragment"; // Used for log output
    public static final String ARG_CONTROL_INVERSE = "ARG_CONTROL_INVERSE";

    private Database mDatabase = Database.getInstance();
    // Members for Sensor
    private SensorManager mSensorManager;
    private float[] mRotationVector = new float[5];
    private float[] mRotation = new float[3];
    private float mRotationspeed = 0.0f;
    private float[] mRotationMatrix = new float[16];
    private volatile float[] mResultRotationMatrix = new float[16];
    private volatile float[] mOrientation = new float[3];
    private volatile float[] mOrientationScaled = new float[3];

    private boolean mInverseControls = mDatabase.isControlInversed();
    private float mRollMultiplier = mDatabase.getRollMultiplier();
    private float mPitchMultiplier = mDatabase.getPitchMultiplier();
    private float mPhoneInclinationRadian = mDatabase.getPhoneInclination() * (float) Math.PI / 180;

    private GameSettings mSettings;

    private boolean mLandscape;
    private boolean mViewCreated = false;
    private boolean mPaused = true;
    private ArrayList<GameCallBackListener> mListeners = new ArrayList<GameCallBackListener>();

    public static enum FreeAxisCount {
        Zero, One, Two, Three
    }

    public static enum Effect {
        Accuracy, Speed, Strength, Endurance
    }

    // Empty constructor required for fragment subclasses
    public GameFragment() {

    }

    public final void onPauseGame() {
        mPaused = true;
        if (mViewCreated) {
            doPauseGame();
        }
    }

    public final void onResumeGame() {
        mPaused = false;
        if (mViewCreated) {
            doResumeGame();
        }
    }

    protected abstract void doPauseGame();

    protected abstract void doResumeGame();

    public abstract FreeAxisCount getFreeAxisCount();

    public abstract Effect[] getEffects();

    private boolean isUsingSensor() {
        return getFreeAxisCount() != FreeAxisCount.Zero;
    }

    public void registerGameCallBackListener(GameCallBackListener listener) {
        mListeners.add(listener);
    }

    public boolean notifyFinished(String highScore) {
        boolean handled = false;
        for (GameCallBackListener curListener : mListeners) {
            curListener.onGameFinished(highScore);
            handled = true;
        }
        return handled;
    }

    public void notifyStatusChanged(float status) {
        for (GameCallBackListener curListener : mListeners) {
            curListener.onStatusChanged(status);
        }
    }

    /**
     * WARNING: may gimbal lock
     * 
     * @return azimuth, pitch and roll
     */
    public float[] getScaledOrientation() {
        return mOrientationScaled;
    }

    public float[] getOrientation() {
        return mOrientation;
    }

    public float[] getRotationMatrix() {
        return mRotationMatrix;
    }

    public float getRotationSpeed() {
        return mRotationspeed;

    }

    public boolean hasOrientation() {
        // TODO maybe set boolean in onAccuracyChanged?
        return !(getScaledOrientation()[1] == 0.0f);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        // TODO maybe onActivitycreated instead
        if (mInverseControls) {
            mRollMultiplier *= -1;
            mPitchMultiplier *= -1;
        }
        Display display = ((WindowManager) getActivity().getSystemService(Context.WINDOW_SERVICE))
                .getDefaultDisplay();
        mLandscape = (display.getRotation() == Surface.ROTATION_90);
        super.onCreate(savedInstanceState);
        // SharedPreferences sharedPref =
        // PreferenceManager.getDefaultSharedPreferences(getActivity());
        // mLandscape = sharedPref.getBoolean("landscape", false);
        if (isUsingSensor()) {
            // Setup Sensormanager
            mSensorManager = (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);
        }
    }

    @Override
    public void onViewStateRestored(Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        mViewCreated = true;
        if (mPaused) {
            doPauseGame();
        } else {
            doResumeGame();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (isUsingSensor()) {
            // Unbind all sensors
            mSensorManager.unregisterListener(this);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (isUsingSensor()) {
            Sensor gSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
            Sensor rvSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
            mSensorManager.registerListener(this, gSensor, SensorManager.SENSOR_DELAY_GAME);
            mSensorManager.registerListener(this, rvSensor, SensorManager.SENSOR_DELAY_GAME);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        mRollMultiplier = mDatabase.getRollMultiplier();
        mPitchMultiplier = mDatabase.getPitchMultiplier();
        mPhoneInclinationRadian = mDatabase.getPhoneInclination() * (float) Math.PI / 180;

        switch (event.sensor.getType()) {
            case Sensor.TYPE_ROTATION_VECTOR:
                mRotationVector[0] = event.values[0];
                mRotationVector[1] = event.values[1];
                mRotationVector[2] = event.values[2];
                mRotationVector[3] = event.values[3];
                mRotationVector[4] = event.values[4];

                // get rotationMatrix
                SensorManager.getRotationMatrixFromVector(mRotationMatrix, mRotationVector);

                // remap rotationMatrix depending on landscape or portrait mode
                if (mLandscape) { // FIXME
                    SensorManager.remapCoordinateSystem(mRotationMatrix,
                            SensorManager.AXIS_Y, SensorManager.AXIS_MINUS_X,
                            mResultRotationMatrix);
                } else {
                    SensorManager.remapCoordinateSystem(mRotationMatrix,
                            SensorManager.AXIS_X, SensorManager.AXIS_Z,
                            mResultRotationMatrix);
                    // System.arraycopy(mRotationMatrix, 0,
                    // mResultRotationMatrix, 0, mRotationMatrix.length);
                }

                // TODO inclination

                /*
                 * get matrix for azimuth, pitch, roll: All values are 0 when
                 * the phone is upright (NOT flat on a table), and facing north.
                 * Pitch increases when tilting the phone forward to a maximum
                 * of PI/2 when horizontal. Roll increased when tilting the
                 * phone rightward to a maximum of PI when horizontal.
                 */
                SensorManager.getOrientation(mResultRotationMatrix, mOrientation);
                // Add inclination of the phone in its holder
                mOrientation[1] = mOrientation[1] - mPhoneInclinationRadian;
                mOrientation[2] = -mOrientation[2]; //

                // Scaling azimuth, pitch, roll from -1.0f to 1.0f
                mOrientationScaled[0] = mOrientation[0] / (float) Math.PI;
                mOrientationScaled[1] = ((mOrientation[1]) * mPitchMultiplier)
                        / ((float) Math.PI / 2.0f);
                mOrientationScaled[2] = ((mOrientation[2]) * mRollMultiplier)
                        / ((float) Math.PI / 2.0f);
                // cutoff
                mOrientationScaled[1] = Math.min(1.0f, Math.max(mOrientationScaled[1], -1.0f));
                mOrientationScaled[2] = Math.min(1.0f, Math.max(mOrientationScaled[2], -1.0f));
                break;

            case Sensor.TYPE_GYROSCOPE:
                mRotation[0] = event.values[0];
                mRotation[1] = event.values[1];
                mRotation[2] = event.values[2];
                // TODO add multipliers
                mRotationspeed = (float) Math.sqrt(mRotation[0] * mRotation[0]
                        + mRotation[1] * mRotation[1]
                        + mRotation[2] * mRotation[2]);
                break;
            default:
                return;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    public void setSettings(GameSettings settings) {
        mSettings = settings;
    }

    protected GameSettings getSettings() {
        return mSettings;
    }

}
