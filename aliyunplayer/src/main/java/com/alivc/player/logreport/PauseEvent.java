package com.alivc.player.logreport;

public class PauseEvent {
    public static long mLastPauseVideoTime = -1;

    public static void sendEvent(long videoTimeStampMs, PublicPraram publicPraram) {
        mLastPauseVideoTime = System.currentTimeMillis();
        EventUtils.sendUrl(publicPraram.getFinalUrl("2003", getArgsStr(videoTimeStampMs)));
    }

    private static String getArgsStr(long videoTimeStamp) {
        StringBuilder sb = new StringBuilder();
        sb.append("vt=").append(videoTimeStamp);
        return EventUtils.urlEncode(sb.toString());
    }
}
