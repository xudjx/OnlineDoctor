package com.onlinedoctor.pojo.mine;

public class DoctorInfo implements Cloneable{
    private int id = 1;
    private String thumbnail = "";
    private String avatar = "";
    private String name = "";
    private String cid = "";
    private int auth_status = -1;
    private String auth_photo = "";
    private String gender = "";
    private String email = "";
    private Long birth = -1L;
    private String addr = "";
    private String clinic = "";
    private String room = "";
    private String rank = "";
    private String intro = "";
    private long time;
    private String qrcode = "";

    public DoctorInfo() {
    }

    public DoctorInfo(DoctorInfo dInfo) {
        thumbnail = dInfo.getThumbnail();
        avatar = dInfo.getAvatar();
        name = dInfo.getName();
        cid = dInfo.getCid();
        gender = dInfo.getGender();
        email = dInfo.getEmail();
        birth = dInfo.getBirth();
        addr = dInfo.getAddr();
        clinic = dInfo.getClinic();
        room = dInfo.getRoom();
        rank = dInfo.getRank();
        intro = dInfo.getIntro();
        time = dInfo.getTime();
        qrcode = dInfo.getQrcode();
        auth_photo = dInfo.getAuth_photo();
        auth_status = dInfo.getAuth_status();
    }

    public int getId() {
        return id;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public String getAvatar() {
        return avatar;
    }

    public String getCid() {
        return cid;
    }

    public String getGender() {
        return gender;
    }

    public Long getBirth() {
        return birth;
    }

    public String getAddr() {
        return addr;
    }

    public String getClinic() {
        return clinic;
    }

    public String getRoom() {
        return room;
    }

    public String getRank() {
        return rank;
    }

    public String getEmail() {
        return email;
    }

    public String getQrcode() {
        return qrcode;
    }

    public String getIntro() {
        return intro;
    }

    public String getName() {
        return name;
    }

    public long getTime() {
        return time;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setThumbnail(String thumbnail) {
        if (thumbnail != null)
            this.thumbnail = thumbnail;
    }

    public void setAvatar(String avatar) {
        if (avatar != null)
            this.avatar = avatar;
    }

    public void setCid(String cid) {
        if (cid != null)
            this.cid = cid;
    }

    public void setGender(String gender) {
        if (gender != null)
            this.gender = gender;
    }

    public void setBirth(Long birth) {
        if (birth != null)
            this.birth = birth;
    }

    public void setAddr(String addr) {
        if (addr != null)
            this.addr = addr;
    }

    public void setClinic(String clinic) {
        if (clinic != null)
            this.clinic = clinic;
    }

    public void setRoom(String room) {
        if (room != null)
            this.room = room;
    }

    public void setRank(String rank) {
        if (rank != null)
            this.rank = rank;
    }

    public void setEmail(String email) {
        if (email != null)
            this.email = email;
    }

    public void setIntro(String intro) {
        if (intro != null)
            this.intro = intro;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public void setName(String name) {
        if (name != null)
            this.name = name;
    }

    public void setQrcode(String qrcode) {
        if (qrcode != null)
            this.qrcode = qrcode;
    }

    public int getAuth_status() {
        return auth_status;
    }

    public String getAuth_photo() {
        return auth_photo;
    }

    public void setAuth_status(int auth_status) {
        this.auth_status = auth_status;
    }

    public void setAuth_photo(String auth_photo) {
        if (auth_photo != null)
            this.auth_photo = auth_photo;
    }

    public boolean equals(DoctorInfo doctorInfo) {
        return thumbnail.equals(doctorInfo.getThumbnail()) &&
                avatar.equals(doctorInfo.getAvatar()) &&
                name.equals(doctorInfo.getName()) &&
                cid.equals(doctorInfo.getCid()) &&
                gender.equals(doctorInfo.getGender()) &&
                email.equals(doctorInfo.getEmail()) &&
                birth.equals(doctorInfo.getBirth()) &&
                addr.equals(doctorInfo.getAddr()) &&
                clinic.equals(doctorInfo.getClinic()) &&
                room.equals(doctorInfo.getRoom()) &&
                rank.equals(doctorInfo.getRank()) &&
                intro.equals(doctorInfo.getIntro()) &&
                (time == doctorInfo.getTime()) &&
                qrcode.equals(doctorInfo.getQrcode()) &&
                //auth_photo.equals(doctorInfo.getAuth_photo()) &&
                (auth_status == doctorInfo.getAuth_status());
    }

    @Override
    public String toString() {
        return "DoctorInfo{" +
                "id=" + id +
                ", thumbnail='" + thumbnail + '\'' +
                ", avatar='" + avatar + '\'' +
                ", name='" + name + '\'' +
                ", cid='" + cid + '\'' +
                ", auth_status=" + auth_status +
                ", auth_photo='" + auth_photo + '\'' +
                ", gender='" + gender + '\'' +
                ", email='" + email + '\'' +
                ", birth=" + birth +
                ", addr='" + addr + '\'' +
                ", clinic='" + clinic + '\'' +
                ", room='" + room + '\'' +
                ", rank='" + rank + '\'' +
                ", intro='" + intro + '\'' +
                ", time=" + time +
                ", qrcode='" + qrcode + '\'' +
                '}';
    }

    @Override
    public Object clone() {
        DoctorInfo doInfo = null;
        try {
            doInfo = (DoctorInfo) super.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return doInfo;
    }
}

