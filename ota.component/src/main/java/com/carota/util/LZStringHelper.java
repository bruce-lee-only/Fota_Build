/*******************************************************************************
 * Copyright (C) 2018-2020 CAROTA Technology Crop. <www.carota.ai>.
 * All Rights Reserved.
 *
 * Unauthorized using, copying, distributing and modifying of this file,
 * via any medium is strictly prohibited.
 *
 * Proprietary and confidential.
 ******************************************************************************/

package com.carota.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class LZStringHelper {

	static class BitInputStream {

		InputStream is;

		private int pos = 128;
		private byte thisByte = 0;

		public BitInputStream(InputStream is) throws IOException {
			this.is = is;
			thisByte = (byte) is.read();
		}

		public void close() throws IOException {

			is.close();
		}

		public int readBits(int numBits) throws IOException {

			int returnVal = 0;

			if (numBits > 16)
				numBits = 16;

			for (int i = 0; i < numBits; i++) {

				returnVal |= (1 << i) * (((thisByte & pos) == 0) ? 0 : 1);

				pos >>= 1;

				if (pos == 0) {
					// fix decompress null error
					int b = is.read();
					if(-1 == b) {
						thisByte = 0;
					} else {
						thisByte = (byte) b;
					}
					//thisByte = (byte) is.read();

					pos = 128;
				}
			}

			return returnVal;
		}
	}

	static class BitOutputStream {

		ByteArrayOutputStream bos = new ByteArrayOutputStream();

		private int pos = 0, bytesWritten = 0;
		private byte thisByte = 0;

		public byte[] getContent() throws IOException {

			// Replicate the 16-bit behaviour of the JavaScript implementation
			// for terminating the string.
			while (thisByte != 0)
				writeBits(1, 0);

			if (bytesWritten % 2 != 0)
				bos.write((byte) 0);

			return bos.toByteArray();
		}

		public void writeBits(int numBits, int value) throws IOException {

			for (int i = 0; i < numBits; i++) {

				thisByte = (byte) ((thisByte << 1) | (value & 1));

				if (++pos % 8 == 0) {

					bos.write(thisByte);
					pos = 0;

					bytesWritten++;
				}

				value >>= 1;
			}
		}
	}

	static class Context {

		private Map<String, Integer> dictionary = new HashMap<String, Integer>();
		private Set<String> dictionaryToCreate = new HashSet<String>();

		private String wc = "", w = "", result = "";

		private BitOutputStream data = new BitOutputStream();

		private int enlargeIn = 2, dictSize = 3, numBits = 2;

		public BitOutputStream getData() {

			return data;
		}

		public Map<String, Integer> getDictionary() {

			return dictionary;
		}

		public Set<String> getDictionaryToCreate() {

			return dictionaryToCreate;
		}

		public String getWc() {

			return wc;
		}

		public void setWc(String wc) {

			this.wc = wc;
		}

		public String getW() {

			return w;
		}

		public void setW(String w) {

			this.w = w;
		}

		public String getResult() {

			return result;
		}

		public void setResult(String result) {

			this.result = result;
		}

		public int getEnlargeIn() {

			return enlargeIn;
		}

		public void setEnlargeIn(int enlargeIn) {

			this.enlargeIn = enlargeIn;
		}

		public int getDictSize() {

			return dictSize;
		}

		public void setDictSize(int dictSize) {

			this.dictSize = dictSize;
		}

		public int getNumBits() {

			return numBits;
		}

		public void setNumBits(int numBits) {

			this.numBits = numBits;
		}
	}

	public static String decompress(String compressed) {

		String output = null;

		try {
			output = decompress(new ByteArrayInputStream(compressed.getBytes()));
		} catch (IOException e) {
			e.printStackTrace();
		}

		return output;
	}

	public static String decompress(InputStream compressed) throws IOException{

		BitInputStream bis = new BitInputStream(compressed);

		HashMap<Integer, String> dict = new HashMap<Integer, String>();

		int next, enlargeIn = 4, dictSize = 4, numBits = 3, errorCount = 0;
		int c;

		String entry = "";
		String w = "";

		StringBuffer result = new StringBuffer();

		for (int i = 0; i < 3; i++)
			dict.put(i, null);

		next = bis.readBits(2);

		if (next == 0 || next == 1)
			c = bis.readBits((next + 1) * 8);
		else
			return "";

		dict.put(3, Character.toString((char) c));

		w = Character.toString((char) c);
		result.append(w);

		while (true) {

			c = bis.readBits(numBits);

			if (c == 0 || c == 1) {

				if (errorCount++ > 10000)
					throw new IOException("This is not a compressed string.");

				c = bis.readBits((c + 1) * 8);
				dict.put(dictSize++, Character.toString((char) c));
				c = dictSize - 1;
				enlargeIn--;
			} else if (c == 2)
				return result.toString();

			if (enlargeIn == 0)
				enlargeIn = 1 << numBits++;

			if (dict.get(c) != null)
				entry = dict.get(c);
			else {
				if (c == dictSize)
					entry = w + w.charAt(0);
				else
					return null;
			}

			result.append(entry);

			dict.put(dictSize++, w + entry.charAt(0));
			enlargeIn--;

			w = entry;

			if (enlargeIn == 0)
				enlargeIn = 1 << numBits++;
		}
	}

	private static void produceW(Context context) throws IOException {

		Map<String, Integer> dict = context.getDictionary();
		Set<String> cDict = context.getDictionaryToCreate();

		String w = context.getW();
		BitOutputStream bos = context.getData();

		int numBits = context.getNumBits();
		int enlargeIn = context.getEnlargeIn();

		if (cDict.contains(w)) {

			int charCode = Character.codePointAt(w, 0);
			boolean notUnicode = charCode < 256;

			bos.writeBits(numBits, notUnicode ? 0 : 1);
			bos.writeBits(notUnicode ? 8 : 16, charCode);

			enlargeIn = (--enlargeIn == 0) ? 1 << numBits++ : enlargeIn;

			cDict.remove(w);
		} else {

			bos.writeBits(numBits, dict.get(w));
		}

		enlargeIn = (--enlargeIn == 0) ? 1 << numBits++ : enlargeIn;

		context.setEnlargeIn(enlargeIn);
		context.setNumBits(numBits);
	}

	public static byte[] compress(String uncompressed) {

		byte[] output = null;

		try {
			output = compress(new ByteArrayInputStream(uncompressed.getBytes()));
		} catch (IOException e) {
			e.printStackTrace();
		}

		return output;
	}

	public static byte[] compress(InputStream uncompressed) throws IOException {

		Context context = new Context();
		int c;

		Map<String, Integer> dict = context.getDictionary();

		// Create a character stream from the binary input stream.
		InputStreamReader isr = new InputStreamReader(uncompressed);

		while ((c = isr.read()) != -1) {

			String nextCharacter = Character.toString((char) c);

			if (!context.getDictionary().containsKey(nextCharacter)) {

				dict.put(nextCharacter, 3 + dict.size());

				context.getDictionaryToCreate().add(nextCharacter);
			}

			context.setWc(context.getW() + nextCharacter);

			if (context.getDictionary().containsKey(context.getWc()))
				context.setW(context.getWc());
			else {

				produceW(context);

				dict.put(context.getWc(), 3 + dict.size());
				context.setW(nextCharacter);
			}
		}

		if (!context.getW().equals(""))
			produceW(context);

		context.getData().writeBits(context.getNumBits(), 2);
		uncompressed.close();

		return context.getData().getContent();
	}
}
