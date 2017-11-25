package com.alivc.player.downloader;

import android.os.Build.VERSION;
import android.util.Log;
import java.util.HashMap;
import java.util.Map;

public class DownloaderCallback {
    private static Map<Integer, IDownloadHandler> mHandlerMap = new HashMap();

    public static void setPlayingHandler(int playerId, IDownloadHandler h) {
        mHandlerMap.put(Integer.valueOf(playerId), h);
    }

    public static void removePlayingHandler(int playerId) {
        mHandlerMap.remove(Integer.valueOf(playerId));
    }

    public static int onNotification(int playerId, int what, int arg0, int arg1, String obj_id) {
        IDownloadHandler mHandler = (IDownloadHandler) mHandlerMap.get(Integer.valueOf(playerId));
        if (mHandler != null) {
            return mHandler.onStatus(what, arg0, arg1, obj_id);
        }
        Log.d("MPlayer", "not find handle. " + playerId);
        return -1;
    }

    public static int getAndroidVersion() {
        return VERSION.SDK_INT;
    }
}
