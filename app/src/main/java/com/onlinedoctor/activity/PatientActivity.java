package com.onlinedoctor.activity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.onlinedoctor.activity.chats.ForwardActivity;
import com.onlinedoctor.activity.chats.MsgMutilChatActivity;
import com.onlinedoctor.adapter.patient.PatientListAdapter;
import com.onlinedoctor.application.MyApplication;
import com.onlinedoctor.log.Logger;
import com.onlinedoctor.net.HandlerManager;
import com.onlinedoctor.pojo.Common;
import com.onlinedoctor.pojo.patient.Label;
import com.onlinedoctor.pojo.patient.Patient;
import com.onlinedoctor.pojo.RunDataContainer;
import com.onlinedoctor.sqlite.dao.LabelServiceImpl;
import com.onlinedoctor.sqlite.dao.PatientInfoServiceImpl;
import com.onlinedoctor.sqlite.dao.PatientLabelRelationServiceImpl;
import com.onlinedoctor.sqlite.service.LabelService;
import com.onlinedoctor.sqlite.service.PatientLabelRelationService;
import com.onlinedoctor.view.CommonActionbarBackableRelativeLayout;
import com.onlinedoctor.util.Pinyin;
import com.onlinedoctor.util.PinyinComparator;
import com.onlinedoctor.view.PatientSideBar;
import com.onlinedoctor.view.SearchEditText;
import com.onlinedoctor.view.PatientSideBar.OnTouchingLetterChangedListener;
import com.umeng.analytics.MobclickAgent;

import android.view.View.OnClickListener;
import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * 患者界面类
 * @author Song
 *
 */

public class PatientActivity extends Activity {
    private static final String TAG = "PatientActivity";
    private PatientSideBar sideBar;
    private TextView dialog;
    private PatientListAdapter adapter;
    //private List<PatientListModel> SourceDateList;
    private List<Patient> SourceDataList;
    private PinyinComparator pinyinComparator;
    private SearchEditText et_searchContact = null;
    //private ListView lv_contactList = null;
    private RecyclerView lv_contactList = null;
    private TextView rightView = null;
    private ImageView noContentImage = null;
    private Boolean nowIn = false;
    //private List<PatientListModel> contactList = null;
    private String keywords = null;
    private String labelId = null;
    private String mode = null;
    private RunDataContainer dataContainer = RunDataContainer.getContainer();
    private HashMap<String, List<String>> patientLabel = null;

    private final String MODE_LIST = "list";
    private final String MODE_LABEL = "label";
    private final String MODE_GROUPSEND = "groupsend";

    private String activitySource = null;
    //private MyChatClient chatClient;

    public List<Patient> selectedPatients = null;
    public boolean resumeChanged = false;
    public HashMap<Integer, Boolean> isSelectedMap = null;

    private Handler patientHandler = null;

    private void initHandler(){
        patientHandler = new Handler(){
            @Override
            public void handleMessage(Message msg){
                if(msg.what == Common.MSG_WHAT_PATIENTCOUNTCHANGE_MESSAGE){
                    //test
                    //Toast.makeText(getApplicationContext(), "已收到患者关注请求", Toast.LENGTH_SHORT).show();
                    Log.d("patientAttention", "Get Patient Attention");
                    if(nowIn) {
                        refreshPatients(MODE_LIST, null);
                    }
                    else {
                        resumeChanged = true;
                    }
                }
            }
        };
        HandlerManager.getManager().setPatientHandler(patientHandler);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);		// 不显示标题
        Logger.i(TAG, "onCreate");
        setContentView(R.layout.activity_search_contact);

        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        mode = bundle.getString("mode");
        switch (mode){
            case MODE_LIST:
                loadComponents(mode);
                initData(mode, null);
                initHandler();
                break;
            case MODE_LABEL:
                labelId = bundle.getString("id");
                loadComponents(mode);
                initData(mode, labelId);
                break;
            case MODE_GROUPSEND:
                activitySource = bundle.getString("ActivityType", null);
                loadComponents(mode);
                initData(mode, null);
                break;
        }
    }

    private void loadComponents(String mode) {
        et_searchContact = (SearchEditText) this.findViewById(R.id.search_contact);
        //lv_contactList = (ListView) this.findViewById(R.id.contact_list);
        lv_contactList = (RecyclerView) this.findViewById(R.id.contact_list);
        lv_contactList.setLayoutManager(new LinearLayoutManager(this));
        lv_contactList.setItemAnimator(new DefaultItemAnimator());
        lv_contactList.setHasFixedSize(true);
        pinyinComparator = new PinyinComparator();
        sideBar = (PatientSideBar) findViewById(R.id.sidebar);
        dialog = (TextView) findViewById(R.id.dialog);
        sideBar.setTextView(dialog);
        noContentImage = (ImageView) findViewById(R.id.patient_no_content_pic);

        if(mode.equals(MODE_LIST)){

        }else if(mode.equals(MODE_LABEL)) {
            CommonActionbarBackableRelativeLayout cabrl = (CommonActionbarBackableRelativeLayout) findViewById(R.id.patient_info_bar);
            cabrl.setVisibility(View.VISIBLE);
            cabrl.setTitle("标签患者");
            cabrl.setRight("");
            View bigTitle = findViewById(R.id.patient_info_big_title);
            bigTitle.setVisibility(View.GONE);
        }else if(mode.equals(MODE_GROUPSEND)){
            CommonActionbarBackableRelativeLayout cabrl = (CommonActionbarBackableRelativeLayout) findViewById(R.id.patient_info_bar);
            cabrl.setVisibility(View.VISIBLE);
            cabrl.setTitle("选择收信人");
            cabrl.setRight("下一步");
            rightView = (TextView) cabrl.getRightView();
            View bigTitle = findViewById(R.id.patient_info_big_title);
            bigTitle.setVisibility(View.GONE);
        }
        setComponentsListener(mode);

    }

    private void setComponentsListener(String mode) {
        sideBar.setOnTouchingLetterChangedListener(new OnTouchingLetterChangedListener() {
            @Override
            public void onTouchingLetterChanged(String s) {
                //该字母首次出现的位置
                int position = adapter.getPositionForSection(s.charAt(0));
                if (position != -1) {
                    //lv_contactList.setSelection(position);
                    LinearLayoutManager llm = new LinearLayoutManager(MyApplication.context);
                    llm.scrollToPositionWithOffset(position, 0);
                    lv_contactList.setLayoutManager(llm);
                }
            }
        });
        if(mode.equals(MODE_GROUPSEND)){
            rightView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (selectedPatients != null && !selectedPatients.isEmpty()) {
                        Intent intent = new Intent(PatientActivity.this, MsgMutilChatActivity.class);
                        Bundle bundle = new Bundle();
                        bundle.putSerializable("patientList", (Serializable) selectedPatients);
                        intent.putExtras(bundle);
                        if(activitySource != null && activitySource.equals(ForwardActivity.CLASS_NAME_STRING)){
                            PatientActivity.this.setResult(RESULT_OK, intent);
                        }
                        else{
                            startActivity(intent);
                        }
                        finish();
                    } else {
                        Toast.makeText(getApplicationContext(), "请选择发送对象", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    private void initData(String mode, String labelId) {
        SourceDataList = fillData(mode, labelId);
        Collections.sort(SourceDataList, pinyinComparator);
        loadDataToAdapter(mode);
        final Resources res = getApplicationContext().getResources();
        final int len = res.getStringArray(R.array.contact_func).length;
        lv_contactList.setAlwaysDrawnWithCacheEnabled(false);

        et_searchContact.addTextChangedListener(new TextWatcher() {


            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                keywords = et_searchContact.getText().toString().trim();
                filterData(keywords);
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    private List<Patient> fillData(String mode, String labelId){
        List<Patient> contactList = new ArrayList<Patient>();
        if(mode == null || (mode.equals(MODE_LABEL) && labelId == null)){
            return contactList;
        }
        //init
        PatientInfoServiceImpl pImpl = new PatientInfoServiceImpl(this, null);
        LabelService ls = new LabelServiceImpl(this, null);
        patientLabel = new HashMap<String, List<String>>();
        PatientLabelRelationService plr = new PatientLabelRelationServiceImpl(this, null);
        List<Label> labels = ls.listLabels();
        HashMap<Long, String> idName = new HashMap<Long, String>();
        for(Iterator<Label> iter = labels.iterator(); iter.hasNext();){
            Label l = iter.next();
            idName.put(l.getId(), l.getLabelName());
        }
        List<Patient> pList = null;
        if(mode.equals(MODE_LIST)){
            pList = pImpl.listAllPatientInfo();
        }else if(mode.equals(MODE_LABEL)){
            List<String> patientIdsTmp = plr.listPatientByLabelId(Long.valueOf(labelId));
            String [] patientIds = patientIdsTmp.toArray(new String[patientIdsTmp.size()]);
            pList = pImpl.listPatientInfoById(patientIds);
        }else if(mode.equals(MODE_GROUPSEND)){
            pList = pImpl.listAllPatientInfo();
            this.isSelectedMap = new HashMap<Integer, Boolean>();
            this.selectedPatients = new LinkedList<Patient>();
        }
        //List<PatientDoctorRel> pdrList = pdrImpl.listRelationInfoByDoctorId(sid);
        //int len = pdrList.size();
        int index = 0;
        for(Iterator<Patient> iter = pList.iterator(); iter.hasNext(); index++){
            //PatientDoctorRel pdrTmp = pdrList.get(i);
            //Patient patient = pImpl.viewPatientInfoByPatientId(pdrTmp.patientId);
            Patient patient = iter.next();
            if(!dataContainer.isAttention(patient.getPatientId())){
                continue;
            }
            patient.setSortLetters(patient.getPinyin().substring(0, 1).toUpperCase());
            contactList.add(patient);
            List<Long> plabels = plr.listLabelByPatientId(patient.getPatientId());
            List<String> plabelString = new ArrayList<String>();
            for(Iterator<Long> iter2 = plabels.iterator(); iter2.hasNext();){
                long id = iter2.next();
                if(idName.containsKey(id)) {
                    plabelString.add(idName.get(id));
                }
            }
            patientLabel.put(patient.getPatientId(), plabelString);
            if(mode.equals(MODE_GROUPSEND)) {
                this.isSelectedMap.put(index, false);
            }
        }
        if(contactList.size() == 0){
            noContentImage.setImageResource(R.drawable.patient_no_content);
            noContentImage.setVisibility(View.VISIBLE);
        }
        else{
            noContentImage.setVisibility(View.GONE);
        }
        return contactList;
    }

    private void loadDataToAdapter(String mode) {
        if(mode.equals(MODE_LIST)) {
            adapter = new PatientListAdapter(this, R.array.contact_func, R.array.contact_icons, SourceDataList, patientLabel, mode, this);
        }
        else if(mode.equals(MODE_LABEL) || mode.equals(MODE_GROUPSEND)){
            adapter = new PatientListAdapter(this, 0, 0, SourceDataList, patientLabel, mode, this);
        }
        lv_contactList.setAdapter(adapter);
    }




    /**
     *
     * @param position
     */
	/*
	private void deleteContact(int position) {
		PatientSortModel model = (PatientSortModel)adapter.getItem(position);
		final String contact_name = model.getName();

		AlertDialog.Builder builder = new AlertDialog.Builder(SearchContactActivity.this);
		builder.setTitle("删除联系人");
		builder.setMessage("确定要删除联系人吗？");
		builder.setPositiveButton("确 定", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				int count = db.deleteContact(contact_name);
				if (count > 0) {
					refresh();
					showToast("删除成功");
				} else {
					showToast("删除失败");
				}

			}
		});
		builder.setNegativeButton("取 消", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
		builder.create().show();
	}
*/
    /**
     *
     * @param position
     */
	/*
	private void editContact(int position) {
		PatientSortModel model = (PatientSortModel)adapter.getItem(position);
		String contact_name = model.getName();

		Intent intent = new Intent();
		intent.putExtra("name", contact_name);
		intent.setAction(Intent.ACTION_EDIT);
		intent.setDataAndType(Uri.parse(MyConstants.CONTENT_URI),
				MyConstants.CONTENT_TYPE_EDIT);
		startActivity(intent);
	}*/

    /**
     *
     * @param msg
     */
    /*
    public void showToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }*/

    /**
     *
     */
    /*public void refresh() {
        initData();
        keywords = et_searchContact.getText().toString().trim();
        if (keywords != null) {
            filterData(keywords);
        }
    }*/

    /**
     *
     * @return
     */
    /*
    private Bitmap getDefaultIcon() {
        Resources res = getResources();
        Bitmap bmp = BitmapFactory.decodeResource(res, R.drawable.defaulthead);
        return bmp;
    }*/

    /**
     *
     * @param filterStr
     */
    /*
    private void filterData(String filterStr){
        List<PatientListModel> filterDateList = new ArrayList<PatientListModel>();
        boolean isFound = false;
        if(TextUtils.isEmpty(filterStr)){
            filterDateList = SourceDateList;
        }else{
            filterDateList.clear();
            for(PatientListModel patientListModel : SourceDateList){
                String name = patientListModel.getName();
                if(name.indexOf(filterStr.toString()) != -1 || Pinyin.convertToPinyin(name).startsWith(filterStr.toLowerCase().toString())){
                    filterDateList.add(patientListModel);
                }
            }
            isFound = true;
        }

        // 根据a-z进行排序
        Collections.sort(filterDateList, pinyinComparator);
        adapter.updateView(filterDateList, isFound);
    }*/

    private void filterData(String filterStr){
        List<Patient> filterDataList = new ArrayList<Patient>();
        boolean isFound = false;
        if(TextUtils.isEmpty(filterStr)){
            filterDataList = SourceDataList;
        }else{
            filterDataList.clear();
            for(Patient patient : SourceDataList){
                String name = patient.getName();
                if(name.indexOf(filterStr.toString()) != -1 || Pinyin.convertToPinyin(name).startsWith(filterStr.toLowerCase().toString())){
                    filterDataList.add(patient);
                }
            }
            isFound = true;
        }

        // 根据a-z进行排序
        Collections.sort(filterDataList, pinyinComparator);
        adapter.updateView(filterDataList, isFound, patientLabel);
    }

    private void refreshPatients(String mode, String labelId){
        SourceDataList = fillData(mode, labelId);
        Collections.sort(SourceDataList, pinyinComparator);
        adapter.updateView(SourceDataList, false, patientLabel);
    }
    /**
     *
     */
    @Override
    protected void onResume() {
        super.onResume();
        nowIn = true;
        if(resumeChanged){
            //initData(mode, labelId);
            refreshPatients(mode, labelId);
            resumeChanged = false;
        }

        /*keywords = et_searchContact.getText().toString().trim();
        if (!keywords.isEmpty()) {
            filterData(keywords);
        }*/
        dataContainer.setInPatientActivity(true);
        MobclickAgent.onResume(this);
    }

    @Override
    protected void onPause(){
        super.onPause();
        nowIn = false;
        MobclickAgent.onPause(this);
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        Logger.i(TAG, "onDestroy");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Logger.i(TAG, "onRestart");
    }

    @Override
    protected void onStop() {
        dataContainer.setInPatientActivity(false);
        super.onStop();
        Logger.i(TAG, "onStop");
    }
}
