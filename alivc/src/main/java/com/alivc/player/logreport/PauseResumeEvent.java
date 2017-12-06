package com.alivc.player.logreport;

public class PauseResumeEvent {

    public static class PauseResumeEventArgs {
        public long costMs;
        public long videoTimeStampMs;
    }

    public static void sendEvent(PauseResumeEventArgs args, PublicPraram publicPraram) {
        EventUtils.sendUrl(publicPraram.getFinalUrl("2010", getArgsStr(args)));
    }

    private static String getArgsStr(PauseResumeEventArgs args) {
        StringBuilder sb = new StringBuilder();
        sb.append("vt=").append(args.videoTimeStampMs).append("&");
        sb.append("cost=").append(args.costMs);
        return EventUtils.urlEncode(sb.toString());
    }
}
