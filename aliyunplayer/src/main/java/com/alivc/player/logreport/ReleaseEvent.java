package com.alivc.player.logreport;

public class ReleaseEvent {
    public static void sendEvent(PublicPraram publicPraram) {
        EventUtils.sendUrl(publicPraram.getFinalUrl("1002", ""));
    }
}
