package com.start;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Handler;
import android.os.Message;

import org.json.JSONException;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParser;

public class userLogin {
    Context context;
    Editor editor;
    loginThread mThread;
    String result;
    private SharedPreferences settings;

    class loginThread extends Thread {
        String androidID = null;
        Context context = null;
        String id = null;
        String imei = null;
        Handler mHandler;
        String mac = null;

        public loginThread(Context context, String id, String imei, String mac, String androidID, Handler handler) {
            this.context = context;
            this.id = id;
            this.imei = imei;
            this.mac = mac;
            this.androidID = androidID;
            this.mHandler = handler;
        }

        public void run() {
            Message msg;
//            userLogin.this.settings = this.context.getSharedPreferences("settings", 0);
//            String serviceUrl = skyconfig.ServerWSDL;
//            SoapObject soapObject = new SoapObject(skyconfig.NAMESPACE, "aiqiyiLogin");
//            soapObject.addProperty("id", (Object) this.id);
//            soapObject.addProperty("imei", (Object) this.imei);
//            soapObject.addProperty("mac", (Object) this.mac);
//            soapObject.addProperty("androidID", (Object) this.androidID);
//            if (!TextUtils.isEmpty(skyconfig.sign).booleanValue()) {
//                soapObject.addProperty("sign", (Object) skyconfig.sign);
//            } else if (TextUtils.isEmpty(skyconfig.value).booleanValue()) {
//                soapObject.addProperty("sign", (Object) "null_sign");
//            } else {
//                soapObject.addProperty("sign", (Object) skyconfig.value);
//            }
//            soapObject.addProperty("Agent", (Object) "v" + String.valueOf(skyconfig.current_version));
//            SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(110);
//            envelope.bodyOut = soapObject;
//            envelope.dotNet = true;
//            envelope.setOutputSoapObject(soapObject);
            try {
//                new AndroidHttpTransport(serviceUrl).call(null, envelope);
//                userLogin.this.result = String.valueOf(envelope.getResponse());
                userLogin.this.editor = userLogin.this.settings.edit();
                try {
                    JSONObject jsonObject = new JSONObject(userLogin.this.result);
                    String message = null;
                    String downloadurl = null;
                    String remainday = null;
                    String version = null;
                    if (jsonObject.has("message")) {
                        message = jsonObject.getString("message");
                    }
                    if (jsonObject.has("downloadurl")) {
                        downloadurl = jsonObject.getString("downloadurl");
                    }
                    if (jsonObject.has("remainday")) {
                        remainday = jsonObject.getString("remainday");
                    }
                    if (jsonObject.has("version")) {
                        version = jsonObject.getString("version");
                    }
                    String admessage = XmlPullParser.NO_NAMESPACE;
                    if (jsonObject.has("admessage")) {
                        admessage = jsonObject.getString("admessage");
                    }
                    if (jsonObject.has("errorCode")) {
                        userLogin.this.editor.putString("sky_errorCode", jsonObject.getString("errorCode"));
                    } else {
                        userLogin.this.editor.putString("sky_errorCode", "0");
                    }
                    if (jsonObject.has("APPId")) {
                        userLogin.this.editor.putString("APPId", jsonObject.getString("APPId"));
                        skyconfig.APPId = jsonObject.getString("APPId");
                    }
                    if (jsonObject.has("BannerPosId")) {
                        userLogin.this.editor.putString("BannerPosId", jsonObject.getString("BannerPosId"));
                        skyconfig.BannerPosId = jsonObject.getString("BannerPosId");
                    }
                    if (jsonObject.has("FullId")) {
                        userLogin.this.editor.putString("FullId", jsonObject.getString("FullId"));
                        skyconfig.FullId = jsonObject.getString("FullId");
                    }
                    if (jsonObject.has("adselect")) {
                        userLogin.this.editor.putInt("adselect", Integer.parseInt(jsonObject.getString("adselect")));
                    }
                    if (jsonObject.has("admode")) {
                        int isBaiduWithAdsage = Integer.parseInt(jsonObject.getString("admode"));
                        userLogin.this.editor.putInt("admode", isBaiduWithAdsage);
                        skyconfig.admode = isBaiduWithAdsage;
                    }
                    if (jsonObject.has("baiduID")) {
                        userLogin.this.editor.putString("baiduID", jsonObject.getString("baiduID"));
                        skyconfig.baiduID = jsonObject.getString("baiduID");
                    }
                    if (jsonObject.has("baiduSplash")) {
                        userLogin.this.editor.putString("baiduSplash", jsonObject.getString("baiduSplash"));
                        skyconfig.baiduSplash = jsonObject.getString("baiduSplash");
                    }
                    if (jsonObject.has("baiduBannerID")) {
                        userLogin.this.editor.putString("baiduBannerID", jsonObject.getString("baiduBannerID"));
                        skyconfig.baiduBannerID = jsonObject.getString("baiduBannerID");
                    }
                    if (jsonObject.has("packagename")) {
                        userLogin.this.editor.putString("packagename", jsonObject.getString("packagename"));
                        skyconfig.packageName = jsonObject.getString("packagename");
                    }
                    userLogin.this.editor.putString("sky_message", message);
                    userLogin.this.editor.putString("sky_downloadurl", downloadurl);
                    userLogin.this.editor.putString("sky_remainday", remainday);
                    userLogin.this.editor.putString("sky_version", version);
                    userLogin.this.editor.putBoolean("sky_isinit", true);
                    if (jsonObject.has("noBaiduPercent")) {
                        skyconfig.noBaiduPercent = jsonObject.getInt("noBaiduPercent");
                        userLogin.this.editor.putInt("noBaiduPercent", jsonObject.getInt("noBaiduPercent"));
                    }
                    if (jsonObject.has("noGdtPercent")) {
                        skyconfig.noGdtPercent = jsonObject.getInt("noGdtPercent");
                        userLogin.this.editor.putInt("noGdtPercent", jsonObject.getInt("noGdtPercent"));
                    }
                    if (jsonObject.has("user_id")) {
                        skyconfig.user_id = jsonObject.getInt("user_id");
                        userLogin.this.editor.putInt("user_id", jsonObject.getInt("user_id"));
                    }
                    if (jsonObject.has("recommendInfo")) {
                        skyconfig.recommendInfo = jsonObject.getString("recommendInfo");
                        userLogin.this.editor.putString("recommendInfo", jsonObject.getString("recommendInfo"));
                    }
                    if (jsonObject.has("isRecommend")) {
                        skyconfig.isRecommend = Boolean.valueOf(jsonObject.getBoolean("isRecommend"));
                        userLogin.this.editor.putBoolean("isRecommend", jsonObject.getBoolean("isRecommend"));
                    }
                    if (jsonObject.has("InterstitialAD")) {
                        skyconfig.InterstitialAD = jsonObject.getString("InterstitialAD");
                        userLogin.this.editor.putString("InterstitialAD", jsonObject.getString("InterstitialAD"));
                    }
                    if (jsonObject.has("baiduInterID")) {
                        skyconfig.baiduInterID = jsonObject.getString("baiduInterID");
                        userLogin.this.editor.putString("baiduInterID", jsonObject.getString("baiduInterID"));
                    }
                    if (jsonObject.has("isShowGdtInser")) {
                        skyconfig.isShowGdtInser = Boolean.valueOf(jsonObject.getBoolean("isShowGdtInser"));
                        userLogin.this.editor.putBoolean("isShowGdtInser", jsonObject.getBoolean("isShowGdtInser"));
                    }
                    if (jsonObject.has("shareInfo")) {
                        skyconfig.shareInfo = jsonObject.getString("shareInfo");
                        userLogin.this.editor.putString("shareInfo", jsonObject.getString("shareInfo"));
                    }
                    if (jsonObject.has("isEnableInser")) {
                        skyconfig.isEnableInser = Boolean.valueOf(jsonObject.getBoolean("isEnableInser"));
                        userLogin.this.editor.putBoolean("isEnableInser", jsonObject.getBoolean("isEnableInser"));
                    }
                    if (jsonObject.has("serverDomain")) {
                        skyconfig.serverDomain = jsonObject.getString("serverDomain");
                        userLogin.this.editor.putString("serverDomain", skyconfig.serverDomain);
                    }
                    if (jsonObject.has("sign")) {
                        skyconfig.qysign = jsonObject.getString("sign");
                        userLogin.this.editor.putString("sign", skyconfig.qysign);
                        Editor signeditor = this.context.getApplicationContext().getSharedPreferences("settings", 0).edit();
                        signeditor.putString("qysign", skyconfig.qysign);
                        signeditor.commit();
                    }
                    if (jsonObject.has("vipConfirmMsg")) {
                        skyconfig.vipConfirmMsg = jsonObject.getString("vipConfirmMsg");
                        userLogin.this.editor.putString("vipConfirmMsg", skyconfig.vipConfirmMsg);
                    }
                    if (jsonObject.has("xiaomiAppId")) {
                        skyconfig.xiaomiAPPID = jsonObject.getString("xiaomiAppId");
                        userLogin.this.editor.putString("xiaomiAPPID", skyconfig.xiaomiAPPID);
                    }
                    if (jsonObject.has("xiaomiBannerId")) {
                        skyconfig.xiaomiBannerID = jsonObject.getString("xiaomiBannerId");
                        userLogin.this.editor.putString("xiaomiBannerID", skyconfig.xiaomiBannerID);
                    }
                    if (jsonObject.has("noXiaomiPercent")) {
                        skyconfig.noXiaomiPercent = jsonObject.getInt("noXiaomiPercent");
                        userLogin.this.editor.putInt("noXiaomiPercent", skyconfig.noXiaomiPercent);
                    }
                    if (jsonObject.has("cookie")) {
                        skyconfig.value = jsonObject.getString("cookie");
                        userLogin.this.editor.putString("cookie", skyconfig.value);
                    }
                    if (jsonObject.has("isNeedVipConfirm")) {
                        skyconfig.isNeedVipConfirm = Boolean.valueOf(jsonObject.getBoolean("isNeedVipConfirm"));
                        userLogin.this.editor.putBoolean("isNeedVipConfirm", jsonObject.getBoolean("isNeedVipConfirm"));
                    }
                    skyconfig.isLoginSuccess = Boolean.valueOf(true);
                    if (!userLogin.this.settings.getBoolean("sky_isinit", false)) {
                        msg = new Message();
                        msg.what = 5;
                        this.mHandler.sendMessage(msg);
                    }
                    userLogin.this.editor.putString("sky_admessage", admessage);
                    userLogin.this.editor.commit();
                    msg = new Message();
                    msg.what = 8;
                    this.mHandler.sendMessage(msg);
                } catch (JSONException e) {
                    e.printStackTrace();
                    userLogin.this.editor.putString("sky_errorCode", "1");
                    userLogin.this.editor.commit();
                    msg = new Message();
                    msg.what = 6;
                    try {
                        Thread.sleep(4000);
                    } catch (InterruptedException e1) {
                        e1.printStackTrace();
                    }
                    this.mHandler.sendMessage(msg);
                }
            } catch (Exception e2) {
//                msg = new Message();
//                msg.what = 6;
//                try {
//                    Thread.sleep(4000);
//                } catch (InterruptedException e12) {
//                    e12.printStackTrace();
//                }
//                this.mHandler.sendMessage(msg);
//            } catch (XmlPullParserException e) {
//                e.printStackTrace();
            }
            super.run();
        }
    }

    public userLogin(Context context, String id, String imei, String mac, String androidID, Handler handler) {
        this.context = context;
        this.mThread = new loginThread(context, id, imei, mac, androidID, handler);
    }

    public void startLogin() {
        this.mThread.start();
    }
}
