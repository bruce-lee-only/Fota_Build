package com.carota.config.data;

import org.json.JSONObject;

/*******************************************************************************
 * Copyright (C) 2022-2025 CAROTA Technology Crop. <www.carota.ai>.
 * All Rights Reserved.
 *
 * Unauthorized using, copying, distributing and modifying of this file,
 * via any medium is strictly prohibited.
 *
 * Proprietary and confidential.
 ******************************************************************************/
public class ConfigInfo {
    public String md5;
    public long size;
    public String digest;
    public String digestAlgo;
    public String fileUrl;

    public static ConfigInfo fromJson(JSONObject raw) {
        ConfigInfo configInfo = new ConfigInfo();
        JSONObject obj = raw.optJSONObject("data");
        if (obj == null) {
            return null;
        }
        JSONObject infoObj = obj.optJSONObject("info");
        String fileUrl = obj.optString("file_url");
        if (infoObj == null || fileUrl.isEmpty()) {
            return null;
        }
        String md5 = infoObj.optString("md5");
        if (md5.isEmpty()) {
            return null;
        }
        configInfo.setMd5(infoObj.optString("md5"));
        configInfo.setSize(infoObj.optInt("size"));
        configInfo.setDigest(infoObj.optString("digest"));
        configInfo.setDigestAlgo(infoObj.optString("digest_algo"));
        configInfo.setFileUrl(fileUrl);
        return configInfo;
    }

    public String getMd5() {
        return md5;
    }

    public void setMd5(String md5) {
        this.md5 = md5;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public String getDigest() {
        return digest;
    }

    public void setDigest(String digest) {
        this.digest = digest;
    }

    public String getDigestAlgo() {
        return digestAlgo;
    }

    public void setDigestAlgo(String digestAlgo) {
        this.digestAlgo = digestAlgo;
    }

    public String getFileUrl() {
        return fileUrl;
    }

    public void setFileUrl(String fileUrl) {
        this.fileUrl = fileUrl;
    }
}
