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

import android.text.TextUtils;

import com.carota.dtc.log.engine.Rule;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Piece {
    private String time;
    private String tag;
    private String app;
    private String level;
    private String message;
    private String origin;
    private Map<Integer,String> formatMap;
    private static boolean needFormat = true;

    public static Piece parse(String line, Map<Integer,String> format) {
        if (TextUtils.isEmpty(line) || format == null || format.size() <= 0) {
            return null;
        }
        Piece piece = new Piece();
        piece.origin = line;
        piece.formatMap = format;
        if (format.size() == 1 && format.get(Rule.TARGET_MESSAGE).equals("*")) {
            needFormat = false;
        }

        return piece;
    }

    public String getOrigin() {
        return origin;
    }

    public String getTimestamp() {
        if (!needFormat) {
            return "";
        }

        if (time == null) {
            time = getMatcherString(Rule.TARGET_TIME);
        }
        return time;
    }

    public String getApp() {
        if (!needFormat) {
            return "";
        }

        if (app == null) {
            app = getMatcherString(Rule.TARGET_APP);
        }
        return app;
    }

    public String getTag() {
        if (!needFormat) {
            return "";
        }

        if (tag == null) {
            tag = getMatcherString(Rule.TARGET_TAG);
        }
        return tag;
    }

    public String getLevel() {
        if (!needFormat) {
            return "";
        }

        if (level == null) {
            level = getMatcherString(Rule.TARGET_LEVEL);
        }
        return level;
    }

    public String getMessage() {
        if (!needFormat) {
            return origin;
        }

        if (message == null) {
            message = getMatcherString(Rule.TARGET_MESSAGE);
        }
        return message;
    }

    @Override
    public String toString() {
        String time = "\t" + getTimestamp() + "\t";
        String tag = getTag();
        String app = "\t" + getApp() + "\t";
        String level = getLevel();
        String message = getMessage();
        if (message != null) {
            if (message.contains("\"")) {
                message = message.replaceAll("\"", "\"\"");
            }
            if (message.contains(",")) {
                message = "\"" + message + "\"";
            }
        }
        return time + "," + tag + "," + app + "," + level + "," + message;
    }

    private String getMatcherString(int target) {
        String result = null;
        String rule = formatMap.get(target);
        if (rule == null) {
            return null;
        }
        if (target == Rule.TARGET_MESSAGE) {
            try {
                int fmt = Integer.parseInt(rule.substring(0, 1));
                rule = rule.substring(1);
                if (fmt == Rule.MESSAGE_FMT_OTHER) {
                    if (rule.contains("TAG")) {
                        rule = rule.replace("TAG", getTag());
                    } else if (rule.contains("TIME")) {
                        rule = rule.replace("TIME", getTimestamp());
                    } else if (rule.contains("LEVEL")) {
                        rule = rule.replace("LEVEL", getLevel());
                    } else if (rule.contains("APP")) {
                        rule = rule.replace("APP", getApp());
                    }
                }
            } catch (Exception e) {
            }
        }
        Pattern pattern = Pattern.compile(rule);
        Matcher matcher = pattern.matcher(origin);
        if (matcher.find()) {
            result = matcher.group(0);
        }
        return result;
    }

    public boolean isNeedFormat() {
        return needFormat;
    }
}
