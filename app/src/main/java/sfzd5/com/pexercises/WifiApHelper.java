package sfzd5.com.pexercises;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.util.Log;

import java.io.BufferedReader;
import java.io.FileReader;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by fsp on 17-11-9.
 */

public class WifiApHelper {

    private String TAG = "WifiAp";

    public static final int WIFI_AP_STATE_DISABLING = 10;
    public static final int WIFI_AP_STATE_DISABLED = 11;
    public static final int WIFI_AP_STATE_ENABLING = 12;
    public static final int WIFI_AP_STATE_ENABLED = 13;
    public static final int WIFI_AP_STATE_FAILED = 14;


    public String getServer(Context mContext){
        if(isApEnabled(mContext)){
            List<String> ips = getConnectedHotIP();
            for(String ip : ips){
                if(ip.startsWith("192.168.43")){
                    return ip;
                }
            }
            return null;
        } else {
            return null;
        }
    }

    public int getWifiApState(Context mContext) {
        WifiManager wifiManager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
        try {
            Method method = wifiManager.getClass().getMethod("getWifiApState");
            int i = (Integer) method.invoke(wifiManager);
            Log.i(TAG,"wifi state:  " + i);
            return i;
        } catch (Exception e) {
            Log.e(TAG,"Cannot get WiFi AP state" + e);
            return WIFI_AP_STATE_FAILED;
        }
    }

    public boolean isApEnabled(Context mContext) {
        int state = getWifiApState(mContext);
        return WIFI_AP_STATE_ENABLING == state || WIFI_AP_STATE_ENABLED == state;
    }


    public ArrayList<String> getConnectedHotIP() {
        ArrayList<String> connectedIP = new ArrayList<String>();
        try {
            BufferedReader br = new BufferedReader(new FileReader(
                    "/proc/net/arp"));
            String line;
            while ((line = br.readLine()) != null) {
                String[] splitted = line.split(" +");
                if (splitted != null && splitted.length >= 4) {
                    String ip = splitted[0];
                    connectedIP.add(ip);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return connectedIP;
    }


    //输出链接到当前设备的IP地址
    public void printHotIp() {

        ArrayList<String> connectedIP = getConnectedHotIP();
        StringBuilder resultList = new StringBuilder();
        for (String ip : connectedIP) {
            resultList.append(ip);
            resultList.append("\n");
        }
        System.out.print(resultList);
        Log.d(TAG,"---->>heww resultList="+resultList);
    }

}
