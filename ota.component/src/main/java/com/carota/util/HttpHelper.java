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

import android.content.Context;
import android.text.TextUtils;

import com.carota.build.ParamLocal;
import com.carota.protobuf.HttpProxy;
import com.carota.svr.PrivReqHelper;
import com.momock.util.FileHelper;
import com.momock.util.Logger;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import kotlin.text.Charsets;
import okhttp3.Call;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.internal.Util;
import okio.BufferedSink;
import okio.ByteString;
import okio.Okio;
import okio.Source;

public class HttpHelper {

    private static final MediaType JSON = MediaType.parse(MimeType.JSON);
    private static final MediaType TEXT = MediaType.parse(MimeType.TEXT);
    private static final MediaType CJ = MediaType.parse(MimeType.LZSTRING);
    private static final MediaType STREAM = MediaType.parse(MimeType.STREAM);

    public static class Response {
        private boolean interrupted;
        private int statusCode;
        private String body;

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

        public String getBody() {
            return body;
        }

        public void setBody(String body) {
            this.body = body;
        }
    }

    public static String getUrlParameter(String url, String key) {
        Map<String, String> params = parseUrlParameters(url);
        return params.get(key);
    }

    public static String getUrlPath(String url) {
        if (url == null) return null;
        int pos1 = url.indexOf("://");
        if (pos1 == -1) return null;
        pos1 += 3;
        int pos2 = url.indexOf('?');
        return pos2 > pos1 ? url.substring(pos1, pos2) : url.substring(pos1);
    }

    public static Map<String, String> parseUrlParameters(String url) {
        Map<String, String> params = new HashMap<>();
        int pos;
        if (null == url || 0 > (pos = url.indexOf('?')) || url.length() <= (pos + 1)) {
            return params;
        }

        String query = url.substring(pos + 1);
        String[] pairs = query.split("&");
        try {
            for (String pair : pairs) {
                if(0 > (pos = pair.indexOf('=')) || pair.length() <= (pos + 1)){
                    continue;
                }
                params.put(pair.substring(0, pos),
                        URLDecoder.decode(pair.substring(pos + 1), "UTF-8"));
            }
        } catch (Exception e) {
            Logger.error(e);
        }
        return params;
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

    private static OkHttpClient getHttpClient() {
        synchronized (OkHttpClient.class) {
            if(null == sDirectClient) {
                sDirectClient = new OkHttpClient.Builder().readTimeout(60, TimeUnit.SECONDS).retryOnConnectionFailure(true).build();
            }
        }
        return sDirectClient;
    }

    private static Response doHttpRequest(Request.Builder req, Map<String, String> headers) {
        Response resp = new Response();
        try {
            if (headers != null && headers.size() > 0) {
                for (Map.Entry<String, String> entry : headers.entrySet()) {
                    req.addHeader(entry.getKey(), entry.getValue());
                }
            }
            okhttp3.Response response = getHttpClient().newCall(req.build()).execute();
            resp.setStatusCode(response.code());

            String type = response.header("Content-Type");
            // Logger.debug("HttpHelper Content-Type : " + type);
            if(MimeType.LZSTRING.equals(type)) {
                resp.setBody(LZStringHelper.decompress(response.body().byteStream()));
            } else {
                resp.setBody(response.body().string());
            }
        } catch (InterruptedIOException iie) {
            Logger.error(iie);
           resp.setInterrupted(true);
        } catch (Exception e) {
            Logger.error(e);
        }
        return resp;
    }

    public static Response doGet(String url, Map<String, String> params) {
        if (isNeedProxy(url)) {
            return doProxy(url, params, null, null, 1);
        }

        Request.Builder req = new Request.Builder().url(getFullUrl(url, params));
        return doHttpRequest(req, null);
    }

    public static Response doPost(String url, Map<String, String> params, Object body) {
        return doPost(url, params, null, body);
    }

    public static Response doPost(String url, Map<String, String> params, Map<String, String> headers, Object body) {
        if (isNeedProxy(url)) {
            return doProxy(url, params, headers, body, 0);
        }

        Request.Builder req = new Request.Builder().url(getFullUrl(url, params));
        if(body instanceof JSONObject || body instanceof JSONArray) {
            req.post(RequestBody.create(JSON, body.toString()));
        } else if (body instanceof File) {
            req.post(RequestBody.create(STREAM, (File) body));
        } else if(body instanceof ByteString) {
            req.post(RequestBody.create(CJ, (ByteString) body));
        }else if(body instanceof byte[]) {
            req.post(RequestBody.create(STREAM,(byte[]) body));
        } else if(body instanceof InputStream) {
            req.post(create(STREAM, (InputStream) body));
        } else {
            req.post(RequestBody.create(TEXT, null == body ? "" : body.toString()));
        }
        return doHttpRequest(req, headers);
    }

    private static RequestBody create(final MediaType contentType, final InputStream input) {
        if (input == null) throw new NullPointerException("content == null");

        return new RequestBody() {
            @Override
            public MediaType contentType() {
                return contentType;
            }

            @Override
            public long contentLength() {
                try {
                    return input.available();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return 0;
            }

            @Override
            public void writeTo(BufferedSink sink) throws IOException {
                Source source = null;
                try {
                    source = Okio.source(input);
                    sink.writeAll(source);
                } finally {
                    Util.closeQuietly(source);
                }
            }
        };
    }

    /**
     * 获取网络文件长度
     *
     * @param url
     * @return
     */
    public static long getFileLength(String url) {
        long length = 0;
        Request request = new Request.Builder()
                .url(url)
                .build();
        Call call = getHttpClient().newCall(request);
        try {
            okhttp3.Response response = call.execute();
            length = response.body().contentLength();
        } catch (Exception e) {
            Logger.error(e);
        }
        call.cancel();
        return length;
    }

    /**
     * 分片下载
     *
     * @param startIndex 起始位置
     * @param endIndex   结束位置
     * @param url        下载地址
     * @return
     * @throws IOException
     */
    public static DownloadCall downloadFile(final long startIndex, final long endIndex, String url) throws IOException {
        Request request = new Request.Builder()
                .header("RANGE", "bytes=" + startIndex + "-" + endIndex)
                .url(url)
                .build();
        return new DownloadCall(getHttpClient().newCall(request));
    }

    /**
     * 下载文件
     * @param url        下载地址
     * @return
     * @throws IOException
     */
    public static DownloadCall downloadFile(String url) throws IOException {
        Request request = new Request.Builder()
                .header("RANGE", "bytes=0-" + getFileLength(url))
                .url(url)
                .build();
        return new DownloadCall(getHttpClient().newCall(request));
    }


    /**
     * 分片下载
     *
     * @param startIndex 起始位置
     * @param url        下载地址
     * @return
     */
    public static DownloadCall downloadFile(final long startIndex, String url) {
        Request request = new Request.Builder()
                .header("RANGE", "bytes=" + startIndex + "-")
                .url(url)
                .build();
        return new DownloadCall(getHttpClient().newCall(request));
    }

    public static boolean downloadFile(String url, File out) {
        Request request = new Request.Builder()
                .url(url)
                .build();
        try {
            okhttp3.Response response = getHttpClient().newCall(request).execute();
            if (200 == response.code()) {
                InputStream stream = response.body().byteStream();
                FileHelper.copy(stream, out);
                stream.close();
                return true;
            }
        } catch (IOException e) {
            Logger.error(e);
        }
        return false;
    }

    public static class DownloadCall {
        private final Call mCall;
        private InputStream mInputStream;

        public DownloadCall(Call call) {
            mCall = call;
        }


        public void close() {
            mCall.cancel();
            if (mInputStream != null) {
                try {
                    mInputStream.close();
                } catch (IOException e) {
                    Logger.error(e);
                }
            }
        }

        /**
         * 返回下载流
         *
         * @return
         */
        public InputStream getInputStream() {
            try {
                okhttp3.Response response = mCall.execute();
                if (response.code() == 200 || response.code() == 206) {
                    mInputStream = response.body().byteStream();
                    return mInputStream;
                } else {
                    Logger.info("DF Connect Fail Code：%d ", response.code());
                }
            } catch (IOException e) {
                Logger.error(e);
            }
            return null;
        }


    }

    //Add for Http Proxy
    public static HttpProxyResponse doHttpProxyGet(String url,
                                                   Map<String, String> headers) {
        HttpProxyResponse httpProxyResponse = null;
        try {
            Request.Builder req = new Request
                    .Builder()
                    .url(getFullUrl(url, null));

            if (headers != null && headers.size() > 0) {
                for (Map.Entry<String, String> entry : headers.entrySet()) {
                    req.addHeader(entry.getKey(), entry.getValue());
                }
            }

            okhttp3.Response response = getHttpClient().newCall(req.build()).execute();
            httpProxyResponse = new HttpProxyResponse(response);
        } catch (Exception e) {
            Logger.error(e);
        }

        return httpProxyResponse;
    }

    public static HttpProxyResponse doHttpProxyPost(String url,
                                                    Map<String, String> headers,
                                                    byte[] body) {
        HttpProxyResponse httpProxyResponse = null;
        String type = "application/json";
        if (headers != null && headers.containsKey("Content-Type")) {
            type = headers.get("Content-Type");
        }
        if (type == null) {
            type = "application/json";
        }
        try {
            Request.Builder req = new Request
                    .Builder()
                    .url(getFullUrl(url, null));

            if (headers != null && headers.size() > 0) {
                for (Map.Entry<String, String> entry : headers.entrySet()) {
                    req.addHeader(entry.getKey(), entry.getValue());
                }
            }

            if (type.startsWith("application/json")) {
                req.post(RequestBody.create(JSON, new String(body)));
            } else if (type.startsWith("application/octet-stream")) {
                req.post(RequestBody.create(STREAM, body));
            } else if (type.startsWith("application/cj")) {
                req.post(RequestBody.create(CJ, ByteString.of(body, 0, body.length)));
            } else {
                req.post(RequestBody.create(TEXT, null == body ? "" : new String(body)));
            }

            if (headers != null && headers.size() > 0) {
                for (Map.Entry<String, String> entry : headers.entrySet()) {
                    req.addHeader(entry.getKey(), entry.getValue());
                }
            }

            okhttp3.Response response = getHttpClient().newCall(req.build()).execute();
            httpProxyResponse = new HttpProxyResponse(response);
        } catch (Exception e) {
            Logger.error(e);
        }
        return httpProxyResponse;
    }

    public static class HttpProxyResponse {
        private String contentType;
        private long len;
        private int statusCode;
        private InputStream body;
        private Map<String, String> header = new HashMap<>();

        public HttpProxyResponse(okhttp3.Response response) {
            statusCode = response.code();
            Logger.error("[HttpProxyResponse] statusCode = " + statusCode);
            for (Map.Entry<String, List<String>> data : response.headers().toMultimap().entrySet()) {
                header.put(data.getKey(), data.getValue().get(0));
            }

            MediaType type = response.body().contentType();
            if (null == type) {
                Logger.error("[HttpProxyResponse] Missing Type");
            } else {
                contentType = type.toString();
                Logger.warn("[HttpProxyResponse] contentType = " + contentType);
            }
            len = response.body().contentLength();
            Logger.warn("[HttpProxyResponse] len = " + len);
            body = response.body().byteStream();
        }

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

    /**
     * 使用代理发出网络请求
     *
     * @param url
     * @param params
     * @param headers     请求头
     * @param body
     * @param proxyMethod 0为POST，1为GET
     * @return 请求结果
     */
    private static Response doProxy(String url,
                                    Map<String, String> params,
                                    Map<String, String> headers,
                                    Object body,
                                    int proxyMethod) {
        HttpProxy.HttpProxyReq.Builder builder = HttpProxy.HttpProxyReq.newBuilder();

        //RequestBody requestBody = null;
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
                //requestBody = RequestBody.create(STREAM, (File) body);
                conType = STREAM.toString();
                contentLength = ((File) body).length();
                InputStream inputStream = null;
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    inputStream = Files.newInputStream(((File) body).toPath());
                }
                builder.setBody(com.google.protobuf.ByteString.readFrom(inputStream));
            } else if (body instanceof ByteString) {
                //requestBody = RequestBody.create(CJ, (ByteString) body);
                conType = CJ.toString();
                byte[] bytes = ((ByteString) body).toByteArray();
                contentLength = bytes.length;
                builder.setBody(com.google.protobuf.ByteString.copyFrom(bytes));
            } else if (body instanceof byte[]) {
                //requestBody = RequestBody.create(STREAM, (byte[]) body);
                conType = STREAM.toString();
                contentLength = ((byte[]) body).length;
                InputStream inputStream = new ByteArrayInputStream((byte[]) body);
                builder.setBody(com.google.protobuf.ByteString.readFrom(inputStream));
            } else if (body instanceof InputStream) {
                //requestBody = create(STREAM, (InputStream) body);
                conType = STREAM.toString();
                contentLength = ((InputStream) body).available();
                builder.setBody(com.google.protobuf.ByteString.readFrom((InputStream) body));
            } else {
                //requestBody = RequestBody.create(TEXT, null == body ? "" : body.toString());
                conType = TEXT.toString();
                Charset charset = null;
                MediaType contentType = TEXT;
                charset = contentType.charset();
                if (charset == null) {
                    charset = StandardCharsets.UTF_8;
                    contentType = MediaType.parse(contentType + "; charset=utf-8");
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
            String sendUrl = getFullUrl(url, params);
            builder.setUrl(sendUrl);
            builder.setMethod(method);
        } catch (Exception e) {
            Logger.error("doProxy e = " + e.getMessage());
        }
        return doProxyRequest(builder.build());
    }

    private static Response doProxyRequest(HttpProxy.HttpProxyReq req) {
        Response resp = new Response();
        try {
            PrivReqHelper.Response privResp = PrivReqHelper.doPost("http://ota_net_proxy/http_proxy", req.toByteArray());
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

    private static boolean isNeedProxy(String url) {
        if (TextUtils.isEmpty(url)) {
            return false;
        }

//        https://api-fota-uat.mychery.com:8443/v0/data
        //todo: 非PKI
//        if (url.startsWith("https://api-fota.mychery.com")
//         || url.startsWith("https://api-fota-uat.mychery.com")) {
//            return true;
//        }

        //todo: PKI
        if (url.startsWith("https://apis-fota.mychery.com")
         || url.startsWith("https://apis-fota-uat.mychery.com")) {
            return true;
        }

        return false;
    }
}
