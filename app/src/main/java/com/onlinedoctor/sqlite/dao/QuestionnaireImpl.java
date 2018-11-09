package com.onlinedoctor.sqlite.dao;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDoneException;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;

import com.onlinedoctor.pojo.Common;
import com.onlinedoctor.pojo.tools.survey.BaseQuestionDTO;
import com.onlinedoctor.pojo.tools.survey.Questionnaire;
import com.onlinedoctor.sqlite.DBOpenHelper;
import com.onlinedoctor.util.JsonUtil;

public class QuestionnaireImpl {
	private DBOpenHelper dbHelper;
	private static final String TABLE = "questionnaire";
	private static final String SP = "@";
	public QuestionnaireImpl(Context context, List<String> updateSql) {
		dbHelper = DBOpenHelper.createInstance();
	}
	
	public long addQuestionnaire(Questionnaire q) {
		long id = -1;
		SQLiteDatabase db=null;
		String sql = "insert into "+TABLE+"(name, doctorId, questionJson, globalId, time) values(?,?,?,?,?)";
		db = dbHelper.getWritableDatabase();
		SQLiteStatement stat = db.compileStatement(sql);
		stat.bindString(1, q.getName());
		stat.bindString(2, q.getDoctorId());
		stat.bindString(3, q.getQuestionJson());
		if(q.getId() != -1) stat.bindLong(4,q.getId());
		else stat.bindNull(4);
		if(q.getTimestamp() != -1) stat.bindLong(5, q.getTimestamp());
		else stat.bindNull(5);
		id = stat.executeInsert();
		db.close();
		return id;
	}
	

	public boolean updateQuestionnaire(Questionnaire q){
		if(q.localId == -1 && q.getId() != -1){
			Questionnaire result = getQuestionnairebyGlobalId(q.getId());
			if(result != null) q.localId = result.localId;
			else  return addQuestionnaire(q) > 0;
		}
		if (q.getId() == -1 && q.localId == -1) return false;
		Boolean flag = false;
		SQLiteDatabase db = null;
		db = dbHelper.getWritableDatabase();
		ContentValues values = new ContentValues();
		
		if (q.getName() != null)values.put("name", q.getName());
		if (q.getQuestionJson() != null) values.put("questionJson", q.getQuestionJson());
		if(q.getId() !=-1)values.put("globalId", q.getId());
		if(q.getTimestamp() !=-1)values.put("time", q.getTimestamp());
		if(db.update(TABLE, values, "id = ?", new String []{Integer.toString(q.localId)}) == 1) flag=true;
		else flag = false;
		db.close();
		flag = true;
		return flag;
	}
	

	public boolean deleteQuestionnaireByGlobalId(long id) {
		Boolean flag=false;
		SQLiteDatabase db=null;
		db = dbHelper.getWritableDatabase();
		db.delete(TABLE, "globalId=?", new String[]{Long.toString(id)});
		db.close();
		flag = true;
		return flag;
	}

	public boolean deleteQuestionnaireByLocalId(long id) {
		Boolean flag=false;
		SQLiteDatabase db=null;
		db = dbHelper.getWritableDatabase();
		db.delete(TABLE, "id=?", new String[]{Long.toString(id)});
		db.close();
		flag = true;
		return flag;
	}

	public List<Questionnaire> getAllQuestionnaire() {
		List<Questionnaire> list = new ArrayList<Questionnaire>();
		SQLiteDatabase db = null;
		db = dbHelper.getReadableDatabase();
		String sql = "select * from "+TABLE;
		Cursor cursor = db.rawQuery(sql, null);
		while(cursor.moveToNext()){
			Questionnaire q = new Questionnaire();
			int localId;
			if (cursor.isNull(cursor.getColumnIndex("id"))) localId=-1;
			else localId = cursor.getInt(cursor.getColumnIndex("id"));
			String name = cursor.getString(cursor.getColumnIndex("name"));
			String questionJson = cursor.getString(cursor.getColumnIndex("questionJson"));
			int globalId;
			if(cursor.isNull(cursor.getColumnIndex("globalId"))) globalId = -1;
			else globalId = cursor.getInt(cursor.getColumnIndex("globalId"));
			long time;
			if (cursor.isNull(cursor.getColumnIndex("time"))) time=-1;
			else time = cursor.getLong(cursor.getColumnIndex("time"));
			Log.d("db",JsonUtil.objectToJson(cursor.getColumnNames()));
			Log.d("globalId", Integer.toString(globalId));
			Log.d("time", time+"");
			q.localId = localId;
			q.setId(globalId);
			q.setName(name);
			q.setQuestionJson(questionJson);
			list.add(q);
		}
		cursor.close();
		db.close();
		return list;
	}
	
	public Questionnaire getQuestionnairebyGlobalId(long id){
		if (id<0) return null;
		SQLiteDatabase db=null;
		Questionnaire q = new Questionnaire();
		String sql = "select * from "+TABLE+" where globalId= "+id;
		db = dbHelper.getReadableDatabase();
		Cursor cursor = db.rawQuery(sql, null);
		if (cursor.moveToNext())
		{
			int localId;
			if(cursor.isNull(cursor.getColumnIndex("id"))) localId=-1;
			else localId = cursor.getInt(cursor.getColumnIndex("id"));
			String name = cursor.getString(cursor.getColumnIndex("name"));
			String questionJson = cursor.getString(cursor.getColumnIndex("questionJson"));
			int globalId = cursor.getInt(cursor.getColumnIndex("globalId"));
			long time;
			if (cursor.isNull(cursor.getColumnIndex("time"))) time=-1;
			else time = cursor.getLong(cursor.getColumnIndex("time"));
			Log.d("db",JsonUtil.objectToJson(cursor.getColumnNames()));
			Log.d("globalId", Integer.toString(globalId));
			Log.d("time", time+"");
			q.localId = localId;
			q.setId(globalId);
			q.setName(name);
			q.setQuestionJson(questionJson);
			q.setTimestamp(time);
		}
		else q=null;
		cursor.close();
		return q;
		
	}
	
	public Questionnaire getQuestionnairebyLocalId(long id){
		if (id<0) return null;
		SQLiteDatabase db=null;
		Questionnaire q = new Questionnaire();
		String sql = "select * from "+TABLE+" where id= "+id;
		db = dbHelper.getReadableDatabase();
		Cursor cursor = db.rawQuery(sql, null);
		if (cursor.moveToNext())
		{
			int localId;
			if(cursor.isNull(cursor.getColumnIndex("id"))) localId=-1;
			else localId = cursor.getInt(cursor.getColumnIndex("id"));
			String name = cursor.getString(cursor.getColumnIndex("name"));
			String questionJson = cursor.getString(cursor.getColumnIndex("questionJson"));
			int globalId = cursor.isNull(cursor.getColumnIndex("globalId"))?-1:cursor.getInt(cursor.getColumnIndex("globalId"));
			long time;
			if (cursor.isNull(cursor.getColumnIndex("time"))) time=-1;
			else time = cursor.getLong(cursor.getColumnIndex("time"));
			Log.d("db",JsonUtil.objectToJson(cursor.getColumnNames()));
			Log.d("globalId", Integer.toString(globalId));
			Log.d("time", time+"");
			q.localId = localId;
			q.setId(globalId);
			q.setName(name);
			q.setQuestionJson(questionJson);
			q.setTimestamp(time);
		}
		else q=null;
		cursor.close();
		return q;
		
	}
	
	public long getLastUpdate(){
		String sql = "select max(time) from "+TABLE;
		SQLiteDatabase db=dbHelper.getReadableDatabase();
		SQLiteStatement stat = db.compileStatement(sql);
		long result =-1;
		try{
			result = stat.simpleQueryForLong();
		}catch (SQLiteDoneException e){
			result = 0;
		}
		stat.close();
		db.close();
		return result;
	}

}
