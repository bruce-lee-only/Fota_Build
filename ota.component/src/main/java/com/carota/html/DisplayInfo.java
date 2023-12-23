package com.carota.html;

import android.content.Context;

import com.carota.core.IDisplayInfo;
import com.momock.util.FileHelper;
import com.momock.util.JsonHelper;
import com.momock.util.Logger;
import com.momock.util.SystemHelper;

import org.json.JSONObject;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class DisplayInfo implements IDisplayInfo {
    private static final String DISPLAY_INFO_NAME_CONDITION_TEXT = "cc_text";
    private static final String DISPLAY_INFO_NAME_VERSION_AND_RELEASE = "f_ver_desc";

    private final Map<String, JSONObject> mInfo;
    private final Context mContext;


    public DisplayInfo(Context applicationContext) {
        this.mInfo = new HashMap<>();
        this.mContext = applicationContext;
    }

    @Override

    public JSONObject getConditionText() {

        return getData(DISPLAY_INFO_NAME_CONDITION_TEXT, false);
    }

    @Override
    public JSONObject getVersionAndRelease(Boolean isBackup) {
        return getData(DISPLAY_INFO_NAME_VERSION_AND_RELEASE, isBackup);
    }

    private JSONObject getData(String name, Boolean isBackup) {//name = "f_ver_desc"
        JSONObject object = null;
        try {
            File file = HtmlHelper.getFileForLanguage(mContext, name, isBackup);
            JSONObject root = JsonHelper.parseObject(FileHelper.readText(file));
            String language = DISPLAY_INFO_NAME_VERSION_AND_RELEASE.concat("_")
                    .concat(SystemHelper.getLanguage(mContext))
                    .concat("-")
                    .concat(SystemHelper.getCountry(mContext).toUpperCase());
            object = root.getJSONObject(DISPLAY_INFO_NAME_VERSION_AND_RELEASE);
            object = root.getJSONObject(language);
        } catch (Exception e) {
            Logger.error(e);
        }
        return object == null ? new JSONObject() : object;
    }

    private JSONObject getInfoJson(String name) {
        if (mInfo.get(name) != null) {
            String[] names = HtmlHelper.getLanguageNames(name, mContext);

        }
        return null;
    }

}
