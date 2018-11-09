package com.onlinedoctor.net.message;

import java.io.File;
import java.util.List;

/**
 * Created by xuweidong on 15/11/18.
 * 上传文件到七牛云，传入文件list
 */
public class UploadFileToQiniuMessage extends SimpleMessage{

    public List<File> fileLists;
    public byte[] bytes;
    public String token;

    public UploadFileToQiniuMessage(String url, HttpCallBack onSuccess, HttpFailedCallBack onFailed, List<File> fileLists, byte[] bytes, String token) {
        super(url, onSuccess, onFailed);
        this.fileLists = fileLists;
        this.bytes = bytes;
        this.token = token;
    }
}
