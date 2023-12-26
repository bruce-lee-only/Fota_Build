package com.carota.dev.sync;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;

import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.carota.dev.R;
import com.carota.mda.telemetry.AppLogCollector;
import com.carota.mda.telemetry.FotaAnalytics;
import com.carota.sync.DataSyncManager;
import com.carota.sync.analytics.AppAnalytics;
import com.carota.sync.analytics.UpgradeAnalytics;
import com.carota.sync.uploader.AppLogUploader;
import com.momock.util.JsonDatabase;
import com.momock.util.Logger;

import java.io.File;

public class SyncPreferenceFragment extends PreferenceFragmentCompat implements Preference.OnPreferenceClickListener {
    JsonDatabase mJsonDatabase;
    JsonDatabase.Collection mFileCol;
    JsonDatabase.Collection mAppLogCol;
    private String sUrl = "http://api.carota.ai/v0/data";
    private String sUsid = "5f86b235e55b974e7dffc744";
    private String sUlid = "5f86b235e55b974e7dffc743";

    private final static String SYNC_FILE = "sync_file";
    private final static String UPLOAD_FILE = "upload_file";
    private final static String SYNC_LOG = "sync_log";
    private final static String SYNC_ANALYTICS = "sync_analytics";
    private final static String SYNC_EVENT_TRACKING = "sync_event_tracking";

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.pref_sync);
        mJsonDatabase = JsonDatabase.get(getContext(), "DEBUG_SYNC");
        mFileCol = mJsonDatabase.getCollection("DATA_FILE");
        mAppLogCol = mJsonDatabase.getCollection("LOG_FILE");
        Logger.open(getContext(),"carota-",20,Logger.LEVEL_ALL);

        findPreference(SYNC_FILE).setOnPreferenceClickListener(this);
        findPreference(UPLOAD_FILE).setOnPreferenceClickListener(this);
        findPreference(SYNC_LOG).setOnPreferenceClickListener(this);
        findPreference(SYNC_ANALYTICS).setOnPreferenceClickListener(this);
        findPreference(SYNC_EVENT_TRACKING).setOnPreferenceClickListener(this);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mJsonDatabase.forceClose();
    }

    public void onClickSyncFile() {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                File src = new File("/sdcard/carota");
                //File src = new File("/sdcard/Download/SyncSrc");
                File dst = new File("/sdcard/Download/SyncDst");

                DebugFileSync sync = new DebugFileSync(mFileCol, "SDF-A", getContext().getFilesDir(), getContext().getExternalCacheDir());
                sync.setFile("ReqCampaignID", src);

                try {
                    do {
                        Thread.sleep(1000);
                    } while (sync.isSyncing());
                    sync.assembleFile(dst);
                }catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void onClickSyncUploadFile() {
        File src = new File("/sdcard/carota/test.txt");
        AppLogUploader sync = new AppLogUploader(mAppLogCol, getContext().getExternalCacheDir(), sUrl);
        sync.LogFile(sUlid,src);
        if(!sync.isSyncing()) sync.syncData();
    }

    public void onClickAppLogSync() {
        File src = new File(getContext().getExternalCacheDir().getAbsolutePath());
        AppLogUploader sync = DataSyncManager.get(getContext()).getSync(AppLogUploader.class);
        AppLogCollector collector = new AppLogCollector(getContext(), src, sync);
        collector.active(sUlid);
    }

    public void onClickUpgradeAnalytics() {
        UpgradeAnalytics upgrade = DataSyncManager.get(getContext()).getSync(UpgradeAnalytics.class);
        AsyncTask.execute(new Runnable(){
            @Override
            public void run() {
                upgrade.logState(sUsid,"ivi", FotaAnalytics.OTA.STATE_DOWNLOADED,0);
                if(!upgrade.isSyncing()) upgrade.syncData();
                upgrade.logState(sUsid,"ivi",FotaAnalytics.OTA.STATE_MD5_SUCCESS,0);
                upgrade.logState(sUsid,"ivi",FotaAnalytics.OTA.STATE_PKI_SUCCESS,0);
            }
        });

    }

    public void onClickEventTrackingAnalytics() {
        AppAnalytics analy = DataSyncManager.get(getContext()).getSync(AppAnalytics.class);
        AsyncTask.execute(new Runnable(){
            @Override
            public void run() {
                ErrorCode.getInstance().addAppErrCodeBuried(analy,sUsid,0,null);
                if(!analy.isSyncing()) analy.syncData();
                ErrorCode.getInstance().addAppErrCodeBuried(analy,sUsid,2,null);
                ErrorCode.getInstance().addAppErrCodeBuried(analy,sUsid,4,null);
                ErrorCode.getInstance().addAppErrCodeBuried(analy,sUsid,6,null);
                ErrorCode.getInstance().addAppErrCodeBuried(analy,sUsid,7,null);
                ErrorCode.getInstance().addAppErrCodeBuried(analy,sUsid,9,null);
            }
        });
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        switch (preference.getKey()) {
            case SYNC_FILE:
                onClickSyncFile();
                break;
            case UPLOAD_FILE:
                onClickSyncUploadFile();
                break;
            case SYNC_LOG:
                onClickAppLogSync();
                break;
            case SYNC_ANALYTICS:
                onClickUpgradeAnalytics();
                break;
            case SYNC_EVENT_TRACKING:
                onClickEventTrackingAnalytics();
                break;
        }
        return true;
    }
}
