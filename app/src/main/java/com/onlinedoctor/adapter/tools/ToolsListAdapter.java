package com.onlinedoctor.adapter.tools;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.onlinedoctor.activity.R;

/**
 * Created by wds on 15/8/7.
 */
public class ToolsListAdapter extends BaseAdapter {

    private final Context mContext;
    private final String[] funcs;
    private final TypedArray images;

    public ToolsListAdapter(Context mContext, int funcArrayRes, int imageArrayRes) {
        this.mContext = mContext;
        final Resources res = this.mContext.getResources();
        this.funcs = res.getStringArray(funcArrayRes);
        this.images = res.obtainTypedArray(imageArrayRes);
    }

    @Override
    public int getCount() {
        return funcs.length;
    }

    @Override
    public Object getItem(int i) {
        return funcs[i];
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder2 vh2 = null;
        int type = getItemViewType(position);
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.item_mine_general, null);
            vh2 = new ViewHolder2();
            vh2.icon = (ImageView) convertView.findViewById(R.id.mine_icon);
            vh2.title = (TextView) convertView.findViewById(R.id.mine_title);
            convertView.setTag(vh2);

        } else {
            vh2 = (ViewHolder2) convertView.getTag();
        }
        vh2.icon.setImageDrawable(images.getDrawable(position));
        vh2.title.setText(funcs[position]);
        return convertView;
    }

    private static class ViewHolder2 {
        ImageView icon;
        TextView title;
    }
}
