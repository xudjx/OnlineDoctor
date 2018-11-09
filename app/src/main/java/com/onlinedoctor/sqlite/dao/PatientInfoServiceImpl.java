package com.onlinedoctor.sqlite.dao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

import com.onlinedoctor.pojo.Common;
import com.onlinedoctor.pojo.patient.Patient;
import com.onlinedoctor.sqlite.DBOpenHelper;
import com.onlinedoctor.sqlite.service.PatientInfoService;

public class PatientInfoServiceImpl implements PatientInfoService{

	private DBOpenHelper dbHelper;

	// updateSql 需要的时候才弄
	public PatientInfoServiceImpl(Context context, List<String> updateSql) {
		dbHelper = DBOpenHelper.createInstance();
	}
	
	@Override
	public boolean addPatientInfoOne(Patient p) {
		boolean flag=false;
		SQLiteDatabase db=null;
		try {
			db=dbHelper.getWritableDatabase();
			String sql="insert into patient_info_tb(patientId, name, gender, birthday, registerTime, lastTime, thumbnail, avatar, phone, pinyin, comment) values(?,?,?,?,?,?,?,?,?,?,?)";
			SQLiteStatement stat = db.compileStatement(sql);
			//test
			stat.clearBindings();
			if(p.getPatientId() != null)
				stat.bindString(1, p.getPatientId());
			if(p.getName() != null)
				stat.bindString(2, p.getName());
			if(p.getGender() != null)
				stat.bindString(3, p.getGender());
			stat.bindLong(4, p.getBirthday());
			stat.bindLong(5, p.getRegisterTime());
			stat.bindLong(6, p.getLastTime());
			if(p.getThumbnail() != null)
				stat.bindString(7, p.getThumbnail());
			if(p.getAvatar() != null)
				stat.bindString(8, p.getAvatar());
			if(p.getPhone() != null)
				stat.bindString(9, p.getPhone());
			if(p.getPinyin() != null)
				stat.bindString(10, p.getPinyin());
			if(p.getComment() != null)
				stat.bindString(11, p.getComment());
			if(stat.executeInsert() < 0){
				throw new Exception();
			}
			flag=true;

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (db != null)
				db.close();
		}
		return flag;
	}

	@Override
	public boolean addPatientInfo(ArrayList<Patient> pList) {
		Boolean flag = false;
		String sql = "insert into patient_info_tb(patientId, name, gender, birthday, registerTime, lastTime, thumbnail, avatar, phone, pinyin, comment) values(?,?,?,?,?,?,?,?,?,?)";
		SQLiteDatabase db = null;
		try {
			db = dbHelper.getWritableDatabase();
			SQLiteStatement stat = db.compileStatement(sql);
			db.beginTransaction();
			for (Patient p : pList) {
				stat.clearBindings();
				stat.bindString(1, p.getPatientId());
				stat.bindString(2, p.getName());
				stat.bindString(3, p.getGender());
				stat.bindLong(4, p.getBirthday());
				stat.bindLong(5, p.getRegisterTime());
				stat.bindLong(6, p.getLastTime());
				stat.bindString(7, p.getThumbnail());
				stat.bindString(8, p.getAvatar());
				stat.bindString(9, p.getPhone());
				stat.bindString(10, p.getPinyin());
				stat.bindString(11, p.getComment());
				if (stat.executeInsert() < 0) {
					throw new Exception();
				}
			}
			db.setTransactionSuccessful();
			flag = true;
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if(db != null){
				db.endTransaction();
				db.close();
			}
		}
		return flag;
	}

	@Override
	public boolean updatePatientInfo(Object[] params) {
		Boolean flag = false;
		SQLiteDatabase db = null;
		try {
			db = dbHelper.getWritableDatabase();
			String sql = "update patient_info_tb set patient_id=?, name=?, gender=?, birthday=?, register_time=?, last_time=? thumbnail=?, avatar=?, phone=?, pinyin=? comment=? where id=?";
			db.execSQL(sql, params);
			flag = true;
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (db != null)
				db.close();
		}
		return flag;
	}

	@Override
	public boolean updatePatientComment(String patientId, String comment){
		Boolean flag = false;
		SQLiteDatabase db = null;
		try {
			db = dbHelper.getWritableDatabase();
			String sql = "update patient_info_tb set comment=? where patientId=?";
			String [] params = {comment, patientId};
			db.execSQL(sql, params);
			flag = true;
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (db != null)
				db.close();
		}
		return flag;
	}
	
	@Override
	public boolean deletePatientInfo(String patientId) {
		Boolean flag = false;
		SQLiteDatabase db = null;
		try {
			db = dbHelper.getWritableDatabase();
			String sql = "delete from patient_info_tb where patientId=?";
			String [] ss = {patientId};
			db.execSQL(sql, ss);
			flag = true;
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (db != null)
				db.close();
		}
		return flag;
	}

	@Override
	public List<Patient> listPatientInfoById(String [] patientIds){
		List<Patient> pList = new ArrayList<Patient>();
		SQLiteDatabase db = null;
		try{
			db = dbHelper.getWritableDatabase();
			String sql = "select patientId, name, gender, birthday, registerTime, lastTime, thumbnail, avatar, phone, pinyin, comment from patient_info_tb where patientId in (" + DBOpenHelper.makePlaceHolders(patientIds.length) + ")";
			Cursor cursor = db.rawQuery(sql, patientIds);
			while(cursor.moveToNext()){
				Patient p = new Patient();
				p.setPatientId(cursor.getString(0));
				p.setName(cursor.getString(1));
				p.setGender(cursor.getString(2));
				p.setBirthday(cursor.getLong(3));
				p.setRegisterTime(cursor.getLong(4));
				p.setLastTime(cursor.getLong(5));
				p.setThumbnail(cursor.getString(6));
				p.setAvatar(cursor.getString(7));
				p.setPhone(cursor.getString(8));
				p.setPinyin(cursor.getString(9));
				p.setComment(cursor.getString(10));
				pList.add(p);
			}
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			if(db != null){
				db.close();
			}
		}
		return pList;
	}

	@Override
	public List<Patient> listAllPatientInfo(){
		List<Patient> pList = new ArrayList<Patient>();
		SQLiteDatabase db = null;
		try{
			db = dbHelper.getWritableDatabase();
			String sql = "select patientId, name, gender, birthday, registerTime, lastTime, thumbnail, avatar, phone, pinyin, comment from patient_info_tb";
			Cursor cursor = db.rawQuery(sql, null);
			while(cursor.moveToNext()){
				Patient p = new Patient();
				p.setPatientId(cursor.getString(0));
				p.setName(cursor.getString(1));
				p.setGender(cursor.getString(2));
				p.setBirthday(cursor.getLong(3));
				p.setRegisterTime(cursor.getLong(4));
				p.setLastTime(cursor.getLong(5));
				p.setThumbnail(cursor.getString(6));
				p.setAvatar(cursor.getString(7));
				p.setPhone(cursor.getString(8));
				p.setPinyin(cursor.getString(9));
				p.setComment(cursor.getString(10));
				pList.add(p);
			}
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			if(db != null){
				db.close();
			}
		}
		return pList;
	}
	
	/*@Override
	public ConcurrentHashSet<String> getPatientGuidSet(){
		ConcurrentHashSet<String> patientGuidSet = new ConcurrentHashSet<String>();
		SQLiteDatabase db = null;
		try{
			db = dbHelper.getReadableDatabase();
			String sql = "select patientId from patient_info_tb";
			Cursor cursor = db.rawQuery(sql, null);
			while(cursor.moveToNext()){
				String pGuid = cursor.getString(0);
				patientGuidSet.add(pGuid);
			}
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			if(db != null){
				db.close();
			}
		}
		return patientGuidSet;
	}*/
	
	@Override
	public List<Map<String, String>> listPatientInfoBy(String[] selectionArgs) {
		List<Map<String, String>> list = new ArrayList<Map<String, String>>();
		SQLiteDatabase db = null;
		try {
			db = dbHelper.getWritableDatabase();
			String sql = "select * from patient_info_tb";
			Cursor cursor = db.rawQuery(sql, selectionArgs);
			// 获取当前库表中列的个数
			int columns = cursor.getColumnCount();
			while (cursor.moveToNext()) {
				Map<String, String> map = new HashMap<String, String>();
				for (int i = 0; i < columns; i++) {
					//需要改
					String col_name = cursor.getColumnName(i);
					String col_value = cursor.getString(cursor
							.getColumnIndex(col_name));
					if (col_value == null)
						col_value = "";
					map.put(col_name, col_value);
				}
				list.add(map);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (db != null)
				db.close();
		}
		return list;
	}

	
	@Override
	public Map<String, String> viewPatientInfo(String[] selectionArgs) {
		Map<String, String> map = new HashMap<String, String>();
		SQLiteDatabase db = null;
		try {
			db = dbHelper.getWritableDatabase();
			String sql = "select * from patient_info_tb where id=?";
			Cursor cursor = db.rawQuery(sql, selectionArgs);
			// 获取当前库表中列的个数
			int columns = cursor.getColumnCount();
			while (cursor.moveToNext()) {
				for (int i = 0; i < columns; i++) {
					String col_name = cursor.getColumnName(i);
					String col_value = cursor.getString(cursor
							.getColumnIndex(col_name));
					if (col_value == null)
						col_value = "";
					map.put(col_name, col_value);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (db != null)
				db.close();
		}
		return map;
	}
	
	@Override
	public Patient viewPatientInfoByPatientId(String patientId){
		Patient patient = null;
		SQLiteDatabase db = null;
		try{
			db = dbHelper.getWritableDatabase();
			String sql = "select * from patient_info_tb where patientId=?";
			String [] ss = {patientId};
			Cursor cursor = db.rawQuery(sql, ss);	
			if(!cursor.moveToFirst()){
				throw new Exception();
			}
			patient = new Patient();
			patient.setPatientId(cursor.getString(cursor.getColumnIndex("patientId")));
			patient.setName(cursor.getString(cursor.getColumnIndex("name")));
			patient.setGender(cursor.getString(cursor.getColumnIndex("gender")));
			patient.setBirthday(cursor.getLong(cursor.getColumnIndex("birthday")));
			patient.setRegisterTime(cursor.getLong(cursor.getColumnIndex("registerTime")));
			patient.setLastTime(cursor.getLong(cursor.getColumnIndex("lastTime")));
			patient.setThumbnail(cursor.getString(cursor.getColumnIndex("thumbnail")));
			patient.setAvatar(cursor.getString(cursor.getColumnIndex("avatar")));
			patient.setPhone(cursor.getString(cursor.getColumnIndex("phone")));
			patient.setPinyin(cursor.getString(cursor.getColumnIndex("pinyin")));
			patient.setComment(cursor.getString(cursor.getColumnIndex("comment")));
		}
		catch(Exception e){
			e.printStackTrace();
		}
		finally{
			if(db != null){
				db.close();
			}
		}
		return patient;
	}

	
	@Override
	public boolean deleteAllPatient() {
		Boolean flag = false;
		SQLiteDatabase db = null;
		try {
			db = dbHelper.getWritableDatabase();
			String sql = "delete from patient_info_tb";
			db.execSQL(sql);
			flag = true;
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (db != null)
				db.close();
		}
		return flag;
	}

}
