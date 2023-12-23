/*******************************************************************************
 * Copyright (C) 2018-2020 CAROTA Technology Crop. <www.carota.ai>.
 * All Rights Reserved.
 *
 * Unauthorized using, copying, distributing and modifying of this file,
 * via any medium is strictly prohibited.
 *
 * Proprietary and confidential.
 ******************************************************************************/

package com.carota.svr;

import android.text.TextUtils;

import com.carota.component.BuildConfig;
import com.carota.util.MimeType;
import com.momock.util.FileHelper;
import com.momock.util.Logger;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

public class PrivReqHelper {

    private static final MediaType TRANSFER = MediaType.parse(BuildConfig.DATA_TYPE_PBA);

    public static final TransferEncoder sEncoder = new TransferEncoder(BuildConfig.SALT);

    public static class Response {
        private boolean interrupted;
        private int statusCode;
        private byte[] body;
        private String mContentType = MimeType.JSON;

        public boolean isInterrupted() {
            return interrupted;
        }

        public void setInterrupted(boolean Interrupt) {
            interrupted = Interrupt;
        }

        public int getStatusCode() {
            return statusCode;
        }

        public void setStatusCode(int statusCode) {
            this.statusCode = statusCode;
        }

        public byte[] getBody() {
            return body;
        }

        public void setBody(byte[] body) {
            this.body = body;
        }

        public void setContentType(String contentType){
            mContentType = contentType;
        }

        public String getContentType(){
            return mContentType;
        }
    }

    public static String getFullUrl(String url, Map<String, String> params) {
        String ret = url;
        if (null != url && null != params && !params.isEmpty()) {
            StringBuilder urlBuilder = new StringBuilder(128);
            urlBuilder.append(url);
            urlBuilder.append(url.lastIndexOf('?') == -1 ? "?" : "&");
            try {
                for(Map.Entry<String, String> entry : params.entrySet()) {
                    urlBuilder.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
                    urlBuilder.append('=');
                    String value = null == entry.getValue() ? "" : entry.getValue();
                    urlBuilder.append(URLEncoder.encode(value, "UTF-8"));
                    urlBuilder.append('&');
                }
            } catch (Exception e) {
                Logger.error(e);
            }
            int lastCharIndex = urlBuilder.length() - 1;
            if (urlBuilder.charAt(lastCharIndex) == '&') {
                urlBuilder.setLength(lastCharIndex);
            }
            ret = urlBuilder.toString();
        }
        //Logger.debug("FULL URL : " + ret);
        return ret;
    }


    private static OkHttpClient sDirectClient = null;
    private static OkHttpClient sProxyClient = null;
    private static Proxy sGlobalProxyCache = null;

    private static class ResetReadTimeOut implements Interceptor {
        @Override
        public okhttp3.Response intercept(Chain chain) throws IOException {
            Request request = chain.request();
            Chain chainB;
            if (request.url().encodedPath().equals("/connect")) {
                chainB=chain.withReadTimeout(15, TimeUnit.MINUTES);
            } else {
                chainB=chain.withReadTimeout(5, TimeUnit.MINUTES);
            }
            return chainB.proceed(chain.request());
        }
    }

    private static OkHttpClient getHttpClient(boolean useProxy) {
        synchronized (OkHttpClient.class) {
            if(useProxy) {
                if(null != sGlobalProxyCache) {
                    sProxyClient = new OkHttpClient.Builder()
                            .addInterceptor(new ResetReadTimeOut())
//                            .readTimeout(120, TimeUnit.SECONDS)
                            .retryOnConnectionFailure(true)
                            .proxy(sGlobalProxyCache)
                            .build();
                    sGlobalProxyCache = null;
                }
                if (null != sProxyClient) {
                    return sProxyClient;
                }
            }
            // default client
            if(null == sDirectClient) {
                sDirectClient = new OkHttpClient.Builder()
                        .readTimeout(120, TimeUnit.SECONDS)
                        .retryOnConnectionFailure(true).build();
            }
        }
        return sDirectClient;
    }

    public static void setGlobalProxy(String addr, int port) {
        synchronized (OkHttpClient.class) {
            sGlobalProxyCache = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(addr, port));
        }
    }

    /**
     * 单元测试扩展
     *
     * @param proxy
     */
    public static void setGlobalProxy(Proxy proxy) {
        synchronized (OkHttpClient.class) {
            sGlobalProxyCache = proxy;
        }
    }

    private static String createSeed(){
        int len = 20;
        Random generator = new Random();
        StringBuilder strBuilder = new StringBuilder(len);
        for(int i = 0; i < len; i++) {
            strBuilder.append((char)(generator.nextInt(26) + 'a'));
        }
        return strBuilder.toString();
    }

    private static Request.Builder createBasicRequest(String url, String host, Map<String, String> params, boolean keep) {
        Request.Builder req = new Request.Builder().url(getFullUrl(url, params));
        if(!keep) {
            req.addHeader("Connection", "close");
        }
        if(!TextUtils.isEmpty(host)) {
            req.addHeader("Host", host);
        }
        return req;
    }

    private static Response doHttpRequest(Request.Builder req, byte[] body, boolean useProxy) {
        Response resp = new Response();
        try {
            String reqSeed = createSeed();
            req.addHeader("Seed", reqSeed);
            if(null != body) {
                byte[] payload = sEncoder.encodeToByte(body, reqSeed);
                req.post(RequestBody.create(TRANSFER, payload));
            }
            okhttp3.Response response = getHttpClient(useProxy).newCall(req.build()).execute();
            resp.setStatusCode(response.code());
            resp.setContentType(response.header("Content-Type"));

            byte[] byteData = response.body().bytes();
            String rspSeed = response.header("Seed");
            if(null != rspSeed) {
                resp.setBody(sEncoder.decodeFromByte(byteData, rspSeed));
            } else {
                resp.setBody(byteData);
            }
        } catch (InterruptedIOException iie) {
           resp.setInterrupted(true);
        } catch (Exception e) {
            Logger.error(e);
        }
        return resp;
    }

    public static Response doGet(String url, String host, Map<String, String> params) {
        Request.Builder req = createBasicRequest(url, host, params, false);
        return doHttpRequest(req, null, true);
    }

    public static Response doGet(String url, Map<String, String> params) {
        return doGet(url, null, params);
    }

    public static Response doPost(String url, String host, byte[] body) {
        Request.Builder req = createBasicRequest(url, host, null, false);
        return doHttpRequest(req, body, true);
    }

    public static Response doPost(String url, byte[] body) {
        return doPost(url, null, body);
    }

    public static boolean doDownload(String url, File out) {
        Request.Builder req = createBasicRequest(url, null, null, false);
        try {
            okhttp3.Response response = getHttpClient(true).newCall(req.build()).execute();
            if(200 == response.code()) {
                FileHelper.copy(response.body().byteStream(), out);
                return true;
            }
        } catch (Exception e) {
            Logger.error(e);
        }
        return false;
    }

    public static InputStream getInputStream(String url) {
        Request.Builder req = createBasicRequest(url, null, null, false);
        try {
            okhttp3.Response response = getHttpClient(true).newCall(req.build()).execute();
            if(200 == response.code()) {
                return response.body()!=null?response.body().byteStream():null;
            }
        } catch (Exception e) {
            Logger.error(e);
        }
        return null;
    }

    public static long getFileLength(String url) {
        Request.Builder req = createBasicRequest(url, null, null, false);
        try {
            okhttp3.Response response = getHttpClient(true).newCall(req.build()).execute();
            if(200 == response.code()) {
                return response.body()!=null?response.body().contentLength():null;
            }
        } catch (Exception e) {
            Logger.error(e);
        }
        return 0;
    }

    public static class ProxyResponse {
        private String contentType;
        private long len;
        private int statusCode;
        private InputStream body;
        private Map<String, String> header = new HashMap<>();

        public int getStatusCode() {
            return statusCode;
        }

        public InputStream getBody() {
            return body;
        }

        public long getLength() {
            return len;
        }

        public Map<String, String> getHeader() {
            return header;
        }

        public String getContentType() {
            return contentType;
        }
    }

    public static ProxyResponse forwardGet(String url, String host, String seed, Map<String, String> params) {
        ProxyResponse proxyResp = new ProxyResponse();
        try {
            Request.Builder req = createBasicRequest(url, host, params, false);
            if(null != seed) {
                req.addHeader("Seed", seed);
            }

            okhttp3.Response response = getHttpClient(false).newCall(req.build()).execute();
            proxyResp.statusCode = response.code();
            for(Map.Entry<String, List<String>> data : response.headers().toMultimap().entrySet()) {
                proxyResp.header.put(data.getKey(), data.getValue().get(0));
            }
            MediaType type = response.body().contentType();
            if(null == type) {
                Logger.error("[FG] Missing Type @ " + url);
            } else {
                proxyResp.contentType = type.toString();
            }
            proxyResp.len = response.body().contentLength();
            proxyResp.body = response.body().byteStream();
        } catch (Exception e) {
            Logger.error(e);
        }
        return proxyResp;
    }

    public static ProxyResponse forwardPost(String url, String host, String seed, byte[] body) {
        ProxyResponse proxyResp = new ProxyResponse();
        try {
            Request.Builder req = createBasicRequest(url, host, null, false);
            if(null != seed) {
                req.addHeader("Seed", seed);
            }

            byte[] payload = sEncoder.encodeToByte(body, seed);
            req.post(RequestBody.create(TRANSFER, payload));

            okhttp3.Response response = getHttpClient(false).newCall(req.build()).execute();
            proxyResp.statusCode = response.code();
            for(Map.Entry<String, List<String>> data : response.headers().toMultimap().entrySet()) {
                proxyResp.header.put(data.getKey(), data.getValue().get(0));
            }

            MediaType type = response.body().contentType();
            if(null == type) {
                Logger.error("[FP] Missing Type @ " + url);
            } else {
                proxyResp.contentType = type.toString();
            }
            proxyResp.len = response.body().contentLength();
            proxyResp.body = response.body().byteStream();
        } catch (Exception e) {
            Logger.error(e);
        }
        return proxyResp;
    }
}
