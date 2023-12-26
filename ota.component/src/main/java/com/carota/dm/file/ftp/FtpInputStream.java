package com.carota.dm.file.ftp;

import org.apache.commons.net.ftp.FTPClient;

import java.io.IOException;
import java.io.InputStream;

class FtpInputStream extends InputStream {

    private FTPClient mFTPClient;
    private InputStream mInputStream;

    FtpInputStream(FTPClient ftpClient, InputStream inputStream) throws IOException {
        if (ftpClient == null) throw new IllegalArgumentException("FTPClient is Null");
        mFTPClient = ftpClient;
        mInputStream = inputStream;
        if (mInputStream == null) {
            close();
        }
    }

    public int read() throws IOException {
        int ret = mInputStream.read();
        if (ret < 0) {
            close();
        }
        return ret;
    }


    public int read(byte[] b) throws IOException {
        return read(b, 0, b.length);
    }


    public int read(byte[] b, int off, int len) throws IOException {
        int ret = mInputStream.read(b, off, len);
        if (ret < 0) {
            close();
        }
        return ret;
    }


    public long skip(long n) throws IOException {
        return mInputStream.skip(n);
    }


    public int available() throws IOException {
        return mInputStream.available();
    }


    public synchronized void mark(int readlimit) {
        mInputStream.mark(readlimit);
    }


    public synchronized void reset() throws IOException {
        mInputStream.reset();
    }


    public boolean markSupported() {
        return mInputStream.markSupported();
    }


    @Override
    public void close() throws IOException {
        if (mInputStream != null) mInputStream.close();
        if (mFTPClient != null) mFTPClient.getReply();
        mInputStream = null;
        mFTPClient = null;
    }

}
