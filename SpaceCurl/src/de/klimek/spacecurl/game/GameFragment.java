
package de.klimek.spacecurl.game;

import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import de.klimek.spacecurl.MainActivityPrototype.State;
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
    public static final String ARG_CONTROL_INVERSE = "ARG_CONTROL_INVERSE";
    public static final String ARG_CONTROL_FACTOR_X = "ARG_CONTROL_FAKTOR_X";
    public static final String ARG_CONTROL_FACTOR_Y = "ARG_CONTROL_FAKTOR_Y";

    // Members for Sensor
    private SensorManager mSensorManager;
    private float[] mGravityData = new float[3]; // Gravity or accelerometer
    private float[] mMagnetData = new float[3]; // Magnetometer
    private float[] mRotation = new float[3];
    private float mRotationspeed = 0.0f;
    private float[] mRotationMatrix = new float[9];
    private float[] mInclinationMatrix = new float[9];
    private volatile float[] mResultRotationMatrix = new float[9];
    private volatile float[] mOrientation = new float[3];
    private volatile float[] mOrientationScaled = new float[3];
    private boolean mHasGrav = false;
    private boolean mHasAccel = false;
    private boolean mHasMag = false;

    private boolean mInverseControls = false;
    private float mControlFactorX = -3.0f;
    private float mControlFactorY = -3.0f;
    private float mPhoneInclination = Database.getInstance().getPhoneInclination();

    private Status mStatus;

    private boolean mLandscape;
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

    public void setState(State running) {
        mState = running;
        if (mViewCreated) {
            switch (running) {
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

    protected float[] getOrientation() {
        return mOrientation;
    }

    protected float[] getRotationMatrix() {
        return mResultRotationMatrix;
    }

    protected float getRotationSpeed() {
        return mRotationspeed;

    }

    protected boolean hasOrientation() {
        return !(getScaledOrientation()[1] == 0.0f);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        // TODO maybe onActivitycreated instead
        mControlFactorX = getArguments().getFloat(ARG_CONTROL_FACTOR_X, mControlFactorX);
        mControlFactorY = getArguments().getFloat(ARG_CONTROL_FACTOR_Y, mControlFactorY);
        mInverseControls = getArguments().getBoolean(ARG_CONTROL_INVERSE, mInverseControls);
        if (mInverseControls) {
            mControlFactorX *= -1;
            mControlFactorY *= -1;
        }
        super.onCreate(savedInstanceState);
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        mLandscape = sharedPref.getBoolean("landscape", false);
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
            default:
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
        // TODO Quaternion from TYPE_ROTATION_VECTOR to prevent gimbal lock
        switch (event.sensor.getType()) {
            case Sensor.TYPE_GRAVITY:
                // inbuild sensorfusion (gyro drift eliminated by accelerometer)
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
                mRotationspeed = (float) Math.sqrt(mRotation[0] * mRotation[0]
                        + mRotation[1] * mRotation[1]
                        + mRotation[2] * mRotation[2]);
                // return Math.abs(mRotation[0]) + Math.abs(mRotation[1]) +
                // Math.abs(mRotation[2]);
                break;
            default:
                return;
        }

        if ((mHasGrav || mHasAccel) && mHasMag) {
            // calculate rotationMatrix from rotation and magnetometer data
            SensorManager.getRotationMatrix(mRotationMatrix, mInclinationMatrix, mGravityData,
                    mMagnetData);

            // remap rotationMatrix depending on landscape or portrait mode
            if (mLandscape) {
                SensorManager.remapCoordinateSystem(mRotationMatrix,
                        SensorManager.AXIS_Z, SensorManager.AXIS_MINUS_X,
                        mResultRotationMatrix);
            } else {
                // SensorManager.AXIS_Y, SensorManager.AXIS_MINUS_X,
                SensorManager.remapCoordinateSystem(mRotationMatrix,
                        SensorManager.AXIS_X, SensorManager.AXIS_Z, mResultRotationMatrix);
            }
            // TODO remap to spacecurl coordinate system
            // TODO add phoneInclination

            /*
             * get matrix for azimuth, pitch, roll: All values are 0 when the
             * phone is upright (NOT flat on a table), and facing north. Pitch
             * increases when tilting the phone forward to a maximum of PI/2
             * when horizontal. Roll increased when tilting the phone rightward
             * to a maximum of PI when horizontal.
             */
            SensorManager.getOrientation(mResultRotationMatrix, mOrientation);

            // Scaling azimuth, pitch, roll from -1.0f to 1.0f
            // Azimuth
            mOrientationScaled[0] = mOrientation[0] / (float) Math.PI;
            // Pitch
            mOrientationScaled[1] = (mOrientation[1] * mControlFactorX) / ((float) Math.PI / 2.0f);
            // Roll
            mOrientationScaled[2] = (mOrientation[2] * mControlFactorY) / ((float) Math.PI / 2.0f);

            // cutoff
            if (mOrientationScaled[1] > 1.0f)
                mOrientationScaled[1] = 1.0f;
            if (mOrientationScaled[1] < -1.0f)
                mOrientationScaled[1] = -1.0f;
            if (mOrientationScaled[2] > 1.0f)
                mOrientationScaled[2] = 1.0f;
            if (mOrientationScaled[2] < -1.0f)
                mOrientationScaled[2] = -1.0f;

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
