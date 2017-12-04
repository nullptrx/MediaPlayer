package com.alivc.player;

import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

public class HttpClientUtil {
    private static final int CONNECTION_TIMEOUT = 10000;

    public static class SSLSocketFactoryImp extends SSLSocketFactory {
        final SSLContext sslContext = SSLContext.getInstance("TLS");

        public SSLSocketFactoryImp(KeyStore truststore) throws NoSuchAlgorithmException, KeyManagementException, KeyStoreException, UnrecoverableKeyException {
            super(truststore);
            TrustManager tm = new X509TrustManager() {
                public X509Certificate[] getAcceptedIssuers() {
                    return null;
                }

                public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                }

                public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                }
            };
            this.sslContext.init(null, new TrustManager[]{tm}, null);
        }

        public Socket createSocket(Socket socket, String host, int port, boolean autoClose) throws IOException, UnknownHostException {
            return this.sslContext.getSocketFactory().createSocket(socket, host, port, autoClose);
        }

        public Socket createSocket() throws IOException {
            return this.sslContext.getSocketFactory().createSocket();
        }
    }

    public static String doHttpGet(String serverURL) throws Exception {
        HttpParams httpParameters = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(httpParameters, 10000);
        HttpConnectionParams.setSoTimeout(httpParameters, 10000);
        HttpClient hc = new DefaultHttpClient();
        HttpGet get = new HttpGet(serverURL);
        get.addHeader("Content-Type", "text/xml");
        get.setParams(httpParameters);
        try {
            HttpResponse response = hc.execute(get);
            int sCode = response.getStatusLine().getStatusCode();
            String value = EntityUtils.toString(response.getEntity());
            hc.getConnectionManager().shutdown();
            if (sCode == 200) {
                return value;
            }
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("StatusCode", sCode);
            jsonObject.put("ResponseStr", value);
            return jsonObject.toString();
        } catch (UnknownHostException e) {
            throw new Exception("Unable to access " + e.getLocalizedMessage());
        } catch (SocketException e2) {
            throw new Exception(e2.getLocalizedMessage());
        }
    }

    public static String doHttpsGet(String serverURL) throws Exception {
        HttpParams httpParameters = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(httpParameters, 10000);
        HttpConnectionParams.setSoTimeout(httpParameters, 10000);
        HttpClient hc = initHttpClient(httpParameters);
        HttpGet get = new HttpGet(serverURL);
        get.addHeader("Content-Type", "text/xml");
        get.setParams(httpParameters);
        try {
            String responseStr = EntityUtils.toString(hc.execute(get).getEntity());
            hc.getConnectionManager().shutdown();
            return responseStr;
        } catch (Exception e) {
            throw new Exception("Unable to access " + e.getLocalizedMessage());
        }
    }

    public static String doHttpPost(String serverURL, String xmlString) throws Exception {
        HttpParams httpParameters = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(httpParameters, 10000);
        HttpConnectionParams.setSoTimeout(httpParameters, 10000);
        HttpProtocolParams.setVersion(httpParameters, HttpVersion.HTTP_1_1);
        HttpProtocolParams.setContentCharset(httpParameters, "UTF-8");
        HttpClient hc = new DefaultHttpClient();
        HttpPost post = new HttpPost(serverURL);
        post.addHeader("Content-Type", "text/xml");
        post.setEntity(new StringEntity(xmlString, "UTF-8"));
        post.setParams(httpParameters);
        try {
            String responseStr = EntityUtils.toString(hc.execute(post).getEntity());
            hc.getConnectionManager().shutdown();
            return responseStr;
        } catch (UnknownHostException e) {
            throw new Exception("Unable to access " + e.getLocalizedMessage());
        } catch (SocketException e2) {
            throw new Exception(e2.getLocalizedMessage());
        }
    }

    public static String doHttpsPost(String serverURL, String xmlString) throws Exception {
        HttpParams httpParameters = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(httpParameters, 10000);
        HttpConnectionParams.setSoTimeout(httpParameters, 10000);
        HttpClient hc = initHttpClient(httpParameters);
        HttpPost post = new HttpPost(serverURL);
        post.addHeader("Content-Type", "text/xml");
        post.setEntity(new StringEntity(xmlString, "UTF-8"));
        post.setParams(httpParameters);
        try {
            String responseStr = EntityUtils.toString(hc.execute(post).getEntity());
            hc.getConnectionManager().shutdown();
            return responseStr;
        } catch (UnknownHostException e) {
            throw new Exception("Unable to access " + e.getLocalizedMessage());
        } catch (SocketException e2) {
            throw new Exception(e2.getLocalizedMessage());
        }
    }

    public static HttpClient initHttpClient(HttpParams params) {
        try {
            KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
            trustStore.load(null, null);
            SSLSocketFactory sf = new SSLSocketFactoryImp(trustStore);
            sf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
            HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
            HttpProtocolParams.setContentCharset(params, "UTF-8");
            SchemeRegistry registry = new SchemeRegistry();
            registry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
            registry.register(new Scheme("https", sf, 443));
            return new DefaultHttpClient(new ThreadSafeClientConnManager(params, registry), params);
        } catch (Exception e) {
            return new DefaultHttpClient(params);
        }
    }
}
