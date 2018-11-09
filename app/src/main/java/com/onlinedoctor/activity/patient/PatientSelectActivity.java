package com.onlinedoctor.activity.patient;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.onlinedoctor.activity.R;
import com.onlinedoctor.activity.chats.MsgMutilChatActivity;
import com.onlinedoctor.adapter.patient.PatientListSelectAdapter;
import com.onlinedoctor.pojo.patient.Patient;
import com.onlinedoctor.pojo.patient.PatientDoctorRel;
import com.onlinedoctor.pojo.RunDataContainer;
import com.onlinedoctor.sqlite.dao.PatientDocRelationServiceImpl;
import com.onlinedoctor.sqlite.dao.PatientInfoServiceImpl;
import com.onlinedoctor.util.Pinyin;
import com.onlinedoctor.util.PinyinComparator;
import com.onlinedoctor.util.SharedpreferenceManager;
import com.onlinedoctor.view.PatientSideBar;
import com.onlinedoctor.view.PatientSideBar.OnTouchingLetterChangedListener;
import com.onlinedoctor.view.SearchEditText;
import com.umeng.analytics.MobclickAgent;

/**
 * 患者界面类
 * 
 * @author Song
 * 
 */

public class PatientSelectActivity extends Activity {

	private static final String TAG = "PatientActivitySelect";
	private PatientSideBar sideBar;
	private TextView dialog;
	private TextView title_tv;
	private TextView next_tv;
	private PatientListSelectAdapter adapter;
	//private List<PatientListModel> SourceDateList;
	private List<Patient> SourceDataList;
	private PinyinComparator pinyinComparator;
	private SearchEditText et_searchContact = null;
	private ListView lv_contactList = null;
	//private List<PatientListModel> contactList = null;
	private List<Patient> contactList = null;
	private String keywords = null;
	private HashMap<Integer, Boolean> isSelectedMap = null;
	//private List<PatientListModel> selectedPatients = null;
	private List<Patient> selectedPatients = null;
	private RunDataContainer dataContainer = RunDataContainer.getContainer();

	// 群发助手 和 转发消息有些区分
	private String ActivityType = "";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE); // 不显示标题
		Log.i(TAG, "onCreate");
		setContentView(R.layout.activity_search_contact_select);
		ActivityType = getIntent().getStringExtra("ActivityType");
		loadingFormation();
		initData();
	}

	/**
	 * 加载组件
	 */
	private void loadingFormation() {
		et_searchContact = (SearchEditText) this.findViewById(R.id.search_contact);
		lv_contactList = (ListView) this.findViewById(R.id.contact_list);
		pinyinComparator = new PinyinComparator();
		sideBar = (PatientSideBar) findViewById(R.id.sidebar);
		dialog = (TextView) findViewById(R.id.dialog);
		sideBar.setTextView(dialog);
		title_tv = (TextView) findViewById(R.id.actionbar_common_backable_title_tv);
		title_tv.setText("选择收信人");
		next_tv = (TextView) findViewById(R.id.actionbar_common_backable_right_tv);
		if (ActivityType.equals("ForwardActivity")) {
			next_tv.setText("确定");
		} else if (ActivityType.equals("MsgMutilChatActivity")) {
			next_tv.setText("下一步");
		}
		setComponentsListener();
	}

	/**
	 * 为组件设置监听器
	 */
	private void setComponentsListener() {
		sideBar.setOnTouchingLetterChangedListener(new OnTouchingLetterChangedListener() {
			@Override
			public void onTouchingLetterChanged(String s) {
				// 该字母首次出现的位置
				int position = adapter.getPositionForSection(s.charAt(0));
				if (position != -1) {
					lv_contactList.setSelection(position);
				}
			}
		});
	}

	/**
	 * 初始化数据
	 */
	private void initData() {
		SourceDataList = fillData();
		Collections.sort(SourceDataList, pinyinComparator);
		loadDataToAdapter();
		final Resources res = getApplicationContext().getResources();
		final int len = res.getStringArray(R.array.contact_func).length;
		lv_contactList.setAlwaysDrawnWithCacheEnabled(false);
		lv_contactList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				PatientListSelectAdapter.ViewHolder vh = (PatientListSelectAdapter.ViewHolder) view.getTag();
				vh.isSelect.toggle();
				isSelectedMap.put(position, vh.isSelect.isChecked());
				if (vh.isSelect.isChecked()) {
					// add
					selectedPatients.add(SourceDataList.get(position));
					Log.d("CheckBox", "add " + SourceDataList.get(position).getName());
				} else {
					// delete
					selectedPatients.remove(SourceDataList.get(position));
					Log.d("CheckBox", "delete " + SourceDataList.get(position).getName());
				}

			}
		});
		next_tv.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if(selectedPatients != null && !selectedPatients.isEmpty()){
					Intent intent = new Intent(PatientSelectActivity.this, MsgMutilChatActivity.class);
					Bundle bundle = new Bundle();
					bundle.putSerializable("patientList", (Serializable) selectedPatients);
					intent.putExtras(bundle);
					startActivity(intent);
					finish();
				}
				else{
					Toast.makeText(getApplicationContext(), "请选择联系人", Toast.LENGTH_SHORT).show();
				}
			}

		});

		// 快速搜索
		et_searchContact.addTextChangedListener(new TextWatcher() {

			/**
			 * 文件变化时
			 */
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				keywords = et_searchContact.getText().toString().trim();
				filterData(keywords);
			}

			/**
			 * 文本变化前
			 */
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {

			}

			/**
			 * 文本变化后
			 */
			@Override
			public void afterTextChanged(Editable s) {

			}
		});
	}

	/*
	private List<PatientListModel> fillData() {
		contactList = new ArrayList<PatientListModel>();
		SharedpreferenceManager spManager = SharedpreferenceManager.getInstance();
		String sid = spManager.getOne("keySid");
		PatientDocRelationServiceImpl pdrImpl = new PatientDocRelationServiceImpl(this, null);
		PatientInfoServiceImpl pImpl = new PatientInfoServiceImpl(this, null);

		List<PatientDoctorRel> pdrList = pdrImpl.listRelationInfoByDoctorId(sid);
		this.isSelectedMap = new HashMap<Integer, Boolean>();
		this.selectedPatients = new LinkedList<PatientListModel>();
		int len = pdrList.size();
		// 需要优化
		for (int i = 0; i < len; i++) {
			PatientDoctorRel pdrTmp = pdrList.get(i);
			Patient patient = pImpl.viewPatientInfoByPatientId(pdrTmp.patientId);
			PatientListModel patientListModel = new PatientListModel();
			patientListModel.setName(patient.getName());
			patientListModel.setImgUrl(patient.getThumbnail());
			patientListModel.setSortLetters(patient.getPinyin().substring(0, 1).toUpperCase());
			patientListModel.setUserId(patient.getPatientId());
			contactList.add(patientListModel);
			isSelectedMap.put(i, false);
		}

		return contactList;
	}*/

	private List<Patient> fillData() {
		contactList = new ArrayList<Patient>();
		SharedpreferenceManager spManager = SharedpreferenceManager.getInstance();
		String sid = spManager.getOne("keySid");
		PatientDocRelationServiceImpl pdrImpl = new PatientDocRelationServiceImpl(this, null);
		PatientInfoServiceImpl pImpl = new PatientInfoServiceImpl(this, null);

		List<PatientDoctorRel> pdrList = pdrImpl.listRelationInfoByDoctorId(sid);
		this.isSelectedMap = new HashMap<Integer, Boolean>();
		this.selectedPatients = new LinkedList<Patient>();
		int len = pdrList.size();
		// 需要优化
		for (int i = 0; i < len; i++) {
			PatientDoctorRel pdrTmp = pdrList.get(i);
			Patient patient = pImpl.viewPatientInfoByPatientId(pdrTmp.patientId);
			patient.setSortLetters(patient.getPinyin().substring(0, 1).toUpperCase());
			contactList.add(patient);
			isSelectedMap.put(i, false);
		}

		return contactList;
	}

	private void loadDataToAdapter() {
		adapter = new PatientListSelectAdapter(getApplicationContext(), SourceDataList, isSelectedMap);
		lv_contactList.setAdapter(adapter);
	}

	/**
	 * 弹出提示信息
	 * 
	 * @param msg
	 */
	public void showToast(String msg) {
		Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
	}

	/**
	 * 刷新界面(手动刷新)
	 */
	public void refresh() {
		initData();
		keywords = et_searchContact.getText().toString().trim();
		if (keywords != null) {
			filterData(keywords);
		}
	}

	/**
	 * 根据输入框中的值来过滤数据并更新ListView
	 * 
	 * @param filterStr
	 */
	/*
	private void filterData(String filterStr) {
		List<PatientListModel> filterDateList = new ArrayList<PatientListModel>();
		boolean isFound = false;
		if (TextUtils.isEmpty(filterStr)) {
			filterDateList = SourceDateList;
		} else {
			filterDateList.clear();
			for (PatientListModel patientListModel : SourceDateList) {
				String name = patientListModel.getName();
				if (name.indexOf(filterStr.toString()) != -1
						|| Pinyin.convertToPinyin(name).startsWith(filterStr.toLowerCase().toString())) {
					filterDateList.add(patientListModel);
				}
			}
			isFound = true;
		}

		// 根据a-z进行排序
		Collections.sort(filterDateList, pinyinComparator);
		adapter.updateListView(filterDateList, isFound);
	}*/

	private void filterData(String filterStr) {
		List<Patient> filterDataList = new ArrayList<Patient>();
		boolean isFound = false;
		if (TextUtils.isEmpty(filterStr)) {
			filterDataList = SourceDataList;
		} else {
			filterDataList.clear();
			for (Patient patient : SourceDataList) {
				String name = patient.getName();
				if (name.indexOf(filterStr.toString()) != -1
						|| Pinyin.convertToPinyin(name).startsWith(filterStr.toLowerCase().toString())) {
					filterDataList.add(patient);
				}
			}
			isFound = true;
		}

		// 根据a-z进行排序
		Collections.sort(filterDataList, pinyinComparator);
		adapter.updateListView(filterDataList, isFound);
	}

	/**
	 * Activity被覆盖后重新显示出来时自动刷新
	 */
	@Override
	protected void onResume() {
		super.onResume();
		MobclickAgent.onResume(this);
		// cursor = db.getAllContacts();
		// SourceDateList = filledDataDemo();
		// SourceDateList = fillDataTest();
		keywords = et_searchContact.getText().toString().trim();
		if (!keywords.isEmpty()) {
			filterData(keywords);
		}
		// dataContainer.setInPatientActivity(true);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		Log.i(TAG, "onDestroy");
	}

	@Override
	protected void onRestart() {
		super.onRestart();
		Log.i(TAG, "onRestart");
	}

	@Override
	protected void onStop() {
		// dataContainer.setInPatientActivity(false);
		super.onStop();
		Log.i(TAG, "onStop");
	}

	@Override
	protected void onPause(){
		super.onPause();
		MobclickAgent.onPause(this);
	}
}
