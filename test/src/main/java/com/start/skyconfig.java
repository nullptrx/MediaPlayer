package com.start;

import android.app.Application;
import android.content.SharedPreferences;
import android.util.Log;
import org.xmlpull.v1.XmlPullParser;

public class skyconfig {
    public static String APPId = XmlPullParser.NO_NAMESPACE;
    public static String BannerPosId = XmlPullParser.NO_NAMESPACE;
    public static String FullId = XmlPullParser.NO_NAMESPACE;
    public static String InterstitialAD = XmlPullParser.NO_NAMESPACE;
    public static String NAMESPACE = "http://userService";
    public static String ServerWSDL = "http://goodsmanagement.duapp.com/services/aiqiyi?wsdl";
    public static int admode = 1;
    public static int adselect = 0;
    public static String baiduBannerID = XmlPullParser.NO_NAMESPACE;
    public static String baiduID = XmlPullParser.NO_NAMESPACE;
    public static String baiduInterID = XmlPullParser.NO_NAMESPACE;
    public static String baiduSplash = XmlPullParser.NO_NAMESPACE;
    public static int current_version = 29;
    public static int flag = 0;
    public static Boolean isEnableInser = Boolean.valueOf(false);
    public static Boolean isInit = Boolean.valueOf(false);
    public static Boolean isLoadXiaomiSuccess = Boolean.valueOf(false);
    public static Boolean isLoginSuccess = Boolean.valueOf(false);
    public static Boolean isNeedVipConfirm = Boolean.valueOf(true);
    public static Boolean isRecommend = Boolean.valueOf(true);
    public static Boolean isShowGdtInser = Boolean.valueOf(true);
    public static int noBaiduPercent = 20;
    public static int noGdtPercent = 30;
    public static int noXiaomiPercent = 80;
    public static Boolean noadmode = Boolean.valueOf(false);
    public static String packageName = XmlPullParser.NO_NAMESPACE;
    public static String qysign = XmlPullParser.NO_NAMESPACE;
    public static String recommendInfo = XmlPullParser.NO_NAMESPACE;
    public static String serverDomain = XmlPullParser.NO_NAMESPACE;
    public static SharedPreferences settings;
    public static String shareInfo = XmlPullParser.NO_NAMESPACE;
    public static String sign = XmlPullParser.NO_NAMESPACE;
    public static String userID = XmlPullParser.NO_NAMESPACE;
    public static int user_id = 0;
    public static String value = XmlPullParser.NO_NAMESPACE;
    public static String vipConfirmMsg = XmlPullParser.NO_NAMESPACE;
    public static String xiaomiAPPID = XmlPullParser.NO_NAMESPACE;
    public static String xiaomiBannerID = XmlPullParser.NO_NAMESPACE;

    public static void initMode(Application application) {
        SharedPreferences settings = application.getSharedPreferences("settings", 0);
        int restartFor = settings.getInt("restartFor", 0);
        serverDomain = settings.getString("serverDomain", XmlPullParser.NO_NAMESPACE);
        PmsHookBinderInvocationHandler.SIGN = settings.getString("sign", XmlPullParser.NO_NAMESPACE);
        if (restartFor == 2) {
            noadmode = Boolean.valueOf(false);
        } else {
            noadmode = Boolean.valueOf(true);
        }
    }

    public static String getPlayerNatieName() {
        return !noadmode.booleanValue() ? "libplayer1.so" : "libplayer0.so";
    }

    public static String getDomainName() {
        return !noadmode.booleanValue() ? serverDomain : XmlPullParser.NO_NAMESPACE;
    }

    public static void LogInt(int i) {
        Log.d("skylog", "logInt:" + i);
    }
}
