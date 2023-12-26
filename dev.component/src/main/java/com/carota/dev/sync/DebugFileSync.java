/*******************************************************************************
 * Copyright (C) 2018-2020 CAROTA Technology Crop. <www.carota.ai>.
 * All Rights Reserved.
 *
 * Unauthorized using, copying, distributing and modifying of this file,
 * via any medium is strictly prohibited.
 *
 * Proprietary and confidential.
 ******************************************************************************/

package com.carota.dev.sync;

import com.carota.sync.base.FileDataLogger;
import com.momock.util.EncryptHelper;
import com.momock.util.FileHelper;
import com.momock.util.JsonDatabase;
import com.momock.util.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class DebugFileSync extends FileDataLogger {

    private final String TAG;
    private final File mOutputDir;
    private List<File> mPartList;

    public DebugFileSync(JsonDatabase.Collection col, String tag, File cacheDir, File outCache) {
        super(col, tag, 1024, cacheDir);
        TAG = tag;
        mOutputDir = outCache;
        FileHelper.cleanDir(mOutputDir);
        mPartList = new ArrayList<>();
    }

    public void setFile(String id, File file) {
        recordFileData(id, file, null);
    }

    @Override
    protected boolean send(FileMeta meta, String md5, InputStream body) {
        if(0 == meta.getBlockIndex()) {
            mPartList.clear();
        }
//        throw new RuntimeException("DEBUG");
        File file = new File(mOutputDir,
                TAG + "@" + meta.getBlockIndex() + "-" + meta.getBlockNum());
        Logger.error("ON SEND : " + file.getAbsolutePath());
        mPartList.add(file);
        FileHelper.copy(body, file);
        String checksum = EncryptHelper.calcFileMd5(file);
        return checksum.equals(md5);
    }

    public void assembleFile(File out) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(out)) {
            for (File f : mPartList) {
                try (InputStream is = new FileInputStream(f)) {
                    FileHelper.copy(is, fos);
                }
            }
        }
    }
}
