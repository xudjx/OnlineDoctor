package com.onlinedoctor.test;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;

import com.onlinedoctor.activity.R;
import com.onlinedoctor.pojo.Common;
import com.onlinedoctor.pojo.patient.Patient;
import com.onlinedoctor.pojo.patient.PatientDoctorRel;
import com.onlinedoctor.sqlite.dao.PatientDocRelationServiceImpl;
import com.onlinedoctor.sqlite.dao.PatientInfoServiceImpl;
import com.onlinedoctor.util.BitmapUtil;
import com.onlinedoctor.util.Pinyin;
import com.onlinedoctor.util.SharedpreferenceManager;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.widget.ImageView;

public class InitTest {
	
	Context mContext;

	public InitTest(Context mContext) {
		this.mContext = mContext;
	}
	
	private void initSharedPrefernce(){
		SharedpreferenceManager spManager = SharedpreferenceManager.getInstance();
		HashMap<String, String> map = new HashMap<String, String>();
		map.put("keySid", "dtest");
		map.put("keyPassword", "123456");
		spManager.set(map);
	}
	
	private void initPatient(){
		ArrayList<Patient> pList = new ArrayList<Patient>();
		Patient p1 = new Patient(), p2 = new Patient();
		p1.setPatientId("p1"); p1.setName("周大仙"); p1.setGender("M"); p1.setBirthday(567964800);
		p1.setRegisterTime(1430323200); p1.setLastTime(1430582400); p1.setThumbnail(Common.sdPicSave + File.separator + "wumeiniang.jpg");
		p1.setAvatar(Common.sdPicSave + File.separator + "wumeiniang.jpg"); p1.setPhone("13512345678"); p1.setPinyin(Pinyin.convertToPinyin(p1.getName()));
		p2.setPatientId("p2"); p2.setName("武媚娘"); p2.setGender("F"); p2.setBirthday(-1062576000);
		p2.setRegisterTime(1425139200); p2.setLastTime(1430409600); p2.setThumbnail(Common.sdPicSave + File.separator + "wumeiniang2.jpg");
		p2.setAvatar(Common.sdPicSave + File.separator + "wumeiniang2.jpg"); p2.setPhone("13612345678"); p2.setPinyin(Pinyin.convertToPinyin(p2.getName()));
		
		pList.add(p1);
		pList.add(p2);
		
		PatientInfoServiceImpl pImpl = new PatientInfoServiceImpl(mContext, null);
		pImpl.addPatientInfo(pList);
		
		PatientDocRelationServiceImpl pdrImpl = new PatientDocRelationServiceImpl(mContext, null);
		PatientDoctorRel pdr1 = new PatientDoctorRel(), pdr2 = new PatientDoctorRel();
		pdr1.doctorId = "dtest";
		pdr1.patientId = "p1";
		pdr1.time = 1430582400;
		pdr1.status = 1;
		
		pdr2.doctorId = "dtest";
		pdr2.patientId = "p2";
		pdr2.time = 1430409600;
		pdr2.status = 1;
		
		pdrImpl.addRelationInfoOne(pdr1);
		pdrImpl.addRelationInfoOne(pdr2);
	}
	
	private void savePic(){
		File file = new File(Common.sdPicSave + File.separator);
		file.mkdirs();
		Bitmap bitmap = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.defaulthead);
		String imgPathString = Common.sdPicSave + File.separator + "wumeiniang.jpg";
		File bitmapFile = new File(imgPathString);
		try {
			bitmapFile.createNewFile();
			FileOutputStream fos = new FileOutputStream(bitmapFile);
			bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
			fos.flush();
			fos.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		Bitmap bitmap2 = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.login_default_avatar);
		String imgPathString2 = Common.sdPicSave + File.separator + "wumeiniang2.jpg";
		File bitmapFile2 = new File(imgPathString2);
		try {
			bitmapFile2.createNewFile();
			FileOutputStream fos2 = new FileOutputStream(bitmapFile2);
			bitmap2.compress(Bitmap.CompressFormat.JPEG, 100, fos2);
			fos2.flush();
			fos2.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void testBitmapStringConvert(ImageView iv){
		Bitmap bitmap = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.defaulthead);
		String tmp = BitmapUtil.bitmapToString(bitmap);
		Log.d("bitmap", tmp);
		Bitmap bitmap2 = BitmapUtil.stringToBitmap(tmp);
		iv.setImageBitmap(bitmap2);
	}
	
	public void test(){
		initSharedPrefernce();
		savePic();
		initPatient();
	}

}

