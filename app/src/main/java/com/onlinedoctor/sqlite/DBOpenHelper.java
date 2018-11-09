package com.onlinedoctor.sqlite;

import java.util.List;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.onlinedoctor.application.MyApplication;
import com.onlinedoctor.log.Logger;
import com.onlinedoctor.pojo.Common;
import com.onlinedoctor.util.SharedpreferenceManager;

public class DBOpenHelper extends SQLiteOpenHelper {

	private static Context context = MyApplication.context;
	private static int version = Common.Version;

	private DBOpenHelper(Context mContext, String dBName, int mVersion) {
		super(mContext, dBName, null, mVersion);
	}

	public static DBOpenHelper createInstance(){
		String dBName = "com.onlinedoctor.db" + SharedpreferenceManager.getInstance().getOne("keySid");
		DBOpenHelper helper = new DBOpenHelper(context, dBName, version);
		Logger.i("DBOpenHelper", "Create OpenHelper" + dBName);
		return helper;
	}

	/**
	 * It will only be called once, after the DB created, it will never be called
	 * DB 的建表操作都应该在这个函数里进行
	 */
	@Override
	public void onCreate(SQLiteDatabase db) {
		String patient_message_tb = "create table patient_message_tb(id integer primary key autoincrement, guid varchar(128) unique, content text, contentType varchar(20), fromID varchar(128), toID varchar(128), isRead integer, sendStatus integer, deleted integer, created timestamp)";
		String patient_info_tb = "create table patient_info_tb(id integer primary key autoincrement, patientId varchar(128) unique, name varchar(20), gender varchar(10), birthday integer, registerTime integer, lastTime integer, thumbnail varchar(254), avatar varchar(254), phone varchar(30), pinyin varchar(20), comment text)";
		String new_patient_info_tb = "create table new_patient_info_tb(id integer primary key autoincrement, patientId varchar(128) unique, name varchar(20), gender varchar(10), followTime integer, thumbnail varchar(254), status integer, isChecked integer)";
		String patient_doctor_relation_tb = "create table patient_doctor_relation_tb(id integer primary key autoincrement, patientId varchar(128), doctorId varchar(128), time integer, status integer)";
		String record_tb = "create table record_tb(id integer primary key autoincrement, patientId varchar(128), doctorId varchar(128), thumbnail text, recordPic text, recordType integer, created timestamp)";
		String label_tb = "create table label_tb(id integer primary key autoincrement, labelName text, labelColor integer)";
		String patient_label_relation_tb = "create table patient_label_relation_tb(id integer primary key autoincrement, patientId varchar(128), labelId integer, status integer)";

		String current_chat_user_message_tb = "create table current_chat_user_message_tb(id integer primary key autoincrement,count integer,userId varchar(128) unique, userName varchar(128),message varchar(50),faceImageUrl varchar(254),time integer, ifNoDisturbing integer, contentType text, labels text, sendStatus integer, pGuid text, isDraft integer)";
		db.execSQL(patient_message_tb);
		db.execSQL(patient_info_tb);
		db.execSQL(new_patient_info_tb);
		db.execSQL(patient_doctor_relation_tb);
		db.execSQL(record_tb);
		db.execSQL(label_tb);
		db.execSQL(patient_label_relation_tb);
		db.execSQL(current_chat_user_message_tb);
		
		/*Following are added by Dongsheng Wei*/
		//fast response
		String fast_response_tb = "create table fast_response_tb("
				+"id integer primary key autoincrement, "
				+"msg text)";
		db.execSQL(fast_response_tb);
		
		//diesase survey
		String disease_survey_title_tb = "create table disease_survey_title_tb("
				+"id integer primary key autoincrement,"
				+"title text,"
				+"abstractStr text)";
		db.execSQL(disease_survey_title_tb);
		String disease_survey_questions_tb = "create table disease_survey_questions_tb("
				+"id integer primary key autoincrement,"
				+"type integer,"
				+"question text,"
				+"options text default null,"
				+"title_id integer)";
		db.execSQL(disease_survey_questions_tb);

		//自定义收费
		String self_defined_fees_tb = "create table self_defined_fees_tb("
				+"id integer primary key autoincrement,"
				+"title text default null,"
				+"description text default null,"
				+"fee text default null,"
				+"update_time bigint default -1,"
				+"global_id integer default -1)";
		db.execSQL(self_defined_fees_tb);
		
		String create_questionnaire_sql = "create table `questionnaire` ("
				+ "id integer primary key autoincrement,"
				+ "name varchar(50),"
				+ "doctorId varchar(50),"
				+ "questionJson text,"
				+ "globalId integer,"
				+ "time timestamp)";
		
		db.execSQL(create_questionnaire_sql);

		//我的基本信息
		String doctor_info_tb = "create table `doctor_info_tb` ("
				+ "id integer primary key autoincrement,"
				+ "thumbnail text null default null,"
				+ "avatar text null default null,"
				+ "name text null default null,"
				+ "cid text null default null,"
				+ "qrcode text null default null,"
				+ "gender text null default null,"
				+ "birth bigint null default -1,"
				+ "email text null default null,"
				+ "addr text null default null,"
				+ "clinic text null default null,"
				+ "room text null default null,"
				+ "rank text null default null,"
				+ "intro text null default null,"
				+ "auth_status integer null default -1,"
				+ "auth_photo text null default null,"
				+ "time bigint null default -1"
				+ ")";
		db.execSQL(doctor_info_tb);

		// 我的钱包
		String my_wallet_tb = "create table `my_wallet_tb` ("
				+ "id integer primary key autoincrement,"
				+ "total float null default 0,"
				+ "available float null default 0,"
				+ "unavailable float null default 0"
				+ ")";
		db.execSQL(my_wallet_tb);

		// version = 2，新增表的设计
		if(Common.IsUploadToQiniu){
			String prescription_tb = "create table 'prescription_tb' ("
					+ "id integer primary key autoincrement,"
					+ "global_id integer unique,"
					+ "name varchar(128),"
					+ "detail text null default null,"
					+ "price integer,"
					+ "doctor_id varchar(128),"
					+ "type integer,"
					+ "create_time timestamp,"
					+ "is_deleted integer,"
					+ "remark text,"
					+ "prescription_imgs text,"
					+ "patient_id varchar(128)"
					+ ")";
			db.execSQL(prescription_tb);
		}
	}


	/**
	 * When current DB Version is different from the version created by
	 * DBOpenHelper. This method will trigger.
	 * 数据库版本升级从2 开始，依次进行类推。
	 * 数据库的升级操作要迭代更新，即每次数据库升级的操作在switch case中写，并且是相对上一个version的修改！！！
	 */
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.i("Version: old", String.valueOf(oldVersion));
		Log.i("Version: new", String.valueOf(newVersion));

		for(int version = oldVersion; version <= newVersion; version++){
			switch (version){
				/**
				 * version =2 的数据库表更新操作：增表、修改表之类的
				 */
				case 2:{
					String prescription_tb = "create table 'prescription_tb' ("
							+ "id integer primary key autoincrement,"
							+ "global_id integer unique,"
							+ "name varchar(128),"
							+ "detail text null default null,"
							+ "price integer,"
							+ "doctor_id varchar(128),"
							+ "type integer,"
							+ "create_time timestamp,"
							+ "is_deleted integer,"
							+ "remark text,"
							+ "prescription_imgs text,"
							+ "patient_id varchar(128)"
							+ ")";
					db.execSQL(prescription_tb);
					break;
				}
				default:
					break;
			}
		}
	}

	/*
	 * 生成连续的类似于 ?, ?, ? 的符号占位符
	 */
	public static String makePlaceHolders(int len) {
		if (len < 1) {
			return null;
		} else {
			StringBuilder sb = new StringBuilder(len * 2 - 1);
			sb.append("?");
			for (int i = 1; i < len; i++) {
				sb.append(",?");
			}
			return sb.toString();
		}
	}
}
