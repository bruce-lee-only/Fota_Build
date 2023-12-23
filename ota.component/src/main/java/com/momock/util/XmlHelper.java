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

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;

public class XmlHelper {
	static XmlPullParserFactory fac = null;
	static {
		try {
			fac = XmlPullParserFactory.newInstance();
		} catch (XmlPullParserException e) {
			Logger.error(e);
		}
	}

	public static XmlPullParser createParser(String xml) {
		Logger.check(fac != null,
				"XmlPullParserFactory has not yet been created!");
		try {
			XmlPullParser parser = fac.newPullParser();
			parser.setInput(new StringReader(xml));
			return parser;
		} catch (XmlPullParserException e) {
			Logger.error(e);
		}
		return null;
	}

	public static XmlPullParser createParser(InputStream in, String encode) {
		Logger.check(fac != null,
				"XmlPullParserFactory has not yet been created!");
		try {
			XmlPullParser parser = fac.newPullParser();
			parser.setInput(in, encode);
			return parser;
		} catch (XmlPullParserException e) {
			Logger.error(e);
		}
		return null;
	}

	public static void skipCurrentTag(XmlPullParser parser)
			throws XmlPullParserException, IOException {
		int outerDepth = parser.getDepth();
		int type = parser.getEventType();
		if (type != XmlPullParser.START_TAG) {
			throw new IllegalStateException();
		}
		while ((type = parser.next()) != XmlPullParser.END_DOCUMENT
				&& (type != XmlPullParser.END_TAG || parser.getDepth() > outerDepth)) {
		}
	}
}