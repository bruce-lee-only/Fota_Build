/*******************************************************************************
 * Copyright (C) 2018-2020 CAROTA Technology Crop. <www.carota.ai>.
 * All Rights Reserved.
 *
 * Unauthorized using, copying, distributing and modifying of this file,
 * via any medium is strictly prohibited.
 *
 * Proprietary and confidential.
 ******************************************************************************/

package com.carota.mda.remote;

import com.carota.mda.remote.info.IDownloadStatus;

import java.util.List;

public interface IActionDM {
    String MAKE_SELF = "!@";
    boolean download(String id, String url, String md5, String desc);
    IDownloadStatus queryStatus();
    boolean stop(String id);
    boolean delete(String id);
    boolean clean(long size, List<String> id);
}
