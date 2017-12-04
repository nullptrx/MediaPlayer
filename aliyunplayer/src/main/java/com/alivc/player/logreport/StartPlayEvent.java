package com.alivc.player.logreport;

import android.util.Log;

public class StartPlayEvent {
    private final String TAG = "StartPlayEvent";

    public static void sendEvent(PublicPraram publicPraram) {
        Log.d("StartPlayEvent", "send start play event");
        EventUtils.sendUrl(publicPraram.getFinalUrl("1005", ""));
    }
}
