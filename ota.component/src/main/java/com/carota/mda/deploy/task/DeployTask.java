/*******************************************************************************
 * Copyright (C) 2018-2020 CAROTA Technology Crop. <www.carota.ai>.
 * All Rights Reserved.
 *
 * Unauthorized using, copying, distributing and modifying of this file,
 * via any medium is strictly prohibited.
 *
 * Proprietary and confidential.
 ******************************************************************************/

package com.carota.mda.deploy.task;


import com.carota.mda.data.UpdateItem;
import com.carota.mda.remote.info.BomInfo;

public final class DeployTask {

    public final String targetFileId, targetVer, sourceFileId, sourceVer;
    public final String name, descriptor;
    public final int step, group, line;
    public final boolean hasSecurity;
    public String tgtSignId, srcSignId;
    public final BomInfo bomInfo;

    public DeployTask(UpdateItem task, boolean secureEnable, BomInfo bomInfo) {
        this.targetFileId = task.getProp(UpdateItem.PROP_DST_MD5);
        this.targetVer = task.getProp(UpdateItem.PROP_DST_VER);
        this.sourceFileId = task.getProp(UpdateItem.PROP_SRC_MD5);
        this.sourceVer = task.getProp(UpdateItem.PROP_SRC_VER);
        this.name = task.getProp(UpdateItem.PROP_NAME);
        this.group = task.getProp(UpdateItem.PROP_GROUP, Integer.MAX_VALUE);
        this.step = task.getProp(UpdateItem.PROP_STEP, Integer.MAX_VALUE);
        this.line = task.getProp(UpdateItem.PROP_LINE, Integer.MAX_VALUE);
        this.descriptor = secureEnable ? task.getProp(UpdateItem.PROP_DESCRIPTOR) : null;
        this.hasSecurity = task.getProp(UpdateItem.PROP_HAS_SECURITY, Boolean.FALSE);
        this.bomInfo = bomInfo;
    }

    public boolean isSecurityEnable() {
        return hasSecurity;
    }

    public boolean havaBom() {
        return bomInfo != null;
    }

    @Override
    public String toString() {
        return "Task:{" +
                "name='" + name + '\'' +
                ", step=" + step +
                ", line=" + line +
                ", group=" + group +
                '}';
    }
}
