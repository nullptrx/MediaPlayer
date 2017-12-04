package com.alivc.player.logreport;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.provider.Settings.Secure;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.WindowManager;
import com.alivc.player.HttpClientUtil;
import com.alivc.player.VcPlayerLog;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.URLEncoder;
import java.util.Enumeration;
import java.util.UUID;

public class EventUtils {
    private static String TAG = "usertrace";
    private static boolean isPad = false;
    private static boolean isPadDecied = false;
    private static String localUUID = null;
    private static final String uuid_file = "aliuuid";

    public static String urlEncode(String url) {
        if (url != null) {
            try {
                return URLEncoder.encode(url, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        return "";
    }

    public static String getNetWorkType(Context context) {
        NetworkInfo networkInfo = ((ConnectivityManager) context.getApplicationContext().getSystemService("connectivity")).getActiveNetworkInfo();
        if (networkInfo == null) {
            return "";
        }
        int nType = networkInfo.getType();
        if (nType == 0) {
            return "cellnetwork";
        }
        if (nType == 1) {
            return "WiFi";
        }
        return "";
    }

    public static String getIp() {
        InetAddress inetAddress = null;
        try {
            Enumeration<NetworkInterface> en_netInterface = NetworkInterface.getNetworkInterfaces();
            while (en_netInterface.hasMoreElements()) {
                Enumeration<InetAddress> en_ip = ((NetworkInterface) en_netInterface.nextElement()).getInetAddresses();
                while (en_ip.hasMoreElements()) {
                    inetAddress = (InetAddress) en_ip.nextElement();
                    if (!inetAddress.isLoopbackAddress() && inetAddress.getHostAddress().indexOf(":") == -1) {
                        break;
                    }
                    inetAddress = null;
                }
                if (inetAddress != null) {
                    break;
                }
            }
            if (inetAddress == null) {
                return "127.0.0.1";
            }
            return inetAddress.getHostAddress();
        } catch (Exception e) {
            e.printStackTrace();
            if (inetAddress == null) {
                return "127.0.0.1";
            }
            return inetAddress.getHostAddress();
        } catch (Throwable th) {
            if (inetAddress == null) {
                return "127.0.0.1";
            }
            return inetAddress.getHostAddress();
        }
    }

    public static boolean isPad(Context context) {
        if (isPadDecied) {
            return isPad;
        }
        try {
            Display display = ((WindowManager) context.getApplicationContext().getSystemService("window")).getDefaultDisplay();
            DisplayMetrics dm = new DisplayMetrics();
            display.getMetrics(dm);
            if (Math.sqrt(Math.pow((double) (((float) dm.widthPixels) / dm.xdpi), 2.0d) + Math.pow((double) (((float) dm.heightPixels) / dm.ydpi), 2.0d)) >= 6.0d) {
                isPad = true;
            } else {
                isPad = false;
            }
            isPadDecied = true;
            return isPad;
        } catch (Exception e) {
            VcPlayerLog.e("isPad", "get window service failed :" + e.getMessage());
            return false;
        }
    }

    public static String createUuid(Context context) {
        try {
            TelephonyManager tm = (TelephonyManager) context.getApplicationContext().getSystemService("phone");
            return new UUID((long) ("" + Secure.getString(context.getContentResolver(), "android_id")).hashCode(), (((long) ("" + tm.getDeviceId()).hashCode()) << 32) | ((long) ("" + tm.getSimSerialNumber()).hashCode())).toString();
        } catch (Exception e) {
            VcPlayerLog.e("CreateUuid", "failed " + e.getMessage());
            return null;
        }
    }

    private static String getString(InputStream inputStream) {
        InputStreamReader inputStreamReader = null;
        try {
            inputStreamReader = new InputStreamReader(inputStream, "utf-8");
        } catch (UnsupportedEncodingException e1) {
            e1.printStackTrace();
        }
        BufferedReader reader = new BufferedReader(inputStreamReader);
        StringBuilder sb = new StringBuilder("");
        while (true) {
            try {
                String line = reader.readLine();
                if (line == null) {
                    break;
                }
                sb.append(line);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return sb.toString();
    }

    static String getLocalUuid(Context context) {
        if (localUUID != null) {
            return localUUID;
        }
        File f = new File(context.getFilesDir().getAbsolutePath() + "/" + "aliuuid");
        if (f.exists()) {
            try {
                FileInputStream inputStream = context.openFileInput("aliuuid");
                if (inputStream != null) {
                    localUUID = getString(inputStream);
                    inputStream.close();
                }
                if (localUUID.length() != 36) {
                    f.delete();
                    localUUID = getLocalUuid(context);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            localUUID = UUID.randomUUID().toString();
            try {
                FileOutputStream os = context.openFileOutput("aliuuid", 0);
                os.write(localUUID.getBytes());
                os.flush();
                os.close();
            } catch (IOException e2) {
                e2.printStackTrace();
            }
        }
        return localUUID;
    }

    public static void sendUrl(final String url) {
        VcPlayerLog.d(TAG, "usertrace : url = " + url);
        new Thread(new Runnable() {
            public void run() {
                try {
                    HttpClientUtil.doHttpsGet(url);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}
