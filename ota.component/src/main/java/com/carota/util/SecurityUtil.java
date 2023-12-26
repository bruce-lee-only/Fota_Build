/*******************************************************************************
 * Copyright (C) 2018-2020 CAROTA Technology Crop. <www.carota.ai>.
 * All Rights Reserved.
 *
 * Unauthorized using, copying, distributing and modifying of this file,
 * via any medium is strictly prohibited.
 *
 * Proprietary and confidential.
 ******************************************************************************/

package com.carota.util;

import android.content.Context;
import android.os.Bundle;
import android.os.Environment;

import com.carota.mda.data.SecurityDataCache;
import com.carota.mda.remote.ActionDM;
import com.carota.mda.remote.info.MetaInfo;
import com.carota.mda.security.SecurityData;
import com.momock.util.EncryptHelper;
import com.momock.util.FileHelper;
import com.momock.util.JsonHelper;
import com.momock.util.Logger;
import com.momock.util.SubInputStream;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.nio.channels.Channels;
import java.util.Properties;


public class SecurityUtil {

    public interface ISecureMethod {
        byte[] calcSummary(InputStream is);

        boolean verify(byte[] summary, byte[] signature);
    }

    public static abstract class SecureMethod implements ISecureMethod {

        public byte[] calcSummary(InputStream is) {
            try {
                return EncryptHelper.calcFileSHA256(is);
            } catch (Exception e) {
                Logger.error(e);
            }
            return null;
        }
    }

    public static boolean verifyPackage(File file, ISecureMethod method) {
        try (RandomAccessFile raf = new RandomAccessFile(file, "r")) {
            // read sign length
            byte[] byteSignLen = new byte[4];
            raf.seek(raf.length() - byteSignLen.length);
            raf.read(byteSignLen);
            int signLen = byteArr2Int(byteSignLen);
            // Logger.debug("signature length = " + signatureLen);
            if (raf.length() <= signLen || signLen > 10240 || signLen < 0) {
                // here we set max length of signature is 10K.
                throw new IOException("Invalid Package Structure");
            }
            // read sign
            byte[] signature = new byte[signLen];
            long posSign = raf.length() - signLen - byteSignLen.length;
            raf.seek(posSign);
            raf.read(signature);
            // calc file summary with no sign
            raf.seek(0);
            InputStream is = Channels.newInputStream(raf.getChannel());
            byte[] summary = method.calcSummary(new SubInputStream(is, 0, posSign));
            Logger.debug("signature : " + new String(signature));
            return method.verify(summary, signature);
        } catch (Exception e) {
            Logger.error(e);
            return verifyPackageModify(file, method);
        }
    }

    public static boolean verifyPackages(File file, File signFile, ISecureMethod method) {
        try {
            SignParser signParser = new SignParser(signFile);
            Bundle bundle = signParser.parse();
            String signature = bundle.getString(SignParser.PROP_SIGN);
            InputStream is = new FileInputStream(file);
            byte[] summary = method.calcSummary(is);
            is.close();
            return method.verify(summary, signature.getBytes());
        } catch (Exception e) {
            Logger.error(e);
        }
        return false;
    }

    public static boolean verifyPackages(String dmHost, String targetId, File signFile, ISecureMethod method) {
        try {
            SignParser signParser = new SignParser(signFile);
            Bundle bundle = signParser.parse();
            String signature = bundle.getString(SignParser.PROP_SIGN);
            InputStream fisSummary = ActionDM.openInputStream(dmHost, targetId);
            byte[] summary = method.calcSummary(fisSummary);
            fisSummary.close();
            return method.verify(summary, signature.getBytes());
        } catch (Exception e) {
            Logger.error(e);
        }
        return false;
    }

    public static boolean verifyPackageModify(File file, ISecureMethod method) {
        try {
            FileInputStream fisSignLen = new FileInputStream(file);
            // read sign length
            byte[] byteSignLen = new byte[4];
            int skipLen = fisSignLen.available() - byteSignLen.length;
            fisSignLen.skip(skipLen);
            fisSignLen.read(byteSignLen);
            int signLen = byteArr2Int(byteSignLen);
            Logger.debug("skipLen:%1$d ; signLen:%2$d", skipLen, signLen);
            fisSignLen.close();
            FileInputStream fisSign = new FileInputStream(file);
            if (fisSign.available() <= signLen || signLen > 10240 || signLen < 0) {
                // here we set max length of signature is 10K.
                throw new IOException("Invalid Package Structure");
            }
            // read sign
            byte[] signature = new byte[signLen];
            long posSign = fisSign.available() - signLen - byteSignLen.length;
            fisSign.skip(posSign);
            fisSign.read(signature);
            fisSign.close();
            Logger.debug("posSign:%d", posSign);
            // calc file summary with no sign
            FileInputStream fisSummary = new FileInputStream(file);
            InputStream is = Channels.newInputStream(fisSummary.getChannel());
            byte[] summary = method.calcSummary(new SubInputStream(is, 0, posSign));
            fisSummary.close();
            return method.verify(summary, signature);
        } catch (Exception e) {
            Logger.error(e);
        }
        return false;
    }

    public static String findSignFile(String targetId, File path, String dmHost, String type) {
        try {
            File sFile = new File(path, targetId);
            if (!sFile.exists() || !sFile.isFile()) {
                InputStream metaStream = ActionDM.openInputStream(dmHost, targetId);
                File file = new File(path, "sign");
                FileHelper.unzip(metaStream, file);
                metaStream.close();
            } else {
                FileHelper.unzip(path + "/" + targetId, path + "/sign");
            }
            File jsonFile = new File(path, "/sign/manifest.json");
            if (!jsonFile.exists()) {
                jsonFile = new File(path, "/sign/meta.json");
            }
            MetaInfo metaInfo = new MetaInfo(JsonHelper.parseObject(FileHelper.readText(jsonFile)));
            return metaInfo.getValue(type);
        } catch (Exception e) {
            Logger.error(e);
        }
        return null;
    }

    private static int byteArr2Int(byte[] arr) {
        int x1 = (arr[0] & 0xff) << 24;
        int x2 = (arr[1] & 0xff) << 16;
        int x3 = ((arr[2] & 0xff) << 8);
        int x4 = (arr[3] & 0xff);
        return x1 | x2 | x3 | x4;
    }
}
