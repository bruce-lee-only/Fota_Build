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

public class Convert {

	public static Boolean toBoolean(Object value, Boolean def) {
		if (value instanceof Boolean) {
			return (Boolean) value;
		} else if (value instanceof CharSequence) {
			String stringValue = value.toString();
			if ("true".equalsIgnoreCase(stringValue)) {
				return true;
			} else if ("false".equalsIgnoreCase(stringValue)) {
				return false;
			} else if ("1".equalsIgnoreCase(stringValue)) {
				return true;
			} else if ("0".equalsIgnoreCase(stringValue)) {
				return false;
			}
		} else if (value instanceof Number){
			return ((Number)value).intValue() != 0;
		}
		return def;
	}

	public static Boolean toBoolean(Object value) {
		return toBoolean(value, false);
	}

	public static Double toDouble(Object value, Double def) {
		if (value instanceof Double) {
			return (Double) value;
		} else if (value instanceof Number) {
			return ((Number) value).doubleValue();
		} else if (value instanceof CharSequence) {
			return Double.valueOf(value.toString());
		}
		return def;
	}

	public static Double toDouble(Object value) {
		return toDouble(value, 0.0);
	}

	public static Integer toInteger(Object value, Integer def) {
		if (value instanceof Integer) {
			return (Integer) value;
		} else if (value instanceof Number) {
			return ((Number) value).intValue();
		} else if (value instanceof CharSequence) {
			return Integer.valueOf(value.toString());
		}
		return def;
	}

	public static Integer toInteger(Object value) {
		return toInteger(value, 0);
	}


	public static Long toLong(Object value, Long def) {
		if (value instanceof Long) {
			return (Long) value;
		} else if (value instanceof Number) {
			return ((Number) value).longValue();
		} else if (value instanceof CharSequence) {
			return Long.valueOf(value.toString());
		}
		return def;
	}

	public static Long toLong(Object value) {
		return toLong(value, 0L);
	}

	public static String toString(Object value) {
		if (value instanceof String) {
			return (String) value;
		} else if (value != null) {
			return String.valueOf(value);
		}
		return null;
	}

}
