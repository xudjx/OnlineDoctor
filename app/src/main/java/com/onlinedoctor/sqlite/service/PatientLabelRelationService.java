package com.onlinedoctor.sqlite.service;

import java.util.HashSet;
import java.util.List;

import com.onlinedoctor.pojo.patient.PatientLabelRel;

public interface PatientLabelRelationService {
	
	public boolean addRelationInfoOne(PatientLabelRel plr);
	
	public boolean addRelationInfoOne(String patientId, long labelId);

	public List<String> listPatientByLabelId(Long labelId);
	
	public List<Long> listLabelByPatientId(String patientId);
	
	public HashSet<Long> getLabelByPatientId(String patientId);
	
	public boolean deleteRelationInfoOne(String patientId, long labelId);
	
	public boolean deleteRelationInfoByLabelId(long labelId);

	public boolean deleteRelationIfoByPatientId(String patientId);
}
