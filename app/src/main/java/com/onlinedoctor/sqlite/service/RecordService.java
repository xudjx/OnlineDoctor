package com.onlinedoctor.sqlite.service;

import java.util.List;

import com.onlinedoctor.pojo.patient.Record;

public interface RecordService {

	// CRUD
	public int addRecord(Record record);
	
	public List<Record> listRecords(String patientId, String doctorId);

	public List<Record> listRecordsByType(int recordType);

	public boolean deleteRecord(String patientId);
}
