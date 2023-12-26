package com.carota.hmi.status;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;

import com.carota.core.ISession;
import com.carota.hmi.EventType;
import com.carota.hmi.UpgradeType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HmiStatus implements IStatus {
    private final HmiSharedPreferences mPreferences;
    //升级类型
    private UpgradeType mUpgradeType;

    private final Map<EventType, UpgradeStaus> mNodeStatus;

    private ISession mSession;
    private ArrayList<IConditionItem> mConditionResult;
    private int mInstallState;
    private int mInstallSuccessCount;
    private boolean isAutoRuning;


    public HmiStatus(Context context) {
        mUpgradeType = UpgradeType.DEFULT;
        mNodeStatus = new HashMap<>();
        mPreferences = new HmiSharedPreferences(context);
    }

    public void setUpgradeType(UpgradeType type) {
        if (type == null) return;
        mUpgradeType = type;
        mNodeStatus.clear();
        isAutoRuning = false;
        if (type == UpgradeType.SCHEDULE || type == UpgradeType.UPGRADE_NOW) {
            mNodeStatus.put(EventType.CHECK, UpgradeStaus.SUCCESS);
            mNodeStatus.put(EventType.DOWNLOAD, UpgradeStaus.SUCCESS);
        }
    }

    public UpgradeType getUpgradeType() {
        return mUpgradeType;
    }

    public UpgradeStaus getUpgradeStatus(EventType nodeType) {
        UpgradeStaus v;
        return (v = mNodeStatus.get(nodeType)) != null ? v : UpgradeStaus.IDIL;
    }

    public void setUpgradeStatus(EventType nodeType, UpgradeStaus staus) {
        if (nodeType == null) return;
        mNodeStatus.put(nodeType, staus);
        switch (nodeType) {
            case INSTALL:
                if (staus == UpgradeStaus.SUCCESS || staus == UpgradeStaus.FAIL) {
                    mPreferences.clearInstallType();
                } else if (staus == UpgradeStaus.RUNNING) {
                    UpgradeType type = mPreferences.installType();
                    if (mUpgradeType == UpgradeType.DEFULT) {
                        mUpgradeType = type;
                    } else {
                        mPreferences.saveInstallType();
                    }
                }
                break;
            case ENTER_OTA:
                if (staus == UpgradeStaus.SUCCESS) mPreferences.saveInOta(true);
                break;
            case EXIT_OTA:
                if (staus == UpgradeStaus.SUCCESS) mPreferences.saveInOta(false);
                break;
        }
    }


    public boolean isFactory() {
        return UpgradeType.FACTORY == mUpgradeType;
    }

    @Override
    public ISession getSession() {
        return mSession;
    }

    @Override
    public int getDownloadPro() {
        if (mSession != null && mNodeStatus.get(EventType.DOWNLOAD) != UpgradeStaus.IDIL) {
            int totalProgress = 0;
            int count = mSession.getTaskCount();
            if (count == 0) return 0;
            for (int i = 0; i < count; i++) {
                totalProgress += mSession.getTask(i).getDownloadProgress();
            }
            return totalProgress / mSession.getTaskCount();
        }
        return 0;
    }

    @SuppressLint("DefaultLocale")
    @Override
    public String getDownloadSpeed() {
        long speed = 0;
        if (mSession != null && mNodeStatus.get(EventType.DOWNLOAD) != UpgradeStaus.IDIL) {
            int count = mSession.getTaskCount();
            if (count > 0) {
                for (int i = 0; i < count; i++) {
                    speed += mSession.getTask(i).getDownloadSpeed();
                }
            }
        }
        long kbs = speed >> 10;
        if (kbs >= 1024) {
            return String.format("%.1f MB/S", ((float) kbs) / 1024);
        } else {
            return String.format("%d KB/S", kbs);
        }
    }

    @Override
    public boolean downloadResult() {
        return mNodeStatus.get(EventType.DOWNLOAD) == UpgradeStaus.SUCCESS;
    }

    @Override
    public int getUpgradeStatus() {
        // TODO: 2023/6/30
        return mInstallState;
    }

    @Override
    public int getEcuUpgradeSuccessCount() {
        return mInstallSuccessCount;
    }

    @Override
    public List<IConditionItem> getCondition() {
        return new ArrayList<>(mConditionResult);
    }

    public void setSession(ISession session) {
        if (mSession != session) {
            mSession = session;
        }
    }

    public boolean inOta() {
        return mPreferences.inOta();
    }

    public void setCondition(ArrayList<IConditionItem> result) {
        this.mConditionResult = result;
    }

    public void setInstallState(int state) {
        if (mInstallState != state) {
            mInstallState = state;
        }
    }

    public void setInstallSuccessCount(int successCount) {
        if (mInstallSuccessCount != successCount) mInstallSuccessCount = successCount;
    }

    public boolean isAutoRuning() {
        return isAutoRuning;
    }

    public void setAutoRuning(boolean isAutoRuning) {
        this.isAutoRuning = isAutoRuning;
    }

    @Override
    public boolean canDownload() {
        return getUpgradeStatus(EventType.CHECK) == UpgradeStaus.SUCCESS && mSession != null && mSession.getTaskCount() > 0;
    }

    private class HmiSharedPreferences {
        private final SharedPreferences install_type;
        private final String KEY_UPGRADE_TYPE = "factory";
        private final String KEY_OTA = "ota";

        public HmiSharedPreferences(Context context) {
            install_type = context.getSharedPreferences("carota_install_type", Context.MODE_PRIVATE);
        }

        private void clearInstallType() {
            install_type.edit().putInt(KEY_UPGRADE_TYPE, UpgradeType.DEFULT.getTypeNum()).commit();
        }

        void saveInOta(boolean inOta) {
            install_type.edit().putBoolean(KEY_OTA, inOta).commit();
        }

        boolean inOta() {
            return install_type.getBoolean(KEY_OTA, false);
        }


        void saveInstallType() {
            install_type.edit().putInt(KEY_UPGRADE_TYPE, mUpgradeType.getTypeNum()).commit();
        }

        UpgradeType installType() {
            int i = install_type.getInt(KEY_UPGRADE_TYPE, 0);
            if (i == UpgradeType.FACTORY.getTypeNum()) {
                return UpgradeType.FACTORY;
            } else if (i == UpgradeType.SCHEDULE.getTypeNum()) {
                return UpgradeType.SCHEDULE;
            } else if (i == UpgradeType.UPGRADE_NOW.getTypeNum()) {
                return UpgradeType.UPGRADE_NOW;
            } else {
                return UpgradeType.DEFULT;
            }
        }
    }
}

