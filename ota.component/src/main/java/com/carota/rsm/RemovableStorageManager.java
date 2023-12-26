/*******************************************************************************
 * Copyright (C) 2018-2020 CAROTA Technology Crop. <www.carota.ai>.
 * All Rights Reserved.
 *
 * Unauthorized using, copying, distributing and modifying of this file,
 * via any medium is strictly prohibited.
 *
 * Proprietary and confidential.
 ******************************************************************************/

package com.carota.rsm;

import com.carota.util.MimeType;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

public class RemovableStorageManager {

    private Map<String, FileWrapper> mFileIndex;
    private static RemovableStorageManager sManager = new RemovableStorageManager();

    public static RemovableStorageManager get() {
        return sManager;
    }

    private RemovableStorageManager() {
        mFileIndex = new HashMap<>();
    }

    public void reset() {
        mFileIndex.clear();
    }

    protected boolean addFile(String id, File file, String type, String checksum, String desc) {
        if(null != id && null != file && file.exists()) {
            mFileIndex.put(id, new FileWrapper(file, type, checksum, desc));
            return true;
        }
        return false;
    }

    public boolean addZipFile(File file, String checksum, String targetInZip) {
        if(null != targetInZip) {
            return addFile(checksum, file, MimeType.ZIP, checksum, targetInZip);
        } else {
            return addFile(checksum, file, MimeType.STREAM, checksum, null);
        }
    }

    public boolean addXorFile(File file, String checksum, String key) {
        return addFile(checksum, file, MimeType.XOR, checksum, key);
    }

    public InputStream findFileById(String id, AtomicLong size) throws IOException {
        if(null != id) {
            FileWrapper fw = mFileIndex.get(id);
            if(null != fw) {
                return fw.createStream(size);
            }
        }
        return null;
    }

    public File findFileById(String id) {
        if(null != id) {
            FileWrapper fw = mFileIndex.get(id);
            if(null != fw && !fw.TYPE.equals(MimeType.XOR)) {
                return fw.TARGET;
            }
        }
        return null;
    }

}
