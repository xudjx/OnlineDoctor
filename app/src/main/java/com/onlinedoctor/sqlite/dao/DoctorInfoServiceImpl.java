package com.onlinedoctor.sqlite.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

import com.onlinedoctor.pojo.Common;
import com.onlinedoctor.pojo.mine.DoctorInfo;
import com.onlinedoctor.sqlite.DBOpenHelper;
import com.onlinedoctor.sqlite.service.DoctorInfoService;

import java.util.List;

public class DoctorInfoServiceImpl implements DoctorInfoService{
	private DBOpenHelper dbHelper;
	private static final String TABLE = "doctor_info_tb";

	public DoctorInfoServiceImpl(Context context, List<String> updateSql) {
		dbHelper = DBOpenHelper.createInstance();
	}

	@Override
	public int insert(DoctorInfo dInfo) {
		long id = -1;
		SQLiteDatabase db=null;
		String sql = "insert into doctor_info_tb(" +
				"thumbnail, " +
				"avatar, " +
				"cid, " +
				"gender, " +
				"email, " +
				"birth, " +
				"addr, " +
				"clinic, " +
				"room, " +
				"rank, " +
				"intro, " +
				"time, " +
				"name, " +
				"qrcode, " +
				"auth_status, " +
				"auth_photo) " +
				"values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
		db = dbHelper.getWritableDatabase();
		SQLiteStatement stat = db.compileStatement(sql);

		stat.bindString(1, dInfo.getThumbnail());
		stat.bindString(2, dInfo.getAvatar());
		stat.bindString(3, dInfo.getCid());
		stat.bindString(4, dInfo.getGender());
		stat.bindString(5, dInfo.getEmail());
		stat.bindLong(6, dInfo.getBirth());
		stat.bindString(7, dInfo.getAddr());
		stat.bindString(8, dInfo.getClinic());
		stat.bindString(9, dInfo.getRoom());
		stat.bindString(10, dInfo.getRank());
		stat.bindString(11, dInfo.getIntro());
		stat.bindLong(12, dInfo.getTime());
		stat.bindString(13, dInfo.getName());
		stat.bindString(14, dInfo.getQrcode());
		stat.bindLong(15, dInfo.getAuth_status());
		stat.bindString(16, dInfo.getAuth_photo());
		id = stat.executeInsert();
		dInfo.setId((int)id);
		return (int)id;
	}

	@Override
	public boolean update(DoctorInfo dInfo) {
		//只更新不为空的属性，如果dInfo中有属性为空，则DB表中保持原来的属性值
		DoctorInfo oldDInfo = get(dInfo.getId());
		Boolean flag = false;
		SQLiteDatabase db = null;
		db = dbHelper.getWritableDatabase();
		ContentValues values = new ContentValues();

		values.put("thumbnail",
				dInfo.getThumbnail().isEmpty()? oldDInfo.getThumbnail():dInfo.getThumbnail());
		values.put("avatar", dInfo.getAvatar().isEmpty()? oldDInfo.getAvatar():dInfo.getAvatar());
		values.put("cid", dInfo.getCid().isEmpty()? oldDInfo.getCid():dInfo.getCid());
		//values.put("auth", dInfo.getAuth().isEmpty()?
		values.put("auth_status", dInfo.getAuth_status());
		values.put("auth_photo", dInfo.getAuth_photo().isEmpty()? oldDInfo.getAuth_photo():dInfo.getAuth_photo());
		values.put("gender", dInfo.getGender().isEmpty()? oldDInfo.getGender():dInfo.getGender());
		values.put("email", dInfo.getEmail().isEmpty()? oldDInfo.getEmail():dInfo.getEmail());
		values.put("birth", dInfo.getBirth());
		values.put("addr", dInfo.getAddr().isEmpty()? oldDInfo.getAddr():dInfo.getAddr());
		values.put("clinic", dInfo.getClinic().isEmpty()? oldDInfo.getClinic():dInfo.getClinic());
		values.put("room", dInfo.getRoom().isEmpty()? oldDInfo.getRoom():dInfo.getRoom());
		values.put("rank", dInfo.getRank().isEmpty()? oldDInfo.getRank():dInfo.getRank());
		values.put("intro", dInfo.getIntro().isEmpty()? oldDInfo.getIntro():dInfo.getIntro());
		values.put("time", dInfo.getTime());
		values.put("name", dInfo.getName().isEmpty()? oldDInfo.getIntro():dInfo.getName());
		values.put("qrcode", dInfo.getQrcode().isEmpty()?oldDInfo.getQrcode():dInfo.getQrcode());
		db.update(TABLE, values, "id = ?", new String[]{Integer.toString(dInfo.getId())});
		db.close();
		flag = true;
		return flag;
	}

	@Override
	public boolean delete(DoctorInfo dInfo) {
		Boolean flag=false;
		SQLiteDatabase db=null;
		db = dbHelper.getWritableDatabase();
		db.delete(TABLE, "id=?", new String[]{Integer.toString(dInfo.getId())});
		db.close();
		flag = true;
		return flag;
	}

	@Override
	public DoctorInfo get(int id) {
		SQLiteDatabase db = null;
		DoctorInfo dInfo = new DoctorInfo();
		db = dbHelper.getReadableDatabase();
		String sql = "select * from doctor_info_tb where id = ?";
		Cursor cursor = db.rawQuery(sql, new String[] {Integer.toString(id)});
		while(cursor.moveToNext()){
			String thumbnail = cursor.getString(cursor.getColumnIndex("thumbnail"));
			String avatar = cursor.getString(cursor.getColumnIndex("avatar"));
			String name = cursor.getString(cursor.getColumnIndex("name"));
			String cid = cursor.getString(cursor.getColumnIndex("cid"));
			//String auth = cursor.getString(cursor.getColumnIndex("auth"));
			int auth_status = cursor.getInt(cursor.getColumnIndex("auth_status"));
			String auth_photo = cursor.getString(cursor.getColumnIndex("auth_photo"));
			String gender = cursor.getString(cursor.getColumnIndex("gender"));
			Long birth = cursor.getLong(cursor.getColumnIndex("birth"));
			String email = cursor.getString(cursor.getColumnIndex("email"));
			String addr = cursor.getString(cursor.getColumnIndex("addr"));
			String clinic = cursor.getString(cursor.getColumnIndex("clinic"));
			String room = cursor.getString(cursor.getColumnIndex("room"));
			String rank = cursor.getString(cursor.getColumnIndex("rank"));
			String intro = cursor.getString(cursor.getColumnIndex("intro"));
			Long time = cursor.getLong(cursor.getColumnIndex("time"));
			String qrcode = cursor.getString(cursor.getColumnIndex("qrcode"));

			dInfo.setThumbnail(thumbnail);
			dInfo.setAvatar(avatar);
			dInfo.setName(name);
			dInfo.setCid(cid);
			//dInfo.setAuth(auth);
			dInfo.setAuth_status(auth_status);
			dInfo.setAuth_photo(auth_photo);
			dInfo.setGender(gender);
			dInfo.setBirth(birth);
			dInfo.setEmail(email);
			dInfo.setAddr(addr);
			dInfo.setClinic(clinic);
			dInfo.setRoom(room);
			dInfo.setRank(rank);
			dInfo.setIntro(intro);
			dInfo.setTime(time);
			dInfo.setQrcode(qrcode);
		}
		cursor.close();
		db.close();
		return dInfo;
	}

	@Override
	public boolean isEmpty() {
		SQLiteDatabase db = null;
		DoctorInfo dInfo = new DoctorInfo();
		db = dbHelper.getReadableDatabase();
		String sql = "select * from doctor_info_tb";
		Cursor cursor = db.rawQuery(sql,null);
		while(cursor.moveToNext()){
			return false; //表不为空
		}
		return true; //表为空
	}
}

