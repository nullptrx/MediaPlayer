package com.alivc.player.logreport;

public class BufferEvent {
    public static long mLastBufferVideoTime = -1;

    public static class BufferEventArgs {
        public String error_code;
        public String error_msg;
        public long videoTimeStampMs;
    }

    public static void sendEvent(BufferEventArgs args, PublicPraram publicPraram) {
        mLastBufferVideoTime = System.currentTimeMillis();
        EventUtils.sendUrl(publicPraram.getFinalUrl("3002", getArgsStr(args)));
    }

    private static String getArgsStr(BufferEventArgs args) {
        StringBuilder sb = new StringBuilder();
        sb.append("vt=").append(args.videoTimeStampMs).append("&");
        sb.append("error_code=").append(args.error_code).append("&");
        sb.append("error_msg=").append(args.error_msg);
        return EventUtils.urlEncode(sb.toString());
    }
}
