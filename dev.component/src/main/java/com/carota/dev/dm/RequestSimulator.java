package com.carota.dev.dm;


import android.app.DownloadManager;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;

import com.carota.svr.PrivReqHelper;
import com.carota.util.HttpHelper;
import com.momock.service.MessageService;
import com.momock.util.Logger;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.List;

public class RequestSimulator {
    private static final RequestSimulator mInstance = new RequestSimulator();
    private String URL_BASE;
    private MessageService messageService;
    private DownloadManager downloadManager;

    public static RequestSimulator get() {
        return mInstance;
    }

    public void init(String url, MessageService messageService) {
        URL_BASE = url;
        this.messageService = messageService;
    }

    public void sendDownloadInfo(List<String> id) {
        Logger.info("start send download info");
//        {"download":[{"url:": "www.ssss/sss","dmd5": "dadada","name": "ecu"},....]}
        try {
            JSONObject testJson = new JSONObject();
            JSONArray dataArray = new JSONArray();
            /*
            JSONObject jsonMcu = new JSONObject();
            jsonMcu.put("id", "082afe08f66b5d8a3c0942e3855d4c73");
            jsonMcu.put("url", "http://rock.fotapro.com/files/d/082afe08f66b5d8a3c0942e3855d4c73");
            jsonMcu.put("md5", "082afe08f66b5d8a3c0942e3855d4c73");
            jsonMcu.put("desc", "MCU");
            dataArray.put(jsonMcu);
            JSONObject jsonAp = new JSONObject();
            jsonAp.put("id", "ee9c156c05e36f5bb2db8d497033eeb1");
            jsonAp.put("url", "http://rock.fotapro.com/files/d/ee9c156c05e36f5bb2db8d497033eeb1");
            jsonAp.put("md5", "ee9c156c05e36f5bb2db8d497033eeb1");
            jsonAp.put("desc", "AP_MODEM");
            dataArray.put(jsonAp);
            */
            JSONObject jsonApk;
//            jsonApk = new JSONObject();
//            jsonApk.put("id", "test1");
//            jsonApk.put("url", "http://sdk.generalmobi.com/files/082b2ebd10d1be234c6dec912facc0be.as/082b2ebd10d1be234c6dec912facc0be.apk");
//            jsonApk.put("md5", "082B2EBD10D1BE234C6DEC912FACC0BE");
//            jsonApk.put("desc", "APK1");
//            dataArray.put(jsonApk);
//            jsonApk = new JSONObject();
//            jsonApk.put("id", "test2");
//            jsonApk.put("url", "http://sdk.generalmobi.com/files/082b2ebd10d1be234c6dec912facc0be.as/082b2ebd10d1be234c6dec912facc0be.apk");
//            jsonApk.put("md5", "");
//            jsonApk.put("desc", "APK2");
//            dataArray.put(jsonApk);
//            for (int i = 0; i < id.size(); i++) {
            jsonApk = new JSONObject();
            jsonApk.put("id", /*id.get(i)*/"0d1b24327f55fae05afdf3b16a854b2a");
            jsonApk.put("url", "http://api.reachthings.com/files/d/0d1b24327f55fae05afdf3b16a854b2a");
            jsonApk.put("md5", "0d1b24327f55fae05afdf3b16a854b2a");
//                jsonApk.put("md5", md5);
            jsonApk.put("desc", "APK2");
            dataArray.put(jsonApk);
//            }
            testJson.put("download", dataArray);
            Logger.info("testJson:" + testJson.toString());
            PrivReqHelper.Response response = PrivReqHelper.doPost(URL_BASE + "dl", testJson.toString().getBytes());
            messageService.send(null, IMessageTopic.TOPIC_DOWNLOAD_RESPONSE, new Result(response.getStatusCode(), new String(response.getBody())));
        } catch (Exception e) {
            Logger.error(e);
        }
    }

    public void getDownloadProgress() {
        PrivReqHelper.Response response = PrivReqHelper.doGet(URL_BASE + "pg", null);
        messageService.send(null, IMessageTopic.TOPIC_PROGRESS_RESPONSE,
                new Result(response.getStatusCode(), new String(response.getBody())));
    }

    public void getUpdatePackage(Context context, String id) {
//        if(HttpHelper.download(URL_BASE+"file?file=" + dmd5, path) > 1) {
//            messageService.send(null, IMessageTopic.TOPIC_GET_FILE_RESPONSE, "get file finished\n");
//        }
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(URL_BASE + "file?id=" + id));
        request.setVisibleInDownloadsUi(true);
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE | DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setTitle(id);
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, id);
        if (null == downloadManager)
            downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        downloadManager.enqueue(request);

//        loadMap.put(id, loadId);
    }

    public void deleteDownloadedPackage() {
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("cmd", "delete");
//            JSONArray ecusArray = new JSONArray();
//            ecusArray.put(id);
//            jsonObject.put("id", ecusArray);
            PrivReqHelper.Response response = PrivReqHelper.doPost(URL_BASE + "cmd", jsonObject.toString().getBytes());
            messageService.send(null, IMessageTopic.TOPIC_DELETE_FILE_RESPONSE,
                    new Result(response.getStatusCode(), new String(response.getBody())));

        } catch (JSONException e) {
            Logger.error(e);
        }
    }

    public void stopDownload() {
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("cmd", "stop");
            PrivReqHelper.Response response = PrivReqHelper.doPost(URL_BASE + "cmd", jsonObject.toString().getBytes());
            messageService.send(null, IMessageTopic.TOPIC_STOP_DOWNLOAD_RESPONSE,
                    new Result(response.getStatusCode(), new String(response.getBody())));
        } catch (JSONException e) {
            Logger.error(e);
        }
    }

}
