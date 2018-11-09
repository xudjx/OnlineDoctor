package com.onlinedoctor.test;

import com.onlinedoctor.adapter.DataAdapter;
import com.onlinedoctor.application.MyApplication;
import com.onlinedoctor.pojo.chats.BriefMessagePojo;
import com.onlinedoctor.pojo.patient.PatientMessage;
import com.onlinedoctor.pojo.RunDataContainer;
import com.onlinedoctor.sqlite.dao.CurrentChatUserServiceImpl;
import com.onlinedoctor.sqlite.dao.PatientMessageInfoServiceImpl;
import com.onlinedoctor.sqlite.service.CurrentChatUserService;
import com.onlinedoctor.sqlite.service.PatientMessageInfoService;

public class CacheTest {

	private PatientMessageInfoService patientMessageInfoService = new PatientMessageInfoServiceImpl(
			MyApplication.context, null);
	private CurrentChatUserService currentChatUserService = new CurrentChatUserServiceImpl(MyApplication.context, null);

	int userNum = 4;
	int msgPerUser = 30;
	String sid;

	public CacheTest(String sid) {
		this.sid = sid;
	}

	public void msgInit() {
		for (int i = 0; i < userNum; i++) {
			for (int j = 0; j < msgPerUser; j++) {
				PatientMessage m = new PatientMessage();
				m.setGuid(Integer.toString(i * 1000) + Integer.toString(j));
				m.setContent("武娘" + Integer.toString(i) + "号的第" + Integer.toString(j) + "条信息");
				m.setContentType("text");
				if (j % 2 == 0) {
					m.setFromID("p" + Integer.toString(i));
					m.setToID(sid);
				} else {
					m.setFromID(sid);
					m.setToID("p" + Integer.toString(i));
				}
				if (j == msgPerUser - 1) {
					m.setIsRead(0);
				} else {
					m.setIsRead(1);
				}
				m.setSendStatus(1);
				m.setDeleted(0);
				//if(i == 0){
					m.setTimestamp(j*10000 + i);
				//}
				patientMessageInfoService.addPatientMessageInfo(m);
				if (j == msgPerUser - 1) {
					BriefMessagePojo bm = DataAdapter.briefMsgAdapter(m, "http://img5q.duitang.com/uploads/item/201501/21/20150121073008_vSHHf.jpeg", true, "武娘" + Integer.toString(i) + "号", "标签1&标签2", 0);
					currentChatUserService.addChatUserOne(bm);
				}
			}
		}
	}

	public void testCachePMAdd() {
		RunDataContainer rdc = RunDataContainer.getContainer();
		PatientMessage m = new PatientMessage();

		m.setGuid("12345test");
		m.setContent("我是test");
		m.setContentType("");
		m.setFromID(sid);
		m.setToID("p1");
		m.setIsRead(0);
		m.setSendStatus(1);
		m.setDeleted(0);
		m.setTimestamp(System.currentTimeMillis());

		rdc.testAddPatientMessage("p1", m);
	}

	public void testCachePMUpdate() {
		RunDataContainer rdc = RunDataContainer.getContainer();
		PatientMessage m = new PatientMessage();

		m.setGuid("028");
		m.setContent("我是test");
		m.setContentType("");
		m.setFromID(sid);
		m.setToID("p1");
		m.setIsRead(0);
		m.setSendStatus(1);
		m.setDeleted(0);
		m.setTimestamp(System.currentTimeMillis());

		rdc.testUpdatePatientMessage("p1", m);
		rdc.testUpdatePatientMessage("p0", m);
	}

	public void testCachePMDelete() {
		RunDataContainer rdc = RunDataContainer.getContainer();
		PatientMessage m = new PatientMessage();

		m.setGuid("028");
		m.setFromID(sid);
		m.setToID("p1");
		m.setDeleted(1);
		m.setTimestamp(System.currentTimeMillis());

		rdc.testDeletePatientMessage("p0", m);
	}
	
	/*public void testCacheBMNew(){
		RunDataContainer rdc = RunDataContainer.getContainer();
		BriefMessagePojo m = new BriefMessagePojo();
		m.setCount(2);
		m.setMessage("我是武大郎");
		m.setUserId("w1");
		m.setUserName("武大郎");
		
		rdc.testAddBriefMessage(m);
		
		BriefMessagePojo m2 = new BriefMessagePojo();
		m2.setCount(2);
		m2.setMessage("我是武小郎");
		m2.setUserId("w2");
		m2.setUserName("武小郎");
		
		rdc.testAddBriefMessage(m2);
		
		BriefMessagePojo m3 = new BriefMessagePojo();
		m3.setCount(2);
		m3.setMessage("武大郎又发信息了");
		m3.setUserId("w1");
		m3.setUserName("武大郎");\
		
		rdc.testAddBriefMessage(m3);
	}*/
	
	
	public void testCacheBMUpdate(){
		RunDataContainer rdc = RunDataContainer.getContainer();
		BriefMessagePojo m = new BriefMessagePojo();
		m.setMessage("哇哈哈下雨啦");
		m.setUserId("p2");
		m.setUserName("水王");
		m.setTime(System.currentTimeMillis());
		
		rdc.testUpdateBriefMessage(m);
	}
	
	public void testCacheBMDelete(){
		RunDataContainer rdc = RunDataContainer.getContainer();
		BriefMessagePojo m = new BriefMessagePojo();
		m.setUserId("p2");
		rdc.testDeleteBriefMessage(m);
	}
	
	public void testCacheSave(){
		RunDataContainer rdc = RunDataContainer.getContainer();
		rdc.testSavePatientBriefMessage();
	}
	
	public void testCacheReceive(){
		RunDataContainer rdc = RunDataContainer.getContainer();
		rdc.testReceiveMessage();
	}
	
	public void testCacheDeleteP(){
		RunDataContainer rdc = RunDataContainer.getContainer();
		PatientMessage pm = new PatientMessage();
		pm.setGuid("100029");
		rdc.testDeletePatientMessage("p1", pm);
	}
}
