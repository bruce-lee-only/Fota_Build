package com.carota.mda.provider;

import com.carota.mda.UpdateMaster;
import com.carota.protobuf.MasterDownloadAgent;
import com.carota.svr.HttpResp;
import com.carota.svr.PrivStatusCode;
import com.carota.svr.SimpleHandler;
import com.momock.util.Logger;

import java.util.List;
import java.util.Map;

public class EventHandler extends SimpleHandler {

    private final UpdateMaster mMaster;

    public EventHandler(UpdateMaster master) {
        mMaster = master;
    }

    @Override
    public HttpResp post(String path, Map<String, List<String>> params, byte[] body, Object extra) {
        PrivStatusCode code = PrivStatusCode.SRV_UNKNOWN;
        try {
            MasterDownloadAgent.EventReq req = MasterDownloadAgent.EventReq.parseFrom(body);
            switch (req.getAction()) {
                case EVENT:
                    for (MasterDownloadAgent.EventReq.Event event : req.getEventList()) {
                        if (!mMaster.sendUIEvent(event.getAt(), event.getUpgradeType(), event.getEventCode(),
                                event.getMsg(), event.getResult(), event.getScheduleId(), event.getEICSystem())) {
                            break;
                        }
                    }
                    code = PrivStatusCode.OK;
                    break;
                case POINT:
                    for (MasterDownloadAgent.EventReq.Point point : req.getPointList()) {
                        if (!mMaster.sendUIPoint(point.getAt(), point.getId(), point.getMsg())) {
                            break;
                        }
                    }
                    code = PrivStatusCode.OK;
                    break;
                case FOTA:
                    for (MasterDownloadAgent.EventReq.Fota fota : req.getFotaList()) {
                        if (!mMaster.sendFotaData(fota.getTotalState(), fota.getEcu(), fota.getState(), fota.getCode(), fota.getTime(), fota.getError())) {
                            break;
                        }
                    }
                    code = PrivStatusCode.OK;
                    break;
            }

        } catch (Exception e) {
            Logger.error(e);
        }
        return HttpResp.newInstance(code);
    }
}
