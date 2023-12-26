package com.carota.dm.down;

import android.os.SystemClock;

import com.carota.dm.file.IFileManager;
import com.carota.util.HttpHelper;
import com.momock.util.Logger;

import java.io.InputStream;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 文件加载控制器，只负责下载，不对文件进行处理
 */
public class FileDownloader implements IFileDownloader {
    private final String mUrl;
    private final String mFileName;
    private final int mMaxRetry;
    private final long mLimitSpeed;
    private final IDownCallback mCallback;
    private final IFileManager mFileManager;

    private final AtomicBoolean isCancel = new AtomicBoolean(false);

    private final AtomicBoolean runing = new AtomicBoolean(false);

    private HttpHelper.DownloadCall mCall = null;

    public static final int DEFAULT_BUFFER_SIZE = 8 * 1024;

    /**
     * @param url        文件下载路径
     * @param name       文件名，即要下载保存到本地的文件名，这里外部输入什么内部就是什么不做处理，需要外部管理tmp
     * @param manager    文件管理器
     * @param retry      重试次数
     * @param limitSpeed 限制下载速度，B/s
     * @param callback   下载速度，进度回调
     */
    public FileDownloader(String url, String name, IFileManager manager,
                          int retry, long limitSpeed, IDownCallback callback) {
        this.mUrl = url;
        this.mFileName = name;
        this.mMaxRetry = retry;
        this.mLimitSpeed = limitSpeed;
        this.mCallback = callback;
        this.mFileManager = manager;
    }


    @Override
    public synchronized int start() {
        if (isRun()) return CODE_RUNING;
        //重置所有状态
        clearStatus();
        runing.set(true);
        int code = CODE_ERROE;
        try {
            Logger.info("DM-FD Start Download File %s", mFileName);

            long downloadFileLength = getDownloadFileLength();
            if (downloadFileLength < 1) {
                clearStatus();
                return CODE_NO_LENGTH;
            }
            if (isDownloadSuccess(downloadFileLength)) {
                clearStatus();
                return CODE_SUCCESS;
            }
            code = resumeDownload(downloadFileLength);
        } catch (Exception e) {
            Logger.error(e);
        }
        clearStatus();
        return code;
    }

    @Override
    public void stop() {
        if (isRun()) {
            isCancel.set(true);
            while (isRun()) {
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    Logger.error(e);
                }
            }
            Logger.error("DM-FD Stop Download File %s", mFileName);
        } else {
            Logger.error("DM-FD Not run ，Stoped Download File %s", mFileName);
        }

    }

    @Override
    public boolean isRun() {
        return runing.get();
    }

    private int resumeDownload(long downloadFileLength) throws InterruptedException {
        int retryNum = 0;
        do {
            if (downloadFile(downloadFileLength, mFileManager.findFileLength(mFileName))) break;
            if (isCancel.get()) break;
            Logger.error("DM-FD Retry %d @%s", retryNum, mFileName);
            Thread.sleep(5000);
            retryNum++;
        } while (retryNum < mMaxRetry);
        if (isCancel.get()) {
            Logger.error("DM-FD Cancel @%s", mFileName);
            return CODE_CANCLE;
        } else if (isDownloadSuccess(downloadFileLength)) {
            return CODE_SUCCESS;
        } else if (retryNum < mMaxRetry) {
            Logger.error("DM-FD Error @%s", mFileName);
            return CODE_ERROE;
        } else {
            Logger.error("DM-FD Max Retry @%s", mFileName);
            return CODE_MAX_RETRY;
        }
    }

    private boolean isDownloadSuccess(long downloadFileLength) {
        if (downloadFileLength <= mFileManager.findFileLength(mFileName)) {
            Logger.info("DM-FD downloaded succcess @%s", mFileName);
            clearStatus();
            return true;
        }
        return false;
    }


    private long getDownloadFileLength() {
        long fileLength = HttpHelper.getFileLength(mUrl);
        Logger.info("DM-FD File length:%s @%s", String.valueOf(fileLength), mFileName);
        if (fileLength < 1) {
            clearStatus();
        }
        return fileLength;
    }

    private void clearStatus() {
        if (mCall != null) {
            mCall.close();
            mCall = null;
        }
        runing.set(false);
        isCancel.set(false);
    }

    private boolean downloadFile(long fileLength, long startIndex) {
        try {
            mCall = HttpHelper.downloadFile(startIndex, mUrl);
            InputStream needInputStream = mCall.getInputStream();
//            if (needInputStream != null) {
                if (mFileManager.downloadInit(mFileName, startIndex, fileLength)) {
                    //每次读取4Kb
                    byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
                    int length;

                    long progress = startIndex;
                    long startTime = SystemClock.elapsedRealtime();
                    long startPro = progress;

                    int num = 0;

                    while ((length = needInputStream.read(buffer)) > 0) {
                        if (isCancel.get()) {
                            Logger.error("DM-FD is cancel @%s", mFileName);
                            throw new Exception("Download Cancel");
                        }

                        mFileManager.downloadWrite(buffer, 0, length);
                        progress += length;
                        long endTime = SystemClock.elapsedRealtime();
                        if (endTime - startTime > 1000) {
                            mCallback.progress(
                                    (int) ((progress - startPro) * 1000 / (endTime - startTime)),
                                    progress,
                                    fileLength
                            );
                            startTime = endTime;
                            startPro = progress;
                        }
                        if (mLimitSpeed > 0 && ++num > 7) {
                            // 文件下载需要64kb暂停
                            Thread.sleep(mLimitSpeed);
                            num = 0;
                        }
                    }
                    mFileManager.downloadRelease();
                    mCall.close();
                    long realLength = mFileManager.findFileLength(mFileName);
                    Logger.error("DM-FD Download End %s %s @%s", String.valueOf(fileLength), String.valueOf(realLength), mFileName);
                    return fileLength <= realLength;
                } else {
                    Logger.error("DM-FD init FileManager Input Stream Error @%s", mFileName);
                }
//            } else {
//                Logger.error("DM-FD get Strem is null @%s", mFileName);
//            }
        } catch (Exception e) {
            Logger.error("DM-FD DownLoad Exception @%s", mFileName);
            Logger.error(e);
        }
        mFileManager.downloadRelease();
        mCall.close();
        return false;
    }
}