package com.carota.dm.down;


import com.momock.util.EncryptHelper;
import com.momock.util.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class VerifyProgressInputStream extends InputStream {
    private final InputStream in;
    private final long length;
    private long downlength;
    private final IVerifyCallback mCallBack;

    public VerifyProgressInputStream(InputStream inputStream, long length, IVerifyCallback callback) {
        if (inputStream == null) throw new NullPointerException("ProgressInputStream is null");
        in = inputStream;
        this.length = length;
        mCallBack = callback;
        downlength = 0L;
    }

    public VerifyProgressInputStream(File file, IVerifyCallback callback) throws FileNotFoundException {
        this(new FileInputStream(file), file.length(), callback);
    }

    public int read() throws IOException {
        int ret = in.read();
        updatePro(ret);
        return ret;
    }


    public int read(byte[] b) throws IOException {
        return read(b, 0, b.length);
    }


    public int read(byte[] b, int off, int len) throws IOException {
        int ret = in.read(b, off, len);
        updatePro(ret);
        if (ret < 0) {
            close();
        }
        return ret;
    }


    public long skip(long n) throws IOException {
        downlength = n;
        return in.skip(n);
    }


    public int available() throws IOException {
        return in.available();
    }


    public synchronized void mark(int readlimit) {
        in.mark(readlimit);
    }


    public synchronized void reset() throws IOException {
        downlength = 0;
        in.reset();
    }

    @Override
    public void close() throws IOException {
        in.close();
    }

    public boolean markSupported() {
        return in.markSupported();
    }


    private void updatePro(int ret) {
        if (mCallBack == null) return;
        if (length <= 0) return;
        downlength += ret;
        if (ret < 0) {
            downlength = length;
        }
        mCallBack.verify(downlength, length);
    }

    /**
     * @param md5
     * @return
     */
    public boolean calcMd5(String md5) {
        boolean success = false;
        try {
            String fileMd5 = EncryptHelper.calcFileMd5(this);
            success = md5.equalsIgnoreCase(fileMd5);
        } catch (Exception e) {
            Logger.error(e);
        }
        try {
            close();
        } catch (IOException e) {
            Logger.error(e);
        }
        return success;
    }

}
