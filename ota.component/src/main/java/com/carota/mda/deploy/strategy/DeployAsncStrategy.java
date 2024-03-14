package com.carota.mda.deploy.strategy;

import com.carota.mda.deploy.DeviceUpdater;
import com.carota.mda.deploy.task.DeployTask;
import com.carota.mda.deploy.task.DeployTaskFactory;
import com.carota.mda.remote.info.BomInfo;
import com.momock.util.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class DeployAsncStrategy implements IDeployStratege, DeviceUpdater.IInstallListener {
    private final List<IDeployStratege> mData;
    private final List<BomInfo> mBomData;
    private final DeployTaskFactory mFactory;
    private boolean sendPInstallSuccess;
    private boolean isSendPInstall;
    private final Object mLock = new Object();

    public DeployAsncStrategy(List<DeployTask> tasks, boolean isRollback, DeployTaskFactory factory) {
        mData = new ArrayList<>();
        mBomData = new ArrayList<>();
        mFactory = factory;
        for (DeployTask t : tasks) {
            mData.add(isRollback
                    ? new DeployRollbackStrategy(mFactory, t)
                    : new DeployUpgradeStrategy(mFactory, t));
            if (t.havaBom()) mFactory.setInstallLisiner(t.name, this);
        }
    }

    @Override
    public boolean run() {
        Logger.error("SDA Start Run Asnc Strategy");
        isSendPInstall = false;
        sendPInstallSuccess = false;
        mBomData.clear();
        ExecutorService service = Executors.newFixedThreadPool(mData.size());
        AtomicInteger integer = new AtomicInteger(0);
        try {
            for (IDeployStratege next : mData) {
                service.execute(() -> {
                    try {
                        next.run();
                    } catch (Exception e) {
                        Logger.error(e);
                    }
                    integer.incrementAndGet();
                });
            }
            while (mData.size() != integer.get()) {
                waitSendPInstall(mData.size() - integer.get());
                try {
                    Thread.sleep(10000);
                } catch (Exception e) {
                    Logger.error(e);
                }
            }
        } catch (Exception e) {
            Logger.error(e);
        }
        mData.clear();
        mFactory.clearInstallLisiner(this);
        Logger.error("SDA Asnc Strategy Run End");
        mBomData.clear();
        return true;
    }

    private void waitSendPInstall(int waitEcuNum) {
        if (waitEcuNum == mBomData.size() && !isSendPInstall) {
            sendPInstallSuccess = mFactory.prepareInstall(mBomData);
            Logger.error("SDA Send prepareInstall Result:%b", sendPInstallSuccess);
            //恢复所有线程
            isSendPInstall = true;
            synchronized (mLock) {
                mLock.notifyAll();
            }
        }
    }

    @Override
    public boolean beforeInstall(String name, BomInfo bomInfo) {
        if (mData == null || mData.isEmpty()) return true;
        Logger.error("SDA `%s` wait send PInstall", name);
        synchronized (mBomData) {
            if (!mBomData.contains(bomInfo)) mBomData.add(bomInfo);
        }
        if (!isSendPInstall) {
            synchronized (mLock) {
                try {
                    mLock.wait();
                } catch (InterruptedException e) {
                    Logger.error(e);
                }
            }
        }
        return sendPInstallSuccess;
    }
}
