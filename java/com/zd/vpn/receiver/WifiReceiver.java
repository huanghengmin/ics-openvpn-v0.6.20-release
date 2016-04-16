package com.zd.vpn.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;

public class WifiReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(WifiManager.WIFI_STATE_CHANGED_ACTION)) {
            WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            int wifi_state = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE,WifiManager.WIFI_STATE_UNKNOWN);
            if (wifi_state == WifiManager.WIFI_STATE_ENABLED) {
                wifiManager.setWifiEnabled(false);
            }else if(wifi_state == WifiManager.WIFI_STATE_ENABLING){
                wifiManager.setWifiEnabled(false);
            }
        }
    }
}
