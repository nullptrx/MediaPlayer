package com.start;

public class TextUtils {
    public static Boolean isEmpty(String str) {
        if (str == null || str.length() == 0) {
            return Boolean.valueOf(true);
        }
        return Boolean.valueOf(false);
    }
}
