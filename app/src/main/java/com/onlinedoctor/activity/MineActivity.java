package com.onlinedoctor.activity;

import java.util.HashMap;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;
import com.onlinedoctor.activity.mine.homepage.MinePageActivity;
import com.onlinedoctor.activity.mine.wallet.MyWalletActivity;
import com.onlinedoctor.activity.mine.fee.SelfDefinedFeesActivity;
import com.onlinedoctor.activity.mine.settings.Setting;
import com.onlinedoctor.adapter.mine.MineListAdapter;
import com.onlinedoctor.application.MyApplication;
import com.onlinedoctor.log.Logger;
import com.onlinedoctor.pojo.mine.DoctorInfo;
import com.onlinedoctor.sqlite.dao.DoctorInfoServiceImpl;
import com.onlinedoctor.util.SharedpreferenceManager;
import com.umeng.analytics.MobclickAgent;

public class MineActivity extends Activity {

	private static final String TAG = "MineActivity";
	private ListView lv = null;
	private MineListAdapter adapter;
	private ImageView imageView = null;
	private TextView name_tv = null;
	private TextView brief_tv = null;
	private Context context = MineActivity.this;
	private DoctorInfoServiceImpl dInfoImpl = new DoctorInfoServiceImpl(this, null);
	private DoctorInfo dInfo = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		Logger.i(TAG, "onCreate");
		setContentView(R.layout.activity_mine);
		MyApplication.getInstance().addActivity(this);
		init();
	}

	@Override
	protected void onResume() {
		super.onResume();

		adapter.notifyDataSetChanged();

		if(!dInfoImpl.isEmpty()){
			dInfo = dInfoImpl.get(MinePageActivity.LOCAL_DOCTOR_TB_ITEM_ID);
			image();
			briefIntro();
		}
		MobclickAgent.onResume(this);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		Logger.i(TAG, "onDestroy");
	}

	private void init() {
		// demo级代码
		lv = (ListView) findViewById(R.id.lv_menu);
		adapter = new MineListAdapter(MineActivity.this, R.array.mine_name, R.array.mine_icons);
		lv.setAdapter(adapter);
		imageView = (ImageView)findViewById(R.id.mine_face);
		name_tv = (TextView)findViewById(R.id.mine_name);
		brief_tv = (TextView)findViewById(R.id.mine_brief);

		listListener();
	}

	private void image(){
		Logger.d("image", "come here~~");
		String thumbnail = dInfo.getThumbnail();
		Logger.d("thumbnail", thumbnail);
		DisplayImageOptions option = new DisplayImageOptions.Builder()
				.showImageOnLoading(R.drawable.ic_stub)
				.showImageForEmptyUri(null)
				.showImageOnFail(null).cacheInMemory(true)
				.cacheOnDisk(true).considerExifParams(true)
				.displayer(new RoundedBitmapDisplayer(200)).build();
		ImageLoader.getInstance().displayImage(thumbnail, imageView, option);
	}

	private void briefIntro(){
		String name = dInfo.getName();
		String cid = dInfo.getCid();
		String clinic = dInfo.getClinic();
		String room = dInfo.getRoom();
		String rank = dInfo.getRank();
		Logger.d("name = ", name);
		name_tv.setText(name);

		String brief = clinic + " | " + room  + " | " + rank;
		brief_tv.setText(brief);
	}
	private void listListener() {
		lv.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View view, int position, long id) {
				Intent intent = new Intent();
				switch (position) {
					// 个人主页
					case 0:
						Logger.i(TAG, "个人主页");
						intent.setClass(context, MinePageActivity.class);
						break;
					// 自定义收费
					case 1:
						// TODO
						Logger.d(TAG, "自定义收费");
						intent.setClass(context, SelfDefinedFeesActivity.class);
						break;
					// 个人钱包
					case 2:
						intent.setClass(context, MyWalletActivity.class);
						break;
					// 设置
					case 3:
						Logger.d(TAG, "设置");
						intent.setClass(context, Setting.class);
						break;
					default:
						break;
				}
				startActivity(intent);
			}
		});
	}


	@Override
	protected void onPause(){
		super.onPause();
		MobclickAgent.onPause(this);
	}
}
