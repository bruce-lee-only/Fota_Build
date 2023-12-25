package com.carota.html;

import android.content.Context;

import com.carota.core.IDisplayInfo;
import com.momock.util.FileHelper;
import com.momock.util.JsonHelper;
import com.momock.util.Logger;

import org.json.JSONObject;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class DisplayInfo implements IDisplayInfo {
    private static final String DISPLAY_INFO_NAME_CONDITION_TEXT = "cc_text";
    private static final String DISPLAY_INFO_NAME_VERSION_AND_RELEASE = "f_ver_desc";

    private final Map<String, Info> mInfo;
    private final Context mContext;


    public DisplayInfo(Context applicationContext) {
        this.mInfo = new HashMap<>();
        this.mContext = applicationContext;
    }

    @Override

    public JSONObject getConditionText() {

        return getData(DISPLAY_INFO_NAME_CONDITION_TEXT);
    }

    @Override
    public JSONObject getVersionAndRelease() {
        return getData(DISPLAY_INFO_NAME_VERSION_AND_RELEASE);
    }

    private JSONObject getData(String name) {
        if (mInfo.get(name) == null) {
            mInfo.put(name, new Info(name));
        }
        Info info = mInfo.get(name);
        JSONObject object = info != null ? info.getJsonObject(name) : null;
        return object == null ? new JSONObject() : object;
    }

    private class Info {
        private Map<String, JSONObject> map;

        public Info(String name) {
            this.map = new HashMap<>();
            HtmlHelper.getDisplayInfo(mContext, name, map);
        }

        public JSONObject getJsonObject(String name) {
            String[] languageNames = HtmlHelper.getLanguageNames(name, mContext);
            for (int i = languageNames.length - 1; i > 0; i--) {
                if (map.containsKey(languageNames[i])) {
                    return map.get(languageNames[i]);
                }
            }
            return map.get(name);
        }

    }
}
