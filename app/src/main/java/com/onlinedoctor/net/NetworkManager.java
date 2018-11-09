package com.onlinedoctor.net;

import com.onlinedoctor.application.MyApplication;
import com.onlinedoctor.log.Logger;
import com.onlinedoctor.net.message.GetVoiceMessage;
import com.onlinedoctor.net.message.ImageMessage;
import com.onlinedoctor.net.message.SimpleMessage;
import com.onlinedoctor.net.message.TextHttpsMessage;
import com.onlinedoctor.net.message.TextMessage;
import com.onlinedoctor.net.message.UploadFileToQiniuMessage;
import com.onlinedoctor.net.message.VoiceMessage;

import java.io.File;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import com.onlinedoctor.util.JsonUtil;
import com.qiniu.android.http.ResponseInfo;
import  com.qiniu.android.storage.UploadManager;
import com.qiniu.android.storage.UpCompletionHandler;

public class NetworkManager {

    private static NetworkManager instance;
    private ThreadPoolExecutor threadPoolExecutor;
    private UploadManager uploadManager;

    private final String QINIU_BASEURL = "";

    private NetworkManager() {
        threadPoolExecutor = SingtonThreadPoolExecutor.getThreadPool();
        uploadManager = new UploadManager();
    }

    public static NetworkManager getNetworkManager() {
        if (instance == null) {
            synchronized (NetworkManager.class) {
                if (instance == null) {
                    instance = new NetworkManager();
                }
            }
        }
        return instance;
    }

    public void send(SimpleMessage message) throws ClassNotFoundException {
        String name = message.getClass().getName();
        if (name.equals("com.onlinedoctor.net.message.TextMessage")) {
            doPostText((TextMessage) message);
        } else if(name.equals("com.onlinedoctor.net.message.TextHttpsMessage")){
            doHttpsPostText((TextHttpsMessage) message);
        } else if (name.equals("com.onlinedoctor.net.message.ImageMessage")) {
            doPostImage((ImageMessage) message);
        } else if (name.equals("com.onlinedoctor.net.message.VoiceMessage")) {
            doPostVoice((VoiceMessage) message);
        } else if (name.equals("com.onlinedoctor.net.message.GetVoiceMessage")) {
            doGetVoice((GetVoiceMessage) message);
        } else if(name.equals("com.onlinedoctor.net.message.UploadFileToQiniuMessage")){
            doUploadFileToQiniu((UploadFileToQiniuMessage)message);
        } else {
            throw new ClassNotFoundException();
        }
    }

    private void doGetVoice(final GetVoiceMessage message) {
        String tmpUrl = message.url;
        final List<NameValuePair> params = message.params;
        int len = params.size();
        if (len > 0) {
            tmpUrl += "?";
        }
        for (int i = 0; i < len; i++) {
            tmpUrl += params.get(i).getName() + "=" + params.get(i).getValue() + "&";
        }

        final String url = tmpUrl.substring(0, tmpUrl.length() - 1);

        Thread thread = new Thread(new Runnable() {

            public void run() {
                HttpGet httpGet = new HttpGet(url);
                String errorMessage;
                byte[] result = null;
                HttpClient httpClient = HttpClientUtil.getHttpClient();
                try {
                    HttpResponse httpResponse = httpClient.execute(httpGet);

                    int i = httpResponse.getStatusLine().getStatusCode();

                    if (i == 200) {
                        result = EntityUtils.toByteArray(httpResponse.getEntity());
                        message.onSuccess(result);
                    } else {
                        errorMessage = httpResponse.getStatusLine().toString();
                        message.onFailed(i, errorMessage);
                    }
                } catch (Exception e) {
                    handleException(e, message.onFailed);
                }
            }

        });

        threadPoolExecutor.execute(thread);
    }

    private void doPostText(final TextMessage message) {

        final String url = message.url;
        final List<NameValuePair> params = message.params;
        Logger.i("doPostText", "url: " + url + "params: " + params.toString());

        Thread thread = new Thread(new Runnable() {

            public void run() {
                HttpPost httpRequest = new HttpPost(url);
                String errorMessage = "doPostError";
                String result = null;
                HttpClient httpClient = HttpClientUtil.getHttpClient();
                try {
                    if (params != null) {
                        httpRequest.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));
                    }
                    HttpResponse httpResponse = httpClient.execute(httpRequest);

                    int i = httpResponse.getStatusLine().getStatusCode();

                    if (i == 200) {
                        result = EntityUtils.toString(httpResponse.getEntity());
                        message.onSuccess(result);
                    } else {
                        errorMessage = httpResponse.getStatusLine().toString();
                        message.onFailed(i, errorMessage);
                    }
                } catch (Exception e) {
                    handleException(e, message.onFailed);
                }
            }
        });

        threadPoolExecutor.execute(thread);
    }

    private void doHttpsPostText(final TextHttpsMessage message) {

        final String url = message.url;
        final List<NameValuePair> params = message.params;

        Thread thread = new Thread(new Runnable() {

            public void run() {
                HttpPost httpRequest = new HttpPost(url);
                String errorMessage = "doPostError";
                String result = null;
                HttpClient httpClient = HttpClientUtil.getHttpsClient(MyApplication.context);
                try {
                    if (params != null) {
                        httpRequest.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));
                    }
                    HttpResponse httpResponse = httpClient.execute(httpRequest);

                    int i = httpResponse.getStatusLine().getStatusCode();

                    if (i == 200) {
                        result = EntityUtils.toString(httpResponse.getEntity());
                        message.onSuccess(result);
                    } else {
                        errorMessage = httpResponse.getStatusLine().toString();
                        message.onFailed(i, errorMessage);
                    }
                } catch (Exception e) {
                    handleException(e, message.onFailed);
                }
            }
        });

        threadPoolExecutor.execute(thread);
    }

    private void doPostImage(final ImageMessage message) {
        Thread thread = new Thread(new Runnable() {

            public void run() {
                HttpPost httpRequest = new HttpPost(message.url);
                String errorMessage = "doPostError";
                String result = null;
                HttpClient httpClient = HttpClientUtil.getHttpClient();
                try {
                    Logger.i("Image Upload size = ", message.imageByte.length + "B");
                    httpRequest.setEntity(new ByteArrayEntity(message.imageByte));

                    HttpResponse httpResponse = httpClient.execute(httpRequest);

                    int i = httpResponse.getStatusLine().getStatusCode();

                    if (i == 200) {
                        result = EntityUtils.toString(httpResponse.getEntity());
                        message.onSuccess(result);
                    } else {
                        errorMessage = httpResponse.getStatusLine().toString();
                        message.onFailed(i, errorMessage);
                    }
                } catch (Exception e) {
                    handleException(e, message.onFailed);
                }
            }
        });

        threadPoolExecutor.execute(thread);
    }

    private void doPostVoice(final VoiceMessage message) {
        Thread thread = new Thread(new Runnable() {

            public void run() {
                HttpPost httpRequest = new HttpPost(message.url);
                String errorMessage = "doPostError";
                String result = null;
                HttpClient httpClient = HttpClientUtil.getHttpClient();
                try {
                    httpRequest.setEntity(new ByteArrayEntity(message.voiceByte));

                    HttpResponse httpResponse = httpClient.execute(httpRequest);

                    int i = httpResponse.getStatusLine().getStatusCode();

                    if (i == 200) {
                        result = EntityUtils.toString(httpResponse.getEntity());
                        message.onSuccess(result);
                    } else {
                        errorMessage = httpResponse.getStatusLine().toString();
                        message.onFailed(i, errorMessage);
                    }
                } catch (Exception e) {
                    handleException(e, message.onFailed);
                }
            }
        });

        threadPoolExecutor.execute(thread);
    }

    private void doUploadFileToQiniu(final UploadFileToQiniuMessage message){
        if(message.fileLists == null && message.bytes == null){
            return;
        }
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                if(message.bytes == null && message.fileLists != null){
                    final List<File> files = new ArrayList<>();
                    files.addAll(message.fileLists);
                    final List<String> qiniuKeys = new ArrayList<>();
                    for(final File file: message.fileLists){
                        uploadManager.put(file, null, message.token, new UpCompletionHandler() {
                            public void complete(String k, ResponseInfo rinfo, JSONObject response) {
                                Logger.i("doUploadFileToQiniu", "<qiniu upload callback>"+ k + response);
                                if(rinfo.isOK()){
                                    try{
                                        qiniuKeys.add(response.getString("key"));
                                    }catch (JSONException e){
                                        e.printStackTrace();
                                    }
                                    files.remove(file);
                                    if(files.size() <= 0){
                                        Logger.i("doUploadFileToQiniu", "<qiniu upload filelists>" + qiniuKeys.toString());
                                        message.onSuccess(JsonUtil.objectToJson(qiniuKeys));
                                    }
                                }else{
                                    // 上传失败
                                    // todo
                                    Logger.i("doUploadToQiniuFailed", rinfo.error);
                                    message.onFailed(-1, rinfo.error);
                                }
                            }
                        }, null);
                    }
                } else if(message.fileLists == null && message.bytes != null){
                    final List<String> qiniuKeys = new ArrayList<>();
                    uploadManager.put(message.bytes, null,message.token, new UpCompletionHandler(){
                        @Override
                        public void complete(String key, ResponseInfo info, JSONObject response) {
                            Logger.i("doUploadFileToQiniu", "<qiniu upload callback>"+ key + response);
                            if(info.isOK()){
                                try{
                                    qiniuKeys.add(response.getString("key"));
                                    message.onSuccess(JsonUtil.objectToJson(qiniuKeys));
                                }catch (JSONException e){
                                    e.printStackTrace();
                                }
                            }else{
                                // 上传失败
                                // todo
                                Logger.i("doUploadToQiniuFailed", info.error);
                                message.onFailed(-1, info.error);
                            }
                        }
                    }, null);
                }
            }
        });
        threadPoolExecutor.execute(thread);
    }


    // todo
    // 后面应该细化Exception的处理
    private void handleException(Exception e, SimpleMessage.HttpFailedCallBack onFail){
        String errorMessage = "doPostError";
        if(e instanceof cz.msebera.android.httpclient.client.ClientProtocolException){
            errorMessage = e.getMessage() == null ? e.getClass().toString(): e.getMessage().toString();
            onFail.handle(-1, errorMessage);
        }else if(e instanceof SocketTimeoutException){
            errorMessage = e.getMessage() == null ? e.getClass().toString(): e.getMessage().toString();
            onFail.handle(-1, errorMessage);
        }else if(e instanceof IOException){
            errorMessage = e.getMessage() == null ? e.getClass().toString(): e.getMessage().toString();
            onFail.handle(-1, errorMessage);
        }else{
            errorMessage = e.getMessage() == null ? e.getClass().toString(): e.getMessage().toString();
            onFail.handle(-1, errorMessage);
        }
        e.printStackTrace();
    }
}
