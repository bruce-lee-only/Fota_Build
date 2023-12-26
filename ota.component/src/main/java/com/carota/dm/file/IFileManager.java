package com.carota.dm.file;

import com.carota.dm.down.IVerifyCallback;

import java.io.IOException;
import java.io.InputStream;

/**
 * 文件管理接口
 * 用于扩展管理下载文件位置不同的复杂场景
 */
public interface IFileManager {

    /**
     * 获取下载器标志
     *
     * @return
     */
    String getTag();

    /**
     * 初始化接口，用于初始化某些本地参数或者准备远程链接
     */
    void init();

    /**
     * 清空下载目录
     */
    void clearDm();

    /**
     * 获取输出流
     *
     * @param name
     * @return
     */
    InputStream findFileInputStream(String name);

    /**
     * 校验文件md5
     *
     * @param name
     * @param md5
     * @param callback
     * @return
     */
    boolean verifyMd5(String name, String md5, IVerifyCallback callback);

    /**
     * 获取文件长度
     *
     * @param name
     * @return
     */
    long findFileLength(String name);

    /**
     * 获取所有DM下的文件名
     *
     * @return
     */
    String[] getDmAllFilesName();

    /**
     * 删除指定文件
     *
     * @param name
     */
    void deleteFile(String name);

    /**
     * 获取可用空间
     *
     * @return
     */
    long calcAvailSpace();

    /**
     * 是否存在File
     *
     * @param name
     * @return
     */
    boolean existsFile(String name);

    /**
     * 文件重命名
     *
     * @param name     原始文件
     * @param renameTo 命名后文件
     * @return
     */
    boolean renameFile(String name, String renameTo);

    /**
     * 下载准备，创建文件流或者准备远端写入流
     *
     * @param name       写入文件的名字，没有文件创建文件，有的话在文件末尾追加文件
     * @param startIndex 开始下载的位置
     * @param fileLength
     * @return
     */
    boolean downloadInit(String name, long startIndex, long fileLength) throws Exception;

    /**
     * 开始下载传输
     *
     * @param buffer
     * @param off
     * @param length
     * @throws IOException
     */
    void downloadWrite(byte[] buffer, int off, int length) throws Exception;

    /**
     * 释放下载资源，关闭流等
     */
    void downloadRelease();
}