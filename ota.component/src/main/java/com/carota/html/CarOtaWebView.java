/*
 * Copyright (C) 2018-2020 CAROTA Technology Crop. <www.carota.ai>.
 * All Rights Reserved.
 *
 * Unauthorized using, copying, distributing and modifying of this file,
 * via any medium is strictly prohibited.
 *
 * Proprietary and confidential.
 */
package com.carota.html;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.os.Build;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.momock.util.Logger;

import java.io.File;


public class CarOtaWebView extends WebView {
    public CarOtaWebView(Context context) {
        this(context, null);
    }

    public CarOtaWebView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CarOtaWebView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void init(Context context) {
        setOnKeyListener((v, keyCode, event) -> {
            if (keyCode != KeyEvent.KEYCODE_BACK || !canGoBack()) {
                return false;
            }
            goBack();
            return true;
        });

        CookieManager cookieManager = CookieManager.getInstance();
        CookieManager.setAcceptFileSchemeCookies(true);
        cookieManager.setAcceptThirdPartyCookies(this, true);

        WebSettings webSettings = getSettings();
        webSettings.setDefaultTextEncodingName("utf-8");
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setUseWideViewPort(true);
        webSettings.setNeedInitialFocus(true);
        webSettings.setSupportZoom(false);
        webSettings.setLoadsImagesAutomatically(true);
        webSettings.setMixedContentMode(0);
        webSettings.setJavaScriptEnabled(false);
        webSettings.setJavaScriptCanOpenWindowsAutomatically(false);
        webSettings.setAllowFileAccess(true);
        webSettings.setAllowContentAccess(true);
        webSettings.setSaveFormData(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setPluginState(WebSettings.PluginState.ON);
        webSettings.setCacheMode(WebSettings.LOAD_DEFAULT);
        setLayerType(View.LAYER_TYPE_HARDWARE, null);


        setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView webView, int i) {

            }

            @Override
            public void onReceivedTitle(WebView webView, String title) {
            }
        });
        setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView webView, String url) {
            }

            @Override
            public void onPageStarted(WebView webView, String url, Bitmap bitmap) {
            }

            //            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onReceivedError(WebView webView, WebResourceRequest webResourceRequest, WebResourceError webResourceError) {
                super.onReceivedError(webView, webResourceRequest, webResourceError);
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView webView, String url) {
                return super.shouldOverrideUrlLoading(webView, url);
            }
        });
        setBackgroundColor(0); // 设置背景色
        if (getBackground() != null) getBackground().setAlpha(0); // 设置填充透明度 范围：0-255
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
                && getSettings().getForceDark() == WebSettings.FORCE_DARK_AUTO) {
            WebSettings settings = getSettings();
            if (isDarkMode()) {
//                Logger.info("暗夜模式");
                settings.setForceDark(WebSettings.FORCE_DARK_ON);
            } else {
//                Logger.info("白天模式");
                settings.setForceDark(WebSettings.FORCE_DARK_OFF);
            }
        }
    }

    boolean isDarkMode() {
        int applicationUiMode = getResources().getConfiguration().uiMode;
        int systemMode = applicationUiMode & Configuration.UI_MODE_NIGHT_MASK;
        return systemMode == Configuration.UI_MODE_NIGHT_YES;
    }

    /**
     * load remote html
     *
     * @param name       file name,you needn`t set the Language or Country,
     *                   because The program will search files with language and Country
     *                   based on resources.if not found the file for language and Country,
     *                   will search for the file with the default name.
     *                   and then use the assetsName
     *                   name_Language-Country>name_Language>name>defaultHtml
     * @param assetsName will load {@code @assetsName} when not find remote File
     * @param loadSuccssHtml true :load install success html
     *                       false : load html is new from Server
     */
    public final void loadRemoteHtmlFile(String name, String assetsName, boolean loadSuccssHtml) {
        try {
            if (!TextUtils.isEmpty(name)) {
                File file = HtmlHelper.getHtml(this.getContext().getApplicationContext(), name, loadSuccssHtml);
                if (file != null && file.exists()) {
                    loadUrl("file://" + file.getAbsolutePath());
                    return;
                } else {
                    Logger.error("Html not found file :%s", name);
                }
            } else {
                Logger.error("Html name is null");
            }
            if (!TextUtils.isEmpty(assetsName)) {
                loadUrl("file:///android_asset/".concat(assetsName));
            }
        } catch (Exception e) {
            Logger.error(e);
        }
    }

    /**
     * load html file
     *
     * @param html
     * @param assetsName
     */
    public final void loadRemoteHtmlFile(File html, String assetsName) {
        if (html != null && html.exists()) {
            loadUrl("file://" + html.getAbsolutePath());
        } else if (!TextUtils.isEmpty(assetsName)) {
            loadUrl("file:///android_asset/".concat(assetsName));
        }
    }

    @Override
    public void destroy() {
        clearHistory();
        clearFormData();
        super.destroy();
    }

}
