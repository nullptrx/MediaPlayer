package com.start;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Process;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.qiyi.video.WelcomeActivity;
import com.qq.e.comm.util.Md5Util;

import org.xmlpull.v1.XmlPullParser;

import java.util.Date;

public class StartActivity extends Activity {
    Boolean firstuse = Boolean.valueOf(false);
    private Boolean isTrue = Boolean.valueOf(true);
    ProgressDialog mDialog = null;
    resultHandler mHandler;
    TextView messageTextView;
    long preLoginTime = 0;
    private SharedPreferences settings;
    EditText shareIdEditText = null;
    LinearLayout shareLayout = null;
    String userAndroidID;
    String userMac;
    String userimei;

    class resultHandler extends Handler {
        resultHandler() {
        }

        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (!(StartActivity.this.mDialog == null || !StartActivity.this.mDialog.isShowing() || msg.what == 1 || msg.what == 2 || msg.what == 3 || msg.what == 5 || msg.what == 6)) {
                StartActivity.this.mDialog.dismiss();
            }
//            Handler adhandler = new Handler() {
//                public void handleMessage(Message msg) {
//                    super.handleMessage(msg);
//                    if (StartActivity.this.mDialog.isShowing()) {
//                        StartActivity.this.mDialog.dismiss();
//                    }
//                    switch (msg.what) {
//                        case 0:
//                            Toast.makeText(StartActivity.this, "这次没有获取到时间，稍后再试", Toast.LENGTH_LONG).show();
//                            return;
//                        case 1:
//                            new userLogin(StartActivity.this, skyconfig.userID, StartActivity.this.userimei, StartActivity.this.userMac, StartActivity.this.userAndroidID, StartActivity.this.mHandler).startLogin();
//                            Toast.makeText(StartActivity.this, "恭喜您获取到VIP版爱奇艺使用时间", Toast.LENGTH_LONG).show();
//                            return;
//                        default:
//                            Toast.makeText(StartActivity.this, "网络异常", Toast.LENGTH_LONG).show();
//                            return;
//                    }
//                }
//            };
            switch (msg.what) {
                case 1:
                    return;
                case 2:
                    return;
                case 3:
                    return;
                case 5:
                    Date mDate = new Date();
                    Editor editor = StartActivity.this.settings.edit();
                    editor.putLong("sky_install_time", mDate.getTime());
                    editor.putBoolean("sky_isinit", true);
                    editor.commit();
                    if (Integer.parseInt(StartActivity.this.settings.getString("sky_remainday", XmlPullParser.NO_NAMESPACE)) <= 0 || Integer.parseInt(StartActivity.this.settings.getString("sky_version", "0")) > skyconfig.current_version) {
                        if (StartActivity.this.mDialog != null && StartActivity.this.mDialog.isShowing()) {
                            StartActivity.this.mDialog.dismiss();
                        }
                        Toast.makeText(StartActivity.this, "初始化完成", Toast.LENGTH_LONG).show();
                        StartActivity.this.onCreate(new Bundle());
                        return;
                    }
                    Toast.makeText(StartActivity.this, "初始化完成,马上进入爱奇艺VIP版", Toast.LENGTH_LONG).show();
                    Editor editor1 = StartActivity.this.getApplication().getSharedPreferences("settings", 0).edit();
                    editor1.putInt("restartFor", 2);
                    editor1.putString("serverDomain", skyconfig.serverDomain);
                    editor1.commit();
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    if (StartActivity.this.mDialog != null && StartActivity.this.mDialog.isShowing()) {
                        StartActivity.this.mDialog.dismiss();
                    }
                    new Builder(StartActivity.this).setTitle("初始化完成").setMessage("进入爱奇艺需要重新打开爱奇艺VIP版，如果没有自动打开，需要您手动点击APP重新打开，记住了吗？").setCancelable(false).setPositiveButton("确认", new OnClickListener() {
                        public void onClick(DialogInterface dialogInterface, int i) {
                            StartActivity.this.finish();
                            StartActivity.this.restartAPP();
                        }
                    }).create().show();
                    return;
                case 6:
                    Toast.makeText(StartActivity.this, "网络错误，初始化失败，请稍后再试", Toast.LENGTH_LONG).show();
                    return;
                case 8:
                    TextView textView;
                    StringBuilder append;
                    String string;
                    String str2;
                    String sss;
                    String[] strarray;
                    String serverState = StartActivity.this.settings.getString("sky_errorCode", XmlPullParser.NO_NAMESPACE);
                    if (!serverState.equals("0")) {
                        if (!serverState.equals(XmlPullParser.NO_NAMESPACE)) {
                            serverState = "<服务器异常,请重启软件>";
                            textView = StartActivity.this.messageTextView;
                            append = new StringBuilder().append("VIP版剩余使用天数：").append(StartActivity.this.settings.getString("sky_remainday", XmlPullParser.NO_NAMESPACE)).append("天").append(serverState).append("\n");
                            string = StartActivity.this.settings.getString("sky_message", XmlPullParser.NO_NAMESPACE);
                            str2 = string;
                            Log.i("AD_DEMO", str2);
                            sss = SeeGlobal.SeeDownLoadURL;
                            strarray = str2.split("[http]");
                            if (strarray.length >= 2) {
                                string = strarray[0] + sss;
                            }
                            textView.setText(append.append(string).toString());
                            if (Integer.parseInt(StartActivity.this.settings.getString("sky_remainday", "0")) > 0) {
                                StartActivity.this.isTrue = Boolean.valueOf(false);
                            } else {
                                StartActivity.this.isTrue = Boolean.valueOf(true);
                            }
                            return;
                        }
                    }
                    serverState = "0";
                    textView = StartActivity.this.messageTextView;
                    append = new StringBuilder().append("VIP版剩余使用天数：").append(StartActivity.this.settings.getString("sky_remainday", "0")).append("天").append(serverState).append("\n");
                    string = StartActivity.this.settings.getString("sky_message", "0");
                    str2 = string;
                    Log.i("AD_DEMO", str2);
                    sss = SeeGlobal.SeeDownLoadURL;
                    strarray = str2.split("[http]");
                    if (strarray.length >= 2) {
                        string = strarray[0] + sss;
                    }
                    textView.setText(append.append(string).toString());
                    if (Integer.parseInt(StartActivity.this.settings.getString("sky_remainday", "0")) > 0) {
                        StartActivity.this.isTrue = Boolean.valueOf(true);
                    } else {
                        StartActivity.this.isTrue = Boolean.valueOf(false);
                    }
                    return;
                case 9:
                    if (msg.obj != null) {
                        new userLogin(StartActivity.this, skyconfig.userID, skyconfig.userID, StartActivity.this.userMac, StartActivity.this.userAndroidID, StartActivity.this.mHandler).startLogin();
                        return;
                    }
                    return;
                case 10:
                    if (skyconfig.adselect == 0) {
                        return;
                    }
                    return;
                default:
                    return;
            }
        }
    }

    @SuppressLint("WrongConstant")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
       // else if (Integer.parseInt(this.settings.getString("sky_version", "0")) > skyconfig.current_version) {
//            new Builder(this).setTitle("爱奇艺VIP版更新提醒").setMessage("爱奇艺VIP版发现新版本啦，更新享受更好的VIP服务，旧版本已经无法正常使用，请下载更新后继续使用。<非爱奇艺官方更新>\n最新版为->爱奇艺VIP版v" + this.settings.getString("sky_version", "0")).setCancelable(false).setPositiveButton("下载更新", new OnClickListener() {
//                public void onClick(DialogInterface arg0, int arg1) {
//                    Intent intent = new Intent();
//                    intent.setAction("android.intent.action.VIEW");
//                    StartActivity.this.settings.getString("sky_downloadurl", XmlPullParser.NO_NAMESPACE);
//                    intent.setData(Uri.parse(SeeGlobal.SeeDownLoadURL));
//                    StartActivity.this.startActivity(intent);
//                }
//            }).setNegativeButton("退出", new OnClickListener() {
//                public void onClick(DialogInterface arg0, int arg1) {
//                    StartActivity.this.finish();
//                }
//            }).show();
//        }

        RelativeLayout contentView = new RelativeLayout(this);
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(-1, -1);
        layoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
        LinearLayout first = new LinearLayout(this);
        first.setOrientation(LinearLayout.VERTICAL);
        TextView view = new TextView(this);
        view.setText("爱奇艺VIP版v" + skyconfig.current_version);
        first.addView(view);
//        if (!skyconfig.isRecommend.booleanValue()) {
//            this.shareLayout = new LinearLayout(this);
//            this.shareLayout.setOrientation(0);
//            this.shareLayout.setGravity(7);
//            this.shareIdEditText = new EditText(this);
//            this.shareIdEditText.setInputType(2);
//            this.shareIdEditText.setFilters(new InputFilter[]{new LengthFilter(7)});
//            this.shareIdEditText.setHint("请输入推荐人的ID");
//            view = new Button(this);
//            view.setText("他推荐的我");
//            view.setOnClickListener(new View.OnClickListener() {
//                public void onClick(View arg0) {
//                    String rcmdTx = StartActivity.this.shareIdEditText.getText().toString();
//                    if (rcmdTx.length() == 7) {
//                        int recommentID = Integer.parseInt(rcmdTx);
//                        if (recommentID > 999999 && recommentID < 10000000 && recommentID != skyconfig.user_id) {
//                            new RecommendThread(new resultHandler(), skyconfig.user_id, recommentID, skyconfig.userID, skyconfig.sign).start();
//                        } else if (recommentID == skyconfig.user_id) {
//                            Toast.makeText(StartActivity.this, "不能推荐自己", 0).show();
//                        } else {
//                            Toast.makeText(StartActivity.this, "请输入有效的ID", 0).show();
//                        }
//                    }
//                }
//            });
//            this.shareLayout.addView(this.shareIdEditText);
//            this.shareLayout.addView(first);
//            first.addView(this.shareLayout);
//        }
        view = new Button(this);
        view.setText("分享软件获取使用时间");
        view.setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0) {
                Intent intent = new Intent("android.intent.action.SEND");
                intent.setType("text/plain");
                intent.putExtra("android.intent.extra.SUBJECT", "分享获取使用时间");
                String str = "android.intent.extra.TEXT";
                String str2 = skyconfig.shareInfo;
                String str22 = str2;
                Log.i("AD_DEMO", str22);
                String sss = SeeGlobal.SeeDownLoadURL;
                String[] strarray = str22.split("[http]");
                if (strarray.length >= 2) {
                    str2 = strarray[0] + sss;
                }
                intent.putExtra(str, str2);
                StartActivity.this.startActivity(Intent.createChooser(intent, "分享到"));
            }
        });
        first.addView(view);
        LinearLayout enterLayout = new LinearLayout(this);
        enterLayout.setOrientation(LinearLayout.HORIZONTAL);
        enterLayout.setGravity(1);
        Button btn1 = new Button(this);
        Button btn2 = new Button(this);
        btn1.setText("进入VIP版爱奇艺");
        btn2.setText("进入无广告爱奇艺");
        btn1.setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0) {
                if (StartActivity.this.isTrue.booleanValue()) {
                    if (StartActivity.this.mDialog != null && StartActivity.this.mDialog.isShowing()) {
                        StartActivity.this.mDialog.dismiss();
                    }
                    if (skyconfig.isNeedVipConfirm.booleanValue()) {
                        new Builder(StartActivity.this).setTitle("爱奇艺VIP版进入确认").setMessage(skyconfig.vipConfirmMsg).setCancelable(true).setPositiveButton("进入VIP版", new OnClickListener() {
                            public void onClick(DialogInterface arg0, int arg1) {
                                if (skyconfig.isLoginSuccess.booleanValue()) {
                                    Editor editor = StartActivity.this.getApplication().getSharedPreferences("settings", 0).edit();
                                    editor.putInt("restartFor", 2);
                                    editor.putString("serverDomain", skyconfig.serverDomain);
                                    editor.commit();
                                    new Intent().setClass(StartActivity.this, WelcomeActivity.class);
                                    skyconfig.noadmode = Boolean.valueOf(false);
                                    StartActivity.this.finish();
                                    StartActivity.this.restartAPP();
                                    return;
                                }
                                Date timeNow = new Date();
                                long seconds = (timeNow.getTime() - StartActivity.this.preLoginTime) / 1000;
                                StartActivity.this.preLoginTime = timeNow.getTime();
                                if (seconds > 3) {
                                    new userLogin(StartActivity.this, skyconfig.userID, StartActivity.this.userimei, StartActivity.this.userMac, StartActivity.this.userAndroidID, StartActivity.this.mHandler).startLogin();
                                }
                                Toast.makeText(StartActivity.this, "爱奇艺VIP版初始化还未完成，请稍等或完全退出后重新进入", 1).show();
                            }
                        }).setNegativeButton("取消", new OnClickListener() {
                            public void onClick(DialogInterface arg0, int arg1) {
                            }
                        }).show();
                        return;
                    }
                    Editor editor = StartActivity.this.getApplication().getSharedPreferences("settings", 0).edit();
                    editor.putInt("restartFor", 2);
                    editor.putString("serverDomain", skyconfig.serverDomain);
                    editor.commit();
                    skyconfig.noadmode = Boolean.valueOf(false);
                    Intent intent = new Intent(StartActivity.this, WelcomeActivity.class);
                    startActivity(intent);
                    return;
                }
                Toast.makeText(StartActivity.this, "您的VIP版已经到期，请点击广告获取", 0).show();
            }
        });
        btn2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0) {
                skyconfig.noadmode = Boolean.valueOf(true);
                Editor editor = StartActivity.this.getApplication().getSharedPreferences("settings", 0).edit();
                editor.putInt("restartFor", 1);
                editor.putString("serverDomain", XmlPullParser.NO_NAMESPACE);
                editor.commit();
                Intent intent = new Intent(StartActivity.this, WelcomeActivity.class);
                startActivity(intent);
            }
        });
        enterLayout.addView(btn1);
        enterLayout.addView(btn2);
        first.addView(enterLayout);
        contentView.addView(first, layoutParams);
        setContentView(contentView);
        init();
    }

    private void init() {
        SeeGlobal.httpGetInfo();
        this.userimei = ((TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE)).getDeviceId();
        this.userMac = ((WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE)).getConnectionInfo().getMacAddress();
        try {
            Class<?> c = Class.forName("android.os.SystemProperties");
            this.userAndroidID = (String) c.getMethod("get", new Class[]{String.class, String.class}).invoke(c, new Object[]{"ro.serialno", "unknown"});
        } catch (Exception e) {
        }
        this.settings = getSharedPreferences("settings", 0);
        skyconfig.userID = Md5Util.encode(this.userimei + this.userMac + this.userAndroidID);
        skyconfig.adselect = this.settings.getInt("adselect", 0);
        this.mHandler = new resultHandler();
        this.messageTextView = new TextView(this);
        skyconfig.settings = this.settings;
        skyconfig.flag = 1;
        skyconfig.APPId = this.settings.getString("APPId", XmlPullParser.NO_NAMESPACE);
        skyconfig.BannerPosId = this.settings.getString("BannerPosId", XmlPullParser.NO_NAMESPACE);
        skyconfig.FullId = this.settings.getString("FullId", XmlPullParser.NO_NAMESPACE);
        skyconfig.packageName = this.settings.getString("packageName", XmlPullParser.NO_NAMESPACE);
        skyconfig.baiduID = this.settings.getString("baiduID", XmlPullParser.NO_NAMESPACE);
        skyconfig.baiduSplash = this.settings.getString("baiduSplash", XmlPullParser.NO_NAMESPACE);
        skyconfig.baiduBannerID = this.settings.getString("baiduBannerID", XmlPullParser.NO_NAMESPACE);
        skyconfig.serverDomain = this.settings.getString("serverDomain", XmlPullParser.NO_NAMESPACE);
        skyconfig.admode = this.settings.getInt("admode", 1);
        skyconfig.user_id = this.settings.getInt("user_id", 0);
        skyconfig.recommendInfo = this.settings.getString("recommendInfo", XmlPullParser.NO_NAMESPACE);
        skyconfig.isRecommend = Boolean.valueOf(this.settings.getBoolean("isRecommend", false));
        skyconfig.InterstitialAD = this.settings.getString("InterstitialAD", XmlPullParser.NO_NAMESPACE);
        skyconfig.baiduInterID = this.settings.getString("baiduInterID", XmlPullParser.NO_NAMESPACE);
        skyconfig.isShowGdtInser = Boolean.valueOf(this.settings.getBoolean("isShowGdtInser", true));
        skyconfig.shareInfo = this.settings.getString("shareInfo", XmlPullParser.NO_NAMESPACE);
        skyconfig.isEnableInser = Boolean.valueOf(this.settings.getBoolean("isEnableInser", false));
        skyconfig.vipConfirmMsg = this.settings.getString("vipConfirmMsg", XmlPullParser.NO_NAMESPACE);
        skyconfig.isNeedVipConfirm = Boolean.valueOf(this.settings.getBoolean("isNeedVipConfirm", true));
        skyconfig.xiaomiAPPID = this.settings.getString("xiaomiAPPID", XmlPullParser.NO_NAMESPACE);
        skyconfig.xiaomiBannerID = this.settings.getString("xiaomiBannerID", XmlPullParser.NO_NAMESPACE);
        skyconfig.noXiaomiPercent = this.settings.getInt("noXiaomiPercent", 80);
        skyconfig.value = this.settings.getString("cookie", XmlPullParser.NO_NAMESPACE);
        skyconfig.sign = getSign(this);
        this.mDialog = new ProgressDialog(this);
//        if (!this.firstuse.booleanValue()) {
        new userLogin(this, skyconfig.userID, this.userimei, this.userMac, this.userAndroidID, this.mHandler).startLogin();
//        }
        if (!this.settings.getBoolean("sky_isinit", false)) {
            this.firstuse = Boolean.valueOf(true);
            this.mDialog = new ProgressDialog(this);
            this.mDialog.setTitle("提示");
            this.mDialog.setMessage("初始化中...");
            this.mDialog.show();
        }
    }


    public void finish() {
        Message msg = new Message();
        msg.what = 9;
        this.mHandler.sendMessage(msg);
        if (this.mDialog != null && this.mDialog.isShowing()) {
            this.mDialog.dismiss();
        }
        super.finish();
    }

    protected void onDestroy() {
        if (this.mDialog != null && this.mDialog.isShowing()) {
            this.mDialog.dismiss();
        }
        super.onDestroy();
    }

    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    private String getSign(Context context) {
        for (PackageInfo packageinfo : context.getPackageManager().getInstalledPackages(PackageManager.GET_SIGNATURES)) {
            if (packageinfo.packageName.equals(context.getPackageName())) {
                String sign = packageinfo.signatures[0].toCharsString();
                String substring = sign.substring(sign.length() - 10);
                return "b0128be0a8".replace(" ", XmlPullParser.NO_NAMESPACE);
            }
        }
        return null;
    }

    private void restartAPP() {
        Intent intent = new Intent("restart.app");
        intent.putExtra("pid", Process.myPid());
        sendBroadcast(intent);
        new Handler().postDelayed(new Runnable() {
            public void run() {
                Process.killProcess(Process.myPid());
            }
        }, 100);
    }
}
