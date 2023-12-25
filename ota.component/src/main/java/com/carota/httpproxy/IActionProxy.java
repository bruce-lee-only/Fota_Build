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

import com.carota.util.HttpHelper;

import java.util.Map;

public interface IActionProxy {

    HttpHelper.Response doProxyGet(String url, Map<String, String> params);

    HttpHelper.Response doProxyPost(String url, Map<String, String> params, Map<String, String> headers, Object body);

    boolean isNeedProxy(String url);
}
