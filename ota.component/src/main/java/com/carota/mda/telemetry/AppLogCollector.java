package com.carota.mda.telemetry;

import android.content.Context;
import android.text.TextUtils;

import com.carota.build.ParamRoute;
import com.carota.mda.remote.ActionSDA;
import com.carota.mda.remote.IActionSDA;
import com.carota.sda.util.RemoteAgentCallback;
import com.carota.sync.uploader.AppLogUploader;
import com.carota.sync.util.MaterialCollector;
import com.carota.util.ConfigHelper;
import com.momock.util.FileHelper;
import com.momock.util.Logger;

import java.io.File;
import java.util.HashMap;


public class AppLogCollector extends MaterialCollector<AppLogUploader> {

    private Context mContext;
    private File mWorkDir;
    private AppLogFileChunk mAppLogFileChunk;
    private HashMap<String, String> paths;

    public AppLogCollector(Context context, File workDir, AppLogUploader uploader, AppLogFileChunk appLogFileChunk) {
        super(uploader, 3);
        mContext = context.getApplicationContext();
        mWorkDir = workDir;
        mAppLogFileChunk = appLogFileChunk;
    }

    @Override
    protected boolean doProcess(int stepIndex, CampaignStatus status) {
        mAppLogFileChunk.updateState(stepIndex);
        boolean ret = true;
        switch (stepIndex) {
            case 0:
                // clear last
                FileHelper.cleanDir(mWorkDir);
                break;
            case 1:
                // Download Target Files
                prepareTargetFiles();
                break;
            case 2:
                ret = getUploader().LogFile(status.getToken(), mWorkDir, mAppLogFileChunk.queryType());
                break;
        }
        return ret;
    }

    @Override
    protected void onFinishWork(CampaignStatus status) {
        FileHelper.cleanDir(mWorkDir);
    }

    private void prepareTargetFiles() {
        IActionSDA actionSDA = new ActionSDA();
        ParamRoute paramRoute = ConfigHelper.get(mContext).get(ParamRoute.class);
        for (ParamRoute.Info info : paramRoute.listInfo(ParamRoute.Info.PATH_ETH)) {
            downloadEcuLog(info.ID, info.getHost(ParamRoute.Info.PATH_ETH), null, actionSDA);
        }
    }

    private boolean downloadEcuLog(String ecu, String host, File localSrcDir, IActionSDA actionSDA) {
        File tempFile = new File(mWorkDir, ecu + "_temp");
        FileHelper.delete(tempFile);

        File target = new File(mWorkDir, ecu);
        if (target.exists()) {
            return true;
        }

        try {
            if (null != localSrcDir) {
                // Use Local path first
                FileHelper.copyDir(localSrcDir, tempFile);
            } else if (TextUtils.isEmpty(host)) {
                return false;
            } else if (!actionSDA.collectLogFiles(host, 0, tempFile, paths.get(ecu))) {
                return false;
            }
            return tempFile.renameTo(target);
        } finally {
            // clear temp
            FileHelper.cleanDir(RemoteAgentCallback.getLogCacheDir(mContext, ecu));
            FileHelper.delete(tempFile);
        }
    }

    public void setPaths(HashMap<String, String> paths) {
        this.paths = paths;
    }
}
