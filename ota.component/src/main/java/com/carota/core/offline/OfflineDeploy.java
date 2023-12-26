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

import android.content.Context;
import android.os.SystemClock;
import android.text.TextUtils;

import com.carota.build.IConfiguration;
import com.carota.build.ParamDM;
import com.carota.build.ParamHub;
import com.carota.build.ParamMDA;
import com.carota.build.ParamRSM;
import com.carota.build.ParamRoute;
import com.carota.dm.file.ftp.FtpFileManager;
import com.carota.mda.deploy.DeviceUpdater;
import com.carota.mda.download.DownloadCtrl;
import com.carota.mda.remote.ActionSDA;
import com.carota.mda.remote.IActionSDA;
import com.carota.mda.remote.info.BomInfo;
import com.carota.mda.remote.info.EcuInfo;
import com.carota.rsm.XorInputStream;
import com.carota.svr.PrivReqHelper;
import com.carota.util.ConfigHelper;
import com.carota.util.MainServiceHolder;
import com.momock.util.Logger;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.List;

public class OfflineDeploy {
    public static final int DEFAULT_BUFFER_SIZE = 8 * 1024;
    public static final int QUERY_TIME = 10 * 60 * 1000;

    private class Works {
        Works(String host, BomInfo bom, EcuInfo ecuInfo) {
            this.host = host;
            this.bom = bom;
            this.ecuInfo = ecuInfo;
        }

        Works() {
            this(null, null, null);
        }

        final String host;
        final BomInfo bom;
        final EcuInfo ecuInfo;
    }

    private static OfflineDeploy sDeploy = null;

    public static OfflineDeploy get(Context context) {
        synchronized (OfflineDeploy.class) {
            if (null == sDeploy) {
                sDeploy = new OfflineDeploy(context);
            }
        }
        return sDeploy;
    }

    private IActionSDA mAdapter;
    private MainServiceHolder mServiceHolder;
    private Context mContext;

    private OfflineDeploy(Context context) {
        mContext = context.getApplicationContext();
        mAdapter = new ActionSDA();
        IConfiguration cfg = ConfigHelper.get(context);
        ParamHub paramHub = cfg.get(ParamHub.class);
        PrivReqHelper.setGlobalProxy(paramHub.getAddr(), paramHub.getPort());
        mServiceHolder = new MainServiceHolder(context.getPackageName());
    }

    public boolean startUpgrade(Blueprint data, Module m, boolean clearDm, boolean secure) {
        try {
            List<BomInfo> bomInfoList = data.getBomInfo(m.name);
            ParamRoute pRoute = ConfigHelper.get(mContext).get(ParamRoute.class);
            ParamRoute.Info rtInfo = pRoute.getRoute(m.name);
            String host = ParamRoute.getEcuHost(rtInfo);
            String subHost = pRoute.getSubHost();
            Works works = null;
            if (null != host) {
                works = getEcuInfo(m.name, host);
            } else if (subHost != null && bomInfoList != null) {
                works = getBomEcuInfo(m.name, bomInfoList, subHost);
            } else {
                Logger.error("[OFFLINE] Missing Route :%s", m.name);
            }
            if (works != null && works.ecuInfo != null) {
                Module.FileMeta fm = m.findMeta(works.ecuInfo.hwVer.concat("<>").concat(works.ecuInfo.swVer));
                if (null == fm) {
                    Logger.debug("[OFFLINE] find FULL : " + m.name);
                    fm = m.findMeta(works.ecuInfo.hwVer);
                }
                if (null == fm) {
                    if (inTv(m, works)) {
                        Logger.debug("[OFFLINE] In target Version: " + m.name);
                        return true;
                    } else {
                        Logger.error("[OFFLINE] No Update found : " + m.name);
                    }
                } else if (!fm.target.exists()) {
                    Logger.error("[OFFLINE] Read package Failure : " + m.name);
                } else if (works.ecuInfo.swVer.equals(fm.version)) {
                    Logger.debug("[OFFLINE] up-to-date : " + m.name);
                    return true;
                } else {
                    String dmHost = addFileToDm(fm, m.name, clearDm, secure);
                    Logger.info("[OFFLINE] Dm Host is %s", host);
                    if (!TextUtils.isEmpty(dmHost)) {
                        return install(m, works, dmHost, fm);
                    }
                }
            }
        } catch (Exception e) {
            Logger.error(e);
        }
        return false;
    }

    private boolean inTv(Module m, Works works) {
        Module.FileMeta fm = m.findMetaUseHv(works.ecuInfo.hwVer);
        return fm != null && getResult(works, m, fm.version);
    }

    private Works getBomEcuInfo(String name, List<BomInfo> bomInfoList, String host) {
        if (null == host) {
            Logger.error("[OFFLINE] Missing Bom Host : %s", name);
            return null;
        }
        for (BomInfo b : bomInfoList) {
            if (!name.equals(b.ID)) {
                Logger.error("[OFFLINE] Continue Bom ECU : %s @%s", b.ID, host);
                continue;
            }
            EcuInfo ei = mAdapter.queryInfo(host, name, b);
            if (null == ei) {
                Logger.error("[OFFLINE] Missing Bom ECU Info : %s @%s", name, host);
                continue;
            }
            String hv = b.getFlashConfig().getHv();
            Logger.error("[OFFLINE] Bom ECU Hv is : %s @%s", hv, host);
            if (TextUtils.isEmpty(hv) || hv.equals(ei.hwVer)) {
                return new Works(host, b, ei);
            } else {
                Logger.error("[OFFLINE] Bom ECU Hv Error @%s", host);
            }
        }
        return null;
    }

    /**
     * get route ecu info
     *
     * @param name
     * @param host
     * @return
     */
    private Works getEcuInfo(String name, String host) {
        if (null == host) {
            Logger.error("[OFFLINE] Missing Host : " + name);
            return null;
        }
        EcuInfo ei = mAdapter.queryInfo(host, name, null);
        if (null == ei) {
            Logger.error("[OFFLINE] Missing ECU : " + name + "@" + host);
            return null;
        }
        return new Works(host, null, ei);
    }

    private String addFileToDm(Module.FileMeta fm, String ecu, boolean clearDm, boolean secure) throws Exception {

        IConfiguration configuration = ConfigHelper.get(mContext);
        ParamMDA paramMDA = configuration.get(ParamMDA.class);
        ParamRoute route = configuration.get(ParamRoute.class);
        String dmManager = paramMDA.findDownloadManagerName(ecu);
        String host = route.getRoute(dmManager).getHost(ParamRoute.Info.PATH_VETH);
        Logger.info("[OFFLINE] Host is %s", host);
        ParamDM paramDM = configuration.get(ParamDM.class);
        ParamDM.Info info = paramDM.findDmInfo(host);
        if (info!=null && info.isFtp()) {
            //step1,stop download and clear dm
            DownloadCtrl downloadCtrl = new DownloadCtrl(null, paramMDA, configuration.get(ParamRoute.class));
            downloadCtrl.stop(false);
            //step2，push file to ftp
            boolean pushFile2Ftp = pushFile2Ftp(fm, info, clearDm, secure);
            if (pushFile2Ftp) return downloadCtrl.findDownloaderHost(ecu);
        } else {
            if (secure) {
                mServiceHolder.addFileSync(mContext, fm.target, fm.checksum, fm.checksum);
            } else {
                mServiceHolder.addFileSync(mContext, fm.target, null);
            }
            return configuration.get(ParamRSM.class).getHost();
        }
        Logger.error("[OFFLINE] Dm Manager Error %s", dmManager);
        return null;
    }

    private boolean pushFile2Ftp(Module.FileMeta fm, ParamDM.Info info, boolean clearDm, boolean secure) {
        FtpFileManager fileManager = FtpFileManager.newInstance(info);
        if (fileManager.verifyMd5(fm.checksum, fm.checksum, null)) {
            Logger.info("[OFFLINE] Push File to Ftp Success");
            return true;
        }
        if (clearDm) {
            fileManager.clearDm();
        } else {
            fileManager.deleteFile(fm.checksum);
        }
        int num = 0;
        InputStream inputStream = null;
        while (num < 10) {
            try {
                String tmp = fm.checksum.concat(".tmp");
                long startIndex = fileManager.findFileLength(tmp);
                Logger.info("[OFFLINE] Push File to Ftp Start,%s %s", String.valueOf(startIndex), String.valueOf(fm.target.length()));
                fileManager.downloadInit(tmp, startIndex, fm.target.length());
                if (secure) {
                    inputStream = new XorInputStream(fm.target, fm.checksum);
                } else {
                    inputStream = new FileInputStream(fm.target);
                }
                byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
                int length;
                inputStream.skip(startIndex);
                while ((length = inputStream.read(buffer, 0, buffer.length)) > 0) {
                    fileManager.downloadWrite(buffer, 0, length);
                }
                inputStream.close();
                fileManager.downloadRelease();
                boolean result = false;
                boolean downloadSuccess = fileManager.verifyMd5(tmp, fm.checksum, null);
                Logger.error("[OFFLINE] Push File '%s' to Ftp Success? %b", tmp, downloadSuccess);
                if (!downloadSuccess) {
                    Logger.error("[OFFLINE] Push File to Ftp End,Check Fail");
                    fileManager.deleteFile(tmp);
                } else if (fileManager.renameFile(tmp, fm.checksum)) {
                    Logger.info("[OFFLINE] Push File to Ftp Success");
                    result = true;
                } else {
                    Logger.error("[OFFLINE] Push File to Ftp End,Rename Fail");
                }
                return result;
            } catch (Exception e) {
                Logger.error(e);
            }
            num++;
            Logger.error("[OFFLINE] Push File to Ftp Retry:%d", num);
            try {
                if (inputStream != null) {
                    inputStream.close();
                    inputStream = null;
                }
                fileManager.downloadRelease();
            } catch (Exception e) {
                Logger.error(e);
            }
        }
        Logger.error("[OFFLINE] Push File to Ftp Max Retry");
        return false;
    }

    private boolean install(Module m, Works works, String dmHost, Module.FileMeta fm) throws Exception {
        Logger.info("[OFFLINE] %s Start Upgrade", m.name);
        DeviceUpdater updater = new DeviceUpdater(mAdapter, m.timeout, works.bom);
        updater.setDevice(works.host, m.name, dmHost, m.domain);
        updater.setTarget(fm.checksum, fm.version, null);
        long endTime = SystemClock.elapsedRealtime() + QUERY_TIME;
        if (DeviceUpdater.RET_SUCCESS == updater.call()) {
            do {
                Thread.sleep(10000);
                if (getResult(works, m, fm.version)) {
                    Logger.info("[OFFLINE] %s Upgrade Success", m.name);
                    return true;
                }
                if ((SystemClock.elapsedRealtime() > endTime)) {
                    Logger.info("[OFFLINE] %s Upgrade Qurey Time Out", m.name);
                    return false;
                }
            } while (true);
        }
        Logger.info("[OFFLINE] %s Upgrade Fail", m.name);
        return false;
    }


    public boolean getResult(Works works, Module m, String tv) {
        try {
            EcuInfo ei = mAdapter.queryInfo(works.host, m.name, works.bom);
            return null != ei && ei.swVer.equals(tv);
        } catch (Exception e) {
            Logger.error(e);
        }
        return false;
    }

}
