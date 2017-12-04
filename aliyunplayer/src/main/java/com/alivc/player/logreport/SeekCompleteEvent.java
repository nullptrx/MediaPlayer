package com.alivc.player.logreport;

import android.text.TextUtils;

public class SeekCompleteEvent {

    public static class SeekCompleteEventArgs {
        public String cndVia;
        public long costMs;
        public String eagleId;
        public long videoTimeStampMs;
    }

    public static void sendEvent(SeekCompleteEventArgs args, PublicPraram publicPraram) {
        EventUtils.sendUrl(publicPraram.getFinalUrl("2011", getArgsStr(args)));
    }

    private static String getArgsStr(SeekCompleteEventArgs args) {
        StringBuilder sb = new StringBuilder();
        sb.append("vt=").append(args.videoTimeStampMs).append("&");
        sb.append("cost=").append(args.costMs);
        if (!TextUtils.isEmpty(args.cndVia)) {
            sb.append("&").append("cdnVia=").append(args.cndVia);
        }
        if (!TextUtils.isEmpty(args.eagleId)) {
            sb.append("&").append("eagleID=").append(args.eagleId);
        }
        return EventUtils.urlEncode(sb.toString());
    }
}
