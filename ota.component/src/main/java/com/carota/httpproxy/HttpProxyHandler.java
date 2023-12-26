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

import com.carota.protobuf.HttpProxy;
import com.carota.protobuf.Telemetry;
import com.carota.svr.HttpResp;
import com.carota.svr.PrivStatusCode;
import com.carota.svr.SimpleHandler;
import com.carota.util.HttpHelper;
import com.momock.util.Logger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * http://ota_net_proxy/http_proxy
 */
public class HttpProxyHandler extends SimpleHandler {
    public HttpProxyHandler() {
    }

    @Override
    public HttpResp post(String path, Map<String, List<String>> params,
                         byte[] body, Object extra) {
        Telemetry.EmptyRsp.Builder builder = Telemetry.EmptyRsp.newBuilder();
        try {
            HttpProxy.HttpProxyReq httpProxyReq = HttpProxy.HttpProxyReq.parseFrom(body);
            HttpProxy.HeaderReq[] headerReqs = httpProxyReq.getHeaderList().toArray(new HttpProxy.HeaderReq[0]);

            Map<String, String> header = getHeaderMap(headerReqs);
            byte[] bytes = httpProxyReq.getBody().toByteArray();
            String url = httpProxyReq.getUrl();
            Logger.warn("HttpProxyHandler url = " + url);
            HttpHelper.HttpProxyResponse hpr = null;

            switch (httpProxyReq.getMethodValue()) {
                case HttpProxy.HttpProxyReq.Method.GET_VALUE:
                    hpr = HttpHelper.doHttpProxyGet(url, header);
                    break;
                case HttpProxy.HttpProxyReq.Method.POST_VALUE:
                    hpr = HttpHelper.doHttpProxyPost(url, header, bytes);
                    break;
                default:
                    break;
            }

            if (null != hpr) {
                return new HttpResp.ProxyResp(hpr.getStatusCode(),
                        hpr.getContentType(),
                        hpr.getHeader(),
                        hpr.getBody(),
                        hpr.getLength());
            }
        } catch (Exception e) {
            Logger.error(e);
        }

        return HttpResp.newInstance(PrivStatusCode.SRV_ACT_UNKNOWN, builder.build().toByteArray());
    }

    private Map<String, String> getHeaderMap(HttpProxy.HeaderReq[] headerReqs) {
        Map<String, String> map = new HashMap<>();
        if (headerReqs == null || headerReqs.length == 0) {
            return map;
        }

        for (HttpProxy.HeaderReq headerReq : headerReqs) {
            map.put(headerReq.getKey(), headerReq.getValStr());
        }
        return map;
    }
}
