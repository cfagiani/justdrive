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
    private static String THREAD_NAME = "recognitionThread";
    private static String TOGGLE_KEY = "xxTOGGLExx";
    private static String COUNT_KEY = "xxCOUNTxx";
    private static int CONFIDENCE_THRESHOLD = 70;
    private static int MIN_COUNT = 3;

    public ActivityRecognitionIntentService() {
        super(THREAD_NAME);
    }


    @Override
    protected void onHandleIntent(Intent intent) {
        // If the incoming intent contains an update
        if (ActivityRecognitionResult.hasResult(intent)) {
            // Get the update
            ActivityRecognitionResult result =
                    ActivityRecognitionResult.extractResult(intent);

            DetectedActivity detectedActivity = result.getMostProbableActivity();
            if (isTravelling(detectedActivity)) {
                Log.d("Activity Recognition", "Travelling");
                if (DeviceStateUtil.isPowerConnected(this)) {
                    //if we have power, turn everything on
                    //GPS doesn't work reliably
                    //DeviceStateUtil.setGPSEnabled(this, true);
                }
                writeTogglePreference(true);
                writeCounterPreference(0);
                DeviceStateUtil.setBluetoothEnabled(true);

                DeviceStateUtil.setCarModeEnabled(this, true);

            } else if (notInVehicle(detectedActivity)) {
                Log.d("Activity Recognition", "Not travelling");
                //if we're not travelling and we are unplugged
                if (!DeviceStateUtil.isPowerConnected(this)) {
                    //only turn things off if we turned them on
                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
                    if (prefs.getBoolean(TOGGLE_KEY, false)) {
                        if (prefs.getInt(COUNT_KEY, 0) > MIN_COUNT) {
                            //GPS doesn't work reliably
                            //DeviceStateUtil.setGPSEnabled(this, false);
                            DeviceStateUtil.setBluetoothEnabled(false);
                            DeviceStateUtil.setCarModeEnabled(this, false);
                            writeTogglePreference(false);
                            writeCounterPreference(0);
                        } else {
                            writeCounterPreference(prefs.getInt(COUNT_KEY, 0) + 1);
                        }
                    }
                }
            } else {
                Log.d("Activity Recognition", "No action. Type was " + detectedActivity.getType() + " with conf of " + detectedActivity.getConfidence());
            }

        } else {
            Log.w("Activity Recognition", "No result from detection service");
        }

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

    private boolean isTravelling(DetectedActivity activity) {
        return ((activity.getType() == DetectedActivity.IN_VEHICLE || activity.getType() == DetectedActivity.ON_BICYCLE) && activity.getConfidence() > CONFIDENCE_THRESHOLD);
    }

    private boolean notInVehicle(DetectedActivity activity) {
        return ((activity.getType() != DetectedActivity.IN_VEHICLE && activity.getType() != DetectedActivity.ON_BICYCLE) && activity.getConfidence() > CONFIDENCE_THRESHOLD);
    }

}
