package com.onlinedoctor.pojo.mine;

/**
 * Created by wds on 15/8/20.
 */


public class PackDoctorInfo {
    private String sid;
    private String name;
    private String gender;
    private String cid;
    private long registerTime;
    private long lastTime;
    private String selfInfo;
    private String thumbnail;
    private String avatar;
    private String clinic; //医院
    private String rank;//级别
    private String room;//科室
    private String email;
    private long birthday;
    private String qrcode;
    private int authStatus;
    private String authPhoto;


    public String getEmail() {
        return email;
    }

    public long getBirthday() {
        return birthday;
    }

    public String getSid() {
        return sid;
    }

    public void setSid(String sid) {
        this.sid = sid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getCid() {
        return cid;
    }

    public void setCid(String cid) {
        this.cid = cid;
    }

    public long getRegisterTime() {
        return registerTime;
    }

    public void setRegisterTime(long registerTime) {
        this.registerTime = registerTime;
    }

    public long getLastTime() {
        return lastTime;
    }

    public void setLastTime(long lastTime) {
        this.lastTime = lastTime;
    }

    public String getSelfInfo() {
        return selfInfo;
    }

    public void setSelfInfo(String selfInfo) {
        this.selfInfo = selfInfo;
    }

    public String getClinic() {
        return clinic;
    }

    public String getRank() {
        return rank;
    }

    public String getRoom() {
        return room;
    }

    public void setClinic(String clinic) {
        this.clinic = clinic;
    }

    public void setRank(String rank) {
        this.rank = rank;
    }

    public void setRoom(String room) {
        this.room = room;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setBirthday(long birthday) {
        this.birthday = birthday;
    }

    public String getQrcode() {
        return qrcode;
    }

    public void setQrcode(String qrcode) {
        this.qrcode = qrcode;
    }

    public int getAuthStatus() {
        return authStatus;
    }

    public String getAuthPhoto() {
        return authPhoto;
    }

    public void setAuthStatus(int authStatus) {
        this.authStatus = authStatus;
    }

    public void setAuthPhoto(String authPhoto) {
        this.authPhoto = authPhoto;
    }

    public PackDoctorInfo() {

    }
}
