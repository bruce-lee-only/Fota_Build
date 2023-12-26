/*******************************************************************************
 * Copyright (C) 2018-2023 CAROTA Technology Crop. <www.carota.ai>.
 * All Rights Reserved.
 *
 * Unauthorized using, copying, distributing and modifying of this file,
 * via any medium is strictly prohibited.
 *
 * Proprietary and confidential.
 ******************************************************************************/
package com.carota.httpproxy;


import android.text.TextUtils;

import com.carota.protobuf.HttpProxy;
import com.carota.svr.PrivReqHelper;
import com.carota.util.HttpHelper;
import com.carota.util.LZStringHelper;
import com.carota.util.MimeType;
import com.momock.util.Logger;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

import kotlin.text.Charsets;
import okhttp3.MediaType;
import okhttp3.internal.Util;
import okio.ByteString;

public class ActionProxy implements IActionProxy {
    private static final MediaType JSON = MediaType.parse(MimeType.JSON);
    private static final MediaType TEXT = MediaType.parse(MimeType.TEXT);
    private static final MediaType CJ = MediaType.parse(MimeType.LZSTRING);
    private static final MediaType STREAM = MediaType.parse(MimeType.STREAM);

    private final String mProxyHost;
    private final String[] mWhiteList;
    private final boolean mWhiteListEnabled;

    public ActionProxy(String proxyHost, String[] whiteList, boolean whiteListEnabled) {
        mProxyHost = proxyHost;
        mWhiteList = whiteList;
        mWhiteListEnabled = whiteListEnabled;
    }

    @Override
    public HttpHelper.Response doProxyGet(String url, Map<String, String> params) {
        return doProxy(url, params, null, null, 1);
    }

    @Override
    public HttpHelper.Response doProxyPost(String url,
                                           Map<String, String> params,
                                           Map<String, String> headers,
                                           Object body) {
        return doProxy(url, params, headers, body, 0);
    }

    @Override
    public boolean isNeedProxy(String url) {
        if (TextUtils.isEmpty(url)) {
            return false;
        }

        if (!mWhiteListEnabled) {
            return true;
        }

        if (mWhiteList == null || mWhiteList.length == 0) {
            return false;
        } else {
            for (String str : mWhiteList) {
                if (url.startsWith(str)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 使用代理发出网络请求
     *
     * @param url         地址
     * @param params      url参数
     * @param headers     请求头
     * @param body        请求体
     * @param proxyMethod 0为POST，1为GET
     * @return 请求结果
     */
    private HttpHelper.Response doProxy(String url,
                                        Map<String, String> params,
                                        Map<String, String> headers,
                                        Object body,
                                        int proxyMethod) {
        HttpProxy.HttpProxyReq.Builder builder = HttpProxy.HttpProxyReq.newBuilder();

        String conType = null;
        long contentLength = 0;
        try {
            if (body instanceof JSONObject) {
                conType = JSON.toString();
                MediaType contentType = JSON;
                Charset charset = contentType.charset();
                if (charset == null) {
                    charset = Charsets.UTF_8;
                    contentType = MediaType.parse(contentType + ";charset=utf-8");
                    conType = contentType.toString();
                }
                byte[] bytes = ((JSONObject) body).toString().getBytes(charset);
                contentLength = bytes.length;
                builder.setBody(com.google.protobuf.ByteString.copyFrom(bytes));
            } else if (body instanceof JSONArray) {
                conType = JSON.toString();
                MediaType contentType = JSON;
                Charset charset = contentType.charset();
                if (charset == null) {
                    charset = Charsets.UTF_8;
                    contentType = MediaType.parse(contentType + ";charset=utf-8");
                    conType = contentType.toString();
                }
                byte[] bytes = ((JSONArray) body).toString().getBytes(charset);
                contentLength = bytes.length;
                builder.setBody(com.google.protobuf.ByteString.copyFrom(bytes));
            } else if (body instanceof File) {
                conType = STREAM.toString();
                contentLength = ((File) body).length();
                InputStream inputStream = null;
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    inputStream = Files.newInputStream(((File) body).toPath());
                } else {
                    inputStream = new FileInputStream(((File) body));
                }
                builder.setBody(com.google.protobuf.ByteString.readFrom(inputStream));
            } else if (body instanceof ByteString) {
                conType = CJ.toString();
                byte[] bytes = ((ByteString) body).toByteArray();
                contentLength = bytes.length;
                builder.setBody(com.google.protobuf.ByteString.copyFrom(bytes));
            } else if (body instanceof byte[]) {
                conType = STREAM.toString();
                contentLength = ((byte[]) body).length;
                InputStream inputStream = new ByteArrayInputStream((byte[]) body);
                builder.setBody(com.google.protobuf.ByteString.readFrom(inputStream));
            } else if (body instanceof InputStream) {
                conType = STREAM.toString();
                contentLength = ((InputStream) body).available();
                builder.setBody(com.google.protobuf.ByteString.readFrom((InputStream) body));
            } else {
                conType = TEXT.toString();
                Charset charset = null;
                MediaType contentType = TEXT;
                charset = contentType.charset();
                if (charset == null) {
                    charset = Charsets.UTF_8;
                    contentType = MediaType.parse(contentType + ";charset=utf-8");
                    conType = contentType.toString();
                }
                String content = null == body ? "" : body.toString();
                byte[] bytes = content.getBytes(charset);
                contentLength = bytes.length;
                builder.setBody(com.google.protobuf.ByteString.copyFrom(bytes));
            }

            if (headers == null) {
                headers = new HashMap<>();
            }
            Logger.error("doProxy conType = " + conType + " contentLength = " + contentLength);
            headers.put("Content-Type", conType);
            headers.put("Content-Length", Long.toString(contentLength));
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                HttpProxy.HeaderReq.Builder headerReq = HttpProxy.HeaderReq.newBuilder();
                headerReq.setKey(entry.getKey());
                headerReq.setValStr(entry.getValue());
                Logger.error("doProxy entry.getKey() = " + entry.getKey() + " entry.getValue() = " + entry.getValue());
                builder.addHeader(headerReq.build());
            }

            HttpProxy.HttpProxyReq.Method method = HttpProxy.HttpProxyReq.Method.POST;
            if (proxyMethod == 1) {
                method = HttpProxy.HttpProxyReq.Method.GET;
            }
            String sendUrl = HttpHelper.getFullUrl(url, params);
            builder.setUrl(sendUrl);
            builder.setMethod(method);
        } catch (Exception e) {
            Logger.error("doProxy e = " + e.getMessage());
        }
        return doProxyRequest(builder.build());
    }

    private HttpHelper.Response doProxyRequest(HttpProxy.HttpProxyReq req) {
        HttpHelper.Response resp = new HttpHelper.Response();
        try {
            PrivReqHelper.Response privResp =
                    PrivReqHelper.doPost(
                            "http://" + mProxyHost + "/http_proxy",
                            req.toByteArray());
            resp.setStatusCode(privResp.getStatusCode());

            String type = privResp.getContentType();
            Logger.debug("HttpHelper doProxyRequest Content-Type : " + type);
            if (MimeType.LZSTRING.equals(type)) {
                resp.setBody(LZStringHelper.decompress(new ByteArrayInputStream(privResp.getBody())));
            } else {
                resp.setBody(new String(privResp.getBody()));
            }
        } catch (InterruptedIOException iie) {
            Logger.error(iie);
            resp.setInterrupted(true);
        } catch (Exception e) {
            Logger.error(e);
        }
        return resp;
    }
}
