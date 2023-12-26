/*******************************************************************************
 * Copyright (C) 2018-2020 CAROTA Technology Crop. <www.carota.ai>.
 * All Rights Reserved.
 *
 * Unauthorized using, copying, distributing and modifying of this file,
 * via any medium is strictly prohibited.
 *
 * Proprietary and confidential.
 ******************************************************************************/

package com.carota.rsm;

import android.text.TextUtils;

import com.momock.util.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

public class XorInputStream extends FileInputStream{
    private static String FILE_MD5;
    private boolean SKIP = false;
    private long SKIP_STEP = 0;
    private static long KEY_LEN = 0;
    private static long READ_STEP = 0;


    public XorInputStream(String name) throws FileNotFoundException {
        this(name != null ? new File(name) : null, null);
        reset();
    }

    public XorInputStream(File file, String md5) throws FileNotFoundException {
        super(file);
        reset();
        if(file != null && !TextUtils.isEmpty(md5)) {
            FILE_MD5 = md5;
            KEY_LEN = md5.length();
        } else {
            throw new RuntimeException("file not exist");
        }
        Logger.debug("FILE_MD5 = " + FILE_MD5);
    }

    @Override
    public int read(byte[] b) throws IOException {
        if(b != null) {
            Logger.debug("b = " + b.length);
        }
        return read(b, 0, b.length);
    }

    @Override
    public int read() throws IOException {
        // Android-changed: Read methods delegate to read(byte[], int, int) to share Android logic.
        byte[] b = new byte[1];
        return (read(b, 0, 1) != -1) ? b[0] & 0xff : -1;
    }

    @Override
    public int read(byte b[], int off, int len) throws IOException {
        int len_temp;
        if(len <=FILE_MD5.length()){
            len_temp = len;
        }else{
            len_temp  = (len/FILE_MD5.length())*FILE_MD5.length();
        }
        int res = super.read(b, off, len_temp);

        if(res == -1) return res;
        byte[] keys = stringToByteArray(FILE_MD5);
        long index;
        if(SKIP){
            for(int i=0;i<res;i++){
                index = READ_STEP + SKIP_STEP + i;//keys下表skip(SKIP_STEP)相同长度
                b[i] ^= keys[(int) (index % keys.length)];
            }
        }else{
            for(int i = 0; i < res; i++){
                index = READ_STEP + i;
                b[i] ^= keys[(int) (index % keys.length)];
            }
        }
        READ_STEP += len_temp;
//        Logger.info("FILE_MD5:" + FILE_MD5 + " ;len:" + len + " ; res = " + res);
        return res;
    }

    @Override
    public long skip(long n) throws IOException {
        SKIP = true;
        SKIP_STEP += n;
        return super.skip(n);
    }

    @Override
    public void close() throws IOException {
        reset();
        super.close();
    }

    public void reset(){
        SKIP_STEP = 0;
        SKIP = false;
        READ_STEP = 0;
    }

    public static byte[] stringToByteArray(String string) {
        try{
            return string.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }
}
