package net.crfsol.justdrive.service;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;
import net.crfsol.justdrive.util.DeviceStateUtil;

/**
 * @author Christopher Fagiani
 */
public class ActivityRecognitionIntentService extends IntentService {
    public static final String ACTION_EXTRA = "action";
    public static final String TERMINATE_ACTION = "terminate";
    public static final String SUSPEND_ACTION = "suspend";
    private static final String THREAD_NAME = "recognitionThread";
    private static final String TOGGLE_KEY = "xxTOGGLExx";
    private static final String SUSPEND_KEY = "xxSUSPENDxx";
    private static final String COUNT_KEY = "xxCOUNTxx";
    private static final int CONFIDENCE_THRESHOLD = 75;
    private static final long DEACTIVATION_INTERVAL = 1800L * 1000L; //30 minutes
    private static final int MIN_COUNT = 4;

    public ActivityRecognitionIntentService() {
        super(THREAD_NAME);
    }


    @Override
    protected void onHandleIntent(Intent intent) {
        // If the incoming intent contains an update
        if (intent.hasExtra(ACTION_EXTRA)) {
            if (TERMINATE_ACTION.equals(intent.getStringExtra(ACTION_EXTRA))) {
                deactivateCarMode();
            } else if (SUSPEND_ACTION.equalsIgnoreCase(intent.getStringExtra(ACTION_EXTRA))) {
                deactivateCarMode();
                writeSuspendPreference(System.currentTimeMillis());
            } else {
                Log.w("Activity Recognition", ACTION_EXTRA + " had unknown action: " + intent.getStringExtra(ACTION_EXTRA));
            }

        } else if (ActivityRecognitionResult.hasResult(intent)) {
            // Get the update
            ActivityRecognitionResult result =
                    ActivityRecognitionResult.extractResult(intent);

            DetectedActivity detectedActivity = result.getMostProbableActivity();
            if (isTravelling(detectedActivity)) {
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
                long suspendTime = prefs.getLong(SUSPEND_KEY, 0L);
                if (suspendTime > 0 && ((System.currentTimeMillis() - suspendTime) > DEACTIVATION_INTERVAL)) {
                    Log.d("Activity Recognition", "Travelling");
                    if (DeviceStateUtil.isPowerConnected(this)) {
                        //if we have power, turn everything on
                        //GPS doesn't work reliably
                        //DeviceStateUtil.setGPSEnabled(this, true);
                    }
                    if (DeviceStateUtil.setBluetoothEnabled(true)) {
                        writeTogglePreference(true);
                    }
                    writeCounterPreference(0);

                    //always reactivate car mode so the home screen returns to the forefront in the event that a prior voice action launched a new activity
                    //can't always enable without exiting navigation
                    DeviceStateUtil.setCarModeEnabled(this, true, !DeviceStateUtil.isMapsInForeground(this));
                }

            } else if (notInVehicle(detectedActivity)) {
                Log.d("Activity Recognition", "Not travelling");
                //if we're not travelling and we are unplugged
                if (!DeviceStateUtil.isPowerConnected(this)) {
                    //only turn things off if we turned them on
                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
                    if (prefs.getBoolean(TOGGLE_KEY, false)) {
                        if (prefs.getInt(COUNT_KEY, 0) > MIN_COUNT) {
                            deactivateCarMode();
                        } else {
                            writeCounterPreference(prefs.getInt(COUNT_KEY, 0) + 1);
                        }
                    }
                }
            } else {
                Log.d("Activity Recognition", "No action. Type was " + detectedActivity.getType() + " with conf of " + detectedActivity.getConfidence());
            }

        } else {
            Log.w("Activity Recognition", "Unclear intent");
        }
    }

    /**
     * turn off the things we turned on for car mode
     */
    private void deactivateCarMode() {
        //GPS doesn't work reliably
        //DeviceStateUtil.setGPSEnabled(this, false);
        DeviceStateUtil.setBluetoothEnabled(false);
        DeviceStateUtil.setCarModeEnabled(this, false, false);
        writeTogglePreference(false);
        writeCounterPreference(0);
    }


    private void writeTogglePreference(boolean val) {
        SharedPreferences.Editor prefEditor = PreferenceManager.getDefaultSharedPreferences(this).edit();
        prefEditor.putBoolean(TOGGLE_KEY, val);
        prefEditor.commit();
    }

    private void writeCounterPreference(int count) {
        SharedPreferences.Editor prefEditor = PreferenceManager.getDefaultSharedPreferences(this).edit();
        prefEditor.putInt(COUNT_KEY, count);
        prefEditor.commit();
    }

    private void writeSuspendPreference(long val) {
        SharedPreferences.Editor prefEditor = PreferenceManager.getDefaultSharedPreferences(this).edit();
        prefEditor.putLong(SUSPEND_KEY, val);
        prefEditor.commit();
    }

    private boolean isTravelling(DetectedActivity activity) {
        return ((activity.getType() == DetectedActivity.IN_VEHICLE || activity.getType() == DetectedActivity.ON_BICYCLE) && activity.getConfidence() > CONFIDENCE_THRESHOLD);
    }

    private boolean notInVehicle(DetectedActivity activity) {
        return ((activity.getType() != DetectedActivity.IN_VEHICLE && activity.getType() != DetectedActivity.ON_BICYCLE) && activity.getConfidence() > CONFIDENCE_THRESHOLD);
    }

}
