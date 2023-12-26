/*******************************************************************************
 * Copyright (C) 2018-2020 CAROTA Technology Crop. <www.carota.ai>.
 * All Rights Reserved.
 *
 * Unauthorized using, copying, distributing and modifying of this file,
 * via any medium is strictly prohibited.
 *
 * Proprietary and confidential.
 ******************************************************************************/

package com.carota.sda;

import android.content.Context;
import android.os.Bundle;
import android.os.RemoteException;

import java.io.File;
import java.util.List;

public interface ISlaveDownloadAgent {

    void init(Context context, File workDir);
    String getId();
    String getHost();
    SlaveInfo readInfo(String ecuName, Bundle bomInfo);
    boolean triggerUpgrade(SlaveTask task);
    SlaveState queryState(String ecuName) throws RemoteException;
    boolean isRunning();
    boolean fetchEvent(String type, int max, List<String> events, List<String> ids);
    boolean deleteEvent(List<String> ids);
    boolean listLogFiles(String type, int max, List<String> fileNames, String extraPath);
    File findLogFile(String name);
}
