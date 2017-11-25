package com.start;

import android.content.pm.PackageInfo;
import android.content.pm.Signature;

import org.xmlpull.v1.XmlPullParser;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

public class PmsHookBinderInvocationHandler implements InvocationHandler {
    public static String SIGN = XmlPullParser.NO_NAMESPACE;
    private Object base;

    public PmsHookBinderInvocationHandler(Object base) {
        try {
            this.base = base;
        } catch (Exception e) {
        }
    }

    public Object invoke(Object arg0, Method arg1, Object[] arg2) throws Throwable {
        Signature sign = new Signature(SIGN);
        PackageInfo info = (PackageInfo) arg1.invoke(this.base, arg2);
        if (info == null) {
            return info;
        }
        info.signatures[0] = sign;
        return info;
    }
}
