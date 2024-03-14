/*******************************************************************************
 * Copyright (C) 2018-2020 CAROTA Technology Crop. <www.carota.ai>.
 * All Rights Reserved.
 *
 * Unauthorized using, copying, distributing and modifying of this file,
 * via any medium is strictly prohibited.
 *
 * Proprietary and confidential.
 ******************************************************************************/
package com.carota.dtc.remote;

public enum UploadLogCode {
    /**
     * 参数校验未通过
     **/
    UPLOAD_OK(0, " Upload OK"),
    /**
     * 参数校验未通过
     **/
    PARAMETER_ERRORS(6, " Parameter check fail"),

    /**
     * 文件上传时文件名为空
     */
    FILE_UPLOAD_EMPTY_NAME(38, " Upload filename is empty"),

    /**
     * client log关闭
     */
    CLIENT_LOG_OFF(39, " Client log is turned off"),
    /**
     * 文件分片序号超出范围
     */
    INDEX_OUT_OF_RANGE(40, " The index is out of range"),
    /**
     * 文件分片已存在
     */
    DATA_EXISTS(41, " Data already exists"),
    /**
     * 文件的md5与服务器计算的md5码不一致
     */
    MD5_INCONSISTENT(42, " The MD5 of the uploaded file is inconsistent with the MD5 of the server"),
    /**
     * 诊断任务已关闭，拒绝日志上传
     */
    SCHEDULE_DISABLED(43," The Diagnose Schedule had been Disabled."),
    /**
     * 文件分片序号不连续
     */
    PRE_CLIENT_LOG_SKIPPED(44," Please Upload Pre Client Log Chunk. Do not Skip."),
    ;

    private int code = -1;
    private String msg;

    UploadLogCode(int i, String s) {
        code = i;
        msg = s;
    }

    public String getMsg() {
        return msg;
    }

    public int getCode() {
        return code;
    }
}
