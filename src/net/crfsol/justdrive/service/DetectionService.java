package net.crfsol.justdrive.service;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.ActivityRecognitionClient;
import net.crfsol.justdrive.fragments.SettingsFragment;

/**
 * @author Christopher Fagiani
 */
public class DetectionService extends Service implements GooglePlayServicesClient.ConnectionCallbacks, GooglePlayServicesClient.OnConnectionFailedListener, SharedPreferences.OnSharedPreferenceChangeListener {


    public static final int DETECTION_INTERVAL_MILLISECONDS = 1000 * 120;

    private PendingIntent mActivityRecognitionPendingIntent;

    private ActivityRecognitionClient mActivityRecognitionClient;
    private boolean mInProgress;

    public enum REQUEST_TYPE {START, STOP}

    private REQUEST_TYPE mRequestType;

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(SettingsFragment.ENABLED_KEY)) {
            if (isServiceEnabled()) {
                startUpdates();
            } else {
                Log.d("Activity Recognition", "Stopping service");
                stopUpdates();
                stopSelf();
            }
        }
    }

    public class LocalBinder extends Binder {
        DetectionService getService() {
            return DetectionService.this;
        }
    }

    private final IBinder binder = new LocalBinder();

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("Activity Recognition", "Starting updates");

        if (isServiceEnabled()) {
            startUpdates();
        }
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        PreferenceManager.getDefaultSharedPreferences(this).unregisterOnSharedPreferenceChangeListener(this);
        stopUpdates();
        super.onDestroy();
    }

    private boolean isServiceEnabled() {
        return PreferenceManager.getDefaultSharedPreferences(this).getBoolean(SettingsFragment.ENABLED_KEY, true);
    }

    @Override
    public void onCreate() {

        PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(this);
        mActivityRecognitionClient =
                new ActivityRecognitionClient(this, this, this);
        /*
         * Create the PendingIntent that Location Services uses
         * to send activity recognition updates back to this app.
         */
        Intent intent = new Intent(
                this, ActivityRecognitionIntentService.class);
        /*
         * Return a PendingIntent that starts the IntentService.
         */
        mActivityRecognitionPendingIntent =
                PendingIntent.getService(this, 0, intent,
                        PendingIntent.FLAG_UPDATE_CURRENT);
    }

    /*
     * Called by Location Services once the location client is connected.
     *
     * Continue by requesting activity updates.
     */
    @Override
    public void onConnected(Bundle dataBundle) {
        switch (mRequestType) {
            case START:
                mActivityRecognitionClient.requestActivityUpdates(
                        DETECTION_INTERVAL_MILLISECONDS,
                        mActivityRecognitionPendingIntent);
                break;
            case STOP:
                mActivityRecognitionClient.removeActivityUpdates(mActivityRecognitionPendingIntent);
        }
        /*
         * Since the preceding call is synchronous, turn off the
         * in progress flag and disconnect the client
         */
        mInProgress = false;
        mActivityRecognitionClient.disconnect();
    }

    /**
     * Request activity recognition updates based on the current
     * detection interval.
     */
    public void startUpdates() {
        Log.i("Activity Recognition", "Starting recognition updates");
        mRequestType = REQUEST_TYPE.START;
        if (!servicesConnected()) {
            return;
        }
        // If a request is not already underway
        if (!mInProgress) {
            // Indicate that a request is in progress
            mInProgress = true;
            // Request a connection to Location Services
            mActivityRecognitionClient.connect();
            Log.d("Activity Recognition", "Recognition client connected");
        } else {
            /*
             * A request is already underway. You can handle
             * this situation by disconnecting the client,
             * re-setting the flag, and then re-trying the
             * request.
             */
        }
    }

    public void stopUpdates() {
        // Set the request type to STOP
        mRequestType = REQUEST_TYPE.STOP;
        Log.i("Activity Recognition", "Stoping recognition updates");
        /*
         * Test for Google Play services after setting the request type.
         * If Google Play services isn't present, the request can be
         * restarted.
         */
        if (!servicesConnected()) {
            return;
        }
        // If a request is not already underway
        if (!mInProgress) {
            // Indicate that a request is in progress
            mInProgress = true;
            // Request a connection to Location Services
            mActivityRecognitionClient.connect();
            Log.d("Activity Recognition", "Recognition client connected for stop message");
            //
        } else {

        }

    }


    private boolean servicesConnected() {
        // Check that Google Play services is available
        int resultCode =
                GooglePlayServicesUtil.
                        isGooglePlayServicesAvailable(this);
        // If Google Play services is available
        if (ConnectionResult.SUCCESS == resultCode) {
            // In debug mode, log the status
            Log.d("Activity Recognition",
                    "Google Play services is available.");
            // Continue
            return true;
            // Google Play services was not available for some reason
        } else {
            Log.d("Activity Recognition",
                    "Google Play services not available.");
            return false;
        }
    }

    @Override
    public void onDisconnected() {
        // Turn off the request flag
        mInProgress = false;
        // Delete the client
        mActivityRecognitionClient = null;
    }

    public void onConnectionFailed(ConnectionResult connectionResult) {
        // Turn off the request flag
        mInProgress = false;
        Log.e("Activity Recognition", "Connection failed");

    }
}
