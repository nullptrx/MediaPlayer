package com.alivc.player;

import android.util.Log;

public class VcPlayerLog {
    public static boolean SHOW_LOG = false;

    public static void enableLog() {
        SHOW_LOG = true;
    }

    public static void disableLog() {
        SHOW_LOG = false;
    }

    public static void v(String tag, String msg) {
        if (SHOW_LOG) {
            Log.v(tag, msg);
        }
    }

    public static void d(String tag, String msg) {
        if (SHOW_LOG) {
            Log.d(tag, msg);
        }
    }

    public static void i(String tag, String msg) {
        if (SHOW_LOG) {
            Log.i(tag, msg);
        }
    }

    public static void w(String tag, String msg) {
        if (SHOW_LOG) {
            Log.w(tag, msg);
        }
    }

    public static void e(String tag, String msg) {
        if (SHOW_LOG) {
            Log.e(tag, msg);
        }
    }
}
