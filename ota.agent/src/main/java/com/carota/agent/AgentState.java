package com.carota.agent;

import android.os.Bundle;

public class AgentState {
    public final static int RESULT_SUCCESS = RemoteAgent.INSTALL_SUCCESS;
    public final static int RESULT_UPGRADING = RemoteAgent.INSTALL_WAIT;
    public final static int RESULT_FAIL = RemoteAgent.INSTALL_ERROR_UPGRADE;
    private final int result;
    private int errorCode;
    private final int pro;
    private Bundle extra;

    /**
     * @param result   install Result (RESULT_SUCCESS、RESULT_UPGRADING、RESULT_FAIL)
     * @param progress install progress output(0-100)
     */
    public AgentState(int result, int progress) {
        this.result = result;
        this.pro = progress;
    }

    /**
     * @param result   install Result (RESULT_SUCCESS、RESULT_UPGRADING、RESULT_FAIL)
     * @param progress install progress output(0-100)
     * @param errorCode install Error Code
     */
    public AgentState(int result, int errorCode, int progress) {
        this.result = result;
        this.errorCode = errorCode;
        this.pro = progress;
    }

    public int getResult() {
        return result;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }

    public int getProgress() {
        return pro;
    }

    public Bundle getExtra() {
        return extra;
    }

    public void setExtra(Bundle extra) {
        this.extra = extra;
    }
}
