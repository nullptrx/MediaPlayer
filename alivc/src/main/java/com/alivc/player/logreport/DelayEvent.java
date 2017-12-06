package com.alivc.player.logreport;

public class DelayEvent {

    public static class DelayEventArgs {
        public long audioDurationFromDownloadToRenderMs;
        public long videoDurationFromDownloadToRenderMs;
    }

    public static void sendEvent(DelayEventArgs args, PublicPraram publicPraram) {
        EventUtils.sendUrl(publicPraram.getFinalUrl("9004", getArgsStr(args)));
    }

    private static String getArgsStr(DelayEventArgs args) {
        StringBuilder sb = new StringBuilder();
        sb.append("vrt=").append(args.videoDurationFromDownloadToRenderMs).append("&");
        sb.append("art=").append(args.audioDurationFromDownloadToRenderMs);
        return EventUtils.urlEncode(sb.toString());
    }
}
