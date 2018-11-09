package com.onlinedoctor.pojo.message_content;

import com.onlinedoctor.pojo.Common;
import com.onlinedoctor.util.JsonUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by xuweidong on 15/9/20.
 * contentType = Common.MESSAGE_TYPE_link;
 */
public class LinkPatientMsgDO {
    private String link;
    private String title;
    private String description;
    private String imageUrl;

    public LinkPatientMsgDO(){

    }

    public LinkPatientMsgDO(String link, String title, String description, String imageUrl) {
        this.link = link;
        this.title = title;
        this.description = description;
        this.imageUrl = imageUrl;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    @Override
    public int hashCode() {
        StringBuilder builder = new StringBuilder();
        builder.append(link);
        builder.append(title);
        builder.append(description);
        builder.append(imageUrl);
        return builder.toString().hashCode();
    }

    public String toJson(){
        Map<String, String> map = new HashMap<>();
        map.put("link", this.getLink());
        map.put("description", this.getDescription());
        map.put("title", this.getTitle());
        map.put("imageUrl", this.getImageUrl());
        return JsonUtil.objectToJson(map);
    }
}
