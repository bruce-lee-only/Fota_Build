/*******************************************************************************
 * Copyright (C) 2018-2020 CAROTA Technology Crop. <www.carota.ai>.
 * All Rights Reserved.
 *
 * Unauthorized using, copying, distributing and modifying of this file,
 * via any medium is strictly prohibited.
 *
 * Proprietary and confidential.
 ******************************************************************************/

package com.carota.dm.task;


import java.io.InputStream;
import java.util.List;

public interface ITaskManager {

    void init();
    void resumeDownload(String id);
    void pauseDownload(String id);

    List<TaskInfo> listTasks();
    void deleteTask(String id);
    boolean addTask(String id, String url, String md5, String desc);

    InputStream findFileInputStreamById(String id) throws Exception;
    long findFileInputLengthById(String id);

    TaskInfo findTaskById(String id);

    long freeStorageSpace(List<String> keepIds);
    long calcAvailSpace();

    int getMaxRetry();


    long getLimitSpeed();

    String getTag();
}
