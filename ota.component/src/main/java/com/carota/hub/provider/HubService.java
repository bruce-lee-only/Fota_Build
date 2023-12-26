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

import android.content.Intent;

import com.carota.hub.RouteMap;
import com.carota.svr.IRouterHttp;
import com.carota.svr.RouterService;
import com.momock.util.Logger;

import java.util.ArrayList;
import java.util.List;

public class HubService extends RouterService {

	private List<String> mLocal;
	public HubService(String name) {
		super(name, false);
		mLocal = new ArrayList<>();
	}

	public void addLocalServiceHost(String name) {
		mLocal.add(name);
	}

	@Override
	public void onInit(IRouterHttp server) {
		Logger.debug("Hub Service onInit");
		server.setRequestHandler(mModuleName, "/info", new InfoHandler());
		server.setRequestHandler(mModuleName, "/register", new RegisterHandler());
		server.setRequestHandler(null, "/*", new ProxyHandler());
	}

	@Override
	public void onStart(IRouterHttp server) {
		RouteMap.get().setLocal(mLocal);
	}

	@Override
	public void onStop(IRouterHttp server) {

	}

	@Override
	public void onWakeUp(IRouterHttp server, Intent i) {

	}
}
