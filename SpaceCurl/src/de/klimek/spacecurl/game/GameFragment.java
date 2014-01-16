
package de.klimek.spacecurl.game;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.util.Log;
import de.klimek.spacecurl.util.collection.Status;
import de.klimek.spacecurl.util.sensor.SensorFilterListener;
import de.klimek.spacecurl.util.sensor.SensorFilterService;

/**
 * Prototype Class for GameFragments. <br>
 * Extend Class
 * 
 * @author Mike Klimek
 */
public abstract class GameFragment extends Fragment {
    public static final String TAG = "GameFragment"; // Used for log output
    public static final int TITLE_RESOURCE_ID = -1;
    public static final String ARG_TITLE = "ARG_TITLE";

    public static enum Effect {
        Accuracy, Speed, Strength, Endurance
    }

    // Members for the SensorFilter Service
    private ServiceConnection mConnection;
    private SensorFilterService mBoundSensorFilter;
    private boolean mIsBounded = false;
    // Members for Status
    private Status mStatus;

    // Empty constructor required for fragment subclasses
    public GameFragment() {

    }

    public void setStatus(Status status) {
        mStatus = status;
    }

    protected Status getStatus() {
        return mStatus;
    }

    public abstract void pauseGame();

    public abstract void resumeGame();

    public boolean isUsingSensor() {
        return this instanceof SensorFilterListener;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (isUsingSensor()) {
            setupSensorService();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (isUsingSensor()) {
            doUnbindService();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (isUsingSensor()) {
            doBindService();
        }
    }

    private void setupSensorService() {
        mConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                mBoundSensorFilter = ((SensorFilterService.LocalBinder) service).getService();
                mBoundSensorFilter.registerListener((SensorFilterListener) GameFragment.this);
                mIsBounded = true;
            }

            @Override
            public void onServiceDisconnected(ComponentName className) {
                mBoundSensorFilter = null;
                mIsBounded = false;
            }
        };
        Log.i(TAG, "Sensor set up");
    }

    private void doBindService() {
        getActivity().bindService(new Intent(getActivity(),
                SensorFilterService.class), mConnection, Context.BIND_AUTO_CREATE);
        mIsBounded = true;
        Log.v(TAG, "Sensor bound");
    }

    private void doUnbindService() {
        if (mIsBounded) {
            if (mBoundSensorFilter != null) {
                mBoundSensorFilter.unregisterListener((SensorFilterListener) this);
            }
            getActivity().unbindService(mConnection);
            mIsBounded = false;
            Log.v(TAG, "Sensor unbound");
        }
    }

}
