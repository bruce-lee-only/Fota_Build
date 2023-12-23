/*******************************************************************************
 * Copyright (C) 2018-2020 CAROTA Technology Crop. <www.carota.ai>.
 * All Rights Reserved.
 *
 * Unauthorized using, copying, distributing and modifying of this file,
 * via any medium is strictly prohibited.
 *
 * Proprietary and confidential.
 ******************************************************************************/

package com.carota.mda.remote.info;


import com.carota.dm.task.TaskInfo;

import java.util.List;

public interface IDownloadStatus {

    int DL_STATE_ERROR = TaskInfo.STATUS_ERROR;
    int DL_STATE_IDLE = TaskInfo.STATUS_IDLE;
    int DL_STATE_WAIT = TaskInfo.STATUS_WAIT;
    int DL_STATE_RUNNING = TaskInfo.STATUS_RUNNING;
    int DL_STATE_FINISHED = TaskInfo.STATUS_FINISH;

    interface IDownloadInfo {
        int getProgress();
        int getSpeed();
        int getState();
        String getId();
    }

    IDownloadInfo getDownloadInfo(String id);
    List<IDownloadInfo> listDownloadInfo();
}