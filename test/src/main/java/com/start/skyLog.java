package com.start;

import android.util.Log;

public class skyLog {
    public static void testLog(String logString) {
    }

    public static void testLog(int logInt) {
        Log.d("skylog", String.valueOf(logInt));
    }
}
