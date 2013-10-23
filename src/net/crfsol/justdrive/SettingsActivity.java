package net.crfsol.justdrive;

import android.R;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import net.crfsol.justdrive.fragments.SettingsFragment;
import net.crfsol.justdrive.service.DetectionService;


public class SettingsActivity extends Activity {
    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getFragmentManager().beginTransaction().replace(R.id.content, new SettingsFragment()).commit();
    }
}
