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
import com.carota.protobuf.ServiceHub;
import com.carota.svr.HttpResp;
import com.carota.svr.PrivStatusCode;
import com.carota.svr.SimpleHandler;
import com.momock.util.Logger;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.Map;

import fi.iki.elonen.HttpStatusCode;

public class InfoHandler extends SimpleHandler {
	/**
	 * http://ota_proxy/info
	 */
	@Override
	public HttpResp get(String path, Map<String, List<String>> params, Object extra) {
		ServiceHub.InfoRsp.Builder builder = ServiceHub.InfoRsp.newBuilder();
		try {
			for (RouteMap.Route rt : RouteMap.get().listRoute()) {
				builder.addRoutes(ServiceHub.InfoRsp.Route.newBuilder()
						.setAddr(null == rt.getAddr() ? "" : rt.getAddr())
						.setModule(rt.getHost())
						.build());
			}
		} catch (Exception e) {
			Logger.error(e);
		}
		return HttpResp.newInstance(PrivStatusCode.OK, builder.build().toByteArray());
	}
}
