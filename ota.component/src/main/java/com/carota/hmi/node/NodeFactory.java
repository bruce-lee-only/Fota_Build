package com.carota.hmi.node;

import android.content.Context;
import android.os.Handler;

import com.carota.hmi.EventType;
import com.carota.hmi.callback.CallBackManager;
import com.carota.hmi.node.holder.NodeHolder;
import com.carota.hmi.node.holder.IHolder;
import com.carota.hmi.status.HmiStatus;

import java.util.ArrayList;
import java.util.List;

public class NodeFactory {

    private final HmiStatus mStatus;
    private final Handler mHandler;
    private final Context mContext;
    private final CallBackManager mCallback;

    public NodeFactory(HmiStatus status, Handler handler, Context context, CallBackManager callBack) {
        this.mStatus = status;
        this.mHandler = handler;
        this.mContext = context;
        this.mCallback = callBack;
    }

    public INode getNode(EventType type) {
        switch (type) {
            case CHECK:
                return new CheckNode(mStatus, mHandler, mCallback);
            case DOWNLOAD:
                return new DownloadNode(mStatus, mHandler, mCallback);
            case TASK_VERIFY:
                return new TaskVerifyNode(mStatus, mHandler, mCallback);
            case ENTER_OTA:
                return new EnterOtaNode(mStatus, mHandler, mCallback);
            case CONDITION:
                return new VehicleConditionNode(mStatus, mHandler, mCallback);
            case INSTALL:
                return new InstallNode(mStatus, mHandler, mContext, mCallback);
            case EXIT_OTA:
                return new ExitOtaNode(mStatus, mHandler, mCallback);
            default:
                return null;
        }
    }

    public SetTimeNode getSetTime(long time) {
        return new SetTimeNode(mStatus, mHandler, mCallback, time);
    }

    public IHolder getFactory() {
        List<INode> list = new ArrayList<>();
        list.add(getNode(EventType.ENTER_OTA));
        list.add(getNode(EventType.CHECK));
        list.add(getNode(EventType.DOWNLOAD));
        list.add(getNode(EventType.CONDITION));
        list.add(getNode(EventType.TASK_VERIFY));
        list.add(getNode(EventType.INSTALL));
        return new NodeHolder(list);
    }

    public IHolder getSchedule() {
        List<INode> list = new ArrayList<>();
        list.add(getNode(EventType.ENTER_OTA));
        list.add(getNode(EventType.CONDITION));
        list.add(getNode(EventType.TASK_VERIFY));
        list.add(getNode(EventType.INSTALL));
        return new NodeHolder(list);
    }

    public IHolder getUpgradeNow() {
        List<INode> list = new ArrayList<>();
        list.add(getNode(EventType.ENTER_OTA));
        list.add(getNode(EventType.CONDITION));
        list.add(getNode(EventType.TASK_VERIFY));
        list.add(getNode(EventType.INSTALL));
        return new NodeHolder(list);
    }
}
