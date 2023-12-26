/*******************************************************************************
 * Copyright (C) 2018-2020 CAROTA Technology Crop. <www.carota.ai>.
 * All Rights Reserved.
 *
 * Unauthorized using, copying, distributing and modifying of this file,
 * via any medium is strictly prohibited.
 *
 * Proprietary and confidential.
 ******************************************************************************/

package com.carota.util;

import java.util.ArrayDeque;
import java.util.Iterator;
import java.util.concurrent.Executor;

public class SerialExecutor implements Executor {

    public interface IFilter {
        boolean check(Runnable r);
    }

    private class ExecTask implements Runnable {

        private Runnable mPayload;
        private Runnable mCallback;

        private ExecTask (Runnable payload) {
            mPayload = payload;
            mCallback = null;
        }

        @Override
        public void run() {
            try {
                mPayload.run();
            } finally {
                scheduleNext();
                if(null != mCallback) {
                    mCallback.run();
                }
            }
        }
    }

    private final ArrayDeque<ExecTask> mTasks;
    private ExecTask mActive;
    private Thread mWorkThread;
    private Runnable mFinishNotify;

    public SerialExecutor() {
        mTasks = new ArrayDeque<>();
        mFinishNotify = null;
    }

    public void setFinishNotify(Runnable r) {
        mFinishNotify = r;
    }

    public synchronized void execute(Runnable r) {
        ExecTask task = new ExecTask(r);
        synchronized (mTasks) {
            mTasks.offer(task);
        }
        if (mActive == null) {
            scheduleNext();
        }
    }

    private synchronized void scheduleNext() {
        synchronized (mTasks) {
            mActive = mTasks.poll();
            if (null != mActive) {
                mWorkThread = new Thread(mActive);
                mWorkThread.start();
            } else {
                mWorkThread = null;
                if(null != mFinishNotify) {
                    mFinishNotify.run();
                }
            }
        }
    }

    public int size() {
        return mTasks.size();
    }

    public boolean isEmpty() {
        return mTasks.isEmpty();
    }

    public void stop(IFilter filter, Runnable result) {
        synchronized (mTasks) {
            Iterator<ExecTask> it = mTasks.iterator();
            while (it.hasNext()) {
                ExecTask task = it.next();
                if(null == filter || filter.check(task.mPayload)) {
                    it.remove();
                }
            }
            if(!stopActive(filter, result)) {
                return;
            }
        }
        if(null != result) {
            result.run();
        }
    }

    private synchronized boolean stopActive(IFilter filter, Runnable result) {
        if(null != mActive) {
            if(null == filter || filter.check(mActive.mPayload)) {
                mActive.mCallback = result;
                if(null != mWorkThread) {
                    mWorkThread.interrupt();
                    mWorkThread = null;
                    return false;
                }
            }
        }
        return true;
    }

    public void clearPending() {
        synchronized (mTasks) {
            mTasks.clear();
        }
    }

    public void stop() {
        stop(null, null);
    }

    public boolean contains(Runnable r) {
        return mTasks.contains(r);
    }

    public boolean isRunning() {
        return null != mWorkThread;
    }

    public boolean foreach(IFilter filter) {
        synchronized (mTasks) {
            for (ExecTask task : mTasks) {
                if (filter.check(task.mPayload)) {
                    return true;
                }
            }
            return null != mActive && filter.check(mActive.mPayload);
        }
    }
}
