/*******************************************************************************
 * Copyright (C) 2018-2020 CAROTA Technology Crop. <www.carota.ai>.
 * All Rights Reserved.
 *
 * Unauthorized using, copying, distributing and modifying of this file,
 * via any medium is strictly prohibited.
 *
 * Proprietary and confidential.
 ******************************************************************************/

package com.momock.util;

import java.io.IOException;
import java.io.InputStream;

public class SubInputStream extends InputStream {

    private long mLengthLeft;
    private InputStream mStream;

    public SubInputStream(InputStream is, long offset, long length) throws IOException {
        mStream = is;
        if(offset > mStream.skip(offset)) {
            mLengthLeft = 0;
            // throw new IOException("Fail to set OFFSET");
        } else {
            mLengthLeft = length <= 0 ? -1 : length;
        }
    }

    @Override
    public int read() throws IOException {
        if(mLengthLeft == 0) {
            return -1;
        } else if(mLengthLeft > 0) {
            mLengthLeft--;
        }
        return mStream.read();
    }

    @Override
    public int read(byte[] b) throws IOException {
        return read(b, 0, b.length);
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        if(0 > mLengthLeft) {
            return mStream.read(b, off, len);
        } else if(0 == mLengthLeft) {
            return -1;
        } else if(mLengthLeft < off) {
            mStream.skip(mLengthLeft);
            mLengthLeft = 0;
            return -1;
        } else if(mLengthLeft < off + len) {
            long l = mLengthLeft - off;
            mLengthLeft = 0;
            return mStream.read(b, off, (int)l);
        } else {
            len = mStream.read(b, off, len);
            if(len > 0) {
                mLengthLeft -= len;
            }
            return len;
        }
    }

    @Override
    public long skip(long n) throws IOException {
        mLengthLeft -= n;
        return mStream.skip(n);
    }

    @Override
    public int available() throws IOException {
        int available = mStream.available();
        if(available > mLengthLeft) {
            return (int)mLengthLeft;
        }
        return available;
    }

    @Override
    public void close() throws IOException {
        mStream.close();
    }

    @Override
    public synchronized void mark(int readlimit) {
        mStream.mark(readlimit);
    }

    @Override
    public synchronized void reset() throws IOException {
        mStream.reset();
    }

    @Override
    public boolean markSupported() {
        return mStream.markSupported();
    }

    @Override
    public String toString() {
        return mStream.toString();
    }
}
