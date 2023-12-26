package com.carota.dev.mda;

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;

import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.carota.build.ParamMDA;
import com.carota.core.UpgradeOffline;
import com.carota.core.data.UpdateSession;
import com.carota.core.remote.ActionMDA;
import com.carota.core.remote.IActionMDA;
import com.carota.dev.R;
import com.carota.dev.ServiceHelper;
import com.carota.util.ConfigHelper;
import com.momock.util.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MasterPreferenceFragment extends PreferenceFragmentCompat implements ServiceConnection, Preference.OnPreferenceClickListener {
    private IActionMDA mSktMaster;
    private IActionMDA mRpcMaster;

    // final String mSoftVer = "v1.0.0";
    final String mSoftVer = "SX7_02_P01.04";
    final String mHardVer = "2.0.0";
    private UpdateSession mCurSession;
    private final static String CHECK_SOCKET = "check_socket";
    private final static String CHECK_HTTP = "check_http";
    private final static String TEST_RPC = "test_rpc";

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.pref_master);
        //ServiceHelper.startService(this);
        ServiceHelper.bindService(getContext(), this);
        new UpgradeOffline(getContext());
        ParamMDA param = ConfigHelper.get(getContext()).get(ParamMDA.class);
        mRpcMaster = new ActionMDA(param.getHost(), 0);
        Log.e("=iis=", param.findDownloadManagerName("iis"));
        Log.e("=ivi=", param.findDownloadManagerName("ivi"));
        Log.e("=null=", param.findDownloadManagerName(null));

        findPreference(CHECK_SOCKET).setOnPreferenceClickListener(this);
        findPreference(CHECK_HTTP).setOnPreferenceClickListener(this);
        findPreference(TEST_RPC).setOnPreferenceClickListener(this);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        getActivity().unbindService(this);
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        Logger.error("####MasterActivity onServiceConnected");
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        Logger.error("####MasterActivity onServiceDisconnected");
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        Bundle bundle = new Bundle();
        switch (preference.getKey()) {
            case CHECK_SOCKET:
                bundle.putString("hu_sn", "1rpc1component");
                //mCurSession = mRpcMaster.connect(bundle);
                break;
            case CHECK_HTTP:
                break;
            case TEST_RPC:
                AsyncTask.execute(new Runnable() {
                    @Override
                    public void run() {
                        List<String> lost = new ArrayList<>();
                        Logger.error("Test Alive " + mRpcMaster.checkAlive());
                        Logger.error("Test System @ " + mRpcMaster.checkSystemReady(lost));
                        Logger.error("Test System Lost " + Arrays.toString(lost.toArray()));
                    }
                });
        }
        return true;
    }
}
