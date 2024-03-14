package com.carota.dm.file.local;

import com.carota.dm.file.app.AppFileManager;
import com.momock.util.FileHelper;
import com.momock.util.Logger;
import com.momock.util.SystemHelper;

import java.io.File;

/**
 * 默认文件管理器
 * 一般配置在andriod本地，使用File进行管理
 */
public class LocalFileManager extends AppFileManager {
    private final static String TAG = "Local";

    public LocalFileManager(File downloadDir) {
        super(downloadDir,TAG);
    }

    @Override
    public void init() {
        prepareWorkDir();
    }

    /**
     * 提升DM目录权限
     */
    private void prepareWorkDir() {
        FileHelper.mkdir(mWorkDir);
//        SystemHelper.execScript("chmod -R 777 " + mWorkDir.getAbsolutePath());
    }

}
