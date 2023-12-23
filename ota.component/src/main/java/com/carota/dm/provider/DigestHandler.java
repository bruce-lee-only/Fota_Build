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

import com.carota.CarotaClient;
import com.carota.build.ParamDM;
import com.carota.dm.task.ITaskManager;
import com.carota.protobuf.DownloadManager;
import com.carota.svr.HttpResp;
import com.carota.svr.PrivStatusCode;
import com.carota.util.ConfigHelper;
import com.momock.util.EncryptHelper;
import com.momock.util.Logger;

import java.io.File;
import java.util.List;
import java.util.Map;

public class DigestHandler extends BaseHandler {

    private File file;

    private DownloadManager.DigestResp.DigestStatus status = DownloadManager.DigestResp.DigestStatus.UNDO;

    private String shaData = "";

    private Context context;
    public DigestHandler(Context context, ITaskManager tm) {
        super(context, tm);

        this.context = context;
    }
    
    @Override
    public HttpResp post(String path, Map<String, List<String>> params, byte[] body, Object extra) {
        DownloadManager.DigestResp.Builder builder = DownloadManager.DigestResp.newBuilder();
        try {
            DownloadManager.DigestReq drReq = DownloadManager.DigestReq.parseFrom(body);
            String[] parts = drReq.getId().split("_");
            String fileName = parts[parts.length -1];
            //todo: 开始计算sha值
            if (drReq.getAction() == DownloadManager.DigestReq.Action.START){
                Logger.info("DigestHandler Action: START");
                Logger.info("DigestHandler fileName: " + fileName);
                //todo: 判断当前是否正在计算
                if (status != DownloadManager.DigestResp.DigestStatus.WAIT){
                    shaData = "";
                    status = DownloadManager.DigestResp.DigestStatus.WAIT;
                    new Thread(() -> {
                        switch (drReq.getTypeValue()){
                            case DownloadManager.DigestReq.Type.SHA256_VALUE:
                                shaData = getSHA("SHA256", fileName);
                                break;
                            case DownloadManager.DigestReq.Type.SHA1_VALUE:
                                shaData = getSHA("SHA1", fileName);
                                break;
                            case DownloadManager.DigestReq.Type.SHA512_VALUE:
                                shaData = getSHA("SHA512", fileName);
                                break;
                        }

                        if (shaData.isEmpty())
                            status = DownloadManager.DigestResp.DigestStatus.FAIL;
                        else
                            status = DownloadManager.DigestResp.DigestStatus.SUCCESS;
                    }).start();
                }
                return sendBuild(builder, fileName, DownloadManager.DigestResp.DigestStatus.WAIT);
            }
            //todo: 查询计算结果
            else if (drReq.getAction() == DownloadManager.DigestReq.Action.CHECK){
                Logger.info("DigestHandler Action check:" + "[status:" + status + "]" + "[shaData:" + shaData + "]");
                return sendBuild(builder, fileName, status);
            }else {
                return HttpResp.newInstance(PrivStatusCode.SRV_ACT_UNKNOWN, builder.build().toByteArray());
            }
        } catch (Exception e) {
            Logger.error(e);
            return HttpResp.newInstance(PrivStatusCode.SRV_ACT_UNKNOWN, builder.build().toByteArray());
        }
    }

    private HttpResp sendBuild(DownloadManager.DigestResp.Builder builder,
                               String fileName,
                               DownloadManager.DigestResp.DigestStatus iStatus){
        builder .setId(fileName)
                .setHexData(shaData)
                .setStatus(iStatus);
        return HttpResp.newInstance(PrivStatusCode.OK, builder.build().toByteArray());
    }

    private String getSHA(String sha, String fileName){

        ParamDM paramDM = ConfigHelper.get(context).get(ParamDM.class);
        Logger.info("DigestHandler file path: " + paramDM.getDownloadDir(context) + "/" + fileName);

        file = new File(paramDM.getDownloadDir(context) + "/" + fileName);
        if (!file.exists()){
            Logger.error("DigestHandler file not exists!!");
            return "";
        }

        String shaData = "";
        switch (sha){
            case "SHA256":
                shaData =  EncryptHelper.calcFileSHA256(file);
                Logger.info("DigestHandler calc SHA256 finish");
                break;
            case "SHA1":
                shaData =  EncryptHelper.calcFileSHA1(file);
                Logger.info("DigestHandler calc SHA1 finish");
                break;
            case "SHA512":
                shaData =  EncryptHelper.calcFileSHA512(file);
                Logger.info("DigestHandler calc SHA512 finish");
                break;
        }
        Logger.info("DigestHandler getSHA:" + shaData);
        return shaData;
    }

}
