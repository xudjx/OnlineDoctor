package com.onlinedoctor.activity;

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.Window;

import com.onlinedoctor.adapter.MessageListAdapter;
import com.onlinedoctor.application.MyApplication;
import com.onlinedoctor.log.Logger;
import com.onlinedoctor.net.HandlerManager;
import com.onlinedoctor.pojo.chats.BriefMessagePojo;
import com.onlinedoctor.pojo.Common;
import com.onlinedoctor.pojo.RunDataContainer;
import com.umeng.analytics.MobclickAgent;

public class MessageActivity extends Activity {

	private static final String TAG = "MessageActivity";
	private Context context;
	private MessageListAdapter adapter;
	private List<BriefMessagePojo> messagePojos = null;

	private RecyclerView mRecyclerView;
	private SwipeRefreshLayout mSwipeRefreshLayout;
	//private ProgressBar mProgressBar;

	private RunDataContainer container = RunDataContainer.getContainer();

	private Handler messageHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			if (msg.what == Common.MSG_WHAT_BRIEFCHANGE_MESSAGE) {
				Logger.i(TAG, "receive message=" + msg.toString());
				messagePojos = container.getBriefMessage();
				adapter.notifyDataSetChanged();
			}
		}

	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		Logger.i(TAG, "onCreate");
		setContentView(R.layout.activity_message);
		MyApplication.getInstance().addActivity(this);
		context = MessageActivity.this;

		HandlerManager.getManager().setMessageHandler(messageHandler);
		init();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		Logger.i(TAG, "onDestroy");
	}

	@Override
	protected void onResume() {
		super.onResume();
		container.setInMessageActivity(true);
		Logger.i(TAG, "onResume" + container.getBriefMessage().size());
		messagePojos = container.getBriefMessage();
		adapter.notifyDataSetChanged();
		MobclickAgent.onResume(this);
	}

	@Override
	protected void onRestart() {
		super.onRestart();
		Logger.i(TAG, "onRestart");
	}

	@Override
	protected void onPause() {
		super.onPause();
		container.setInMessageActivity(false);
		MobclickAgent.onPause(this);
	}

	@Override
	protected void onStop() {
		super.onStop();
		Logger.i(TAG, "onStop");
	}

	private void init() {

		// RecyclerView (ListView)
		mRecyclerView = (RecyclerView) findViewById(R.id.messageListView);
		mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
		mRecyclerView.setItemAnimator(new DefaultItemAnimator());

		messagePojos = container.getBriefMessage();
		adapter = new MessageListAdapter(context,  R.layout.item_list_message, messagePojos);
		mRecyclerView.setAdapter(adapter);

		// The Google refresh widget
		mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_container);
		mSwipeRefreshLayout.setColorSchemeColors(getResources().getColor(R.color.theme_accent));
		mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
			@Override
			public void onRefresh() {
				new InitMessageAdapterTask().execute();
			}
		});
	}

	private class InitMessageAdapterTask extends AsyncTask<Void, Void, Void> {
		@Override
		protected void onPreExecute() {
			//adapter.clearMessages();
			super.onPreExecute();
		}

		@Override
		protected Void doInBackground(Void... voids) {
			messagePojos = container.getBriefMessage();
			Logger.i(TAG," Refresh Message size="+messagePojos.size());
			return null;
		}

		@Override
		protected void onPostExecute(Void aVoid) {
			//handle visibility
			mRecyclerView.setVisibility(View.VISIBLE);

			adapter.notifyDataSetChanged();
			mSwipeRefreshLayout.setRefreshing(false);
			super.onPostExecute(aVoid);
		}
	}
}
