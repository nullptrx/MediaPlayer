package tv.lycam.player.utils;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;

public class NetWatchdog {
    private Activity mActivity;
    private NetWatchdog.NetChangeListener mNetChangeListener;
    private IntentFilter mIntentFilter = new IntentFilter();
    private BroadcastReceiver d = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            ConnectivityManager cm = (ConnectivityManager) context.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
            if (cm == null) {
                return;
            }
            NetworkInfo wifiNetworkInfo = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            NetworkInfo mobileNetworkInfo = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
            State wifiState = State.UNKNOWN;
            State mobileState = State.UNKNOWN;
            if (wifiNetworkInfo != null) {
                wifiState = wifiNetworkInfo.getState();
            }

            if (mobileNetworkInfo != null) {
                mobileState = mobileNetworkInfo.getState();
            }

            if (State.CONNECTED != wifiState && State.CONNECTED == mobileState) {
                if (NetWatchdog.this.mNetChangeListener != null) {
                    NetWatchdog.this.mNetChangeListener.onWifiTo4G();
                }
            } else if (State.CONNECTED == wifiState && State.CONNECTED != mobileState) {
                if (NetWatchdog.this.mNetChangeListener != null) {
                    NetWatchdog.this.mNetChangeListener.on4GToWifi();
                }
            } else if (State.CONNECTED != wifiState && State.CONNECTED != mobileState && NetWatchdog.this.mNetChangeListener != null) {
                NetWatchdog.this.mNetChangeListener.onNetDisconnected();
            }

        }
    };

    public static boolean hasNet(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) {
            return false;
        }
        NetworkInfo wifiNetworkInfo = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        NetworkInfo mobileNetworkInfo = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        State wifiState = State.UNKNOWN;
        State mobileState = State.UNKNOWN;
        if (wifiNetworkInfo != null) {
            wifiState = wifiNetworkInfo.getState();
        }

        if (mobileNetworkInfo != null) {
            mobileState = mobileNetworkInfo.getState();
        }

        return State.CONNECTED == wifiState || State.CONNECTED == mobileState;
    }

    public static boolean is4GConnected(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) {
            return false;
        }
        NetworkInfo mobileNetworkInfo = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        State mobileState = State.UNKNOWN;
        if (mobileNetworkInfo != null) {
            mobileState = mobileNetworkInfo.getState();
        }

        return State.CONNECTED == mobileState;
    }

    public NetWatchdog(Activity activity) {
        this.mActivity = activity;
        this.mIntentFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
    }

    public void setNetChangeListener(NetWatchdog.NetChangeListener l) {
        this.mNetChangeListener = l;
    }

    public void startWatch() {
        try {
            this.mActivity.registerReceiver(this.d, this.mIntentFilter);
        } catch (Exception var2) {
            ;
        }

    }

    public void stopWatch() {
        try {
            this.mActivity.unregisterReceiver(this.d);
        } catch (Exception var2) {
            ;
        }

    }

    public interface NetChangeListener {
        void onWifiTo4G();

        void on4GToWifi();

        void onNetDisconnected();
    }
}
