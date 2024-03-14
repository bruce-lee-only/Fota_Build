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
import android.content.Intent;

import com.carota.dm.file.IFileManager;
import com.carota.dm.file.local.LocalFileManager;
import com.carota.dm.task.ITaskManager;
import com.carota.dm.task.TaskManager;
import com.carota.svr.IRouterHttp;
import com.carota.svr.RouterService;
import com.momock.util.Logger;

import java.io.File;

public class DownloadManagerService extends RouterService {

    private final Context mContext;
    private final ITaskManager mTaskManagerFactory;

    public DownloadManagerService(Context context, String name, File downloadDir, int retry, long reserveSpace, long limitSpeed) {
        super(name, true);
        mContext = context;
        IFileManager manager = new LocalFileManager(downloadDir);

        mTaskManagerFactory = new TaskManager(manager, retry, limitSpeed,reserveSpace);
    }

    @Override
    public void onInit(IRouterHttp server) {
        Logger.debug("DMS onInit");
        mTaskManagerFactory.init();
        server.setRequestHandler(mModuleName, "/dl", new DownloadHandler(mContext, mTaskManagerFactory));
        server.setRequestHandler(mModuleName, "/pg", new ProgressHandler(mContext, mTaskManagerFactory));
        server.setRequestHandler(mModuleName, "/file", new FileHandler(mContext, mTaskManagerFactory));
        server.setRequestHandler(mModuleName, "/cmd", new CommandHandler(mContext, mTaskManagerFactory));
    }

    @Override
    public void onStart(IRouterHttp server) {
        mTaskManagerFactory.resumeDownload(null);
    }

    @Override
    public void onStop(IRouterHttp server) {
        mTaskManagerFactory.pauseDownload(null);
    }

    @Override
    public void onWakeUp(IRouterHttp server, Intent i) {

    }
}
