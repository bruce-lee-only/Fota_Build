package com.carota.util.exec;

import android.os.Bundle;

import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.carota.dev.R;

public class ExecPreferenceFragment extends PreferenceFragmentCompat implements Preference.OnPreferenceClickListener {
    private final static String EXEC_A = "exec_a";
    private final static String EXEC_B = "exec_b";
    private final static String EXEC_C = "exec_c";
    private final static String EXEC_D = "exec_d";
    private final static String EXEC_E = "exec_e";

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.pref_exec);
        findPreference(EXEC_A).setOnPreferenceClickListener(this);
        findPreference(EXEC_B).setOnPreferenceClickListener(this);
        findPreference(EXEC_C).setOnPreferenceClickListener(this);
        findPreference(EXEC_D).setOnPreferenceClickListener(this);
        findPreference(EXEC_E).setOnPreferenceClickListener(this);
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        switch (preference.getKey()) {
            case EXEC_A:
                new ExecCase().launchCaseA();
                break;
            case EXEC_B:
                new ExecCase().launchCaseB();
                break;
            case EXEC_C:
                new ExecCase().launchCaseC();
                break;
            case EXEC_D:
                new ExecCase().launchCaseD();
                break;
            case EXEC_E:
                new ExecCase().launchCaseE();
                break;
        }
        return false;
    }

}
