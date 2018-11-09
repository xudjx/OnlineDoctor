package com.onlinedoctor.pojo.mine;

/**
 * Created by wds on 15/10/4.
 */
public class MyWallet {
    private int id = 1;
    private float total;
    private float available;
    private float unavailable;

    public MyWallet(float total, float available, float unavailable) {
        this.total = total;
        this.available = available;
        this.unavailable = unavailable;
    }

    public MyWallet() {
    }

    public float getTotal() {
        return total;
    }

    public void setTotal(float total) {
        this.total = total;
    }

    public float getAvailable() {
        return available;
    }

    public void setAvailable(float available) {
        this.available = available;
    }

    public float getUnavailable() {
        return unavailable;
    }

    public void setUnavailable(float unavailable) {
        this.unavailable = unavailable;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
