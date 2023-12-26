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

import com.carota.build.ParamMDA;
import com.carota.build.ParamRoute;
import com.carota.mda.data.UpdateCampaign;
import com.carota.mda.data.UpdateItem;
import com.carota.mda.remote.ActionDM;
import com.carota.mda.remote.IActionDM;
import com.carota.mda.security.SecurityCenter;
import com.carota.mda.security.SecurityData;
import com.carota.util.SerialExecutor;
import com.momock.util.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

public class DownloadCtrl implements Runnable, IDownloadCtrlStatus {

    private SerialExecutor mWorker;
    private List<DownloadSection> mSectionList;
    private List<IActionDM> mDownloaderList;
    private DownloadGroup mDownloadGroup;
    private ParamMDA mParamMDA;
    private IDownloadObserver mMonitor;
    private String mUSID;
    private AtomicBoolean mIsFinished;
    private AtomicBoolean mHasError;
    private ParamRoute mParamRoute;
    private SecurityCenter mSecurityCenter;
    private final static int mTimeout = 60 * 1000;

    public DownloadCtrl(SecurityCenter securityCenter, ParamMDA paramMDA, ParamRoute paramRoute) {
        mParamMDA = paramMDA;
        mParamRoute = paramRoute;
        mSectionList = null;
        mMonitor = null;
        mWorker = new SerialExecutor();
        mDownloaderList = initDownloader();
        mSecurityCenter = securityCenter;
    }

    public synchronized void reset(UpdateCampaign updateSession, IDownloadObserver monitor) {
        Logger.debug("D_Ctrl RESET : " + updateSession.getUSID());
        stop(false);

        List<DownloadSection> sections = new ArrayList<>();
        DownloadGroup group = new DownloadGroup();
        AtomicLong tmpSize = new AtomicLong();

        for(int i = 0; i < updateSession.getItemCount(); i++) {
            tmpSize.set(0);
            DownloadSection ds = createFromSession(updateSession, i, tmpSize);
            group.addAllFile(ds.getDM(), ds.listFileId(), tmpSize.get());
            sections.add(ds);
        }

        synchronized (this) {
            mSectionList = sections;
            mDownloadGroup = group;
            mMonitor = monitor;
            mUSID = updateSession.getUSID();
            mIsFinished = new AtomicBoolean(false);
            mHasError = new AtomicBoolean(false);
        }
    }

    public synchronized void start() {
        if(mWorker.isRunning()) {
            Logger.debug("D_Ctrl START: Running");
            return;
        }
        mWorker.execute(this);
    }

    @Override
    public void run() {
        List<DownloadSection> sections;
        IDownloadObserver monitor;
        String usid;
        AtomicBoolean isFinished;
        AtomicBoolean hasError;
        DownloadGroup group;
        synchronized (this) {
            // cache all parameters to prevent parameter lost
            // when user call "void reset(UpdateSession, IMonitor)" in processing
            if(null != mSectionList) {
                sections = mSectionList;
            } else {
                Logger.error("D_Ctrl DL: Empty");
                return;
            }
            monitor = mMonitor;
            usid = mUSID;
            isFinished = mIsFinished;
            hasError = mHasError;
            group = mDownloadGroup;
        }
        Logger.debug("D_Ctrl DL: Start @ " + usid);
        if(null != monitor) {
            monitor.onStart(usid);
        }

        // prepare storage space
        Logger.debug("D_Ctrl DL: Prepare @ " + usid);
        if(null != group) {
            List<String> ids;
            AtomicLong totalSize = new AtomicLong();
            for(IActionDM dm : mDownloaderList) {
                ids = group.listFileId(dm.toString(), totalSize);
                if(ids!=null&&!dm.clean(totalSize.get(), ids)) {
                    Logger.error("D_Ctrl DL: Failure @ Storage =>" + usid);
                    if(null != monitor) {
                        mMonitor.onError(usid, sections.get(0));
                    }
                    return;
                }
            }
        }

        int finishedCount = 0;
        DownloadSection lastPart = null;
        try {
            for(DownloadSection part : sections) {
                if(part.download()) {
                    Logger.debug("D_Ctrl DL: Run @ " + part.getName() + "=>" + usid);
                    if(null != monitor) {
                        monitor.onDownload(usid, part);
                    }

                    while (part.update()) {
                        Logger.debug("D_Ctrl DL: Query @ " + part.getName() + "=>" + usid);
                        if(null != monitor) {
                            monitor.onDownloading(usid, part);
                        }
                        Thread.sleep(2000);
                    }
                    lastPart = part;
                    if(part.getErrorCode() == 0) {
                        Logger.debug("D_Ctrl DL: Done @ " + part.getName() + "=>" + usid);
                        if(finishedCount < sections.size() -1) {
                            if(null != monitor) {
                                monitor.onDownloaded(usid, part);
                            }
                        }
                        finishedCount++;
                        continue;
                    }
                }
                hasError.set(true);
                Logger.error("D_Ctrl DL: Failure @ " + part.getName() + "=>" + usid);
                if(null != monitor) {
                    monitor.onError(usid, part);
                }
                break;
            }
        } catch (RuntimeException re) {
            Logger.error(re);
        } catch (InterruptedException ie) {
            Logger.debug("D_Ctrl DL: Interrupt @ " + finishedCount + "=>" + usid);
        }

        isFinished.set(finishedCount == sections.size());

        Logger.debug("D_Ctrl DL: Stop @ " + usid);
        if(null != monitor) {
            monitor.onStop(usid, isFinished.get(), lastPart);
        }
    }

    public synchronized void stop(boolean clear) {
        Logger.debug("D_Ctrl STOP: " + clear);
        mWorker.stop();
        for (IActionDM dm : mDownloaderList) {
            if(clear) {
                dm.delete(null);
            } else {
                dm.stop(null);
            }
        }
    }

    private DownloadSection createFromSession(UpdateCampaign us, int index, AtomicLong totalSize) {
        UpdateItem task = us.getItem(index);
        String name, id, ver, url;

        name = task.getProp(UpdateItem.PROP_NAME);
        IActionDM mgr = findDownloader(name);
        DownloadSection section = new DownloadSection(name,  task.getIndex(), mgr);

        id = task.getProp(UpdateItem.PROP_SRC_MD5);
        if(!id.isEmpty()) {
            ver = task.getProp(UpdateItem.PROP_SRC_VER);
            url = us.getUrl(UpdateCampaign.PROP_URL_FILE, id);
            section.addFile(id, url, id, name + ":" + ver);
            totalSize.getAndAdd(task.getProp(UpdateItem.PROP_SRC_SIZE, 0L));
        }

        id = task.getProp(UpdateItem.PROP_DST_MD5);
        ver = task.getProp(UpdateItem.PROP_DST_VER);
        url = us.getUrl(UpdateCampaign.PROP_URL_FILE, id);
        section.addFile(id, url, id, name + ":" + ver);
        totalSize.getAndAdd(task.getProp(UpdateItem.PROP_DST_SIZE, 0L));

        if(task.getProp(UpdateItem.PROP_CFG_ENABLE, false)) {
            id = task.getProp(UpdateItem.PROP_ID);
            url = us.getUrl(UpdateCampaign.PROP_URL_CFG, id);
            section.addFile(id, url, null, name + ":cfg");
        }
        Logger.debug("D_Ctrl CREATE : DownloadSection[" + index + "], count = " + section.getFileCount());

        boolean hasSecurity = task.getProp(UpdateItem.PROP_HAS_SECURITY, Boolean.FALSE);
        if (hasSecurity && mSecurityCenter != null) {
            SecurityData securityData = mSecurityCenter.load(id);
            Logger.debug("security data : " + id +"|" + securityData.toString());
            section.addFile(securityData.md5, securityData.url, securityData.md5, "meta");
            totalSize.getAndAdd(securityData.size);
        }
        return section;
    }

    protected List<IActionDM> initDownloader() {
        List<IActionDM> ret = new ArrayList<>();
        for(String dmName : mParamMDA.listDownloadManager()) {
            ParamRoute.Info pri = mParamRoute.getRoute(dmName);
            String dmHost = pri.getHost(ParamRoute.Info.PATH_VETH);
            IActionDM downloader = new ActionDM(dmName, dmHost);
            ret.add(downloader);
            Logger.debug("D_Ctrl ADD: DM [" + dmName + "] @ " + dmHost);
        }
        return ret;
    }

    public IActionDM findDownloader(String ecu) {
        String dmName = mParamMDA.findDownloadManagerName(ecu);
        for(IActionDM d : mDownloaderList) {
            if(d.toString().equals(dmName)) {
                return d;
            }
        }
        Logger.error("D_Ctrl FIND: DM [" + dmName + "] Failure");
        return null;
    }

    public String findDownloaderHost(String ecu) {
        String dmName = mParamMDA.findDownloadManagerName(ecu);
        ParamRoute.Info pri = mParamRoute.getRoute(dmName);
        return pri.getHost(ParamRoute.Info.PATH_VETH);
    }

    @Override
    public IDownloadSection[] query() {
        return null == mSectionList ? null : mSectionList.toArray(new IDownloadSection[]{});
    }

    @Override
    public boolean isRunning() {
        return mWorker.isRunning();
    }

    @Override
    public boolean isFinished() {
        return null != mIsFinished && mIsFinished.get();
    }

    @Override
    public boolean hasError() {
        return null != mHasError && mHasError.get();
    }
}
