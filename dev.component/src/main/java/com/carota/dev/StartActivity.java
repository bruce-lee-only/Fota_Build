package com.carota.dev;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.carota.dev.core.CorePreferenceFragment;
import com.carota.dev.dm.DownloadPreferenceFragment;
import com.carota.dev.mda.MasterPreferenceFragment;
import com.carota.dev.sda.SlavePreferenceFragment;
import com.carota.dev.sync.SyncPreferenceFragment;
import com.carota.usb.rsm.StoragePreferenceFragment;
import com.carota.util.exec.ExecPreferenceFragment;
import com.carota.util.jdb.JdbPreferenceFragment;
import com.carota.util.log.LogPreferenceFragment;
import com.carota.util.svr.HttpPreferenceFragment;

public class StartActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.add(R.id.fg_start, new StartPreferenceFragment());
        transaction.add(R.id.fg_start, new CorePreferenceFragment());
        transaction.add(R.id.fg_start, new DownloadPreferenceFragment());
        transaction.add(R.id.fg_start, new MasterPreferenceFragment());
        transaction.add(R.id.fg_start, new SlavePreferenceFragment());
        transaction.add(R.id.fg_start, new SyncPreferenceFragment());
        transaction.add(R.id.fg_start, new StoragePreferenceFragment());
        transaction.add(R.id.fg_start, new ExecPreferenceFragment());
        transaction.add(R.id.fg_start, new LogPreferenceFragment());
        transaction.add(R.id.fg_start, new HttpPreferenceFragment());
        transaction.add(R.id.fg_start, new JdbPreferenceFragment());
        transaction.commit();
    }

//    public void onClickHttp(View view) {
//        startActivity(new Intent(this, HttpActivity.class));
//    }
//
//    public void onClickDownload(View view) {
//        startActivity(new Intent(this, DownloadActivity.class));
//    }
//
//    public void onClickMaster(View view) {
//        startActivity(new Intent(this, MasterActivity.class));
//    }
//
//    public void onClickUnit(View view) {
//        startActivity(new Intent(this, ExecActivity.class));
//    }
//
//    public void onClickDownloadV2(View view) {
//        startActivity(new Intent(this,CoreActivity.class));
//    }
//
//    public void onClickSlave(View view) {
//        startActivity(new Intent(this, SlaveActivity.class));
//    }
//
//    public void onClickStorage(View view) {
//        startActivity(new Intent(this, StorageActivity.class));
//    }
//
//    public void onClickJdb(View view) {
//        startActivity(new Intent(this, JdbActivity.class));
//    }
//
//    public void onClickSync(View view) {
//        startActivity(new Intent(this, SyncActivity.class));
//    }
}
