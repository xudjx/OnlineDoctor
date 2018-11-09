package com.onlinedoctor.net;

import com.onlinedoctor.log.Logger;
import com.qiniu.android.utils.UrlSafeBase64;

import java.security.MessageDigest;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 * Created by xuweidong on 15/11/19.
 */
public class QiniuUtil {

    // 私密文件的加密key
    public static final String SecretKey = "bNKCI60eeOJSeca8";

    public static final int Type_Image = 1;
    public static final int Type_Voice = 2;

    // 七牛云空间域名
    public static final String ImageDomain = "http://7xoeiw.com1.z0.glb.clouddn.com/";
    public static final String VoiceDomain = "http://7xoeiy.com1.z0.glb.clouddn.com/";

    // 七牛图片预处理mode
    public static final String ThumbnailMode200x200 = "imageView2/1/w/200/h/200"; // 等比缩放，200*200
    public static final String ThumbnailModeAdaptive = "/0/w/300/h/150"; // 自适应，长边最多300，短边最多150

    /**
     *  生成普通的七牛云文件下载url
     */
    public static String getDownloadUrl(String key, int type){
        String downloadUrl = "";
        switch (type){
            case Type_Image:
                downloadUrl =  ImageDomain + key;
                break;
            case Type_Voice:
                downloadUrl =  VoiceDomain + key;
                break;
        }
        return  downloadUrl;
    }

    /**
     *  生成图片缩略图url
     */
    public static String getThumbnailUrl(String key, int type, String mode){
        String downloadUrl = "";
        switch (type){
            case Type_Image:
                downloadUrl =  ImageDomain + key + "?" + mode;
                break;
            case Type_Voice:
                downloadUrl =  VoiceDomain + key + "?" + mode;
                break;
        }
        return  downloadUrl;
    }

    /**
     * 对指定的图片生成文字水印
     */
    public static String generateWordMark(String imageUrl, String words){
        if(words == null){
            words = "医行者 处方图";
        }
        String font = "/font/"+Base64Encoding("微软雅黑");
        String fontColor = "/fill/" + Base64Encoding("#23a9ff"); // blue
        String fontSize = "/fontsize/" + 400;
        String text = "/text/" + Base64Encoding(words);
        StringBuilder builder = new StringBuilder();
        if(imageUrl.contains("imageView2/1")){
            builder.append(imageUrl);
            builder.append("|");
        }else{
            builder.append(imageUrl);
            builder.append("?");
        }
        builder.append("watermark/2");
        builder.append(text);
        builder.append(font);
        builder.append(fontColor);
        builder.append(fontSize);
        return builder.toString();
    }

    /** 生成七牛云私密空间的文件url
     * 1、构造url
     * 2、为下载url加上过期时间: DownloadUrl = 'http://developer.qiniu.com/resource/flower.jpg?e=1451491200'
     * 3、对url生成HmacSHA1签名，并做UrlSafeBase64编码
     * 4、将key和上述加密数据拼接 Token = 'MY_ACCESS_KEY:yN9WtB0lQheegAwva64yBuH3ZgU='
     * 5、拼接url： RealDownloadUrl = 'http://developer.qiniu.com/resource/flower.jpg?e=1451491200&token=MY_ACCESS_KEY:yN9WtB0lQheegAwva64yBuH3ZgU='
     */
    public static String getSafeDownloadUrl(String url){
        Logger.i("Url", url);
        String safeDownloadUrl = url;
        try{
            String sign = getSignature(safeDownloadUrl.getBytes("UTF-8"), SecretKey.getBytes("UTF-8"));
            String encodeSign = Base64Encoding(sign);
            String token = SecretKey+":"+encodeSign;
            safeDownloadUrl = safeDownloadUrl+"?token="+token;
            Logger.i("safeDownloadUrl",safeDownloadUrl);
        }catch (Exception e){
            e.printStackTrace();
        }
        return safeDownloadUrl;
    }

    public static String Base64Encoding(String data){
        return UrlSafeBase64.encodeToString(data);
    }

    /**
     * 生成签名数据
     *
     * @param data 待加密的数据
     * @param key  加密使用的key
     * @return 生成MD5编码的字符串
     */
    public static String getSignature(byte[] data, byte[] key) {
        try{
            SecretKeySpec signingKey = new SecretKeySpec(key, "HmacSHA1");
            Mac mac = Mac.getInstance("HmacSHA1");
            mac.init(signingKey);
            byte[] rawHmac = mac.doFinal(data);
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(rawHmac);
            String result = new String(md.digest(),"UTF-8");
            return result;
        }catch (Exception e){
            e.printStackTrace();
        }
        return "";
    }
}
