
package de.klimek.spacecurl.game;

import java.util.Arrays;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import de.klimek.spacecurl.util.collection.Status;

/**
 * Prototype Class for GameFragments. <br>
 * Extend Class
 * 
 * @author Mike Klimek
 */
public abstract class GameFragment extends Fragment implements SensorEventListener {
    public static final String TAG = "GameFragment"; // Used for log output
    public static final int DEFAULT_TITLE_RESOURCE_ID = -1;
    public static final String ARG_TITLE = "ARG_TITLE";

    private Status mStatus;

    // Members for Sensor
    private SensorManager mSensorManager;
    private float[] mGravityData = new float[3]; // Gravity or accelerometer
    private float[] mMagnetData = new float[3]; // Magnetometer
    private float[] mRotationMatrix = new float[9];
    private float[] mInclinationMatrix = new float[9];
    private float[] mResultRotationMatrix = new float[9];
    private float[] mOrientation = new float[3];
    private boolean mHasGrav = false;
    private boolean mHasAccel = false;
    private boolean mHasMag = false;

    public static enum FreeAxisCount {
        Zero, One, Two, Three
    }

    public static enum Effect {
        Accuracy, Speed, Strength, Endurance
    }

    // Empty constructor required for fragment subclasses
    public GameFragment() {

    }

    public abstract void pauseGame();

    public abstract void resumeGame();

    public abstract FreeAxisCount getFreeAxisCount();

    public abstract Effect[] getEffects();

    private boolean isUsingSensor() {
        return getFreeAxisCount() != FreeAxisCount.Zero;
    }

    public void setStatus(Status status) {
        mStatus = status;
    }

    protected Status getStatus() {
        return mStatus;
    }

    protected float[] getOrientationAxes() {
        synchronized (mResultRotationMatrix) {
            return mResultRotationMatrix;
        }
    }

    protected float[] getOrientationRotationMatrix() {
        synchronized (mResultRotationMatrix) {
            return mResultRotationMatrix;
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        // TODO maybe onActivitycreated instead
        super.onCreate(savedInstanceState);
        if (isUsingSensor()) {
            // Setup Sensormanager
            mSensorManager = (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (isUsingSensor()) {
            // Unbind Sensor
            mSensorManager.unregisterListener(this);
            Log.d(TAG, Arrays.toString(mRotationMatrix));

        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (isUsingSensor()) {
            // Bind Sensor
            Sensor gsensor = mSensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
            Sensor asensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            Sensor msensor = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
            mSensorManager.registerListener(this, gsensor, SensorManager.SENSOR_DELAY_GAME);
            mSensorManager.registerListener(this, asensor, SensorManager.SENSOR_DELAY_GAME);
            mSensorManager.registerListener(this, msensor, SensorManager.SENSOR_DELAY_GAME);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        switch (event.sensor.getType()) {
            case Sensor.TYPE_GRAVITY:
                mGravityData[0] = event.values[0];
                mGravityData[1] = event.values[1];
                mGravityData[2] = event.values[2];
                mHasGrav = true;
                break;
            case Sensor.TYPE_ACCELEROMETER:
                if (mHasGrav)
                    break; // don't need it, we have better
                mGravityData[0] = event.values[0];
                mGravityData[1] = event.values[1];
                mGravityData[2] = event.values[2];
                mHasAccel = true;
                break;
            case Sensor.TYPE_MAGNETIC_FIELD:
                mMagnetData[0] = event.values[0];
                mMagnetData[1] = event.values[1];
                mMagnetData[2] = event.values[2];
                mHasMag = true;
                break;
            default:
                return;
        }

        if ((mHasGrav || mHasAccel) && mHasMag) {
            SensorManager.getRotationMatrix(mRotationMatrix, mInclinationMatrix, mGravityData,
                    mMagnetData);
            synchronized (mResultRotationMatrix) {
                // TODO synchronized on cached array
                SensorManager.remapCoordinateSystem(mRotationMatrix,
                        SensorManager.AXIS_Y, SensorManager.AXIS_MINUS_X, mResultRotationMatrix);
            }
            // Orientation isn't as useful as a rotation matrix, but
            // we'll show it here anyway.
            final int DEG = 360;
            SensorManager.getOrientation(mResultRotationMatrix, mOrientation);
            float incl = SensorManager.getInclination(mInclinationMatrix);
            // Log.d(TAG, "Azimuth: " + (int) (mOrientation[0] * DEG));
            // Log.d(TAG, "pitch: " + (int) (mOrientation[1] * DEG));
            // Log.d(TAG, "roll: " + (int) (mOrientation[2] * DEG));
            // Log.d(TAG, "inclination: " + (int) (incl * DEG));
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

}
