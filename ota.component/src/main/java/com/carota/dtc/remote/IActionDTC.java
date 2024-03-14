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

import com.carota.mda.remote.info.EcuInfo;

import java.io.InputStream;
import java.util.List;

public interface IActionDTC {

    int uploadFilterLogFile(String url, String token, int length, int index, String md5, String fullMd5, String sc, InputStream is);
    String queryDtcTask(String url, String vin, List<String> caps, List<EcuInfo> ecuInfoList);

}
