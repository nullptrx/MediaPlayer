package com.alivc.player.logreport;

public class DownloadEvent {

    public static class DownloadEventArgs {
        public long downloadBytes;
        public long downloadDuration;
        public double mediaBitRate;
    }

    public static void sendEvent(DownloadEventArgs args, PublicPraram publicPraram) {
        EventUtils.sendUrl(publicPraram.getFinalUrl("3005", getArgsStr(args)));
    }

    private static String getArgsStr(DownloadEventArgs args) {
        StringBuilder sb = new StringBuilder();
        sb.append("dd=").append(args.downloadDuration).append("&");
        sb.append("db=").append(args.downloadBytes).append("&");
        sb.append("bitrate=").append(args.mediaBitRate);
        return EventUtils.urlEncode(sb.toString());
    }
}
