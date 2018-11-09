package com.onlinedoctor.activity.tools.survey;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.onlinedoctor.activity.R;
import com.onlinedoctor.net.tools.SurveyNetManager;
import com.onlinedoctor.adapter.tools.BaseQuestionAdapter;
import com.onlinedoctor.log.Logger;
import com.onlinedoctor.pojo.Common;
import com.onlinedoctor.pojo.tools.survey.BaseQuestionDTO;
import com.onlinedoctor.pojo.tools.survey.IMG;
import com.onlinedoctor.pojo.tools.survey.MultiChoicesDTO;
import com.onlinedoctor.pojo.tools.survey.Questionnaire;
import com.onlinedoctor.pojo.tools.survey.SingleChoiceDTO;
import com.onlinedoctor.pojo.tools.survey.SubjectiveDTO;
import com.onlinedoctor.sqlite.dao.QuestionnaireImpl;
import com.onlinedoctor.util.JsonUtil;
import com.onlinedoctor.util.SharedpreferenceManager;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class DiseaseSurveyQuestionsActivity extends Activity {

    private TextView add_tv = null;
    private Button finish_btn = null;
    private Context mContext = this;
    private ListView listView = null;
    private List<BaseQuestionDTO> bqList = new ArrayList();;
    private String title = null;
    private int localId = -1;
    private int globalId = -1;
    private String abstractStr = null;
    private final static int TYPE_CHOICES = 1;
    private final static int TYPE_SUBJECTIVES = 2;
    private QuestionnaireImpl dsImpl = null;
    private BaseQuestionAdapter bqAdapter = null;
    private FrameLayout progressBarHolder;


    private static final String TAG = "SurveyQuestion";
    private final Handler mhandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            progressBarHolder.setVisibility(View.GONE);
            if (msg.what == SurveyNetManager._msg_insert_questionnaire) {
                try {
                    JSONObject jo = new JSONObject(msg.obj.toString());
                    int localId = msg.arg1;
                    int globalId = jo.getInt("id");
                    long time = jo.getLong("time");
                    Questionnaire q = new Questionnaire();
                    q.localId = localId;
                    q.setId(globalId);
                    q.setTimestamp(time);
                    q.localId = msg.arg1;
                    dsImpl.updateQuestionnaire(q);
                    Toast.makeText(mContext, "添加成功", Toast.LENGTH_SHORT).show();
                    DiseaseSurveyQuestionsActivity.this.finish();
                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    Toast.makeText(mContext, "无法解析服务器数据", Toast.LENGTH_SHORT).show();
                }

            } else if (msg.what == SurveyNetManager._msg_update_questionnaire) {
                try {
                    JSONObject jo = new JSONObject(msg.obj.toString());
                    long time = jo.getLong("time");
                    int globalId = jo.getInt("id");
                    Questionnaire q = new Questionnaire();
                    q.setTimestamp(time);
                    q.setId(globalId);
                    q.localId = -1;
                    QuestionnaireImpl dsImpl = new QuestionnaireImpl(mContext, null);
                    if (dsImpl.updateQuestionnaire(q)) {
                        Toast.makeText(mContext, "修改成功", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(mContext, "修改失败", Toast.LENGTH_SHORT).show();
                    }
                    DiseaseSurveyQuestionsActivity.this.finish();
                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                    Toast.makeText(mContext, "无法解析服务器数据", Toast.LENGTH_SHORT).show();
                }
            } else if (msg.what == SurveyNetManager._msg_fail) {
                Toast.makeText(mContext, "提交服务器数据失败", Toast.LENGTH_SHORT).show();
            } else if (msg.what == SurveyNetManager._msg_unknown_exception) {
                Toast.makeText(mContext, "未知错误", Toast.LENGTH_SHORT).show();
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.disease_survey_create_questions);

        TextView title_tv = (TextView) findViewById(R.id.actionbar_common_backable_title_tv);
        add_tv = (TextView) findViewById(R.id.actionbar_common_backable_right_tv);
        finish_btn = (Button) findViewById(R.id.finish_btn);
        add_tv.setText("添加题目");
        Intent intent = getIntent();
        String className = intent.getStringExtra("fromActivity");


        dsImpl = new QuestionnaireImpl(mContext, null);
        Questionnaire q = null;
        if ("ChatActivity".equals(className)) {
            globalId = (int) intent.getLongExtra("globalId", -1);
            q = dsImpl.getQuestionnairebyGlobalId(globalId);
            title_tv.setText(q.getName());
        } else if ("DiseaseSurveyActivity".equals(className)) {
            title = intent.getStringExtra("title");
            localId = intent.getIntExtra("localId", -1);
            abstractStr = "";
            globalId = intent.getIntExtra("globalId", -1);
            title_tv.setText(title);
            q = dsImpl.getQuestionnairebyLocalId(localId);
        } else if ("DiseaseSurveyCreateNew".equals(className)) {
            title = intent.getStringExtra("title");
            title_tv.setText(title);
        }
        
        if(q != null)
            bqList = q.toQuesionDTOList();

        listView = (ListView) findViewById(R.id.lv_disease_survey);
        bqAdapter = new BaseQuestionAdapter(mContext, R.layout.common_listview_text_item_3, bqList);
        listView.setAdapter(bqAdapter);
        progressBarHolder = (FrameLayout) findViewById(R.id.progressBarHolder);
        add();
        clickItem();
        finish_create_questions();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        BaseQuestionDTO bq = null;
        switch (requestCode) {
            case TYPE_CHOICES:
                if (resultCode == RESULT_OK) {
                    Logger.d(TAG, "通过Intent获得刚新创建的:选择题");
                    Bundle bundle = data.getExtras();
                    bq = (BaseQuestionDTO) bundle.getSerializable("ChoicesQuestion");
                }
                break;
            case TYPE_SUBJECTIVES:
                if (resultCode == RESULT_OK) {
                    Logger.d(TAG, "通过Intent获得刚新创建的：主观题");
                    Bundle bundle = data.getExtras();
                    bq = (BaseQuestionDTO) bundle.getSerializable("SubjectiveQuestion");
                }
                break;
            default:

                break;
        }

        if (bq != null) {

            int position = data.getIntExtra("position", -1);
            if (position != -1) {
                bqList.set(position, bq);
            } else {
                bqList.add(bq);
            }
            bqAdapter.notifyDataSetChanged();

        }
    }


    private void add() {
        add_tv.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {

                AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                builder.setTitle("");
                builder.setItems(R.array.disease_survey_question_types, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = null;
                        switch (which) {
                            case 0://选择题
                                intent = new Intent(mContext, DiseaseSurveyQuestionTypeChoices.class);
                                startActivityForResult(intent, TYPE_CHOICES);
                                break;
                            case 1://主观题
                                intent = new Intent(mContext, DiseaseSurveyQuestionTypeSubjective.class);
                                startActivityForResult(intent, TYPE_SUBJECTIVES);
                                break;
                            default:

                                break;
                        }
                    }
                });
                builder.show();

            }
        });
    }

    private void clickItem() {
        listView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View view, int position,
                                    long id) {

                Log.d(TAG, "update & edit question");
                BaseQuestionDTO bq = bqList.get(position);
                int type = bq.getType();
                Intent intent = null;
                Bundle bundle = null;
                switch (type) {
                    case Common.TYPE_SINGLE_CHOICE:
                        intent = new Intent(mContext, DiseaseSurveyQuestionTypeChoices.class);
                        bundle = new Bundle();
                        bundle.putSerializable("ChoicesQuestion", bq);
                        bundle.putInt("position", position);
                        intent.putExtras(bundle);
                        startActivityForResult(intent, TYPE_CHOICES);
                        break;
                    case Common.TYPE_MUTIL_CHOICES:
                        intent = new Intent(mContext, DiseaseSurveyQuestionTypeChoices.class);
                        bundle = new Bundle();
                        bundle.putSerializable("ChoicesQuestion", bq);
                        bundle.putInt("position", position);
                        intent.putExtras(bundle);
                        startActivityForResult(intent, TYPE_CHOICES);
                        break;
                    case Common.TYPE_SUBJECTIVE:
                        intent = new Intent(mContext, DiseaseSurveyQuestionTypeSubjective.class);
                        bundle = new Bundle();
                        bundle.putSerializable("SubjectiveQuestion", bq);
                        bundle.putInt("position", position);
                        intent.putExtras(bundle);
                        startActivityForResult(intent, TYPE_SUBJECTIVES);
                        break;
                    case Common.TYPE_IMAGE:
                        intent = new Intent(mContext, DiseaseSurveyQuestionTypeSubjective.class);
                        bundle = new Bundle();
                        bundle.putSerializable("SubjectiveQuestion", bq);
                        bundle.putInt("position", position);
                        intent.putExtras(bundle);
                        startActivityForResult(intent, TYPE_SUBJECTIVES);
                        break;
                    default:
                        break;
                }

            }
        });

        listView.setOnItemLongClickListener(new OnItemLongClickListener() {

            @Override
            public boolean onItemLongClick(AdapterView<?> arg0, View view,
                                           final int position, long id) {
                new AlertDialog.Builder(mContext)
                        .setTitle("提示")
                        .setMessage("如何处理?")
                        .setNegativeButton("删除",
                                new DialogInterface.OnClickListener() {

                                    @Override
                                    public void onClick(DialogInterface dialog,
                                                        int which) {
                                        //Quesn bq = bqList.get(position);

                                        //Log.d(TAG, "从本地db中删除该问卷");
                                        //dsImpl.deleteQuestion(bq);

                                        Log.d(TAG, "更新调查问卷的listview");
                                        bqList.remove(position);
                                        bqAdapter.notifyDataSetChanged();

                                        dialog.dismiss();
                                    }
                                })
                        .setPositiveButton("取消",
                                new DialogInterface.OnClickListener() {

                                    @Override
                                    public void onClick(DialogInterface dialog,
                                                        int which) {
                                        dialog.dismiss();
                                    }
                                }).show();
                return true;
            }
        });
    }


    private void finish_create_questions() {
        finish_btn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                int i = 1;
                for (BaseQuestionDTO q : bqList) {
                    q.setId(i++);
                }
                JSONObject jo = new JSONObject();
                try {
                    jo.put("qNum", bqList.size());
                    JSONArray obj = new JSONArray(JsonUtil.objectToJson(bqList));
                    //for(BaseQuestionDTO q : bqList){
                    //	obj.put(new JSONObject(JsonUtil.toMap(q)));
                    //}
                    jo.put("qList", obj);
                    QuestionnaireImpl dsImpl = new QuestionnaireImpl(mContext, null);
                    Questionnaire q = new Questionnaire();
                    q.localId = localId;
                    q.setTimestamp(-1);
                    q.setQuestionJson(jo.toString());
                    q.setName(title);
                    q.setId(globalId);
                    SharedpreferenceManager spManager = SharedpreferenceManager.getInstance();
                    String sid = spManager.getOne("keySid");
                    q.setDoctorId(sid);
                    if (localId == -1) {
                        localId = (int) dsImpl.addQuestionnaire(q);
                        q.localId = localId;
                    } else {
                        dsImpl.updateQuestionnaire(q);
                    }
                    progressBarHolder.setVisibility(View.VISIBLE);
                    if (globalId == -1) {
                        SurveyNetManager.insert(localId, title, jo.toString(), mhandler);
                    } else {
                        SurveyNetManager.update(localId, title, jo.toString(), globalId, mhandler);
                    }
                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        });
    }
}
