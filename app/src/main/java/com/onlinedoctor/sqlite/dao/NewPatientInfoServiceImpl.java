package com.onlinedoctor.sqlite.dao;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

import com.onlinedoctor.pojo.Common;
import com.onlinedoctor.pojo.patient.NewPatient;
import com.onlinedoctor.sqlite.DBOpenHelper;
import com.onlinedoctor.sqlite.service.NewPatientInfoService;

public class NewPatientInfoServiceImpl implements NewPatientInfoService{

	private DBOpenHelper dbHelper;
	
	public NewPatientInfoServiceImpl(Context context, List<String> updateSql) {
		dbHelper = DBOpenHelper.createInstance();
	}

	@Override
	public boolean addNewPatientInfo(ArrayList<NewPatient> npList) {
		boolean flag = false;
		String sql = "insert into new_patient_info_tb(patientId, name, gender, followTime, thumbnail, status, isChecked) values(?, ?, ?, ?, ?, ?, ?)";
		SQLiteDatabase db = null;
		try{
			db = dbHelper.getWritableDatabase();
			SQLiteStatement stat = db.compileStatement(sql);
			db.beginTransaction();
			for(NewPatient np : npList){
				stat.bindString(1, np.getPatientId());
				stat.bindString(2, np.getName());
				stat.bindString(3, np.getGender());
				stat.bindLong(4, np.getFollowTime());
				stat.bindString(5, np.getThumbnail());
				stat.bindLong(6, np.getStatus());
				stat.bindLong(7, np.getIsChecked());
				if(stat.executeInsert() < 0){
					throw new Exception();
				}
			}
			db.setTransactionSuccessful();
			flag = true;
		} catch(Exception e){
			e.printStackTrace();
		} finally{
			db.endTransaction();
			db.close();
		}
		return flag;
	}

	@Override
	public boolean addNewPatientInfoOne(NewPatient np) {
		boolean flag = false;
		SQLiteDatabase db = null;
		try{
			db = dbHelper.getWritableDatabase();
			String sql = "insert into new_patient_info_tb(patientId, name, gender, followTime, thumbnail, status, isChecked) values(?, ?, ?, ?, ?, ?, ?)";
			SQLiteStatement stat = db.compileStatement(sql);
			if(np.getPatientId() != null)
				stat.bindString(1, np.getPatientId());
			if(np.getName() != null)
				stat.bindString(2, np.getName());
			if(np.getGender() != null)
				stat.bindString(3, np.getGender());
			stat.bindLong(4, np.getFollowTime());
			if(np.getThumbnail() != null)
				stat.bindString(5, np.getThumbnail());
			stat.bindLong(6, np.getStatus());
			stat.bindLong(7, np.getIsChecked());
			if(stat.executeInsert() < 0){
				throw new Exception();
			}
			flag = true;
		} catch(Exception e){
			e.printStackTrace();
		} finally{
			if(db != null)
				db.close();
		}
		return flag;
	}
	
	@Override
	public boolean updateStatusNewPatientInfoOne(String patientId, int status){
		boolean flag = false;
		SQLiteDatabase db = null;
		String sql = "update new_patient_info_tb set status=? where patientId=?";
		try{
			db = dbHelper.getWritableDatabase();
			SQLiteStatement stat = db.compileStatement(sql);
			stat.clearBindings();
			stat.bindLong(1, status);
			stat.bindString(2, patientId);
			stat.execute();
			flag = true;
		} catch(Exception e){
			if(db != null){
				db.close();
			}
		}
		return flag;
	}

	@Override
	public NewPatient viewNewPatientInfoById(String patientId) {
		NewPatient newPatient = null;
		SQLiteDatabase db = null;
		try{
			db = dbHelper.getWritableDatabase();
			String sql = "select * from new_patient_info_tb where patientId=?";
			String [] ss = {patientId};
			Cursor cursor = db.rawQuery(sql, ss);
			if(!cursor.moveToNext()){
				throw new Exception();
			}
			newPatient = new NewPatient();
			newPatient.setPatientId(cursor.getString(cursor.getColumnIndex("patientId")));
			newPatient.setName(cursor.getString(cursor.getColumnIndex("name")));
			newPatient.setGender(cursor.getString(cursor.getColumnIndex("gender")));
			newPatient.setFollowTime(cursor.getLong(cursor.getColumnIndex("followTime")));
			newPatient.setThumbnail(cursor.getString(cursor.getColumnIndex("thumbnail")));
			newPatient.setStatus(cursor.getInt(cursor.getColumnIndex("status")));
			newPatient.setIsChecked(cursor.getInt(cursor.getColumnIndex("isChecked")));
		} catch(Exception e){
			e.printStackTrace();
		} finally{
			if(db != null){
				db.close();
			}
		}
		return newPatient;
	}
	
	@Override
	public List<NewPatient> listAll(){
		List<NewPatient> list = new ArrayList<NewPatient>();
		SQLiteDatabase db = null;
		try{
			db = dbHelper.getWritableDatabase();
			String sql = "select * from new_patient_info_tb where status=1 order by followTime DESC";
			Cursor cursor = db.rawQuery(sql, null);
			while(cursor.moveToNext()){
				NewPatient newPatient = new NewPatient();
				newPatient.setPatientId(cursor.getString(cursor.getColumnIndex("patientId")));
				newPatient.setName(cursor.getString(cursor.getColumnIndex("name")));
				newPatient.setGender(cursor.getString(cursor.getColumnIndex("gender")));
				newPatient.setFollowTime(cursor.getLong(cursor.getColumnIndex("followTime")));
				newPatient.setThumbnail(cursor.getString(cursor.getColumnIndex("thumbnail")));
				newPatient.setStatus(cursor.getInt(cursor.getColumnIndex("status")));
				newPatient.setIsChecked(cursor.getInt(cursor.getColumnIndex("isChecked")));
				list.add(newPatient);
			}
		} catch(Exception e){
			e.printStackTrace();
		} finally{
			if(db != null)
				db.close();
		}
		return list;
	}

	@Override
	public boolean allCheck(){
		SQLiteDatabase db = null;
		boolean flag = false;
		try{
			db = dbHelper.getWritableDatabase();
			String sql = "update new_patient_info_tb set isChecked = 1 where isChecked = 0";
			db.execSQL(sql);
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
	public int getUnchechedCount(){
		SQLiteDatabase db = null;
		boolean flag = false;
		int count = 0;
		try{
			db = dbHelper.getReadableDatabase();
			String sql = "select count(*) from new_patient_info_tb where isChecked = 0";
			Cursor cursor = db.rawQuery(sql, null);
			if(cursor.moveToNext()){
				count = cursor.getInt(0);
			}
			flag = true;
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			if(db != null){
				db.close();
			}
		}
		return count;
	}

}
