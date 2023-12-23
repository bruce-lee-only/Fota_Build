package com.carota.versions;

import android.content.Context;

import com.carota.build.IConfiguration;
import com.carota.build.ParamLocal;
import com.carota.build.ParamRoute;
import com.carota.util.ConfigHelper;
import com.carota.util.HttpHelper;
import com.carota.util.SerialExecutor;
import com.momock.util.JsonHelper;
import com.momock.util.Logger;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class HistoryVersions implements IHistoryVersion{
    private final int mSize = 5;

    private int mPage = 1;

    private int mTotalPage = 0;

    private Context mContext;

    private IHistoryCallback mCallback;

    private ArrayList<HVersionInfo> arrayInfo = new ArrayList<>();

    private static final String tag = "HistoryVersions";

    private static final String CALL_TAG = "[Query-History-Versions] ";

    private static String BaseUrl = "https://api-fota-uat.mychery.com:8443";

    private static final String infoAddress = "info";
    private static final String detailAddress = "detail";

    private static final int SUCCESS = 1;

    private static final Object sLocker = new Object();

    private static HistoryVersions mHistoryVersions;

    private static final SerialExecutor sExecutor = new SerialExecutor();

    private static void init(IHistoryCallback callback, Context context) {
        synchronized (sLocker) {
            if (mHistoryVersions == null) {
                mHistoryVersions = new HistoryVersions();
                mHistoryVersions.mContext = context;
                BaseUrl = mHistoryVersions.getRealUrl() + "/tsp/ota/vcp/v0/schedule/";
            }else {
                mHistoryVersions.arrayInfo = new ArrayList<>();
                mHistoryVersions.mPage = 1;
                mHistoryVersions.mTotalPage = 0;
                mHistoryVersions.mCallback = callback;
                mHistoryVersions.mContext = context;
                BaseUrl = mHistoryVersions.getRealUrl() + "/tsp/ota/vcp/v0/schedule/";
            }
        }
    }

    private String getRealUrl(){
        ParamLocal paramLocal = ConfigHelper.get(mContext).get(ParamLocal.class);
        if (ConfigHelper.isTestModeEnabled(mContext)){
            return paramLocal.getTestBaseUrl();
        }else {
            return paramLocal.getProductionBaseUrl();
        }
    }

    public static String queryVersions(String vin, String lang, IHistoryCallback callback, Context context){
        init(callback, context);

        if (!sExecutor.isEmpty() || sExecutor.isRunning()) {
            Logger.debug("queryVersions is running, return");
            return "";
        }

        sExecutor.execute(new Runnable() {
            @Override
            public void run() {
                while (true){
                    try {
                        JSONObject root = new JSONObject();
                        root.put("vin", vin);
                        root.put("lang", lang);
                        root.put("page", mHistoryVersions.mPage);
                        root.put("size", mHistoryVersions.mSize);

                        Logger.debug(CALL_TAG + "BODY : %1s", root.toString());
                        HttpHelper.Response response = HttpHelper.doPost(BaseUrl + infoAddress, null, root);
                        Logger.info(CALL_TAG + "RSP : %1d", response.getStatusCode());
                        Logger.info(CALL_TAG + "DATA : %1s", response.getBody());
                        if (response.getStatusCode() != 0){
                            analysisInfoBody(response.getBody());
                        }else {
                            return;
                        }
                        Thread.sleep(500);
                    } catch (Exception e) {
                        Logger.error(CALL_TAG + e);
                    }

                    if (mHistoryVersions.mPage == mHistoryVersions.mTotalPage || mHistoryVersions.mTotalPage == 0){
                        break;
                    }else {
                        mHistoryVersions.mPage += 1;
                    }
                }

                callback.HistoryInfo(mHistoryVersions.arrayInfo);
            }
        });
        return "";
    }

    public static String queryDetail(String taskId, String vin, String  language){
        try {
            JSONObject root = new JSONObject();
            root.put("vin", vin);
            root.put("lang", language);
            root.put("task_id", taskId);

            Logger.debug(CALL_TAG + "BODY : %1s", root.toString());
            HttpHelper.Response response = HttpHelper.doPost(BaseUrl + detailAddress, null, root);
            Logger.info(CALL_TAG + "RSP : %1d", response.getStatusCode());
            Logger.info(CALL_TAG + "DATA : %1s", response.getBody());
            if (response.getStatusCode() == 200){
                return response.getBody();
            }
        } catch (Exception e) {
            Logger.error(CALL_TAG + e);
        }
        return "";
    }

    private static void analysisInfoBody(String body){
        JSONObject jsonObject = JsonHelper.parseObject(body);
        try {
            int code = jsonObject.getInt("code");
            String msg = jsonObject.getString("msg");
            if (code != 0){
                Logger.error("error code:%d, error msg: %s",code, msg);
            }else {
                JSONObject dataObject = jsonObject.getJSONObject("data");
                mHistoryVersions.mTotalPage = dataObject.getInt("total_page"); // 获取total_page字段的值
                JSONArray tasksArray = dataObject.getJSONArray("tasks");
                for (int i = 0; i < tasksArray.length(); i++) {
                    JSONObject taskJson = tasksArray.getJSONObject(i);

                    //todo: 过滤掉升级失败的任务
                    int result = taskJson.getInt("result");
                    if (result != SUCCESS)
                        continue;

                    HVersionInfo info = new HVersionInfo();
                    info.title = taskJson.getString("title");
                    info.taskId = taskJson.getString("task_id");
                    info.timestamp = taskJson.getLong("_at");

                    mHistoryVersions.arrayInfo.add(info);
                }
            }
        } catch (JSONException e) {
            Logger.error("queryVersions JSONException: " + e);
            throw new RuntimeException(e);
        }
    }
}
