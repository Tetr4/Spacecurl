
package de.klimek.spacecurl.util.sensor;

import java.util.ArrayList;
import java.util.List;

import android.app.Service;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Binder;
import android.os.IBinder;

/**
 * Usage:
 * <ol>
 * <li>Set up Sensor</li>
 * <li>???</li>
 * <li>Profit</li>
 * </ol>
 */
public class SensorFilterService extends Service implements SensorEventListener {
    final static String ARG_SENSOR = "sensor";
    @SuppressWarnings("unused")
    private static final String TAG = "SensorFilterService";
    private List<SensorFilterListener> mListeners = new ArrayList<SensorFilterListener>(); // Listeners
    private SensorManager mSensorManager;
    private final IBinder mBinder = new LocalBinder();

    private float[] mGravityData = new float[3]; // Gravity or accelerometer
    private float[] mMagnetData = new float[3]; // Magnetometer
    private float[] mRotationMatrix = new float[9];
    private float[] mInclinationMatrix = new float[9];
    private float[] mResultRotationMatrix = new float[9];
    // private float[] mOrientation = new float[3];
    private boolean mHasGrav = false;
    private boolean mHasAccel = false;
    private boolean mHasMag = false;

    // public SensorFilter() {}

    @Override
    public void onCreate() {
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        Sensor gsensor = mSensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
        Sensor asensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        Sensor msensor = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        mSensorManager.registerListener(this, gsensor, SensorManager.SENSOR_DELAY_GAME);
        mSensorManager.registerListener(this, asensor, SensorManager.SENSOR_DELAY_GAME);
        mSensorManager.registerListener(this, msensor, SensorManager.SENSOR_DELAY_GAME);
    }

    /**
     * Class for clients to access. Because we know this service always runs in
     * the same process as its clients, we don't need to deal with IPC.
     */
    public class LocalBinder extends Binder {
        public SensorFilterService getService() {
            return SensorFilterService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return true;
    }

    @Override
    public void onDestroy() {
        mSensorManager.unregisterListener(this);
        super.onDestroy();
    }

    public void registerListener(SensorFilterListener listener) {
        mListeners.add(listener);
    }

    public void unregisterListener(SensorFilterListener listener) {
        mListeners.remove(listener);
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
            SensorManager.remapCoordinateSystem(mRotationMatrix,
                    SensorManager.AXIS_Y, SensorManager.AXIS_MINUS_X, mResultRotationMatrix);
            // float[] rbla = {1,0,0,0,1,0,0,0,1};
            // float[] blubb = matrixMultiplication(rbla,mRotationMatrix2);
            // int[] blargh = new int[blubb.length];
            // for (int i = 0 ; i < blubb.length; i++)
            // {
            // blargh[i] = (int) (blubb[i]*100);
            // }
            // Log.d(MainActivity.TAG, "rbla:" + Arrays.toString(blargh));
            // Orientation isn't as useful as a rotation matrix, but
            // we'll show it here anyway.
            // int DEG=360;
            // SensorManager.getOrientation(mRotationMatrix2, mOrientation);
            // float incl = SensorManager.getInclination(mInclinationMatrix);
            // Log.d(MainActivity.TAG, "mh: " + (int)(mOrientation[0]*DEG));
            // Log.d(MainActivity.TAG, "pitch: " + (int)(mOrientation[1]*DEG));
            // Log.d(MainActivity.TAG, "roll: " + (int)(mOrientation[2]*DEG));
            // Log.d(MainActivity.TAG, "yaw: " + (int)(mOrientation[0]*DEG));
            // Log.d(MainActivity.TAG, "inclination: " + (int)(incl*DEG));
        }
        // Send update
        for (SensorFilterListener curListener : mListeners) {
            curListener.onSensorFilterChanged(mResultRotationMatrix);
        }
    }

    // private float[] matrixMultiplication(float[] A, float[] B) {
    // float[] result = new float[9];
    //
    // result[0] = A[0] * B[0] + A[1] * B[3] + A[2] * B[6];
    // result[1] = A[0] * B[1] + A[1] * B[4] + A[2] * B[7];
    // result[2] = A[0] * B[2] + A[1] * B[5] + A[2] * B[8];
    //
    // result[3] = A[3] * B[0] + A[4] * B[3] + A[5] * B[6];
    // result[4] = A[3] * B[1] + A[4] * B[4] + A[5] * B[7];
    // result[5] = A[3] * B[2] + A[4] * B[5] + A[5] * B[8];
    //
    // result[6] = A[6] * B[0] + A[7] * B[3] + A[8] * B[6];
    // result[7] = A[6] * B[1] + A[7] * B[4] + A[8] * B[7];
    // result[8] = A[6] * B[2] + A[7] * B[5] + A[8] * B[8];
    //
    // return result;
    // }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
