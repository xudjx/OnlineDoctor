package com.onlinedoctor.activity.tools.survey;

import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.onlinedoctor.activity.R;
import com.onlinedoctor.net.tools.SurveyNetManager;
import com.onlinedoctor.adapter.tools.DiseaseSurveyAdapter;
import com.onlinedoctor.pojo.tools.survey.Questionnaire;
import com.onlinedoctor.sqlite.dao.QuestionnaireImpl;

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
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class DiseaseSurveyActivity extends Activity {
    private TextView add_tv = null;
    private ListView listView = null;
    private ImageView bg_iv = null;
    private List<Questionnaire> dsList = null;
    private DiseaseSurveyAdapter dsAdapter = null;
    private Context mContext = this;
    private QuestionnaireImpl dsImpl = null;
    private FrameLayout progressBarHolder;
    private final String TAG = "DiseaseSurveyActivity";

    private final Handler diseaseSurveyActivityHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == SurveyNetManager._msg_get_questionnaire) {
                try {
                    JSONObject jo = new JSONObject(msg.obj.toString());
                    int isNew = jo.getInt("isNew");
                    if (isNew == 0) {
                        JSONArray ja = jo.getJSONArray("synObject");
                        for (int i = 0; i < ja.length(); i++) {
                            JSONObject tmp = ja.getJSONObject(i);
                            if (tmp.getInt("isDelete") == 1)
                                dsImpl.deleteQuestionnaireByGlobalId(tmp.getInt("id"));
                            else {
                                Questionnaire qa = Questionnaire.fromJO(tmp);
                                qa.localId = -1;
                                dsImpl.updateQuestionnaire(qa);
                            }
                        }
                        dsList.clear();
                        dsList.addAll(dsImpl.getAllQuestionnaire());
                        dsAdapter.notifyDataSetChanged();
                    }
                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    Toast.makeText(mContext, "无法解析服务器数据", Toast.LENGTH_SHORT).show();
                }

            } else if (msg.what == SurveyNetManager._msg_delete_questionnaire) {
                Toast.makeText(mContext, "删除成功", Toast.LENGTH_SHORT).show();
            } else if (msg.what == SurveyNetManager._msg_fail) {

                Toast.makeText(mContext, "请求失败", Toast.LENGTH_SHORT).show();
            } else if (msg.what == SurveyNetManager._msg_unknown_exception) {
                Toast.makeText(mContext, "未知错误", Toast.LENGTH_SHORT).show();
            }
            progressBarHolder.setVisibility(View.GONE);
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.common_listview);

        TextView title_tv = (TextView) findViewById(R.id.actionbar_common_backable_title_tv);
        title_tv.setText("病情调查");
        add_tv = (TextView) findViewById(R.id.actionbar_common_backable_right_tv);
        add_tv.setText("添加");
        bg_iv = (ImageView) findViewById(R.id.bg_iv);

        dsImpl = new QuestionnaireImpl(this, null);
        dsList = dsImpl.getAllQuestionnaire();
        dsAdapter = new DiseaseSurveyAdapter(this,
                R.layout.item_labels_3, dsList);
        listView = (ListView) findViewById(R.id.common_lv);
        listView.setAdapter(dsAdapter);
        progressBarHolder = (FrameLayout) findViewById(R.id.progressBarHolder);
        add();
        clickItem();
        queryChange();
    }

    @Override
    protected void onResume() {
        super.onResume();
        dsList.clear();
        dsList.addAll(dsImpl.getAllQuestionnaire());
        dsAdapter.notifyDataSetChanged();

		/* 背景图何时出现 */
        if (dsList.isEmpty()) {
            bg_iv.setVisibility(View.VISIBLE);
            listView.setVisibility(View.GONE);
        } else {
            bg_iv.setVisibility(View.GONE);
            listView.setVisibility(View.VISIBLE);
        }

    }

    private void add() {
        add_tv.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                Intent intent = new Intent(DiseaseSurveyActivity.this,
                        DiseaseSurveyCreateNew.class);
                startActivity(intent);
            }
        });
    }

    private void clickItem() {
        // short click
        listView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View view,
                                    final int position, long id) {
                Log.d(TAG, "查看调查问卷中的所有题目列表");
                Intent intent = new Intent(mContext, DiseaseSurveyQuestionsActivity.class);
                intent.putExtra("fromActivity", "DiseaseSurveyActivity");
                intent.putExtra("title", dsList.get(position).getName());
                intent.putExtra("abstractStr", "");
                intent.putExtra("globalId", dsList.get(position).getId());
                intent.putExtra("localId", dsList.get(position).localId);
                if ("selectQuestionnaire".equals(getIntent().getType())) {
                    setResult(RESULT_OK, getIntent().putExtra("globalId", dsList.get(position).getId()));
                    DiseaseSurveyActivity.this.finish();
                    return;
                }
                startActivity(intent);
            }
        });

        // long click
        listView.setOnItemLongClickListener(new OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> arg0, View view,
                                           final int position, long id) {

                new AlertDialog.Builder(mContext)
                        .setTitle("提示")
                        .setMessage("如何处理?")
                        .setNegativeButton("编辑问卷",
                                new DialogInterface.OnClickListener() {

                                    @Override
                                    public void onClick(DialogInterface dialog,
                                                        int which) {
                                        Intent intent = new Intent(DiseaseSurveyActivity.this, DiseaseSurveyUpdate.class);
                                        intent.putExtra("title", dsList.get(position).getName());
                                        intent.putExtra("abstractStr", "");
                                        intent.putExtra("questionJson", dsList.get(position).getQuestionJson());
                                        intent.putExtra("localId", dsList.get(position).localId);
                                        intent.putExtra("globalId", dsList.get(position).getId());
                                        startActivity(intent);
                                    }
                                })
                        .setNeutralButton("删除问卷",
                                new DialogInterface.OnClickListener() {

                                    @Override
                                    public void onClick(DialogInterface dialog,
                                                        int which) {
                                        Questionnaire ds = dsList.get(position);

                                        Log.d(TAG, "从本地db中删除该问卷");
                                        dsImpl.deleteQuestionnaireByLocalId(ds.localId);


                                        Log.d(TAG, "更新调查问卷的listview");
                                        dsList.remove(position);
                                        dsAdapter.notifyDataSetChanged();

                                        dialog.dismiss();
                                        //向远程服务器请求删除
                                        progressBarHolder.setVisibility(View.VISIBLE);
                                        SurveyNetManager.delete(ds.getId(), diseaseSurveyActivityHandler);
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

    void queryChange() {
        long time = dsImpl.getLastUpdate();
        progressBarHolder.setVisibility(View.VISIBLE);
        SurveyNetManager.get(time, diseaseSurveyActivityHandler);
    }
}
