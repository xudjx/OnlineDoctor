package com.onlinedoctor.sqlite.dao;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

import com.onlinedoctor.pojo.Common;
import com.onlinedoctor.pojo.patient.Label;
import com.onlinedoctor.sqlite.DBOpenHelper;
import com.onlinedoctor.sqlite.service.LabelService;

public class LabelServiceImpl implements LabelService{

	private DBOpenHelper dbHelper;
	
	public LabelServiceImpl(Context context, List<String> updateSql){
		dbHelper = DBOpenHelper.createInstance();
	}

	@Override
	public int addLabelOne(Label l) {
		int flag = -1;
		SQLiteDatabase db = null;
		String sql = "insert into label_tb(labelName, labelColor) values(?, ?)";
		String sqlQuery = "select id from label_tb where labelName = ?";
		String [] ss = {l.getLabelName()};
		try{
			db = dbHelper.getWritableDatabase();
			//如果名字相同，则不允许添加
			db.beginTransaction();
			Cursor cursor = db.rawQuery(sqlQuery, ss);
			if(cursor.moveToFirst()){
				flag = -2;
				throw new Exception();
			}
			SQLiteStatement stat = db.compileStatement(sql);
			stat.bindString(1, l.getLabelName());
			stat.bindLong(2, l.getLabelColor());
			long newLabelId = stat.executeInsert();
			if(newLabelId < 0){
				throw new Exception();
			}
			l.setId(newLabelId);
			db.setTransactionSuccessful();
			flag = 0;
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
	public List<Label> listLabels() {
		List<Label> list = new ArrayList<Label>();
		SQLiteDatabase db = null;
		String sql = "select id, labelName, labelColor from label_tb";
		try{
			db = dbHelper.getReadableDatabase();
			Cursor cursor = db.rawQuery(sql, null);
			while(cursor.moveToNext()){
				Label label = new Label();
				label.setId(cursor.getLong(0));
				label.setLabelName(cursor.getString(1));
				label.setLabelColor(cursor.getInt(2));
				list.add(label);
			}
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			if(db != null){
				db.close();
			}
		}
		return list;
	}
	
	@Override
	public Label getLabelById(long labelId){
		Label label = null;
		SQLiteDatabase db = null;
		String sql = "select id, labelName, labelColor from label_tb where id=?";
		String [] args = {Long.toString(labelId)};
		try{
			db = dbHelper.getReadableDatabase();
			Cursor cursor = db.rawQuery(sql, args);
			if(!cursor.moveToFirst()){
				throw new Exception();
			}
			label = new Label();
			label.setId(cursor.getLong(0));
			label.setLabelName(cursor.getString(1));
			label.setLabelColor(cursor.getInt(2));
		} catch(Exception e){
			e.printStackTrace();
		} finally{
			if(db != null){
				db.close();
			}
		}
		return label;
	}
	@Override
	public boolean deleteLabelByName(String name){
		boolean flag = false;
		SQLiteDatabase db = null;
		try{
			db = dbHelper.getWritableDatabase();
			String [] ss = {name};
			flag = (db.delete("label_tb", "labelName=?", ss) > 0);
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
	public boolean deleteLabelById(long id){
		boolean flag = false;
		SQLiteDatabase db = null;
		try{
			db = dbHelper.getWritableDatabase();
			String [] ss = {String.valueOf(id)};
			flag = (db.delete("label_tb", "id=?", ss) > 0);
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			if(db != null){
				db.close();
			}
		}
		return flag;
	}
	
	/*
	 * -1: 未知错误
	 * -2: 名字重复
	 * -3: 名字已存在
	 * 
	 */
	@Override
	public int updateLabelNameById(long id, String newName){
		int flag = -1;
		SQLiteDatabase db = null;
		String sqlQuery1 = "select labelName from label_tb where id = ?";
		String sqlQuery2 = "select id from label_tb where labelName = ?";
		String [] ss1 = {String.valueOf(id)};
		ContentValues cv = new ContentValues();
		cv.put("labelName", newName);
		try{
			db = dbHelper.getWritableDatabase();
			db.beginTransaction();
			Cursor cursor1 = db.rawQuery(sqlQuery1, ss1);
			if(!cursor1.moveToFirst()){
				throw new Exception();
			}
			String oldName = cursor1.getString(0);
			if(oldName.equals(newName)){
				flag = -2;
				throw new Exception();
			}
			String [] ss2 = {newName};
			Cursor cursor2 = db.rawQuery(sqlQuery2, ss2);
			if(cursor2.moveToFirst()){
				flag = -3;
				throw new Exception();
			}
			String [] ss = {String.valueOf(id)};
			int ret = db.update("label_tb", cv, "id=?", ss);
			if(ret > 0){
				flag = 0;
			}
			else{
				throw new Exception();
			}
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
	public boolean updateLabel(long id, Label label){
		Boolean flag = false;
		String sql = "update label_tb set labelName=?, labelColor=? where id=?";
		SQLiteDatabase db = null;
		try{
			db = dbHelper.getWritableDatabase();
			SQLiteStatement stat = db.compileStatement(sql);
			stat.bindString(1, label.getLabelName());
			stat.bindLong(2, label.getLabelColor());
			stat.bindLong(3, label.getId());
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
}
