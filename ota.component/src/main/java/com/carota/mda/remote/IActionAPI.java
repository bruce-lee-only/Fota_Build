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

import com.carota.mda.remote.info.EcuInfo;
import com.carota.mda.remote.info.EventInfo;
import com.carota.mda.remote.info.SecurityInfo;
import com.carota.mda.remote.info.TokenInfo;

import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public interface IActionAPI {

    String syncBom(String serverUrl, String vin);

    String connect(String serverUrl, String vin, String lang, List<EcuInfo> infos, boolean isFactory);

    boolean sendUpgradeReport(String serverUrl, String usid, EventInfo state);

    boolean sendUpgradeReportV2(String serverUrl, String usid, EventInfo state);

    boolean sendEventReport(String url, String vin, EventInfo state);

    boolean uploadLogFile(String url, String ulid,int len,int index,String md5, byte[] bLog);

    boolean uploadLogFile(String url, String id, int totalBlockNum, int blockIndex, String md5, InputStream is, String type);

    SecurityInfo querySecurtyInfo(String url, String token, List<String> fileId, long timeout);

    TokenInfo querySignatureInfo(String url, String cid, String usid, long timeout);

    boolean confirmExpired(String url, String vmid, String sid, Map<String, String> extra, AtomicBoolean ret);
}
