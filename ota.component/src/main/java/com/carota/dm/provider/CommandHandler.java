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
import com.carota.protobuf.DownloadManager;
import com.carota.svr.HttpResp;
import com.carota.svr.PrivStatusCode;
import com.momock.util.Logger;

import java.util.List;
import java.util.Map;

public class CommandHandler extends BaseHandler {

    public CommandHandler(Context context, ITaskManager tm) {
        super(context, tm);
    }

    @Override
    public HttpResp post(String path, Map<String, List<String>> params, byte[] body, Object extra) {
        DownloadManager.CommandRsp.Builder builder = DownloadManager.CommandRsp.newBuilder();
        try {
            DownloadManager.CommandReq cmdReq = DownloadManager.CommandReq.parseFrom(body);
            List<String> arrData = cmdReq.getIdsList();
            switch (cmdReq.getActionValue()) {
                case DownloadManager.CommandReq.Action.DELETE_VALUE:
                    methodDelete(arrData);
                    break;
                case DownloadManager.CommandReq.Action.STOP_VALUE:
                    methodStop(arrData);
                    break;
                case DownloadManager.CommandReq.Action.PREPARE_VALUE:
                    methodStop(null);
                    long total = Long.parseLong(cmdReq.getExtra());
                    long downloaded = mTm.freeStorageSpace(arrData);
                    long free = mTm.calcAvailSpace();
                    builder.setRequire(total - downloaded)
                            .setFree(free)
                            .setExtra(cmdReq.getExtra());
                    break;
                default:
                    return super.post(path, params, body, extra);
            }
            return HttpResp.newInstance(PrivStatusCode.OK, builder.build().toByteArray());
        } catch (Exception e) {
            Logger.error(e);
            return HttpResp.newInstance(PrivStatusCode.SRV_ACT_UNKNOWN, builder.build().toByteArray());
        }
    }

    private void methodDelete(List<String> arr) {
        if(null == arr || arr.size() <= 0) {
            mTm.deleteTask(null);
        } else {
            for(String id : arr) {
                mTm.deleteTask(id);
            }
        }
    }

    private void methodStop(List<String> arr) {
        if(null == arr || arr.size() <= 0) {
            mTm.pauseDownload(null);
        } else {
            for(String id : arr) {
                mTm.pauseDownload(id);
            }
        }
    }
}
