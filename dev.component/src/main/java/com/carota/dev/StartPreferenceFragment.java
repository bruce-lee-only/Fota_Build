package com.carota.dev;

import android.os.Bundle;

import androidx.preference.PreferenceFragmentCompat;

public class StartPreferenceFragment extends PreferenceFragmentCompat {
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.pref_start);
    }
}
