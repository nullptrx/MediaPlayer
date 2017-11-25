package com.start;

import android.util.Log;

import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class SeeGlobal {
    public static String BaiduAppID;
    public static String BaiduPos1ID;
    public static String BaiduPos2ID;
    public static String BaiduSplashID;
    public static String GDTAppID;
    public static String GDTBannerID;
    public static String GDTSplashID;
    public static String SeeDownLoadURL;

    public static String read(InputStream in) throws IOException {
        return null;
    }

    public static void httpGetInfo() {
        new Thread(new Runnable() {
            public void run() {
                try {
                    HttpURLConnection conn = (HttpURLConnection) new URL("http://123.57.231.190/SeeService/getInfo.php").openConnection();
                    conn.setConnectTimeout(3000);
                    conn.setRequestMethod("GET");
                    if (conn.getResponseCode() == 200) {
                        String str = SeeGlobal.read(conn.getInputStream());
                        SeeGlobal.SeeDownLoadURL = ((JSONObject) new JSONTokener(str).nextValue()).getString("SeeDownLoadUrl");
                        Log.i("HTTP", str);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}
