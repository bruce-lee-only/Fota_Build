/*******************************************************************************
 * Copyright (C) 2018-2020 CAROTA Technology Crop. <www.carota.ai>.
 * All Rights Reserved.
 *
 * Unauthorized using, copying, distributing and modifying of this file,
 * via any medium is strictly prohibited.
 *
 * Proprietary and confidential.
 ******************************************************************************/

package com.carota.build;

import android.content.Context;
import android.os.Environment;
import android.text.TextUtils;

import com.momock.util.FileHelper;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ParamDTC extends ConfigParser {

    /**
     *  <node id="dtc">
     *      <dir>FilterLog</dir>
     *      <task>/pivot/ota/v0/measure/scripts</task>
     *      <upload>/pivot/ota/v0/measure/logs</upload>
     * </node>
     */

    private final Map<String, String> mExtra;

    public ParamDTC() {
        super("dtc");
        mExtra = new HashMap<>();
    }

    @Override
    protected void set(String tag, String name, String val, boolean enabled) {
        mExtra.put(tag, val);
    }

    public String getTaskUrl() {
        return mockUrl(mExtra.get("task"));
    }

    public String getUploadUrl() {
        return mockUrl(mExtra.get("upload"));
    }


    public File getWorkDir(Context context) {
        String path = mExtra.get("dir");
        File ret;
        if(TextUtils.isEmpty(path) || !Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            ret = new File(context.getFilesDir(), "dtc");
        } else {
            ret = new File(Environment.getExternalStorageDirectory(), path);
        }
        FileHelper.mkdir(ret);
        return ret;
    }
}
