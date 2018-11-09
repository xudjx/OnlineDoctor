package com.onlinedoctor.pojo.mine;

import java.math.BigDecimal;

public class OrderDetailItem {
	String chargingTitle;
	String status;
	long time;
	String fee;
	public String getChargingTitle() {
		return chargingTitle;
	}
	public void setChargingTitle(String chargingTitle) {
		this.chargingTitle = chargingTitle;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public long getTime() {
		return time;
	}
	public void setTime(long time) {
		this.time = time;
	}
	public String getFee() {
		return fee;
	}
	public void setFee(String fee) {
		this.fee = fee;
	}
}
