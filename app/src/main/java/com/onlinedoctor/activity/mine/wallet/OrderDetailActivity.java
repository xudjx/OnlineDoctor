package com.onlinedoctor.activity.mine.wallet;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.google.gson.reflect.TypeToken;
import com.onlinedoctor.activity.R;
import com.onlinedoctor.log.Logger;
import com.onlinedoctor.net.MyWalletNetManager;
import com.onlinedoctor.pojo.mine.Order;
import com.onlinedoctor.pojo.mine.OrderDetailItem;
import com.onlinedoctor.pojo.mine.OrderItemConvertable;
import com.onlinedoctor.pojo.mine.WithdrawHistory;
import com.onlinedoctor.view.CommonActionbarBackableRelativeLayout;
import com.onlinedoctor.util.JsonUtil;
import com.onlinedoctor.view.DynamicListView;
import com.onlinedoctor.view.DynamicListView.DynamicListViewListener;
import com.umeng.analytics.MobclickAgent;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class OrderDetailActivity extends Activity {
	private CommonActionbarBackableRelativeLayout title;
	private TextView saveTextView;
	private DynamicListView orderDetailList;
	private FrameLayout progressBarHolder;

	private ImageView bg_iv;

	Boolean _receive_withdraw = false;
	Boolean _receive_order = false;
	List<WithdrawHistory> withdrawList;
	List<Order> orderList;
	List<OrderItemConvertable> list = new ArrayList<OrderItemConvertable>();
	long withdraw_last_timestamp = Long.MAX_VALUE;
	long order_last_timestamp = Long.MAX_VALUE;
	private final int _PAGE_SIZE = 20;
	OrderListAdapter adapter;
	Handler mhandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			if(msg.what == MyWalletNetManager._msg_get_order_list) {
				orderList = JsonUtil.getInstance().fromJson(msg.obj.toString(), new TypeToken<ArrayList<Order>>() {}.getType());
				_receive_order = true;
			}else if(msg.what == MyWalletNetManager._msg_get_withdraw_list) {
				withdrawList = JsonUtil.getInstance().fromJson(msg.obj.toString(), new TypeToken<ArrayList<WithdrawHistory>>() {}.getType());
				_receive_withdraw = true;
			}else if (msg.what == MyWalletNetManager._msg_fail) {
				progressBarHolder.setVisibility(View.GONE);
				Toast.makeText(OrderDetailActivity.this, "请求失败", Toast.LENGTH_SHORT).show();
				_receive_order = _receive_withdraw = false;
			} else if (msg.what == MyWalletNetManager._msg_unknown_exception) {
				progressBarHolder.setVisibility(View.GONE);
				Toast.makeText(OrderDetailActivity.this, "未知错误", Toast.LENGTH_SHORT)
						.show();
				_receive_order = _receive_withdraw = false;
			}
			if(_receive_order && _receive_withdraw) {
				int count = 0;
				int p1=0,p2=0;
				while (count < _PAGE_SIZE) {
					if(p1<withdrawList.size() && p2<orderList.size()) {
						WithdrawHistory item_withdrawHistory = withdrawList.get(p1);
						Order item_order = orderList.get(p2);
						if(item_withdrawHistory.getTime() < item_order.getTimeStart()) {
							list.add(item_order);
							p2++;
							order_last_timestamp = item_order.getTimeStart();
						}else {
							list.add(item_withdrawHistory);
							p1++;
							withdraw_last_timestamp = item_withdrawHistory.getTime();
						}
						count++;
						continue;
					}
					if(p1 == withdrawList.size()) {
						for(int i = p2;i<Math.min(orderList.size(), p2+_PAGE_SIZE-count);i++){
							list.add(orderList.get(i));
							order_last_timestamp = orderList.get(i).getTimeStart();
						}
					}else {
						for(int i = p1;i<Math.min(withdrawList.size(), p1+_PAGE_SIZE-count);i++){
							list.add(withdrawList.get(i));
							withdraw_last_timestamp = withdrawList.get(i).getTime();
						}
					}
					break;
					
				}
				_receive_order = _receive_withdraw = false;
				adapter.notifyDataSetChanged();
				orderDetailList.doneMore();
				progressBarHolder.setVisibility(View.GONE);

				if(list.isEmpty()){
					Logger.d("list ", "isEmpty()");
					bg_iv.setVisibility(View.VISIBLE);
					orderDetailList.setVisibility(View.GONE);
				}else{
					Logger.d("list ", "is not Empty()");
					bg_iv.setVisibility(View.GONE);
					orderDetailList.setVisibility(View.VISIBLE);
				}

			}
			
		}
	};
	
	@Override
	protected void onCreate(Bundle bundle){
		super.onCreate(bundle);
		this.setContentView(R.layout.frag_order_list);
		title = (CommonActionbarBackableRelativeLayout)findViewById(R.id.title);
		((TextView)findViewById(R.id.actionbar_common_backable_title_tv)).setText("收入详情");
		saveTextView = ((TextView)findViewById(R.id.actionbar_common_backable_right_tv));
		saveTextView.setVisibility(View.GONE);
		orderDetailList = (DynamicListView)findViewById(R.id.orderlist);
		bg_iv = (ImageView) findViewById(R.id.bg_iv);

		progressBarHolder = (FrameLayout) findViewById(R.id.progressBarHolder);
		adapter = new OrderListAdapter(this, R.layout.order_list_item, list);
		orderDetailList.setAdapter(adapter);
		orderDetailList.setDoMoreWhenBottom(false);
		orderDetailList.setOnRefreshListener(null);
		orderDetailList.setOnMoreListener(new DynamicListViewListener() {
			@Override
			public boolean onRefreshOrMore(DynamicListView dynamicListView,
					boolean isRefresh) {
				MyWalletNetManager.getOrderList(_PAGE_SIZE, 0, 0, order_last_timestamp-1, mhandler);
				MyWalletNetManager.getWithdrawList(_PAGE_SIZE, 0, 0, withdraw_last_timestamp-1, mhandler);
				return false;
			}
		});
		progressBarHolder.setVisibility(View.VISIBLE);
		MyWalletNetManager.getOrderList(_PAGE_SIZE, 0, 0, 0, mhandler);
		MyWalletNetManager.getWithdrawList(_PAGE_SIZE, 0, 0, 0, mhandler);
		
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



class OrderListAdapter extends ArrayAdapter<OrderItemConvertable> {
	private Context mContext;
	public OrderListAdapter(Context context, int resource,
			List<OrderItemConvertable> objects) {
		super(context, resource, objects);
		mContext = context;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		OrderItemConvertable item = getItem(position);
		OrderDetailItem listItem = item.toOrderDetailItem();
		View view = LayoutInflater.from(getContext()).inflate(R.layout.order_list_item, null);
		TextView tv_title = (TextView)view.findViewById(R.id.tv_title);
		TextView tv_date = (TextView)view.findViewById(R.id.tv_date);
		TextView tv_amount = (TextView)view.findViewById(R.id.et_amount);
		TextView tv_status = (TextView)view.findViewById(R.id.tv_status);
		tv_title.setText(listItem.getChargingTitle());
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		tv_date.setText(sdf.format(new Date(listItem.getTime())));
		tv_status.setText(listItem.getStatus());
		tv_amount.setText(listItem.getFee());
		if(listItem.getFee().startsWith("+")) tv_amount.setTextColor(mContext.getResources().getColor(R.color.btn_blue));
		else if(listItem.getFee().startsWith("-")) tv_amount.setTextColor(Color.RED);
		return view;
	}
}




