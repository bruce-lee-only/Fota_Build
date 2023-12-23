/*******************************************************************************
 * Copyright (C) 2018-2020 CAROTA Technology Crop. <www.carota.ai>.
 * All Rights Reserved.
 *
 * Unauthorized using, copying, distributing and modifying of this file,
 * via any medium is strictly prohibited.
 *
 * Proprietary and confidential.
 ******************************************************************************/

package com.carota.agent;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

public class FileHelper {

	public static boolean copy(InputStream is, OutputStream os, int bufferSize) {
		if (is == null || os == null) {
			return false;
		}
		try {
			byte[] bs = new byte[bufferSize];
			int len;
			while((len = is.read(bs)) > 0){
				os.write(bs, 0, len);
			}
		} catch (Exception e) {
			return false;
		}
		return true;
	}


	public static void writeTextImmediately(File file, String text, String encoding) throws IOException{
		if (text == null) return;
		FileOutputStream fos = new FileOutputStream(file, false);
		fos.write(text.getBytes(encoding == null ? "UTF-8" : encoding));
		fos.getFD().sync();
		fos.close();
	}

	public static String readText(File source, String encoding) throws IOException{
		if (source == null || !source.exists()) return null;
		StringBuilder sb = new StringBuilder();
		FileInputStream fis = new FileInputStream(source);
		InputStreamReader isr = new InputStreamReader(fis, encoding == null ? "UTF-8" : encoding);
		char[] cs = new char[10240];
		int len;
		while((len = isr.read(cs)) > 0){
			sb.append(cs, 0, len);
		}
		isr.close();
		fis.close();
		return sb.toString();
	}
}
