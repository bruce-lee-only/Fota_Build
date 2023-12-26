/*******************************************************************************
 * Copyright (C) 2018-2020 CAROTA Technology Crop. <www.carota.ai>.
 * All Rights Reserved.
 *
 * Unauthorized using, copying, distributing and modifying of this file,
 * via any medium is strictly prohibited.
 *
 * Proprietary and confidential.
 ******************************************************************************/

package com.carota.mda.download;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

public class DownloadGroup {

    private class Group {
        private final String name;
        private List<String> files;
        private long size;

        private Group(String group) {
            name = group;
            files = new ArrayList<>();
        }
    }

    private Map<String, Group> mGroupMap;

    public DownloadGroup() {
        mGroupMap = new HashMap<>();
    }

    public void addAllFile(String group, List<String> fid, long size) {
        Group gp = mGroupMap.get(group);
        if(null == gp) {
            gp = new Group(group);
            mGroupMap.put(group, gp);
        }
        gp.files.addAll(fid);
        if(size > 0) {
            gp.size += size;
        }
    }

    public List<String> listFileId(String group, AtomicLong totalSize) {
        Group gp = mGroupMap.get(group);
        if(null != gp) {
            totalSize.set(gp.size);
            return gp.files;
        }
        return null;
    }
}
