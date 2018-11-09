package com.onlinedoctor.activity.mine.wallet;

import org.json.JSONException;
import org.json.JSONObject;

import com.onlinedoctor.activity.R;
import com.onlinedoctor.log.Logger;
import com.onlinedoctor.net.HandlerManager;
import com.onlinedoctor.net.MyWalletNetManager;
import com.onlinedoctor.pojo.mine.MyWallet;
import com.onlinedoctor.sqlite.dao.MyWalletServiceImpl;
import com.onlinedoctor.util.BadgeViewUtil;
import com.onlinedoctor.util.SharedpreferenceManager;
import com.onlinedoctor.view.BadgeView;
import com.onlinedoctor.view.CommonActionbarBackableRelativeLayout;
import com.umeng.analytics.MobclickAgent;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DecimalFormat;

public class MyWalletActivity extends Activity{
	
	private TextView tv_total;
	private TextView tv_available;
	private TextView tv_unavailable;
	private CommonActionbarBackableRelativeLayout title;
	private TextView saveTextView;
	private RelativeLayout firstPart;
	private Button thirdPart;
	private ImageView next_iv;
	private TextView tv1;
	private BadgeView orderBadge;
	private MyWalletServiceImpl myWalletImpl = null;
	private DecimalFormat decimalFormat=new DecimalFormat("0.00");
	private Handler mhandler = new Handler(){
		@Override
		public void handleMessage(Message msg){
			if(msg.what == MyWalletNetManager._msg_get_money){
				try {
					JSONObject jo = new JSONObject(msg.obj.toString());
//					double totalMoney = jo.getDouble("totalMoney");
//					double availableMoney = jo.getDouble("availableMoney");
					double totalMoney = ((double)jo.getInt("totalMoney") / 100); //服务器返回回来的单位是整型, 单位位分
					double availableMoney = ((double)jo.getInt("availableMoney") / 100); //服务器返回回来的单位是整型, 单位位分
					tv_total.setText(decimalFormat.format(totalMoney)+"");
					tv_available.setText(decimalFormat.format(availableMoney)+"");
					double unavailableMoney = totalMoney - availableMoney;
					tv_unavailable.setText(decimalFormat.format(unavailableMoney) + "");

					MyWallet myWallet = new MyWallet((float)totalMoney, (float)availableMoney, (float)unavailableMoney);
					if(myWalletImpl.isEmpty())
						myWalletImpl.insert(myWallet);
					else
						myWalletImpl.update(myWallet);

				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					Toast.makeText(MyWalletActivity.this, "无法解析服务器数据", Toast.LENGTH_SHORT).show();
				}
			}else if (msg.what == MyWalletNetManager._msg_fail) {
			
				Toast.makeText(MyWalletActivity.this, "请求失败", Toast.LENGTH_SHORT).show();
			} else if (msg.what ==MyWalletNetManager._msg_unknown_exception){
				Toast.makeText(MyWalletActivity.this, "未知错误", Toast.LENGTH_SHORT).show();
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_my_wallet);

		myWalletImpl = new MyWalletServiceImpl(this, null);

		tv_total = (TextView)findViewById(R.id.total);
		tv_available = (TextView)findViewById(R.id.available);
		tv_unavailable = (TextView)findViewById(R.id.unavailable);

		title = (CommonActionbarBackableRelativeLayout)findViewById(R.id.title);
		((TextView)findViewById(R.id.actionbar_common_backable_title_tv)).setText("我的钱包");
		saveTextView = ((TextView)findViewById(R.id.actionbar_common_backable_right_tv));
		saveTextView.setVisibility(View.GONE);
		firstPart = (RelativeLayout)findViewById(R.id.firstPart);
		thirdPart = (Button)findViewById(R.id.thirdPart);
		next_iv = (ImageView) findViewById(R.id.next_iv);

		tv1 = (TextView) findViewById(R.id.tv1);
		tv1.setText("总额     ");
		orderBadge = new BadgeView(this, tv1, BadgeView.POSITION_RIGHT, 0);
		//orderBadge = new BadgeView(this, tv1);

		//设置消息处理
		HandlerManager.getManager().setMyWalletHandler(mhandler);

		if(savedInstanceState != null){
			float total = savedInstanceState.getFloat("total");
			float available = savedInstanceState.getFloat("available");
			tv_total.setText(decimalFormat.format(total) + "");
			tv_available.setText(decimalFormat.format(available) + "");
			float unavailable = total - available;
			tv_unavailable.setText(decimalFormat.format(unavailable) + "");
		}

		firstPart.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(MyWalletActivity.this,OrderDetailActivity.class);
				startActivity(intent);

				//红点消失
				SharedpreferenceManager spManager = SharedpreferenceManager.getInstance();
				spManager.setOne("keyUnReadPayItemNum", 0+"");
			}});
		
		thirdPart.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(MyWalletActivity.this,CashActivity.class);
				startActivity(intent);
			}});
	}
	

	@Override
	protected  void onResume(){

		super.onResume();

		//显示红点
		BadgeViewUtil.paymentSetBadgeView(orderBadge);
		Logger.d("getMoney ", "before");
		MyWalletNetManager.getMoney(mhandler);
		MobclickAgent.onResume(this);
	}

	@Override
	protected void onPause() {
		super.onPause();
		MobclickAgent.onPause(this);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putFloat("total", Float.parseFloat(tv_total.getText().toString()));
		outState.putFloat("available", Float.parseFloat(tv_available.getText().toString()));
		Logger.d("total", tv_total.getText().toString());
		super.onSaveInstanceState(outState);
	}
}
