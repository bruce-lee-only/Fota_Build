/*******************************************************************************
 * Copyright (C) 2018-2020 CAROTA Technology Crop. <www.carota.ai>.
 * All Rights Reserved.
 *
 * Unauthorized using, copying, distributing and modifying of this file,
 * via any medium is strictly prohibited.
 *
 * Proprietary and confidential.
 ******************************************************************************/

package com.carota.core.offline;

import android.os.Build;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * 字节码处理
 */
public class ByteHelper {
    /**
     * String 转byte[]
     * @param string UTF-8编码格式
     * @return
     */
    public static byte[] stringToByteArray(String string) {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            return string.getBytes(StandardCharsets.UTF_8);
        } else {
            return string.getBytes(Charset.forName("UTF-8"));
        }
    }

    /**
     *  byte[]转String
     * @param bytes
     * @return
     */
    public static String byteArrayToString(byte[] bytes) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            return new String(bytes, StandardCharsets.UTF_8);
        } else {
            return new String(bytes, Charset.forName("UTF-8"));
        }
    }

    /**
     * 一个字节的高四位与低四位交换
     * @param srcByte
     * @return
     */
    public static byte bitSwrap(byte srcByte){
        int bytes = srcByte & 0xff;
        return (byte) (((bytes & 0x0f) << 4 | bytes >> 4) & 0xFF);
    }

    /**
     * 异或
     * @return
     */
    public static byte xor (byte srcByte,byte keyByte) {
        return (byte) (srcByte^keyByte);
    }


    /**
     * 异或
     * @return
     */
    public static int xor (int srcByte,int keyByte) {
        return srcByte^keyByte;
    }
}
