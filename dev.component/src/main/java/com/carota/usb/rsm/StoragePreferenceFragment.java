package com.carota.usb.rsm;

import android.os.Bundle;
import android.os.Environment;
import android.util.Log;

import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.carota.build.IConfiguration;
import com.carota.build.ParamHub;
import com.carota.build.ParamRSM;
import com.carota.dev.R;
import com.carota.svr.PrivReqHelper;
import com.carota.util.ConfigHelper;
import com.carota.util.MainServiceHolder;
import com.momock.util.EncryptHelper;
import com.momock.util.FileHelper;

import java.io.File;

public class StoragePreferenceFragment extends PreferenceFragmentCompat implements Preference.OnPreferenceClickListener {
    private static final String TAG = StoragePreferenceFragment.class.getName();
    private MainServiceHolder mHolder;
    private String mChecksum;
    private final static String ADD_FILE = "add_file";
    private final static String DOWNLOAD_FILE = "download_file";

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.pref_storage);
        findPreference(ADD_FILE).setOnPreferenceClickListener(this);
        findPreference(DOWNLOAD_FILE).setOnPreferenceClickListener(this);
        mHolder = new MainServiceHolder(getContext().getPackageName());
    }

    public void onClickAdd() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                File file = new File(Environment.getExternalStorageDirectory(), "Download/test.bin");
                Log.e(TAG, "Add path = " + file.getAbsolutePath());
                try {
                    IConfiguration cfg = ConfigHelper.get(getActivity().getApplicationContext());
                    ParamHub paramHub = cfg.get(ParamHub.class);
                    PrivReqHelper.setGlobalProxy(paramHub.getAddr(), paramHub.getPort());
                    mChecksum = mHolder.addFileSync(getActivity().getApplicationContext(), file, null);
                    Log.e(TAG, "Add Verify = " + mChecksum);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public void onClickFetch() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                IConfiguration cfg = ConfigHelper.get(getActivity().getApplicationContext());
                String host = cfg.get(ParamRSM.class).getHost();
                File target = new File(getActivity().getExternalCacheDir(), mChecksum);
                target.delete();
                FileHelper.mkdirForFile(target);
                String url = "http://" + host + "/file?id=" + mChecksum;
                Log.e(TAG, "URL = " + url);
                if(PrivReqHelper.doDownload(url, target)) {
                    if(mChecksum.equals(EncryptHelper.calcFileMd5(target))) {
                        Log.e(TAG, "Fetch Success");
                    } else {
                        Log.e(TAG, "Fetch Error");
                    }
                } else {
                    Log.e(TAG, "Fetch failure");
                }
            }
        }).start();
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        switch (preference.getKey()) {
            case ADD_FILE:
                onClickAdd();
                break;
            case DOWNLOAD_FILE:
                onClickFetch();
        }
        return true;
    }
}
