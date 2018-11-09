package com.onlinedoctor.sqlite.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.onlinedoctor.pojo.patient.Patient;

public interface PatientInfoService {

	// CRUD
	public boolean addPatientInfo(ArrayList<Patient> pList);
	
	public boolean addPatientInfoOne(Patient p);

	public boolean updatePatientInfo(Object[] params);

	public boolean updatePatientComment(String patientId, String comment);

	public boolean deletePatientInfo(String patientId);

	// 根据一定的条件找出所有的信息
	public List<Map<String, String>> listPatientInfoBy(String[] selectionArgs);
	
	public List<Patient> listPatientInfoById(String[] patientIds);
	
	//public ConcurrentHashSet<String> getPatientGuidSet();

	// 查看某条信息
	public Map<String, String> viewPatientInfo(String[] selectionArgs);

	public Patient viewPatientInfoByPatientId(String patientId);

	public List<Patient> listAllPatientInfo();

	// 彻底删除消息
	public boolean deleteAllPatient();

}
