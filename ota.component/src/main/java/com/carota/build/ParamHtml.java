package com.carota.build;

import android.content.Context;
import android.text.TextUtils;

import com.momock.util.FileHelper;

import java.io.File;

public class ParamHtml extends ConfigParser {
    private String mDir;

    /**
     * <node id="html" enabled="true">
     * <dir>/sdcard/xxx</dir>
     * </node>
     */

    public ParamHtml() {
        super("html");
    }

    @Override
    protected void set(String tag, String name, String val, boolean enabled) {
        if ("dir".equals(tag)) {
            mDir = val;
        }
    }

    public File getDownloadDir(Context context) {
        File ret = context.getFilesDir();
        if (!TextUtils.isEmpty(mDir)) {
            ret = new File(mDir);
        }
        FileHelper.mkdir(ret);
        return ret;
    }
}
