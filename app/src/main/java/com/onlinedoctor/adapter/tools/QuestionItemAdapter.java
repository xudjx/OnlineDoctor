package com.onlinedoctor.adapter.tools;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.onlinedoctor.activity.R;
import java.util.List;

/**
 * Created by wds on 15/11/15.
 *
 * 显示选择题选项的Adapter
 *
 */
public class QuestionItemAdapter extends ArrayAdapter<String> {
    private LayoutInflater inflater;
    private int resourceId;
    private List<String> list;
    private static final char BEGIN_TAG = 'A'; //选项的起始编号，这里从A开始，依次为B，C，D，E....
    private static final String SPLIT_TAG = "."; //用于分割编号和内容选项之前，这里显示的效果类似于"A) xxxxx", "B) xxxxx" .....
    public QuestionItemAdapter(Context context, int resource, List<String> objects) {
        super(context, resource,  objects);
        inflater = LayoutInflater.from(context);
        resourceId = resource;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        String content = getItem(position);
        int pos = ((int)BEGIN_TAG) + position; //起始选项编号，这里是char的int类型表示
        String serial = ""+(char)pos + SPLIT_TAG; //转化为String型，这里从A开始，依次是B，C，D，E....
        if(convertView == null){
            /**首次加载*/
            Holder holder = new Holder();
            convertView = inflater.inflate(R.layout.item_choice,null);

            holder.serial = (TextView)convertView.findViewById(R.id.serial);
            holder.content = (TextView)convertView.findViewById(R.id.content);
            holder.serial.setText(serial);
            holder.content.setText(content);

            convertView.setTag(holder);
        }else{
            /**第n加载(n > 1)*/
            Holder holder = (Holder) convertView.getTag();
            holder.serial.setText(serial);
            holder.content.setText(content);
        }
        return convertView;
    }

    class Holder{
        public TextView serial;
        public TextView content;
    }
}
