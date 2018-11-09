package com.onlinedoctor.sqlite.dao;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

import com.onlinedoctor.pojo.Common;
import com.onlinedoctor.pojo.patient.PatientLabelRel;
import com.onlinedoctor.sqlite.DBOpenHelper;
import com.onlinedoctor.sqlite.service.PatientLabelRelationService;

public class PatientLabelRelationServiceImpl implements PatientLabelRelationService{

	private DBOpenHelper dbHelper;
	
	public PatientLabelRelationServiceImpl(Context context, List<String> updateSql) {
		dbHelper = DBOpenHelper.createInstance();
	}
	
	@Override
	public boolean addRelationInfoOne(PatientLabelRel plr){
		boolean flag = false;
		SQLiteDatabase db = null;
		String sqlQuery = "select id from patient_label_relation_tb where patientId = ? and labelId = ?";
		String sql = "insert into patient_label_relation_tb(patientId, labelId, status) values(?, ?, ?)";
		String [] ss = {plr.getPatientId(), String.valueOf(plr.getLabelId())};
		try{
			db = dbHelper.getWritableDatabase();
			db.beginTransaction();
			Cursor cursor = db.rawQuery(sqlQuery, ss);
			if(cursor.moveToFirst()){
				throw new Exception();
			}
			SQLiteStatement stat = db.compileStatement(sql);
			stat.bindString(1,  plr.getPatientId());
			stat.bindLong(2, plr.getLabelId());
			stat.bindLong(3, plr.getStatus());
			if(stat.executeInsert() < 0){
				throw new Exception();
			}
			flag = true;
			db.setTransactionSuccessful();
		}catch(Exception e){
			e.printStackTrace();
		}
		finally{
			if(db != null){
				db.endTransaction();
				db.close();
			}
		}
		return flag;
	}
	
	@Override
	public boolean addRelationInfoOne(String patientId, long labelId){
		boolean flag = false;
		SQLiteDatabase db = null;
		String sqlQuery = "select id from patient_label_relation_tb where patientId = ? and labelId = ?";
		String sql = "insert into patient_label_relation_tb(patientId, labelId, status) values(?, ?, ?)";
		String [] ss = {patientId, Long.toString(labelId)};
		try{
			db = dbHelper.getWritableDatabase();
			db.beginTransaction();
			Cursor cursor = db.rawQuery(sqlQuery, ss);
			if(cursor.moveToFirst()){
				throw new Exception();
			}
			SQLiteStatement stat = db.compileStatement(sql);
			stat.bindString(1, patientId);
			stat.bindLong(2, labelId);
			stat.bindLong(3, 1);
			if(stat.executeInsert() < 0){
				throw new Exception();
			}
			flag = true;
			db.setTransactionSuccessful();
		}catch(Exception e){
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
	public boolean deleteRelationInfoOne(String patientId, long labelId){
		boolean flag = false;
		SQLiteDatabase db = null;
		String sql = "delete from patient_label_relation_tb where patientId=? and labelId=?";
		String [] args = {patientId, Long.toString(labelId)};
		try{
			db = dbHelper.getWritableDatabase();
			db.execSQL(sql, args);
			flag = true;
		} catch(Exception e){
			e.printStackTrace();
		} finally{
			if(db != null){
				db.close();
			}
		}
		return flag;
	}
	
	@Override
	public boolean deleteRelationInfoByLabelId(long labelId){
		boolean flag = false;
		SQLiteDatabase db = null;
		String sql = "delete from patient_label_relation_tb where labelId=?";
		String [] args = {Long.toString(labelId)};
		try{
			db = dbHelper.getWritableDatabase();
			db.execSQL(sql, args);
			flag = true;
		} catch(Exception e){
			e.printStackTrace();
		} finally{
			if(db != null){
				db.close();
			}
		}
		return flag;
	}

	@Override
	public boolean deleteRelationIfoByPatientId(String patientId){
		boolean flag = false;
		SQLiteDatabase db = null;
		String sql = "delete from patient_label_relation_tb where patientId=?";
		String [] args = {patientId};
		try{
			db = dbHelper.getWritableDatabase();
			db.execSQL(sql, args);
			flag = true;
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			if(db != null){
				db.close();
			}
		}
		return flag;
	}
	
	@Override
	public List<String> listPatientByLabelId(Long labelId){
		SQLiteDatabase db = null;
		String sql = "select patientId from patient_label_relation_tb where labelId = ? and status = ?";
		String [] ss = {Long.toString(labelId), String.valueOf(1)};
		List<String> patientIds = new ArrayList<String>();
		try{
			db = dbHelper.getReadableDatabase();
			Cursor cursor = db.rawQuery(sql, ss);
			while(cursor.moveToNext()){
				String patientId = cursor.getString(0);
				patientIds.add(patientId);
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		finally{
			if(db != null){
				db.close();
			}
		}
		return patientIds;
	}
	
	@Override
	public List<Long> listLabelByPatientId(String patientId){
		SQLiteDatabase db = null;
		String sql  = "select labelId from patient_label_relation_tb where patientId=? and status=?";
		String [] args = {patientId, String.valueOf(1)};
		List<Long> labelIds = new ArrayList<Long>();
		try{
			db = dbHelper.getReadableDatabase();
			Cursor cursor = db.rawQuery(sql, args);
			while(cursor.moveToNext()){
				Long labelId = cursor.getLong(0);
				labelIds.add(labelId);
			}
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			if(db != null){
				db.close();
			}
		}
		return labelIds;
	}
	
	@Override
	public HashSet<Long> getLabelByPatientId(String patientId){
		SQLiteDatabase db = null;
		String sql  = "select labelId from patient_label_relation_tb where patientId=? and status=?";
		String [] args = {patientId, String.valueOf(1)};
		HashSet<Long> labelIds = new HashSet<Long>();
		try{
			db = dbHelper.getReadableDatabase();
			Cursor cursor = db.rawQuery(sql, args);
			while(cursor.moveToNext()){
				Long labelId = cursor.getLong(0);
				labelIds.add(labelId);
			}
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			if(db != null){
				db.close();
			}
		}
		return labelIds;
	}

}
