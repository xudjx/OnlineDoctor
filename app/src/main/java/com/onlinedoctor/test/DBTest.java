package com.onlinedoctor.test;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import android.content.Context;
import android.util.Log;

import com.onlinedoctor.pojo.chats.BriefMessagePojo;
import com.onlinedoctor.pojo.patient.Label;
import com.onlinedoctor.pojo.patient.Patient;
import com.onlinedoctor.pojo.patient.PatientDoctorRel;
import com.onlinedoctor.pojo.patient.PatientLabelRel;
import com.onlinedoctor.pojo.patient.PatientMessage;
import com.onlinedoctor.sqlite.dao.CurrentChatUserServiceImpl;
import com.onlinedoctor.sqlite.dao.LabelServiceImpl;
import com.onlinedoctor.sqlite.dao.PatientDocRelationServiceImpl;
import com.onlinedoctor.sqlite.dao.PatientInfoServiceImpl;
import com.onlinedoctor.sqlite.dao.PatientLabelRelationServiceImpl;
import com.onlinedoctor.sqlite.dao.PatientMessageInfoServiceImpl;
import com.onlinedoctor.sqlite.service.CurrentChatUserService;
import com.onlinedoctor.sqlite.service.PatientMessageInfoService;

public class DBTest {

	/*public static void patientMessageTest(Context context) {
		PatientMessageInfoService messageImpl = new PatientMessageInfoServiceImpl(context, null);
		PatientMessage message = new PatientMessage(UUID.randomUUID().toString(), "消息测试", "text", "1231637", "121424",
				1, 1, 0, System.currentTimeMillis());
		if (messageImpl.addPatientMessageInfo(message)) {
			List<Map<String, String>> result = messageImpl.listPatientMessageInfoBy(null);
			Log.i("DBTest--", "MessageSize: " + result.size());
			Log.i("DBTest--", "MessageSize: " + result.toString());
			Object[] params = { 1 };
			messageImpl.deletePatientMessageInfo(params);
			Log.i("DBTest--", "After delete MessageSize: " + messageImpl.listPatientMessageInfoBy(null).toString());
		}
		Object[] params = { "update message", 0, 0, 0, 0, message.getGuid() };
		messageImpl.updatePatientMessageInfo(params);
		PatientMessage temp = messageImpl.viewPatientMessageInfo(message.getGuid());
		if (temp.getContent().equals("update message")) {
			Log.i("DBTest--", "Update message success");
		}

		PatientMessage message1 = new PatientMessage(UUID.randomUUID().toString(), "消息测试", "text", "1231637", "121424",
				1, 1, 0, System.currentTimeMillis());
		PatientMessage message2 = new PatientMessage(UUID.randomUUID().toString(), "消息测试", "text", "121424", "1231637",
				1, 1, 0, System.currentTimeMillis());
		PatientMessage message3 = new PatientMessage(UUID.randomUUID().toString(), "消息测试", "text", "13241", "434", 1,
				1, 0, System.currentTimeMillis());
		List<PatientMessage> list = new ArrayList<PatientMessage>();
		list.add(message1);
		list.add(message2);
		list.add(message3);
		messageImpl.addPatientMessageInfo(list);
		List<Map<String, String>> resultList = messageImpl.listPatientMessageInfoBy(null);
		if (resultList.size() == 4) {
			Log.i("DBTest--", "Insert List Message Success");
		}
		List<PatientMessage> list2 = messageImpl.listTop50PatientMessage("1231637");
		Log.i("DBTest--", "listTop50PatientMessage" + list2.toString());
		if (list2.size() == 3 && list2.get(2).getToID().equals("1231637")) {
			Log.i("DBTest--", "listTop50PatientMessage Success");
		}
		messageImpl.deleteAllPatientMessage();
	}*/
	
	public void patientMessageTest2(Context context){
		PatientMessageInfoService messageImpl = new PatientMessageInfoServiceImpl(context, null);
		PatientMessage message1 = new PatientMessage(UUID.randomUUID().toString(), "早上好", "text", "1", "2",
				1, 1, 0, System.currentTimeMillis());
		PatientMessage message2 = new PatientMessage(UUID.randomUUID().toString(), "中午好", "text", "2", "1",
				1, 1, 0, System.currentTimeMillis());
		PatientMessage message3 = new PatientMessage(UUID.randomUUID().toString(), "晚上好", "text", "1", "2", 1,
				1, 0, System.currentTimeMillis());
		messageImpl.addPatientMessageInfo(message1);
		messageImpl.addPatientMessageInfo(message2);
		messageImpl.addPatientMessageInfo(message3);
		List<PatientMessage> list = messageImpl.listLastKPatientMessage("1", 3);
		for(Iterator<PatientMessage> iter = list.iterator(); iter.hasNext();){
			PatientMessage tmp = iter.next();
			Log.d("DBTest", tmp.getContent());
		}
		
		message1.setGuid(UUID.randomUUID().toString());
		message1.setContent("好早上");
		message1.setTimestamp(System.currentTimeMillis());
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		message2.setGuid(UUID.randomUUID().toString());
		message2.setContent("好中午");
		message2.setTimestamp(System.currentTimeMillis());
		try{
			Thread.sleep(1000);
		} catch (InterruptedException e){
			e.printStackTrace();
		}
		long tmpTime = System.currentTimeMillis();
		message3.setGuid(UUID.randomUUID().toString());
		message3.setContent("好晚上");
		message3.setTimestamp(System.currentTimeMillis());
		List<PatientMessage> list2 = new ArrayList<PatientMessage>();
		list2.add(message1);
		list2.add(message2);
		list2.add(message3);
		messageImpl.addPatientMessageInfo(list2);
		
		List<PatientMessage> list3 = messageImpl.listLastKPatientMessageBeforeId("1", 10, message2.getGuid());
		for(Iterator<PatientMessage> iter = list3.iterator(); iter.hasNext();){
			PatientMessage tmp = iter.next();
			Log.d("DBTest", tmp.getContent());
		}
		
		message2.setContent("好你个鬼都被烦死了");
		messageImpl.updatePatientMessageInfo(message2);
		
		List<PatientMessage> list4 = messageImpl.listLastKPatientMessage("1", 10);
		for(Iterator<PatientMessage> iter = list4.iterator(); iter.hasNext();){
			PatientMessage tmp = iter.next();
			Log.d("DBTest", tmp.getContent());
		}
	}
	
	public void briefMessageTest(Context context){
		CurrentChatUserService ccusImpl = new CurrentChatUserServiceImpl(context, null);
		BriefMessagePojo bm = new BriefMessagePojo();
		bm.setCount(2);
		bm.setFaceImageUrl("www.hahaha.com");
		bm.setIfNoDisturbing(false);
		bm.setMessage("wahahaa");
		bm.setTime(System.currentTimeMillis());
		bm.setUserId("w1");
		bm.setUserName("武媚娘");
		
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		BriefMessagePojo bm2 = new BriefMessagePojo();
		bm2.setCount(1);
		bm2.setFaceImageUrl("www.hahaha.com");
		bm2.setIfNoDisturbing(false);
		bm2.setMessage("wahahaa");
		bm2.setTime(System.currentTimeMillis());
		bm2.setUserId("w2");
		bm2.setUserName("武媚娘2");
		
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		BriefMessagePojo bm3 = new BriefMessagePojo();
		bm3.setCount(1);
		bm3.setFaceImageUrl("www.hahaha.com");
		bm3.setIfNoDisturbing(false);
		bm3.setMessage("wahahaa");
		bm3.setTime(System.currentTimeMillis());
		bm3.setUserId("w3");
		bm3.setUserName("武媚娘2");
		
		/*ccusImpl.addChatUserOne(bm);
		ccusImpl.addChatUserOne(bm2);
		ccusImpl.addChatUserOne(bm3);*/
		
		List<BriefMessagePojo> addList = new ArrayList<BriefMessagePojo>();
		addList.add(bm);
		addList.add(bm3);
		addList.add(bm2);
		ccusImpl.addChatUserList(addList);
		
		List<BriefMessagePojo> list = ccusImpl.listTopKBriefMessage(2);
		for(Iterator<BriefMessagePojo> iter = list.iterator(); iter.hasNext(); ){
			Log.d("DBTest", iter.next().getUserId());
		}
		
		ccusImpl.deleteChatUser("w3");
		
		List<BriefMessagePojo> list2 = ccusImpl.listTopKBriefMessage(2);
		for(Iterator<BriefMessagePojo> iter = list2.iterator(); iter.hasNext(); ){
			Log.d("DBTest", iter.next().getUserId());
		}
		
	}


	private static String getDate() {
		Calendar c = Calendar.getInstance();
		String year = String.valueOf(c.get(Calendar.YEAR));
		String month = String.valueOf(c.get(Calendar.MONTH));
		String day = String.valueOf(c.get(Calendar.DAY_OF_MONTH) + 1);
		String hour = String.valueOf(c.get(Calendar.HOUR_OF_DAY));
		String mins = String.valueOf(c.get(Calendar.MINUTE));
		StringBuffer sbBuffer = new StringBuffer();
		sbBuffer.append(year + "-" + month + "-" + day + " " + hour + ":" + mins);
		return sbBuffer.toString();
	}

	public static void patientInfoTest(Context context) {
		PatientInfoServiceImpl p = new PatientInfoServiceImpl(context, null);
		Patient p1 = new Patient(), p2 = new Patient();
		ArrayList<Patient> pList = new ArrayList<Patient>();
		p1.setPatientId("p123");
		p1.setName("周大仙");
		p1.setGender("M");

		p2.setPatientId("p234");
		p2.setName("武媚娘");
		p2.setGender("F");

		p1.setPatientId("p123");
		p1.setName("周大仙");
		p1.setGender("M");

		p2.setPatientId("p234");
		p2.setName("武媚娘");
		p2.setGender("F");

		pList.add(p1);
		pList.add(p2);
		p.addPatientInfo(pList);

		Patient p3 = new Patient();
		p3.setPatientId("p345");
		p3.setName("六媚娘");
		p3.setGender("F");
		p.addPatientInfoOne(p3);

		List<Map<String, String>> list = p.listPatientInfoBy(null);
		for (Iterator<Map<String, String>> it = list.iterator(); it.hasNext();) {
			Map<String, String> ele = it.next();
			Log.d("sqltest", "patientId: " + ele.get("patientId"));
			Log.d("sqltest", "name: " + ele.get("name"));
			Log.d("sqltest", "gender: " + ele.get("gender"));
		}

		String[] ids = new String[3];
		ids[0] = "p123";
		ids[1] = "p234";
		ids[2] = "p345";

		List<Patient> pList2 = p.listPatientInfoById(ids);

		for (Iterator<Patient> it = pList2.iterator(); it.hasNext();) {
			Patient ele = it.next();
			Log.d("sqltest", "patientId: " + ele.getPatientId());
		}
	}

	public static void patientDocRelTest(Context context) {
		PatientDocRelationServiceImpl p = new PatientDocRelationServiceImpl(context, null);
		PatientDoctorRel pdr1 = new PatientDoctorRel(), pdr2 = new PatientDoctorRel(), pdr3 = new PatientDoctorRel();
		pdr1.doctorId = "d2";
		pdr1.patientId = "p123";
		pdr1.time = 123;
		pdr1.status = 1;
		p.addRelationInfoOne(pdr1);
		pdr2.doctorId = "d2";
		pdr2.patientId = "p234";
		pdr2.time = 123;
		pdr2.status = 1;
		p.addRelationInfoOne(pdr2);
		pdr3.doctorId = "d2";
		pdr3.patientId = "p345";
		pdr3.time = 123;
		pdr3.status = 1;
		p.addRelationInfoOne(pdr3);

		List<PatientDoctorRel> list = p.listRelationInfoByDoctorId(pdr1.doctorId);
		int len = list.size();
		for (int i = 0; i < len; i++) {
			Log.d("sqltest", "find rel " + i + " " + list.get(i).patientId);
		}
	}

	public static void labelListTest(Context context) {
		LabelServiceImpl p = new LabelServiceImpl(context, null);
		Label label1 = new Label(), label2 = new Label(), label3 = new Label(), label4 = new Label();
		label1.setLabelName("VIP");
		label2.setLabelName("不孕不育");
		label3.setLabelName("我的狂热粉尸");
		label4.setLabelName("我的狂热粉尸");

		p.addLabelOne(label1);
		p.addLabelOne(label2);
		p.addLabelOne(label3);
		p.addLabelOne(label4);

		List<Label> list = p.listLabels();
		int len = list.size();
		for (int i = 0; i < len; i++) {
			Log.d("sqltest", "find label id: " + list.get(i).getId() + ", name: " + list.get(i).getLabelName());
		}
		Log.d("sqltest", "id after insert of label1: " + label1.getId());
		Log.d("sqltest", "id after insert of label2: " + label2.getId());
		Log.d("sqltest", "id after insert of label3: " + label3.getId());
		Log.d("sqltest", "id after insert of label4: " + label4.getId());

		// p.deleteLabelByName(label1.getLabelName());
		p.deleteLabelById(label1.getId());
		list = p.listLabels();
		len = list.size();
		for (int i = 0; i < len; i++) {
			Log.d("sqltest", "find label id: " + list.get(i).getId() + ", name: " + list.get(i).getLabelName());
		}

		p.addLabelOne(label1);

		list = p.listLabels();
		len = list.size();
		for (int i = 0; i < len; i++) {
			Log.d("sqltest", "find label id: " + list.get(i).getId() + ", name: " + list.get(i).getLabelName());
		}

		Log.d("sqltest", "id after insert of label1: " + label1.getId());

		p.updateLabelNameById(2, "有孕有育");
		p.updateLabelNameById(3, "我的狂热粉尸，转化！");

		list = p.listLabels();
		len = list.size();
		for (int i = 0; i < len; i++) {
			Log.d("sqltest", "find label id: " + list.get(i).getId() + ", name: " + list.get(i).getLabelName());
		}
	}

	public static void patientLabelRelTest(Context context) {
		PatientLabelRelationServiceImpl p = new PatientLabelRelationServiceImpl(context, null);
		PatientLabelRel plr1 = new PatientLabelRel(), plr2 = new PatientLabelRel(), plr3 = new PatientLabelRel(), plr4 = new PatientLabelRel();
		plr1.setLabelId(0);
		plr1.setPatientId("p123");
		plr1.setStatus(1);

		plr2.setLabelId(0);
		plr2.setPatientId("p124");
		plr2.setStatus(1);

		plr3.setLabelId(1);
		plr3.setPatientId("p123");
		plr3.setStatus(1);

		plr4.setLabelId(1);
		plr4.setPatientId("p124");
		plr4.setStatus(0);

		p.addRelationInfoOne(plr1);
		p.addRelationInfoOne(plr2);
		p.addRelationInfoOne(plr3);

		Label l = new Label();
		l.setId(0);
		l.setLabelName("VIP");

		List<String> list = p.listPatientByLabelId(Long.valueOf(l.getId()));
		int len = list.size();
		for (int i = 0; i < len; i++) {
			Log.d("sqltest", "find patient of label 0: " + list.get(i));
		}

		l.setId(1);
		list = p.listPatientByLabelId(Long.valueOf(l.getId()));
		len = list.size();
		for (int i = 0; i < len; i++) {
			Log.d("sqltest", "find patient of label 1: " + list.get(i));
		}
	}

	/*public static void currentChatUserTest(Context context) {
		Log.i("currentChatUserTest: ", "Test");
		CurrentChatUserService cService = new CurrentChatUserServiceImpl(context, null);
		List<BriefMessagePojo> list = new ArrayList<BriefMessagePojo>();
		BriefMessagePojo pojo1 = new BriefMessagePojo(10, "xud", "xud", "test message", "", System.currentTimeMillis(),
				true, "text", "标签1&标签2", 0);
		BriefMessagePojo pojo2 = new BriefMessagePojo(5, "song", "song", "test message", "",
				System.currentTimeMillis(), false, "text", "标签1&标签2", 0);
		BriefMessagePojo pojo3 = new BriefMessagePojo(10, "dong", "dong", "test message", "",
				System.currentTimeMillis(), true, "text", "签1&标签2", 0);
		
		list.add(pojo1);
		list.add(pojo2);
		list.add(pojo3);
		Log.i("currentChatUserTest insert data: ", list.toString());

		// 数据插入
		cService.addChatUserList(list);

		// 数据查询
		List<BriefMessagePojo> queryList = cService.getAllChatUsers();
		Log.i("currentChatUserTest: ", queryList.toString());

		// 数据更新
		int count = 2;
		String userId = list.get(0).getUserId();
		list.get(0).setCount(count);
		cService.updateChatUser(list.get(0));

		String[] args = new String[1];
		args[0] = userId;
		boolean flag = cService.getChatUserByUserId(args).getCount() == count ? true : false;
		if (flag) {
			Log.i("currentChatUserTest: ", "Update success");
		} else {
			Log.i("currentChatUserTest: ", "Update Error");
		}
	}*/
}
