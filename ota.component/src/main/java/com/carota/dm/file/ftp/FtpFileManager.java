package com.carota.dm.file.ftp;

import com.carota.build.ParamDM;
import com.carota.dm.down.IVerifyCallback;
import com.carota.dm.file.IFileManager;
import com.momock.util.EncryptHelper;
import com.momock.util.Logger;

import java.io.IOException;
import java.io.InputStream;

/**
 * FTP文件管理器
 * 一般用于适配Android+Qnx,解决多端存储问题
 */
public class FtpFileManager implements IFileManager {

    private final FtpHelper ftpHelper;
    private static FtpFileManager mInstance;
    private final String mTag;
    private final long mMaxSpace;

    public static synchronized FtpFileManager newInstance(ParamDM.Info info) {
        if (mInstance == null) {
            String md5 = info.getUsername().concat(String.valueOf(info.getPort()));
            try {
                md5 = EncryptHelper.calcFileMd5(info.getUsername().getBytes());
            } catch (Exception e) {
                Logger.error(e);
            }
            //初始化FtpFileManager
            mInstance = new FtpFileManager(info.getAddr(), info.getPort(), info.getUsername(),
                    md5, info.getType(), info.getMaxSpace());
        }
        return mInstance;
    }

    private FtpFileManager(String ip, int port, String username, String passward, String tag, long maxSpace) {
        mTag = tag;
        ftpHelper = new FtpHelper(ip, port, username, passward);
        mMaxSpace = maxSpace;
    }

    @Override
    public String getTag() {
        return mTag;
    }

    /**
     * 初始化接口，用于初始化某些本地参数或者准备远程链接
     */
    @Override
    public void init() {
        ftpHelper.connect();
    }

    /**
     * 清空下载目录
     */
    @Override
    public void clearDm() {
        ftpHelper.clearAll();
    }

    /**
     * 获取输出流
     *
     * @param name
     * @return
     */
    @Override
    public InputStream findFileInputStream(String name) {
        return ftpHelper.getFileInputStream(name);
    }

    /**
     * 校验文件md5
     *
     * @param name
     * @param md5
     * @param callback
     * @return
     */
    @Override
    public boolean verifyMd5(String name, String md5, IVerifyCallback callback) {
        if (md5 == null) return true;
        return ftpHelper.verifyMd5(name, md5, callback);
    }

    /**
     * 获取文件长度
     *
     * @param name
     * @return
     */
    @Override
    public long findFileLength(String name) {
        return ftpHelper.getFileSize(name);
    }

    /**
     * 获取所有DM下的文件名
     *
     * @return
     */
    @Override
    public String[] getDmAllFilesName() {
        return ftpHelper.listFiles();
    }

    /**
     * 删除指定文件
     *
     * @param name
     */
    @Override
    public void deleteFile(String name) {
        ftpHelper.deleteFile(name);
    }

    /**
     * 获取可用空间
     *
     * @return
     */
    @Override
    public long calcAvailSpace() {
        try {
            return mMaxSpace - ftpHelper.calcUseSpace();
        } catch (Exception e) {
            Logger.error(e);
        }
        return -1L;
    }

    /**
     * 是否存在File或者tmp
     *
     * @param name
     * @return
     */
    @Override
    public boolean existsFile(String name) {
        return ftpHelper.existsFile(name);
    }

    /**
     * 文件重命名
     *
     * @param name     原始文件
     * @param renameTo 命名后文件
     * @return
     */
    @Override
    public boolean renameFile(String name, String renameTo) {
        return ftpHelper.renameFile(name, renameTo);
    }

    /**
     * 下载准备，创建文件流或者准备远端写入流
     *
     * @param name       写入文件的名字，没有文件创建文件，有的话在文件末尾追加文件
     * @param startIndex 开始下载的位置
     * @param fileLength
     * @return
     */
    @Override
    public boolean downloadInit(String name, long startIndex, long fileLength) throws Exception {
        return ftpHelper.uploadInit(name, startIndex, fileLength);
    }

    /**
     * 开始下载传输
     *
     * @param buffer
     * @param off
     * @param length
     */
    @Override
    public void downloadWrite(byte[] buffer, int off, int length) throws Exception {
        ftpHelper.upload(buffer, off, length);
    }

    /**
     * 释放下载资源，关闭流等
     */
    @Override
    public void downloadRelease() {
        ftpHelper.release();
    }

}