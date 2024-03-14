package com.carota.dtc.log.upload;

import android.content.Context;

import com.carota.dtc.log.data.Instruction;
import com.carota.dtc.log.engine.Processor;
import com.carota.sync.uploader.SysLogUploader;
import com.carota.sync.util.MaterialCollector;
import com.momock.util.FileHelper;
import com.momock.util.JsonHelper;
import com.momock.util.Logger;

import java.io.File;


public class SysLogCollector extends MaterialCollector<SysLogUploader> {
    private static final String TAG = "SysLogCollector";

    private Context mContext;
    private File mWorkDir;
    private Processor mProcessor;
    private Instruction mInstruction;
    private LogFileChunk mLogFileChunk;

    public SysLogCollector(Context context, File workDir, SysLogUploader uploader, LogFileChunk logFileChunk) {
        super(uploader, 3);
        mContext = context.getApplicationContext();
        mWorkDir = workDir;
        mProcessor = new Processor(mWorkDir.getPath());
        mLogFileChunk = logFileChunk;
    }

    @Override
    protected boolean doProcess(int stepIndex, CampaignStatus status) {
        boolean ret = true;
        Logger.debug(TAG + " doProcess " + stepIndex + " / mInstruction = " + (mInstruction == null));
        mLogFileChunk.updateState(stepIndex);
        switch (stepIndex) {
            case 0:
                // clear last
                if (mWorkDir != null) {
                    FileHelper.cleanDir(mWorkDir);
                    if (!mWorkDir.exists()) {
                        FileHelper.mkdir(mWorkDir);
                    }
                }
                break;
            case 1:
                // Filter logs
                if (mInstruction == null) {
                    mInstruction = new Instruction(JsonHelper.parseObject(status.getExtra()));
                    if (mInstruction == null) return false;
                }
                mProcessor.doProcess(mInstruction);
                break;
            case 2:
                if (mInstruction == null) {
                    mInstruction = new Instruction(JsonHelper.parseObject(status.getExtra()));
                    if (mInstruction == null) return false;
                }
                ret = getUploader().LogFile(status.getToken(), mWorkDir, mInstruction.getVin());
                break;
        }
        return ret;
    }

    @Override
    protected void onFinishWork(CampaignStatus status) {
        FileHelper.cleanDir(mWorkDir);
    }

}
