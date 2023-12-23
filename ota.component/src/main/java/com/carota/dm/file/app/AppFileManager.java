package com.carota.dm.file.app;


import com.carota.dm.down.VerifyProgressInputStream;
import com.carota.dm.down.IVerifyCallback;
import com.carota.dm.file.IFileManager;
import com.momock.util.FileHelper;
import com.momock.util.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;

/**
 * 适配AppManager apk下载器
 */
public class AppFileManager implements IFileManager {

    protected final File mWorkDir;
    private final String mTag;
    private RandomAccessFile tmpAccessFile;

    public AppFileManager(File workDir, String tag) {
        this.mWorkDir = workDir;
        this.mTag = tag;
    }

    /**
     * 获取下载器标志
     *
     * @return
     */
    @Override
    public String getTag() {
        return mTag;
    }

    /**
     * 初始化接口，用于初始化某些本地参数或者准备远程链接
     */
    @Override
    public void init() {

    }

    @Override
    public void clearDm() {
        FileHelper.delete(mWorkDir);
    }

    /**
     * 获取输出流
     *
     * @param name
     * @return
     */
    @Override
    public InputStream findFileInputStream(String name) {
        try {
            File file = new File(mWorkDir, name);
            if (file.exists() && file.isFile()) {
                return new FileInputStream(file);
            } else {
                Logger.info("DM-%s Not find File@%s", mTag, name);
            }
        } catch (Exception e) {
            Logger.error(e);
        }
        return null;
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
        try {
            File file = new File(mWorkDir, name);
            return new VerifyProgressInputStream(file, callback).calcMd5(md5);
        } catch (Exception e) {
            Logger.error(e);
        }
        return false;
    }

    /**
     * 获取文件长度
     *
     * @param name
     * @return
     */
    @Override
    public long findFileLength(String name) {
        return new File(mWorkDir, name).length();
    }

    /**
     * 获取所有DM下的文件名
     *
     * @return
     */
    @Override
    public String[] getDmAllFilesName() {
        return mWorkDir.list();
    }

    /**
     * 删除指定文件
     *
     * @param name
     */
    @Override
    public void deleteFile(String name) {
        FileHelper.delete(new File(mWorkDir, name));
    }

    /**
     * 获取可用空间
     *
     * @return
     */
    @Override
    public long calcAvailSpace() {
        return mWorkDir.getFreeSpace();
    }

    /**
     * 是否存在File
     *
     * @param name
     * @return
     */
    @Override
    public boolean existsFile(String name) {
        File file = new File(mWorkDir, name);
        return file.exists();
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
        return new File(mWorkDir, name).renameTo(new File(mWorkDir, renameTo));
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
        File file = createFile(name);
        if (file != null) {
            tmpAccessFile = new RandomAccessFile(file, "rw");
            tmpAccessFile.seek(startIndex);
            return true;
        }
        return false;
    }

    /**
     * 开始下载传输
     *
     * @param buffer
     * @param off
     * @param length
     * @throws IOException
     */
    @Override
    public void downloadWrite(byte[] buffer, int off, int length) throws Exception {
        tmpAccessFile.write(buffer, off, length);
    }

    /**
     * 释放下载资源，关闭流等
     */
    @Override
    public void downloadRelease() {
        if (tmpAccessFile != null) {
            try {
                tmpAccessFile.close();
            } catch (IOException e) {
                Logger.error(e);
            }
        }
        tmpAccessFile = null;
    }

    private File createFile(String name) throws IOException {
        File file = new File(mWorkDir, name);
        if (!file.exists()) {
            FileHelper.mkdirForFile(file);
            if (!file.createNewFile()) {
                Logger.info("DM-AFM createNewFile Fail");
                return null;
            }
        }
        return file;
    }

}
