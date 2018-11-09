package com.onlinedoctor.sqlite.service;

import java.util.ArrayList;
import java.util.List;

import com.onlinedoctor.pojo.patient.NewPatient;

public interface NewPatientInfoService {

	public boolean addNewPatientInfo(ArrayList<NewPatient> npList);
	
	public boolean addNewPatientInfoOne(NewPatient np);
	
	public boolean updateStatusNewPatientInfoOne(String patientId, int status);
	
	public NewPatient viewNewPatientInfoById(String patientId);
	
	public List<NewPatient> listAll();

	public boolean allCheck();

	public int getUnchechedCount();
}
