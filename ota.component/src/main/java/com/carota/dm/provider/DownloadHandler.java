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
import com.carota.protobuf.Telemetry;
import com.carota.svr.HttpResp;
import com.carota.svr.PrivStatusCode;
import com.google.protobuf.InvalidProtocolBufferException;
import com.momock.util.Logger;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;
import java.util.Map;

import fi.iki.elonen.HttpStatusCode;

/**
 * 执行下载任务
 */
public class DownloadHandler extends BaseHandler {

    public DownloadHandler(Context context, ITaskManager tm) {
        super(context, tm);
    }

    @Override
    public HttpResp post(String path, Map<String, List<String>> params, byte[] body, Object extra) {
        Telemetry.EmptyRsp.Builder builder = Telemetry.EmptyRsp.newBuilder();
        try {
            DownloadManager.DownloadReq dlReq = DownloadManager.DownloadReq.parseFrom(body);
            int newTaskCount = 0;
            for(DownloadManager.DownloadReq.Plan p : dlReq.getPlansList()) {
                if(mTm.addTask(p.getId(), p.getUrl(), p.getMd5(), p.getDesc())){
                    newTaskCount++;
                }
            }
            if(newTaskCount == dlReq.getPlansCount()) {
                mTm.resumeDownload(null);
                return HttpResp.newInstance(PrivStatusCode.OK, builder.build().toByteArray());
            }
        } catch (Exception e) {
            Logger.error(e);
        }
        return super.post(path, params, body, extra);
    }
}
