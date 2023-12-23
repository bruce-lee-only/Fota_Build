/*******************************************************************************
 * Copyright (C) 2018-2020 CAROTA Technology Crop. <www.carota.ai>.
 * All Rights Reserved.
 *
 * Unauthorized using, copying, distributing and modifying of this file,
 * via any medium is strictly prohibited.
 *
 * Proprietary and confidential.
 ******************************************************************************/

package com.carota.hub.provider;

import com.carota.hub.RouteMap;
import com.carota.svr.HttpResp;
import com.carota.svr.IHttpHandler;
import com.carota.svr.PrivReqHelper;
import com.carota.svr.PrivStatusCode;
import com.momock.util.Logger;

import java.util.List;
import java.util.Map;

import fi.iki.elonen.NanoHTTPD;

public class ProxyHandler implements IHttpHandler {

	@Override
	public HttpResp get(String path, Map<String, List<String>> params, Object extra) {
		return forward(path, null, (NanoHTTPD.IHTTPSession) extra);
	}

	@Override
	public HttpResp post(String path, Map<String, List<String>> params, byte[] body, Object extra) {
		return forward(path, body, (NanoHTTPD.IHTTPSession)extra);
	}

	@Override
	public HttpResp other(String path, Map<String, List<String>> params, Object extra) {
		return null;
	}

	private HttpResp forward(String path, byte[] body, NanoHTTPD.IHTTPSession s) {
		try {
			//NOTE : eg path - /ota_dm/file
			String host = path;
			int pos = path.indexOf('/', 1);
			if(pos > 0) {
				host = path.substring(1, pos);
			}
			RouteMap.Route rt = RouteMap.get().findRemoteRoute(host);
			if(null != rt && null != rt.getAddr()) {
				String url = "http://" + rt.getAddr() + path.substring(pos);

				Map<String, String> headers = s.getHeaders();
				String seed = headers.get("seed");

				if(NanoHTTPD.Method.GET == s.getMethod()) {
					PrivReqHelper.ProxyResponse pr = PrivReqHelper.forwardGet(url, host, seed, s.getParms());
					if(pr.getStatusCode() > 0) {
						return new HttpResp.ProxyResp(pr.getStatusCode(), pr.getContentType(), pr.getHeader(), pr.getBody(), pr.getLength());
					}
				} else if(NanoHTTPD.Method.POST == s.getMethod()) {
					PrivReqHelper.ProxyResponse pr = PrivReqHelper.forwardPost(url, host, seed, body);
					if(pr.getStatusCode() > 0) {
						return new HttpResp.ProxyResp(pr.getStatusCode(), pr.getContentType(), pr.getHeader(), pr.getBody(), pr.getLength());
					}
				}
			}
		} catch (Exception e) {
			Logger.error(e);
		}
		return HttpResp.newInstance(PrivStatusCode.REQ_TARGET_UNKNOWN);
	}
}
