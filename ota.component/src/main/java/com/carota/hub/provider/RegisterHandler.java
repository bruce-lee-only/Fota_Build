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
import com.carota.protobuf.Telemetry;
import com.carota.svr.HttpResp;
import com.carota.svr.PrivStatusCode;
import com.carota.svr.SimpleHandler;
import com.google.protobuf.InvalidProtocolBufferException;
import com.momock.util.Logger;

import java.util.List;
import java.util.Map;

import fi.iki.elonen.NanoHTTPD;

public class RegisterHandler extends SimpleHandler {

	@Override
	public HttpResp post(String path, Map<String, List<String>> params, byte[] body, Object extra) {
		PrivStatusCode code = PrivStatusCode.SRV_ACT_UNKNOWN;
		Telemetry.EmptyRsp.Builder builder = Telemetry.EmptyRsp.newBuilder();
		NanoHTTPD.IHTTPSession session = (NanoHTTPD.IHTTPSession) extra;
		String ip = session.getRemoteIpAddress();
		try {
			ServiceHub.RegisterReq req = ServiceHub.RegisterReq.parseFrom(body);
			RouteMap.get().addRemote(req.getPort(), req.getModulesList().toArray(new String[0]), ip);
			code = PrivStatusCode.OK;
		} catch (InvalidProtocolBufferException e) {
			Logger.error(e);
		}
		return HttpResp.newInstance(code, builder.build().toByteArray());
	}
}
