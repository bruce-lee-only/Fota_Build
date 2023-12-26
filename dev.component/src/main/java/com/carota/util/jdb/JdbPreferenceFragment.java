package com.carota.util.jdb;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.carota.dev.R;
import com.momock.util.JsonDatabase;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class JdbPreferenceFragment extends PreferenceFragmentCompat implements Preference.OnPreferenceClickListener {
    private static final String TAG = "JdbActivity";
    private JsonDatabase.Collection mCacheCollection;
    private JsonDatabase.Collection mDataCollection;
    private static final int MAX_COUNT = 1000;
    private JsonDatabase mJsonDatabase;
    private final static String CACHE_INSERT = "cache_insert";
    private final static String CACHE_LIST = "cache_list";
    private final static String DATA_INSERT = "data_insert";
    private final static String DATA_LIST = "data_list";

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.pref_jdb);
        mJsonDatabase = JsonDatabase.get(getContext().getApplicationContext(), "DEBUG_JDB");
        mCacheCollection = mJsonDatabase.getCacheCollection("cache");
        mDataCollection = mJsonDatabase.getCollection("data");

        findPreference(CACHE_INSERT).setOnPreferenceClickListener(this);
        findPreference(CACHE_LIST).setOnPreferenceClickListener(this);
        findPreference(DATA_LIST).setOnPreferenceClickListener(this);
        findPreference(DATA_INSERT).setOnPreferenceClickListener(this);
    }

    public void onClickCacheInsert() {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < MAX_COUNT; i++) {
                    try {
                        mCacheCollection.set(String.valueOf(i), new JSONObject().put("index", "Cache-" + i));
                        Log.i(TAG, "onClickCacheInsert insert i = " + i);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    public void onClickCacheList() {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                List<JsonDatabase.Document> data;
                do {
                    data = mCacheCollection.list(new JsonDatabase.IFilter() {
                        @Override
                        public boolean check(String id, JSONObject doc) {
                            return true;
                        }
                    }, false, 20);
                    Log.i(TAG, "onClickCacheList data: " + data);
                    if (data != null) {
                        Log.i(TAG, "onClickCacheList data: " + data.size());
                    }
                    try {
                        for (JsonDatabase.Document doc : data) {
                            Log.i(TAG, "onClickCacheList jsonObject: " + doc.getData());
                            Thread.sleep(50);
                            mCacheCollection.set(doc.getId(), null);
                        }
                        Thread.sleep(50);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } while (null != data && data.size() > 0);
            }
        });
    }

    public void onClickDataInsert() {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < MAX_COUNT; i++) {
                    try {
                        mDataCollection.set(String.valueOf(i), new JSONObject().put("index", "Data-" + i));
                        Log.i(TAG, "onClickDataInsert insert i = " + i);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    public void onClickDataList() {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                List<JsonDatabase.Document> data;
                do {
                    data = mDataCollection.list(new JsonDatabase.IFilter() {
                        @Override
                        public boolean check(String id, JSONObject doc) {
                            return true;
                        }
                    }, false, 20);
                    Log.i(TAG, "onClickDataList data: " + data);
                    if (data != null) {
                        Log.i(TAG, "onClickDataList data: " + data.size());
                    }
                    try {
                        for (JsonDatabase.Document doc : data) {
                            Log.i(TAG, "onClickDataList jsonObject: " + doc.getData());
                            Thread.sleep(50);
                            mDataCollection.set(doc.getId(), null);
                        }
                        Thread.sleep(50);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } while (null != data && data.size() > 0);
            }
        });
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        switch (preference.getKey()) {
            case CACHE_INSERT:
                onClickCacheInsert();
                break;
            case CACHE_LIST:
                onClickCacheList();
                break;
            case DATA_INSERT:
                onClickDataInsert();
                break;
            case DATA_LIST:
                onClickDataList();
                break;
        }
        return true;
    }
}
