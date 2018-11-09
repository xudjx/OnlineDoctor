package com.onlinedoctor.activity.tools.survey;

import java.util.ArrayList;
import java.util.List;

import com.onlinedoctor.activity.R;
import com.onlinedoctor.adapter.tools.DiseaseSurveyQuestionTypeChoicesAdapter;
import com.onlinedoctor.pojo.Common;
import com.onlinedoctor.pojo.tools.survey.BaseQuestionDTO;
import com.onlinedoctor.util.BaseQuestionFactory;
import com.onlinedoctor.util.DensityUtil;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

public class DiseaseSurveyQuestionTypeChoices extends Activity {
    private Button okBtn = null;
    private RelativeLayout addIcon = null;
    private EditText question_et = null;
    private RadioGroup radioGroup = null;
    private ListView listView = null;
    private List<EditText> dataList = new ArrayList();
    private static final String TAG = "DiseaseSurveyQuestionTypeChoices";
    private int type = Common.TYPE_SINGLE_CHOICE;
    private Context mContext = null;
    private LinearLayout choicesLayout = null;
    private DiseaseSurveyQuestionTypeChoicesAdapter adapter = null;

    /**
     * 以下四个这样构成:
     * BEGIN_TAG + SPLIT_TAG + " " + ITEM_HINT + (++item_num);
     * 例如：A) 选项1
     */
    private static final char BEGIN_TAG = 'A';
    private static final String SPLIT_TAG = ")";
    private static final String ITEM_HINT = "选项";
    private static int item_num = 0;

    private static final int DEFAULT_CHOICES_ITEM_NUM = 2;
    private Switch switchView = null;
    private LinearLayout.LayoutParams layoutParams =
            new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.FILL_PARENT,
                    LinearLayout.LayoutParams.FILL_PARENT);


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.disease_survey_question_type_choices);
        mContext = DiseaseSurveyQuestionTypeChoices.this;
        okBtn = (Button) findViewById(R.id.bt_ok);
        addIcon = (RelativeLayout) findViewById(R.id.rl_add);
        TextView title_tv = (TextView) findViewById(R.id.actionbar_common_backable_title_tv);
        title_tv.setText(getResources().getString(R.string.survey_choices));
        TextView right_tv = (TextView) findViewById(R.id.actionbar_common_backable_right_tv);
        right_tv.setText("");
        question_et = (EditText) findViewById(R.id.et_title);
        choicesLayout = (LinearLayout) findViewById(R.id.choices);
        adapter = new DiseaseSurveyQuestionTypeChoicesAdapter(mContext, R.layout.edittext_survey_choices, dataList);
        switchView = (Switch) findViewById(R.id.switch_view);
        layoutParams.setMargins(
                DensityUtil.dip2px(mContext, 15),
                DensityUtil.dip2px(mContext, -1),
                DensityUtil.dip2px(mContext, 15),
                DensityUtil.dip2px(mContext, -1));

        item_num = 0;
        update();
        switchViewCheck();
        addItem();
        ok();
    }

    private void addItem() {
        addIcon.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                createEditText("", item_num++);
            }
        });
    }

    private void switchViewCheck() {
        switchView.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked == true) {
                    type = Common.TYPE_MUTIL_CHOICES;
                } else {
                    type = Common.TYPE_SINGLE_CHOICE;
                }
            }
        });
    }

    private void ok() {
        okBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                String question = question_et.getText().toString();
                Log.d("question = ", question);
                if (question.isEmpty()) {
                    Toast.makeText(mContext, "题目不能为空", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (type == -1) {
                    Toast.makeText(mContext, "请选择题目类型", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (dataList.isEmpty()) {
                    Toast.makeText(mContext, "选项不能为空1", Toast.LENGTH_SHORT).show();
                    return;
                }
                Intent intent = new Intent();
                Bundle bundle = new Bundle();

                BaseQuestionDTO q = BaseQuestionFactory.instance(type);
                List<String> list = new ArrayList();
                for (EditText data : dataList) {
                    String item = data.getText().toString();
                    if (!item.isEmpty())
                        list.add(item);
                }
                q.setChoicesList(list);
                q.setTitle(question);
                bundle.putSerializable("ChoicesQuestion", q);
                bundle.putInt("position", getIntent().getIntExtra("position", -1));
                intent.putExtras(bundle);
                setResult(RESULT_OK, intent);
                finish();
            }
        });
    }

    private void update() {
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        if (bundle != null) {
            BaseQuestionDTO bq = (BaseQuestionDTO) bundle.getSerializable("ChoicesQuestion");
            question_et.setText(bq.getTitle());
            type = bq.getType();
            List<String> options = bq.getChoicesList();
            for (String option : options) {
                createEditText(option, item_num++);
            }
        } else {
            /**
             * 自动添加默认两个选项EditText
             */
            for (int i = 0; i < DEFAULT_CHOICES_ITEM_NUM; i++) {
                createEditText("", item_num++);
            }
        }
    }

    private void createEditText(String str, int num) {
        EditText editText = new EditText(mContext);
        editText.setBackground(getResources().getDrawable(R.drawable.bg_edittext));
        editText.setLayoutParams(layoutParams);
        editText.setPadding(
                DensityUtil.dip2px(mContext, 10),
                DensityUtil.dip2px(mContext, 10),
                DensityUtil.dip2px(mContext, 10),
                DensityUtil.dip2px(mContext, 10));
        int pos = ((int) BEGIN_TAG) + num; //起始选项编号，这里是char的int类型表示

        String hint = ("" + (char)pos) + SPLIT_TAG + " " + ITEM_HINT + "" + (++num);
        editText.setHint(hint);
        editText.setText(str);
        //editText.setTextSize(DensityUtil.sp2px(mContext,));
        dataList.add(editText);
        choicesLayout.addView(editText);
    }
}
