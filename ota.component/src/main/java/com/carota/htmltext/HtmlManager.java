/*******************************************************************************
 * Copyright (C) 2018-2020 CAROTA Technology Crop. <www.carota.ai>.
 * All Rights Reserved.
 *
 * Unauthorized using, copying, distributing and modifying of this file,
 * via any medium is strictly prohibited.
 *
 * Proprietary and confidential.
 ******************************************************************************/
package com.carota.htmltext;

import android.content.Context;
import android.widget.TextView;

import com.carota.build.ParamDisclaimer;
import com.carota.htmltext.data.HtmlData;
import com.carota.htmltext.engine.HtmlText;
import com.carota.htmltext.remote.ActionHtml;
import com.carota.htmltext.remote.IActionHtml;
import com.carota.util.ConfigHelper;
import com.carota.util.SerialExecutor;
import com.momock.util.FileHelper;
import com.momock.util.Logger;

import java.io.File;
import java.io.IOException;
import java.util.Locale;

public class HtmlManager {

    public static final String EN_GB = "en-GB";
    public static final String ZH_CN = "zh-CN";
    private static final String[] LANGUAGES = new String[]{EN_GB, ZH_CN};
    public static final String DISCLAIMER_EN_GB_NAME = "disclaimer_en-GB";
    public static final String DISCLAIMER_ZH_CN_NAME = "disclaimer_zh-CN";
    public static final String DISCLAIMER= "disclaimer";
    private static final Object sLocker = new Object();
    private static IActionHtml mActionHtml;
    private static final SerialExecutor sExecutor = new SerialExecutor();

    private static void init() {
        synchronized (sLocker) {
            if (mActionHtml == null) {
                mActionHtml = new ActionHtml();
            }
        }
    }

    public static void downloadHtml(Context context, String scheduleId, IDownloadHtmlCallback callback) {
        init();
        if (!sExecutor.isEmpty() || sExecutor.isRunning()) {
            Logger.debug("downloadHtml is running return");
            return;
        }
        sExecutor.execute(new Runnable() {
            @Override
            public void run() {
                ParamDisclaimer paramDisclaimer = ConfigHelper.get(context).get(ParamDisclaimer.class);
                for (String language : LANGUAGES) {
                    HtmlData htmlData = mActionHtml.getHtmlData(paramDisclaimer.getUrl(), scheduleId, language);
                    if (htmlData == null || htmlData.getCode() != 0) {
                        callback.onError(-1, "get HtmlData error");
                        return;
                    }
                    String fileName = DISCLAIMER + "_" + language;

                    File file = new File(context.getFilesDir(), "html/" + fileName);
                    FileHelper.mkdirForFile(file);
                    boolean success = mActionHtml.downloadHtml(htmlData.getFileUrl(), htmlData.getMd5(), file, paramDisclaimer.getMaxRetry());
                    if (success) {
                        callback.onSuccess(file);
                    } else {
                        callback.onError(-1, "download html error");
                    }
                }
                callback.onFinish();
            }
        });
    }

    public static void downloadHtml(Context context, String scheduleId, String language, IDownloadHtmlCallback callback) {
        init();
        if (!sExecutor.isEmpty() || sExecutor.isRunning()) {
            Logger.debug("downloadHtml is running return");
            return;
        }
        sExecutor.execute(new Runnable() {
            @Override
            public void run() {
                ParamDisclaimer paramDisclaimer = ConfigHelper.get(context).get(ParamDisclaimer.class);
                HtmlData htmlData = mActionHtml.getHtmlData(paramDisclaimer.getUrl(), scheduleId, language);
                if (htmlData == null || htmlData.getCode() != 0) {
                    callback.onError(-1, "get HtmlData error");
                    return;
                }
                String fileName = DISCLAIMER + language;
                File file = new File(context.getFilesDir(), "html/" + fileName);
                FileHelper.mkdirForFile(file);
                boolean success = mActionHtml.downloadHtml(htmlData.getFileUrl(), htmlData.getMd5(), file, paramDisclaimer.getMaxRetry());
                if (success) {
                    callback.onSuccess(file);
                } else {
                    callback.onError(-1, "download html error");
                }
                callback.onFinish();
            }
        });
    }

    public static void loadDisclaimer(Context context, IHtmlImageLoader htmlImageLoader, TextView view) {
        Locale locale = context.getResources().getConfiguration().locale;
        String fileName = HtmlManager.DISCLAIMER_ZH_CN_NAME;
        if (locale.getLanguage().equals(Locale.ENGLISH.getLanguage())) {
            fileName = HtmlManager.DISCLAIMER_EN_GB_NAME;
        }
        File file = new File(context.getFilesDir(),"html/" + fileName);
        if (file.exists() && file.length() > 0) {
            try {
                HtmlText.from(FileHelper.readText(file)).setImageLoader(htmlImageLoader).into(view);
            } catch (IOException e) {
                e.printStackTrace();
                Logger.error("HtmlManager read disclaimer file error ,msg = " + e.getMessage());
            }
        }
    }
}
