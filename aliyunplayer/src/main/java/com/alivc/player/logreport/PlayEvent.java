package com.alivc.player.logreport;

import android.text.TextUtils;

public class PlayEvent {

    public enum DefinitionPlayMode {
        auto,
        fixed
    }

    public static class PlayEventArgs {
        public String cdnVia;
        public long connectTimeMs = 0;
        public long donwloadTimeMs = 0;
        public String eagleId;
        public String encrypted;
        public long ffprobeTimeMs = 0;
        public DefinitionPlayMode mode = DefinitionPlayMode.fixed;
        public String openTime;
        public int videoHeight = 0;
        public long videoTimeStempMs = 0;
        public int videoWidth = 0;
    }

    public static void sendEvent(PlayEventArgs args, PublicPraram publicPraram) {
        EventUtils.sendUrl(publicPraram.getFinalUrl("2001", getArgsStr(args)));
    }

    private static String getArgsStr(PlayEventArgs args) {
        StringBuilder sb = new StringBuilder();
        sb.append("dsm=").append(args.mode == DefinitionPlayMode.auto ? "a" : "f").append("&");
        sb.append("vt=").append(args.videoTimeStempMs).append("&");
        sb.append("connect_time=").append(args.connectTimeMs).append("&");
        sb.append("ffprobe_time=").append(args.ffprobeTimeMs).append("&");
        sb.append("download_time=").append(args.donwloadTimeMs).append("&");
        sb.append("encrypted=").append(args.encrypted).append("&");
        sb.append("specified_definition=").append(args.videoWidth).append("*").append(args.videoHeight);
        if (!TextUtils.isEmpty(args.openTime)) {
            sb.append("&").append(args.openTime);
        }
        if (!TextUtils.isEmpty(args.eagleId)) {
            sb.append("&").append("eagleID=").append(args.eagleId);
        }
        if (!TextUtils.isEmpty(args.cdnVia)) {
            sb.append("&").append("cdnVia=").append(args.cdnVia);
        }
        return EventUtils.urlEncode(sb.toString());
    }
}
