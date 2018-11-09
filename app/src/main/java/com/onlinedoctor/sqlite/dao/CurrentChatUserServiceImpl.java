package com.onlinedoctor.sqlite.dao;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

import com.onlinedoctor.pojo.chats.BriefMessagePojo;
import com.onlinedoctor.pojo.Common;
import com.onlinedoctor.sqlite.DBOpenHelper;
import com.onlinedoctor.sqlite.service.CurrentChatUserService;

public class CurrentChatUserServiceImpl implements CurrentChatUserService {

	private DBOpenHelper dbHelper;

	// updateSql 需要的时候才弄
	public CurrentChatUserServiceImpl(Context context, List<String> updateSql) {
		dbHelper = DBOpenHelper.createInstance();
	}

	@Override
	public boolean addChatUserList(List<BriefMessagePojo> messagePojos) {
		boolean flag = false;
		SQLiteDatabase db = null;
		try {
			db = dbHelper.getWritableDatabase();
			String sql = "insert into current_chat_user_message_tb(count,userId,userName,message,faceImageUrl,time,ifNoDisturbing,contentType,labels,sendStatus,pGuid,isDraft) values(?,?,?,?,?,?,?,?,?,?,?,?)";
			SQLiteStatement statement = db.compileStatement(sql);
			db.beginTransaction();
			for (BriefMessagePojo messagePojo : messagePojos) {
				statement.bindLong(1, messagePojo.getCount());
				statement.bindString(2, messagePojo.getUserId());
				statement.bindString(3, messagePojo.getUserName());
				statement.bindString(4, messagePojo.getMessage());
				statement.bindString(5, messagePojo.getFaceImageUrl());
				statement.bindLong(6, messagePojo.getTime());
				statement.bindLong(7, messagePojo.isIfNoDisturbing() ? 1 : 0);
				statement.bindString(8, messagePojo.getContentType());
				statement.bindString(9, messagePojo.getLabels());
				statement.bindLong(10, messagePojo.getSendStatus());
				statement.bindString(11, messagePojo.getpGuid());
				statement.bindLong(12, messagePojo.getIsDraft());
				if (statement.executeInsert() < 0) {
					throw new Exception();
				}
				statement.clearBindings();
			}
			db.setTransactionSuccessful();
			flag = true;
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (db != null) {
				db.endTransaction();
				db.close();
			}
		}
		return flag;
	}

	@Override
	public boolean addChatUserOne(BriefMessagePojo messagePojo) {
		boolean flag = false;
		SQLiteDatabase db = null;
		try {
			db = dbHelper.getWritableDatabase();
			String sql = "insert into current_chat_user_message_tb(count,userId,userName,message,faceImageUrl,time,ifNoDisturbing,contentType,labels,sendStatus,pGuid,isDraft) values(?,?,?,?,?,?,?,?,?,?,?,?)";
			SQLiteStatement statement = db.compileStatement(sql);
			statement.clearBindings();
			statement.bindLong(1, messagePojo.getCount());
			statement.bindString(2, messagePojo.getUserId());
			statement.bindString(3, messagePojo.getUserName());
			statement.bindString(4, messagePojo.getMessage());
			statement.bindString(5, messagePojo.getFaceImageUrl());
			statement.bindLong(6, messagePojo.getTime());
			statement.bindLong(7, messagePojo.isIfNoDisturbing() ? 1 : 0);
			statement.bindString(8, messagePojo.getContentType());
			statement.bindString(9, messagePojo.getLabels());
			statement.bindLong(10, messagePojo.getSendStatus());
			statement.bindString(11, messagePojo.getpGuid());
			statement.bindLong(12, messagePojo.getIsDraft());
			if (statement.executeInsert() < 0) {
				throw new Exception();
			}
			flag = true;
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (db != null) {
				db.close();
			}
		}
		return flag;
	}

	@Override
	public boolean updateChatUser(BriefMessagePojo messagePojo) {
		Boolean flag = false;
		SQLiteDatabase db = null;
		String sql = "update current_chat_user_message_tb set count=?,userName=?,message=?,faceImageUrl=?,time=?,ifNoDisturbing=?,contentType=?,labels=?,sendStatus=?,pGuid=?,isDraft=? where userId=?";
		try {
			db = dbHelper.getWritableDatabase();
			SQLiteStatement statement = db.compileStatement(sql);
			statement.bindLong(1, messagePojo.getCount());
			statement.bindString(2, messagePojo.getUserName());
			statement.bindString(3, messagePojo.getMessage());
			statement.bindString(4, messagePojo.getFaceImageUrl());
			statement.bindLong(5, messagePojo.getTime());
			statement.bindLong(6, messagePojo.isIfNoDisturbing() ? 1 : 0);
			statement.bindString(7, messagePojo.getContentType());
			statement.bindString(8, messagePojo.getLabels());
			statement.bindLong(9, messagePojo.getSendStatus());
			statement.bindString(10, messagePojo.getpGuid());
			statement.bindLong(11, messagePojo.getIsDraft());
			statement.bindString(12, messagePojo.getUserId());
			statement.execute();
			flag = true;
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (db != null) {
				db.close();
			}
		}
		return flag;
	}

	@Override
	public boolean deleteChatUser(String userId) {
		Boolean flag = false;
		SQLiteDatabase db = null;
		String sql = "delete from current_chat_user_message_tb where userId=?";
		String[] args = { userId };
		try {
			db = dbHelper.getWritableDatabase();
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
	public List<BriefMessagePojo> getAllChatUsersInTimeOrder(){
		List<BriefMessagePojo> list = new ArrayList<BriefMessagePojo>();
		SQLiteDatabase db = null;
		boolean flag = false;
		try {
			db = dbHelper.getReadableDatabase();
			String sql = "select * from current_chat_user_message_tb order by time DESC";
			Cursor cursor = db.rawQuery(sql, null);
			while (cursor.moveToNext()) {
				BriefMessagePojo messagePojo = new BriefMessagePojo();
				messagePojo.setCount(Integer.parseInt(cursor.getString(cursor.getColumnIndex("count"))));
				messagePojo.setUserId(cursor.getString(cursor.getColumnIndex("userId")));
				messagePojo.setUserName(cursor.getString(cursor.getColumnIndex("userName")));
				messagePojo.setMessage(cursor.getString(cursor.getColumnIndex("message")));
				messagePojo.setTime(cursor.getLong(cursor.getColumnIndex("time")));
				messagePojo.setFaceImageUrl(cursor.getString(cursor.getColumnIndex("faceImageUrl")));
				messagePojo.setIfNoDisturbing(cursor.getInt(cursor.getColumnIndex("ifNoDisturbing")) == 0 ? false
						: true);
				messagePojo.setContentType(cursor.getString(cursor.getColumnIndex("contentType")));
				messagePojo.setLabels(cursor.getString(cursor.getColumnIndex("labels")));
				messagePojo.setSendStatus(cursor.getInt(cursor.getColumnIndex("sendStatus")));
				messagePojo.setpGuid(cursor.getString(cursor.getColumnIndex("pGuid")));
				messagePojo.setIsDraft(cursor.getInt(cursor.getColumnIndex("isDraft")));
				list.add(messagePojo);
			}
			flag = true;
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (db != null)
				db.close();
		}
		return flag ? list : null;
	}
	
	@Override
	public BriefMessagePojo getChatUserByUserId(String userId) {
		SQLiteDatabase db = null;
		BriefMessagePojo messagePojo = null;
		try {
			db = dbHelper.getReadableDatabase();
			String sql = "select * from current_chat_user_message_tb where userId=?";
			String [] userIds = {userId};
			Cursor cursor = db.rawQuery(sql, userIds);
			if(cursor.moveToFirst()){
				messagePojo = new BriefMessagePojo();
				messagePojo.setCount(Integer.parseInt(cursor.getString(cursor.getColumnIndex("count"))));
				messagePojo.setUserId(cursor.getString(cursor.getColumnIndex("userId")));
				messagePojo.setUserName(cursor.getString(cursor.getColumnIndex("userName")));
				messagePojo.setMessage(cursor.getString(cursor.getColumnIndex("message")));
				messagePojo.setTime(cursor.getLong(cursor.getColumnIndex("time")));
				messagePojo.setFaceImageUrl(cursor.getString(cursor.getColumnIndex("faceImageUrl")));
				messagePojo.setIfNoDisturbing(cursor.getInt(cursor.getColumnIndex("ifNoDisturbing")) == 0 ? false
						: true);
				messagePojo.setContentType(cursor.getString(cursor.getColumnIndex("contentType")));
				messagePojo.setLabels(cursor.getString(cursor.getColumnIndex("labels")));
				messagePojo.setSendStatus(cursor.getInt(cursor.getColumnIndex("sendStatus")));
				messagePojo.setpGuid(cursor.getString(cursor.getColumnIndex("pGuid")));
				messagePojo.setIsDraft(cursor.getInt(cursor.getColumnIndex("isDraft")));
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (db != null)
				db.close();
		}
		return messagePojo;
	}
	
	@Override
	public List<BriefMessagePojo> listTopKBriefMessage(long k){
		List<BriefMessagePojo> list = new ArrayList<BriefMessagePojo>();
		SQLiteDatabase db = null;
		BriefMessagePojo messagePojo = null;
		try {
			db = dbHelper.getReadableDatabase();
			String sql = "select * from current_chat_user_message_tb order by time DESC limit ?";
			String[] args = {Long.toString(k)};
			Cursor cursor = db.rawQuery(sql, args);
			while (cursor.moveToNext()) {
				messagePojo = new BriefMessagePojo();
				messagePojo.setCount(Integer.parseInt(cursor.getString(cursor.getColumnIndex("count"))));
				messagePojo.setUserId(cursor.getString(cursor.getColumnIndex("userId")));
				messagePojo.setUserName(cursor.getString(cursor.getColumnIndex("userName")));
				messagePojo.setMessage(cursor.getString(cursor.getColumnIndex("message")));
				messagePojo.setTime(cursor.getLong(cursor.getColumnIndex("time")));
				messagePojo.setFaceImageUrl(cursor.getString(cursor.getColumnIndex("faceImageUrl")));
				messagePojo.setIfNoDisturbing(cursor.getInt(cursor.getColumnIndex("ifNoDisturbing")) == 0 ? false
						: true);
				messagePojo.setContentType(cursor.getString(cursor.getColumnIndex("contentType")));
				messagePojo.setLabels(cursor.getString(cursor.getColumnIndex("labels")));
				messagePojo.setSendStatus(cursor.getInt(cursor.getColumnIndex("sendStatus")));
				messagePojo.setpGuid(cursor.getString(cursor.getColumnIndex("pGuid")));
				messagePojo.setIsDraft(cursor.getInt(cursor.getColumnIndex("isDraft")));
				list.add(messagePojo);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (db != null)
				db.close();
		}
		return list;
	}
}
