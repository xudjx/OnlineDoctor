package com.onlinedoctor.adapter.mine;

import java.util.HashMap;

import android.app.ActionBar;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.onlinedoctor.activity.R;
import com.onlinedoctor.pojo.Common;
import com.onlinedoctor.util.BadgeViewUtil;
import com.onlinedoctor.util.SharedpreferenceManager;
import com.onlinedoctor.view.BadgeView;

import org.w3c.dom.Text;

public class MineListAdapter extends BaseAdapter {

    private final Context mContext;
    private final String[] funcs;
    private final TypedArray images;

    private static class ViewHolder1 {
        ImageView head, twodcode;
        TextView name, id, idContent;
    }

    private static class ViewHolder2 {
        ImageView icon;
        TextView title;
    }

    public MineListAdapter(Context mContext, int funcArrayRes, int imageArrayRes) {
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
    public Object getItem(int position) {

        return funcs[position];
    }


    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View view = LayoutInflater.from(mContext).inflate(R.layout.item_mine_general, null);
        ImageView icon = (ImageView) view.findViewById(R.id.mine_icon);
        TextView title = (TextView) view.findViewById(R.id.mine_title);
        icon.setImageDrawable(images.getDrawable(position));
        title.setText(funcs[position]);


        //添加钱包提示红点
        BadgeView walletBadge = new BadgeView(mContext, title, BadgeView.POSITION_RIGHT,0);
        walletBadge.hide();

        if (position == 2) {
            //title.setLayoutParams(new ActionBar.LayoutParams(10, 20));
            title.setText(funcs[position] + "      ");
            BadgeViewUtil.paymentSetBadgeView(walletBadge);
        }
        return view;
    }

}
