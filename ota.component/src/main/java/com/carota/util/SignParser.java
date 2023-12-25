package com.carota.util;

import android.os.Bundle;

import com.momock.util.FileHelper;
import com.momock.util.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

public class SignParser {
    private Properties mProp;
    private File mFile;
    private static final String PROP_TYPE = "type";
    public static final String PROP_SIGN = "sign";
    public static final String PROP_MODE = "mode";
    public static final String PROP_OPTION = "option";
    public static final String PROP_KEY = "key";
    public static final String PROP_KEY_LEN = "keylen";
    public static final String PROP_IV = "iv";
    public static final String PROP_PADDING = "padding";

    //Add for Type 3
    //描述
    public static final String PROP_DESC = "desc";
    //签名的编码，不区分大小写
    public static final String PROP_SIGN_ENCODE = "signencode";
    //证书文件的编码，不区分大小写
    public static final String PROP_CERT_ENCODE = "certencode";
    //证书文件下载地址
    public static final String PROP_CERT_URL = "certurl";
    //证书文件名，包含在ZIP包中（MD5）
    public static final String PROP_CERT_FILE = "certfile";
    //证书内容，字符形式，单行（不支持多行）
    public static final String PROP_CERT_BODY = "certbody";

    public SignParser(File file) throws IOException {
        mFile = file;
        mProp = new Properties();
        mProp.load(new FileInputStream(file));
    }

    public String getType() {
        return mProp.getProperty(PROP_TYPE, "");
    }

    public Bundle parse() {
        switch (getType()) {
            case "1":
                return parseType1();
            case "2":
                return parseType2();
            case "3":
                return parseType3();
            default:
                return parseDefault();
        }
    }

    private Bundle parseDefault() {
        Bundle bundle = new Bundle();
        try {
            bundle.putString(PROP_SIGN, FileHelper.readText(mFile));
        } catch (Exception e) {
            Logger.error(e);
        }
        return bundle;
    }

    private Bundle parseType1() {
        Bundle bundle = new Bundle();
        bundle.putString(PROP_SIGN, mProp.getProperty(PROP_SIGN));
        return bundle;
    }

    private Bundle parseType2() {
        Bundle bundle = new Bundle();
        bundle.putString(PROP_MODE, mProp.getProperty(PROP_MODE));
        bundle.putString(PROP_OPTION, mProp.getProperty(PROP_OPTION));
        bundle.putString(PROP_KEY, mProp.getProperty(PROP_KEY));
        bundle.putString(PROP_KEY_LEN, mProp.getProperty(PROP_KEY_LEN));
        bundle.putString(PROP_IV, mProp.getProperty(PROP_IV));
        bundle.putString(PROP_PADDING, mProp.getProperty(PROP_PADDING));
        bundle.putString(PROP_SIGN, mProp.getProperty(PROP_SIGN));
        return bundle;
    }

    //Add for Type 3
    private Bundle parseType3() {
        Bundle bundle = new Bundle();
        bundle.putString(PROP_DESC, mProp.getProperty(PROP_DESC));
        bundle.putString(PROP_SIGN_ENCODE, mProp.getProperty(PROP_SIGN_ENCODE));
        bundle.putString(PROP_SIGN, mProp.getProperty(PROP_SIGN));
        bundle.putString(PROP_CERT_ENCODE, mProp.getProperty(PROP_CERT_ENCODE));
        bundle.putString(PROP_CERT_URL, mProp.getProperty(PROP_CERT_URL));
        bundle.putString(PROP_CERT_FILE, mProp.getProperty(PROP_CERT_FILE));
        bundle.putString(PROP_CERT_BODY, mProp.getProperty(PROP_CERT_BODY));
        return bundle;
    }
}
