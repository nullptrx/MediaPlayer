package com.alivc.player;

import android.content.Context;

import com.alivc.player.logreport.PublicPraram;
import com.alivc.player.model.AuthModel;
import com.alivc.player.model.Switch;
import com.alivc.player.model.SwithList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.security.SignatureException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class AuthControl {
    private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
    private Map<String, String> mAuthMap = new HashMap();

    public AuthControl(Context context) {
    }

    public boolean isPlayerAuthed() {
        return isAuthed("FunctionBase");
    }

    public boolean isSoftDecodeAuthed() {
        return isAuthed("SoftwareDecode264");
    }

    public boolean isInfoReportAuthed() {
        return isAuthed("ReportLog");
    }

    private boolean isAuthed(String functionName) {
        String state = (String) this.mAuthMap.get(functionName);
        if (state == null || !state.equalsIgnoreCase("on")) {
            return false;
        }
        return true;
    }

    public Map<String, String> getAuthMap() {
        return this.mAuthMap;
    }

    private AuthModel createAuthFromJson(String json) {
        AuthModel authModel = new AuthModel();
        try {
            JSONObject jsonObject = new JSONObject(json);
            authModel.setLogURL(jsonObject.getString("LogURL"));
            SwithList swithList = new SwithList();
            authModel.setSwitchList(swithList);
            List<Switch> switchs = new ArrayList<>();
            if (jsonObject.getJSONObject("SwitchList") != null) {
                JSONArray switchsJson = jsonObject.getJSONObject("SwitchList").getJSONArray("Switch");
                if (switchsJson != null && switchsJson.length() > 0) {
                    for (int i = 0; i < switchsJson.length(); i++) {
                        JSONObject switchJson = switchsJson.getJSONObject(i);
                        if (switchJson != null) {
                            Switch switcher = new Switch();
                            switcher.setFunctionName(switchJson.getString("FunctionName"));
                            switcher.setState(switchJson.getString("State"));
                            switcher.setSwitchId(switchJson.getString("SwitchId"));
                            switchs.add(switcher);
                        }
                    }
                }
            }
            if (switchs.size() > 0) {
                swithList.setSwitch(switchs.toArray(new Switch[switchs.size()]));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return authModel;
    }

    public boolean checkTopValid(AccessKey accessToken) {
        try {
            String json = HttpClientUtil.doHttpsGet(constructUrl(accessToken.getAccessId(), accessToken.getAccessKey(), UUID.randomUUID().toString()));
            if (json == null) {
                return false;
            }
            AuthModel auth = createAuthFromJson(json);
            if (!(auth == null || auth.getSwitchList() == null || auth.getSwitchList().getSwitch() == null)) {
                if (auth.getLogURL() != null) {
                    PublicPraram.setHost(auth.getLogURL());
                }
                for (Switch sw : auth.getSwitchList().getSwitch()) {
                    if (!(sw.getFunctionName() == null || sw.getState() == null)) {
                        this.mAuthMap.put(sw.getFunctionName(), sw.getState());
                    }
                }
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static String inputStream2String(InputStream is) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        while (true) {
            int i = is.read();
            if (i == -1) {
                return baos.toString();
            }
            baos.write(i);
        }
    }

    private String constructUrl(String accessKeyId, String accessKeySecret, String signatureNonce) {
        String seperator = "&";
        StringBuffer urlSB = new StringBuffer();
        String timestamp = sdf.format(new Date(System.currentTimeMillis() - 28800000));
        urlSB.append("Action=PlayerAuth&Format=JSON&PageSize=2&").append("SignatureMethod=").append(toURLEncoded("HMAC-SHA1"));
        urlSB.append(seperator).append("SignatureNonce=").append(toURLEncoded(signatureNonce)).append(seperator).append("SignatureVersion=");
        urlSB.append(toURLEncoded("1.0")).append(seperator).append("TimeStamp=").append(toURLEncoded(timestamp));
        urlSB.append(seperator).append("Version=2014-06-18&").append("accessKeyId=").append(toURLEncoded(accessKeyId));
        String key = accessKeySecret + seperator;
        StringBuffer getStrSB = new StringBuffer();
        getStrSB.append("GET&").append(toURLEncoded("/")).append(seperator).append(toURLEncoded(urlSB.toString()));
        String strUrl = getStrSB.toString();
        StringBuffer resStrSB = new StringBuffer();
        try {
            resStrSB.append("https://mts.aliyuncs.com?").append("Signature=").append(toURLEncoded(Signature.calculateRFC2104HMAC(strUrl, key)));
            resStrSB.append(seperator).append(urlSB.toString());
        } catch (SignatureException e) {
            e.printStackTrace();
        }
        return resStrSB.toString();
    }

    public static String toURLEncoded(String paramString) {
        if (paramString == null || paramString.equals("")) {
            return "";
        }
        try {
            return URLEncoder.encode(new String(paramString.getBytes(), "UTF-8"), "UTF-8");
        } catch (Exception e) {
            return "";
        }
    }
}
