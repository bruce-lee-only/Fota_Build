package com.carota.dm.file.ftp;

import com.momock.util.Logger;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPCmd;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPFileFilter;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicBoolean;

public class FtpClientExtend extends FTPClient {

    private Socket mSocket;
    private OutputStream mOutputStream;
    private AtomicBoolean isLock = new AtomicBoolean(false);
    private static final String IGNORE = ".log";


    public boolean init(String name) throws Exception {
        openSocket(name);
        if (mSocket == null || mOutputStream == null) {
            release();
            return false;
        }
        isLock.set(true);
        return true;
    }

    private void openSocket(String name) throws IOException {
        setFileType(FTP.BINARY_FILE_TYPE);
        enterLocalPassiveMode();
        mSocket = _openDataConnection_(FTPCmd.APPE.getCommand(), name);
        if (mSocket != null) {
            mOutputStream = mSocket.getOutputStream();
        }
    }

    public boolean write(byte[] buffer, int off, int length) throws IOException {
        if (mOutputStream != null) {
            mOutputStream.write(buffer, off, length);
            mOutputStream.flush();
            return true;
        }
        return false;
    }

    public boolean release() throws Exception {
        if (mOutputStream != null) mOutputStream.close();
        if (mSocket != null) mSocket.close();
        mOutputStream = null;
        mSocket = null;
        isLock.set(false);
        return completePendingCommand();
    }

    @Override
    public FTPFile[] listFiles() throws IOException {
        FTPFile[] ftpFiles = super.listFiles(null, file -> !file.getName().endsWith(IGNORE));
        return ftpFiles == null ? new FTPFile[0] : ftpFiles;
    }

    @Override
    public FTPFile[] listFiles(String pathname) throws IOException {
        FTPFile[] ftpFiles = super.listFiles(null, file -> file.getName().equals(pathname));
        return ftpFiles == null ? new FTPFile[0] : ftpFiles;
    }


    @Override
    public boolean deleteFile(String pathname) throws IOException {
        return pathname.endsWith(IGNORE) || super.deleteFile(pathname);
    }

    @Override
    public boolean rename(String from, String to) throws IOException {
        return super.rename(from, to);
    }

    @Override
    public InputStream retrieveFileStream(String remote) throws IOException {
        setFileType(FTP.BINARY_FILE_TYPE);
        enterLocalPassiveMode();
        return super.retrieveFileStream(remote);
    }

    private boolean getIsLock() {
        if (isLock.get()) {
            Logger.error("FTP Stream is Used");
        }
        return isLock.get();
    }

    public long calcUseSpace() throws IOException {
        FTPFile[] ftpFiles = listFiles(null, file -> true);
        long useSpace = 0;
        if (ftpFiles != null) {
            for (FTPFile file : ftpFiles) {
                useSpace += file.getSize();
            }
        }
        return useSpace;
    }
}
