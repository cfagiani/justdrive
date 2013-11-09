package net.crfsol.justdrive.util;

import android.app.ActivityManager;
import android.app.UiModeManager;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.BatteryManager;
import android.provider.Settings;

import java.util.List;

/**
 * @author Christopher Fagiani
 */
public class DeviceStateUtil {

    private static final String MAPS_PACKAGE = "com.google.android.apps.maps";

    public static boolean isPowerConnected(Context context) {
        Intent intent = context.registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        int plugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
        return plugged == BatteryManager.BATTERY_PLUGGED_AC || plugged == BatteryManager.BATTERY_PLUGGED_USB;
    }


    /**
     * enables/disables bluetooth. Will return true if the status was toggled, false if no change was made (due to adapter already being in requested state)
     *
     * @param enabled
     * @return
     */
    public static boolean setBluetoothEnabled(boolean enabled) {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (!enabled && bluetoothAdapter.isEnabled()) {
            bluetoothAdapter.disable();
            return true;
        } else if (enabled && !bluetoothAdapter.isEnabled()) {
            bluetoothAdapter.enable();
            return true;
        }
        return false;
    }

    public static void setGPSEnabled(Context context, boolean enabled) {
        Intent intent = new Intent("android.location.GPS_ENABLED_CHANGE");
        intent.putExtra("enabled", true);
        context.sendBroadcast(intent);

        String provider = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
        if (enabled && !provider.contains("gps")) {
            //if gps is disabled and the caller wants it on
            sendToggle(context, "3");
        } else if (!enabled && provider.contains("gps")) {
            //if gps is enabled and the caller wants it off
            sendToggle(context, "3");
        }
    }

    /**
     * toggles car mode
     *
     * @param context
     * @param enable
     * @param alwaysActivate
     */
    public static void setCarModeEnabled(Context context, boolean enable, boolean alwaysActivate) {
        UiModeManager uiManager = (UiModeManager) context.getSystemService(Context.UI_MODE_SERVICE);
        if (alwaysActivate || (enable && uiManager.getCurrentModeType() != Configuration.UI_MODE_TYPE_CAR)) {
            uiManager.enableCarMode(UiModeManager.MODE_NIGHT_AUTO | UiModeManager.ENABLE_CAR_MODE_GO_CAR_HOME);
        } else if (!enable && uiManager.getCurrentModeType() == Configuration.UI_MODE_TYPE_CAR) {
            uiManager.disableCarMode(UiModeManager.DISABLE_CAR_MODE_GO_HOME);
        }
    }

    /**
     * somewhat hackish method of determining if Google Maps is in the foreground.
     * What we really want to do is detect if navigation is enabled but there is no API for that.
     *
     * @param context
     * @return
     */
    public static boolean isMapsInForeground(Context context) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> services = activityManager
                .getRunningTasks(Integer.MAX_VALUE);
        return MAPS_PACKAGE.equals(services.get(0).topActivity.getPackageName());
    }

    /**
     * toggles a setting in the widget provider. This is a hack to bypass GPS activation restrictions and only works on older versions of Android.
     *
     * @param context
     * @param position
     */
    private static void sendToggle(Context context, String position) {
        final Intent toggle = new Intent();
        toggle.setClassName("com.android.settings", "com.android.settings.widget.SettingsAppWidgetProvider");
        toggle.addCategory(Intent.CATEGORY_ALTERNATIVE);
        toggle.setData(Uri.parse(position));
        context.sendBroadcast(toggle);
    }


}
