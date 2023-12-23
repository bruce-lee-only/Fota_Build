/*******************************************************************************
 * Copyright (C) 2018-2020 CAROTA Technology Crop. <www.carota.ai>.
 * All Rights Reserved.
 *
 * Unauthorized using, copying, distributing and modifying of this file,
 * via any medium is strictly prohibited.
 *
 * Proprietary and confidential.
 ******************************************************************************/

package com.carota.dm.provider;

import android.content.Context;

import com.carota.dm.task.ITaskManager;
import com.carota.dm.task.TaskInfo;
import com.carota.protobuf.DownloadManager;
import com.carota.svr.HttpResp;
import com.carota.svr.PrivStatusCode;
import com.momock.util.Logger;

import java.util.List;
import java.util.Map;

public class ProgressHandler extends BaseHandler {

    public ProgressHandler(Context context, ITaskManager tm) {
        super(context, tm);
    }

    @Override
    public HttpResp get(String path, Map<String, List<String>> params, Object extra) {
        Logger.info("DM_Console[pg] : recv");
        DownloadManager.ProgressRsp.Builder builder = DownloadManager.ProgressRsp.newBuilder();

        for (TaskInfo task : mTm.listTasks()) {
            builder.addWorks(DownloadManager.ProgressRsp.Work.newBuilder()
                    .setStatus(getStatus(task))
                    .setId(task.getId())
					.setSpeed(task.getSpeed())
                    .setProgress(task.getProgress())
                    .setDesc(task.getDesc())
            );
        }
        Logger.info("DM_Console[pg] : OK");
        return HttpResp.newInstance(PrivStatusCode.OK, builder.build().toByteArray());

    }

    private DownloadManager.ProgressRsp.Work.Status getStatus(TaskInfo task) {
        switch (task.getStatus()) {
            case TaskInfo.STATUS_ERROR:
                return DownloadManager.ProgressRsp.Work.Status.ERROR;
            case TaskInfo.STATUS_IDLE:
                return DownloadManager.ProgressRsp.Work.Status.IDLE;
            case TaskInfo.STATUS_WAIT:
                return DownloadManager.ProgressRsp.Work.Status.WAIT;
            case TaskInfo.STATUS_RUNNING:
                return DownloadManager.ProgressRsp.Work.Status.RUN;
            case TaskInfo.STATUS_FINISH:
                return DownloadManager.ProgressRsp.Work.Status.FINISH;
        }
        return DownloadManager.ProgressRsp.Work.Status.ERROR;
    }
}
