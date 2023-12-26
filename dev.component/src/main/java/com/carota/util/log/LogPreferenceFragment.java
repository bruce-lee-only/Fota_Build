package com.carota.util.log;

import android.os.Bundle;

import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.carota.dev.R;

public class LogPreferenceFragment extends PreferenceFragmentCompat implements Preference.OnPreferenceClickListener {
    private final static String CACHE_INSERT = "cache_insert";

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.pref_log);
        findPreference(CACHE_INSERT).setOnPreferenceClickListener(this);
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        switch (preference.getKey()) {
            case CACHE_INSERT:

        }
        return false;
    }
}
