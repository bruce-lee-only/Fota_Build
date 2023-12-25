package com.carota.html;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.text.TextUtils;

import com.carota.build.ParamHtml;
import com.carota.util.ConfigHelper;
import com.carota.util.HttpHelper;
import com.momock.util.FileHelper;
import com.momock.util.JsonHelper;
import com.momock.util.Logger;
import com.momock.util.SystemHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class HtmlHelper {
    private final static String HTML_BASE_PATH = "/DisplayInfo";
    private final static String HTML_UNZIP_PATH = "/DisplayInfo/unzip";
    private final static String HTML_UNZIP_FOR_SUCCESS_PATH = "/DisplayInfoSuccess";
    private final static String HTML_UNZIP_PATH_TMP = "/DisplayInfo/unzip.tmp";

    @SuppressLint("SoonBlockedPrivateApi")
    public static void init() {
        int sdkInt = Build.VERSION.SDK_INT;
        try {
            ReflectionLimitUtil.clearLimit();
            Class<?> factoryClass = Class.forName("android.webkit.WebViewFactory");
            Field field = factoryClass.getDeclaredField("sProviderInstance");
            field.setAccessible(true);
            Object sProviderInstance = field.get(null);
            if (sProviderInstance != null) {
                Logger.info("Html init sProviderInstance isn't null");
                return;
            }

            Method getProviderClassMethod;
            if (sdkInt > 22) {
                getProviderClassMethod = factoryClass.getDeclaredMethod("getProviderClass");
            } else if (sdkInt == 22) {
                getProviderClassMethod = factoryClass.getDeclaredMethod("getFactoryClass");
            } else {
                Logger.info("Html init Don't need to Hook WebView");
                return;
            }
            getProviderClassMethod.setAccessible(true);
            Class<?> factoryProviderClass = (Class<?>) getProviderClassMethod.invoke(factoryClass);
            Class<?> delegateClass = Class.forName("android.webkit.WebViewDelegate");
            Constructor<?> delegateConstructor = delegateClass.getDeclaredConstructor();
            delegateConstructor.setAccessible(true);
            if (sdkInt < 26) {//低于Android O版本
                Constructor<?> providerConstructor = factoryProviderClass.getConstructor(delegateClass);
                if (providerConstructor != null) {
                    providerConstructor.setAccessible(true);
                    sProviderInstance = providerConstructor.newInstance(delegateConstructor.newInstance());
                }
            } else {
                Field chromiumMethodName = factoryClass.getDeclaredField("CHROMIUM_WEBVIEW_FACTORY_METHOD");
                chromiumMethodName.setAccessible(true);
                String chromiumMethodNameStr = (String) chromiumMethodName.get(null);
                if (chromiumMethodNameStr == null) {
                    chromiumMethodNameStr = "create";
                }
                Method staticFactory = factoryProviderClass.getMethod(chromiumMethodNameStr, delegateClass);
                if (staticFactory != null) {
                    sProviderInstance = staticFactory.invoke(null, delegateConstructor.newInstance());
                }
            }

            if (sProviderInstance != null) {
                field.set("Html init sProviderInstance", sProviderInstance);
                Logger.info("Html init Hook success!");
            } else {
                Logger.info("Html init Hook failed!");
            }
        } catch (Throwable e) {
            Logger.error(e);
        }
    }

    public static synchronized void downloadHtml(Context context, String url, String scheduleID) {
        String infoUrl = url;
        File dir = ConfigHelper.get(context).get(ParamHtml.class).getDownloadDir(context);
        copyOldHtml(context.getFilesDir(), dir);
        if (TextUtils.isEmpty(infoUrl)) {
            Logger.info("Html url is null");
        } else if (!infoUrl.startsWith("http")) {
            Logger.info("Html url have error");
        } else {
            try {
                File html = downFile(dir, infoUrl);
                if (html.exists()) {
                    unzipFile(html, dir);
                }
            } catch (Exception e) {
                Logger.error(e);
                Logger.info("Html is downloaded Fail");
            }
        }
    }

    /**
     * 兼容老版本固定保存到/data/data/包名/files/目录下的html
     *
     * @param filesDir
     * @param downloadDir
     */
    private static void copyOldHtml(File filesDir, File downloadDir) {
        File file = new File(filesDir, HTML_BASE_PATH);
        if (file.exists() && !downloadDir.getAbsolutePath().equals(filesDir.getAbsolutePath())) {
            Logger.info("Html Copy Old Html");
            boolean success = file.renameTo(new File(downloadDir, HTML_BASE_PATH));
            Logger.info("Html Copy Old Html %b", success);
        }
    }


    private static void unzipFile(File html, File filesDir) {
        File unzip = new File(filesDir, HTML_UNZIP_PATH);
        File unzipTmp = new File(filesDir, HTML_UNZIP_PATH_TMP);
        if (!unzip.exists()) {
            FileHelper.delete(unzipTmp);
            FileHelper.delete(unzip);
            if (!FileHelper.unzip(html.getAbsolutePath(), unzipTmp.getAbsolutePath())) {
                FileHelper.cleanDir(unzip.getParentFile());
                Logger.info("Html is UnZip Fail");
            } else if (!unzipTmp.renameTo(unzip)) {
                Logger.info("Html is UnZip Rename Fail");
            } else {
                Logger.info("Html is UnZip Success");
                return;
            }
            FileHelper.delete(unzipTmp);
            FileHelper.delete(unzip);
        }

    }

    private static File downFile(File f, String url) {
        File base = new File(f, HTML_BASE_PATH);
        File html = new File(base, parseUrlName(url));
        Logger.info("Html Start Down Html :%s", url);
        if (html.exists()) {
            Logger.info("Html is downloaded");
        } else {
            FileHelper.cleanDir(base);
            File htmlTmp = new File(html.getAbsolutePath().concat(".tmp"));
            FileHelper.mkdirForFile(htmlTmp);
            if (!HttpHelper.downloadFile(url, htmlTmp)) {
                Logger.info("Html is download fail");
            } else if (!htmlTmp.renameTo(html)) {
                Logger.info("Html is download rename Fail");
            } else {
                Logger.info("Html is downloaded");
            }
            FileHelper.delete(htmlTmp);
        }
        return html;
    }

    private static String parseUrlName(final String url) {
        return url.substring(url.lastIndexOf("/"));
    }


    public static File getHtml(Context context, String fileName, boolean loadSuccssHtml) {
        String[] names = getLanguageNames(fileName.concat("*.html"), context);
        return getFileForLanguage(context, names, loadSuccssHtml);
    }

    private static File getFileForLanguage(Context context, String[] names, boolean loadSuccssHtml) {
        try {
            File dir = ConfigHelper.get(context).get(ParamHtml.class).getDownloadDir(context);
            File htmlUnzip = new File(dir, loadSuccssHtml ? HTML_UNZIP_FOR_SUCCESS_PATH : HTML_UNZIP_PATH);
            List<String> strings = Arrays.asList(htmlUnzip.list());
            if (strings.size() > 0) {
                for (String s : names) {
                    if (strings.contains(s)) {
                        Logger.info("Html Find Remote File :%s", s);
                        return new File(htmlUnzip, s);
                    } else {
                        Logger.info("Html Not Find Remote File :%s", s);
                    }
                }
            }
        } catch (Exception e) {
            Logger.error(e);
        }
        return null;
    }

    static String[] getLanguageNames(String name, Context context) {
        String file = name.replace("*", "");
        String fileLanguage = name.replace("*", "_".concat(SystemHelper.getLanguage(context)));
        String fileLanguageCountry =
                name.replace("*", "_".concat(SystemHelper.getLanguage(context))
                        .concat("-")
                        .concat(SystemHelper.getCountry(context)));
        return new String[]{fileLanguageCountry, fileLanguage, file};
    }

    public static void setSuccessHtml(Context context) {
        Logger.info("Html copy Html to %s", HTML_UNZIP_FOR_SUCCESS_PATH);
        File dir = ConfigHelper.get(context).get(ParamHtml.class).getDownloadDir(context);
        File file = new File(dir, HTML_UNZIP_FOR_SUCCESS_PATH);
        File newHtml = new File(dir, HTML_UNZIP_PATH);
        FileHelper.cleanDir(file);
        FileHelper.copyDir(newHtml, file);
        Logger.info("Html copy Html to %s End", HTML_UNZIP_FOR_SUCCESS_PATH);
    }

    static void getDisplayInfo(Context context, String name, Map<String, JSONObject> map) {
        try {
            File dir = ConfigHelper.get(context).get(ParamHtml.class).getDownloadDir(context);
            File htmlUnzip = new File(dir, HTML_UNZIP_PATH);
            htmlUnzip.list((dir1, fileName) -> {
                if (fileName.startsWith(name)) {
                    try {
                        JSONObject object = JsonHelper.parseObject(FileHelper.readText(new File(dir1, fileName)));
                        if (object != null) map.put(fileName, object);
                    } catch (IOException e) {
                        Logger.error(e);
                    }
                }
                return false;
            });
        } catch (Exception e) {
            Logger.error(e);
        }
    }

}
