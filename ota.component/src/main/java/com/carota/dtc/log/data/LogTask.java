/*******************************************************************************
 * Copyright (C) 2018-2020 CAROTA Technology Crop. <www.carota.ai>.
 * All Rights Reserved.
 *
 * Unauthorized using, copying, distributing and modifying of this file,
 * via any medium is strictly prohibited.
 *
 * Proprietary and confidential.
 ******************************************************************************/

package com.carota.dtc.log.data;

import com.carota.dtc.log.engine.Rule;
import com.momock.util.JsonHelper;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LogTask {

    /**
     * {
     *  "name":"ivi",
     *  "cmd":[
     *    {
     *      "path":"route",
     *      "filter":[
     *        ["FilterA1","FilterA2","FilterA3"],
     *        ["FilterB1","FilterB2","FilterB3"]
     *      ]
     *    }
     *  ]
     * }
    * */

    private String mName;
    private JSONArray mRaw;
    private JSONArray mFormat;

    public LogTask(JSONObject task) {
        mName = task.optString("name");
        mRaw = task.optJSONArray("cmds");
        mFormat = task.optJSONArray("fmts");
    }

    public int getCommandCount() {
        return null == mRaw ? 0 : mRaw.length();
    }

    public String getTargetName() {
        return mName;
    }

    public Command getCommand(int index) {
        JSONObject jo = mRaw.optJSONObject(index);
        return null == jo ? null : new Command(jo);
    }

    public Map<Integer,String> getFormat() {
        List<String> format = JsonHelper.parseArray(mFormat, String.class);
        if (format.size() <=0) {
            return null;
        }
        Map<Integer,String> formatMap = new HashMap<>();
        if (format.size() == 1 && format.get(0).equals("*")) {
            formatMap.put(Rule.TARGET_MESSAGE,"*");
        } else {
            for (String raw : format) {
                int pos = raw.indexOf(':');
                int target = Integer.parseInt(raw.substring(0, pos));
                String rule = raw.substring(pos + 1);
                formatMap.put(target,rule);
            }
        }
        return formatMap;
    }

    public static class Command {

        private JSONArray mRaw;
        private String mPath;

        private Command(JSONObject jo) {
            mPath = jo.optString("path");
            mRaw = jo.optJSONArray("filters");
        }

        public String getPath() {
            return mPath;
        }

        public int getFilterCount() {
            return null == mRaw ? 0 : mRaw.length();
        }

        public List<String> getFilter(int index) {
            if(null != mRaw) {
                return JsonHelper.parseArray(mRaw.optJSONArray(index), String.class);
            }
            return null;
        }
    }

}
