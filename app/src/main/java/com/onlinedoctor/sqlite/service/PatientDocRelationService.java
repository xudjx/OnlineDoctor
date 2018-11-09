package com.onlinedoctor.sqlite.service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.onlinedoctor.pojo.patient.PatientDoctorRel;

public interface PatientDocRelationService {

	// CRUD
	public boolean addRelationInfo(Object[] params);
	
	public boolean addRelationInfoOne(PatientDoctorRel pdr);

	public boolean updateRelationInfo(Object[] params);
	
	public boolean updateStatus(String doctorId, String patientId, int status);

	// 根据一定的条件找出所有的信息
	public List<Map<String, Object>> listRelationInfoByPatientId(String patientId);
	public List<PatientDoctorRel> listRelationInfoByDoctorId(String doctorId);
	public ConcurrentHashMap<String, Boolean> getPatientIdMapByDoctroId(String doctorId);

	// 查看某条信息
	public Map<String, String> viewRelationInfo(String[] selectionArgs);
	
	public PatientDoctorRel viewRelationInfo(String doctorId, String patientId);

	// 彻底删除消息
	public boolean deleteAllRelation();


}
