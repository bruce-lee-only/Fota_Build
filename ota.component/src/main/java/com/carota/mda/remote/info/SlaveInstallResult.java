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

import com.carota.protobuf.SlaveDownloadAgent;

import java.util.HashMap;
import java.util.Map;

public class SlaveInstallResult {

    public static final int STATUS_UNKNOWN = 0;
    public static final int STATUS_IDLE = 1;
    public static final int STATUS_DOWNLOAD = 2;
    public static final int STATUS_UPGRADE = 3;
    public static final int STATUS_ROLLBACK = 4;
    public static final int STATUS_SUCCESS = 5;
    public static final int STATUS_ERROR = 6;
    public static final int STATUS_FAILURE = 7;

    public static final int STEP_NONE = 0;
    public static final int STEP_TRANSPORT = 1;
    public static final int STEP_VERIFY = 2;
    public static final int STEP_DEPLOY = 3;
    public static final int STEP_INTERRUPT = 4;
    public static final int STEP_REBOOT = 5;

    private Map<String, SlaveDownloadAgent.ResultRsp.Task> mRaw;
    private final String mDesc;

    public SlaveInstallResult(SlaveDownloadAgent.ResultRsp raw) {
        mRaw = new HashMap<>();
        StringBuilder builder = new StringBuilder();
        for(int i = 0; i < raw.getTasksCount(); i++) {
            SlaveDownloadAgent.ResultRsp.Task t = raw.getTasks(i);
            mRaw.put(t.getName(), t);
            builder.append("[")
                    .append(t.getName())
                    .append(" @ ")
                    .append(t.getStatusValue())
                    .append("; PG = ").append(t.getProgress())
                    .append("; ER = ").append(t.getError())
                    .append("]; ");
        }
        mDesc = builder.toString();
    }

    public int getDomainByName(String name) {
        SlaveDownloadAgent.ResultRsp.Task task = mRaw.get(name);
        if(null != task) {
            return task.getDomain();
        } else {
            return -1;
        }
    }

    public int getStatusByName(String name) {
        SlaveDownloadAgent.ResultRsp.Task task = mRaw.get(name);
        if(null != task) {
            return convertStatus(task.getStatus());
        } else {
            return STATUS_UNKNOWN;
        }
    }

    public int getStepByName(String name) {
        SlaveDownloadAgent.ResultRsp.Task task = mRaw.get(name);
        if(null != task) {
            return convertStep(task.getStep());
        }
        return STEP_NONE;
    }

    public int getProgress(String name) {
        SlaveDownloadAgent.ResultRsp.Task task = mRaw.get(name);
        if(null != task) {
            return task.getProgress();
        }
        return 0;
    }

    public int getErrorCodeByName(String name) {
        SlaveDownloadAgent.ResultRsp.Task task = mRaw.get(name);
        if(null != task) {
            return task.getError();
        }
        return 0;
    }

    @Override
    public String toString() {
        return mDesc;
    }

    private static int convertStep(SlaveDownloadAgent.ResultRsp.Step error) {
        int er = STEP_DEPLOY;
        switch (error) {
            case TRANSPORT:
                er = STEP_TRANSPORT;
                break;
            case VERIFY:
                er = STEP_VERIFY;
                break;
            case DEPLOY:
                er = STEP_DEPLOY;
                break;
            case INTERRUPT:
                er = STEP_INTERRUPT;
                break;
            case REBOOT:
                er = STEP_REBOOT;
                break;
        }
        return er;
    }

    private static int convertStatus(SlaveDownloadAgent.ResultRsp.Status status) {
        int st = STATUS_UNKNOWN;
        switch (status) {
            case IDLE:
                st = STATUS_IDLE;
                break;
            case UPGRADE:
                st = STATUS_UPGRADE;
                break;
            case SUCCESS:
                st = STATUS_SUCCESS;
                break;
            case ROLLBACK:
                st = STATUS_ROLLBACK;
                break;
            case ERROR:
                st = STATUS_ERROR;
                break;
            case FAILURE:
                st = STATUS_FAILURE;
                break;
        }
        return st;
    }
}
