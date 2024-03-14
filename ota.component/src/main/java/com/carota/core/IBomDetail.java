package com.carota.core;

import com.carota.protobuf.SlaveDownloadAgent;

public interface IBomDetail {
    String getName();
    SlaveDownloadAgent.InfoReq.Builder getInfoReq();
    SlaveDownloadAgent.InstallReq.ApplyInfo.Builder getInstallReq();
}
