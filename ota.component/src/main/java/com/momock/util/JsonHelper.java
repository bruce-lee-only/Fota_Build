/*******************************************************************************
 * Copyright (C) 2018-2020 CAROTA Technology Crop. <www.carota.ai>.
 * All Rights Reserved.
 *
 * Unauthorized using, copying, distributing and modifying of this file,
 * via any medium is strictly prohibited.
 *
 * Proprietary and confidential.
 ******************************************************************************/
package com.momock.util;

import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

public class JsonHelper {
	public static Object select(JSONObject node, String path, Object def){
		if (node == null) return def;
		if (path == null) return def;
		int pos = path.indexOf("/");
		String current = pos == -1 ? path : path.substring(0, pos);
		String next = pos == -1 ? null : path.substring(pos + 1);
		Iterator<?> keys = node.keys();
        while( keys.hasNext() ){
            String key = (String)keys.next();
            if (current.equals(key)){
	            Object val;
				try {
					val = node.get(key);
					if (next == null) return val;
		            if(val instanceof JSONObject){
		            	return select((JSONObject)val, next, def);
		            } else {
		            	return def;
		            }
				} catch (JSONException e) {
					Logger.error(e);
				}
            }
        }
        return def;
	}
	public static String selectString(JSONObject node, String path, String def){
		Object val = select(node, path, null);
		return val == null ? def : Convert.toString(val);
	}
	public static Integer selectInteger(JSONObject node, String path, Integer def){
		Object val = select(node, path, null);
		return val == null ? def : Convert.toInteger(val);
	}
	public static Double selectDouble(JSONObject node, String path, Double def){
		Object val = select(node, path, null);
		return val == null ? def : Convert.toDouble(val);
	}

	public static boolean selectBoolean(JSONObject node, String path, boolean def){
		Object val = select(node, path, null);
		return val == null ? def : Convert.toBoolean(val);
	}

	static <T> T parse(String json, Class<T> data) {
		if (!TextUtils.isEmpty(json)) {
			JSONTokener tokener = new JSONTokener(json);
			try {
				return data.cast(tokener.nextValue());
			} catch (Exception e) {
				Logger.error(e);
			}
		}
		return null;
	}

	@Deprecated
	public static JSONObject parse(String json) {
		return parse(json, JSONObject.class);
	}

	public static JSONObject parseObject(String json) {
		return parse(json, JSONObject.class);
	}

	public static JSONArray parseArray(String json) {
		return parse(json, JSONArray.class);
	}

	public static Map<String, String> toStringMap(JSONObject json) {
		if(null == json) {
			return null;
		}
		Map<String, String> ret = new HashMap<>();
		Iterator<String> iterator = json.keys();
		while (iterator.hasNext()) {
			String key = iterator.next();
			Object val = json.opt(key);
			if(val instanceof String) {
				ret.put(key, (String)val);
			}
		}
		return ret;
	}

	public static JSONObject parse(Bundle data) {
		JSONObject jo = new JSONObject();
		if(null != data) {
			Set<String> keys = data.keySet();
			try {
				for (String key : keys) {
					Object val = data.get(key);
					if(null == val) {
						continue;
					}
					if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
						jo.put(key, JSONObject.wrap(val));
					} else {
						jo.put(key, val);
					}
				}
			} catch (Exception e) {
				Logger.error(e);
			}
		}
		return jo;
	}

	public static<T> List<T> parseArray(JSONArray ja, Class<T> klass) {
		List<T> ret = new ArrayList<>();
		if(null != ja && ja.length() > 0) {
			for(int i = 0; i < ja.length(); i++) {
				Object data = ja.opt(i);
				if(klass.isAssignableFrom(data.getClass())) {
					ret.add(klass.cast(data));
				}
			}
		}
		return ret;
	}
}
