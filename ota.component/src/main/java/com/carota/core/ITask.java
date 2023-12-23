/*******************************************************************************
 * Copyright (C) 2018-2020 CAROTA Technology Crop. <www.carota.ai>.
 * All Rights Reserved.
 *
 * Unauthorized using, copying, distributing and modifying of this file,
 * via any medium is strictly prohibited.
 *
 * Proprietary and confidential.
 ******************************************************************************/

package com.carota.core;

public interface ITask {

    String PROP_SRC_VER = "sv";
    String PROP_DST_VER = "tv";
    String PROP_NAME = "name";
    String PROP_RELEASE_NOTE = "rn";
    String PROP_DST_SIZE = "d_size";
    String PROP_SRC_SIZE = "s_size";
    String PROP_UPDATE_TIME = "update_time";

    int getIndex();
    int getDownloadProgress();
    int getDownloadSpeed();
    int getDownloadState();
    int getInstallState();
    int getInstallProgress();
    <T> T getProp(String name, T def);
    String getProp(String name);
    String getSrcVer();
    String getDstVer();
    long getPackageSize();
    String getReleaseNote();
}
