package com.onlinedoctor.adapter.tools;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.onlinedoctor.activity.R;

public class ToolsImageAdapter extends BaseAdapter {

	private final Context context;
	private final String[] funcs;
	private final TypedArray images;
	private final LayoutInflater container;

	private final class GridItemView {
		public TextView func;
		public ImageView image;
	}

	@SuppressLint("Recycle")
	public ToolsImageAdapter(Context context, int funcArrayRes, int imageArrayRes) {
		super();
		this.context = context;
		this.funcs = context.getResources().getStringArray(funcArrayRes);
		this.images = context.getResources().obtainTypedArray(imageArrayRes);
		container = LayoutInflater.from(context);
		Log.i("ImageAdapter", "imageAdapter init success" + funcs.length);
	}

	public int getCount() {
		return funcs.length;
	}

	public Object getItem(int position) {
		return funcs[position];
	}

	public long getItemId(int position) {
		Log.i("ImageAdapter", "get item:" + funcs.length);
		return position;
	}

	public View getView(int position, View view, ViewGroup parentGroup) {
		GridItemView holderView = null;
		if (view == null) {
			view = container.inflate(R.layout.tool_grid_item, null);
			holderView = new GridItemView();
			holderView.image = (ImageView) view.findViewById(R.id.toolImage);
			holderView.func = (TextView) view.findViewById(R.id.toolName);
			view.setTag(holderView);
		} else {
			holderView = (GridItemView) view.getTag();
		}
		holderView.image.setImageDrawable(images.getDrawable(position));
		holderView.func.setText(funcs[position]);
		return view;
	}
}
