package com.onlinedoctor.sqlite.dao;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

import com.onlinedoctor.pojo.Common;
import com.onlinedoctor.pojo.mine.SelfDefinedFee;
import com.onlinedoctor.sqlite.DBOpenHelper;
import com.onlinedoctor.sqlite.service.SelfDefinedFeesService;

public class SelfDefinedFeesImpl implements SelfDefinedFeesService {
	private DBOpenHelper dbHelper;
	private static final String TABLE = "self_defined_fees_tb";
	
	public SelfDefinedFeesImpl(Context context, List<String> updateSql) {
		dbHelper = DBOpenHelper.createInstance();
	}

	@Override
	public long add(SelfDefinedFee sdf) {
		long id = -1;
		SQLiteDatabase db=null;
		String sql = "insert into self_defined_fees_tb(title, description, fee, global_id, update_time) values(?,?,?,?,?)";
		db = dbHelper.getWritableDatabase();
		SQLiteStatement stat = db.compileStatement(sql);
		stat.bindString(1, sdf.getTitle());
		stat.bindString(2, sdf.getDescription());
		stat.bindString(3, sdf.getFee());
		stat.bindLong(4, sdf.getGlobal_id());
		stat.bindLong(5, sdf.getUpdate_time());
		id = stat.executeInsert();
		sdf.setId((int)id);
		return id;
	}

	@Override
	public boolean update(SelfDefinedFee sdf) {
		Boolean flag = false;
		SQLiteDatabase db = null;
		db = dbHelper.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put("title", sdf.getTitle());
		values.put("description", sdf.getDescription());
		values.put("fee", sdf.getFee());
		values.put("global_id", sdf.getGlobal_id());
		values.put("update_time", sdf.getUpdate_time());
		db.update(TABLE, values, "id = ?", new String []{Integer.toString(sdf.getId())});
		db.close();
		flag = true;
		return flag;
	}

	@Override
	public boolean delete(SelfDefinedFee sdf) {
		Boolean flag=false;
		SQLiteDatabase db=null;
		db = dbHelper.getWritableDatabase();
		db.delete(TABLE, "id=?", new String[]{Integer.toString(sdf.getId())});
		db.close();
		flag = true;
		return flag;
	}

	@Override
	public List<SelfDefinedFee> getAll() {
		List<SelfDefinedFee> list = new ArrayList<SelfDefinedFee>();
		SQLiteDatabase db = null;
		db = dbHelper.getReadableDatabase();
		String sql = "select * from self_defined_fees_tb";
		Cursor cursor = db.rawQuery(sql, null);
		while(cursor.moveToNext()){
			int id = cursor.getInt(cursor.getColumnIndex("id"));
			String title = cursor.getString(cursor.getColumnIndex("title"));
			String description = cursor.getString(cursor.getColumnIndex("description"));
			String fee = cursor.getString(cursor.getColumnIndex("fee"));
			long global_id = cursor.getLong(cursor.getColumnIndex("global_id"));
			long update_time = cursor.getLong(cursor.getColumnIndex("update_time"));
			SelfDefinedFee sdf = new SelfDefinedFee(id, title, description, fee, global_id, update_time);
			//Log.d("SelfDefinedFeeImpl: global_id", Long.toString(global_id));
			list.add(sdf);
		}
		cursor.close();
		db.close();
		return list;
	}

	@Override
	public boolean clean() {
		Boolean flag=false;
		SQLiteDatabase db=null;
		db = dbHelper.getWritableDatabase();
		db.execSQL("delete from "+ TABLE);
		db.close();
		flag = true;
		return flag;
	}

	@Override
	public SelfDefinedFee queryGlobalId(long globalId) {
		SQLiteDatabase db = null;
		SelfDefinedFee sdf = null;
		db = dbHelper.getReadableDatabase();
		String sql = "select * from self_defined_fees_tb where global_id = ?";
		Cursor cursor = db.rawQuery(sql, new String[] {Long.toString(globalId)});
		while(cursor.moveToNext()){
			int id = cursor.getInt(cursor.getColumnIndex("id"));
			String title = cursor.getString(cursor.getColumnIndex("title"));
			String description = cursor.getString(cursor.getColumnIndex("description"));
			String fee = cursor.getString(cursor.getColumnIndex("fee"));
			long global_id = cursor.getLong(cursor.getColumnIndex("global_id"));
			long update_time = cursor.getLong(cursor.getColumnIndex("update_time"));
			sdf = new SelfDefinedFee(id, title, description, fee, global_id, update_time);
		}
		cursor.close();
		db.close();
		return sdf;
	}

	@Override
	public SelfDefinedFee queryId(int id) {
		SQLiteDatabase db = null;
		SelfDefinedFee sdf = null;
		db = dbHelper.getReadableDatabase();
		String sql = "select * from self_defined_fees_tb where id = ?";
		Cursor cursor = db.rawQuery(sql, new String[] {Integer.toString(id)});
		while(cursor.moveToNext()){
			String title = cursor.getString(cursor.getColumnIndex("title"));
			String description = cursor.getString(cursor.getColumnIndex("description"));
			String fee = cursor.getString(cursor.getColumnIndex("fee"));
			long global_id = cursor.getLong(cursor.getColumnIndex("global_id"));
			long update_time = cursor.getLong(cursor.getColumnIndex("update_time"));
			sdf = new SelfDefinedFee();
			sdf.setDescription(description);
			sdf.setFee(fee);
			sdf.setGlobal_id(global_id);
			sdf.setId(id);
			sdf.setTitle(title);
			sdf.setUpdate_time(update_time);
		}
		cursor.close();
		db.close();
		return sdf;
	}
	
//	public long getLastUpdate(){
//		String sql = "select max(update_time) from "+TABLE;
//		SQLiteDatabase db=dbHelper.getReadableDatabase();
//		SQLiteStatement stat = db.compileStatement(sql);
//		long result =-1;
//		try{
//			result = stat.simpleQueryForLong();
//		}catch (SQLiteDoneException e){
//			result = 0;
//		}
//		stat.close();
//		db.close();
//		return result;
//		
//	}

}
