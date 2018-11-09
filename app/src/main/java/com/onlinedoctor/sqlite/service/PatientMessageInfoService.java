package com.onlinedoctor.sqlite.service;

import java.util.List;

import com.onlinedoctor.pojo.patient.PatientMessage;

public interface PatientMessageInfoService {

	// 暂时就是增删改查四大操作
	public boolean addPatientMessageInfo(PatientMessage message);

	public boolean addPatientMessageInfo(List<PatientMessage> messages);
	
	public boolean updatePatientMessageInfo(PatientMessage message);
	
	public boolean updatePatientMessageInfo(List<PatientMessage> messages);

	public boolean deletePatientMessageInfo(String guid);

	// 根据一定的条件找出所有的信息
	//public List<Map<String, String>> listPatientMessageInfoBy(String[] selectionArgs);
	
	public List<PatientMessage> listLastKPatientMessage(String userId, int k);
	
	public List<PatientMessage> linkedListLastKPatientMessage(String userId, int k);
	
	//public List<PatientMessage> listLastKPatientMessageBeforeTime(String userId, int k, long timestamp);
	public List<PatientMessage> listLastKPatientMessageBeforeId(String userId, int k, String guid);
	
	// 查看某条信息
	public PatientMessage viewPatientMessageInfo(String guid);

	// 彻底删除消息
	//public boolean deleteAllPatientMessage();

	public List<PatientMessage> listPaymentUnsuccess();
}
