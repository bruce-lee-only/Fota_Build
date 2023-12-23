/*******************************************************************************
 * Copyright (C) 2018-2020 CAROTA Technology Crop. <www.carota.ai>.
 * All Rights Reserved.
 *
 * Unauthorized using, copying, distributing and modifying of this file,
 * via any medium is strictly prohibited.
 *
 * Proprietary and confidential.
 ******************************************************************************/

package com.carota.mda.security;

import android.content.Context;

import com.carota.build.ParamDM;
import com.carota.mda.data.SecurityDataCache;
import com.carota.mda.data.UpdateItem;
import com.carota.mda.remote.ActionAPI;
import com.carota.mda.remote.IActionAPI;
import com.carota.mda.remote.info.TokenInfo;
import com.carota.util.ConfigHelper;
import com.momock.util.Logger;

import java.io.File;
import java.util.concurrent.atomic.AtomicBoolean;


public class SecurityCenter{

    private SecurityDataCache mDataCache;
    private final long mTimeout;
    private IActionAPI mActionAPI;
    private ISecuritySolution mVerifyMethod;
    // add for temp use
    private File mDownloadDir;

    public SecurityCenter(Context context, long timeout, ISecuritySolution verifyMethod) {
        mActionAPI = new ActionAPI();
        mDataCache = new SecurityDataCache(context);
        mTimeout = timeout;
        mVerifyMethod = verifyMethod;
        mDownloadDir = ConfigHelper.get(context).get(ParamDM.class).getDownloadDir(context);
    }

    public boolean checkSecurityInfo(UpdateItem task, String url, String usid) {
        String tid = task.getProp(UpdateItem.PROP_DST_MD5);
        String sid = task.getProp(UpdateItem.PROP_SRC_MD5);
        String cid = task.getProp(UpdateItem.PROP_CID);
        url = url.replace("{PID}", cid).replace("{USID}", usid);
        return cache(url, cid, usid, tid, sid);
    }

    public boolean cache(String url, String cid, String usid, String tid, String sid) {
        synchronized (this) {
            int retryCount = 0;
            while (retryCount < 3) {
                retryCount++;
                if(null == url || null == cid || null == usid) {
                    Logger.error("SEC Parameter Error @ cache");
                    return false;
                }
                TokenInfo tokenInfo = mActionAPI.querySignatureInfo(url, cid, usid, mTimeout);
                if(null != tokenInfo) {
                    return mDataCache.putData(tid, tokenInfo)
                            && mDataCache.putData(sid, tokenInfo);
                }
            }
        }

        return false;
    }

    public SecurityData load(String fileId) {
        return mDataCache.loadData(fileId);
    }

    public boolean verifyPackage(String fileId, String signId) {
        File target = new File(mDownloadDir, fileId);
        File signFile = new File(mDownloadDir, "/sign/" + signId);
        if(!target.exists()) {
            return false;
        }
        AtomicBoolean verifyResult = new AtomicBoolean();

        mVerifyMethod.decryptPackage(verifyResult, target, mDownloadDir, signFile);
        return verifyResult.get();
    }

    public boolean verifyPackage(String dmHost, String fileId, String signId) {
        File signFile = new File(mDownloadDir, "/sign/" + signId);
        AtomicBoolean verifyResult = new AtomicBoolean();

        mVerifyMethod.decryptPackage(verifyResult, dmHost, fileId, signFile);
        return verifyResult.get();
    }
}
