package com.carota.dev.core;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SeekBarPreference;

import com.carota.CarotaClient;
import com.carota.InstallToast;
import com.carota.build.ParamRAS;
import com.carota.core.ICheckCallback;
import com.carota.core.IDownloadCallback;
import com.carota.core.ISession;
import com.carota.dev.R;
import com.carota.dev.ServiceHelper;
import com.carota.util.ConfigHelper;
import com.momock.util.Logger;

import java.util.concurrent.ExecutionException;

public class CorePreferenceFragment extends PreferenceFragmentCompat implements IDownloadCallback, ICheckCallback, Preference.OnPreferenceClickListener {

    private final static String START_DOWNLOAD = "start_download";
    private final static String STOP_DOWNLOAD = "stop_download";
    private final static String START_CHECK = "start_check";
    private final static String DOWNLOAD_PROGRESS = "download_progress";
    private Preference stopPreference;
    private SeekBarPreference dwSeekBar;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.pref_core);
        stopPreference = findPreference(STOP_DOWNLOAD);
        dwSeekBar = findPreference(DOWNLOAD_PROGRESS);
        findPreference(START_DOWNLOAD).setOnPreferenceClickListener(this);
        stopPreference.setOnPreferenceClickListener(this);
        findPreference(START_CHECK).setOnPreferenceClickListener(this);

        ServiceHelper.startService(getContext());
        CarotaClient.init(getActivity(), new CarotaClient.IInstallViewHandlerFactory() {
            @Override
            public InstallToast create(Context context) {
                return new EcusInstall(getContext());
            }
        }, 10 * 60);
        ParamRAS param = ConfigHelper.get(getContext()).get(ParamRAS.class);
        for (ParamRAS.Info i : param.listInfo()) {
            Log.e("===PRAS====", "Type = " + i.getId() + "; Host = " + i.getHost() + "; Pkg = " + i.getPackage());
        }
    }

    Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message message) {
            int progress = (int) message.obj;
            switch (message.what) {
                case 1:
                    Logger.info("progress %d", progress);
                    dwSeekBar.setValue(progress);
                    dwSeekBar.setTitle(progress + "%");
                    break;
                case 2:
                    dwSeekBar.setValue(progress);
                    dwSeekBar.setTitle(progress + "%");
                default:
                    break;
            }
            return false;
        }
    });

    @Override
    public void onProcess(ISession s) {
        int count = s.getTaskCount();
        int progress = 0;
        int totalProgress = 0;
        for (int i = 0; i < count; i++) {
            totalProgress += (int) s.getTask(i).getDownloadProgress();
        }
        progress = (totalProgress / count);
        Message msg = new Message();
        msg.what = 1;
        msg.obj = progress;
        mHandler.sendMessage(msg);
    }

    @Override
    public void onFinished(ISession s, boolean success) {
        Logger.info(" success %b ", success);

        int count = s.getTaskCount();
        int progress = 0;
        int totalProgress = 0;
        Logger.info(" onFinished count %d", count);
        for (int i = 0; i < count; i++) {
            totalProgress += (int) s.getTask(i).getDownloadProgress();
        }
        progress = (totalProgress / count);
        Logger.info(" onFinished progress %d", progress);
        Message msg = new Message();
        msg.what = 2;
        msg.obj = progress;
        mHandler.sendMessage(msg);
    }

    public void startDownload() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    CarotaClient.startDownload(CorePreferenceFragment.this);
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public void stopDownload() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    CarotaClient.stopDownload();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public void startCheck() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    CarotaClient.check(null, CorePreferenceFragment.this);
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    @Override
    public void onConnected(final ISession s) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (null != s && s.getTaskCount() > 0) {
                    stopPreference.setEnabled(true);
                }
            }
        });
    }

    @Override
    public void onError(String error, int erCode) {

    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        switch (preference.getKey()) {
            case START_DOWNLOAD:
                startDownload();
                break;
            case STOP_DOWNLOAD:
                stopDownload();
                break;
            case START_CHECK:
                startCheck();
        }
        return true;
    }
}
