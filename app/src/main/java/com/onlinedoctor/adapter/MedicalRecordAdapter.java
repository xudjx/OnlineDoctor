package com.onlinedoctor.adapter;

import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.display.SimpleBitmapDisplayer;
import com.onlinedoctor.activity.R;
import com.onlinedoctor.pojo.patient.Record;

public class MedicalRecordAdapter extends BaseAdapter{
	private Context mContext;
	//private String[] imageUrls;
	List<Record> list;
	private DisplayImageOptions uilOptions;
	private ImageLoader imageLoader;
	
	public MedicalRecordAdapter(Context context, List<Record> list) {
		this.mContext = context;
		this.list = list;
		
		uilOptions = new DisplayImageOptions.Builder()
		.showImageOnLoading(R.drawable.ic_record_loading)
		.showImageForEmptyUri(R.drawable.ic_record_empty)
		.showImageOnFail(R.drawable.ic_record_error)
		.cacheInMemory(true)
		.cacheOnDisk(true)
		.considerExifParams(true)
		.bitmapConfig(Bitmap.Config.RGB_565)
		.displayer(new SimpleBitmapDisplayer())
		.resetViewBeforeLoading(false)
		.build();
		
		imageLoader = ImageLoader.getInstance();
	}

	@Override
	public int getCount() {
		return list.size();
	}

	@Override
	public Object getItem(int position) {
		return list.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		//这是一个demo级别的代码，正式代码后面改
		ViewHolder holder = null;
		if(convertView == null){
			holder = new ViewHolder();
			convertView = LayoutInflater.from(this.mContext)
					.inflate(R.layout.gridview_item, null, false);
			holder.imageView = (ImageView) convertView
					.findViewById(R.id.medical_record_image_view);
			convertView.setTag(holder);
		}
		else{
			holder = (ViewHolder) convertView.getTag();
		}

		final ImageView final_view = holder.imageView;

		imageLoader
			.displayImage(list.get(position).getThumbnail(), holder.imageView, uilOptions);

		return convertView;
	}
	
	private class ViewHolder{
		ImageView imageView;
	}

}
