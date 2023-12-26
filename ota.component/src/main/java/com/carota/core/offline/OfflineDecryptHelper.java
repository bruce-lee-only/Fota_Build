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

import com.momock.util.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;


/**
 * 文件解密
 */
public class OfflineDecryptHelper {

    /**
     * 字符串倒叙翻转
     *
     * @return
     */
    private static String stringReverse(String srcString) {
        StringBuilder buf = new StringBuilder(srcString);
        return String.valueOf(buf.reverse());
    }


    /**
     * 加密文件
     *
     * @param encFile 加密文件
     */
    public static void encFile(File encFile, String info, String key) {
        if(key == null || key.length() != 32) {
            throw new IllegalArgumentException("key error");
        }
        try{
            if(encFile.exists()) {
                encFile.delete();
            }
            encFile.createNewFile();
            FileOutputStream outputStream = new FileOutputStream(encFile, true);
            outputStream.write(encKey(key));
            outputStream.write(encInfo(key, info));
            outputStream.close();
        } catch (IOException e) {
            Logger.error(e);
            System.out.println("error：" + e.toString());
        }
    }

    /**
     * 加密key
     *
     * @param key 要加密的key
     * @return 加密后的key
     */
    private static byte[] encKey(String key) {
        byte[] bytes = ByteHelper.stringToByteArray(stringReverse(key));
        byte[] result = new byte[bytes.length];
        for(int i = 0; i < bytes.length; i++){
            result[i] = ByteHelper.bitSwrap(bytes[i]);
        }
        return result;
    }

    /**
     * 加密内容
     *
     * @param key
     * @param info
     */
    private static byte[] encInfo(String key, String info) {
        byte[] keys = ByteHelper.stringToByteArray(key);
        byte[] infos = ByteHelper.stringToByteArray(info);
        byte[] result = new byte[infos.length];
        byte keyByte;
        for(int i = 0; i < result.length; i++){
            keyByte = keys[i % keys.length];
            result[i] = ByteHelper.xor(infos[i], keyByte);
        }
        return result;
    }

    /**
     * 解密文件
     * @param encFile
     */
    public static String DecFile(File encFile) {
        long length = encFile.length();
        if(!encFile.exists() || length < 32) {
            return null;
        }
        try{
            FileInputStream inputStream = new FileInputStream(encFile);
            byte[] key = new byte[32];
            inputStream.read(key);

            byte[] info = new byte[(int) (length - 32)];
            inputStream.read(info);
            inputStream.close();
            listToArray(info, ByteHelper.stringToByteArray(decKey(key)));
            return ByteHelper.byteArrayToString(info);
        } catch (IOException e) {
            Logger.error(e);
        }
        return null;
    }

    private static void listToArray(byte[] data, byte[] key) {
        for(int i = 0; i < data.length; i++){
            data[i] = ByteHelper.xor(data[i], key[i % 32]);
        }
    }

    private static String decKey(byte[] decKey) {
        byte[] result = new byte[decKey.length];
        for(int i = 0; i < decKey.length; i++){
            result[i] = ByteHelper.bitSwrap(decKey[i]);
        }
        return stringReverse(ByteHelper.byteArrayToString(result));
    }



    public static String decryptConfigure(File file) {
        if(!file.exists()) return null;
        FileInputStream fis = null;
        try{
            fis = new FileInputStream(file);
            byte[] bKey = new byte[32];
            fis.read(bKey);
            String key = decKey(bKey);
            Logger.debug("key:" + key );
            byte[] keys = ByteHelper.stringToByteArray(key);
            byte[] b = new byte[fis.available()];
            int len = fis.read(b);
            for(int i = 0; i < len; i++){
                b[i] ^= keys[i % keys.length];
            }
            fis.close();
            return ByteHelper.byteArrayToString(b);
        } catch (Exception e) {
            Logger.error(e);
        }
        if (fis!=null) {
            try {
                fis.close();
            } catch (IOException e) {
                Logger.error(e);
            }
        }
        return null;
    }
}
