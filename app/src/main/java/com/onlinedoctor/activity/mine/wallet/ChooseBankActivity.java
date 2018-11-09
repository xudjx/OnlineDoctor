package com.onlinedoctor.activity.mine.wallet;

import com.onlinedoctor.activity.R;
import com.umeng.analytics.MobclickAgent;

import android.app.Activity;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
public class ChooseBankActivity extends Activity {
	private TypedArray images;
	private String names[];
	@Override
	protected void onCreate(Bundle bundle){
		super.onCreate(bundle);
		setContentView(R.layout.activity_choose_bank);
		final ListView lv = (ListView)findViewById(R.id.lv_banks);
		((TextView)findViewById(R.id.actionbar_common_backable_right_tv)).setText("");
		String title = getResources().getString(R.string.wallet_bank_list);

		((TextView)findViewById(R.id.actionbar_common_backable_title_tv)).setText(title);
		images = this.getResources().obtainTypedArray(R.array.bank_icon);
		names = this.getResources().getStringArray(R.array.bank);

		lv.setAdapter(new BaseAdapter(){
			
			@Override
			public int getCount() {
				// TODO Auto-generated method stub
				return names.length;
			}

			@Override
			public Object getItem(int position) {
				// TODO Auto-generated method stub
				return names[position];
			}

			@Override
			public long getItemId(int position) {
				// TODO Auto-generated method stub
				return 0;
			}

			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				// TODO Auto-generated method stub
				View view = LayoutInflater.from(ChooseBankActivity.this).inflate(R.layout.simple_item, null);
				((ImageView)view.findViewById(R.id.bank_img)).setImageDrawable(images.getDrawable(position));
				((TextView)view.findViewById(R.id.bank_name)).setText(names[position]);
				return view;
			}
			
		});
		
		lv.setOnItemClickListener(new OnItemClickListener(){

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				Intent intent  = new Intent();
				intent.putExtra("bank",lv.getItemAtPosition(position).toString());
				setResult(RESULT_OK,intent);
				ChooseBankActivity.this.finish();
				
			}});
	}

	@Override
	protected void onResume(){
		super.onResume();
		MobclickAgent.onResume(this);
	}

	@Override
	protected void onPause(){
		super.onPause();
		MobclickAgent.onPause(this);
	}
}
