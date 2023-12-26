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

import static android.os.Environment.getExternalStorageDirectory;
import static android.os.Environment.getExternalStorageState;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.os.Environment;
import android.text.TextUtils;

public class FileHelper {
	public static void writeText(File file, String text) throws IOException{
		writeText(file, text, null);
	}
	public static void writeText(File file, String text, String encoding) throws IOException{
		if (file.exists()) file.delete();
		if (text == null) return;
		try {
			FileOutputStream fos = new FileOutputStream(file);
			fos.write(text.getBytes(encoding == null ? "UTF-8" : encoding));
			fos.close();
		} catch (FileNotFoundException e) {
			Logger.error(e);
		}
	}
	public static String readText(File source) throws IOException{
		return readText(source, null);
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
	public static String readText(InputStream source) throws IOException{
		return readText(source, null);
	}
	public static String readText(InputStream source, String encoding) throws IOException{
		StringBuilder sb = new StringBuilder();
		InputStreamReader isr = new InputStreamReader(source, encoding == null ? "UTF-8" : encoding);
		char[] cs = new char[10240];
		int len;
		while((len = isr.read(cs)) > 0){
			sb.append(cs, 0, len);
		}
		isr.close();
		return sb.toString();
	}

	public static void copyDir(File oldFile,File newFile){
		if(!oldFile.exists()) return;
		if(oldFile.isFile()){
			copy(oldFile,newFile);
		}else{
			mkdir(newFile);
			String[] filePath = oldFile.list();
			if(filePath == null) return;
			for(String name : filePath){
				File tempFile = new File(oldFile,name);
				copy(tempFile,new File(newFile,name));
			}
		}
	}

	public static void copy(File source, File target) {
		try (FileInputStream fis = new FileInputStream(source)) {
			copy(fis, target);
		} catch (IOException e) {
			Logger.error(e);
		}
	}

	public static void copy(InputStream is, String file) {
		copy(is, new File(file));
	}

	public static void copy(InputStream is, File file) {
		try (FileOutputStream fos = new FileOutputStream(file)){
			copy(is, fos);
			fos.getFD().sync();
		} catch (Exception e) {
			Logger.error(e);
		}
	}

	public static void copy(InputStream is, OutputStream os) {
		copy(is, os, 8 * 1024);
	}

	public static void copy(InputStream is, OutputStream os, int bufferSize) {
		if (is == null || os == null) return ;
		try {
			byte[] bs = new byte[bufferSize];
			int len;
			while((len = is.read(bs)) > 0){
				os.write(bs, 0, len);
			}
			os.flush();
		} catch (Exception e) {
			Logger.error(e);
		}
	}
	public static String getFilenameOf(String uri) {
		return uri.replaceFirst("https?:\\/\\/", "").replaceAll("[^a-zA-Z0-9.]",
				"_");
	}

	@TargetApi(Build.VERSION_CODES.FROYO)
	static File getExternalCacheDir(final Context context) {
		return context.getExternalCacheDir();
	}
	static File cacheDir = null;
	public static File getCacheDir(Context context, String category) {
		if (cacheDir == null){
			if (getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
				cacheDir = Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO ? 
						getExternalCacheDir(context)
						: new File(getExternalStorageDirectory().getPath() + "/Android/data/" + context.getPackageName() + "/cache/");
			} else {
				cacheDir = context.getCacheDir();
			}
			if (cacheDir != null && !cacheDir.exists()) {
				cacheDir.mkdirs();
			}
		}
		File fc = category == null ? cacheDir : new File(cacheDir, category);
		if (!fc.exists())
			fc.mkdir();
		return fc;
	}

	public static File getCacheOf(Context context, String category, String uri) {
		return new File(getCacheDir(context, category), getFilenameOf(uri));
	}
	public static File getFileInCard(String path){
		if (getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) 
			return new File(getExternalStorageDirectory().getPath() + path);
		return null;
	}
	public static void mkdirForFile(File f){
		mkdir(f.getParentFile());
	}
	public static void mkdir(File dir){
		try{
			if (dir == null) return;
			if (!dir.exists()){
				mkdir(dir.getParentFile());
				dir.mkdir();
			}
		}catch(Exception e){
			Logger.error(e);
		}
	}

	public static boolean unzip(String zipFile, String outputFolder) {
		try {
			return unzip(new FileInputStream(zipFile), new File(outputFolder));
		} catch (FileNotFoundException e) {
			Logger.error(e);
			return false;
		}
	}

	public static boolean unzip(InputStream is, File folder) {
		if (is == null || folder == null) return false;
		byte[] buffer = new byte[1024 * 8];
		folder.mkdir();
		try (ZipInputStream zis = new ZipInputStream(is)) {
			ZipEntry ze = zis.getNextEntry();
			while (ze != null) {
				String fileName = ze.getName();
				File newFile = new File(folder, fileName);

				Logger.debug("file unzip : " + newFile.getAbsoluteFile());

				newFile.getParentFile().mkdir();

				if (ze.isDirectory()){
					newFile.mkdir();
				} else {
					copy(zis, newFile);
				}
				ze = zis.getNextEntry();
			}

			zis.closeEntry();
			Logger.debug("Done");
			return true;
		} catch (Exception e) {
			Logger.error(e);
			return false;
		}
	}

	public static boolean unzipSingleFile(File dst, ZipFile zf, String filePath) {
		FileHelper.delete(dst);
		ZipEntry ze = zf.getEntry(filePath);
		try {
			InputStream is = zf.getInputStream(ze);
			FileHelper.copy(is, dst);
			is.close();
		} catch (Exception e) {
			return false;
		}

		return true;
	}

    public static boolean zip(String src, String dst) {
        return zip(new File(src), new File(dst));
    }

	public static boolean zip(File src, File dst) {
		boolean ret = false;
		try (FileOutputStream fos = new FileOutputStream(dst);
			 ZipOutputStream out = new ZipOutputStream(fos)) {
			zipFileOrDirectory(out, src, "");
			out.flush();
			fos.getFD().sync();
			ret = true;
		} catch (IOException ex) {
			Logger.error(ex);
		}
		return ret;
	}

//	public static boolean zip(File fileOrDirectory, OutputStream dst) {
//		try (ZipOutputStream out = new ZipOutputStream(dst)){
//			zipFileOrDirectory(out, fileOrDirectory, "");
//			out.flush();
//		} catch (Exception ex) {
//			Logger.error(ex);
//			return false;
//		}
//		return true;
//	}

	public static boolean isFileInZip(ZipFile zf, String filePath) {
		try {
			ZipEntry ze = zf.getEntry(filePath);
			if (null == ze) {
				return false;
			}
		}catch (Exception e){
			return false;
		}
		return true;
	}

    public static void zipFileOrDirectory(ZipOutputStream out, File fileOrDirectory, String curPath) throws IOException {
		if(fileOrDirectory.isFile()) {
			ZipEntry entry = new ZipEntry(curPath + fileOrDirectory.getName());
			out.putNextEntry(entry);
			try (FileInputStream in = new FileInputStream(fileOrDirectory)) {
				copy(in, out);
			}
			out.closeEntry();
		} else {
			File[] entries = fileOrDirectory.listFiles();
			if(null != entries && 0 != entries.length) {
				for (File entry : entries) {
					zipFileOrDirectory(out, entry,
							curPath + fileOrDirectory.getName() + "/");
				}
			}
		}
    }

	public static void cleanDir(File dir, List<String> keep) {
		if(dir.isFile()) {
			return;
		}
		File[] childFiles = dir.listFiles();
		if (childFiles == null || childFiles.length == 0) {
			return;
		}
		for(File target : childFiles) {
			if(target.isDirectory()) {
				cleanDir(target, keep);
			} else if(null != keep && keep.contains(target.getName())) {
				continue;
			}
			target.delete();
		}
	}

    public static void cleanDir(File dir) {
		cleanDir(dir, null);
	}

	public static void delete(File file) {  
	    if (file.isFile()) {  
	        file.delete();  
	        return;  
	    }  

	    if(file.isDirectory()){  
	        File[] childFiles = file.listFiles();  
	        if (childFiles == null || childFiles.length == 0) {  
	            file.delete();  
	            return;  
	        }  
	  
	        for (int i = 0; i < childFiles.length; i++) {  
	        	delete(childFiles[i]);  
	        }  
	        file.delete();  
	    }  
	}
	
	public static String getFileExtensionName(String fileName){
		try {
			int index = fileName.lastIndexOf(".");
			String pf = fileName.substring(index+1, fileName.length());
			return pf;
		} catch (Exception e) {
			Logger.error(e);
		}
		
		return null;
	} 
	
	public static long getFileSize(File file) {
        if (file == null) {
            return -1;
        }

        return (file.exists() && file.isFile() ? file.length() : -1);
    }
}
