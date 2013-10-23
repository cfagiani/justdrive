package net.crfsol.justdrive.fragments;

import android.os.Bundle;
import android.preference.PreferenceFragment;
import net.crfsol.justdrive.R;

/**
 * @author Christopher Fagiani
 */
public class SettingsFragment extends PreferenceFragment {

    public static final String ENABLED_KEY = "service_enabled_preference";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
    }
}
