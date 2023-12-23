/*******************************************************************************
 * Copyright (C) 2018-2020 CAROTA Technology Crop. <www.carota.ai>.
 * All Rights Reserved.
 *
 * Unauthorized using, copying, distributing and modifying of this file,
 * via any medium is strictly prohibited.
 *
 * Proprietary and confidential.
 ******************************************************************************/

package com.carota.core.remote.info;

import com.carota.core.ClientState;
import com.carota.protobuf.MasterDownloadAgent;

import java.util.HashMap;
import java.util.Map;

public class DownloadProgress {

    public static final String DP_STATE_ERROR = "error";
    public static final String DP_STATE_IDLE = "idle";
    public static final String DP_STATE_RUNNING = "run";
    public static final String DP_STATE_FINISHED = "ready";

    private Map<String, MasterDownloadAgent.DownloadRsp.Task> mTaskMap;
    private int mState;
    private String mDesc;

    public DownloadProgress(MasterDownloadAgent.DownloadRsp raw) {
        mTaskMap = new HashMap<>();
        mState = readState(raw);
        StringBuilder builder = new StringBuilder();
        builder.append(mState);
        for (MasterDownloadAgent.DownloadRsp.Task t : raw.getTasksList()) {
            mTaskMap.put(t.getName(), t);
            builder.append(" [").append(t.getName())
                    .append("; PG = ").append(t.getProgress())
                    .append("; SP = ").append(t.getSpeed())
                    .append("];");
        }
        mDesc = builder.toString();
    }

    public int getState() {
        return mState;
    }

    public int getProgress(String ecuName) {
        MasterDownloadAgent.DownloadRsp.Task task = mTaskMap.get(ecuName);
        if(null != task) {
            return task.getProgress();
        }
        return 0;
    }
	
	public int getSpeed(String ecuName) {
        MasterDownloadAgent.DownloadRsp.Task task = mTaskMap.get(ecuName);
        if(null != task) {
            return task.getSpeed();
        }
        return 0;
    }

    private int readState(MasterDownloadAgent.DownloadRsp raw) {
        int state = ClientState.DOWNLOAD_STATE_ERROR;
        switch (raw.getStatus()) {
            case IDLE:
                state = ClientState.DOWNLOAD_STATE_IDLE;
                break;
            case RUN:
                state = ClientState.DOWNLOAD_STATE_RUNNING;
                break;
            case READY:
                state = ClientState.DOWNLOAD_STATE_COMPLETE;
                break;
            case ERROR:
                state = ClientState.DOWNLOAD_STATE_ERROR;
                break;
        }
        return state;
    }

    @Override
    public String toString() {
        return mDesc;
    }
}
