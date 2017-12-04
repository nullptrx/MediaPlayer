package com.alivc.player.logreport;

import android.text.TextUtils;

public class SeekEvent {
    public static long mLastSeekVideoTime = 0;

    public static class SeekEventArgs {
        public String cndVia;
        public String eagleId;
        public long fromTimeStampMs;
        public long toTimeStampMs;
    }

    public static void sendEvent(SeekEventArgs args, PublicPraram publicPraram) {
        mLastSeekVideoTime = System.currentTimeMillis();
        EventUtils.sendUrl(publicPraram.getFinalUrl("2004", getArgsStr(args)));
    }

    private static String getArgsStr(SeekEventArgs args) {
        StringBuilder sb = new StringBuilder();
        sb.append("drag_from_timestamp=").append(args.fromTimeStampMs).append("&");
        sb.append("drag_to_timestamp=").append(args.toTimeStampMs);
        if (!TextUtils.isEmpty(args.cndVia)) {
            sb.append("&").append("cdnVia=").append(args.cndVia);
        }
        if (!TextUtils.isEmpty(args.eagleId)) {
            sb.append("&").append("eagleID=").append(args.eagleId);
        }
        return EventUtils.urlEncode(sb.toString());
    }
}
