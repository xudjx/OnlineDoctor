package com.onlinedoctor.adapter;

import com.onlinedoctor.pojo.chats.BriefMessagePojo;
import com.onlinedoctor.pojo.chats.AtnMessageDTO;
import com.onlinedoctor.pojo.chats.PatientMessageDTO;
import com.onlinedoctor.pojo.patient.PatientDTO;
import com.onlinedoctor.pojo.patient.Patient;
import com.onlinedoctor.pojo.patient.NewPatient;
import com.onlinedoctor.pojo.patient.PatientMessage;
import com.onlinedoctor.util.Pinyin;
import com.onlinedoctor.util.SharedpreferenceManager;

/**
 * 实现各种数据适配
 */
public class DataAdapter {
	
	private static SharedpreferenceManager spManager = SharedpreferenceManager.getInstance();

	public static PatientMessage patientMessageAdapter(PatientMessageDTO message) {
		PatientMessage message2 = new PatientMessage();
		message2.setContent(message.getContent());
		message2.setGuid(message.getGuid());
		message2.setFromID(message.getFrom());
		message2.setToID(message.getTo());
		message2.setTimestamp(message.getTime());
		message2.setContentType(message.getContentType());
		message2.setDeleted(0);
		message2.setIsRead(0);
		message2.setSendStatus(0);
		return message2;
	}

	public static BriefMessagePojo briefMsgAdapter(PatientMessage message, String imageUrl, boolean ifNoDisturbing, String userName, String labels, int isDraft) {
		//to do : modify to the formal one
		BriefMessagePojo pojo = new BriefMessagePojo();
		pojo.setCount(1);
		pojo.setFaceImageUrl(imageUrl);
		pojo.setIfNoDisturbing(ifNoDisturbing);
		pojo.setMessage(message.getContent());
		pojo.setUserId(DataAdapter.getUserId(message));
		pojo.setTime(message.getTimestamp());
		pojo.setUserName(userName);
		pojo.setContentType(message.getContentType());
		pojo.setLabels(labels);
		pojo.setSendStatus(message.getSendStatus());
		pojo.setpGuid(message.getGuid());
		pojo.setIsDraft(isDraft);
		return pojo;
	}
	
	public static PatientMessageDTO patientMessageAdapter(PatientMessage patientMessage){
		PatientMessageDTO patientMessageDTO = new PatientMessageDTO();
		patientMessageDTO.setContent(patientMessage.getContent());
		patientMessageDTO.setGuid(patientMessage.getGuid());
		patientMessageDTO.setFrom(patientMessage.getFromID());
		patientMessageDTO.setTo(patientMessage.getToID());
		patientMessageDTO.setTime(patientMessage.getTimestamp());
		patientMessageDTO.setContentType(patientMessage.getContentType());
		return patientMessageDTO;
	}
	
	public static String getUserId(PatientMessage pm){
		String userId = spManager.getOne("keySid"), fromId = pm.getFromID(), toId = pm.getToID(), ret = null;
		if(userId.equals(fromId)){
			ret = pm.getToID();
		}
		else if(userId.equals(toId)){
			ret = pm.getFromID();
		}
		return ret;
	}
	
	public static void updateBriefMessageByPreviousPatientMessage(BriefMessagePojo bm, PatientMessage pm){
		bm.setMessage(pm.getContent());
		bm.setTime(pm.getTimestamp());
		bm.setContentType(pm.getContentType());
		bm.setSendStatus(pm.getSendStatus());
		bm.setpGuid(pm.getGuid());
	}
	
	public static void updateBriefMessageByNewPatientMessage(BriefMessagePojo bm, PatientMessage pm, boolean isSend){
		bm.setContentType(pm.getContentType());
		bm.setCount(isSend?0:bm.getCount()+1);//如果是发送，直接将数量置为0
		bm.setFaceImageUrl(bm.getFaceImageUrl());
		bm.setIfNoDisturbing(bm.isIfNoDisturbing());
		bm.setIsDraft(isSend?0:bm.getIsDraft());//如果是发送，直接将草稿置为0，即草稿状态结束
		bm.setLabels(bm.getLabels());
		bm.setMessage(bm.getIsDraft()==0?pm.getContent():bm.getMessage());//如果是草稿，则保留原来bm的值不做更新
		bm.setpGuid(pm.getGuid());
		bm.setSendStatus(pm.getSendStatus());
		bm.setTime(pm.getTimestamp());
		bm.setUserId(bm.getUserId());
		bm.setUserName(bm.getUserName());
	}
	
	public static NewPatient NewPatientAttentionAdatper(AtnMessageDTO message){
		NewPatient np = new NewPatient();
		np.setFollowTime(message.getTimestamp());
		np.setGender(message.getPackPatientMessage().getGender());
		np.setName(message.getPackPatientMessage().getName());
		np.setPatientId(message.getPackPatientMessage().getSid());
		np.setThumbnail(message.getPackPatientMessage().getThumbnail());
		np.setStatus(message.getStatus());
		np.setIsChecked(0);
		return np;
	}
	
	public static Patient PatientAdapter(PatientDTO message){
		Patient p = new Patient();
		p.setAvatar(message.getAvatar());
		p.setBirthday(message.getBirthday());
		p.setGender(message.getGender());
		p.setLastTime(message.getLastTime());
		p.setName(message.getName());
		p.setPatientId(message.getSid());
		p.setPhone(message.getCellphone());
		p.setRegisterTime(message.getRegisterTime());
		p.setThumbnail(message.getThumbnail());
		String pinyin = Pinyin.convertToPinyin(p.getName());
		p.setPinyin(pinyin);
		String comment="";
		p.setComment(comment);
		return p;
	}
}
