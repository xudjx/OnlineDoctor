package com.onlinedoctor.adapter.tools;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.onlinedoctor.activity.R;
import com.onlinedoctor.log.Logger;
import com.onlinedoctor.pojo.Common;
import com.onlinedoctor.pojo.tools.survey.BaseQuestionDTO;
import com.onlinedoctor.util.ListViewUtil;


public class BaseQuestionAdapter extends ArrayAdapter<BaseQuestionDTO> {
    private List<BaseQuestionDTO> bqList;
    private LayoutInflater mInflater;
    private int resourceId;
    private Context mContext;
    private static final int BEGIN_TAG = 1; //选项的起始编号，这里从1开始，依次为1，2，3，4....
    private static final String SPLIT_TAG = "."; //用于分割编号和内容选项之前，这里显示的效果类似于"1. xxxxx", "2. xxxxx" .....
    public BaseQuestionAdapter(Context context, int resource, List<BaseQuestionDTO> objects) {
        super(context, resource, objects);
        mContext = context;
        resourceId = resource;
        this.bqList = objects;
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getViewTypeCount() {
        return Common.SURVEY_QUESTION_TYPE_NUM;
    }

    @Override
    public int getItemViewType(int position) {
        BaseQuestionDTO bq = bqList.get(position);
        int type = bq.getType();
        Logger.d("TYPE:", "" + type);
        return type;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        BaseQuestionDTO bq = getItem(position);
        int type = getItemViewType(position);
        ViewHolderChoice holderChoice;
        ViewHolderSubjective holderSubjective;
        int pos = ((int)BEGIN_TAG) + position; //起始选项编号，这里是char的int类型表示
        String serial = Integer.toString(pos) + SPLIT_TAG; //转化为String型
        if (convertView == null) {
            if (type == Common.TYPE_SINGLE_CHOICE || type == Common.TYPE_MUTIL_CHOICES) { //单选题 & 多选题
                holderChoice = new ViewHolderChoice();
                convertView = mInflater.inflate(R.layout.item_survey_choices, null);
                holderChoice.titleName = (TextView) convertView.findViewById(R.id.labelname);
                holderChoice.typeName = (TextView) convertView.findViewById(R.id.labelcount);
                holderChoice.listChoices = (ListView) convertView.findViewById(R.id.list_choices);

                holderChoice.titleName.setText( serial + bq.getTitle());
                holderChoice.typeName.setText(Common.getTypeName(mContext, bq.getType()));

                holderChoice.listChoices.setAdapter(
                        new QuestionItemAdapter(mContext,R.layout.item_choice, bq.getChoicesList()));
                /**
                 * 根据ListView的子项目重新计算ListView的高度，然后把高度再作为LayoutParams设置给ListView，
                 * 这样它的高度就正确了，
                 */
                ListViewUtil.setListViewHeightBasedOnChildren(holderChoice.listChoices);
                convertView.setTag(holderChoice);

            } else if (type == Common.TYPE_IMAGE || type == Common.TYPE_SUBJECTIVE) {
                holderSubjective = new ViewHolderSubjective();
                convertView = mInflater.inflate(R.layout.item_survey_choices, null); //主观题 & 图像题的显示效果
                holderSubjective.titleName = (TextView) convertView.findViewById(R.id.labelname);
                holderSubjective.typeName = (TextView) convertView.findViewById(R.id.labelcount);

                holderSubjective.titleName.setText(serial + bq.getTitle());
                holderSubjective.typeName.setText(Common.getTypeName(mContext, bq.getType()));

                convertView.setTag(holderSubjective);
            }
        } else {
            if (type == Common.TYPE_SINGLE_CHOICE || type == Common.TYPE_MUTIL_CHOICES) { //单选题 & 多选题
                holderChoice = (ViewHolderChoice) convertView.getTag();
                holderChoice.titleName.setText(serial + bq.getTitle());
                holderChoice.typeName.setText(Common.getTypeName(mContext, bq.getType()));
                holderChoice.listChoices.setAdapter(
                        new QuestionItemAdapter(mContext, R.layout.item_choice, bq.getChoicesList()));
                /**
                 * 根据ListView的子项目重新计算ListView的高度，然后把高度再作为LayoutParams设置给ListView，
                 * 这样它的高度就正确了，
                 */
                ListViewUtil.setListViewHeightBasedOnChildren(holderChoice.listChoices);
            } else if (type == Common.TYPE_IMAGE || type == Common.TYPE_SUBJECTIVE) { //主观题 & 图像题的显示效果
                holderSubjective = (ViewHolderSubjective) convertView.getTag();
                holderSubjective.titleName.setText(serial+ bq.getTitle());
                holderSubjective.typeName.setText(Common.getTypeName(mContext, bq.getType()));
            }
        }

        return convertView;
    }

    class ViewHolderChoice {
        public TextView titleName;
        public TextView typeName;
        public ListView listChoices;
    }

    class ViewHolderSubjective {
        public TextView titleName;
        public TextView typeName;
    }

}
