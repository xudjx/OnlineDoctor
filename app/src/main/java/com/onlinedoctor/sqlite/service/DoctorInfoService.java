package com.onlinedoctor.sqlite.service;

import com.onlinedoctor.pojo.mine.DoctorInfo;

public interface DoctorInfoService {

//	// CRUD
//	public boolean addDoctorInfo(Object[] params);
//
//	public boolean updateDoctorInfo(Object[] params);
//
//	public boolean deleteDoctorInfo(Object[] params);
//
//	// 根据一定的条件找出所有的信息
//	public List<Map<String, String>> listDoctorInfoBy(String[] selectionArgs);
//
//	// 查看某条信息
//	public Map<String, String> viewDoctorInfo(String[] selectionArgs);
//
//	// 彻底删除消息
//	public boolean deleteAllDoctor();

	public int insert(DoctorInfo dInfo);
	public boolean update(DoctorInfo dInfo);
	public boolean delete(DoctorInfo dInfo);
	public DoctorInfo get(int id);
	public boolean isEmpty();
}
