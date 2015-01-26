package org.wilds.quadcontroller.app;

import android.content.Context;
import android.net.DhcpInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

public class LinkQuality {
    private WifiManager wm;
    private int rssi = 0;
    private int linkspeed = 0;

    public LinkQuality(Context mContext) {
        wm = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
        DhcpInfo dhcpInfo = wm.getDhcpInfo();
        //gw = dhcpInfo.gateway;
    }

    public void update() {
        WifiInfo info = wm.getConnectionInfo();
        rssi = info.getRssi();
        linkspeed = info.getLinkSpeed();
    }

    public int getLinkSpeed() {
        return linkspeed;
    }

    public int getSignal() {
        return rssi;
    }

    public int getSignalLevel(int numLevels) {
        return wm.calculateSignalLevel(rssi, numLevels);
    }

}