package com.onlinedoctor.pojo.mine;

import java.math.BigDecimal;

	public class Order implements OrderItemConvertable
	{
		private String id;
		private String chargingTitle;
		private String chargingDetail;
		private String doctorId;
		private String patientId;
		private String customerIp;
		private String attach;
		private String feeType;
		private BigDecimal totalPrice;
		private long timeStart;
		private long timeExpire;
		private String goodsTag;
		private BigDecimal transportFee;
		private BigDecimal productFee;
		private int status;
		
		public String getId() {
			return id;
		}
		public void setId(String id) {
			this.id = id;
		}
		public String getChargingTitle() {
			return chargingTitle;
		}
		public void setChargingTitle(String chargingTitle) {
			this.chargingTitle = chargingTitle;
		}
		public String getChargingDetail() {
			return chargingDetail;
		}
		public void setChargingDetail(String chargingDetail) {
			this.chargingDetail = chargingDetail;
		}
		public String getAttach() {
			return attach;
		}
		public void setAttach(String attach) {
			this.attach = attach;
		}
		public String getFeeType() {
			return feeType;
		}
		public void setFeeType(String feeType) {
			this.feeType = feeType;
		}
		public BigDecimal getTotalPrice() {
			return totalPrice;
		}
		public void setTotalPrice(BigDecimal totalPrice) {
			this.totalPrice = totalPrice;
		}
		public String getCustomerIp() {
			return customerIp;
		}
		public void setCustomerIp(String customerIp) {
			this.customerIp = customerIp;
		}
		public long getTimeStart() {
			return timeStart;
		}
		public void setTimeStart(long timeStart) {
			this.timeStart = timeStart;
		}
		public long getTimeExpire() {
			return timeExpire;
		}
		public void setTimeExpire(long timeExpire) {
			this.timeExpire = timeExpire;
		}
		public String getGoodsTag() {
			return goodsTag;
		}
		public void setGoodsTag(String goodsTag) {
			this.goodsTag = goodsTag;
		}
		public BigDecimal getTransportFee() {
			return transportFee;
		}
		public void setTransportFee(BigDecimal transportFee) {
			this.transportFee = transportFee;
		}
		public BigDecimal getProductFee() {
			return productFee;
		}
		public void setProductFee(BigDecimal productFee) {
			this.productFee = productFee;
		}
		
		public String getDoctorId() {
			return doctorId;
		}
		public void setDoctorId(String doctorId) {
			this.doctorId = doctorId;
		}
		public String getPatientId() {
			return patientId;
		}
		public void setPatientId(String patientId) {
			this.patientId = patientId;
		}
		public int getStatus() {
			return status;
		}
		public void setStatus(int status) {
			this.status = status;
		}
		public OrderDetailItem toOrderDetailItem() {
			OrderDetailItem item = new OrderDetailItem();

			item.setFee("+" + totalPrice.divide(new BigDecimal(100)).setScale(2)); //服务器返回回来的单位是整型, 单位位分
			item.setTime(timeStart);
			item.setChargingTitle(chargingTitle);
			switch(status) {
				case 0:item.setStatus("交易成功"); break;
				case 1:item.setStatus("未付款"); break;
				case 2:item.setStatus("订单过期"); break;
				case 3:item.setStatus("订单取消"); break;
				default: item.setStatus("未知状态"); break;
			}
			return item;
		}
	}

