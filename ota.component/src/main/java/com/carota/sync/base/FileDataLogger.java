/*******************************************************************************
 * Copyright (C) 2018-2020 CAROTA Technology Crop. <www.carota.ai>.
 * All Rights Reserved.
 *
 * Unauthorized using, copying, distributing and modifying of this file,
 * via any medium is strictly prohibited.
 *
 * Proprietary and confidential.
 ******************************************************************************/

package com.carota.sync.base;

import android.text.TextUtils;

import com.momock.util.EncryptHelper;
import com.momock.util.FileHelper;
import com.momock.util.JsonDatabase;
import com.momock.util.Logger;
import com.momock.util.SubInputStream;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

public abstract class FileDataLogger extends DataLogger<FileDataLogger.FileMeta> {

    public static class FileMeta {

        /**
         "ulid":"dd"
         "md5":"dkldkfhsldfl"
         "path":"/sdcard/data"
         "block_size":512
         "block_number":1024
         "block_index":5
         */

        private final static String RECORD_REQ_ID = "rid";
        private final static String RECORD_CHECKSUM = "md5";
        private final static String RECORD_PATH = "path";
        private final static String RECORD_BLOCK_SIZE = "block_size";
        private final static String RECORD_BLOCK_NUM = "block_num";
        private final static String RECORD_BLOCK_INDEX = "block_index";
        private final static String RECORD_EXTRA = "extra";

        private JSONObject mRaw;

        private FileMeta(JSONObject raw) {
            mRaw = raw;
            if(null == mRaw) {
                mRaw = new JSONObject();
            }
        }

        public String getRequestId () {
            return mRaw.optString(RECORD_REQ_ID);
        }

        public File getFile() {
            return new File(mRaw.optString(RECORD_PATH));
        }

        public String getChecksum() {
            return mRaw.optString(RECORD_CHECKSUM);
        }

        public int getBlockSize() {
            return mRaw.optInt(RECORD_BLOCK_SIZE);
        }

        public int getBlockNum() {
            return mRaw.optInt(RECORD_BLOCK_NUM);
        }

        public int getBlockIndex() {
            return mRaw.optInt(RECORD_BLOCK_INDEX);
        }

        public String getExtra() {
            return mRaw.optString(RECORD_EXTRA);
        }

        FileMeta setBlockIndex(int index) {
            try {
                mRaw.put(RECORD_BLOCK_INDEX, index);
            } catch (JSONException e) {
                Logger.error(e);
            }
            return this;
        }
    }

    private final String mName;
    private final String mDataName;
    private final long mBlockSize;
    private final JsonDatabase.Collection mCollection;
    private final File mCacheDir;

    public FileDataLogger(JsonDatabase.Collection col, String tag, long blockSize, File cacheDir) {
        super();
        mCollection = col;
        mCollection.setCachable(true);
        mName =  "FDL@" + tag;
        mDataName = "DATA@" + mName;
        mBlockSize = blockSize;
        mCacheDir = cacheDir;
        Logger.debug("[SYNC-FDL] NAME = %s; BLOCK = %d; DIR = %s", mDataName, mBlockSize, cacheDir.getPath());
    }

    private FileMeta loadFileMeta() {
        return new FileMeta(mCollection.get(mName));
    }

    public String getRequestId() {
        return loadFileMeta().getRequestId();
    }

    public void setData(JSONObject param) {
        mCollection.set(mDataName, param);
    }

    public JSONObject getData() {
        return mCollection.get(mDataName);
    }

    protected synchronized boolean recordFileData(String rid, File data, String extra) {
        if(TextUtils.isEmpty(rid) || null == data || !data.exists()) {
            recordData(null, false);
            Logger.error("[SYNC-FDL] Record Er @ Call");
            return false;
        }

        File target = new File(mCacheDir, mName);

        FileMeta meta = loadFileMeta();
        String reqId = meta.getRequestId();
        String checksum = meta.getChecksum();
        if(reqId.equals(rid) && !checksum.isEmpty()
                && checksum.equals(EncryptHelper.calcFileMd5(target))) {
            Logger.debug("[SYNC-FDL] Record Er @ Exist");
            syncData();
            return true;
        }

        recordData(null, false);

        target.delete();
        if(data.isFile()) {
            FileHelper.copy(data, target);
        } else if(!FileHelper.zip(data, target)) {
            Logger.error("[SYNC-FDL] Record Er @ Pack");
            target.delete();
        }

        if(!target.exists() || 0 == target.length()) {
            Logger.error("[SYNC-FDL] Record Er @ Cache");
            return false;
        }

        String targetChecksum = EncryptHelper.calcFileMd5(target);
        int blockOffset = (target.length() % mBlockSize) > 0 ? 1 : 0;
        int blockNumber = (int)(target.length() / mBlockSize) + blockOffset;

        try {
            JSONObject raw = new JSONObject()
                    .put(FileMeta.RECORD_REQ_ID, rid)
                    .put(FileMeta.RECORD_CHECKSUM, targetChecksum)
                    .put(FileMeta.RECORD_PATH, target.getAbsolutePath())
                    .put(FileMeta.RECORD_BLOCK_SIZE, mBlockSize)
                    .put(FileMeta.RECORD_BLOCK_NUM, blockNumber)
                    .put(FileMeta.RECORD_BLOCK_INDEX, 0)
                    .put(FileMeta.RECORD_EXTRA, extra);
            recordData(new FileMeta(raw), true);
        } catch (Exception je) {
            target.delete();
            Logger.error("[SYNC-FDL] Record Er @ Meta");
            return false;
        }
        return true;
    }

    @Override
    protected IDataCache<FileMeta> onCreateDataCache() {
        return  new IDataCache<FileMeta>() {

            @Override
            public void remove(String key) {
                Logger.debug("[SYNC-FDL] remove key = %s", key);
                new File(mCacheDir, mName).delete();
                mCollection.set(mName, null);
            }

            @Override
            public void put(FileMeta data) {
                if(null == data) {
                    mCollection.set(mName, null);
                } else {
                    mCollection.set(mName, data.mRaw);
                }
            }

            @Override
            public List<Record<FileMeta>> list() {
                List<Record<FileMeta>> ret = new ArrayList<>();
                FileMeta meta = loadFileMeta();
                String id = meta.getRequestId();
                if(!TextUtils.isEmpty(id)) {
                    ret.add(new Record<>(mName, loadFileMeta()));
                }
                return ret;
            }
        };
    }

    @Override
    protected boolean onSyncPrepare() {
        // check if target in cache is valid
        FileMeta record = loadFileMeta();
        String checksum = record.getChecksum();
        if(TextUtils.isEmpty(checksum)) {
            Logger.error("[SYNC-FDL] Prepare Er @ Parameter");
            return false;
        }
        File target = record.getFile();
        return EncryptHelper.calcFileMd5(target).equals(checksum);
    }

    @Override
    protected boolean onSyncData(FileMeta record) {
        int retryCount = 0;
        File target = record.getFile();
        int blockSize = record.getBlockSize();
        int blockNum = record.getBlockNum();

        int index;
        while ((index = record.getBlockIndex()) < blockNum) {
            // calculate block MD5
            String cs;
            try (FileInputStream fis = new FileInputStream(target);
                 SubInputStream sis = new SubInputStream(fis, index * blockSize, blockSize)) {
                cs = EncryptHelper.calcFileMd5(sis);
            } catch (Exception e) {
                // Intercept SYNC
                throw new RuntimeException("[SYNC-FDL] SYNC Er @ CS", e);
            }

            // upload block
            try (FileInputStream fis = new FileInputStream(target);
                 SubInputStream sis = new SubInputStream(fis, index * blockSize, blockSize)) {
                if (send(record, cs, sis)) {
                    Logger.debug("[SYNC-FDL] SYNC OK @ %d", index);
                    // Block uploaded, set next index
                    recordData(record.setBlockIndex(index + 1), false);
                    retryCount = 0;
                } else if(retryCount < 4){
                    retryCount++;
                    Logger.debug("[SYNC-FDL] SYNC Er @ Wait Retry");
                    Thread.sleep(15 * 1000);
                } else {
                    throw new RuntimeException("[SYNC-FDL] SYNC Er @ Retry");
                }
            } catch (InterruptedException e) {
                // Intercept SYNC
                throw new RuntimeException("[SYNC-FDL] SYNC Intercept");
            } catch (IOException e) {
                throw new RuntimeException("[SYNC-FDL] SYNC Er @ FIO", e);
            } catch (ExecutionException e) {
                // TOKEN is deleted by server, no more sending needed
                Logger.error(e);
                break;
            }
        }
        return true;

    }

    /**
     * @param meta block information
     * @param md5  block checksum
     * @param body block data
     * @return
     * @throws ExecutionException transmission procedure will never be finished. (eg: server close upload token)
     */
    protected abstract boolean send(FileMeta meta, String md5, InputStream body) throws ExecutionException;

}
