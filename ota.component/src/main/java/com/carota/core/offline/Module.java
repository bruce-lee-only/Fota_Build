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

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class Module {
    private static final String DEFAULT_KEY = "default";

    public static class FileMeta {
        public final String version;
        public final String checksum;
        public final File target;

        public FileMeta(String ver, String cs, File file) {
            version = ver;
            checksum = cs;
            target = file;
        }
    }

    public final String name;
    public final int domain;
    public final long timeout;
    private Map<String, FileMeta> mMetaMap;

    public Module(String name, int domain, long timeout) {
        this.name = name;
        this.domain = domain;
        this.timeout = timeout;
        mMetaMap = new HashMap<>();
    }

    public void addFileMeta(FileMeta meta) {
        mMetaMap.put(DEFAULT_KEY, meta);
    }

    public void addFileMeta(String key, FileMeta meta) {
        mMetaMap.put(key, meta);
    }

    public FileMeta findMeta(String key) {
        FileMeta fm = mMetaMap.get(key);
        if(null == fm) {
            fm = mMetaMap.get(DEFAULT_KEY);
        }
        return fm;
    }

    public FileMeta findMetaUseHv(String hwVer) {
        for (String key : mMetaMap.keySet()) {
            if (key.startsWith(hwVer))return mMetaMap.get(key);
        }
        return null;
    }
}
