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

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.bluetooth.BluetoothAdapter;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Environment;
import android.os.Process;
import android.os.StatFs;
import android.provider.Settings.Secure;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.text.format.Formatter;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.Surface;
import android.view.WindowManager;


import com.carota.util.HttpHelper;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

public class SystemHelper {
	public static class PhoneInfo{
		private String imei;
		private String imsi;
		private String mcc;
		private String mnc;
		public PhoneInfo(String imei, String imsi, String mcc, String mnc){
			this.imei = imei;
			this.imsi = imsi;
			this.mcc = mcc;
			this.mnc = mnc;
		}
		public String getImei(){
			return imei;
		}
		public String getImsi(){
			return imsi;
		}
		public String getMcc(){
			return mcc;
		}
		public String getMnc(){
			return mnc;
		}
	}
	public static String getAndroidId(Context context){
		return Secure.getString(context.getContentResolver(),Secure.ANDROID_ID);
	}
	public static String getWifiMac(Context context){
		if (!checkPermission(context, Manifest.permission.ACCESS_WIFI_STATE)){
			Logger.warn("PE : WIFI");
			return null;
		}
		try {
			WifiManager wm = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
			if (wm.getConnectionInfo() == null) return null;
			return wm.getConnectionInfo().getMacAddress();
		} catch (Exception e) {
			Logger.error(e);
			return null;
		}
	}
	public static String getNetworkType(Context context){
		if (!checkPermission(context, Manifest.permission.ACCESS_NETWORK_STATE)){
			Logger.warn("PE : NetworkType");
			return null;
		}
		
		ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
		
		NetworkInfo info = cm.getActiveNetworkInfo();
		if (info != null && info.isConnected()){
			switch (info.getType()){
			case ConnectivityManager.TYPE_WIFI : 
			case ConnectivityManager.TYPE_WIMAX : 
			case ConnectivityManager.TYPE_VPN : 
			case ConnectivityManager.TYPE_ETHERNET : 
				return "WIFI";
			case ConnectivityManager.TYPE_MOBILE : 
			    switch (tm.getNetworkType()) {
			        case TelephonyManager.NETWORK_TYPE_GPRS:
			        case TelephonyManager.NETWORK_TYPE_EDGE:
			        case TelephonyManager.NETWORK_TYPE_CDMA:
			        case TelephonyManager.NETWORK_TYPE_1xRTT:
			        case TelephonyManager.NETWORK_TYPE_IDEN:
			            return "2G";
			        case TelephonyManager.NETWORK_TYPE_UMTS:
			        case TelephonyManager.NETWORK_TYPE_EVDO_0:
			        case TelephonyManager.NETWORK_TYPE_EVDO_A:
			        case TelephonyManager.NETWORK_TYPE_HSDPA:
			        case TelephonyManager.NETWORK_TYPE_HSUPA:
			        case TelephonyManager.NETWORK_TYPE_HSPA:
			        case TelephonyManager.NETWORK_TYPE_EVDO_B:
			        case TelephonyManager.NETWORK_TYPE_EHRPD:
			        case TelephonyManager.NETWORK_TYPE_HSPAP:
			            return "3G";
			        case TelephonyManager.NETWORK_TYPE_LTE:
			            return "4G";
			    }
			}
		}
		return null;
	}
	public static String getBluetoothMac(Context context){
		try {
			if (BluetoothAdapter.getDefaultAdapter() == null) return null;
			return BluetoothAdapter.getDefaultAdapter().getAddress();
		} catch (Exception e) {
			Logger.error(e);
			return null;
		}
	}

	@SuppressLint("DefaultLocale")
	public static String getMd5(String text) {
		MessageDigest m = null;
		try {
			m = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
			Logger.error(e);
		}
		m.update(text.getBytes());
		byte md5Data[] = m.digest();

		String uniqueID = new String();
		for (int i = 0; i < md5Data.length; i++) {
			int b = (0xFF & md5Data[i]);
			if (b <= 0xF)
				uniqueID += "0";
			uniqueID += Integer.toHexString(b);
		}
		return uniqueID.toUpperCase();
	}
	public static PhoneInfo getPhoneInfo(Context context){
		String imsi = null, imei = null, mcc = null, mnc = null;
		TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
		try {
			if (checkPermission(context, Manifest.permission.READ_PHONE_STATE)) {
				imsi = tm.getSubscriberId();
				if (imsi != null && imsi.length() == 16)
					imsi = imsi.substring(1);
				if (imsi != null && imsi.length() != 15)
					imsi = null;
				imei = tm.getDeviceId();
			}
			String op = tm.getNetworkOperator();
			if ((op != null) && (((op.length() == 5) || (op.length() == 6)))) {
				mcc = tm.getNetworkOperator().substring(0, 3);
				mnc = tm.getNetworkOperator().substring(3);
			}
		} catch (Exception e) {
			Logger.error(e);
		}
		return new PhoneInfo(imei, imsi, mcc, mnc);
	}
	public static String getOsVersion(){
		return android.os.Build.VERSION.RELEASE;
	}
	public static String getAppId(Context context){
		return context.getPackageName();
	}
	static Map<String, String> appMeta = new HashMap<String, String>();

	public static void setAppMeta(String key, String val){
		Logger.info("Set App Meta : " + key + " = " + val);
		if(null == val) {
			appMeta.remove(key);
		} else {
			appMeta.put(key, val);
		}
	}

	private static Object getAppMeta(Context context, String key) {
		Object val = appMeta.get(key);
		if (null == val) {
			try {
				ApplicationInfo ai = context.getPackageManager()
						.getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
				val = ai.metaData.get(key);
			} catch (NameNotFoundException e) {
				e.printStackTrace();
			}
//			val = context.getApplicationInfo().metaData.get(key);
		}
		return val;
	}

	public static String getAppMeta(Context context, String key, String def){
		Object val = getAppMeta(context, key);
		if(null != val) {
			Object ret = Convert.toString(val);
			if(null != ret) {
				return (String)ret;
			}
		}
		return def;
	}

	public static int getAppMeta(Context context, String key, int def){
		Object val = getAppMeta(context, key);
		if(null != val) {
			return Convert.toInteger(val, def);
		} else {
			return def;
		}
	}

	public static boolean getAppMeta(Context context, String key, boolean def){
		Object val = getAppMeta(context, key);
		if(null != val){
			Object data = Convert.toBoolean(val);
			if(null != data){
				return (boolean)data;
			}
		}
		return def;
	}
	public static String getAppVersion(Context context) {
		PackageManager pm = context.getPackageManager();	
		PackageInfo pInfo;
		try {
			pInfo = pm.getPackageInfo(getAppId(context), 0);
			return pInfo.versionName;
		} catch (NameNotFoundException e) {
			Logger.error(e);
		}
		return "0";
	}
	public static String getUA(boolean withAndroidVersion){		
		String ua = "android";	
		if (withAndroidVersion)
			ua += ";VERSION/" + android.os.Build.VERSION.RELEASE;
		ua += ";MANUFACTURER/" + android.os.Build.MANUFACTURER;
		ua += ";MODEL/" + android.os.Build.MODEL;
		ua += ";BOARD/" + android.os.Build.BOARD;
		ua += ";BRAND/" + android.os.Build.BRAND;
		ua += ";DEVICE/" + android.os.Build.DEVICE;
		ua += ";HARDWARE/" + android.os.Build.HARDWARE;
		ua += ";PRODUCT/" + android.os.Build.PRODUCT;
		return ua;
	}
	public static Location getLastLocation(Context context){
		Location loc = null;
		if(checkPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
				&& checkPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)) {
			try {
				LocationManager lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
				loc = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
				if (loc == null)
					loc = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
			} catch (Exception e) {
				Logger.error(e);
			}
		}
		return loc;
	}
	public static boolean isSystemApp(Context context){
		PackageManager pm = context.getPackageManager();	
		try {
			ApplicationInfo ai = pm.getApplicationInfo(context.getPackageName(), 0);
			return (ai.flags & ApplicationInfo.FLAG_SYSTEM) != 0;
		} catch (NameNotFoundException e) {
			Logger.error(e);
		}
		return false;
	}
	public static boolean isSystemApp(Context context, String packageName){
		if (packageName == null) return false;
		try {
			PackageManager pm = context.getPackageManager();
			ApplicationInfo ai = pm.getApplicationInfo(packageName, 0);
			return (ai.flags & ApplicationInfo.FLAG_SYSTEM) != 0;
		} catch (Exception e) {
			Logger.error(e);
		}
		return false;
	}
	public static String getCountry(Context context){
		String country = Locale.getDefault().getCountry();
		String c = null;
		Logger.info("Locale Country : " + country);
		TelephonyManager tm = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
		c = tm.getSimCountryIso();
		Logger.info("Sim Country : " + c);
		if (c != null && c.length() == 2)
			country = c;
		c = tm.getNetworkCountryIso();
		Logger.info("Net Country : " + country);	
		if (c != null && c.length() == 2)
			country = c;

		if(checkPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
				&& checkPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)) {
			try {
				LocationManager lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
				Criteria criteria = new Criteria();
				String provider = lm.getBestProvider(criteria, false);
				Location loc = lm.getLastKnownLocation(provider);
				if (loc != null) {
					Geocoder gc = new Geocoder(context, Locale.ENGLISH);
					List<Address> addrs = gc.getFromLocation(loc.getLatitude(), loc.getLongitude(), 1);
					if (addrs != null && addrs.size() == 1) {
						c = addrs.get(0).getCountryCode();
						Logger.info("GPS Country : " + c);
						if (c != null && c.length() == 2)
							country = c;
					}
				}
			} catch (Exception e) {
				//Logger.error(e);
			}
		}
		country = country.toLowerCase();
		Logger.info("Country : " + country);
		return country;
	}
	public static boolean isScreenRotated(Context context){
		WindowManager wm = (WindowManager)context.getSystemService(Context.WINDOW_SERVICE);
		Display display = wm.getDefaultDisplay();
		int r = display.getRotation();
		return r == Surface.ROTATION_90 || r == Surface.ROTATION_270;
	}
	public static String getScreen(Context context){
		WindowManager wm = (WindowManager)context.getSystemService(Context.WINDOW_SERVICE);
		Display display = wm.getDefaultDisplay();
		DisplayMetrics metrics = new DisplayMetrics();
		display.getMetrics(metrics);
		return metrics.widthPixels + "x" + metrics.heightPixels;
	}

	public static int getScreenWidth(Context context){
		WindowManager wm = (WindowManager)context.getSystemService(Context.WINDOW_SERVICE);
		Display display = wm.getDefaultDisplay();
		DisplayMetrics metrics = new DisplayMetrics();
		display.getMetrics(metrics);
		return metrics.widthPixels;
	}

	public static int getScreenHeight(Context context){
		WindowManager wm = (WindowManager)context.getSystemService(Context.WINDOW_SERVICE);
		Display display = wm.getDefaultDisplay();
		DisplayMetrics metrics = new DisplayMetrics();
		display.getMetrics(metrics);
		return metrics.heightPixels;
	}

	public static float getScreenDensity(Context context){
		WindowManager wm = (WindowManager)context.getSystemService(Context.WINDOW_SERVICE);
		Display display = wm.getDefaultDisplay();
		DisplayMetrics metrics = new DisplayMetrics();
		display.getMetrics(metrics);
		return metrics.density;
	}
	public static void openLink(Context context, String url){
		try{
			if (url == null)
				url = "http://google.com";
			else if (url.indexOf("://") == -1)
				url = "http://" + url;

			Logger.debug("Open Link :" + url);
			Intent intent = new Intent();
			intent.setAction(Intent.ACTION_VIEW);
			intent.setData(Uri.parse(url));
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			context.startActivity(intent);
		}catch (Exception e){
			Logger.error(e);
		}
	}
	
	public static String getPackageNameOfApk(Context context, String path) {
		if(null == context || null == path) {
			return null;
		}
		try {
			PackageInfo pi = context.getPackageManager().getPackageArchiveInfo(path, 0);
			return null == pi ? null : pi.packageName;
		} catch (Exception ex){
			return null;
		}
	}

	public static int getVersionCodeOfApk(Context context, String path) {
		int code = -1;
		if(null == context || null == path) {
			return code;
		}
		try {
			PackageInfo pi = context.getPackageManager().getPackageArchiveInfo(path, 0);
			code = null == pi ? 0 : pi.versionCode;
		} catch (Exception e){
			code = -2;
		}
		return code;

	}

	public static boolean isInstalled(Context context, String id){
		return getVersionCode(context, id) > 0;
	}

	public static int getVersionCode(Context context, String id){
		int code = -1;
		if(null == id || null == context) {
			return code;
		}
		try {
			PackageInfo pi = context.getPackageManager().getPackageInfo(id, 0);
			code = null == pi ? 0 : pi.versionCode;
		} catch (NameNotFoundException e) {
			code = 0;
		} catch (Exception e) {
			code = -2;
		}
		return code;
	}

	public static boolean isTablet(Context context) {
		boolean xlarge = ((context.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) == Configuration.SCREENLAYOUT_SIZE_XLARGE);
		boolean large = ((context.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) == Configuration.SCREENLAYOUT_SIZE_LARGE);
		return (xlarge || large);
	}

	public static String getPackageNameFromUrl(String url){
		if (url == null || url.indexOf("details?") == -1) return null;
		return HttpHelper.getUrlParameter(url, "id");
	}
	
	public static PackageInfo[] getInstalledApps(Context context, boolean includeSystemApp){
		PackageManager pkgManager = context.getPackageManager();
		List<PackageInfo> packs = pkgManager.getInstalledPackages(0);
		List<PackageInfo> appList = new ArrayList<PackageInfo>();
		for (int i = 0; i < packs.size(); i++) {
			PackageInfo p = packs.get(i);
			Intent intent = pkgManager.getLaunchIntentForPackage(p.packageName);
			if (intent != null){
				boolean sa = false;
				try {
					ApplicationInfo ai = pkgManager.getApplicationInfo(p.packageName, 0);
					sa = (ai.flags & ApplicationInfo.FLAG_SYSTEM) != 0;
				} catch (NameNotFoundException e) {
					Logger.error(e);
				}
				if (includeSystemApp || !sa)
					appList.add(p);
			}
		}
		PackageInfo[] apps = new PackageInfo[appList.size()];
		appList.toArray(apps);
		Arrays.sort(apps, new Comparator<PackageInfo>(){

			@Override
			public int compare(PackageInfo lhs, PackageInfo rhs) {
				return lhs.packageName.compareToIgnoreCase(rhs.packageName);
			}
			
		});
		return apps;
	}
	/*
	public static List<String> getEmailAccounts(Context context){
		List<String> emails = new ArrayList<String>();
		if (!checkPermission(context, "android.permission.GET_ACCOUNTS")){
			Logger.warn("PE : Accounts");
			return emails;
		}
		try{
			Pattern emailPattern = Patterns.EMAIL_ADDRESS; 
			Account[] accounts = AccountManager.get(context).getAccounts();
			for (Account account : accounts) {
			    if (emailPattern.matcher(account.name).matches()) {
			    	emails.add(account.name);		        
			    }
			}
		}catch(Exception e){
			Logger.error(e);
		}
		return emails;
	}
	*/
	
	public static boolean isRoaming(Context context)
	{
		TelephonyManager tm = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
		return tm.isNetworkRoaming();
	}
	
	public static String getMarketUrl(String packageName){
		return "market://details?id=" + packageName;
	}
	
	public static boolean hasSdcard(Context context){
		return android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED);
	}
	static Boolean gpInstalled = null;
	public static boolean isGooglePlayInstalled(Context context){
		if (gpInstalled != null) return gpInstalled;
		PackageManager pkgManager = context.getPackageManager();
		List<PackageInfo> packs = pkgManager.getInstalledPackages(0);
		gpInstalled = false;
		for (int i = 0; i < packs.size(); i++) {
			PackageInfo p = packs.get(i);
			//Logger.debug("App " + i + ":" + p.packageName + "(" + p.applicationInfo.className + ")");
			if (!gpInstalled && "com.android.vending".equals(p.packageName)){
				Logger.debug("com.android.vending -> " + p.applicationInfo.className);
				if (p.applicationInfo.className != null && p.applicationInfo.className.startsWith("com.google.")){
					gpInstalled = true;
					Logger.debug("Google Play in installed in this device!");
				}
			}
		}
		return gpInstalled;
	}
	public static boolean checkPermission(Context context, String permission){

		int result = PackageManager.PERMISSION_DENIED;
		if(null != context && null != permission){
			result = context.checkPermission(permission, Process.myPid(), Process.myUid());
		}
		return PackageManager.PERMISSION_GRANTED == result;
	}

	public static void startAppByGid(Context context, String id){
		try {
			Intent i = context.getPackageManager().getLaunchIntentForPackage(id);
			i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			context.startActivity(i);
		}catch (Exception e){
			Logger.error(e);
		}
	}

	public static boolean isConnected(NetworkInfo ni) {
		return null != ni && ni.isConnectedOrConnecting();
	}

	public static boolean isConnected(ConnectivityManager cm, int type) {
		NetworkInfo ni = cm.getNetworkInfo(type);
		return isConnected(ni);
	}

	public static String getTimeZone() {
		Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT"),
				Locale.getDefault());
		Date currentLocalTime = calendar.getTime();
		DateFormat date = new SimpleDateFormat("Z");
		return date.format(currentLocalTime);
	}
	

    public static long getTotalMemory() {
        String str1 = "/proc/meminfo";
        String str2;
        String[] arrayOfString;
        long initial_memory = 0;
        try {
            FileReader localFileReader = new FileReader(str1);
            BufferedReader localBufferedReader = new BufferedReader(localFileReader, 8192);
            str2 = localBufferedReader.readLine();

            arrayOfString = str2.split("\\s+");

            initial_memory = Long.valueOf(arrayOfString[1]).longValue() * 1024;
            localBufferedReader.close();

        } catch (IOException e) {
        }
        return initial_memory;
    }

    public static long getAvailMemory(Context context) {
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
        am.getMemoryInfo(mi);
        return mi.availMem;
    }

    public static class CpuInfo{
    	public int count = 0;
    	public String name = "";
    	public String arch = "";
    }
    
    public static CpuInfo getCpuInfo() {
    	CpuInfo ci = new CpuInfo();
    	FileReader fr = null;
    	BufferedReader br = null;
        try {
            fr = new FileReader("/proc/cpuinfo");
            br = new BufferedReader(fr);
            String text;
            while ((text = br.readLine()) != null) {
                String[] arrayOfString = text.split(":");
                if (text.startsWith("Hardware")) {
                    ci.name = arrayOfString[1].trim();
                } else if (text.startsWith("processor")) {
                    ci.count++;
                } else if (text.startsWith("Processor")) {
                    ci.arch = arrayOfString[1].trim();
                }
            }
        } catch (Exception e) {
            Logger.error(e);
        } finally {
        	try{
	        	if(null != br){
	        		br.close();
	        	}
	        	if(null != fr){
	        		fr.close();
	        	}
        	} catch (Exception ex){
    			
    		}
        }
        return ci;
    }

    public static class DiskInfo{
    	public long total = 0;
    	public long available = 0;
    }
    
    public static DiskInfo getDiskInfo(File path) {
    	DiskInfo di = new DiskInfo();
    	try{
            StatFs stat = new StatFs(path.getPath());
            
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){
            	di.available = stat.getAvailableBytes();
                di.total = stat.getTotalBytes();
            } else {
            	di.available = stat.getAvailableBlocks();
                di.total = stat.getFreeBlocks();
            }
    	}catch(Exception e){
    		Logger.error(e);
    	}
        return di;
    }
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    public static String getRomTotalSize(Context context) {
        File path = Environment.getDataDirectory();
        StatFs stat = new StatFs(path.getPath());
        long blockSize = stat.getBlockSizeLong();
        long totalBlocks = stat.getBlockCountLong();

        long dataSize = blockSize * totalBlocks;


        path = Environment.getRootDirectory();
        stat = new StatFs(path.getPath());
        blockSize = stat.getBlockSizeLong();
        totalBlocks = stat.getBlockCountLong();
        long rootSize = blockSize * totalBlocks;


        return Formatter.formatFileSize(context, dataSize + rootSize);
    }

	public static String getBrowserPackage(Intent intent, Context context) {
		PackageManager pm = context.getPackageManager();
		List<ResolveInfo> appsList = pm.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
		String tmpPkg = "", pkg = "";
		for (ResolveInfo app : appsList) {
			tmpPkg = app.activityInfo.packageName.toLowerCase();
			if ("com.android.chrome".equals(tmpPkg)) {
				return tmpPkg;
			}
			if (tmpPkg.contains("browse") || tmpPkg.contains("chrome")
					|| tmpPkg.contains("com.UCMobile.intl")
					|| tmpPkg.contains("org.mozilla.firefox")
					|| tmpPkg.contains("com.opera.mini.native")) {
				pkg = tmpPkg;
			}
		}
		return pkg;
	}
	

	public static void openLinkInDefaultBrowser(Context context, String url){
		try{
			if (url == null)
				url = "http://google.com";
			else if (url.indexOf("://") == -1)
				url = "http://" + url;

			Logger.debug("Open Link :" + url);
			Intent intent = new Intent();
			intent.setAction(Intent.ACTION_VIEW);
			intent.setData(Uri.parse(url));
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

			final String browserPackage = getBrowserPackage(intent, context);
			if (!browserPackage.isEmpty()) {
				intent.setPackage(browserPackage);
			} 
			
			context.startActivity(intent);
		}catch (Exception e){
			Logger.error(e);
		}
	}
	
	public static String getOSBuildType(){
		return android.os.Build.TYPE == null ? "" : android.os.Build.TYPE;	
	}
	
	public static boolean checkUSBConnect(Context context){
		boolean ret = false;
		try {
			IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
			Intent batteryStatusIntent = context.registerReceiver(null, ifilter);
			
			int status = batteryStatusIntent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
			boolean isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
			        status == BatteryManager.BATTERY_STATUS_FULL;

			int chargePlug = batteryStatusIntent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
			boolean usbCharge = chargePlug == BatteryManager.BATTERY_PLUGGED_USB;

			if (isCharging) {
			    if (usbCharge) {
			        ret = true;
			    }
			}
		} catch (Exception e) {
			Logger.error(e);
		}
		
		return ret;
	}
	
	public static boolean checkVPN(Context context) {
		boolean ret = false;
		
		try {
			if (context != null && checkPermission(context, "android.permission.ACCESS_NETWORK_STATE")){
				ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
				ret = cm.getNetworkInfo(ConnectivityManager.TYPE_VPN).isConnectedOrConnecting();
			}
		} catch (Exception e) {
			Logger.error(e);
		}
		
	    return ret;
	}

	public static boolean isAppExist(Context context, String appId) {
		ApplicationInfo info = null;
		if(!TextUtils.isEmpty(appId)) {
			try {
				info = context.getPackageManager().getApplicationInfo(appId, 0);
			} catch (PackageManager.NameNotFoundException e) {
				e.printStackTrace();
			}
		}
		return info != null;
	}

	public static boolean execScript(String command) {
		try {
			java.lang.Process process = Runtime.getRuntime().exec(command);
			process.waitFor();
			return process.exitValue() == 0;
		} catch (IOException | InterruptedException e) {
			Logger.error(e);
		}
		return false;
	}

	public static boolean isComponentEnabled(Context context, ComponentName cn, boolean def) {
		int cfg = context.getPackageManager().getComponentEnabledSetting(cn);
		if(PackageManager.COMPONENT_ENABLED_STATE_DEFAULT == cfg) {
			return def;
		}
		return PackageManager.COMPONENT_ENABLED_STATE_ENABLED == cfg;
	}



	public static void setComponentEnabled(Context context, ComponentName cn, boolean def, boolean enabled) {
		int target = PackageManager.COMPONENT_ENABLED_STATE_DISABLED;
		if(enabled) {
			target = PackageManager.COMPONENT_ENABLED_STATE_ENABLED;
		}
		if(def == enabled) {
			target = PackageManager.COMPONENT_ENABLED_STATE_DEFAULT;
		}

		PackageManager pm = context.getPackageManager();

		if(target != pm.getComponentEnabledSetting(cn)) {
			pm.setComponentEnabledSetting(cn, target, PackageManager.DONT_KILL_APP);
		}
	}

	public static Locale getResourcesLocale(Context context) {
		Configuration configuration = context.getResources().getConfiguration();
		Locale locale;
		if (Build.VERSION.SDK_INT > Build.VERSION_CODES.N) {
			locale = configuration.getLocales().get(0);
		} else {
			locale = configuration.locale;
		}
		return locale;
	}
	public static String getLanguage(Context context) {
		return getResourcesLocale(context).getLanguage();
	}
	public static String getResourcesCountry(Context context) {
		return getResourcesLocale(context).getCountry();
	}
}
