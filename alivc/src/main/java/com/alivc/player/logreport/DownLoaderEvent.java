package com.alivc.player.logreport;

public class DownLoaderEvent {

    public class DownLoaderErrorEventArgs {
        int error_code = 0;
        String error_msg = "";
        String server_requestID = "";
    }

    public class DownLoaderStartEventArgs {
        int connect_time_ms;
        boolean continue_download = false;
        String definition = "custom";
        boolean encrypted = false;
        long video_timestamp = 0;
    }

    public void sendEvent(DownLoaderStartEventArgs args, PublicPraram publicPraram) {
        EventUtils.sendUrl(publicPraram.getFinalUrl("5001", getArgsStr(args)));
    }

    public void sendStopEvent(long video_timestamp, PublicPraram publicPraram) {
        EventUtils.sendUrl(publicPraram.getFinalUrl("5002", getArgsStr(video_timestamp)));
    }

    public void sendFinishEvent(long video_timestamp, PublicPraram publicPraram) {
        EventUtils.sendUrl(publicPraram.getFinalUrl("5003", getArgsStr(video_timestamp)));
    }

    public void sendRemoveEvent(boolean completed, PublicPraram publicPraram) {
        StringBuilder sb = new StringBuilder();
        sb.append("cv=").append(completed);
        EventUtils.sendUrl(publicPraram.getFinalUrl("5004", sb.toString()));
    }

    public void sendEvent(DownLoaderErrorEventArgs args, PublicPraram publicPraram) {
        EventUtils.sendUrl(publicPraram.getFinalUrl("4001", getArgsStr(args)));
    }

    private static String getArgsStr(DownLoaderErrorEventArgs args) {
        StringBuilder sb = new StringBuilder();
        sb.append("error_code=").append(args.error_code).append("&");
        sb.append("error_msg=").append(args.error_msg).append("&");
        sb.append("sri=").append(args.server_requestID);
        return EventUtils.urlEncode(sb.toString());
    }

    private static String getArgsStr(long video_timestamp) {
        StringBuilder sb = new StringBuilder();
        sb.append("vt=").append(video_timestamp);
        return EventUtils.urlEncode(sb.toString());
    }

    private static String getArgsStr(DownLoaderStartEventArgs args) {
        StringBuilder sb = new StringBuilder();
        sb.append("vt=").append(args.video_timestamp).append("&");
        sb.append("ct=").append(args.connect_time_ms).append("&");
        sb.append("dn=").append(args.definition).append("&");
        sb.append("cd=").append(args.continue_download).append("&");
        sb.append("encrypted=").append(args.encrypted);
        return EventUtils.urlEncode(sb.toString());
    }
}
