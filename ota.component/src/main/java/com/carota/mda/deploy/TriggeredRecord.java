/*******************************************************************************
 * Copyright (C) 2018-2020 CAROTA Technology Crop. <www.carota.ai>.
 * All Rights Reserved.
 *
 * Unauthorized using, copying, distributing and modifying of this file,
 * via any medium is strictly prohibited.
 *
 * Proprietary and confidential.
 ******************************************************************************/

package com.carota.mda.deploy;

import android.content.Context;

import com.momock.util.FileHelper;
import com.momock.util.Logger;

import java.io.File;
import java.io.IOException;

public class TriggeredRecord {

    private static TriggeredRecord mInstances = null;

    private File mCacheDir;

    private TriggeredRecord(Context context) {
        mCacheDir = new File(context.getFilesDir(), "trig");
        mCacheDir.mkdirs();
    }


    public static TriggeredRecord get(Context context) {
        synchronized (TriggeredRecord.class) {
            if(null == mInstances) {
                mInstances = new TriggeredRecord(context);
            }
        }
        return mInstances;
    }

    public void reset() {
        Logger.info("TR Clear ALL");
        FileHelper.delete(mCacheDir);
        mCacheDir.mkdirs();
    }

    private String getData(String name, String id) {
        return name + "_" + id;
    }

    public boolean setTriggered(String name, String id) {
        String data = getData(name, id);
        File flag = new File(mCacheDir, data);
        try {
            if(!flag.exists()) {
                com.carota.agent.FileHelper.writeTextImmediately(flag, data, null);
                return true;
            }
        } catch (IOException e) {
            Logger.error(e);
        }
        return false;
    }

    public boolean isTriggered(String name, String id) {
        return new File(mCacheDir, getData(name, id)).exists();
    }
}
