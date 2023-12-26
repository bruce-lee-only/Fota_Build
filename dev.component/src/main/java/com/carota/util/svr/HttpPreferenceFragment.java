package com.carota.util.svr;

import android.app.DownloadManager;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.view.View;
import android.widget.TextView;

import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.carota.dev.R;
import com.carota.svr.PrivReqHelper;
import com.carota.util.HttpHelper;
import com.momock.util.EncryptHelper;
import com.momock.util.Logger;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class HttpPreferenceFragment extends PreferenceFragmentCompat implements ServiceConnection, Preference.OnPreferenceClickListener {
    private Handler handler = new Handler(Looper.getMainLooper());
    private HttpService mService;
    private final static String GET = "get";
    private final static String POST = "post";
    private final static String DOWNLOAD = "download";
    private final static String INTERRUPT = "interrupt";
    private final static String MSG = "msg";
    private Preference msgPreference;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.pref_http);
        getContext().bindService(new Intent(getContext(), HttpService.class), this, Service.BIND_AUTO_CREATE);
        new URIFormat().test();
        msgPreference = findPreference(MSG);
        findPreference(GET).setOnPreferenceClickListener(this);
        findPreference(POST).setOnPreferenceClickListener(this);
        findPreference(DOWNLOAD).setOnPreferenceClickListener(this);
        findPreference(INTERRUPT).setOnPreferenceClickListener(this);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        getContext().unbindService(this);
    }

    private synchronized String getReqUrl() {
        if (null != mService) {
            int port = mService.ensureReady();
            if (port > 0) {
                return "http://127.0.0.1:" + port + "/debug";
            }
        }
        return null;
    }

    public void onClickReqGet() {
        //测试get
        new Thread(new Runnable() {
            @Override
            public void run() {
                String url = getReqUrl();
                if (null != url) {
                    Map<String, String> map = new HashMap<>();
                    map.put("m", "GET");
                    Logger.error("URL GET = " + url);
                    final PrivReqHelper.Response response = PrivReqHelper.doGet(url, HttpService.SVR_HOST, map);
                    final String rspData = new String(response.getBody());
                    Logger.error("Resp [%d]: %s", response.getStatusCode(), rspData);
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            msgPreference.setTitle(rspData);
                        }
                    });
                } else {
                    Logger.error("Fail to do Req");
                }

            }
        }).start();
    }

    public void onClickReqPost() {
        //测试post
        new Thread(new Runnable() {
            @Override
            public void run() {
                String url = getReqUrl();
                if (null != url) {

                    JSONObject postBody = new JSONObject();
                    try {
                        postBody.put("dataStr", "this is test post body");
                        postBody.put("dataInt", 1234);
                        //postBody.put("dataBoolean", false);
                    } catch (JSONException je) {
                        Logger.error(je);
                    }
                    // final HttpHelper.Response response = HttpHelper.doPost(url, null, postBody);
                    final PrivReqHelper.Response response = PrivReqHelper.doPost(url, HttpService.SVR_HOST, postBody.toString().getBytes());
                    final String rspData = new String(response.getBody());
                    Logger.error("Resp [%d]: %s", response.getStatusCode(), rspData);
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            msgPreference.setTitle(rspData);
                        }
                    });
                } else {
                    Logger.error("Fail to do Get");
                }
            }
        }).start();
    }

    public void onClickReqDownload() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String url = getReqUrl();
                if (null == url) {
                    Logger.error("Fail to Start Downlod");
                    return;
                }
                Map<String, String> params = new HashMap<>();
                params.put("m", "DL");
                String realUrl = HttpHelper.getFullUrl(url, params);
                Logger.error("Download URL - %s", realUrl);
                // downloadViaDownloadMgr(realUrl);
                downloadViaMultPart(realUrl);
            }
        }).start();
    }

    public void onClickReqInterrupt() {
        //测试get
        final Thread tester = new Thread(new Runnable() {
            @Override
            public void run() {
                String url = getReqUrl();
                if (null != url) {
                    Map<String, String> map = new HashMap<>();
                    map.put("m", "DELAY");
                    final PrivReqHelper.Response response = PrivReqHelper.doGet(url, HttpService.SVR_HOST, map);
                    final String rspData = null == response.getBody() ? null : new String(response.getBody());
                    Logger.error("Resp [%d]: %s", response.getStatusCode(), rspData);
                    Logger.error("Resp Interrupt : " + response.isInterrupted());
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            msgPreference.setTitle(rspData);
                        }
                    });
                } else {
                    Logger.error("Fail to do Req");
                }

            }
        });
        tester.start();

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                tester.interrupt();
                Logger.error("Resp Interrupt DONE");
            }
        }, 2000);

    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        mService = ((HttpService.HttpBinder) service).get();
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        mService = null;
    }

    private void downloadViaDownloadMgr(String url) {
        DownloadManager.Request req = new DownloadManager.Request(Uri.parse(url));
        //req.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI);
        req.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE);
        //file:///storage/emulated/0/Download/update.apk
        req.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "TEST_DEV_DL");
        // 设置一些基本显示信息
        req.setTitle("测试下载");
        req.setDescription("文件下载描述");
        req.setMimeType("application/vnd.android.package-archive");
        DownloadManager mDownloadManager = (DownloadManager) getContext().getSystemService(Context.DOWNLOAD_SERVICE);
        //加入下载队列
        mDownloadManager.enqueue(req);
    }

    private void downloadViaMultPart(String url) {
        File file = new File(getContext().getFilesDir(), "test.demo");
        file.delete();
        WGetHelper.download(url, file);
        Logger.error("DST FILE MD5 = %s", EncryptHelper.calcFileMd5(file));
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        switch (preference.getKey()) {
            case GET:
                onClickReqGet();
                break;
            case POST:
                onClickReqPost();
                break;
            case DOWNLOAD:
                onClickReqDownload();
                break;
            case INTERRUPT:
                onClickReqInterrupt();
        }
        return true;
    }
}
