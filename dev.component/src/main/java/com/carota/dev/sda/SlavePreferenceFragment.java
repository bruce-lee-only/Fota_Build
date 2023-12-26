package com.carota.dev.sda;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;

import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.carota.dev.R;
import com.carota.sda.ISlaveDownloadAgent;
import com.carota.sda.SlaveInfo;
import com.carota.sda.SlaveState;
import com.carota.sda.SlaveTask;
import com.carota.sda.UpdateSlave;
import com.carota.sda.util.SlaveMethod;
import com.momock.util.FileHelper;
import com.momock.util.Logger;

import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class SlavePreferenceFragment extends PreferenceFragmentCompat implements Preference.OnPreferenceClickListener {
    private final static String UPGRADE = "upgrade";
    private final static String EVENT = "event";

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.pref_slave);
        findPreference(UPGRADE).setOnPreferenceClickListener(this);
        findPreference(EVENT).setOnPreferenceClickListener(this);
    }

    public void onClickStart() {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                startUpgrade(new UpdateSlave("slave_id", "slave_host",
                        "slave_id", "com.carota.agent",
                        60 * 1000, 0, new SlaveMethod(),  null));
            }
        });
    }

    public void onClickEvent() {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                startEvent(new UpdateSlave("slave_id", "slave_host",
                        "slave_id", "com.carota.agent",
                        60 * 1000, 0, new SlaveMethod(), null));
            }
        });
    }

    private SlaveTask createTask(String name) {
        try {
            String fileName = "target_package";
            FileHelper.writeText(new File(getContext().getCacheDir(), fileName), "this is test");
            return SlaveTask.parseFrom(new JSONObject()
                    .put("name", name)
                    .put("tv", "sw-9.0")
                    .put("dst", fileName)
            );
        } catch (Exception e) {
            Logger.error(e);
        }
        return null;
    }

    private void startEvent(ISlaveDownloadAgent sda) {
        sda.init(getContext(), getContext().getCacheDir());
        List<String> eventList = new ArrayList<>();
        List<String> idList = new ArrayList<>();
        if(sda.fetchEvent(null, 0, eventList, idList)) {
            for(String event: eventList) {
                Logger.debug("EVENT : " + event);
            }
            sda.deleteEvent(idList);
        }
        Logger.debug("EVENT : Finished");

        List<String> fileList = new ArrayList<>();
        if(sda.listLogFiles(null, 0, fileList)) {
            Logger.debug("LOG : READY");
        } else {
            Logger.debug("LOG : FAIL");
        }
        for(String f : fileList) {
            File log = sda.findLogFile(f);
            if(log.exists()) {
                Logger.debug("LOG : Find");
                log.delete();
            }
        }
        Logger.debug("LOG : Finished");
    }

    private void startUpgrade(ISlaveDownloadAgent sda) {
        String tag = sda.getId() + "@" + sda.getHost();
        Logger.info(tag);
        sda.init(getContext(), getContext().getCacheDir());
        SlaveInfo info = sda.readInfo();
        Logger.info(SlaveInfo.toJson(info).toString());
        //sda.init(this, getCacheDir());
        if(!sda.isRunning()) {
            if(sda.triggerUpgrade(createTask(sda.getId()))) {
                Logger.info("trigger Agent : " + tag);
            } else {
                Logger.error("trigger Agent Fail: " + tag);
                return;
            }
        } else {
            Logger.error("Agent is Running: " + tag);
        }
        Logger.info("Wait Agent : " + tag);
        do {
            try {
                SlaveState state = sda.queryState();
                Logger.debug(tag + " [" + state.getState() + "]-" + state.getProgress());
                Thread.sleep(700);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } while (sda.isRunning());
        Logger.info(tag + " Finished");
    }


    @Override
    public boolean onPreferenceClick(Preference preference) {
        switch (preference.getKey()) {
            case UPGRADE:
                onClickStart();
                break;
            case EVENT:
                onClickEvent();
        }
        return false;
    }
}
