package com.onlinedoctor.sqlite.dao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

import com.onlinedoctor.pojo.Common;
import com.onlinedoctor.pojo.patient.PatientDoctorRel;
import com.onlinedoctor.sqlite.DBOpenHelper;
import com.onlinedoctor.sqlite.service.PatientDocRelationService;

public class PatientDocRelationServiceImpl implements PatientDocRelationService {

	private DBOpenHelper dbHelper;

	// updateSql 需要的时候才弄
	public PatientDocRelationServiceImpl(Context context, List<String> updateSql) {
		dbHelper = DBOpenHelper.createInstance();
	}
	
	@Override
	public boolean addRelationInfo(Object[] params) {
		Boolean flag=false;
		SQLiteDatabase db=null;
		try {
			db=dbHelper.getWritableDatabase();
			String sql="insert into patient_doctor_relation_tb(patientId, doctorId, time, status) values(?,?,?,?)";
			db.execSQL(sql, params);
			flag=true;
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			if(db!=null)
				db.close();
		}
		return flag;
	}
	
	public boolean addRelationInfoOne(PatientDoctorRel pdr){
		Boolean flag = false;
		SQLiteDatabase db = null;
		String sql="insert into patient_doctor_relation_tb(patientId, doctorId, time, status) values(?,?,?,?)";
		String sqlQuery = "select status from patient_doctor_relation_tb where patientId=? and doctorId=?";
		String [] ss = {pdr.patientId, pdr.doctorId};
		try {
			db=dbHelper.getWritableDatabase();
			db.beginTransaction();
			Cursor cursor = db.rawQuery(sqlQuery, ss);
			if(cursor.moveToFirst() && cursor.getLong(cursor.getColumnIndex("status")) == 1){
				throw new Exception();
			}
			SQLiteStatement stat = db.compileStatement(sql);
			stat.bindString(1, pdr.patientId);
			stat.bindString(2, pdr.doctorId);
			stat.bindLong(3, pdr.time);
			stat.bindLong(4, pdr.status);
			if(stat.executeInsert() < 0){
				throw new Exception();
			}
			db.setTransactionSuccessful();
			flag = true;
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			if(db != null){
				db.endTransaction();
				db.close();
			}
		}
		return flag;
	}

	@Override
	public boolean updateRelationInfo(Object[] params) {
		Boolean flag=false;
		SQLiteDatabase db=null;
		try {
			db=dbHelper.getWritableDatabase();
			String sql="update patient_doctor_relation_tb set patientId=?, doctorId=?, time=?, status=? where id=?";
			db.execSQL(sql, params);
			flag=true;
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			if(db!=null)
				db.close();
		}
		return flag;
	}
	
	@Override
	public boolean updateStatus(String doctorId, String patientId, int status){
		Boolean flag=false;
		SQLiteDatabase db=null;
		try {
			db=dbHelper.getWritableDatabase();
			String sql="update patient_doctor_relation_tb set status=? where doctorId=? and patientId=?";
			String [] args = {Integer.toString(status), doctorId, patientId};
			db.execSQL(sql, args);
			flag=true;
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			if(db!=null)
				db.close();
		}
		return flag;
	}

	@Override
	public List<Map<String, Object>> listRelationInfoByPatientId(String patientId) {
		List<Map<String, Object>> list=new ArrayList<Map<String,Object>>();
		SQLiteDatabase db=null;
		try {
			db=dbHelper.getWritableDatabase();
			String sql="select patientId, doctorId, time, status from patient_doctor_relation_tb where patient_id="+patientId;
			Cursor cursor=db.rawQuery(sql, null);
			//获取当前库表中列的个数
			int columns=cursor.getColumnCount();
			while (cursor.moveToNext()) {
				Map<String, Object> map=new HashMap<String,Object>();
				String patientId2 = cursor.getString(cursor.getColumnIndex("patientId"));
				String doctorId = cursor.getString(cursor.getColumnIndex("doctorId"));
				long time = cursor.getLong(cursor.getColumnIndex("time"));
				long status = cursor.getLong(cursor.getColumnIndex("status"));
				map.put("patientId", patientId2);
				map.put("doctorId", doctorId);
				map.put("time", time);
				map.put("status", status);
				/*
				for(int i=0;i<columns;i++){
					String col_name=cursor.getColumnName(i);
					String col_value=cursor.getString(cursor.getColumnIndex(col_name));
					if(col_value==null)
						col_value="";
					map.put(col_name, col_value);
				}
				*/
				list.add(map);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			if(db!=null)
				db.close();
		}
		return list;
	}

	@Override
	public List<PatientDoctorRel> listRelationInfoByDoctorId(String doctorId) {
		//List<Map<String, Object>> list=new ArrayList<Map<String,Object>>();
		List<PatientDoctorRel> list = new ArrayList<PatientDoctorRel>();
		SQLiteDatabase db=null;
		
		try {
			db=dbHelper.getWritableDatabase();
			String sql="select * from patient_doctor_relation_tb where doctorId=?";
			String [] ss = {doctorId};
			Cursor cursor=db.rawQuery(sql, ss);
			//获取当前库表中列的个数
			while (cursor.moveToNext()) {
				/*Map<String, Object> map=new HashMap<String,Object>();
				String patientId = cursor.getString(cursor.getColumnIndex("patientId"));
				String doctorId2 = cursor.getString(cursor.getColumnIndex("doctorId"));
				long time = cursor.getLong(cursor.getColumnIndex("time"));
				long status = cursor.getLong(cursor.getColumnIndex("status"));
				map.put("patientId", patientId);
				map.put("doctorId", doctorId2);
				map.put("time", time);
				map.put("status", status);
				list.add(map);*/
				PatientDoctorRel pdr = new PatientDoctorRel();
				pdr.patientId = cursor.getString(cursor.getColumnIndex("patientId"));
				pdr.doctorId = cursor.getString(cursor.getColumnIndex("doctorId"));
				pdr.time = cursor.getLong(cursor.getColumnIndex("time"));
				pdr.status = cursor.getLong((cursor.getColumnIndex("status")));
				list.add(pdr);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			if(db!=null)
				db.close();
		}
		return list;
	}
	
	@Override
	public ConcurrentHashMap<String, Boolean> getPatientIdMapByDoctroId(String doctorId){
		ConcurrentHashMap<String, Boolean> patientIdMap = null;
		SQLiteDatabase db = null;
		try{
			db = dbHelper.getReadableDatabase();
			String sql = "select patientId from patient_doctor_relation_tb where doctorId=? and status=1";
			String [] args = {doctorId};
			Cursor cursor = db.rawQuery(sql, args);
			patientIdMap = new ConcurrentHashMap<String, Boolean>();
			while(cursor.moveToNext()){
				String patientId = cursor.getString(cursor.getColumnIndex("patientId"));
				patientIdMap.put(patientId, true);
			}
		} catch(Exception e){
			e.printStackTrace();
		} finally{
			if(db != null){
				db.close();
			}
		}
		return patientIdMap;
	}
	
	@Override
	public PatientDoctorRel viewRelationInfo(String doctorId, String patientId){
		PatientDoctorRel pdr = null;
		SQLiteDatabase db = null;
		try{
			db = dbHelper.getReadableDatabase();
			String sql = "select * from patient_doctor_relation_tb where doctorId=? and patientId=?";
			String [] args = {doctorId, patientId};
			Cursor cursor = db.rawQuery(sql, args);
			if(!cursor.moveToFirst()){
				throw new Exception();
			}
			pdr = new PatientDoctorRel();
			pdr.patientId = cursor.getString(cursor.getColumnIndex("patientId"));
			pdr.doctorId = cursor.getString(cursor.getColumnIndex("doctorId"));
			pdr.time = cursor.getLong(cursor.getColumnIndex("time"));
			pdr.status = cursor.getLong(cursor.getColumnIndex("status"));
		} catch (Exception e){
			e.printStackTrace();
		} finally{
			if(db != null){
				db.close();
			}
		}
		return pdr;
	}

	@Override
	public Map<String, String> viewRelationInfo(String[] selectionArgs) {
		Map<String, String> map=new HashMap<String, String>();
		SQLiteDatabase db=null;
		try {
			db=dbHelper.getWritableDatabase();
			String sql="select patientId, doctorId, time, status from patient_doctor_relation_tb where id=?";
			Cursor cursor=db.rawQuery(sql, selectionArgs);
			//获取当前库表中列的个数
			int columns=cursor.getColumnCount();
			while (cursor.moveToNext()) {
				for(int i=0;i<columns;i++){
					String col_name=cursor.getColumnName(i);
					String col_value=cursor.getString(cursor.getColumnIndex(col_name));
					if(col_value==null)
						col_value="";
					map.put(col_name, col_value);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			if(db!=null)
				db.close();
		}
		return map;
	}

	@Override
	public boolean deleteAllRelation() {
		Boolean flag=false;
		SQLiteDatabase db=null;
		try {
			db=dbHelper.getWritableDatabase();
			String sql="delete from patient_doctor_relation_tb";
			db.execSQL(sql);
			flag=true;
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			if(db!=null)
				db.close();
		}
		return flag;
	}

}
