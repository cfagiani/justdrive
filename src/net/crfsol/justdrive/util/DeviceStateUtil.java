package net.crfsol.justdrive.util;

import android.app.UiModeManager;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.BatteryManager;
import android.provider.Settings;

/**
 * @author Christopher Fagiani
 */
public class DeviceStateUtil {

    public static boolean isPowerConnected(Context context) {
        Intent intent = context.registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        int plugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
        return plugged == BatteryManager.BATTERY_PLUGGED_AC || plugged == BatteryManager.BATTERY_PLUGGED_USB;
    }


    public static void setBluetoothEnabled(boolean enabled) {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (!enabled && bluetoothAdapter.isEnabled()) {
            bluetoothAdapter.disable();
        } else if (enabled && !bluetoothAdapter.isEnabled()) {
            bluetoothAdapter.enable();
        }
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

    public static void setCarModeEnabled(Context context, boolean enabled) {
        UiModeManager uiManager = (UiModeManager) context.getSystemService(Context.UI_MODE_SERVICE);
        if (enabled && uiManager.getCurrentModeType() != Configuration.UI_MODE_TYPE_CAR) {
            uiManager.enableCarMode(UiModeManager.MODE_NIGHT_AUTO | UiModeManager.ENABLE_CAR_MODE_GO_CAR_HOME);
        } else if (!enabled && uiManager.getCurrentModeType() == Configuration.UI_MODE_TYPE_DESK) {
            uiManager.disableCarMode(UiModeManager.DISABLE_CAR_MODE_GO_HOME);
        }
    }

    private static void sendToggle(Context context, String position) {
        final Intent toggle = new Intent();
        toggle.setClassName("com.android.settings", "com.android.settings.widget.SettingsAppWidgetProvider");
        toggle.addCategory(Intent.CATEGORY_ALTERNATIVE);
        toggle.setData(Uri.parse(position));
        context.sendBroadcast(toggle);
    }


}
