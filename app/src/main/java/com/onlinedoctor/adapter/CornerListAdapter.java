package com.onlinedoctor.adapter;

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
 * �����������ר��Ϊ���ֶ�����List���������
 * ����ͨѶ¼����� �������� ��ǩ��
 */
public class CornerListAdapter extends BaseAdapter {

	private final Context mContext;
	private final String[] funcs;
	private final TypedArray images; 
	private final LayoutInflater listContainer;

	public final class ListItemView { // �Զ���ؼ�����
		public TextView Title;
		public ImageView image;
	}

	public CornerListAdapter(Context context,int funcArrayRes,int imageArrayRes) {
		this.mContext = context;
		listContainer = LayoutInflater.from(mContext);
		final Resources res = context.getResources();
		this.funcs = res.getStringArray(funcArrayRes);
		this.images = res.obtainTypedArray(imageArrayRes);
	}

	public int getCount() {
		return funcs.length;
	}

	public Object getItem(int position) {
		return funcs[position];
	}

	public long getItemId(int position) {
		return position;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		ListItemView holder = null;
		if (convertView == null) {
			convertView = listContainer.inflate(R.layout.corner_list_item, null);
			holder = new ListItemView();
			// ��ȡ�ؼ�����
			holder.Title = (TextView) convertView.findViewById(R.id.title);
			holder.image = (ImageView) convertView.findViewById(R.id.imageView);
			convertView.setTag(holder);
		} else {
			holder = (ListItemView) convertView.getTag();
		}

		holder.Title.setText(funcs[position]);	
		holder.image.setImageDrawable(images.getDrawable(position));
		return convertView;
	}
}
