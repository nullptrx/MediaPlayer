package com.alivc.player.logreport;

import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;
import android.os.Build.VERSION;
import android.text.TextUtils;
import android.util.Log;

import com.alivc.player.InformationReport;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.UUID;

public class PublicPraram {
    private static String TAG = "PublicPraram";
    private static String app_version = "";
    private static String application_id = null;
    private static String application_name = null;
    private static String business_id = "";
    private static String connection = "";
    private static String device_model = Build.MODEL;
    private static String hostname = "";
    private static String log_level = "info";
    private static String log_version = "1.0";
    private static String operation_system = "Android";
    private static String os_version = VERSION.RELEASE;
    private static String product = "player";
    private static String referer = "aliyun";
    //    private static String reportHost = "https://videocloud.cn-hangzhou.log.aliyuncs.com/logstores/newplayer/track?APIVersion=0.6.0";
    private static String reportHost = "https://127.0.0.1/logstores/newplayer/track?APIVersion=0.6.0";
    private static String terminal_type = "";
    private static String time = "";
    private static String user_account = "0";
    private static String uuid = "";
    private String args = "";
    private String cdn_ip = "0.0.0.0";
    private String definition = "custom";
    private String event = "";
    public String module = "player";
    private String request_id = "";
    public String submodule = "play";
    public String ui = "";
    private String user_agent = "";
    private String video_domain = "";
    private String video_type = "vod";
    private String video_url = "";

    public enum VideoType {
        live,
        vod
    }

    public static String getAppName(Context context) {
        try {
            return context.getResources().getString(context.getPackageManager().getPackageInfo(context.getPackageName(), 0).applicationInfo.labelRes);
        } catch (NameNotFoundException e) {
            e.printStackTrace();
            return "";
        }
    }

    public PublicPraram(Context context) {
        if (application_id == null) {
            application_id = context.getPackageName();
            application_id += "|Android";
        }
        if (application_name == null) {
            application_name = getAppName(context);
        }
        hostname = EventUtils.getIp();
        connection = EventUtils.getNetWorkType(context);
        if (TextUtils.isEmpty(terminal_type)) {
            terminal_type = getTerminalType(context);
        }
        uuid = EventUtils.getLocalUuid(context);
    }

    public static void setSDkVersion(String version) {
        app_version = version;
    }

    public void setDefinition(String def) {
        this.definition = def;
    }

    public static void setHost(String logURL) {
        reportHost = logURL;
    }

    public static void setUserId(String userId) {
        user_account = userId;
    }

    public void setCdn_ip(String ip) {
        this.cdn_ip = ip;
    }

    private String getTerminalType(Context context) {
        if (EventUtils.isPad(context)) {
            return "pad";
        }
        return "phone";
    }

    public void setVideoType(VideoType videoType) {
        this.video_type = videoType.name();
    }

    public void changeRequestId() {
        this.request_id = UUID.randomUUID().toString();
    }

    public void resetRequestId() {
        this.request_id = null;
    }

    public static void setBusinessId(String businessId) {
        business_id = businessId;
    }

    public void setVideoUrl(String videoUrl) {
        this.video_url = videoUrl;
        URL url = null;
        try {
            url = new URL(this.video_url);
        } catch (MalformedURLException ignored) {
        }
        if (url != null) {
            this.video_domain = url.getHost();
        }
        if (TextUtils.isEmpty(this.video_domain) && this.video_url.startsWith("rtmp://")) {
            int pos = videoUrl.indexOf(47, 7);
            if (pos < 7) {
                pos = this.video_url.length();
            }
            this.video_domain = videoUrl.substring(7, pos);
        }
    }

    private String getParam(String event, String argsStr) {
        StringBuilder finalSb = new StringBuilder();
        time = System.currentTimeMillis() + "";
        finalSb.append("t=").append(EventUtils.urlEncode(time)).append("&");
        finalSb.append("ll=").append(EventUtils.urlEncode(log_level)).append("&");
        finalSb.append("lv=").append(EventUtils.urlEncode(log_version)).append("&");
        finalSb.append("pd=").append(EventUtils.urlEncode(product)).append("&");
        finalSb.append("md=").append(EventUtils.urlEncode(this.module)).append("&");
        finalSb.append("sm=").append(EventUtils.urlEncode(this.submodule)).append("&");
        finalSb.append("hn=").append(EventUtils.urlEncode(hostname)).append("&");
        finalSb.append("bi=").append(EventUtils.urlEncode(business_id)).append("&");
        finalSb.append("ri=").append(EventUtils.urlEncode(this.request_id)).append("&");
        finalSb.append("e=").append(event).append("&");
        finalSb.append("args=").append(argsStr).append("&");
        finalSb.append("vt=").append(EventUtils.urlEncode(this.video_type)).append("&");
        finalSb.append("tt=").append(EventUtils.urlEncode(terminal_type)).append("&");
        finalSb.append("dm=").append(EventUtils.urlEncode(device_model)).append("&");
        finalSb.append("os=").append(EventUtils.urlEncode(operation_system)).append("&");
        finalSb.append("ov=").append(EventUtils.urlEncode(os_version)).append("&");
        finalSb.append("av=").append(EventUtils.urlEncode(app_version)).append("&");
        finalSb.append("uuid=").append(EventUtils.urlEncode(uuid)).append("&");
        finalSb.append("vu=").append(EventUtils.urlEncode(this.video_url)).append("&");
        finalSb.append("vd=").append(EventUtils.urlEncode(this.video_domain)).append("&");
        finalSb.append("ua=").append(EventUtils.urlEncode(user_account)).append("&");
        finalSb.append("dn=").append(EventUtils.urlEncode(this.definition)).append("&");
        finalSb.append("co=").append(EventUtils.urlEncode(connection)).append("&");
        finalSb.append("uat=").append(EventUtils.urlEncode(this.user_agent)).append("&");
        finalSb.append("ui=").append(EventUtils.urlEncode(this.ui)).append("&");
        finalSb.append("app_id=").append(EventUtils.urlEncode(application_id)).append("&");
        finalSb.append("app_n=").append(EventUtils.urlEncode(application_name)).append("&");
        finalSb.append("cdn_ip=").append(EventUtils.urlEncode(this.cdn_ip)).append("&");
        finalSb.append("r=").append(EventUtils.urlEncode(referer));
        if (InformationReport.enable()) {
            Log.d(TAG, finalSb.toString());
        }
        return finalSb.toString();
    }

    public String getFinalUrl(String event, String argsStr) {
        return reportHost + "&" + getParam(event, argsStr);
    }
}
