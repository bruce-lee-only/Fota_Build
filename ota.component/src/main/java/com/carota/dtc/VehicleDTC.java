/*******************************************************************************
 * Copyright (C) 2018-2020 CAROTA Technology Crop. <www.carota.ai>.
 * All Rights Reserved.
 *
 * Unauthorized using, copying, distributing and modifying of this file,
 * via any medium is strictly prohibited.
 *
 * Proprietary and confidential.
 ******************************************************************************/
package com.carota.dtc;

import android.content.Context;
import android.text.TextUtils;

import com.carota.build.ParamDTC;
import com.carota.build.ParamRoute;
import com.carota.dtc.log.data.Instruction;
import com.carota.dtc.log.upload.LogFileChunk;
import com.carota.dtc.log.upload.SysLogCollector;
import com.carota.dtc.remote.ActionDTC;
import com.carota.mda.remote.info.EcuInfo;
import com.carota.sync.DataSyncManager;
import com.carota.sync.uploader.SysLogUploader;
import com.carota.util.ConfigHelper;
import com.carota.util.DatabaseHolderEx;
import com.carota.util.SerialExecutor;
import com.momock.util.JsonHelper;
import com.momock.util.Logger;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class VehicleDTC {
    private static volatile VehicleDTC sVehicleDTC;
    private SerialExecutor sExecutor;

    public static VehicleDTC get() {
        if (sVehicleDTC == null) {
            synchronized (VehicleDTC.class) {
                if (sVehicleDTC == null) {
                    sVehicleDTC = new VehicleDTC();
                }
            }
        }
        return sVehicleDTC;
    }

    private VehicleDTC() {

    }

    public void activeDtcTask(Context context, String vin, List<EcuInfo> ecuInfos) {
        ParamDTC paramDTC = ConfigHelper.get(context).get(ParamDTC.class);
        String taskUrl = paramDTC.getTaskUrl();
        String uploadUrl = paramDTC.getUploadUrl();

        if (!paramDTC.isEnabled() || TextUtils.isEmpty(taskUrl) || TextUtils.isEmpty(uploadUrl)) {
            Logger.error("VehicleDTC There has no dtc config,return");
            return;
        }
        if (sExecutor == null) {
            sExecutor = new SerialExecutor();
        } else if (sExecutor.isRunning() || !sExecutor.isEmpty()) {
            Logger.error("VehicleDTC Is Running");
            return;
        }
        sExecutor.execute(new Runnable() {
            @Override
            public void run() {
                LogFileChunk logFileChunk = new LogFileChunk(DatabaseHolderEx.getSysLog(context));
                SysLogUploader sysLogUploader = DataSyncManager.get(context).getSync(SysLogUploader.class);
                //mVin = "HZTEST01121100001";
                int state = logFileChunk.queryState();
                Logger.debug("VehicleDTC activeDtcTask  mVin = %s, state = %d", vin, state);
                File workDir = paramDTC.getWorkDir(context);
                Logger.debug("VehicleDTC workDir = " + workDir.getPath() + " / taskUrl = " + taskUrl + " / uploadUrl = " + uploadUrl + " / isEnabled = " + paramDTC.isEnabled());
                SysLogCollector sysLogCollector = new SysLogCollector(context, workDir, sysLogUploader, logFileChunk);
                if (state == 0 || state == 1 || state == 2 && !TextUtils.isEmpty(sysLogUploader.getRequestId())) {
                    sysLogCollector.resume();
                } else {
                    logFileChunk.cleanState();
                    if (TextUtils.isEmpty(vin)) {
                        Logger.error("VehicleDTC Vin is null");
                        return;
                    }
                    getDtcTask(context, taskUrl, uploadUrl, vin, sysLogCollector,ecuInfos);
                }
            }
        });
    }


    private void getDtcTask(Context context, String taskUrl, String uploadUrl, String vin, SysLogCollector sysLogCollector, List<EcuInfo> ecuInfos) {
        ParamRoute paramRoute = ConfigHelper.get(context).get(ParamRoute.class);
        ArrayList<Object> list = new ArrayList<>();
        String filterJson = new ActionDTC().queryDtcTask(taskUrl, vin, null, ecuInfos);
        Logger.debug("VehicleDTC getDtcTask filterJson = %s", filterJson);
        setFilterJson(filterJson, uploadUrl, vin, sysLogCollector);
    }

    public void setFilterJson(String json, String uploadUrl, String vin, SysLogCollector sysLogCollector) {
        Logger.debug("VehicleDTC setFilterJson json = %s", json);
        if (json == null) {
            Logger.error("VehicleDTC setFilterJson json == null");
            return;
        }
        Instruction instruction = new Instruction(JsonHelper.parseObject(json));
        if (instruction.getCode() == 0 && instruction.hasData()) {
            if (!instruction.hasComplete()) {
                Logger.debug("VehicleDTC uploadUrl = %s / mVin = %s", uploadUrl, vin);
                sysLogCollector.active(instruction.getToken(), json);
            } else {
                Logger.debug("VehicleDTC setFilterJson task has complete");
            }
        } else {
            Logger.debug("VehicleDTC setFilterJson has no task,msg = %s", instruction.getMsg());
        }
    }
}
