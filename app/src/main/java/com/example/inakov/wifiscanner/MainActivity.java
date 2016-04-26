package com.example.inakov.wifiscanner;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends Activity {

    WifiManager mainWifi;
    WifiReceiver receiverWifi;
    ListView listview;

    StringBuilder sb = new StringBuilder();

    private final Handler handler = new Handler();


    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d("MainActivity", "onCreate called.");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        WifiConfiguration conf = new WifiConfiguration();
        conf.SSID = "CatchMeIfYouCan";
        conf.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);

        mainWifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        int netId = mainWifi.addNetwork(conf);
        mainWifi.enableNetwork(netId, true);
        Log.d("MainActivity", "mainWifi manger acquired." + mainWifi.toString());

        receiverWifi = new WifiReceiver();

        registerReceiver(receiverWifi, new IntentFilter(
                WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));

        if(!mainWifi.isWifiEnabled()){
            Log.d("MainActivity", "Enabling wifi.");
            mainWifi.setWifiEnabled(true);
        }


        doInback();

        listview = (ListView) findViewById(R.id.networks);
        String[] values = new String[] { "Home", "Work"};

        final ArrayList<String> list = new ArrayList<String>();
        for (int i = 0; i < values.length; ++i) {
            list.add(values[i]);
        }
        final StableArrayAdapter adapter = new StableArrayAdapter(this,
                android.R.layout.simple_list_item_1, list);
        listview.setAdapter(adapter);

        FloatingActionButton refresh = (FloatingActionButton) findViewById(R.id.fab);

        refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mainWifi.startScan();
                Log.d("MainActivity", "Refresh clicked");

            }
        });

    }

    private class StableArrayAdapter extends ArrayAdapter<String> {

        HashMap<String, Integer> mIdMap = new HashMap<String, Integer>();

        public StableArrayAdapter(Context context, int textViewResourceId,
                                  List<String> objects) {
            super(context, textViewResourceId, objects);
            for (int i = 0; i < objects.size(); ++i) {
                mIdMap.put(objects.get(i), i);
            }
        }

        @Override
        public long getItemId(int position) {
            String item = getItem(position);
            return mIdMap.get(item);
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }

    }


    public void doInback(){
        Log.d("MainActivity", "doInback called.");
        handler.postDelayed(new Runnable() {

            @Override
            public void run(){
                Log.d("MainActivity", "run recursive scan.");
                mainWifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);

                receiverWifi = new WifiReceiver();
                registerReceiver(receiverWifi, new IntentFilter(
                        WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
                mainWifi.startScan();
                doInback();
            }
        }, 2000);

    }

    @Override
    protected void onPause()
    {
        unregisterReceiver(receiverWifi);
        super.onPause();
    }

    @Override
    protected void onResume()
    {
        registerReceiver(receiverWifi, new IntentFilter(
                WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        super.onResume();
    }

    class WifiReceiver extends BroadcastReceiver {

        public void onReceive(Context c, Intent intent) {
            Log.d("WifiReceiver", "onReceive.");
            ArrayList<String> connections=new ArrayList<String>();
            ArrayList<Float> Signal_Streelenth= new ArrayList<Float>();

            sb = new StringBuilder();
            List<ScanResult> wifiList;
            wifiList = mainWifi.getScanResults();
            Log.d("WifiReceiver", "Number of ScanResults: " + wifiList.size());
            for (int i = 0; i < wifiList.size(); i++){
                Log.d("WifiReceiver", "Result SSID - " + wifiList.get(i).SSID);
                connections.add(wifiList.get(i).SSID + " " + wifiList.get(i).level);
            }

            final StableArrayAdapter adapter = new StableArrayAdapter(c,
                    android.R.layout.simple_list_item_1, connections);
            listview.setAdapter(adapter);
        }
    }
}
