/*******************************************************************************
 * Copyright (C) 2018-2020 CAROTA Technology Crop. <www.carota.ai>.
 * All Rights Reserved.
 *
 * Unauthorized using, copying, distributing and modifying of this file,
 * via any medium is strictly prohibited.
 *
 * Proprietary and confidential.
 ******************************************************************************/

package com.carota.agent;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class CacheRemoteAgent extends RemoteAgent {

    private File mCacheDir;
    private File mAgentCache;
    private final String KEY_TARGET_ID = "tgt_id";

    /**
     * @param name   name of device(assign by OTA)
     * @param context
     */
    public CacheRemoteAgent(Context context, String name, File cacheDir) {
        super(context, name);
        mCacheDir = cacheDir;
        mAgentCache = new File(mCacheDir, "ra-cache-" + NAME);
        // Clear Last Cache Data
        onFinishUpgrade(null);
    }

    @Override
    protected final boolean triggerInstall(String descriptor,
                                     InputStream pkg, Bundle extra,
                                     boolean isTriggered) throws IOException {
        File target;
        // Check if file is in local dir
        String targetPath = extra.getString("tgt_path");
        if(!TextUtils.isEmpty(targetPath)) {
            target = new File(targetPath);
            if(target.exists()) {
                return installUpgradePackage(descriptor, target, extra, isTriggered);
            }
        }
        // Compatible with older versions
        // For single file upgrade, desc == file MD5
        String name = descriptor;
        if(!TextUtils.isEmpty(name)) {
            target = new File(mCacheDir, name);
            if(target.exists()) {
                return installUpgradePackage(descriptor, target, extra, isTriggered);
            }
        }
        // Copy file to local for upgrade
        target = mAgentCache;
        target.delete();

        FileOutputStream fos = new FileOutputStream(target);
        // here we use large buffer(64KB) to increase copy speed
        if(FileHelper.copy(pkg, fos, 64 * 1024)) {
            return installUpgradePackage(descriptor, target,  extra, isTriggered);
        }

        // This should never happen
        return false;
    }

    @Override
    protected final void onFinishUpgrade(Bundle extra) {
        mAgentCache.delete();
    }

    /**
     * Called by the OTA when tiggerUpgrade called.  Do not call this method directly.
     * Implement of Real Install function with follow steps
     * 1. upload file
     * 2. launch install progress
     * @param descriptor task ID
     * @param file  package file
     * @param extra   Reserved
     * @return  trigger result
     */
    @Deprecated
    public abstract boolean installUpgradePackage(String descriptor,
                                                  File file,Bundle extra,
                                                  boolean isTriggered);

}
