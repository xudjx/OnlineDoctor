package com.onlinedoctor.activity.tools.response;

import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.onlinedoctor.activity.R;
import com.onlinedoctor.adapter.tools.FastResponseAdapter;
import com.onlinedoctor.pojo.tools.FastResponseMessage;
import com.onlinedoctor.sqlite.dao.FastResponseImpl;

public class FastResponseActivity extends Activity {
	public static final int CLICK_ADD = 1;
	public static final int CLICK_UPDATE = 2;
	
	public static final int FLAG_FROM_CHAT_ACTIVITY = 3;
	private TextView add_tv = null;
	private ImageView bg_iv = null;
	List<FastResponseMessage> frmList = null;
	private static final String TAG = "FastResponseActivity";
	private FastResponseImpl frImpl = null;
	private ListView listView = null;
	private FastResponseAdapter frAdapter = null;
	private Context mContext = this;
	private int update_position = -1;
	private int update_id = -1;
	private int delete_position = -1;
	private RelativeLayout relLayout = null;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.d(TAG, "onCreate");
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.common_listview);

		add_tv = (TextView) findViewById(R.id.actionbar_common_backable_right_tv);
		add_tv.setText("添加");
		TextView title_tv = (TextView) findViewById(R.id.actionbar_common_backable_title_tv);
		title_tv.setText("快捷回复");

		bg_iv = (ImageView)findViewById(R.id.bg_iv);
		frImpl = new FastResponseImpl(this, null);
		frmList = frImpl.getAllFastResponses();
		relLayout = (RelativeLayout) findViewById(R.id.relLayout);

		relLayout = (RelativeLayout) findViewById(R.id.relLayout);
		frAdapter = new FastResponseAdapter(this, R.layout.item_labels_3, frmList);
		listView = (ListView) findViewById(R.id.common_lv);
		listView.setAdapter(frAdapter);

		add();
		clickItem();
	}

	@Override
	protected void onResume() {
		super.onResume();

		/* 背景图何时出现 */
		if(frmList.isEmpty()){
			bg_iv.setVisibility(View.VISIBLE);
			listView.setVisibility(View.GONE);
		}else{
			bg_iv.setVisibility(View.GONE);
			listView.setVisibility(View.VISIBLE);
		}
	}

	private void add() {
		add_tv.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View view) {
				Intent intent = new Intent();
				intent.setClass(FastResponseActivity.this,
						FastResponseAdd.class);
				intent.putExtra("msg", "");
				startActivityForResult(intent, CLICK_ADD);
			}
		});
	}

	private void clickItem() {
		listView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View v, int position,
					long id) {
				Intent intent = new Intent();
				int flag = getIntent().getIntExtra("flag", -1);
				switch(flag){
				case FLAG_FROM_CHAT_ACTIVITY:
					String msg = frmList.get(position).getMsg();
					intent.putExtra("msg", msg);
					setResult(RESULT_OK, intent);
					finish();
					break;
				default:
					intent.setClass(FastResponseActivity.this,
							FastResponseAdd.class);
					update_position = position;
					intent.putExtra("msg", frmList.get(position).getMsg());
					update_id = frmList.get(position).getId();
					startActivityForResult(intent, CLICK_UPDATE);
					break;
				}

			}
		});

		listView.setOnItemLongClickListener(new OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> arg0, View view,
					int position, long id) {
				final int pos = position;
				String[] items = {"删除"};
				new AlertDialog.Builder(mContext)
						.setItems(items,
								new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog,
											int arg1) {
										FastResponseMessage frm = frmList
												.get(pos);
										frImpl.deleteFastResponseItem(frm);
										frmList.remove(pos);
										frAdapter.notifyDataSetChanged();
										if(frmList.isEmpty()){
											bg_iv.setVisibility(View.VISIBLE);
											listView.setVisibility(View.GONE);
											//relLayout.setBackgroundColor(getResources().getColor(R.color.white));
										}else{
											bg_iv.setVisibility(View.GONE);
											listView.setVisibility(View.VISIBLE);
											//relLayout.setBackgroundColor(getResources().getColor(R.color.withcard_background));
										}
										dialog.dismiss();
									}
								}).show();

				return true;
			}
		});
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode) {
		case CLICK_ADD:
			if (resultCode == RESULT_OK) {
				String msg = data.getStringExtra("msg");
				Log.d(TAG, msg);
				long id = frImpl.addFastResponseItem(msg);
				frmList.add(new FastResponseMessage((int) id, msg));
				frAdapter.notifyDataSetChanged();
			}
			break;
		case CLICK_UPDATE:
			if (resultCode == RESULT_OK) {
				String msg = data.getStringExtra("msg");
				int id = update_id;
				int position = update_position;
				Log.d(TAG, Integer.toString(id));
				Log.d(TAG, msg);
				frImpl.updateFastResponseItem(new FastResponseMessage(id, msg));
				frmList.get(position).setMsg(msg);
				frAdapter.notifyDataSetChanged();
			}
			break;
		default:
			break;
		}

	}
}
