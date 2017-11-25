package com.start;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class RestartAppReceiver extends BroadcastReceiver {
    public void onReceive(Context context, Intent intent) {
        String packageName = context.getPackageName();
        if (intent.getAction().equals("restart.app")) {
            int count = 120;
            Intent i = context.getPackageManager().getLaunchIntentForPackage(packageName);
            i.addFlags(270532608);
            while (isRunningForeground(context) && count > 0) {
                try {
                    Thread.sleep(100);
                    count--;
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            try {
                Thread.sleep(200);
            } catch (InterruptedException e2) {
                e2.printStackTrace();
            }
            context.startActivity(i);
            count = 40;
            while (!isRunningForeground(context) && count > 0) {
                try {
                    Thread.sleep(100);
                    count--;
                } catch (InterruptedException e22) {
                    e22.printStackTrace();
                }
            }
            if (count == 0) {
                Log.d("skylog", "restart error,try again");
                context.startActivity(i);
            }
        }
    }

    public static boolean isRunningForeground(Context context) {
        String currentPackageName = ((RunningTaskInfo) ((ActivityManager) context.getSystemService("activity")).getRunningTasks(1).get(0)).topActivity.getPackageName();
        if (TextUtils.isEmpty(currentPackageName).booleanValue() || !currentPackageName.equals(context.getPackageName())) {
            return false;
        }
        return true;
    }
}
