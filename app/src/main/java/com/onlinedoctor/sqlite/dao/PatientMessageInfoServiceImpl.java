package com.onlinedoctor.sqlite.dao;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

import com.onlinedoctor.pojo.Common;
import com.onlinedoctor.pojo.patient.PatientMessage;
import com.onlinedoctor.sqlite.DBOpenHelper;
import com.onlinedoctor.sqlite.service.PatientMessageInfoService;

/**
 * 调用之前请查看该函数的说明, 特别的查看该函数的Sql语句 如果需要的方法的没有覆盖到，则自己创建
 */
public class PatientMessageInfoServiceImpl implements PatientMessageInfoService {

	private DBOpenHelper dbHelper;

	// updateSql 需要的时候才弄
	public PatientMessageInfoServiceImpl(Context context, List<String> updateSql) {
		dbHelper = DBOpenHelper.createInstance();
	}

	@Override
	public boolean addPatientMessageInfo(PatientMessage message) {
		Boolean flag = false;
		SQLiteDatabase db = null;
		try {
			db = dbHelper.getWritableDatabase();
			String sql = "insert into patient_message_tb(guid,content,contentType,fromID,toID,isRead,sendStatus,deleted,created) values(?,?,?,?,?,?,?,?,?)";
			SQLiteStatement statement = db.compileStatement(sql);
			statement.bindString(1, message.getGuid());
			statement.bindString(2, message.getContent());
			statement.bindString(3, message.getContentType());
			statement.bindString(4, message.getFromID());
			statement.bindString(5, message.getToID());
			statement.bindLong(6, message.getIsRead());
			statement.bindLong(7, message.getSendStatus());
			statement.bindLong(8, message.getDeleted());
			statement.bindLong(9, message.getTimestamp());
			if (statement.executeInsert() < 0) {
				throw new Exception();
			}
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
	public boolean addPatientMessageInfo(List<PatientMessage> messages){
		Boolean flag = false;
		String sql = "insert into patient_message_tb(guid,content,contentType,fromID,toID,isRead,sendStatus,deleted,created) values(?,?,?,?,?,?,?,?,?)";
		SQLiteDatabase db = null;
		try{
			db = dbHelper.getWritableDatabase();
			SQLiteStatement stat = db.compileStatement(sql);
			db.beginTransaction();
			for(PatientMessage message : messages){
				stat.bindString(1, message.getGuid());
				stat.bindString(2, message.getContent());
				stat.bindString(3, message.getContentType());
				stat.bindString(4, message.getFromID());
				stat.bindString(5, message.getToID());
				stat.bindLong(6, message.getIsRead());
				stat.bindLong(7, message.getSendStatus());
				stat.bindLong(8, message.getDeleted());
				stat.bindLong(9, message.getTimestamp());
				if(stat.executeInsert() < 0){
					throw new Exception();
				}
				stat.clearBindings();
			}
			db.setTransactionSuccessful();
			flag = true;
		} catch(Exception e){
			//to do
			e.printStackTrace();
		} finally{
			if(db != null){
				db.endTransaction();
				db.close();
			}
		}
		return flag;
	}
	
	@Override
	public boolean updatePatientMessageInfo(PatientMessage message){
		Boolean flag = false;
		String sql = "update patient_message_tb set content=?, contentType=?, fromID=?, toID=?, isRead=?,sendStatus=?,deleted=?, created=? where guid=?";
		SQLiteDatabase db = null;
		try{
			db = dbHelper.getWritableDatabase();
			SQLiteStatement stat = db.compileStatement(sql);
			stat.bindString(1, message.getContent());
			stat.bindString(2, message.getContentType());
			stat.bindString(3, message.getFromID());
			stat.bindString(4, message.getToID());
			stat.bindLong(5, message.getIsRead());
			stat.bindLong(6, message.getSendStatus());
			stat.bindLong(7, message.getDeleted());
			stat.bindLong(8, message.getTimestamp());
			stat.bindString(9, message.getGuid());
			stat.execute();
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
	public boolean updatePatientMessageInfo(List<PatientMessage> messages){
		Boolean flag = false;
		String sql = "update patient_message_tb set content=?, contentType=?, fromID=?, toID=?, isRead=?,sendStatus=?,deleted=?, created=? where guid=?";
		SQLiteDatabase db = null;
		try{
			db = dbHelper.getWritableDatabase();
			SQLiteStatement stat = db.compileStatement(sql);
			db.beginTransaction();
			for(PatientMessage message : messages){
				stat.bindString(1, message.getContent());
				stat.bindString(2, message.getContentType());
				stat.bindString(3, message.getFromID());
				stat.bindString(4, message.getToID());
				stat.bindLong(5, message.getIsRead());
				stat.bindLong(6, message.getSendStatus());
				stat.bindLong(7, message.getDeleted());
				stat.bindLong(8, message.getTimestamp());
				stat.bindString(9, message.getGuid());
				stat.execute();
				stat.clearBindings();
			}
			db.setTransactionSuccessful();
			flag = true;
		} catch(Exception e){
			//to do
			e.printStackTrace();
		} finally{
			if(db != null){
				db.endTransaction();
				db.close();
			}
		}
		return flag;
	}
	
	/**
	 * 不删除数据，将deleted 标示为"1"
	 */
	public boolean deletePatientMessageInfo(String guid) {
		Boolean flag = false;
		SQLiteDatabase db = null;
		try {
			db = dbHelper.getWritableDatabase();
			String sql = "update patient_message_tb set deleted=1 where guid=?";
			String [] args = {guid};
			db.execSQL(sql, args);
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
	public PatientMessage viewPatientMessageInfo(String guid) {
		PatientMessage message = null;
		SQLiteDatabase db = null;
		try {
			db = dbHelper.getReadableDatabase();
			String sql = "select * from patient_message_tb where guid=? and deleted=0";
			String[] params = {guid};
			Cursor cursor = db.rawQuery(sql, params);
			if (cursor.moveToNext()) {
				message = new PatientMessage();
				message.setContent(cursor.getString(cursor.getColumnIndex("content")));
				message.setGuid(cursor.getString(cursor.getColumnIndex("guid")));
				message.setFromID(cursor.getString(cursor.getColumnIndex("fromID")));
				message.setToID(cursor.getString(cursor.getColumnIndex("toID")));
				message.setTimestamp(cursor.getLong(cursor.getColumnIndex("created")));
				message.setDeleted(cursor.getInt(cursor.getColumnIndex("deleted")));
				message.setIsRead(cursor.getInt(cursor.getColumnIndex("isRead")));
				message.setSendStatus(cursor.getInt(cursor.getColumnIndex("sendStatus")));
				message.setContentType(cursor.getString(cursor.getColumnIndex("contentType")));
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (db != null)
				db.close();
		}
		return message;
	}

	@Override
	public List<PatientMessage> listLastKPatientMessage(String userId, int k) {
		List<PatientMessage> list = new ArrayList<PatientMessage>();
		SQLiteDatabase db = null;
		try {
			db = dbHelper.getReadableDatabase();
			String sql = "select * from patient_message_tb where (fromID=? or toID=?) and deleted=0 order by id DESC limit ?";
			String[] userIds = { userId, userId, Integer.toString(k)};
			Cursor cursor = db.rawQuery(sql, userIds);
			cursor.moveToLast();
			do{
				PatientMessage message = new PatientMessage();
				message.setContent(cursor.getString(cursor.getColumnIndex("content")));
				message.setFromID(cursor.getString(cursor.getColumnIndex("fromID")));
				message.setToID(cursor.getString(cursor.getColumnIndex("toID")));
				message.setId(Integer.parseInt(cursor.getString(cursor.getColumnIndex("id"))));
				message.setGuid(cursor.getString(cursor.getColumnIndex("guid")));
				message.setTimestamp(Long.parseLong(cursor.getString(cursor.getColumnIndex("created"))));
				message.setContentType(cursor.getString(cursor.getColumnIndex("contentType")));
				message.setIsRead(Integer.parseInt(cursor.getString(cursor.getColumnIndex("isRead"))));
				message.setSendStatus(Integer.parseInt(cursor.getString(cursor.getColumnIndex("sendStatus"))));
				message.setDeleted(Integer.parseInt(cursor.getString(cursor.getColumnIndex("deleted"))));
				list.add(message);
			} while(cursor.moveToPrevious());
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (db != null)
				db.close();
		}
		return list;
	}
	
	@Override
	public List<PatientMessage> linkedListLastKPatientMessage(String userId, int k) {
		List<PatientMessage> list = new LinkedList<PatientMessage>();
		SQLiteDatabase db = null;
		try {
			db = dbHelper.getReadableDatabase();
			String sql = "select * from patient_message_tb where (fromID=? or toID=?) and deleted=0 order by id DESC limit ?";
			String[] userIds = { userId, userId, Integer.toString(k) };
			Cursor cursor = db.rawQuery(sql, userIds);
			cursor.moveToLast();
			do{
				PatientMessage message = new PatientMessage();
				message.setContent(cursor.getString(cursor.getColumnIndex("content")));
				message.setFromID(cursor.getString(cursor.getColumnIndex("fromID")));
				message.setToID(cursor.getString(cursor.getColumnIndex("toID")));
				message.setId(Integer.parseInt(cursor.getString(cursor.getColumnIndex("id"))));
				message.setGuid(cursor.getString(cursor.getColumnIndex("guid")));
				message.setTimestamp(Long.parseLong(cursor.getString(cursor.getColumnIndex("created"))));
				message.setContentType(cursor.getString(cursor.getColumnIndex("contentType")));
				message.setIsRead(Integer.parseInt(cursor.getString(cursor.getColumnIndex("isRead"))));
				message.setSendStatus(Integer.parseInt(cursor.getString(cursor.getColumnIndex("sendStatus"))));
				message.setDeleted(Integer.parseInt(cursor.getString(cursor.getColumnIndex("deleted"))));
				list.add(message);
			} while(cursor.moveToPrevious());
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (db != null)
				db.close();
		}
		return list;
	}
	
	public List<PatientMessage> listLastKPatientMessageBeforeId(String userId, int k, String guid){
		List<PatientMessage> list = new ArrayList<PatientMessage>();
		SQLiteDatabase db = null;
		String sqlQuery = "select id from patient_message_tb where deleted=0 and guid=?";
		String sql = "select * from patient_message_tb where (fromID=? or toID=?) and id<? and deleted=0 order by id DESC limit ?";
		String [] ss = {guid};
		try {
			db = dbHelper.getReadableDatabase();
			db.beginTransaction();
			Cursor cursor = db.rawQuery(sqlQuery, ss);
			if(!cursor.moveToFirst()){
				throw new Exception();
			}
			int id = cursor.getInt(0);
			String[] args = { userId, userId, Integer.toString(id), Integer.toString(k)};
			cursor = db.rawQuery(sql, args);
			if(!cursor.moveToLast()){
				throw new Exception();
			}
			do{
				PatientMessage message = new PatientMessage();
				message.setContent(cursor.getString(cursor.getColumnIndex("content")));
				message.setFromID(cursor.getString(cursor.getColumnIndex("fromID")));
				message.setToID(cursor.getString(cursor.getColumnIndex("toID")));
				message.setId(Integer.parseInt(cursor.getString(cursor.getColumnIndex("id"))));
				message.setGuid(cursor.getString(cursor.getColumnIndex("guid")));
				message.setTimestamp(Long.parseLong(cursor.getString(cursor.getColumnIndex("created"))));
				message.setContentType(cursor.getString(cursor.getColumnIndex("contentType")));
				message.setIsRead(Integer.parseInt(cursor.getString(cursor.getColumnIndex("isRead"))));
				message.setSendStatus(Integer.parseInt(cursor.getString(cursor.getColumnIndex("sendStatus"))));
				message.setDeleted(Integer.parseInt(cursor.getString(cursor.getColumnIndex("deleted"))));
				list.add(message);
			} while(cursor.moveToPrevious());
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (db != null){
				db.endTransaction();
				db.close();
			}
		}
		return list;
	}

	public List<PatientMessage> listPaymentUnsuccess(){
		List<PatientMessage> list = new ArrayList<PatientMessage>();
		SQLiteDatabase db = null;
		String sql = "select * from patient_message_tb where contentType=? and sendStatus <> 1";
		String [] ss = {Common.MESSAGE_TYPE_paymentRequest};
		try {
			db = dbHelper.getReadableDatabase();
			Cursor cursor = db.rawQuery(sql, ss);
			while(cursor.moveToNext()){
				PatientMessage message = new PatientMessage();
				message.setContent(cursor.getString(cursor.getColumnIndex("content")));
				message.setFromID(cursor.getString(cursor.getColumnIndex("fromID")));
				message.setToID(cursor.getString(cursor.getColumnIndex("toID")));
				message.setId(Integer.parseInt(cursor.getString(cursor.getColumnIndex("id"))));
				message.setGuid(cursor.getString(cursor.getColumnIndex("guid")));
				message.setTimestamp(Long.parseLong(cursor.getString(cursor.getColumnIndex("created"))));
				message.setContentType(cursor.getString(cursor.getColumnIndex("contentType")));
				message.setIsRead(Integer.parseInt(cursor.getString(cursor.getColumnIndex("isRead"))));
				message.setSendStatus(Integer.parseInt(cursor.getString(cursor.getColumnIndex("sendStatus"))));
				message.setDeleted(Integer.parseInt(cursor.getString(cursor.getColumnIndex("deleted"))));
				list.add(message);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (db != null){
				db.close();
			}
		}
		return list;
	}
}
