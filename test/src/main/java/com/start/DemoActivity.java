package com.start;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.qiyi.video.WelcomeActivity;

import org.xmlpull.v1.XmlPullParser;

import java.util.Date;

/**
 * @author 诸葛不亮
 * @version 1.0
 * @description
 */

public class DemoActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        RelativeLayout contentView = new RelativeLayout(this);
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(-1, -1);
        layoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
        LinearLayout first = new LinearLayout(this);
        first.setOrientation(LinearLayout.VERTICAL);
        TextView view = new TextView(this);
        view.setText("爱奇艺VIP版v" + skyconfig.current_version);
        first.addView(view);
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
                startActivity(Intent.createChooser(intent, "分享到"));
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
                if (skyconfig.isNeedVipConfirm.booleanValue()) {
                    new AlertDialog.Builder(DemoActivity.this).setTitle("爱奇艺VIP版进入确认").setMessage(skyconfig.vipConfirmMsg).setCancelable(true).setPositiveButton("进入VIP版", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface arg0, int arg1) {
                            if (skyconfig.isLoginSuccess.booleanValue()) {
                                SharedPreferences.Editor editor = getApplication().getSharedPreferences("settings", 0).edit();
                                editor.putInt("restartFor", 2);
                                editor.putString("serverDomain", skyconfig.serverDomain);
                                editor.commit();
                                new Intent().setClass(DemoActivity.this, WelcomeActivity.class);
                                skyconfig.noadmode = Boolean.valueOf(false);
                                finish();
                                return;
                            }
                            Date timeNow = new Date();
                            Toast.makeText(DemoActivity.this, "爱奇艺VIP版初始化还未完成，请稍等或完全退出后重新进入", Toast.LENGTH_LONG).show();
                        }
                    }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface arg0, int arg1) {
                        }
                    }).show();
                    return;
                }
                SharedPreferences.Editor editor = getApplication().getSharedPreferences("settings", 0).edit();
                editor.putInt("restartFor", 2);
                editor.putString("serverDomain", skyconfig.serverDomain);
                editor.commit();
                new Intent().setClass(DemoActivity.this, WelcomeActivity.class);
                skyconfig.noadmode = Boolean.valueOf(false);
                finish();
                return;
            }
        });
        btn2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0) {
                skyconfig.noadmode = Boolean.valueOf(true);
                new Intent().setClass(DemoActivity.this, WelcomeActivity.class);
                SharedPreferences.Editor editor = getApplication().getSharedPreferences("settings", 0).edit();
                editor.putInt("restartFor", 1);
                editor.putString("serverDomain", XmlPullParser.NO_NAMESPACE);
                editor.commit();
            }
        });
        enterLayout.addView(btn1);
        enterLayout.addView(btn2);
        first.addView(enterLayout);
        contentView.addView(first, layoutParams);
        setContentView(contentView);
    }
}
