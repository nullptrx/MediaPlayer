package com.alivc.player.logreport;

public class ReportEvent {

    public static class ReportEventArgs {
        public long interval;
        public long videoTimeStampMs;
    }

    public static void sendEvent(ReportEventArgs args, PublicPraram publicPraram) {
        EventUtils.sendUrl(publicPraram.getFinalUrl("9001", getArgsStr(args)));
    }

    private static String getArgsStr(ReportEventArgs args) {
        StringBuilder sb = new StringBuilder();
        sb.append("vt=").append(args.videoTimeStampMs).append("&");
        sb.append("interval=").append(args.interval);
        return EventUtils.urlEncode(sb.toString());
    }
}
