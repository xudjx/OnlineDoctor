package com.onlinedoctor.pojo.tools.prescription;

import com.onlinedoctor.log.Logger;
import com.onlinedoctor.pojo.Common;
import com.onlinedoctor.util.JsonUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by xuweidong on 15/11/1.
 * 处方描述实体类
 */
public class PrescriptionDO implements Serializable{

    private int id; // 本地表ID，自增
    private int global_id; // 全局ID，从服务器获取,也是处方的唯一ID
    private String name; // 保留字段，对应微信下单收费的name
    private String detail; // 保留字段，对应微信下单收费的detail
    private int price;
    private String doctor_id;
    private long create_time;
    private int is_deleted;
    private int type; // type 医生端发送 or 患者端
    private String remark; // 处方 医嘱信息
    private String prescription_imgs; // JsonArray字符串
    private String patient_id; // 患者ID

    public PrescriptionDO() {
        this.name = "";
        this.detail = "";
        this.doctor_id = "";
        this.remark = "";
        this.prescription_imgs = "";
        this.patient_id = "";
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDetail() {
        return detail;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public String getDoctor_id() {
        return doctor_id;
    }

    public void setDoctor_id(String doctor_id) {
        this.doctor_id = doctor_id;
    }

    public long getCreate_time() {
        return create_time;
    }

    public void setCreate_time(long create_time) {
        this.create_time = create_time;
    }

    public int is_deleted() {
        return is_deleted;
    }

    public void setIs_deleted(int is_deleted) {
        this.is_deleted = is_deleted;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getPrescription_imgs() {
        return prescription_imgs;
    }

    public void setPrescription_imgs(String prescription_imgs) {
        this.prescription_imgs = prescription_imgs;
    }

    public int getGlobal_id() {
        return global_id;
    }

    public void setGlobal_id(int global_id) {
        this.global_id = global_id;
    }

    public String getPatient_id() {
        return patient_id;
    }

    public void setPatient_id(String patient_id) {
        this.patient_id = patient_id;
    }

    /* 解析处方图片
             * FLag = 0, 小图
             * Flag = 1, 大图
             */
    public List<String> getImages(int flag){
        List<String> images = new ArrayList<>();
        try{
            JSONArray jsonArray = new JSONArray(prescription_imgs);
            int size = jsonArray.length();
            if(size <= 0){
                return null;
            }
            if(flag == 0){
               for(int i = 0; i < size; i++){
                   JSONObject jsonObject = jsonArray.getJSONObject(i);
                   images.add(jsonObject.getString(Common.KEY_IMAGE_thumbnailPath));
               }
            }else if(flag == 1){
                for(int i = 0; i < size; i++){
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    images.add(jsonObject.getString(Common.KEY_IMAGE_avaterPath));
                }
            }
        }catch (JSONException e){
            e.printStackTrace();
        }
        Logger.i("Prescription","image size:"+images.size() + images.toString());
        return images;
    }

    public String getFirstImage(){
        List<String> images = getImages(0);
        if(images != null && images.size() >0 ){
            Logger.i("Prescription first image",images.get(0));
            return images.get(0);
        }
        return null;
    }

    @Override
    public String toString() {
        return JsonUtil.objectToJson(this);
    }
}
