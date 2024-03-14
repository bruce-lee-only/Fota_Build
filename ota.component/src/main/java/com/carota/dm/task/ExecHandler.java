/*******************************************************************************
 * Copyright (C) 2018-2020 CAROTA Technology Crop. <www.carota.ai>.
 * All Rights Reserved.
 *
 * Unauthorized using, copying, distributing and modifying of this file,
 * via any medium is strictly prohibited.
 *
 * Proprietary and confidential.
 ******************************************************************************/

package com.carota.dm.task;

import com.carota.dm.file.IFileManager;
import com.carota.dm.down.FileDownloader;
import com.carota.dm.down.IFileDownloader;
import com.momock.util.Logger;

import java.util.concurrent.atomic.AtomicBoolean;


public class ExecHandler implements Runnable {

    private final String mId;
    private final ITaskManager mTaskMgr;
    private IFileDownloader mDownloader;
    private final AtomicBoolean mStopped;

    private final IFileManager mFileManager;

    public ExecHandler(String id, ITaskManager mgr, IFileManager manager) {
        mId = id;
        mTaskMgr = mgr;
        mFileManager = manager;
        mStopped = new AtomicBoolean(false);
        mDownloader = null;
    }

    public String getId() {
        return mId;
    }

    public void stop() {
        mStopped.set(true);
        TaskInfo task = mTaskMgr.findTaskById(mId);
        if (null != task) {
            synchronized (mStopped) {
                if (null != mDownloader) {
                    mDownloader.stop();
                } else {
                    Logger.debug("DM-EH : STOP WAIT @ " + mId);
                    task.setStatus(TaskInfo.STATUS_IDLE);
                }
            }
        } else {
            Logger.error("DM-EH : EMPTY ");
        }
    }

    @Override
    public void run() {
        TaskInfo task = mTaskMgr.findTaskById(mId);
        if (null == task) {
            Logger.error("DM-EH : EMPTY ");
            return;
        }

        if (mStopped.get()) {
            Logger.debug("DM-EH : Stop @ " + mId);
            return;
        }
        Logger.info("DM-EH : Start @" + mId);
        task.setStatus(TaskInfo.STATUS_RUNNING);
        if (mFileManager.existsFile(task.getFileName())) {
            if (mFileManager.verifyMd5(task.getFileName(), task.getVerify(), (length, fileLength) -> {
                task.setProgress((int) (length * 100 / fileLength));
                task.setmSpeed(1 << 20);
            })) {
                task.setStatus(TaskInfo.STATUS_FINISH);
                Logger.debug("DM-EH : REUSE @ " + mId);
                return;
            } else {
                mFileManager.deleteFile(task.getFileName());
                Logger.info("DM-EH : Delete @ " + mId);
            }
        }


        synchronized (mStopped) {
            mDownloader = new FileDownloader(task.getUrl(), task.getTmpFileName(), mFileManager,
                    mTaskMgr.getMaxRetry(), mTaskMgr.getLimitSpeed(), (speed, length, fileLength) -> {
                task.setProgress((int) (length * 80 / fileLength));
                task.setmSpeed(speed);
            });
        }

        try {
            int result = mDownloader.start();
            switch (result) {
                case IFileDownloader.CODE_SUCCESS:
                    boolean renameTo = mFileManager.renameFile(task.getTmpFileName(), task.getFileName());
                    if (renameTo) {
                        if (mFileManager.verifyMd5(task.getFileName(), task.getVerify(), (length, fileLength) -> {
                            task.setProgress((int) (length * 20 / fileLength) + 80);
                        })) {
                            task.setStatus(TaskInfo.STATUS_FINISH);
                            Logger.debug("DM-EH : finished @ " + mId);
                            return;
                        } else {
                            Logger.error("DM-EH : Verify Error @ " + mId);
                            mFileManager.deleteFile(task.getFileName());
                            task.setStatus(TaskInfo.STATUS_ERROR);
                        }
                    } else {
                        mFileManager.deleteFile(task.getTmpFileName());
                        Logger.error("DM-EH : Rename Error @ " + mId);
                        task.setStatus(TaskInfo.STATUS_ERROR);
                    }
                    break;
                case IFileDownloader.CODE_CANCLE:
                    Logger.error("DM-EH : STOP @ " + mId);
                    task.setStatus(TaskInfo.STATUS_IDLE);
                    break;
                default:
                    Logger.debug("DM-EH : Error @ %s Code:%d", mId, result);
                    task.setStatus(TaskInfo.STATUS_ERROR);
                    break;
            }

        } catch (Exception e) {
            Logger.error("DM-EH : EXCP @ " + mId);
            Logger.error(e);
            task.setStatus(TaskInfo.STATUS_ERROR);
        }
    }

}
