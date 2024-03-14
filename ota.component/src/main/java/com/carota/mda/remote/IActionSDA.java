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

import com.carota.mda.remote.info.BomInfo;
import com.carota.mda.remote.info.EcuInfo;
import com.carota.mda.remote.info.SlaveInstallResult;

import java.io.File;
import java.util.List;

public interface IActionSDA {

    interface IOnUploadEvent {
        boolean send(List<String> data);
    }

    int RET_INS_OK = 0;
    int RET_INS_BUSY = 1;
    int RET_INS_FAIL = 2;

    EcuInfo queryInfo(String host, String name, BomInfo bomInfo);

    boolean prepareInstall(String host, List<BomInfo> list);

    int triggerInstall(String host, String dmHost, String ecuName, int domain,
                       String tgtId, String tgtVer,
                       String srcId, String srcVer,  String tgtSignId, String srcSignId, BomInfo bomInfo);

    SlaveInstallResult queryInstallResult(String host, String ecus);

    boolean uploadEvent(String host, String type, int max, IOnUploadEvent cb);

    boolean collectLogFiles(String host, int max, File dir, String extraPath);

    boolean checkAlive(String host);
}
