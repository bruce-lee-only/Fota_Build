/*******************************************************************************
 * Copyright (C) 2018-2020 CAROTA Technology Crop. <www.carota.ai>.
 * All Rights Reserved.
 *
 * Unauthorized using, copying, distributing and modifying of this file,
 * via any medium is strictly prohibited.
 *
 * Proprietary and confidential.
 ******************************************************************************/

package com.carota.core.offline;

import com.carota.mda.remote.info.BomInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Blueprint {

    public final String version;
    public final String condition;
    private final List<Module> mModuleList;
    private final HashMap<String, List<BomInfo>> mBomMap;

    public Blueprint(String version, String condition, List<BomInfo> bomInfoList) {
        this.version = version;
        this.condition = condition;
        mModuleList = new ArrayList<>();
        mBomMap = new HashMap<>();
        if (bomInfoList == null || bomInfoList.size() == 0) return;
        for (BomInfo bom : bomInfoList) {
            List<BomInfo> bomInfos = mBomMap.get(bom.getName());
            if (bomInfos == null) {
                bomInfos = new ArrayList<>();
                bomInfos.add(bom);
                mBomMap.put(bom.getName(), bomInfos);
            } else {
                bomInfos.add(bom);
            }
        }
    }

    public void addModule(Module module) {
        mModuleList.add(module);
    }

    public List<Module> list() {
        return new ArrayList<>(mModuleList);
    }

    public List<BomInfo> getBomInfo(String name) {
        return mBomMap.get(name);
    }
}
