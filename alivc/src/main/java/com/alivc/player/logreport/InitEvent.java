package com.alivc.player.logreport;

public class InitEvent {
    public static void sendEvent(boolean result, PublicPraram publicPraram) {
        EventUtils.sendUrl(publicPraram.getFinalUrl("1001", getArgsStr(result)));
    }

    private static String getArgsStr(boolean result) {
        StringBuilder sb = new StringBuilder();
        sb.append("init=").append(result);
        return EventUtils.urlEncode(sb.toString());
    }
}
