package com.onlinedoctor.pojo.mine;

import com.onlinedoctor.pojo.mine.OrderDetailItem;
import com.onlinedoctor.pojo.mine.OrderItemConvertable;

import java.math.BigDecimal;

public class WithdrawHistory implements OrderItemConvertable {
	private long id;
	private String doctorId;
	private String type;
	private String typeDetail;
	private BigDecimal amount;
	private int status;
	private String failReason;
	private String ip;
	private long time;
	
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public String getDoctorId() {
		return doctorId;
	}
	public void setDoctorId(String doctorId) {
		this.doctorId = doctorId;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getTypeDetail() {
		return typeDetail;
	}
	public void setTypeDetail(String typeDetail) {
		this.typeDetail = typeDetail;
	}
	public BigDecimal getAmount() {
		return amount;
	}
	public void setAmount(BigDecimal amount) {
		this.amount = amount;
	}
	public int getStatus() {
		return status;
	}
	public void setStatus(int status) {
		this.status = status;
	}
	public String getFailReason() {
		return failReason;
	}
	public void setFailReason(String failReason) {
		this.failReason = failReason;
	}
	public String getIp() {
		return ip;
	}
	public void setIp(String ip) {
		this.ip = ip;
	}
	public long getTime() {
		return time;
	}
	public void setTime(long time) {
		this.time = time;
	}
	public OrderDetailItem toOrderDetailItem() {
		OrderDetailItem item = new OrderDetailItem();
		item.setFee("-" + amount.divide(new BigDecimal(100)).setScale(2)); //服务器返回回来的单位是整型, 单位位分
		item.setTime(time);
		if(type.equals("alipay")) {
			item.setChargingTitle("支付宝提现("+typeDetail+")");
		}else if(type.equals("bankcard")) {
			item.setChargingTitle("银行卡提现("+typeDetail+")");
		}else if(type.equals("cellphone")) {
			item.setChargingTitle("手机充值("+typeDetail+")");
		}else {
			item.equals("未知提现项目");
		}
		switch(status) {
		case 0:item.setStatus("提现成功");break;
		case 1:item.setStatus("审核中");break;
		case 2:item.setStatus("提现失败,原因:"+failReason);
		default: item.setStatus("未知状态");break;
		}
		return item;
		
	}
}
