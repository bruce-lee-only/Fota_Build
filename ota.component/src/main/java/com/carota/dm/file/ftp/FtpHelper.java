package com.carota.dm.file.ftp;


import com.carota.dm.down.VerifyProgressInputStream;
import com.carota.dm.down.IVerifyCallback;
import com.momock.util.Logger;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;

import java.io.IOException;
import java.io.InputStream;
import java.net.SocketException;

public class FtpHelper {
    private final String ip;
    private final int port;
    private final String userName;
    private final String password;
    private final FtpClientExtend mFtpClient = new FtpClientExtend();
    private final int timeout = 60000;

    public FtpHelper(String ip, int port, String username, String password) {
        this.ip = ip;
        this.port = port;
        this.userName = username;
        this.password = password;
        mFtpClient.setControlEncoding("UTF-8");
        mFtpClient.setDataTimeout(timeout);
    }

    /**
     * 连接
     *
     * @return 是否连接成功
     */
    public boolean connect() {
        int num = 0;
        while (num < 3) {
            num++;
            try {
                disconnect();
                mFtpClient.connect(ip, port);
                if (FTPReply.isPositiveCompletion(mFtpClient.getReplyCode())) {
                    if (mFtpClient.login(userName, password)) {
                        Logger.info("DM-FTP Service Login success");
                        return true;
                    } else {
                        Logger.info("DM-FTP Service Login Fail");
                    }
                }
            } catch (Exception e) {
                Logger.error(e);
            }
            disconnect();
        }
        return false;
    }

    /**
     * 是否连接,断开连接则重试3次
     *
     * @return
     */
    public boolean checkConnect() {
        try {
            mFtpClient.pasv();
        } catch (Exception e) {
            Logger.error("socket exception");
            return connect();
        }
        return mFtpClient.isConnected() || connect();
    }

    /**
     * 断开连接
     */
    private void disconnect() {
        if (mFtpClient.isConnected()) {
            try {
                mFtpClient.disconnect();
            } catch (Exception e) {
                Logger.error(e);
            }
        }
    }

    public void clearAll() {
        try {
            if (!checkConnect()) {
                throw new SocketException("FTP Service not Connect");
            }
            FTPFile[] files = mFtpClient.listFiles();
            for (FTPFile file : files) {
                mFtpClient.deleteFile(file.getName());
            }
        } catch (Exception e) {
            Logger.error(e);
            disconnect();
        }
    }


    /**
     * 获取文件大小
     *
     * @param name
     * @return
     */
    public long getFileSize(String name) {
        try {
            if (!checkConnect()) {
                throw new SocketException("FTP Service not Connect");
            }
            FTPFile[] ftpFiles = mFtpClient.listFiles(name);
            return ftpFiles.length == 0 ? 0 : ftpFiles[0].getSize();
        } catch (Exception e) {
            Logger.error(e);
            disconnect();
        }
        return 0;
    }

    /**
     * 获取文件list Name
     *
     * @return
     */
    public String[] listFiles() {
        try {
            if (!checkConnect()) {
                throw new SocketException("FTP Service not Connect");
            }
            FTPFile[] files = mFtpClient.listFiles();
            String[] strings = new String[files.length];
            for (int i = 0; i < files.length; i++) {
                strings[i] = files[i].getName();
            }
            return strings;
        } catch (Exception e) {
            Logger.error(e);
            disconnect();
        }
        return new String[0];
    }

    public boolean deleteFile(String name) {
        try {
            if (!checkConnect()) {
                throw new SocketException("FTP Service not Connect");
            }
            return mFtpClient.deleteFile(name);
        } catch (Exception e) {
            Logger.error(e);
            disconnect();
        }
        return false;
    }

    public long calcUseSpace() {
        long useSpace = Long.MAX_VALUE;
        try {
            useSpace = mFtpClient.calcUseSpace();
            Logger.info("DM-FTP Used Space is : %d", useSpace);
        } catch (IOException e) {
            Logger.error(e);
            disconnect();
        }
        return useSpace;
    }


    public boolean existsFile(String name) {
        try {
            if (!checkConnect()) {
                throw new SocketException("FTP Service not Connect");
            }
            return mFtpClient.listFiles(name).length > 0;
        } catch (Exception e) {
            Logger.error(e);
            disconnect();
        }
        return false;
    }

    public boolean renameFile(String name, String renameTo) {
        try {
            if (!checkConnect()) {
                throw new SocketException("FTP Service not Connect");
            }
            mFtpClient.rename(name, renameTo);
            return mFtpClient.listFiles(renameTo).length > 0;
        } catch (Exception e) {
            Logger.error(e);
            disconnect();
        }
        return false;
    }

    /**
     * 获取文件输入流
     *
     * @param name
     * @return
     */
    public InputStream getFileInputStream(String name) {
        try {
            if (!checkConnect()) {
                throw new SocketException("FTP Service not Connect");
            }
            FTPFile[] file = mFtpClient.listFiles(name);
            if (file.length > 0) {
                return new FtpInputStream(mFtpClient, mFtpClient.retrieveFileStream(name));
            } else {
                Logger.info("DM-FTP Not find file@%s", name);
            }
        } catch (Exception e) {
            Logger.error(e);
            disconnect();
        }
        return null;
    }

    /**
     * 文件上传
     *
     * @param buffer
     * @param length
     * @return
     */
    public boolean upload(byte[] buffer, int off, int length) throws Exception {
        return mFtpClient.write(buffer, off, length);
    }

    public boolean uploadInit(String name, long startIndex, long fileLenth) {
        try {
            if (!checkConnect()) {
                throw new SocketException("FTP Service not Connect");
            }
            mFtpClient.init(name);
            return true;
        } catch (Exception e) {
            Logger.error(e);
            disconnect();
        }
        return false;
    }

    public void release() {
        try {
            mFtpClient.release();
        } catch (Exception e) {
            Logger.error(e);
            disconnect();
        }
    }


    public boolean verifyMd5(String name, String md5, IVerifyCallback callback) {
        try {
            if (!checkConnect()) {
                throw new SocketException("FTP Service not Connect");
            }
            FTPFile[] file = mFtpClient.listFiles(name);
            if (file.length > 0) {
                FtpInputStream inputStream = new FtpInputStream(mFtpClient, mFtpClient.retrieveFileStream(name));
                return new VerifyProgressInputStream(inputStream, file[0].getSize(), callback).calcMd5(md5);
            } else {
                Logger.info("DM-FTP Not find file@%s", name);
            }
        } catch (Exception e) {
            Logger.error(e);
            disconnect();
        }
        return false;
    }
}