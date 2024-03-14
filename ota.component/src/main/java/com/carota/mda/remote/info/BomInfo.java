package com.carota.mda.remote.info;

import com.carota.core.IBomDetail;
import com.carota.protobuf.SlaveDownloadAgent;
import com.carota.util.ConvertUtil;
import com.momock.util.EncryptHelper;
import com.momock.util.JsonHelper;
import com.momock.util.Logger;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;
import java.util.List;

public class BomInfo implements IBomDetail {
    public final String ID;
    public String busType;
    public String flashConfig;
    public JSONArray dids;
    public JSONObject doip;
    public JSONObject docan;
    public JSONArray props;
    public static final String BUS_TYPE_CAN = "CAN";
    public static final String BUS_TYPE_ETHERNET = "ETHERNET";
    public static final String BUS_TYPE_FLEXRAY = "FLEXRAY";
    public static final String BUS_TYPE_LIN = "LIN";

    private String md5;
    private String url;

    public BomInfo(String id) {
        ID = id;
    }

    public void setMd5(String md5) {
        this.md5 = md5;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public static JSONObject toJson(BomInfo info) throws JSONException {
        JSONObject jo = new JSONObject();
        jo.put("ecu", info.ID);
        jo.put("dc_type", null != info.busType ? info.busType : "");
        jo.put("flash", null != info.flashConfig ? info.flashConfig : "");
        jo.put("dids", null != info.dids ? info.dids : new JSONArray());
        jo.put("doip", null != info.doip ? info.doip : new JSONObject());
        jo.put("docan", null != info.docan ? info.docan : new JSONObject());
        jo.put("props", null != info.props ? info.props : new JSONArray());
        return jo;
    }

    public static BomInfo fromJson(JSONObject info) throws JSONException {
        BomInfo ret = new BomInfo(info.optString("ecu"));
        ret.busType = info.optString("dc_type");
        ret.flashConfig = info.optString("flash");
        ret.dids = info.optJSONArray("dids");
        ret.doip = info.optJSONObject("doip");
        ret.docan = info.optJSONObject("docan");
        ret.props = info.optJSONArray("props");
        return ret;
    }

    public static SlaveDownloadAgent.InstallPrepareReq.Builder getInstallPrepareReq(List<BomInfo> infos) {
        SlaveDownloadAgent.InstallPrepareReq.Builder req = SlaveDownloadAgent.InstallPrepareReq.newBuilder();
        if (infos != null) {
            for (BomInfo info : infos) {
                SlaveDownloadAgent.InstallPrepareReq.flashInfo.Builder builder = SlaveDownloadAgent.InstallPrepareReq.flashInfo.newBuilder();
                builder.setName(info.getName());
                builder.setBusType(info.getInstallBusType());
                builder.setDocan(info.getPrepareInstallDoCAN());
                builder.setDoip(info.getPrepareInstallDoIP());
                builder.setHvo(info.getFlashConfig().getHvo());
                builder.setFileUrl(info.url);
                builder.setMd5(info.md5);
                req.addFlashInfo(builder.build());
            }
        }

        return req;
    }

    private SlaveDownloadAgent.DoIP getPrepareInstallDoIP() {
        SlaveDownloadAgent.DoIP.Builder doIp = SlaveDownloadAgent.DoIP.newBuilder();
        doIp.setIp(doip.optString("ip"));
        doIp.setExternalLogicalAddr(ConvertUtil.toHex(doip.optString("ext_la")));
        doIp.setInternalLogicalAddr(ConvertUtil.toHex(doip.optString("int_la")));
        doIp.setEcuLogicalAddr(ConvertUtil.toHex(doip.optString("ecu_la")));
        List<String> funcLa = JsonHelper.parseArray(doip.optJSONArray("func_la"), String.class);
        for (String s : funcLa) {
            doIp.addFuncLogicalAddr(ConvertUtil.toHex(s));
        }
        return doIp.build();
    }

    private SlaveDownloadAgent.DoCAN getPrepareInstallDoCAN() {
        SlaveDownloadAgent.DoCAN.Builder doCan = SlaveDownloadAgent.DoCAN.newBuilder();
        doCan.setReqId(ConvertUtil.toHex(docan.optString("req_id")));
        doCan.setReqId(ConvertUtil.toHex(docan.optString("res_id")));
        List<String> funcId = JsonHelper.parseArray(docan.optJSONArray("func_id"), String.class);
        for (String s : funcId) {
            doCan.addFuncId(ConvertUtil.toHex(s));
        }
        return doCan.build();
    }

    @Override
    public String getName() {
        return ID;
    }

    private SlaveDownloadAgent.InfoReq.BusType getInfoBusType() {
        switch (busType) {
            case BUS_TYPE_CAN:
                return SlaveDownloadAgent.InfoReq.BusType.CAN;
            case BUS_TYPE_ETHERNET:
                return SlaveDownloadAgent.InfoReq.BusType.ETHERNET;
            case BUS_TYPE_FLEXRAY:
                return SlaveDownloadAgent.InfoReq.BusType.FLEXRAY;
            case BUS_TYPE_LIN :
                return SlaveDownloadAgent.InfoReq.BusType.LIN;
        }
        return SlaveDownloadAgent.InfoReq.BusType.UNKNOWN;
    }

    private SlaveDownloadAgent.BusType getInstallBusType() {
        switch (busType) {
            case BUS_TYPE_CAN:
                return SlaveDownloadAgent.BusType.CAN;
            case BUS_TYPE_ETHERNET:
                return SlaveDownloadAgent.BusType.ETHERNET;
            case BUS_TYPE_FLEXRAY:
                return SlaveDownloadAgent.BusType.FLEXRAY;
            case BUS_TYPE_LIN:
                return SlaveDownloadAgent.BusType.LIN;
        }
        return SlaveDownloadAgent.BusType.UNKNOWN;
    }

    private SlaveDownloadAgent.InfoReq.EncodeType getEncodeType(String response) {
        if ("ascii".equals(response)) {
            return SlaveDownloadAgent.InfoReq.EncodeType.ASCII;
        } else if ("bcd".equals(response)) {
            return SlaveDownloadAgent.InfoReq.EncodeType.BCD;
        }
        return SlaveDownloadAgent.InfoReq.EncodeType.ASCII;
    }

    private SlaveDownloadAgent.InstallReq.FirmwareType getFirmwareType(String fType) {
        if ("bin".equals(fType)) {
            return SlaveDownloadAgent.InstallReq.FirmwareType.BIN;
        } else if ("hex".equals(fType)) {
            return SlaveDownloadAgent.InstallReq.FirmwareType.HEX;
        } else if ("s19".equals(fType)) {
            return SlaveDownloadAgent.InstallReq.FirmwareType.S19;
        }
        return SlaveDownloadAgent.InstallReq.FirmwareType.FIRMWARE_UNKNOWN;
    }

    private SlaveDownloadAgent.InstallReq.EraseType getEraseType(String eType) {
        if ("full".equals(eType)) {
            return SlaveDownloadAgent.InstallReq.EraseType.FULL;
        } else if ("sector".equals(eType)) {
            return SlaveDownloadAgent.InstallReq.EraseType.SECTOR;
        }
        return SlaveDownloadAgent.InstallReq.EraseType.ERASE_UNKNOWN;
    }

    private SlaveDownloadAgent.InstallReq.ChecksumType getChecksumType(String cType) {
        if ("with_address".equals(cType)) {
            return SlaveDownloadAgent.InstallReq.ChecksumType.WITH_ADDRESS;
        } else if ("without_address".equals(cType)) {
            return SlaveDownloadAgent.InstallReq.ChecksumType.WITHOUT_ADDRESS;
        }
        return SlaveDownloadAgent.InstallReq.ChecksumType.CHECKSUM_UNKNOWN;
    }

    @Override
    public SlaveDownloadAgent.InfoReq.Builder getInfoReq() {
        SlaveDownloadAgent.InfoReq.Builder req = SlaveDownloadAgent.InfoReq.newBuilder();
        req.setName(ID);
        req.setBusType(getInfoBusType());
        req.setDoip(getInfoDoIP());
        req.setDocan(getInfoDoCAN());
        if (dids != null) {
            for (int i = 0; i < dids.length(); i++) {
                JSONObject jobj = dids.optJSONObject(i);
                SlaveDownloadAgent.InfoReq.DataSet.Builder did = SlaveDownloadAgent.InfoReq.DataSet.newBuilder();
                did.setKey(jobj.optString("name"));
                did.setValStr(jobj.optString("value"));
                did.setResponse(getEncodeType(jobj.optString("response")));
                req.addDids(did);
            }
        }
        if (props != null) {
            for (int i = 0; i < props.length(); i++) {
                JSONObject jobj = props.optJSONObject(i);
                SlaveDownloadAgent.InfoReq.DataSet.Builder prop = SlaveDownloadAgent.InfoReq.DataSet.newBuilder();
                prop.setKey(jobj.optString("name"));
                prop.setValStr(jobj.optString("value"));
                req.addProps(prop);
            }
        }
        return req;
    }

    @Override
    public SlaveDownloadAgent.InstallReq.ApplyInfo.Builder getInstallReq() {
        SlaveDownloadAgent.InstallReq.ApplyInfo.Builder req = SlaveDownloadAgent.InstallReq.ApplyInfo.newBuilder();
        req.setBusType(getInstallBusType());
        req.setDocan(getInstallDoCAN());
        req.setDoip(getInstallDoIP());
        req.setFlash(getFlashConfig());
        return req;
    }

    private SlaveDownloadAgent.InfoReq.DoIP.Builder getInfoDoIP() {
        SlaveDownloadAgent.InfoReq.DoIP.Builder doIp = SlaveDownloadAgent.InfoReq.DoIP.newBuilder();
        doIp.setIp(doip.optString("ip"));
        doIp.setExternalLogicalAddr(doip.optString("ext_la"));
        doIp.setInternalLogicalAddr(doip.optString("int_la"));
        doIp.setEcuLogicalAddr(doip.optString("ecu_la"));
        doIp.addAllFuncLogicalAddr(JsonHelper.parseArray(doip.optJSONArray("func_la"), String.class));
        return doIp;
    }

    private SlaveDownloadAgent.InfoReq.DoCAN.Builder getInfoDoCAN() {
        SlaveDownloadAgent.InfoReq.DoCAN.Builder doCan = SlaveDownloadAgent.InfoReq.DoCAN.newBuilder();
        doCan.setReqId(docan.optString("req_id"));
        doCan.setRespId(docan.optString("res_id"));
        doCan.addAllFuncId(JsonHelper.parseArray(docan.optJSONArray("func_id"), String.class));
        return doCan;
    }

    private SlaveDownloadAgent.InstallReq.DoIP.Builder getInstallDoIP() {
        SlaveDownloadAgent.InstallReq.DoIP.Builder doIp = SlaveDownloadAgent.InstallReq.DoIP.newBuilder();
        doIp.setIp(doip.optString("ip"));
        doIp.setExternalLogicalAddr(doip.optString("ext_la"));
        doIp.setInternalLogicalAddr(doip.optString("int_la"));
        doIp.setEcuLogicalAddr(doip.optString("ecu_la"));
        doIp.addAllFuncLogicalAddr(JsonHelper.parseArray(doip.optJSONArray("func_la"), String.class));
        return doIp;
    }

    private SlaveDownloadAgent.InstallReq.DoCAN.Builder getInstallDoCAN() {
        SlaveDownloadAgent.InstallReq.DoCAN.Builder doCan = SlaveDownloadAgent.InstallReq.DoCAN.newBuilder();
        doCan.setReqId(docan.optString("req_id"));
        doCan.setRespId(docan.optString("res_id"));
        doCan.addAllFuncId(JsonHelper.parseArray(docan.optJSONArray("func_id"), String.class));
        return doCan;
    }

    public SlaveDownloadAgent.InstallReq.Flash.Builder getFlashConfig() {
        SlaveDownloadAgent.InstallReq.Flash.Builder flashCfg = SlaveDownloadAgent.InstallReq.Flash.newBuilder();
        try {
            byte[] decode = flashConfig.getBytes("utf-8");
            String flashJson = EncryptHelper.decryptBASE64(decode);
            JSONObject jsonObject = JsonHelper.parseObject(flashJson);
            flashCfg.setFlashSeq(jsonObject.optString("flash_seq"));
            flashCfg.setHvo(Integer.parseInt(jsonObject.optString("hvo")));
            flashCfg.setFirmwareType(getFirmwareType(jsonObject.optString("firmware_type")));
            flashCfg.setEraseType(getEraseType(jsonObject.optString("erase_type")));
            flashCfg.setChecksumType(getChecksumType(jsonObject.optString("checksum_type")));
            flashCfg.setAppAddress(jsonObject.optString("app_address"));
            flashCfg.setAppSize(jsonObject.optString("app_size"));
            flashCfg.setDriverAddress(jsonObject.optString("driver_address"));
            flashCfg.setDriverSize(jsonObject.optString("driver_size"));
            flashCfg.setCalAddress(jsonObject.optString("cal_address"));
            flashCfg.setCalSize(jsonObject.optString("cal_size"));
            flashCfg.setMask(jsonObject.optString("mask"));
            flashCfg.setHv(jsonObject.optString("hv"));
            flashCfg.setSaType(jsonObject.optString("sa_type"));
            flashCfg.setTargetPath(jsonObject.optString("target_path"));
            flashCfg.setFileIntegrityCheck(jsonObject.optString("file_integrity_check"));
            JSONArray jsonArray = jsonObject.optJSONArray("props");
            if (jsonArray != null) {
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jObj = jsonArray.getJSONObject(i);
                    Iterator<String> iterator = jObj.keys();
                    while (iterator.hasNext()) {
                        SlaveDownloadAgent.InstallReq.Prop.Builder prop = SlaveDownloadAgent.InstallReq.Prop.newBuilder();
                        String key = iterator.next();
                        prop.setKey(key);
                        prop.setValue(jObj.optString(key));
                        flashCfg.addProps(prop);
                    }
                }
            }
        } catch (Exception e) {
            Logger.error(e);
        }
        return flashCfg;
    }

    @Override
    public String toString() {
        return "BomInfo{" +
                "ID='" + ID + '\'' +
                ", busType='" + busType + '\'' +
                ", flashConfig='" + flashConfig + '\'' +
                ", dids=" + dids +
                ", doip=" + doip +
                ", docan=" + docan +
                ", props=" + props +
                '}';
    }
}
