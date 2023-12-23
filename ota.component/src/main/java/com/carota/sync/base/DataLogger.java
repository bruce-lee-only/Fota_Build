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

import com.momock.util.Logger;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class DataLogger<T> {

    public static class Record<T> {
        private String mId;
        private T mVal;

        public Record(String id, T value) {
            mId = id;
            mVal = value;
        }

        public String getId() {
            return mId;
        }
        public T getValue() {
            return mVal;
        }
    }

    public interface IDataCache<T> {
        void remove(String id);
        void put(T data);
        List<Record<T>> list();
    }

    private final IDataCache<T> mDataCache;
    private final AtomicBoolean mIsRun;

    private static final ExecutorService EXECUTOR = Executors.newFixedThreadPool(3);

    public DataLogger() {
        mDataCache = onCreateDataCache();
        mIsRun = new AtomicBoolean(false);
    }

    protected abstract IDataCache<T> onCreateDataCache();

    protected abstract boolean onSyncData(T data);

    protected boolean onSyncPrepare() {
        return true;
    }

    protected final void recordData(T data, boolean syncNow) {
        Logger.debug("[SYNC-DL] Record");
        mDataCache.put(data);
        if(syncNow) {
            syncData();
        }
    }

    private synchronized void doSyncData() throws InterruptedException {
        List<Record<T>> data = mDataCache.list();
        do {
            Logger.debug("[SYNC-DL] SYNC List @ %d", null == data ? 0 : data.size());
            for(Record<T> r : data) {
                if(onSyncData(r.getValue())) {
                    Logger.debug("[SYNC-DL] SYNC OK @ %s", r.getId());
                    mDataCache.remove(r.getId());
                } else {
                    Logger.error("[SYNC-DL] SYNC Er @ %s, Wait Retry", r.getId());
                    Thread.sleep(15 * 1000);
                    break;
                }
                Thread.sleep(1000);
            }
            Thread.sleep(1000);
            data = mDataCache.list();
        } while (null != data && data.size() > 0);
    }

    public void syncData() {
        if(mIsRun.get()) {
            return;
        }
        mIsRun.set(true);
        try {
            EXECUTOR.execute(new Runnable() {
                @Override
                public void run() {
                    Logger.debug("[SYNC-DL] SYNC Start");
                    try {
                        if(onSyncPrepare()) {
                            doSyncData();
                        } else {
                            Logger.error("[SYNC-DL] SYNC Er @ Prepare");
                        }
                    } catch (Exception e) {
                        Logger.error(e);
                    } finally {
                        mIsRun.set(false);
                        Logger.debug("[SYNC-DL] SYNC Stop");
                    }
                }
            });
        } catch (Exception e) {
            Logger.error("[SYNC-DL] SYNC Er @ Trigger", e);
            mIsRun.set(false);
        }
    }

    public boolean isSyncing() {
        return mIsRun.get();
    }
}
