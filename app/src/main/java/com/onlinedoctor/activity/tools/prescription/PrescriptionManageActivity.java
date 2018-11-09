package com.onlinedoctor.activity.tools.prescription;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.google.gson.internal.LinkedTreeMap;
import com.onlinedoctor.activity.R;
import com.onlinedoctor.adapter.patient.PatientInfoAdapter;
import com.onlinedoctor.log.Logger;
import com.onlinedoctor.pojo.Common;
import com.onlinedoctor.pojo.patient.PatientDoctorRel;
import com.onlinedoctor.pojo.patient.Record;
import com.onlinedoctor.sqlite.dao.PrescriptionServiceImpl;
import com.onlinedoctor.sqlite.dao.RecordServiceImpl;
import com.onlinedoctor.sqlite.service.PrescriptionService;
import com.onlinedoctor.sqlite.service.RecordService;
import com.onlinedoctor.util.JsonUtil;
import com.onlinedoctor.util.SharedpreferenceManager;
import com.onlinedoctor.view.CommonActionbarBackableRelativeLayout;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

/**
 * Created by xuweidong on 15/10/31.
 * 处方管理
 */
public class PrescriptionManageActivity extends Activity{

    private static final String TAG = "PrescriptionManageActivity";
    private ListView listView = null;
    private ImageView noContentView;

    private PrescriptionService prescriptionService;
    private RecordService recordService;
    private PatientInfoAdapter adapter;
    private List<List<Record>> inputList;
    private List<Record> allRecordList = null;
    private List<String> allRecordUrlList = null;
    private HashMap<Long, List<String>> prescriptions = null;
    private SharedpreferenceManager spManager = SharedpreferenceManager.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tool_presmanage);

        prescriptionService = new PrescriptionServiceImpl();
        recordService = new RecordServiceImpl(null,null);

        listView = (ListView) findViewById(R.id.listView_question);
        noContentView = (ImageView) findViewById(R.id.patient_no_content_pic);
        CommonActionbarBackableRelativeLayout cabrl = (CommonActionbarBackableRelativeLayout) findViewById(R.id.patient_info_bar);
        cabrl.setTitle("患者详情");
        cabrl.setRight("");
        loadData();
    }

    private void loadData(){
        fillData();
        if(allRecordUrlList.size() == 0){
            noContentView.setImageResource(R.drawable.patient_no_content);
            noContentView.setVisibility(View.VISIBLE);
        }
        else{
            noContentView.setVisibility(View.GONE);
        }
        adapter = new PatientInfoAdapter(PrescriptionManageActivity.this, null, inputList, allRecordUrlList, prescriptions);
        listView.setAdapter(adapter);
    }

    private void fillData(){
        String doctorId = spManager.getOne("keySid");

        inputList = new ArrayList<List<Record>>();
        allRecordList = recordService.listRecordsByType(Record.TYPE_PRESCRIPTION);
        allRecordUrlList = new ArrayList<String>();
        prescriptions = new HashMap<>();
        if(allRecordList.size() == 0){
            //to do
        }
        else{
            long oneDay = 3600000*24, lastDay = -1;
            List<Record> oneList = null;
            for(Iterator<Record> iter = allRecordList.iterator(); iter.hasNext();){
                Record record = iter.next();
                if(record.getRecordType() == Record.TYPE_PRESCRIPTION){
                    allRecordUrlList.add(record.getThumbnail());
                    List<LinkedTreeMap<String, String>> images = (List<LinkedTreeMap<String, String>>) JsonUtil.jsonToList(record.getRecordPic());
                    List<String> urls = new ArrayList<>();
                    for(LinkedTreeMap<String,String> item:images){
                        urls.add(item.get(Common.KEY_IMAGE_avaterPath));
                    }
                    prescriptions.put(record.getId(), urls);
                    Logger.i(TAG, "prescriptions: " + prescriptions.toString());
                }else{
                    allRecordUrlList.add(record.getRecordPic());
                }
                long curDay = record.getCreated() / oneDay;
                if(curDay != lastDay){
                    oneList = new ArrayList<Record>();
                    inputList.add(oneList);
                    lastDay = curDay;
                }
                oneList.add(record);
            }
        }
    }
}
