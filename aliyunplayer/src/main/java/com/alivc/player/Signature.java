package com.alivc.player;

import android.util.Base64;
import java.lang.reflect.Method;
import java.security.SignatureException;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class Signature {
    private static final String HMAC_SHA1_ALGORITHM = "HmacSHA1";

    public static String calculateRFC2104HMAC(String data, String key) throws SignatureException {
        try {
            SecretKeySpec signingKey = new SecretKeySpec(key.getBytes(), HMAC_SHA1_ALGORITHM);
            Mac mac = Mac.getInstance(HMAC_SHA1_ALGORITHM);
            mac.init(signingKey);
            return encode(mac.doFinal(data.getBytes()));
        } catch (Exception e) {
            throw new SignatureException("Failed to generate HMAC : " + e.getMessage());
        }
    }

    public static String encodeBase64(byte[] input) throws Exception {
        Method mainMethod = Class.forName("com.sun.org.apache.xerces.internal.impl.dv.util.Base64").getMethod("encode", new Class[]{byte[].class});
        mainMethod.setAccessible(true);
        return (String) mainMethod.invoke(null, new Object[]{input});
    }

    public static String encode(byte[] bytes) {
        return Base64.encodeToString(bytes, 2);
    }
}
