
package net.smallchat.im.api;

import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import net.smallchat.im.Entity.MoreFile;
import net.smallchat.im.R;
import net.smallchat.im.global.ChatApplication;
import net.smallchat.im.global.GlobalParam;
import net.smallchat.im.global.IMCommon;


public class Utility {
    public  static String TAG="IM_APP_API";
    private static IMParameters mRequestHeader = new IMParameters();
    private static HttpHeaderFactory mAuth;

    public static final String BOUNDARY = "7cd4a6d158c";
    public static final String MP_BOUNDARY = "--" + BOUNDARY;
    public static final String END_MP_BOUNDARY = "--" + BOUNDARY + "--";
    public static final String MULTIPART_FORM_DATA = "multipart/form-data";

    public static final String HTTPMETHOD_POST = "POST";
    public static final String HTTPMETHOD_GET = "GET";
    public static final String HTTPMETHOD_DELETE = "DELETE";

    private static final int SET_CONNECTION_TIMEOUT = 50000;
    private static final int SET_SOCKET_TIMEOUT = 30000;
    private static final int PER_SPEED = 16;
    private static HttpClient mClient;


    public static void setAuthorization(HttpHeaderFactory auth) {
        mAuth = auth;
    }

    public static void setHeader(String httpMethod, HttpUriRequest request,
                                 IMParameters authParam, String url) {
        if (!isBundleEmpty(mRequestHeader)) {
            for (int loc = 0; loc < mRequestHeader.size(); loc++) {
                String key = mRequestHeader.getKey(loc);
                request.setHeader(key, mRequestHeader.getValue(key));
            }
        }
        if (!isBundleEmpty(authParam) && mAuth != null) {
            String authHeader = mAuth.getWeiboAuthHeader(httpMethod, url, authParam);
            if (authHeader != null) {
                request.setHeader("Authorization", authHeader);
            }
        }
        request.setHeader("User-Agent", System.getProperties().getProperty("http.agent")
                + " ChatAndroidSDK");
    }

    public static boolean isBundleEmpty(IMParameters bundle) {
        /*if (bundle == null || bundle.size() == 0) {
            return true;
        }*/
        if (bundle == null) {
            return true;
        }
        return false;
    }

    public static void setRequestHeader(String key, String value) {
        // mRequestHeader.clear();
        mRequestHeader.add(key, value);
    }

    public static void setRequestHeader(IMParameters params) {
        mRequestHeader.addAll(params);
    }

    public static void clearRequestHeader() {
        mRequestHeader.clear();

    }

    public static String encodePostBody(Bundle parameters, String boundary) {
        if (parameters == null)
            return "";
        StringBuilder sb = new StringBuilder();

        for (String key : parameters.keySet()) {
            if (parameters.getByteArray(key) != null) {
                continue;
            }

            sb.append("Content-Disposition: form-data; name=\"" + key + "\"\r\n\r\n"
                    + parameters.getString(key));
            sb.append("\r\n" + "--" + boundary + "\r\n");
        }

        return sb.toString();
    }

    public static String encodeUrl(IMParameters parameters) {
        if (parameters == null) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (int loc = 0; loc < parameters.size(); loc++) {
            if (first)
                first = false;
            else{
                sb.append("&");
            }
            try {
                sb.append(URLEncoder.encode(parameters.getKey(loc), "UTF-8") + "="
                        + URLEncoder.encode(parameters.getValue(loc), "UTF-8"));
            } catch (UnsupportedEncodingException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return sb.toString();
    }

    public static Bundle decodeUrl(String s) {
        Bundle params = new Bundle();
        if (s != null) {
            String array[] = s.split("&");
            for (String parameter : array) {
                String v[] = parameter.split("=");
                params.putString(URLDecoder.decode(v[0]), URLDecoder.decode(v[1]));
            }
        }
        return params;
    }

    /**
     * Parse a URL query and fragment parameters into a key-value bundle.
     *
     * @param url
     *            the URL to parse
     * @return a dictionary bundle of keys and values
     */
    public static Bundle parseUrl(String url) {
        // hack to prevent MalformedURLException
        url = url.replace("weiboconnect", "http");
        try {
            URL u = new URL(url);
            Bundle b = decodeUrl(u.getQuery());
            b.putAll(decodeUrl(u.getRef()));
            return b;
        } catch (MalformedURLException e) {
            return new Bundle();
        }
    }

    /**
     * Construct a url encoded entity by parameters .
     *
     * @param bundle
     *            :parameters key pairs
     * @return UrlEncodedFormEntity: encoed entity
     */
    public static UrlEncodedFormEntity getPostParamters(Bundle bundle){
        if (bundle == null || bundle.isEmpty()) {
            return null;
        }
        try {
            List<NameValuePair> form = new ArrayList<NameValuePair>();
            for (String key : bundle.keySet()) {
                form.add(new BasicNameValuePair(key, bundle.getString(key)));
            }
            UrlEncodedFormEntity entity = new UrlEncodedFormEntity(form, "UTF-8");
            return entity;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }


    public static String openUrl(String url, String method,
                                 IMParameters params,int loginType) throws IMException {
        /*params.add("userType", "2");*/

        if(loginType == 1){
            if(!IMCommon.checkLogin()){


                Intent toastIntent = new Intent(GlobalParam.ACTION_SHOW_TOAST);

                toastIntent.putExtra("toast_msg",ChatApplication.getInstance().getResources().getString(R.string.account_repeat));
                ChatApplication.getInstance().sendBroadcast(toastIntent);


                return "";
            }
        }
        //处理上传文件
        String rlt = "";
        List<MoreFile> filePath = new ArrayList<MoreFile>();
        if (params!=null && !params.equals("")) {
            for (int loc = 0; loc < params.size(); loc++) {
                String key = params.getKey(loc);
                //如果是发送图片
                if (key.equals("imageUpload")) {
                    Log.d(TAG,"REQUEST image file size="+filePath.size());
                    //HashMap<String,List<MoreFile>> picMap =params.getImageList("pic");
                    filePath = params.getImageList("imageUpload");
                    params.remove(key);
                }
                //文件发送
                if (key.equals("fileUpload")) {

                    Log.d(TAG,"REQUEST file size="+filePath.size());
                    //HashMap<String,List<MoreFile>> picMap =params.getImageList("pic");
                    filePath = params.getFileList("fileUpload");
                    params.remove(key);
                }

                //语音发送
                if (key.equals("audioUpload")) {

                    Log.d(TAG,"REQUEST audio file size="+filePath.size());
                    //HashMap<String,List<MoreFile>> picMap =params.getImageList("pic");
                    filePath = params.getAudioList("audioUpload");
                    params.remove(key);
                }

                //视频发送
                if (key.equals("videoUpload")) {

                    Log.d(TAG,"REQUEST video file size="+filePath.size());
                    //HashMap<String,List<MoreFile>> picMap =params.getImageList("pic");
                    filePath = params.getVideoList("videoUpload");
                    params.remove(key);
                }

            }
            //发送请求到服务器
            if (filePath == null || filePath.equals("")) {
                //普通POST请求
                Log.d(TAG,"General POST");
                rlt = openUrl(url, method, params,null);
            } else {
                //有文件上传
                Log.d(TAG,"File POST");
                rlt = openUrl(url, method, params, filePath);
            }
        }


        return rlt;
    }

    public static HttpClient getClient(){
        return mClient;
    }

    public static String openUrl(String url, String method, IMParameters params, List<MoreFile> filePath) throws IMException{
        String result = "";

        long timeout = 0;
    	/*File files = null;
    	if(!TextUtils.isEmpty(file)){
    		files = new File(file);
    		timeout = files.length() * 1000/(PER_SPEED * 1024);
    	}*/

        HttpClient client = getNewHttpClient(timeout);
        //mClient = client;
        try {
            HttpUriRequest request = null;
            ByteArrayOutputStream bos = null;

            if (method.equals("GET")) {
                url = url + "?" + encodeUrl(params); //Log.e("url",url);
                Log.e(TAG,"API GET URL="+ url);
                HttpGet get = new HttpGet(url);
                request = get;

            } else if (method.equals("POST")) {
                Log.e(TAG,"API POST URL="+ url);
                HttpPost post = new HttpPost(url);
                byte[] data = null;
                bos = new ByteArrayOutputStream(1024 * 50);
                if (filePath != null && filePath.size() > 0) {
                    Utility.paramToUpload(bos, params);
                    post.setHeader("Content-Type", MULTIPART_FORM_DATA + "; boundary=" + BOUNDARY);
                    //post.setHeader("Charset", "UTF-8");
                    for (int i = 0; i < filePath.size(); i++) {
                        /*	Bitmap bf = BitmapFactory.decodeFile(filePath.get(i).filePath);*/
                        Log.d(TAG, "file key= "+filePath.get(i).key+" file_path="+filePath.get(i).filePath);
                        Utility.fileContentToUpload(bos, new File(filePath.get(i).filePath), filePath.get(i).key);
                    }
                    Log.d(TAG,"start write post data with file");
                    bos.write(("\r\n" + END_MP_BOUNDARY).getBytes());
                } else {

                    post.setHeader("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8");
                    String postParam = encodeParameters(params);
                    data = postParam.getBytes("UTF-8");
                    Log.d(TAG,"start write post data");
                    bos.write(data);
                }
                data = bos.toByteArray();
                bos.close();
                // UrlEncodedFormEntity entity = getPostParamters(params);
                ByteArrayEntity formEntity = new ByteArrayEntity(data);
                post.setEntity(formEntity);
                request = post;
            } else if (method.equals("DELETE")) {
                request = new HttpDelete(url);
            }
            setHeader(method, request, params, url);
            Log.d(TAG,"start post execute sendRequest.");
            HttpResponse response = client.execute(request);
            StatusLine status = response.getStatusLine();

            int statusCode = status.getStatusCode();
            Log.d(TAG,"API RESULT STATUS CODE="+status.getStatusCode()+" RESPONSE="+result);
            if (statusCode != 200) {
                result = read(response);
                String err = null;
                int errCode = 0;
                try {
                    Log.d(TAG,"API RESULT ="+result);
                    JSONObject json = new JSONObject(result);
                    err = json.getString("error");
                    errCode = json.getInt("error_code");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                //throw new WeiboException(String.format(err), errCode);e
            }
            // parse content stream from response
            result = read(response);
            return result;
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
            Log.e(TAG, e.getClass().toString());
            if(e.getClass().toString().equalsIgnoreCase("class java.nio.channels.UnresolvedAddressException")){
                throw new IMException("UnresolvedAddress", e, R.string.unknown_addr);
            }else if(e.getClass().toString().equalsIgnoreCase("class java.net.UnknownHostException")){
                throw new IMException("UnknownHost", e, R.string.error_host);
            }else if(e.getClass().toString().equalsIgnoreCase("class org.apache.http.conn.ConnectTimeoutException")){
                throw new IMException("ConnectionTimeout", e, R.string.timeout);
            }else if(e.getClass().toString().equalsIgnoreCase("class java.net.SocketTimeoutException")){
                throw new IMException("SocketTimeout", e, R.string.timeout);
            }
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            if(client != null && client.getConnectionManager() != null){
                client.getConnectionManager().shutdown();
                client = null;
            }
        }
        return null;
    }



    public static HttpClient getNewHttpClient(long timeout) {
        try {
            KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
            trustStore.load(null, null);

            SSLSocketFactory sf = new MySSLSocketFactory(trustStore);
            sf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);

            HttpParams params = new BasicHttpParams();

            //HttpConnectionParams.setConnectionTimeout(params, 10000);
            //HttpConnectionParams.setSoTimeout(params, 10000);

            HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
            //HttpProtocolParams.setContentCharset(params, HTTP.);

            SchemeRegistry registry = new SchemeRegistry();
            registry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
            registry.register(new Scheme("https", sf, 443));

            ClientConnectionManager ccm = new ThreadSafeClientConnManager(params, registry);

            // Set the default socket timeout (SO_TIMEOUT) // in
            // milliseconds which is the timeout for waiting for data.
            HttpConnectionParams.setConnectionTimeout(params, Utility.SET_CONNECTION_TIMEOUT);
            long soc_time = Utility.SET_SOCKET_TIMEOUT + timeout;
            HttpConnectionParams.setSoTimeout(params, (int)soc_time);
            HttpClient client = new DefaultHttpClient(ccm, params);
            return client;
        } catch (Exception e) {
            return new DefaultHttpClient();
        }
    }

    public static class MySSLSocketFactory extends SSLSocketFactory {
        SSLContext sslContext = SSLContext.getInstance("TLS");

        public MySSLSocketFactory(KeyStore truststore) throws NoSuchAlgorithmException,
                KeyManagementException, KeyStoreException, UnrecoverableKeyException {
            super(truststore);

            TrustManager tm = new X509TrustManager() {
                public void checkClientTrusted(X509Certificate[] chain, String authType)
                        throws CertificateException {
                }

                public void checkServerTrusted(X509Certificate[] chain, String authType)
                        throws CertificateException {
                }

                public X509Certificate[] getAcceptedIssuers() {
                    return null;
                }
            };

            sslContext.init(null, new TrustManager[] { tm }, null);
        }

        @Override
        public Socket createSocket(Socket socket, String host, int port, boolean autoClose)
                throws IOException, UnknownHostException {
            return sslContext.getSocketFactory().createSocket(socket, host, port, autoClose);
        }

        @Override
        public Socket createSocket() throws IOException {
            return sslContext.getSocketFactory().createSocket();
        }
    }

    /**
     * Get a HttpClient object which is setting correctly .
     *
     * @param context
     *            : context of activity
     * @return HttpClient: HttpClient object
     */
    public static DefaultHttpClient getHttpClient(Context context) {
        BasicHttpParams httpParameters = new BasicHttpParams();
        // Set the default socket timeout (SO_TIMEOUT) // in
        // milliseconds which is the timeout for waiting for data.
        HttpConnectionParams.setConnectionTimeout(httpParameters, Utility.SET_CONNECTION_TIMEOUT);
        HttpConnectionParams.setSoTimeout(httpParameters, Utility.SET_SOCKET_TIMEOUT);
        DefaultHttpClient client = new DefaultHttpClient(httpParameters);
//        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
//        if (!wifiManager.isWifiEnabled()) {
//
//            Uri uri = Uri.parse("content://telephony/carriers/preferapn");
//            Cursor mCursor = context.getContentResolver().query(uri, null, null, null, null);
//            if (mCursor != null && mCursor.moveToFirst()) {
//
//                String proxyStr = mCursor.getString(mCursor.getColumnIndex("proxy"));
//                if (proxyStr != null && proxyStr.trim().length() > 0) {
//                    HttpHost proxy = new HttpHost(proxyStr, 80);
//                    client.getParams().setParameter(ConnRouteParams.DEFAULT_PROXY, proxy);
//                }
//                mCursor.close();
//            }
//        }
        return client;
    }

    /**
     * Upload file into output stream .
     *
     * @param out
     *            : output stream for uploading
     * @param file
     *            : file for uploading
     * @param key
     * 				 :uploaded files' key;
     * @return void
     */
    private static void fileContentToUpload(OutputStream out, /*Bitmap imgpath*/File file,String key )
            throws IMException {
        StringBuilder temp = new StringBuilder();

        temp.append(MP_BOUNDARY).append("\r\n");
       /* temp.append("Content-Disposition: form-data; name=\"f_upload\"; filename=\"" + file.getName() + "")
                .append("").append("\"\r\n");*/
        temp.append("Content-Disposition: form-data; name=\""+key+"\"; filename=\""+file.getName())
                /*.append(key+".png")*/.append("").append("\"\r\n");
        byte[] fileData = getFileByte(file);
        String filetype = "multipart/form-data";

        temp.append("Content-Type: ").append(filetype).append("\r\n\r\n");
        byte[] res = temp.toString().getBytes();
        BufferedInputStream bis = null;
        try {
            out.write(res);
            out.write(fileData);
            //imgpath.compress(CompressFormat.PNG, 75, out);
            out.write("\r\n".getBytes());
            //out.write(("\r\n" + END_MP_BOUNDARY).getBytes());
        } catch (IOException e) {
            throw new IMException(e);
        } finally {
            if (null != bis) {
                try {
                    bis.close();
                } catch (IOException e) {
                    throw new IMException(e);
                }
            }
        }
    }

    private static byte[] getFileByte(File file){

        byte[] buffer = null;
        FileInputStream fin;
        try {
            fin = new FileInputStream(file.getPath());
            int length;
            try {
                length = fin.available();
                buffer = new byte[length];
                fin.read(buffer);
                fin.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return buffer;
    }


    private static void paramToUpload(OutputStream baos, IMParameters params){
        String key = "";
        for (int loc = 0; loc < params.size(); loc++) {
            try {
                //key = URLEncoder.encode(params.getKey(loc), "UTF-8");
                key = params.getKey(loc);
                StringBuilder temp = new StringBuilder(10);
                temp.setLength(0);
                temp.append(MP_BOUNDARY).append("\r\n");
                temp.append("content-disposition: form-data; name=\"").append(key).append("\"\r\n\r\n");
                Log.d(TAG,"paramToUpload  name="+key+" value="+params.getValue(key));
                //temp.append(URLEncoder.encode(params.getValue(key), "UTF-8")).append("\r\n");
                temp.append(params.getValue(key)).append("\r\n");
                byte[] res;
                res = temp.toString().getBytes();
                baos.write(res);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Read http requests result from response .
     *
     * @param response
     *            : http response by executing httpclient
     *
     * @return String : http response content
     */
    public static String read(HttpResponse response){
        String result = "";
        HttpEntity entity = response.getEntity();
        InputStream inputStream;
        try {
            inputStream = entity.getContent();
            ByteArrayOutputStream content = new ByteArrayOutputStream();

            Header header = response.getFirstHeader("Content-Encoding");
            if (header != null && header.getValue().toLowerCase().indexOf("gzip") > -1) {
                inputStream = new GZIPInputStream(inputStream);
            }

            // Read response into a buffered stream
            int readBytes = 0;
            byte[] sBuffer = new byte[512];
            while ((readBytes = inputStream.read(sBuffer)) != -1) {
                content.write(sBuffer, 0, readBytes);
            }
            // Return result from buffered stream
            result = new String(content.toByteArray(), "UTF-8");
            return result;
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Read http requests result from inputstream .
     *
     * @param in
     *            : http inputstream from HttpConnection
     *
     * @return String : http response content
     */
    @SuppressWarnings("unused")
    private static String read(InputStream in) throws IOException {
        StringBuilder sb = new StringBuilder();
        BufferedReader r = new BufferedReader(new InputStreamReader(in), 1000);
        for (String line = r.readLine(); line != null; line = r.readLine()) {
            sb.append(line);
        }
        in.close();
        return sb.toString();
    }

    /**
     * Clear current context cookies .
     *
     * @param context
     *            : current activity context.
     *
     * @return void
     */
    public static void clearCookies(Context context) {
        @SuppressWarnings("unused")
        CookieSyncManager cookieSyncMngr = CookieSyncManager.createInstance(context);
        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.removeAllCookie();
    }

    /**
     * Display a simple alert dialog with the given text and title.
     *
     * @param context
     *            Android context in which the dialog should be displayed
     * @param title
     *            Alert dialog title
     * @param text
     *            Alert dialog message
     */
    public static void showAlert(Context context, String title, String text) {
        Builder alertBuilder = new Builder(context);
        alertBuilder.setTitle(title);
        alertBuilder.setMessage(text);
        alertBuilder.create().show();
    }

    public static String encodeParameters(IMParameters httpParams) {
        if (null == httpParams || Utility.isBundleEmpty(httpParams)) {
            return "";
        }
        StringBuilder buf = new StringBuilder();
        int j = 0;
        for (int loc = 0; loc < httpParams.size(); loc++) {
            String key = httpParams.getKey(loc);
            if (j != 0) {
                buf.append("&");
            }
            try {
                String v= httpParams.getValue(key);
                Log.d(TAG,"encodeParameters httpParams  Key=="+key+",getValue=="+v);
                if(v!=null) {
                    buf.append(URLEncoder.encode(key, "UTF-8")).append("=")
                            .append(URLEncoder.encode(v, "UTF-8"));
                }
            } catch (java.io.UnsupportedEncodingException neverHappen) {
                neverHappen.printStackTrace();
            }
            j++;
        }
        return buf.toString();

    }

    public static char[] base64Encode(byte[] data) {
        final char[] alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/="
                .toCharArray();
        char[] out = new char[((data.length + 2) / 3) * 4];
        for (int i = 0, index = 0; i < data.length; i += 3, index += 4) {
            boolean quad = false;
            boolean trip = false;
            int val = (0xFF & (int) data[i]);
            val <<= 8;
            if ((i + 1) < data.length) {
                val |= (0xFF & (int) data[i + 1]);
                trip = true;
            }
            val <<= 8;
            if ((i + 2) < data.length) {
                val |= (0xFF & (int) data[i + 2]);
                quad = true;
            }
            out[index + 3] = alphabet[(quad ? (val & 0x3F) : 64)];
            val >>= 6;
            out[index + 2] = alphabet[(trip ? (val & 0x3F) : 64)];
            val >>= 6;
            out[index + 1] = alphabet[val & 0x3F];
            val >>= 6;
            out[index + 0] = alphabet[val & 0x3F];
        }
        return out;
    }

    /**
     * //获取完整的域名
     *
     * @param text 获取浏览器分享出来的text文本
     */
    public static String getCompleteUrl(String text) {
        Pattern p = Pattern.compile("((http|ftp|https)://)(([a-zA-Z0-9\\._-]+\\.[a-zA-Z]{2,6})|([0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}))(:[0-9]{1,4})*(/[a-zA-Z0-9\\&%_\\./-~-]*)?", Pattern.CASE_INSENSITIVE);
        Matcher matcher = p.matcher(text);
        matcher.find();
        return matcher.group();
    }
}
