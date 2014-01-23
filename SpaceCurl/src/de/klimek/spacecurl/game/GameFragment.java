
package de.klimek.spacecurl.game;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import de.klimek.spacecurl.MainActivity.State;
import de.klimek.spacecurl.util.collection.Database;
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
    public static final String ARG_INVERSE_CONTROL = "ARG_INVERSE_CONTROL";

    // Members for Sensor
    private SensorManager mSensorManager;
    private float[] mGravityData = new float[3]; // Gravity or accelerometer
    private float[] mMagnetData = new float[3]; // Magnetometer
    private float[] mRotation = new float[3];
    private float[] mRotationMatrix = new float[9];
    private float[] mInclinationMatrix = new float[9];
    private float[] mResultRotationMatrix = new float[9];
    private float[] mOrientation = new float[3];
    private float[] mOrientationScaled = new float[3];
    private boolean mHasGrav = false;
    private boolean mHasAccel = false;
    private boolean mHasMag = false;
    private float mPhoneInclination = Database.getInstance().getPhoneInclination();

    private Status mStatus;

    private boolean mViewCreated = false;
    private State mState = State.Running;

    public static enum FreeAxisCount {
        Zero, One, Two, Three
    }

    public static enum Effect {
        Accuracy, Speed, Strength, Endurance
    }

    // Empty constructor required for fragment subclasses
    public GameFragment() {

    }

    public void setState(State state) {
        mState = state;
        if (mViewCreated) {
            switch (state) {
                case Paused:
                    pauseGame();
                    break;
                case Running:
                    resumeGame();
                    break;

                default:
                    break;
            }
        }
    }

    protected abstract void pauseGame();

    protected abstract void resumeGame();

    public abstract FreeAxisCount getFreeAxisCount();

    public abstract Effect[] getEffects();

    private boolean isUsingSensor() {
        return getFreeAxisCount() != FreeAxisCount.Zero;
    }

    protected void notifyFinished() {
        FragmentActivity activity = getActivity();
        if (activity instanceof GameCallBackListener) {
            ((GameCallBackListener) activity).onGameFinished();
        }
    }

    protected void notifyStatusChanged(Status status) {
        FragmentActivity activity = getActivity();
        if (activity instanceof GameCallBackListener) {
            ((GameCallBackListener) activity).onStatusChanged(status);
        }
    }

    /**
     * @return azimuth, pitch and roll
     */
    protected float[] getScaledOrientation() {
        return mOrientationScaled;
    }

    protected float[] getRotationMatrix() {
        return mResultRotationMatrix;
    }

    protected float getRotationSpeed() {
        return Math.abs(mRotation[0]) + Math.abs(mRotation[1]) + Math.abs(mRotation[2]);
    }

    protected boolean hasOrientation() {
        return !(getScaledOrientation()[1] == 0.0f);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        // TODO maybe onActivitycreated instead
        super.onActivityCreated(savedInstanceState);
        if (isUsingSensor()) {
            // Setup Sensormanager
            mSensorManager = (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);
        }
    }

    @Override
    public void onViewStateRestored(Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        mViewCreated = true;
        switch (mState) {
            case Paused:
                pauseGame();
                break;
            case Pausing:
                pauseGame();
                break;
            case Running:
                resumeGame();
                break;
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (isUsingSensor()) {
            // Unbind Sensor
            mSensorManager.unregisterListener(this);
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
            Sensor rsensor = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
            mSensorManager.registerListener(this, gsensor, SensorManager.SENSOR_DELAY_GAME);
            mSensorManager.registerListener(this, asensor, SensorManager.SENSOR_DELAY_GAME);
            mSensorManager.registerListener(this, msensor, SensorManager.SENSOR_DELAY_GAME);
            mSensorManager.registerListener(this, rsensor, SensorManager.SENSOR_DELAY_GAME);
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

            case Sensor.TYPE_GYROSCOPE:
                mRotation[0] = event.values[0];
                mRotation[1] = event.values[1];
                mRotation[2] = event.values[2];
                break;
            default:
                return;
        }

        if ((mHasGrav || mHasAccel) && mHasMag) {
            SensorManager.getRotationMatrix(mRotationMatrix, mInclinationMatrix, mGravityData,
                    mMagnetData);
            // TODO synchronized on cached array
            SensorManager.remapCoordinateSystem(mRotationMatrix,
                    SensorManager.AXIS_Y, SensorManager.AXIS_MINUS_X, mResultRotationMatrix);
            // Orientation isn't as useful as a rotation matrix, but
            // we'll show it here anyway.
            // final int DEG = 360;
            SensorManager.getOrientation(mResultRotationMatrix, mOrientation);

            // azimuth
            // TODO Adjust
            mOrientationScaled[1] = mOrientation[0];
            // Pitch
            mOrientationScaled[1] = mOrientation[1] / (float) Math.PI + 0.5f;
            // Roll adjusted
            mOrientationScaled[2] = (mOrientation[2] / (float) (Math.PI)) + mPhoneInclination;
            if (mOrientationScaled[2] <= 0.0f && mOrientationScaled[2] > -0.5f)
                mOrientationScaled[2] = 0.0f;
            if (mOrientationScaled[2] > 1 || mOrientationScaled[2] < -0.5f)
                mOrientationScaled[2] = 1.0f;

            // float incl = SensorManager.getInclination(mInclinationMatrix);
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
