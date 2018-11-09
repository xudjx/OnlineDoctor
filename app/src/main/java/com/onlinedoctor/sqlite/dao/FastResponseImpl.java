package com.onlinedoctor.sqlite.dao;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;

import com.onlinedoctor.pojo.Common;
import com.onlinedoctor.pojo.tools.FastResponseMessage;
import com.onlinedoctor.sqlite.DBOpenHelper;
import com.onlinedoctor.sqlite.service.FastResponseService;
import com.onlinedoctor.util.JsonUtil;

public class FastResponseImpl implements FastResponseService {

	private DBOpenHelper dbHelper;
	private static final String TABLE = "fast_response_tb";
	public FastResponseImpl(Context context, List<String> updateSql) {
		dbHelper = DBOpenHelper.createInstance();
	}

	@Override
	public long addFastResponseItem(String msg) {
		long id = -1;
		SQLiteDatabase db=null;
		String sql = "insert into fast_response_tb(msg) values(?)";
		db = dbHelper.getWritableDatabase();
		SQLiteStatement stat = db.compileStatement(sql);
		stat.bindString(1, msg);
		id = stat.executeInsert();
		return id;
	}

	@Override
	public boolean deleteFastResponseItem(FastResponseMessage frm) {
		Boolean flag=false;
		SQLiteDatabase db=null;
		db = dbHelper.getWritableDatabase();
		db.delete(TABLE, "id=?", new String[]{Integer.toString(frm.getId())});
		db.close();
		flag = true;
		return flag;
	}

	@Override
	public boolean updateFastResponseItem(FastResponseMessage frm) {
		Boolean flag=false;
		SQLiteDatabase db=null;
		db = dbHelper.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put("msg", frm.getMsg());
		db.update(TABLE, values, "id=?", new String[]{Integer.toString(frm.getId())});
		db.close();
		flag = true;
		return flag;
	}

	@Override
	public List<FastResponseMessage> getAllFastResponses() {
		List<FastResponseMessage> list = new ArrayList<FastResponseMessage>();
		SQLiteDatabase db = null;
		db = dbHelper.getReadableDatabase();
		String sql = "select * from fast_response_tb";
		Cursor cursor = db.rawQuery(sql, null);
		while(cursor.moveToNext()){
			int id = cursor.getInt(cursor.getColumnIndex("id"));
			
			Log.d("db",JsonUtil.objectToJson(cursor.getColumnNames()));
			Log.d("id", Integer.toString(id));
			String msg = cursor.getString(cursor.getColumnIndex("msg"));
			Log.d("msg",msg);
			FastResponseMessage frm = new FastResponseMessage(id, msg);
			list.add(frm);
		}
		cursor.close();
		db.close();
		return list;
	}
}
