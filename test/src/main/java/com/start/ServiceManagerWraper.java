package com.start;

import android.content.Context;
import android.content.pm.PackageManager;
import java.lang.reflect.Field;
import java.lang.reflect.Proxy;

public class ServiceManagerWraper {
    public static void hookPMS(Context context) {
        if (context != null) {
            try {
                Class<?> activityThreadClass = Class.forName("android.app.ActivityThread");
                Object currentActivityThreadMethod = activityThreadClass.getDeclaredMethod("currentActivityThread", new Class[0]).invoke(null, new Object[0]);
                Field sPackageManagerField = activityThreadClass.getDeclaredField("sPackageManager");
                sPackageManagerField.setAccessible(true);
                Object sPackageManager = sPackageManagerField.get(currentActivityThreadMethod);
                Class<?> iPackageManagerInterface = Class.forName("android.content.pm.IPackageManager");
                PmsHookBinderInvocationHandler pmsHookBinderInvocationHandler = new PmsHookBinderInvocationHandler(sPackageManager);
                Object proxy = Proxy.newProxyInstance(iPackageManagerInterface.getClassLoader(), new Class[]{iPackageManagerInterface}, pmsHookBinderInvocationHandler);
                sPackageManagerField.set(currentActivityThreadMethod, proxy);
                PackageManager pm = context.getPackageManager();
                Field mPmField = pm.getClass().getDeclaredField("mPM");
                mPmField.setAccessible(true);
                mPmField.set(pm, proxy);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
