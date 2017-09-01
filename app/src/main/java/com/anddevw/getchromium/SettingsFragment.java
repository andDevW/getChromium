package com.anddevw.getchromium;

import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.support.v7.preference.PreferenceFragmentCompat;

/**
 * Created by devnet on 8/31/17.
 */

public class SettingsFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle bundle, String s) {
        // Load the preferences from the XML file
        addPreferencesFromResource(R.xml.app_preferences);
    }


}
