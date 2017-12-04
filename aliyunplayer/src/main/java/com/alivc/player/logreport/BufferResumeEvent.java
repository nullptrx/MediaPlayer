package com.alivc.player.logreport;

public class BufferResumeEvent {

    public static class BufferResumeEventArgs {
        public long costMs;
        public long videoTimeStampMs;
    }

    public static void sendEvent(BufferResumeEventArgs args, PublicPraram publicPraram) {
        EventUtils.sendUrl(publicPraram.getFinalUrl("3001", getArgsStr(args)));
    }

    private static String getArgsStr(BufferResumeEventArgs args) {
        StringBuilder sb = new StringBuilder();
        sb.append("vt=").append(args.videoTimeStampMs).append("&");
        sb.append("cost=").append(args.costMs);
        return EventUtils.urlEncode(sb.toString());
    }
}
