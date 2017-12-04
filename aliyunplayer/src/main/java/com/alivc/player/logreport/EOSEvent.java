package com.alivc.player.logreport;

public class EOSEvent {
    public static void sendEvent(long videoTimeStampMs, PublicPraram publicPraram) {
        EventUtils.sendUrl(publicPraram.getFinalUrl("2002", getArgsStr(videoTimeStampMs)));
    }

    private static String getArgsStr(long videoTimeStampMs) {
        StringBuilder sb = new StringBuilder();
        sb.append("vt=").append(videoTimeStampMs);
        return EventUtils.urlEncode(sb.toString());
    }
}
