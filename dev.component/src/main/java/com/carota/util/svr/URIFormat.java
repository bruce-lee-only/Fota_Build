/*******************************************************************************
 * Copyright (C) 2018-2020 CAROTA Technology Crop. <www.carota.ai>.
 * All Rights Reserved.
 *
 * Unauthorized using, copying, distributing and modifying of this file,
 * via any medium is strictly prohibited.
 *
 * Proprietary and confidential.
 ******************************************************************************/

package com.carota.util.svr;

import android.util.Log;

import com.carota.util.HttpHelper;
import com.carota.util.PrintHelper;

import java.util.ArrayList;
import java.util.List;

class URIFormat {

    public void test() {
        List<String[]> testPool = new ArrayList<>();
        testPool.add(new String[]{"A", "http://ota_temp/data/test", "ota_temp"});
        testPool.add(new String[]{"B", "http://ota_temp/data/test", null});
        testPool.add(new String[]{"C", "/ota_temp/data/test", "ota_temp"});
        testPool.add(new String[]{"D", "/ota_temp/data/test", null});
        testPool.add(new String[]{"E", "ota_temp/data/test", "ota_temp"});
        testPool.add(new String[]{"F", "ota_temp/data/test", null});

        testPool.add(new String[]{"G", "/data/test", "ota_temp"});
        testPool.add(new String[]{"H", "/data/test", null});
        testPool.add(new String[]{"I", "data/test", "ota_temp"});
        testPool.add(new String[]{"J", "data/test", null});

        for(String[] tester: testPool) {
            Log.e("Format", tester[0] + " @ " + formatUri(tester[1], tester[2]));
        }
        String path = "/ota_temp/data/file";
        String host = path;
        int pos = path.indexOf("/", 1);
        if(pos > 0) {
            host = path.substring(1, pos);
        }
        Log.e("Data", "Host = " + host + "; sub = " + path.substring(pos));

        String params;
        List<String> urls = new ArrayList<>();
        urls.add("http://local=host/data");
        urls.add("http://local=host/data?");
        urls.add("http://local=host/data?file");
        urls.add("http://local=host/data?file=");
        urls.add("http://local=host/data?file=1&");
        urls.add("http://local=host/data?file=1&data=value");
        urls.add("http://local=host/data?file=1&data=value&");
        urls.add("http://local=host/data?file=1&data=");

        for (String v : urls) {
            params = PrintHelper.print(HttpHelper.parseUrlParameters(v));
            Log.e("URL", v + " [" + params + "]");
        }
    }

    private String formatUri(String raw, String host) {
        StringBuilder uriCache = new StringBuilder(raw);
        // format uri remove head
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
        if(null != host) {
            if(1 != uriCache.indexOf(host)) {
                uriCache.insert(0, host);
                uriCache.insert(0, '/');
            }
        }
        return uriCache.toString();
    }
}
