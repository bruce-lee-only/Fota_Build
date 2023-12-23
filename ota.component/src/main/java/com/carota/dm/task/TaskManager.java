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
import com.carota.util.SerialExecutor;
import com.momock.util.Logger;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class TaskManager implements ITaskManager {

    private final IFileManager mFileManager;
    private final SerialExecutor mExecutor;
    private final Map<String, TaskInfo> mTasks;
    //小于等于0无限重试
    private final int mMaxRetry;
    // 限制下载速度64KB/S 需要暂停的时间,小于等于0不限制.等待配置文件进行添加动态设置
    private final long mLimitSpeed;
    private final String TAG;
    private final long mReservedStorage;


    public TaskManager(IFileManager manager, int maxRetry, int limitTime, long reservedStorage) {
        this.mFileManager = manager;
        this.mMaxRetry = maxRetry;
        this.mTasks = new HashMap<>();
        this.mExecutor = new SerialExecutor();
        mLimitSpeed = limitTime;
        TAG = "DM-TM-".concat(mFileManager.getTag());
        if (reservedStorage < 0) {
            mReservedStorage = Integer.MIN_VALUE / 2;
        } else {
            mReservedStorage = reservedStorage;
        }
        Logger.debug("%s [RESERVED STORAGE] : %d", TAG, mReservedStorage);
    }

    @Override
    public void init() {
        mFileManager.init();
    }

    @Override
    public void resumeDownload(String id) {
        synchronized (this) {
            if (null == id) {
                Logger.debug("%s Resume ALL", TAG);
                for (TaskInfo ti : mTasks.values()) {
                    resumeDownload(ti);
                }
            } else {
                Logger.debug("%s Resume TARGET%s", TAG, id);
                TaskInfo ti = mTasks.get(id);
                if (null != ti) {
                    resumeDownload(ti);
                }
            }
        }
    }

    private void resumeDownload(TaskInfo ti) {
        int status = ti.getStatus();
        if (TaskInfo.STATUS_IDLE == status || TaskInfo.STATUS_ERROR == status) {
            ti.setStatus(TaskInfo.STATUS_WAIT);
            String tid = ti.getId();
            Logger.debug("%s Resume : %s", TAG, tid);
            mExecutor.execute(new ExecHandler(tid, this, mFileManager));
        }
    }

    @Override
    public void pauseDownload(final String id) {
        synchronized (this) {
            mExecutor.foreach(r -> {
                if (r instanceof ExecHandler) {
                    ExecHandler handler = (ExecHandler) r;
                    if (null == id || handler.getId().equals(id)) {
                        handler.stop();
                    }
                }
                return false;
            });

            mExecutor.stop(r -> {
                ExecHandler handler = (ExecHandler) r;
                return null == id || handler.getId().equals(id);
            }, null);

            if (null == id) {
                Logger.debug("%s Pause ALL", TAG);
            } else {
                Logger.debug("%s Pause TARGET-%s", TAG, id);
            }
        }
    }

    @Override
    public List<TaskInfo> listTasks() {
        return new ArrayList<>(mTasks.values());
    }

    @Override
    public void deleteTask(String id) {
        synchronized (this) {
            if (null == id) {
                Logger.debug("%s[del] : ALL", TAG);
                mTasks.clear();
                mExecutor.stop(null, () -> {
                    Logger.debug("%s[del] : File-ALL", TAG);
                    mFileManager.clearDm();
                });
            } else {
                Logger.debug("%s[del] : %s", TAG, id);
                final TaskInfo task = mTasks.remove(id);
                if (null != task) {
                    mExecutor.stop(r -> {
                        if (r instanceof ExecHandler) {
                            return ((ExecHandler) r).getId().equals(task.getId());
                        }
                        return false;
                    }, () -> {
                        mFileManager.deleteFile(task.getFileName());
                        Logger.debug("%s[del] : File-%s", TAG, task.getId());
                    });
                } else {
                    Logger.debug("DM-TM-%s[del] : Not Found-%s", TAG, id);
                }
            }
        }
    }

    @Override
    public boolean addTask(String id, String url, String md5, String desc) {
        TaskInfo task = findTaskById(id);

        if (null != task) {
            int status = task.getStatus();
            if (TaskInfo.STATUS_RUNNING == status || TaskInfo.STATUS_WAIT == status) {
                Logger.debug("%s[add] : Exist", TAG);
            } else {
                Logger.debug("%s[add] : Restart", TAG);
                task.setStatus(TaskInfo.STATUS_WAIT);
                mExecutor.execute(new ExecHandler(id, this, mFileManager));
            }
            return true;
        }

        task = new TaskInfo.Builder(id, url)
                .setVerify(md5).setDesc(desc).build();

        if (null != task) {
            Logger.debug("%s[add] : %s", TAG, task.toString());
            mTasks.put(id, task);
            if (task.getStatus() == TaskInfo.STATUS_FINISH) {
                Logger.debug("%s[add] : Finished", TAG);
            } else {
                Logger.debug("%s[add] : In Queue", TAG);
                task.setStatus(TaskInfo.STATUS_WAIT);
                mExecutor.execute(new ExecHandler(id, this, mFileManager));
            }
            return true;
        }
        return false;
    }

    @Override
    public InputStream findFileInputStreamById(String id) {
        TaskInfo taskInfo = mTasks.get(id);
        if (taskInfo != null) {
            return mFileManager.findFileInputStream(taskInfo.getFileName());
        }
        return mFileManager.findFileInputStream(id);
    }

    @Override
    public long findFileInputLengthById(String id) {
        TaskInfo taskInfo = mTasks.get(id);
        if (taskInfo != null) {
            return mFileManager.findFileLength(taskInfo.getFileName());
        }
        return mFileManager.findFileLength(id);
    }

    @Override
    public TaskInfo findTaskById(String id) {
        if (null != id) {
            return mTasks.get(id);
        }
        return null;
    }

    @Override
    public long freeStorageSpace(List<String> keepIds) {
        long downloadedSize = 0;

        if (keepIds == null || keepIds.isEmpty()) {
            mTasks.clear();
        } else {
            Iterator<String> iterator = mTasks.keySet().iterator();
            while (iterator.hasNext()) {
                String id = iterator.next();
                if (!keepIds.contains(id)) {
                    iterator.remove();
                }
            }
        }

        String[] fileList = mFileManager.getDmAllFilesName();
        if (null != fileList) {
            for (String f : fileList) {
                // match all file like key, key.a, key.b
                String key = f.replaceAll("[.][^.]*$", "");
                List<String> ids = new ArrayList<>();
                if (keepIds != null) {
                    for (String id: keepIds) {
                        if (id.contains(key)) {
                            ids.add(key);
                        }
                    }
                    Logger.debug("%s[free] : id %s", TAG, keepIds.toString());
                }
                if (ids.contains(key)) {
                    downloadedSize += mFileManager.findFileLength(f);
                    Logger.debug("%s[free] : keep %s", TAG, key);
                } else if (mTasks.containsKey(key)) {
                    Logger.debug("%s[free] : task %s", TAG, key);
                } else {
                    mFileManager.deleteFile(f);
                    Logger.debug("%s[free] : del %s", TAG, key);
                }
            }
        }
        Logger.debug("%s[free] : downloaded %s", TAG, downloadedSize);
        return downloadedSize;
    }

    public long calcAvailSpace() {
        long free = mFileManager.calcAvailSpace();
        Logger.debug("%s[avail] : %d", TAG, free);
        return free - mReservedStorage;
    }

    @Override
    public int getMaxRetry() {
        return mMaxRetry;
    }

    @Override
    public long getLimitSpeed() {
        return mLimitSpeed;
    }

    @Override
    public String getTag() {
        return TAG;
    }


}
