package com.alivc.player.model;

import android.annotation.SuppressLint;

import org.apache.http.conn.ssl.AllowAllHostnameVerifier;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.Map;
import java.util.Map.Entry;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.X509TrustManager;

public class HttpClient {
    @SuppressLint("AllowAllHostnameVerifier")
    private static final AllowAllHostnameVerifier HOSTNAME_VERIFIER = new AllowAllHostnameVerifier();
    private static HttpURLConnection conn = null;
    private static X509TrustManager xtm = new X509TrustManager() {
        @SuppressLint("TrustAllX509TrustManager")
        public void checkClientTrusted(X509Certificate[] chain, String authType) {
        }

        @SuppressLint("TrustAllX509TrustManager")
        public void checkServerTrusted(X509Certificate[] chain, String authType) {
        }

        public X509Certificate[] getAcceptedIssuers() {
            return null;
        }
    };
    private static X509TrustManager[] xtmArray = new X509TrustManager[]{xtm};

    public static InputStream sendPOSTRequestForInputStream(String path, Map<String, String> params, String encoding) throws Exception {
        StringBuilder entityBuilder = new StringBuilder("");
        if (!(params == null || params.isEmpty())) {
            for (Entry<String, String> entry : params.entrySet()) {
                entityBuilder.append((String) entry.getKey()).append('=');
                entityBuilder.append(URLEncoder.encode((String) entry.getValue(), encoding));
                entityBuilder.append('&');
            }
            entityBuilder.deleteCharAt(entityBuilder.length() - 1);
        }
        byte[] entity = entityBuilder.toString().getBytes();
        conn = (HttpURLConnection) new URL(path).openConnection();
        if (conn instanceof HttpsURLConnection) {
            SSLContext context = SSLContext.getInstance("TLS");
            context.init(new KeyManager[0], xtmArray, new SecureRandom());
            ((HttpsURLConnection) conn).setSSLSocketFactory(context.getSocketFactory());
            ((HttpsURLConnection) conn).setHostnameVerifier(HOSTNAME_VERIFIER);
        }
        conn.setConnectTimeout(5000);
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        conn.setRequestProperty("Content-Length", String.valueOf(entity.length));
        OutputStream outStream = conn.getOutputStream();
        outStream.write(entity);
        outStream.flush();
        outStream.close();
        if (conn.getResponseCode() == 200) {
            return conn.getInputStream();
        }
        return conn.getInputStream();
    }

    public static void closeConnection() {
        if (conn != null) {
            conn.disconnect();
        }
    }
}
