/*******************************************************************************
 * Copyright (C) 2018-2020 CAROTA Technology Crop. <www.carota.ai>.
 * All Rights Reserved.
 *
 * Unauthorized using, copying, distributing and modifying of this file,
 * via any medium is strictly prohibited.
 *
 * Proprietary and confidential.
 ******************************************************************************/

package com.carota.util;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.os.SystemClock;
import android.text.TextUtils;

import com.carota.CoreServer;
import com.carota.MainService;
import com.carota.OTAService;
import com.momock.util.EncryptHelper;
import com.momock.util.Logger;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;


public class MainServiceHolder implements ServiceConnection, Handler.Callback {

    private Messenger mRemote;
    private final Intent mIntent;
    private final Messenger mMessenger;
    private long mMsgCount;

    public MainServiceHolder(String packageName) {
        mIntent = new Intent("ota.intent.action.CORE").setPackage(packageName);
        mRemote = null;
        mMessenger = new Messenger(new Handler(Looper.getMainLooper(), this));
        mMsgCount = 0;
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        mRemote = new Messenger(service);
        sendEmptyMessage(MainService.MSG_START);
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        mRemote = null;
    }

    private boolean sendEmptyMessage(int what) {
        try {
            Message msg = Message.obtain();
            msg.what = what;
            mRemote.send(msg);
            return true;
        } catch (RemoteException e) {
            Logger.error(e);
        }
        return false;
    }

    public synchronized void ensureConnected(Context context) {
        if (null == mRemote) {
            if (context.bindService(mIntent, this, Service.BIND_AUTO_CREATE)) {
                try {
                    do {
                        Thread.sleep(1000);
                    } while (null == mRemote);
                } catch (InterruptedException ie) {
                    throw new RuntimeException("Interrupted @ Bind " + mIntent.getPackage());
                }
            } else {
                throw new RuntimeException("Permission DENY @ Bind " + mIntent.getPackage());
            }
        }
    }

    public void start(Context context) {
        OTAService.startService(context, mIntent);
    }

    public void addFileSync(Context context, File file, String originalChecksum, String secure) throws Exception {
        if(null == originalChecksum || null == secure) {
            throw new FileNotFoundException("Missing original file info");
        }
        ensureConnected(context);
        sendFileMessageSync(CoreServer.FILE_TYPE_XOR, originalChecksum, file, secure);
    }

    public String addFileSync(Context context, File file, String targetInPackage) throws Exception {
        String md5;
        if(null != targetInPackage) {
            try(ZipFile zf = new ZipFile(file)) {
                ZipEntry ze = zf.getEntry(targetInPackage);
                if (null == ze) {
                    throw new FileNotFoundException("File Not Found in ZIP");
                }
                md5 = EncryptHelper.calcFileMd5(zf.getInputStream(ze));
            }
        } else {
            md5 = EncryptHelper.calcFileMd5(file);
        }
        if(TextUtils.isEmpty(md5)) {
            throw new FileNotFoundException("Calc Target Checksum Failure");
        }
        ensureConnected(context);
        int type = null == targetInPackage ? CoreServer.FILE_TYPE_RAW : CoreServer.FILE_TYPE_ZIP;
        sendFileMessageSync(type, md5, file, targetInPackage);
        return md5;
    }

    private synchronized void sendFileMessageSync(int fileType, String md5, File target, String extra) throws Exception {
        long reqId = SystemClock.elapsedRealtime();
        Message msg = Message.obtain();
        msg.what = MainService.MSG_FILE;
        msg.arg1 = fileType;
        msg.replyTo = mMessenger;
        msg.obj = new ParcelableStringArray(new String[]{md5, target.getAbsolutePath(), extra}, reqId);
        mRemote.send(msg);
        do {
            Thread.sleep(1000);
        } while (mMsgCount > reqId);
    }

    @Override
    public boolean handleMessage(Message msg) {
        long reqId = 0;
        if(msg.obj instanceof ParcelableStringArray) {
            ParcelableStringArray psa = (ParcelableStringArray)msg.obj;
            reqId = psa.arg;
        }
        if(reqId > mMsgCount) {
            mMsgCount = reqId;
        }
        return true;
    }
}
