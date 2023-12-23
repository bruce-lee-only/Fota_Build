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

import android.util.Base64;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class EncryptHelper {

	private static String DEFAULT_ENCODING = "UTF-8";

	public static String bytesToHexString(byte[] src, int off, int len) {
		StringBuilder uniqueID = new StringBuilder(len * 3);
		for (int i = off; i < len; i++) {
			int b = (src[i] & 0xFF);
			if (b <= 0xF) {
				uniqueID.append(0);
			}
			uniqueID.append(Integer.toHexString(b));
		}
		return uniqueID.toString();
	}

	public static String bytesToHexString(byte[] src) {
		StringBuilder uniqueID = new StringBuilder(src.length / 2 + 10);
		for (int i = 0; i < src.length; i++) {
			int b = (src[i] & 0xFF);
			if (b <= 0xF) {
				uniqueID.append(0);
			}
			uniqueID.append(Integer.toHexString(b));
		}
		return uniqueID.toString();
	}

	private static byte[] calcFileDigest(InputStream is, String algorithm) throws IOException, NoSuchAlgorithmException {
		MessageDigest m = MessageDigest.getInstance(algorithm);
		byte[] buffer = new byte[1024 * 8];
		int numRead = 0;
		while ((numRead = is.read(buffer)) > 0) {
			m.update(buffer, 0, numRead);
		}
		return m.digest();
	}

	private static byte[] calcByteDigest(byte[] b, String algorithm) throws NoSuchAlgorithmException {
		MessageDigest m = MessageDigest.getInstance(algorithm);
		m.update(b);
		return m.digest();
	}

	private static String calcFileDigestString(InputStream is, String algorithm) throws IOException, NoSuchAlgorithmException {
		return bytesToHexString(calcFileDigest(is, algorithm));
	}

	private static String calcFileDigestString(File f, String algorithm) {
		try (InputStream is = new FileInputStream(f)) {
			return calcFileDigestString(is, algorithm);
		} catch (Exception e) {
			Logger.error(e);
		}
		return "";
	}

	public static String calcFileMd5(InputStream is) throws Exception {
		return calcFileDigestString(is, "MD5");
	}

	public static String calcFileMd5(File f) {
		return calcFileDigestString(f, "MD5");
	}

	public static String calcFileMd5(byte[] b) throws NoSuchAlgorithmException {
		return bytesToHexString(calcByteDigest(b,"MD5"));
	}

	public static String calcFileSHA1(File f) {
		return calcFileDigestString(f, "SHA-1");
	}

	public static String calcFileSHA256(File f) {
		return calcFileDigestString(f, "SHA-256");
	}

	public static String calcFileSHA512(File f) {
		return calcFileDigestString(f, "SHA-512");
	}

	public static byte[] calcFileSHA256(InputStream is) throws Exception{
		return calcFileDigest(is, "SHA-256");
	}

	static byte[] encryptHmac(String algorithm, String encryptKey, String encryptText) throws Exception {
		byte[] data = encryptKey.getBytes(DEFAULT_ENCODING);
		SecretKey secretKey = new SecretKeySpec(data, algorithm);
		Mac mac = Mac.getInstance(algorithm);
		mac.init(secretKey);
		byte[] text = encryptText.getBytes(DEFAULT_ENCODING);
		return mac.doFinal(text);
	}

	public static byte[] encryptHmacMD5(String encryptKey, String encryptText) throws Exception {
		return encryptHmac("HmacMD5", encryptKey, encryptText);
	}

	public static byte[] encryptHmacSHA1(String encryptKey, String encryptText) throws Exception {
		return encryptHmac("HmacSHA1", encryptKey, encryptText);
	}

	public static String encryptForMARS(String encryptKey, String encryptText) {
		try {
			byte[] data = encryptHmacSHA1(encryptKey, encryptText);
			return encryptBASE64(data);
		} catch (Exception e) {
			Logger.error(e);
		} 
		return null;
	}
	
	public static String encryptBASE64(byte[] encode) throws UnsupportedEncodingException {
		byte[] code = Base64.encode(encode, 0, encode.length, Base64.NO_WRAP);
		return new String(code, DEFAULT_ENCODING);
	}

	public static byte[] encryptBASE64Byte(byte[] encode) {
		return Base64.encode(encode, 0, encode.length, Base64.NO_WRAP);
	}
	
	public static String encryptBASE64(String str) throws UnsupportedEncodingException {
		return encryptBASE64(str.getBytes(DEFAULT_ENCODING));
	}
	
	public static String decryptBASE64(byte[] decode) throws UnsupportedEncodingException {
		byte[] code = Base64.decode(decode, 0, decode.length, Base64.DEFAULT);
		return new String(code, DEFAULT_ENCODING);
	}

	public static byte[] decryptBASE64Byte(byte[] decode) throws UnsupportedEncodingException {
		return Base64.decode(decode, 0, decode.length, Base64.DEFAULT);
	}
	
	public static String decryptBASE64(String str) throws UnsupportedEncodingException {
		byte[] decode = str.getBytes(DEFAULT_ENCODING);
		return decryptBASE64(decode);
	}

	public static String encryptSlide(String src, int offset) {
		byte[] data = src.getBytes();
		for(int i = 0; i < data.length; i++) {
			data[i] = (byte)(data[i] + offset);
		}
		return new String(data);
	}

	public static String decryptSlide(String src, int offset) {
		return encryptSlide(src, -offset);
	}

	static byte[] encodeAES(int mode, byte[] raw, byte[] key, byte[] ivParam) throws Exception {
		Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
		SecretKeySpec secKeySpec = new SecretKeySpec(key, "AES");
		IvParameterSpec ivParamSpec = new IvParameterSpec(ivParam, 0, 16);
		cipher.init(mode, secKeySpec, ivParamSpec);
		return cipher.doFinal(raw);
	}

	public static byte[] encryptAES(byte[] raw, byte[] key, byte[] ivParam) throws Exception {
		return encodeAES(Cipher.ENCRYPT_MODE, raw, key, ivParam);
	}

	public static byte[] encryptAES(byte[] raw, byte[] key, String ivParam) throws Exception {
		return encodeAES(Cipher.ENCRYPT_MODE, raw, key, ivParam.getBytes(DEFAULT_ENCODING));
	}

	public static byte[] encryptAES(String raw, byte[] key, String ivParam) throws Exception {
		return encryptAES(raw.getBytes(DEFAULT_ENCODING), key, ivParam.getBytes(DEFAULT_ENCODING));
	}

	public static byte[] decryptAES(byte[] raw, byte[] key, byte[] ivParam) throws Exception {
		return encodeAES(Cipher.DECRYPT_MODE, raw, key, ivParam);
	}

	public static byte[] decryptAES(byte[] raw, byte[] key, String ivParam) throws Exception {
		return encodeAES(Cipher.DECRYPT_MODE, raw, key, ivParam.getBytes(DEFAULT_ENCODING));
	}
}
