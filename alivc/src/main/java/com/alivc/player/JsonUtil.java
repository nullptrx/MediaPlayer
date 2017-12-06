package com.alivc.player;

import org.json.JSONException;
import org.json.JSONObject;

public class JsonUtil {
    private static final int INT_EMPTY_VALUE = 0;
    private static final String STR_EMPTY_VALUE = "";

    public static int getInt(JSONObject jsonObject, String name) {
        int i = 0;
        if (jsonObject != null) {
            try {
                i = jsonObject.getInt(name);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return i;
    }

    public static String getString(JSONObject jsonObject, String name) {
        if (jsonObject == null) {
            return "";
        }
        try {
            return jsonObject.getString(name);
        } catch (JSONException e) {
            e.printStackTrace();
            return "";
        }
    }
}
