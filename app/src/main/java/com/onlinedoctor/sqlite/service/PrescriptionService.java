package com.onlinedoctor.sqlite.service;

import com.onlinedoctor.pojo.tools.prescription.PrescriptionDO;

import java.util.List;
import java.util.Map;

public interface PrescriptionService {

	// CRUD
	public boolean addPrescriptionInfo(PrescriptionDO presDo);

	public boolean updatePrescriptionInfo(PrescriptionDO presDo);

	public boolean deletePrescriptionInfo(PrescriptionDO presDo);

	// 根据一定的条件找出所有的信息
	public List<PrescriptionDO> listPrescriptionInfoByPatientId(String patientId);

	// 查看某条信息
	public PrescriptionDO viewPrescriptionByGrobalId(int globalId);

	// 彻底删除消息
	public boolean deleteAllPrescription();
}
