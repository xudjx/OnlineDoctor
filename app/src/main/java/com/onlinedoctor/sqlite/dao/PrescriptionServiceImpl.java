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
import com.onlinedoctor.pojo.tools.prescription.PrescriptionDO;
import com.onlinedoctor.sqlite.DBOpenHelper;
import com.onlinedoctor.sqlite.service.PrescriptionService;

public class PrescriptionServiceImpl implements PrescriptionService {

	private DBOpenHelper dbHelper;

	public PrescriptionServiceImpl() {
		dbHelper = DBOpenHelper.createInstance();
	}
	
	@Override
	public boolean addPrescriptionInfo(PrescriptionDO presDo){
		Boolean flag=false;
		SQLiteDatabase db=null;
		try {
			db=dbHelper.getWritableDatabase();
			String sql="insert into prescription_tb(global_id, name, detail, price, doctor_id, type, is_deleted,remark,prescription_imgs,patient_id,create_time) values(?,?,?,?,?,?,?,?,?,?,?)";
			SQLiteStatement statement = db.compileStatement(sql);
			statement.bindLong(1, presDo.getGlobal_id());
			statement.bindString(2, presDo.getName());
			statement.bindString(3, presDo.getDetail());
			statement.bindLong(4, presDo.getPrice());
			statement.bindString(5, presDo.getDoctor_id());
			statement.bindLong(6, presDo.getType());
			statement.bindLong(7, presDo.is_deleted());
			statement.bindString(8, presDo.getRemark());
			statement.bindString(9, presDo.getPrescription_imgs());
			statement.bindString(10, presDo.getPatient_id());
			statement.bindLong(11, presDo.getCreate_time());
			if (statement.executeInsert() < 0) {
				throw new Exception();
			}
			flag = true;
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			if(db!=null)
				db.close();
		}
		return flag;
	}

	@Override
	public boolean updatePrescriptionInfo(PrescriptionDO presDo) {
		Boolean flag=false;
		SQLiteDatabase db=null;
		try {
			db=dbHelper.getWritableDatabase();
			String sql="update prescription_tb set global_id=?, name=?, detail=?, price=?, doctor_id=?, type=?, is_deleted=?,remark=?,prescription_imgs=?,patient_id=?,create_time=?";
			SQLiteStatement statement = db.compileStatement(sql);
			statement.bindLong(1, presDo.getGlobal_id());
			statement.bindString(2, presDo.getName());
			statement.bindString(3, presDo.getDetail());
			statement.bindLong(4, presDo.getPrice());
			statement.bindString(5, presDo.getDoctor_id());
			statement.bindLong(6, presDo.getType());
			statement.bindLong(7, presDo.is_deleted());
			statement.bindString(8, presDo.getRemark());
			statement.bindString(9, presDo.getPrescription_imgs());
			statement.bindString(10, presDo.getPatient_id());
			statement.bindLong(11, presDo.getCreate_time());
			statement.execute();
			flag = true;
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			if(db!=null)
				db.close();
		}
		return flag;
	}

	@Override
	public boolean deletePrescriptionInfo(PrescriptionDO presDo) {
		Boolean flag=false;
		SQLiteDatabase db=null;
		try {
			db=dbHelper.getWritableDatabase();
			String sql="update prescription_tb set is_deleted=1 where globa_id=?";
			String [] args = {String.valueOf(presDo.getGlobal_id())};
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
	public List<PrescriptionDO> listPrescriptionInfoByPatientId(
			String patientId) {
		List<PrescriptionDO> list=new ArrayList<PrescriptionDO>();
		SQLiteDatabase db=null;
		try {
			db=dbHelper.getWritableDatabase();
			String sql="select * from prescription_tb where patient_id=? and is_deleted=0";
			String [] args = {patientId};
			Cursor cursor=db.rawQuery(sql, args);
			//获取当前库表中列的个数
			int columns=cursor.getColumnCount();
			while (cursor.moveToNext()) {
				PrescriptionDO prescriptionDO = new PrescriptionDO();
				prescriptionDO.setId(cursor.getInt(cursor.getColumnIndex("id")));
				prescriptionDO.setGlobal_id(cursor.getInt(cursor.getColumnIndex("global_id")));
				prescriptionDO.setName(cursor.getString(cursor.getColumnIndex("name")));
				prescriptionDO.setPrice(cursor.getInt(cursor.getColumnIndex("price")));
				prescriptionDO.setDoctor_id(cursor.getString(cursor.getColumnIndex("doctor_id")));
				prescriptionDO.setPatient_id(cursor.getString(cursor.getColumnIndex("patient_id")));
				prescriptionDO.setType(cursor.getInt(cursor.getColumnIndex("type")));
				prescriptionDO.setIs_deleted(cursor.getInt(cursor.getColumnIndex("is_deleted")));
				prescriptionDO.setCreate_time(cursor.getLong(cursor.getColumnIndex("create_time")));
				prescriptionDO.setRemark(cursor.getString(cursor.getColumnIndex("remark")));
				prescriptionDO.setPrescription_imgs(cursor.getString(cursor.getColumnIndex("prescription_imgs")));
				list.add(prescriptionDO);
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
	public PrescriptionDO viewPrescriptionByGrobalId(int globalId) {
		PrescriptionDO prescriptionDO = null;
		SQLiteDatabase db=null;
		try {
			db=dbHelper.getWritableDatabase();
			String sql="select * from prescription_tb where id=? and is_deleted=0";
			String [] args = {String.valueOf(globalId)};
			Cursor cursor=db.rawQuery(sql, args);
			if (cursor.moveToNext()) {
				prescriptionDO = new PrescriptionDO();
				prescriptionDO.setId(cursor.getInt(cursor.getColumnIndex("id")));
				prescriptionDO.setGlobal_id(cursor.getInt(cursor.getColumnIndex("global_id")));
				prescriptionDO.setName(cursor.getString(cursor.getColumnIndex("name")));
				prescriptionDO.setPrice(cursor.getInt(cursor.getColumnIndex("price")));
				prescriptionDO.setDoctor_id(cursor.getString(cursor.getColumnIndex("doctor_id")));
				prescriptionDO.setPatient_id(cursor.getString(cursor.getColumnIndex("patient_id")));
				prescriptionDO.setType(cursor.getInt(cursor.getColumnIndex("type")));
				prescriptionDO.setIs_deleted(cursor.getInt(cursor.getColumnIndex("is_deleted")));
				prescriptionDO.setCreate_time(cursor.getLong(cursor.getColumnIndex("create_time")));
				prescriptionDO.setRemark(cursor.getString(cursor.getColumnIndex("remark")));
				prescriptionDO.setPrescription_imgs(cursor.getString(cursor.getColumnIndex("prescription_imgs")));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			if(db!=null)
				db.close();
		}
		return prescriptionDO;
	}

	@Override
	public boolean deleteAllPrescription() {
		Boolean flag=false;
		SQLiteDatabase db=null;
		try {
			db=dbHelper.getWritableDatabase();
			String sql="delete from prescription_tb";
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
