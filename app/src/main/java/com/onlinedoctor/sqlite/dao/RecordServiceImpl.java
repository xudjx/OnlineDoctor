package com.onlinedoctor.sqlite.dao;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

import com.onlinedoctor.pojo.Common;
import com.onlinedoctor.pojo.patient.Record;
import com.onlinedoctor.sqlite.DBOpenHelper;
import com.onlinedoctor.sqlite.service.RecordService;

public class RecordServiceImpl implements RecordService {

	private DBOpenHelper dbHelper;

	// updateSql 需要的时候才弄
	public RecordServiceImpl(Context context, List<String> updateSql) {
		dbHelper = DBOpenHelper.createInstance();
	}

	@Override
	public int addRecord(Record record){
		int flag = -1;
		SQLiteDatabase db = null;
		String sql = "insert into record_tb(patientId, doctorId, thumbnail, recordPic, recordType, created) values(?, ?, ?, ?, ?, ?)";
		try{
			db = dbHelper.getWritableDatabase();
			SQLiteStatement stat = db.compileStatement(sql);
			stat.bindString(1, record.getPatientId());
			stat.bindString(2, record.getDoctorId());
			stat.bindString(3, record.getThumbnail());
			stat.bindString(4, record.getRecordPic());
			stat.bindLong(5, record.getRecordType());
			stat.bindLong(6, record.getCreated());
			long newRecordId = stat.executeInsert();
			if(newRecordId < 0){
				throw new Exception();
			}
			record.setId(newRecordId);
			flag = 0;
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
	public List<Record> listRecords(String patientId, String doctorId){
		List<Record> list = new ArrayList<Record>();
		SQLiteDatabase db = null;
		String sql = "select * from record_tb where patientId=? and doctorId=? order by created";
		String [] args = {patientId, doctorId};
		try{
			db = dbHelper.getReadableDatabase();
			Cursor cursor = db.rawQuery(sql, args);
			while(cursor.moveToNext()){
				Record record = new Record();
				record.setId(cursor.getLong(cursor.getColumnIndex("id")));
				record.setPatientId(cursor.getString(cursor.getColumnIndex("patientId")));
				record.setDoctorId(cursor.getString(cursor.getColumnIndex("doctorId")));
				record.setRecordPic(cursor.getString(cursor.getColumnIndex("recordPic")));
				record.setRecordType(cursor.getInt(cursor.getColumnIndex("recordType")));
				record.setThumbnail(cursor.getString(cursor.getColumnIndex("thumbnail")));
				record.setCreated(cursor.getLong(cursor.getColumnIndex("created")));
				list.add(record);
			}
		} catch(Exception e){
			e.printStackTrace();
		} finally{
			if(db != null){
				db.close();
			}
		}
		return list;
	}

	@Override
	public boolean deleteRecord(String patientId) {
		Boolean flag = false;
		SQLiteDatabase db = null;
		try {
			db = dbHelper.getWritableDatabase();
			String sql = "delete from record_tb where patientId=?";
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
	public List<Record> listRecordsByType(int recordType) {
		List<Record> list = new ArrayList<Record>();
		SQLiteDatabase db = null;
		String sql = "select * from record_tb where recordType=? order by created";
		String [] args = {String.valueOf(recordType)};
		try{
			db = dbHelper.getReadableDatabase();
			Cursor cursor = db.rawQuery(sql, args);
			while(cursor.moveToNext()){
				Record record = new Record();
				record.setId(cursor.getLong(cursor.getColumnIndex("id")));
				record.setPatientId(cursor.getString(cursor.getColumnIndex("patientId")));
				record.setDoctorId(cursor.getString(cursor.getColumnIndex("doctorId")));
				record.setRecordPic(cursor.getString(cursor.getColumnIndex("recordPic")));
				record.setRecordType(cursor.getInt(cursor.getColumnIndex("recordType")));
				record.setThumbnail(cursor.getString(cursor.getColumnIndex("thumbnail")));
				record.setCreated(cursor.getLong(cursor.getColumnIndex("created")));
				list.add(record);
			}
		} catch(Exception e){
			e.printStackTrace();
		} finally{
			if(db != null){
				db.close();
			}
		}
		return list;
	}
}
