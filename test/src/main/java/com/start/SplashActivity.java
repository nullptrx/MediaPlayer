package com.start;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.util.Log;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParser;

public class SplashActivity extends Activity  {
    RelativeLayout baiduBanner;
    RelativeLayout gdtBanner;
    Boolean isBaiduSplash = Boolean.valueOf(false);
    SharedPreferences settings;
    SimpleDateFormat sim = new SimpleDateFormat("yyyy-MM-dd");
    RelativeLayout splashLayout;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(1);
        RelativeLayout contentView = new RelativeLayout(this);
        this.baiduBanner = new RelativeLayout(this);
        this.gdtBanner = new RelativeLayout(this);
        this.splashLayout = new RelativeLayout(this);
        this.baiduBanner.setId(1);
        this.gdtBanner.setId(2);
        this.splashLayout.setId(3);
        this.settings = getSharedPreferences("settings", 0);
        LayoutParams baiduLp = new LayoutParams(-1, -2);
        baiduLp.addRule(2, this.gdtBanner.getId());
        LayoutParams gdtLp = new LayoutParams(-1, -2);
        gdtLp.addRule(12);
        LayoutParams splashLp = new LayoutParams(-1, -1);
        splashLp.addRule(2, this.baiduBanner.getId());
        LayoutParams contentlp = new LayoutParams(-1, -1);
        contentView.addView(this.gdtBanner, gdtLp);
        contentView.addView(this.baiduBanner, baiduLp);
        contentView.addView(this.splashLayout, splashLp);
        setContentView(contentView, contentlp);
        if (this.sim.format(new Date()).equals(this.settings.getString("preLoadBaiduSplashDate", "0000-00-00"))) {
            this.splashLayout.removeAllViews();
            return;
        }
        this.isBaiduSplash = Boolean.valueOf(true);
    }

    private void jump() {
        finish();
    }

}
