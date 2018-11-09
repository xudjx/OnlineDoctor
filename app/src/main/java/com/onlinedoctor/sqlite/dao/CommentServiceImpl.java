package com.onlinedoctor.sqlite.dao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.onlinedoctor.pojo.Common;
import com.onlinedoctor.sqlite.DBOpenHelper;
import com.onlinedoctor.sqlite.service.CommentService;

public class CommentServiceImpl implements CommentService {

	private DBOpenHelper dbHelper;

	// updateSql 需要的时候才弄
	public CommentServiceImpl(Context context, List<String> updateSql) {
		dbHelper = DBOpenHelper.createInstance();
	}
	
	@Override
	public boolean addCommentInfo(Object[] params) {
		Boolean flag=false;
		SQLiteDatabase db=null;
		try {
			db=dbHelper.getWritableDatabase();
			String sql="insert into comment_tb(comment_id, patient_id,doctor_id, abstract, content, lastupdate) values(?,?,?,?,?,?)";
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
	public boolean updateCommentInfo(Object[] params) {
		Boolean flag=false;
		SQLiteDatabase db=null;
		try {
			db=dbHelper.getWritableDatabase();
			String sql="update comment_tb set comment_id=?,patient_id=?,doctor_id=?,abstract=?,content=?,lastupdate=? where id=?";
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
	public boolean deleteCommentInfo(Object[] params) {
		Boolean flag=false;
		SQLiteDatabase db=null;
		try {
			db=dbHelper.getWritableDatabase();
			String sql="delete from comment_tb where id=?";
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
	public List<Map<String, String>> listCommentInfoByDoctorId(
			String[] selectionArgs) {
		List<Map<String, String>> list=new ArrayList<Map<String,String>>();
		SQLiteDatabase db=null;
		try {
			db=dbHelper.getWritableDatabase();
			String sql="select * from comment_tb where doctor_id=?";
			Cursor cursor=db.rawQuery(sql, selectionArgs);
			//获取当前库表中列的个数
			int columns=cursor.getColumnCount();
			while (cursor.moveToNext()) {
				Map<String, String> map=new HashMap<String,String>();
				for(int i=0;i<columns;i++){
					String col_name=cursor.getColumnName(i);
					String col_value=cursor.getString(cursor.getColumnIndex(col_name));
					if(col_value==null)
						col_value="";
					map.put(col_name, col_value);
				}
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
	public List<Map<String, String>> listCommentInfoByTime(
			String[] selectionArgs) {
		List<Map<String, String>> list=new ArrayList<Map<String,String>>();
		SQLiteDatabase db=null;
		try {
			db=dbHelper.getWritableDatabase();
			String sql="select * from comment_tb where lastupdate > ?";
			Cursor cursor=db.rawQuery(sql, selectionArgs);
			//获取当前库表中列的个数
			int columns=cursor.getColumnCount();
			while (cursor.moveToNext()) {
				Map<String, String> map=new HashMap<String,String>();
				for(int i=0;i<columns;i++){
					String col_name=cursor.getColumnName(i);
					String col_value=cursor.getString(cursor.getColumnIndex(col_name));
					if(col_value==null)
						col_value="";
					map.put(col_name, col_value);
				}
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
	public Map<String, String> viewCommentInfo(String[] selectionArgs) {
		Map<String, String> map=new HashMap<String, String>();
		SQLiteDatabase db=null;
		try {
			db=dbHelper.getWritableDatabase();
			String sql="select * from comment_tb where id=?";
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
	public boolean deleteAllComment() {
		Boolean flag=false;
		SQLiteDatabase db=null;
		try {
			db=dbHelper.getWritableDatabase();
			String sql="delete from comment_tb";
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
