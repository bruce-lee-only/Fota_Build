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

import android.os.Bundle;
import android.text.TextUtils;

import com.carota.protobuf.SlaveDownloadAgent;
import com.momock.util.EncryptHelper;

import org.json.JSONException;
import org.json.JSONObject;

public final class SlaveTask {

    // private static final int OFFSET_SLIDE = 5;
    String name;
    String targetId;
    String targetVer;
    String targetSign;
    String srcId;
    String srcVer;
    String srcSign;
    String hostDM;
    byte[] applyInfo;
    int domain;

    private SlaveTask(String name, int domain) {
        this.name = name;
        this.domain = domain;
    }

    public static Bundle toBundle(SlaveTask task) {
        Bundle ret = new Bundle();
        ret.putString("name", task.name);
        ret.putString("tgt_id", task.targetId);
        ret.putString("tgt_ver", task.targetVer);
        ret.putString("tgt_sign", task.srcSign);
        ret.putInt("domain", task.domain);
        ret.putString("src_id", task.srcId);
        ret.putString("src_ver", task.srcVer);
        ret.putString("src_sign", task.srcSign);
        ret.putString("host", task.hostDM);
        ret.putByteArray("bom", task.applyInfo);
        return ret;
    }

    public static SlaveTask parseFrom(JSONObject data) throws JSONException {
        SlaveTask task = new SlaveTask(data.getString("name"), data.optInt("domain"));
        task.targetId = data.getString("tgt_id");
        task.targetVer = data.getString("tgt_ver");

        task.srcId = data.optString("src_id");
        task.srcVer = data.optString("src_ver");
        task.hostDM = data.getString("host");
        if (task.srcId.isEmpty() || task.srcVer.isEmpty()) {
            task.srcId = null;
            task.srcVer = null;
        }
        return task;
    }

    public static SlaveTask parseFrom(SlaveDownloadAgent.InstallReq data) {
        SlaveTask task = new SlaveTask(data.getName(), data.getDomain());
        task.hostDM = data.getHost();

        SlaveDownloadAgent.InstallReq.Payload tmp = data.getDst(0);
        task.targetId = tmp.getFile();
        task.targetVer = tmp.getVer();
        task.targetSign = tmp.getSign();

        if(data.getSrcCount() > 0) {
            tmp = data.getSrc(0);
            task.srcId = tmp.getFile();
            task.srcVer = tmp.getVer();
        }
        if (data.getApplyInfo() != null) {
            task.applyInfo = data.getApplyInfo().toByteArray();
        }
        return task;
    }
}
