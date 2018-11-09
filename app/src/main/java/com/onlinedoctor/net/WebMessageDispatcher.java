package com.onlinedoctor.net;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;
import com.onlinedoctor.adapter.DataAdapter;
import com.onlinedoctor.application.MyApplication;
import com.onlinedoctor.pojo.Common;
import com.onlinedoctor.pojo.Common.MessageType;
import com.onlinedoctor.pojo.chats.AtnMessageDTO;
import com.onlinedoctor.pojo.chats.PatientMessageDTO;
import com.onlinedoctor.pojo.patient.NewPatient;
import com.onlinedoctor.pojo.patient.Patient;
import com.onlinedoctor.pojo.patient.PatientMessage;
import com.onlinedoctor.pojo.RunDataContainer;
import com.onlinedoctor.sqlite.dao.PatientMessageInfoServiceImpl;
import com.onlinedoctor.sqlite.service.PatientMessageInfoService;
import com.onlinedoctor.util.JsonUtil;

/**
 * WebSocket Message 消息处理和分发类
 * 
 * @author dong 消息分发机制 1、依据返回的类型进行消息分发，type= pong, messagelist, sendMessage
 *         2、ping: 开一个Service，隔一段时间进行ping-pong 3、synMessage: 获取之后存入数据库中
 *         4、message: 此处单指聊天的message，若恰好是当前聊天患者的message，用handler刷新显示
 *         若不是当前聊天患者的信息，则在handler处理函数中 显示到通知栏
 * 
 *         消息通知机制 1、消息用“轻微的震动”来提示，设置里面可取消震动 2、复诊患者、新患者、消息，只要用户当前不在该界面，就显示到通知栏
 *         3、界面的刷新显示采用Handler机制
 * 
 *         song 增加AtnMessage
 */
public class WebMessageDispatcher {

	private static WebMessageDispatcher instance;
	private static HandlerManager handlerManager = HandlerManager.getManager();
	private Context context = null;
	private RunDataContainer dataContainer;

	private PatientMessageInfoService patientMessageInfoService = new PatientMessageInfoServiceImpl(
			MyApplication.context, null);

	private WebMessageDispatcher() {
		context = MyApplication.context;
		dataContainer = RunDataContainer.getContainer();
	}

	public static WebMessageDispatcher getDispatcher() {
		if (instance == null) {
			synchronized (NetworkManager.class) {
				if (instance == null) {
					instance = new WebMessageDispatcher();
				}
			}
		}
		return instance;
	}

	public void destroy(){
		instance = null;
		System.gc();
	}

	public void dispatchMessage(String message) {
		MessageType type = getType(message);
		Log.d("WebDispatcher", "I am in the dispatcher!");
		if (type == null)
			return;
		switch (type) {
		case TYPE_PONG: // ping pong 不做处理
		{
			break;
		}
		case TYPE_CHATMESSAGE: // 存入数据库，消息通知
		{
			String data = null;
			data = (String) JsonUtil.getJsonStringByKey(message, Common.data);
			PatientMessageDTO patientMessageDTO = getMessage(data);
			PatientMessage pm = DataAdapter.patientMessageAdapter(patientMessageDTO);
			Log.d("WebDispatcher", "receive chat message,userId:" + DataAdapter.getUserId(pm) + ", from:" + pm.getFromID() + ", to:" + pm.getToID() + " content:" + pm.getContent());
			dataContainer.receiveMessage(pm);
			break;
		}
		case TYPE_SYNMESSAGE: // 提取未读消息，并进行消息通知
		{
			try {
				JSONObject jo = new JSONObject(message);
				JSONObject joData = new JSONObject(jo.getString("data"));
				int code = jo.getInt("code");
				switch(code){
				case 200:
					Log.d("WebDispatcher", "receive syn message: " + message);
					int isNew = joData.getInt("isNew");
					if(isNew == 1){
						break;
					}
					String synObject = joData.getString("synObject");
					Gson gson = new Gson();
					
					PatientMessageDTO[] list = gson.fromJson(synObject, PatientMessageDTO[].class);
					int len = list.length;
					for(int i = 0; i < len; i++){
						PatientMessage pm = DataAdapter.patientMessageAdapter(list[i]);
						dataContainer.receiveMessage(pm);
					}
					dataContainer.sendSynMessage();
					break;
				case -1:
					break;
				}
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			break;
		}
		case TYPE_ATNMESSAGE: {
			/*String data = null;
			data = (String) JsonUtil.getJsonStringByKey(message, Common.data);*/
			try {
				JSONObject jo = new JSONObject(message);
				//JSONObject joData = new JSONObject(jo.getString("data"));
				int code = jo.getInt("code");
				switch(code){
				case 200:
					Log.d("WebDispatcher", "receive attention message: " + message);
					AtnMessageDTO atnMessageDTO = getAtnMessage(jo.getString("data"));
					Patient p = DataAdapter.PatientAdapter(atnMessageDTO.getPackPatientMessage());
					NewPatient np = DataAdapter.NewPatientAttentionAdatper(atnMessageDTO);
					dataContainer.receiveAtnMessage(p, np, atnMessageDTO.getStatus());
					break;
				case -1:
					break;
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
			break;
		}
		case TYPE_SYNATNMESSAGE:{
			try {
				JSONObject jo = new JSONObject(message);
				JSONObject joData = new JSONObject(jo.getString("data"));
				int code = jo.getInt("code");
				switch(code){
				case 200:
					Log.d("WebDispatcher", "receive syn attention message: " + message);
					int isNew = joData.getInt("isNew");
					if(isNew == 1){
						break;
					}
					String synObject = joData.getString("synObject");
					Gson gson = new Gson();
					AtnMessageDTO[] list = gson.fromJson(synObject, AtnMessageDTO[].class);
					int len = list.length;
					for(int i = 0; i < len; i++){
						Patient p = DataAdapter.PatientAdapter(list[i].getPackPatientMessage());
						NewPatient np = DataAdapter.NewPatientAttentionAdatper(list[i]);
						dataContainer.receiveAtnMessage(p, np, list[i].getStatus());
					}
					break;
				case -1:
					break;
				}
				RunDataContainer.getContainer().sendSynMessage();
			} catch (JSONException e) {
				e.printStackTrace();
			}
			break;
		}
		
		default:
			break;
		}
	}

	// 从返回数据中解析Message
	private PatientMessageDTO getMessage(String jsonMessage) {
		PatientMessageDTO message = (PatientMessageDTO) JsonUtil.jsonToBean(jsonMessage, PatientMessageDTO.class);
		return message;
	}

	// 从返回数据中解析AtnMessage
	private AtnMessageDTO getAtnMessage(String jsonAtnMessage) {
		AtnMessageDTO message = (AtnMessageDTO) JsonUtil.jsonToBean(jsonAtnMessage, AtnMessageDTO.class);
		return message;
	}

	private MessageType getType(String message) {
		String type = null;
		type = (String) JsonUtil.getJsonValue(message, Common.type);
		if (type == null || "".equals(type))
			return null;
		Log.i("type", type);
		if (type.equals(Common.type_pong))
			return MessageType.TYPE_PONG;
		if (type.equals(Common.type_chatmessage))
			return MessageType.TYPE_CHATMESSAGE;
		if (type.equals(Common.type_synmessage))
			return MessageType.TYPE_SYNMESSAGE;
		if (type.equals(Common.type_atnmessage))
			return MessageType.TYPE_ATNMESSAGE;
		if (type.equals(Common.type_synatnmessage))
			return MessageType.TYPE_SYNATNMESSAGE;
		return null;
	}

}
