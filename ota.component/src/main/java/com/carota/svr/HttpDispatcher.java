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

import com.momock.util.Logger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fi.iki.elonen.NanoHTTPD;

public class HttpDispatcher implements IHttpDispatcher {

    private final Map<String, IHttpHandler> mHandlerMap;

    public HttpDispatcher() {
        mHandlerMap = new HashMap<>();
    }

    /**  find path handler
     *   eg :
     *   if path is /one/two/three
     *   search key will be
     *      /one/two/three
     *      /one/two/*
     *      /one/*
     *      /*
      */

    IHttpHandler findHandler(String uri) {
//        Logger.debug("HD-Find raw path : " + uri);
        IHttpHandler handler = mHandlerMap.get(uri);
        if(null == handler) {
            StringBuilder urlCache = new StringBuilder(uri);
            while (urlCache.length() > 0) {
                int endIndex = urlCache.length() - 1;
                char end = urlCache.charAt(endIndex);
                if('/' == end) {
                    urlCache.append('*');
//                    Logger.debug("HD-Find sub path : " + urlCache.toString());
                    handler = mHandlerMap.get(urlCache.toString());
                    if(null != handler) {
                        return handler;
                    }
                }
                urlCache.setLength(endIndex);
            }
        }
        return handler;
    }

    private String formatUri(String host, String uri) {
        StringBuilder uriCache = new StringBuilder(uri);
        // format uri remove http head
        String httpHead = "http:/";
        if(0 == uriCache.indexOf(httpHead)) {
            uriCache.delete(0, httpHead.length());
        }
        // format uri remote parameter
        int qmi = uriCache.indexOf("?");
        if(qmi > 0) {
            uriCache.delete(qmi, uriCache.length() - 1);
        }
        // format uri add '/' at head
        if('/' != uriCache.charAt(0)) {
            uriCache.insert(0, '/');
        }
        // format uri add host if exist
        if(null != host && !host.contains(":") && 1 != uriCache.indexOf(host)) {
            uriCache.insert(0, host);
            uriCache.insert(0, '/');
        }
        return uriCache.toString();
    }

    @Override
    public HttpResp process(NanoHTTPD.IHTTPSession session, byte[] postData) throws UnsupportedOperationException {
        NanoHTTPD.Method reqMethod = session.getMethod();
//        StringBuilder headers = new StringBuilder();
//        for(Map.Entry<String, String> entry : session.getHeaders().entrySet()) {
//            headers.append('[').append(entry.getKey()).append("-").append(entry.getValue()).append("]; ");
//        }
//        Logger.error("SEC Header = " + headers.toString());
        String uri = formatUri(session.getHeaders().get("host"), session.getUri());
//        Logger.error("SEC URL = " + uri);
        IHttpHandler handler = findHandler(uri);
        if(null == handler) {
            throw new UnsupportedOperationException();
        }
        Map<String, List<String>> params = session.getParameters();
        switch (reqMethod) {
            case GET:
                return handler.get(uri, params, session);
            case POST:
                return handler.post(uri, params, postData, session);
            default:
                return handler.other(uri, params, session);
        }
    }

    @Override
    public void setHandler(String host, String path, IHttpHandler handler) {
        if(TextUtils.isEmpty(path)) {
            return;
        }
        // format path : "/path"
        StringBuilder strPath = new StringBuilder(path);
        if('/' != strPath.charAt(0)) {
            strPath.insert(0, '/');
        }
        int lastIndex = path.length() - 1;
        if ('/' == path.charAt(lastIndex)) {
            strPath.deleteCharAt(lastIndex);
        }

        if(null != host) {
            // add path : "/host/path"
            strPath.insert(0, host);
            strPath.insert(0, '/');
        }
        setHandler(strPath.toString(), handler);
    }

    @Override
    public void removeAllHandlers() {
        mHandlerMap.clear();
    }

    private void setHandler(String path, IHttpHandler handler) {
        if (null == handler) {
            Logger.info("HD-Unregister Path : %s", path);
            mHandlerMap.remove(path);
        } else {
            Logger.info("HD-Register Path : %s", path);
            mHandlerMap.put(path, handler);
        }
    }

}
