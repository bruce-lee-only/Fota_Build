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
}
