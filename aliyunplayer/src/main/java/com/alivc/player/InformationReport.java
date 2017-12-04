package com.alivc.player;

import android.content.Context;
import android.os.Build;
import android.provider.Settings.Secure;
import android.telephony.TelephonyManager;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.UUID;

public class InformationReport {
    public static final String TAG = "UserTraceReport";
    private static String mBusinessId;
    private static String mCdnIp;
    private static String mDeviceModel;
    private static boolean mEnable;
//    private static String mHost = "http://videocloud.cn-hangzhou.log.aliyuncs.com/logstores/player/track?APIVersion=0.6.0&";
    private static String mHost = "http://127.0.0.1/logstores/player/track?APIVersion=0.6.0&";
    private static long mLastBufferVideoTime = -1;
    private static long mLastPauseVideoTime = -1;
    private static long mLastSeekVideoTime = -1;
    private static String mLogVersion;
    private static String mPlayerVersion;
    private static String mResDefinition;
    private static String mSessionId;
    private static String mTermialType;
    private static String mTimeStamp;
    private static String mUserId;
    private static String mUuid;
    private static String mVideoId;

    public static void init(Context context) {
        mLogVersion = "1";
        mTermialType = "phone";
        mDeviceModel = Build.MODEL;
        mPlayerVersion = "2.0.0";
        mUuid = createUuid(context);
        mEnable = false;
    }

    public static void enalbeReport() {
        mEnable = true;
    }

    public static void disableReport() {
        mEnable = false;
    }

    public static String getUserId() {
        return mUserId;
    }

    public static void setUserId(String mUserId) {
        mUserId = mUserId;
    }

    public static void createSessionId() {
        mSessionId = UUID.randomUUID().toString();
    }

    public static void resetSessionId() {
        mSessionId = null;
    }

    public static void setBusinessId(String businessId) {
        mBusinessId = businessId;
    }

    public static void setVideoId(String videoId) {
        mVideoId = videoId;
    }

    public static void setHost(String host) {
        if (host.endsWith("&")) {
            mHost = host;
        } else {
            mHost = host + "&";
        }
    }

    private static String createUuid(Context context) {
        TelephonyManager tm = (TelephonyManager) context.getApplicationContext().getSystemService("phone");
        return new UUID((long) ("" + Secure.getString(context.getContentResolver(), "android_id")).hashCode(), (((long) ("" + tm.getDeviceId()).hashCode()) << 32) | ((long) ("" + tm.getSimSerialNumber()).hashCode())).toString();
    }

    private static String urlEncode(String url) {
        if (url != null) {
            try {
                return URLEncoder.encode(url, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        return "";
    }

    private static String constructPublicParam(boolean needSession) {
        String seperator = "&";
        StringBuffer sb = new StringBuffer();
        sb.append("lv=").append(urlEncode(mLogVersion)).append(seperator);
        sb.append("b=").append(urlEncode(mBusinessId)).append(seperator);
        sb.append("t=").append(urlEncode(mTermialType)).append(seperator);
        sb.append("m=").append(urlEncode(mDeviceModel)).append(seperator);
        sb.append("pv=").append(urlEncode(mPlayerVersion)).append(seperator);
        sb.append("uuid=").append(urlEncode(mUuid)).append(seperator);
        if (mVideoId != null) {
            sb.append("v=").append(urlEncode(mVideoId)).append(seperator);
        }
        if (mUserId != null) {
            sb.append("u=").append(urlEncode(mUserId)).append(seperator);
        }
        if (needSession && mSessionId != null) {
            sb.append("s=").append(urlEncode(mSessionId)).append(seperator);
        }
        if (mResDefinition != null) {
            sb.append("d=").append(urlEncode(mResDefinition)).append(seperator);
        }
        if (mCdnIp != null) {
            sb.append("cdn_ip=").append(urlEncode(mCdnIp)).append(seperator);
        }
        sb.append("ct=").append(urlEncode(mTimeStamp)).append(seperator);
        return sb.toString();
    }

    private static void sendError(String eventId, int erroCode, String errDescription) {
        String seperator = "&";
        StringBuffer sb = new StringBuffer();
        mTimeStamp = "" + System.currentTimeMillis();
        sb.append(constructPublicParam(false));
        sb.append("e=" + urlEncode(eventId));
        sb.append(seperator).append("error_code=").append(urlEncode("" + erroCode));
        sb.append(seperator).append("error_msg=").append(urlEncode(errDescription));
        sendUrl(mHost + sb.toString());
    }

    private static void sendInfo(String eventId, int videoTime, int costTime, int dragFromeTime, int dragToTime, int interval, boolean needSession) {
        String seperator = "&";
        StringBuffer sb = new StringBuffer();
        mTimeStamp = "" + System.currentTimeMillis();
        sb.append(constructPublicParam(needSession));
        sb.append("e=" + urlEncode(eventId));
        if (videoTime >= 0) {
            sb.append(seperator).append("vt=").append(urlEncode("" + videoTime));
        }
        if (costTime >= 0) {
            sb.append(seperator).append("cost=").append(urlEncode("" + costTime));
        }
        if (dragFromeTime >= 0) {
            sb.append(seperator).append("drag_from_timestamp=").append(urlEncode("" + dragFromeTime));
        }
        if (dragToTime >= 0) {
            sb.append(seperator).append("drag_to_timestamp=").append(urlEncode("" + dragToTime));
        }
        if (interval >= 0) {
            sb.append(seperator).append("interval=").append(urlEncode("" + interval));
        }
        sendUrl(mHost + sb.toString());
    }

    private static void sendInfo(String eventId, int videoTime, boolean needSession) {
        sendInfo(eventId, videoTime, -1, -1, -1, -1, needSession);
    }

    public static void sendPlayStartInfo(int videoTime) {
        if (mEnable) {
            sendInfo("2001", videoTime, -1, -1, -1, -1, true);
        }
    }

    public static void sendStopInfo(int videotime) {
        if (mEnable) {
            sendInfo("2002", videotime, -1, -1, -1, -1, true);
            resetSessionId();
        }
    }

    public static void sendPauseInfo(int videotime) {
        if (mEnable) {
            mLastPauseVideoTime = System.currentTimeMillis();
            sendInfo("2003", videotime, -1, -1, -1, -1, true);
        }
    }

    public static void sendPauseToPlayInfo(int videoTime) {
        if (mEnable) {
            long ctime = System.currentTimeMillis() - mLastPauseVideoTime;
            mLastPauseVideoTime = -1;
            sendInfo("2010", videoTime, (int) ctime, -1, -1, -1, true);
        }
    }

    public static void sendSeekStartInfo(int videoTime, int seekTime) {
        if (mEnable) {
            mLastSeekVideoTime = System.currentTimeMillis();
            sendInfo("2004", -1, -1, videoTime, seekTime, -1, true);
        }
    }

    public static void sendSeekEndInfo(int videoTime) {
        if (mEnable) {
            long ctime = System.currentTimeMillis() - mLastSeekVideoTime;
            mLastSeekVideoTime = -1;
            sendInfo("2011", videoTime, (int) ctime, -1, -1, -1, true);
        }
    }

    public static void sendBufferingStartInfo(int videoTime) {
        if (mEnable) {
            mLastBufferVideoTime = System.currentTimeMillis();
            sendInfo("3002", videoTime, -1, -1, -1, -1, true);
        }
    }

    public static void sendBufferingEndInfo(int videoTime) {
        if (mEnable) {
            long ctime = System.currentTimeMillis() - mLastBufferVideoTime;
            mLastBufferVideoTime = -1;
            sendInfo("3001", videoTime, (int) ctime, -1, -1, -1, true);
        }
    }

    public static void sendPlayErrorInfo(int videoTime, int errCode, String errDescription) {
        if (mEnable) {
            sendError("4001", errCode, errDescription);
        }
    }

    public static void sendHeartBeat(int videoTime, int timeInterval) {
        if (mEnable) {
            sendInfo("9001", videoTime, -1, -1, -1, timeInterval, true);
        }
    }

    private static void sendUrl(final String url) {
        VcPlayerLog.d("UserTraceReport", "usertrace : url = " + url);
        new Thread(new Runnable() {
            public void run() {
                try {
                    HttpClientUtil.doHttpsGet(url);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}
