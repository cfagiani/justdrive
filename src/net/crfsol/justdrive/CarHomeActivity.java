package net.crfsol.justdrive;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.speech.RecognizerIntent;
import android.view.View;
import android.view.WindowManager;
import net.crfsol.justdrive.service.DetectionService;

/**
 * home activity that acts as the car home screen. This will display even when the display is locked.
 *
 * @author Christopher Fagiani
 */
public class CarHomeActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PreferenceManager.setDefaultValues(this, net.crfsol.justdrive.R.xml.preferences, false);
        startService(new Intent(this, DetectionService.class));
        setContentView(R.layout.main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
    }

    public void launchSettings(View view) {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

    public void launchVoiceAction(View view) {
        startActivityForResult(new Intent(RecognizerIntent.ACTION_WEB_SEARCH), 999);
    }
}
