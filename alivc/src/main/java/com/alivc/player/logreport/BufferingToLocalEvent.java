package com.alivc.player.logreport;

public class BufferingToLocalEvent {

    public static class BufferingToLocalFinishArgs {
        public int video_duration_ms;
    }

    public static class BufferingToLocalStartArgs {
        public int cache_duration_ms;
        public int cache_size_mb;
    }

    public static void sendEvent(BufferingToLocalStartArgs args, PublicPraram publicPraram) {
        EventUtils.sendUrl(publicPraram.getFinalUrl("2023", getArgsStr(args)));
    }

    public static void sendEvent(BufferingToLocalFinishArgs args, PublicPraram publicPraram) {
        EventUtils.sendUrl(publicPraram.getFinalUrl("2024", getArgsStr(args)));
    }

    private static String getArgsStr(BufferingToLocalStartArgs args) {
        StringBuilder sb = new StringBuilder();
        sb.append("cache_duration=").append(args.cache_duration_ms).append("&");
        sb.append("cache_size=").append(args.cache_size_mb);
        return EventUtils.urlEncode(sb.toString());
    }

    private static String getArgsStr(BufferingToLocalFinishArgs args) {
        StringBuilder sb = new StringBuilder();
        sb.append("vd=").append(args.video_duration_ms);
        return EventUtils.urlEncode(sb.toString());
    }
}
