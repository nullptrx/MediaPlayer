package com.alivc.player.logreport;

import android.text.TextUtils;

public class ErrorEvent {

    public static class ErrorEventArgs {
        public String cdnError;
        public String cdnVia;
        public String eagleId;
        public int error_code;
        public String error_msg;
        public String servier_requestID = "";
        public long videoTimeStampMs;
    }

    public static void sendEvent(ErrorEventArgs args, PublicPraram publicPraram) {
        EventUtils.sendUrl(publicPraram.getFinalUrl("4001", getArgsStr(args)));
    }

    private static String getArgsStr(ErrorEventArgs args) {
        StringBuilder sb = new StringBuilder();
        sb.append("vt=").append(args.videoTimeStampMs).append("&");
        sb.append("error_code=").append(args.error_code).append("&");
        sb.append("error_msg=").append(args.error_msg).append("&");
        sb.append("sri=").append(args.servier_requestID);
        if (!TextUtils.isEmpty(args.cdnError)) {
            sb.append("&").append("cdnError=").append(args.cdnError);
        }
        if (!TextUtils.isEmpty(args.cdnVia)) {
            sb.append("&").append("cdnVia=").append(args.cdnVia);
        }
        if (!TextUtils.isEmpty(args.eagleId)) {
            sb.append("&").append("eagleID=").append(args.eagleId);
        }
        return EventUtils.urlEncode(sb.toString());
    }
}
