package com.alivc.player;

import android.app.Activity;
import android.content.Context;
import android.media.AudioManager;
import android.provider.Settings.SettingNotFoundException;
import android.provider.Settings.System;
import android.view.Window;
import android.view.WindowManager.LayoutParams;

public class VideoAdjust {
    private static final String TAG = VideoAdjust.class.getSimpleName();
    private int currentVolume = 0;
    private AudioManager mAudioManage;
    private Context mContext = null;
    private int maxVolume = 0;

    public VideoAdjust(Context context) {
        this.mContext = context;
        this.mAudioManage = (AudioManager) this.mContext.getApplicationContext().getSystemService("audio");
        this.maxVolume = this.mAudioManage.getStreamMaxVolume(3);
        this.currentVolume = this.mAudioManage.getStreamVolume(3);
    }

    public void SetVolumn(float fVol) {
        this.mAudioManage.setStreamVolume(3, (int) (((float) this.maxVolume) * fVol), 0);
    }

    public int getVolume() {
        this.currentVolume = this.mAudioManage.getStreamVolume(3);
        return (int) ((((float) this.currentVolume) * 100.0f) / ((float) this.maxVolume));
    }

    public void setBrightness(int brightness) {
        if (this.mContext != null) {
            if (this.mContext instanceof Activity) {
                VcPlayerLog.d(TAG, "setScreenBrightness mContext instanceof Activity brightness = " + brightness);
                if (brightness > 0) {
                    Window localWindow = ((Activity) this.mContext).getWindow();
                    LayoutParams localLayoutParams = localWindow.getAttributes();
                    localLayoutParams.screenBrightness = ((float) brightness) / 100.0f;
                    localWindow.setAttributes(localLayoutParams);
                    return;
                }
                return;
            }
            try {
                boolean suc = System.putInt(this.mContext.getContentResolver(), "screen_brightness_mode", 0);
                System.putInt(this.mContext.getContentResolver(), "screen_brightness", (int) (((float) brightness) * 2.55f));
                VcPlayerLog.d(TAG, "setScreenBrightness suc " + suc);
            } catch (Exception e) {
                VcPlayerLog.e(TAG, "cannot set brightness cause of no write_setting permission e = " + e.getMessage());
            }
        }
    }

    public int getScreenBrightness() {
        if (this.mContext == null) {
            return -1;
        }
        if (this.mContext instanceof Activity) {
            float screenBrightness = ((Activity) this.mContext).getWindow().getAttributes().screenBrightness;
            if (screenBrightness > 1.0f) {
                screenBrightness = 1.0f;
            } else if (((double) screenBrightness) < 0.1d) {
                screenBrightness = 0.1f;
            }
            VcPlayerLog.d(TAG, "getActivityBrightness layoutParams.screenBrightness = " + screenBrightness);
            return (int) (100.0f * screenBrightness);
        }
        try {
            return (int) (((float) (System.getInt(this.mContext.getContentResolver(), "screen_brightness") * 100)) / 255.0f);
        } catch (SettingNotFoundException e) {
            VcPlayerLog.e(TAG, "getScreenBrightness failed: " + e.getMessage());
            return -1;
        }
    }

    public void destroy() {
        if (this.mContext != null) {
            this.mContext = null;
        }
    }
}
