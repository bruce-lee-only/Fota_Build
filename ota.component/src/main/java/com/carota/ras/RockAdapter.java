/*******************************************************************************
 * Copyright (C) 2018-2020 CAROTA Technology Crop. <www.carota.ai>.
 * All Rights Reserved.
 *
 * Unauthorized using, copying, distributing and modifying of this file,
 * via any medium is strictly prohibited.
 *
 * Proprietary and confidential.
 ******************************************************************************/

package com.carota.ras;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;

import com.carota.agent.AgentState;
import com.carota.agent.CacheRemoteAgent;
import com.carota.agent.RemoteAgent;
import com.momock.util.FileHelper;
import com.momock.util.Logger;

import java.io.File;
import java.util.concurrent.atomic.AtomicInteger;

public class RockAdapter extends CacheRemoteAgent {

    private final static String UA_RESULT = "result";
    private final static String WORK_DIR_CACHE = "/cache/rock/";
    private final boolean mLegacy;

    public RockAdapter(Context context, String name, File dir, boolean legacy) {
        super(context, name, dir);
        mLegacy = legacy;
    }

    @Override
    public String getSoftwareVersion(Bundle bomInfo) {
        return null;
    }

    @Override
    public String getHardwareVersion(Bundle bomInfo) {
        return null;
    }

    @Override
    public String getSerialNumber(Bundle bomInfo) {
        return null;
    }

    @Override
    public AgentState queryInstallResult(String ecuName) {
        int ret = RemoteAgent.INSTALL_ERROR_UNKNOWN;
        try {
            String sRt = FileHelper.readText(findResultFile());
            if(null != sRt && sRt.equals("0")) {
                ret = RemoteAgent.INSTALL_SUCCESS;
            }
        } catch (Exception e) {
            Logger.error(e);
        }
        // WARN: only set when upgrade is finished
        return new AgentState(ret, 0);
    }

    @Override
    public File packageLogFiles(String type, Bundle extra) {
        return null;
    }

    private File findResultFile() {
        File retFile = new File(WORK_DIR_CACHE + UA_RESULT);
        if (!retFile.exists()) {
            retFile = new File(getContext().getFilesDir(), UA_RESULT);
            if(!retFile.exists()) {
                return null;
            }
        }
        return retFile;
    }

    @Override
    public boolean installUpgradePackage(String descriptor, File file, Bundle extra, boolean isTriggered) {
        if(isTriggered) {
            return true;
        }
        // clear last install result
        File retFile = findResultFile();
        if(null != retFile) {
            retFile.delete();
        }

        try {
            // Wait 2s to make sure all files have been written
            Thread.sleep(2000);

            setUpgradeTriggered(descriptor);

            Logger.info("<<<<<<<<< RUA TRIGGER >>>>>>>>>>>");
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                // compatible old version
                File target = new File(getContext().getFilesDir(), "update");
                target.delete();

                if (file.renameTo(target)) {
                    Logger.info("<<<<<<<<< ROTA REBOOT (COMPATIBLE) >>>>>>>>>>>");
                    PowerManager pm = (PowerManager) getContext().getSystemService(Context.POWER_SERVICE);
                    pm.reboot("recovery");
                }
            } else if (mLegacy) {
                Logger.info("<<<<<<<<< GOTA REBOOT (LEGACY) >>>>>>>>>>>");
                com.carota.ras.util.RecoverySystem.installPackage(getContext(), file);
            } else {
                Logger.info("<<<<<<<<< GOTA REBOOT >>>>>>>>>>>");
                android.os.RecoverySystem.installPackage(getContext(), file);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    // For Android M or later,
                    // RecoverySystem.installPackage is Running in another thread,
                    // So you need to wait here.
                    // And we add timeout mechanism here to prevent failure in RecoverySystem.installPackage,
                    long timeout = extra.getLong("timeout");
                    do {
                        timeout -= 5 * 1000;
                        Thread.sleep(5 * 1000);
                    } while (timeout > 0);
                }
            }
        } catch (Exception e) {
            Logger.error(e);
        }
        Logger.info("<<<<<<<<< RUA TRIGGER ERROR >>>>>>>>>>>");
        return false;
    }
}
