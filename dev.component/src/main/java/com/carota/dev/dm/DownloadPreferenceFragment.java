package com.carota.dev.dm;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;

import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SeekBarPreference;

import com.carota.dev.R;
import com.carota.dev.ServiceHelper;
import com.carota.util.LZStringHelper;
import com.momock.message.IMessageHandler;
import com.momock.message.Message;
import com.momock.service.MessageService;
import com.momock.service.UITaskService;
import com.momock.util.EncryptHelper;
import com.momock.util.Logger;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class DownloadPreferenceFragment extends PreferenceFragmentCompat implements Preference.OnPreferenceClickListener {
    private final static String START_SERVER = "start_server";
    private final static String START_DOWNLOAD = "start_download";
    private final static String GET_PERCENT = "get_percent";
    private final static String STOP_DOWNLOAD = "stop_download";
    private final static String CLEAR_PACKAGE = "clear_package";
    private final static String GET_FILE = "get_file";
    private final static String SYNC_LOG = "sync_log";
    private final static String DEL_LOG = "del_log";
    private final static String RESP_LOG = "resp_log";
    private final static String DOWNLOAD_PROGRESS = "download_progress";
    private final static String DOWNLOAD_MSG = "download_msg";

    private String url;
    private MessageService messageService;
    private boolean isGetProgress = false;
    private Thread thread;
    private BroadcastReceiver receiver;
    private static String[] ids = new String[10];
    private static JSONArray mJSONArray = new JSONArray();

    private List<String> idList = new ArrayList<>();
    private SeekBarPreference dwSeekBar;
    private Preference msgPreference;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.pref_download);
        initId();
        dwSeekBar = findPreference(DOWNLOAD_PROGRESS);
        msgPreference = findPreference(DOWNLOAD_MSG);
        dwSeekBar.setValue(0);
        messageService = new MessageService();
        messageService.start();

        url = "http://127.0.0.1:20003/";

        RequestSimulator.get().init(url, messageService);
        setListener();
    }

    private void initId() {
        for (int i = 0; i < 10; i++) {
            idList.add("test" + i);
        }
        //文件校验
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                long id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
                if (id != -1) {
                    String path = getPath(id);
                    Logger.info("download  ID " + id + " ; Path : " + path);
                    if (!TextUtils.isEmpty(path)) {
                        String md5 = EncryptHelper.calcFileMd5(new File(path));
                        Logger.info("download  id" + id + "MD5:" + md5);
                        if (md5.equals("0d1b24327f55fae05afdf3b16a854b2a")) {
                            Logger.info("download  id" + id + "MD5校验完成");
                        } else {
                            Logger.info("download  id" + id + "MD5校验失败");
                        }
                    } else {
                        Logger.info("download  id" + id + "File not exists");
                    }
                }
            }
        };
        // 注册广播监听系统的下载完成事件。
        IntentFilter intentFilter = new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
        getActivity().registerReceiver(receiver, intentFilter);
    }

    private String getPath(long downloadID) {
        DownloadManager downloadManager = (DownloadManager) getActivity().getSystemService(Context.DOWNLOAD_SERVICE);
//        String savePath = "";
        DownloadManager.Query query = new DownloadManager.Query();
        query.setFilterById(downloadID);
        String fileName = "";
        Cursor c = downloadManager.query(query);
        if (c.moveToFirst()) {

            int fileUriIdx = c.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI);
            String fileUri = c.getString(fileUriIdx);

            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
                if (fileUri != null) {
                    fileName = Uri.parse(fileUri).getPath();
                }
            } else {
                //Android 7.0以上的方式：请求获取写入权限，这一步报错
                //过时的方式：DownloadManager.COLUMN_LOCAL_FILENAME
                int fileNameIdx = c.getColumnIndex(DownloadManager.COLUMN_LOCAL_FILENAME);
                fileName = c.getString(fileNameIdx);
            }

            //获取文件下载路径
//            savePath = c.getString(c.getColumnIndex(DownloadManager.COLUMN_LOCAL_FILENAME));
//            int columnIndex = c.getColumnIndex(DownloadManager.COLUMN_STATUS);
//            if (DownloadManager.STATUS_SUCCESSFUL == c.getInt(columnIndex)) {
//                savePath = c.getString(c.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI));
//            }
        }
        c.close();
        return fileName;
    }

    /**
     * 关于DM信息的回调
     */
    private void setListener() {
        final UITaskService uiTaskService = new UITaskService();
        uiTaskService.start();
        messageService.addHandler(IMessageTopic.TOPIC_DOWNLOAD_RESPONSE, new IMessageHandler() {
            @Override
            public void process(Object sender, final Message msg) {
                uiTaskService.run(new Runnable() {
                    @Override
                    public void run() {
//                        isGetProgress = false;
                        Result data = (Result) msg.getData();
                        String message = data.toString();
                        msgPreference.setTitle("getDownloadResponse:" + message);
                        Logger.error("getDownloadResponse:" + msg.getData());
                    }
                });
            }
        });

        messageService.addHandler(IMessageTopic.TOPIC_PROGRESS_RESPONSE, new IMessageHandler() {
            @Override
            public void process(Object sender, final Message msg) {
                uiTaskService.run(new Runnable() {
                    @Override
                    public void run() {
                        Result data = (Result) msg.getData();
//                        Logger.error("getProgressResponse:" + data.getBody());
                        try {
                            JSONObject pro = new JSONObject(data.getBody());
                            Logger.info(" download progress :" + pro.toString());
//                            for (int i = 0; i < idList.size(); i++) {
                            JSONObject jsonObject = pro.getJSONObject("0d1b24327f55fae05afdf3b16a854b2a");

                            if (jsonObject.getInt("status") == 2) {
                                dwSeekBar.setValue(jsonObject.getInt("progress"));
//                                dw_progress_num.setText(jsonObject.getString("progress"));
                                dwSeekBar.setTitle(jsonObject.getString("id"));
                                return;
                            }
//                            }
                            isGetProgress = false;
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
//                        tv.setText("getProgressResponse:" + message);
//                        Logger.error("getProgressResponse:" +message);
                    }
                });
            }
        });
        messageService.addHandler(IMessageTopic.TOPIC_GET_FILE_RESPONSE, new IMessageHandler() {
            @Override
            public void process(Object sender, final Message msg) {
                uiTaskService.run(new Runnable() {
                    @Override
                    public void run() {
                        Result data = (Result) msg.getData();
                        String message = data.toString();
                        msgPreference.setTitle("getFileResponse:" + message);
                        Logger.error("getFileResponse:" + message);
                    }
                });
            }
        });
        messageService.addHandler(IMessageTopic.TOPIC_STOP_DOWNLOAD_RESPONSE, new IMessageHandler() {
            @Override
            public void process(Object sender, final Message msg) {
                uiTaskService.run(new Runnable() {
                    @Override
                    public void run() {
                        Result data = (Result) msg.getData();
                        String message = data.toString();
                        msgPreference.setTitle("stopDownloadResponse:" + message);
                        Logger.error("stopDownloadResponse:" + message);
                    }
                });
            }
        });
        messageService.addHandler(IMessageTopic.TOPIC_DELETE_FILE_RESPONSE, new IMessageHandler() {
            @Override
            public void process(Object sender, final Message msg) {
                uiTaskService.run(new Runnable() {
                    @Override
                    public void run() {
                        Result data = (Result) msg.getData();
                        String message = data.toString();
                        msgPreference.setTitle("deleteFileResponse:" + message);
                        Logger.error("deleteFileResponse:" + message);
                    }
                });
            }
        });
        messageService.addHandler(IMessageTopic.TOPIC_LOG_FILE_RESPONSE, new IMessageHandler() {
            @Override
            public void process(Object sender, final Message msg) {
                uiTaskService.run(new Runnable() {
                    @Override
                    public void run() {
                        Result data = (Result) msg.getData();
                        String message = data.getBody();
                        int curProgress = Integer.parseInt(message.split(File.separator)[0]);
                        int maxProgress = Integer.parseInt(message.split(File.separator)[1]);
                        dwSeekBar.setValue(curProgress);
                        dwSeekBar.setMax(maxProgress);
//                        dw_progress_num.setText("上传进度：" + message);
                    }
                });
            }
        });
    }

    private void startDMService() {
        ServiceHelper.startService(getContext());
        msgPreference.setTitle("server has started, url:" + url);
    }

    private void startDownLoadTask() {
        new Thread() {
            @Override
            public void run() {
                RequestSimulator.get().sendDownloadInfo(null);
                super.run();
            }
        }.start();
        isGetProgress = true;
//        getDownProgress();
    }

    private void stopDownLoadTask() {
        new Thread() {
            @Override
            public void run() {
                RequestSimulator.get().stopDownload();
                super.run();
            }
        }.start();
    }

    private void getDownProgress() {
        if (thread != null) return;
        thread = new Thread() {
            @Override
            public void run() {
                while (true) {
                    try {
                        if (isGetProgress) RequestSimulator.get().getDownloadProgress();
//                        RequestSimulator.get().queryFileLoadProgress();
                        sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
//                    Logger.error("循环回调");
                }
            }
        };
        thread.start();
    }

    private void getDownFile() {
        new Thread() {
            @Override
            public void run() {
                for (int i = 0; i < idList.size(); i++) {
                    RequestSimulator.get().getUpdatePackage(getContext(), idList.get(i));
                }
            }
        }.start();
    }

    private void syncLogFile() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                //RequestSimulator.get().syncLogFile(this);
            }
        }).start();
    }

    private void clearDMPackage() {
        new Thread() {
            @Override
            public void run() {
                RequestSimulator.get().deleteDownloadedPackage();
                super.run();
            }
        }.start();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        getContext().unregisterReceiver(receiver);
    }

    //action = get & pop
    public static void doGetRequest() {
        URL url = null;
        try {
            String urlStr = "http://127.0.0.1:20003/log?action=get&size=20";
            url = new URL(urlStr);
            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setRequestMethod("POST");
            int code = httpURLConnection.getResponseCode();
            Logger.info("code:" + code);
            if (code == 200) {
                InputStream inputStream = httpURLConnection.getInputStream();
                String content = LZStringHelper.decompress(inputStream);
                Logger.info("content:" + content);
                JSONObject jsonObject = new JSONObject(content);
                JSONArray idsArray = jsonObject.getJSONArray("id");
                mJSONArray = idsArray;
                Logger.debug("mJSONArray:" + mJSONArray.toString());
            }
        } catch (Exception e) {
            Logger.error("exception:" + e.toString());
        }
    }

    public static void doPostRequestDel() {
        URL url = null;
        try {
            String urlStr = "http://127.0.0.1:20003/log?action=del";
            url = new URL(urlStr);
            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setDoInput(true);
            httpURLConnection.setDoOutput(true);
            httpURLConnection.setRequestMethod("POST");
            OutputStream outputStream = httpURLConnection.getOutputStream();
            outputStream.write(mJSONArray.toString().getBytes());
            Logger.debug("outputstream :" + mJSONArray.toString());
            int code = httpURLConnection.getResponseCode();
            Logger.info("code:" + code);
            if (code == 200) {
                InputStream inputStream = httpURLConnection.getInputStream();
                String content = LZStringHelper.decompress(inputStream);
                Logger.info("content:" + content);
            }

        } catch (Exception e) {
            Logger.error("exception:" + e.toString());
        }

    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        switch (preference.getKey()) {
            case START_SERVER:
                startDMService();
                break;
            case START_DOWNLOAD:
                startDownLoadTask();
                break;
            case STOP_DOWNLOAD:
                stopDownLoadTask();
                break;
            case CLEAR_PACKAGE:
                clearDMPackage();
                break;
            case GET_FILE:
                getDownFile();
                break;
            case SYNC_LOG:
                syncLogFile();
                break;
            case GET_PERCENT:
                getDownProgress();
                break;
            case RESP_LOG:
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        doGetRequest();
                    }
                }).start();
                break;
            case DEL_LOG:
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        doPostRequestDel();
                    }
                }).start();
        }
        return false;
    }
}
