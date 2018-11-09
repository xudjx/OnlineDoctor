package com.onlinedoctor.view;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Message;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.onlinedoctor.activity.chats.ForwardActivity;
import com.onlinedoctor.activity.R;
import com.onlinedoctor.net.HandlerManager;
import com.onlinedoctor.pojo.Common;
import com.onlinedoctor.pojo.patient.PatientMessage;
import com.onlinedoctor.sqlite.dao.FastResponseImpl;
import com.onlinedoctor.sqlite.service.FastResponseService;
import com.onlinedoctor.util.BitmapUtil;

import java.io.File;
import java.lang.reflect.Type;
import java.util.Map;

public class ChatMessagHandleDialog extends Dialog {

	private Context context;
	private ListView funcListView;
	private String[] textMsgFunc = { "复制", "转发", "删除", "设为快捷回复" };
	private String[] imageMsgFunc = { "转发", "保存到本地", "删除" };
	private String[] voiceMsgFunc = { "转发", "删除" };
	private String[] surveyMsgFunc = {"删除","转发"};
	private String[] otherMsgFunc = {"删除"};
	private PatientMessage message;
	private FastResponseService fastResponseService = null;

	private ImageLoader imageLoader = ImageLoader.getInstance();
	private Gson gson = new Gson();
	private Type type = new TypeToken<Map<String, Object>>() {}.getType();

	public ChatMessagHandleDialog(Context context) {
		super(context);
		this.context = context;
	}

	public ChatMessagHandleDialog(Context context, int theme, PatientMessage message) {
		super(context, theme);
		this.context = context;
		this.message = message;
		fastResponseService = new FastResponseImpl(context, null);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.chat_dialog);
		funcListView = (ListView) findViewById(R.id.chat_handle_listview);

		final String messageType = message.getContentType();
		if (messageType.equals(Common.MESSAGE_TYPE_text)) {
			funcListView
					.setAdapter(new ArrayAdapter<String>(context, android.R.layout.simple_list_item_1, textMsgFunc));
		} else if (messageType.equals(Common.MESSAGE_TYPE_image)) {
			funcListView
					.setAdapter(new ArrayAdapter<String>(context, android.R.layout.simple_list_item_1, imageMsgFunc));
		} else if (messageType.equals(Common.MESSAGE_TYPE_voice)) {
			funcListView
					.setAdapter(new ArrayAdapter<String>(context, android.R.layout.simple_list_item_1, voiceMsgFunc));
		}else if(messageType.equals(Common.MESSAGE_TYPE_survey)){
			funcListView
					.setAdapter(new ArrayAdapter<String>(context, android.R.layout.simple_list_item_1, surveyMsgFunc));
		}else{
			funcListView
					.setAdapter(new ArrayAdapter<String>(context, android.R.layout.simple_list_item_1, otherMsgFunc));
		}

		funcListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
				if (messageType.equals(Common.MESSAGE_TYPE_text)) {
					switch (position) {
					// 复制
					case 0:
						copy(message.getContent());
						Toast.makeText(context, "已复制", Toast.LENGTH_LONG).show();
						break;
					// 转发
					case 1:
						Intent intent = new Intent(context, ForwardActivity.class);
						Bundle bundle = new Bundle();
						bundle.putSerializable("ForwardMessage", message);
						intent.putExtras(bundle);
						context.startActivity(intent);
						break;
					// 删除
					case 2:
						Message msg = new Message();
						msg.what = Common.MSG_WHAT_MESSAGE_DELETE;
						msg.obj = message;
						HandlerManager.getManager().getChatHandler().sendMessage(msg);
						Toast.makeText(context, "已删除", Toast.LENGTH_LONG).show();
						break;
					// 设为快捷回复
					case 3:
						fastResponseService.addFastResponseItem(message.getContent());
						Toast.makeText(context, "已经设为快捷回复", Toast.LENGTH_LONG).show();
						break;
					default:
						break;
					}
				} else if (messageType.equals(Common.MESSAGE_TYPE_image)) {
					switch (position) {
					// 转发
					case 0:
						Intent intent = new Intent(context, ForwardActivity.class);
						Bundle bundle = new Bundle();
						bundle.putSerializable("ForwardMessage", message);
						intent.putExtras(bundle);
						context.startActivity(intent);
						break;
					// 收藏
					case 1:
					{
						Map<String, Object> map = gson.fromJson(message.getContent(), type);
						String thumbnailPath = (String) map.get(Common.KEY_IMAGE_thumbnailPath);
						String avaterPath = (String) map.get(Common.KEY_IMAGE_avaterPath);
						ImageLoader.getInstance().loadImage(avaterPath, new ImageLoadingListener() {
							@Override
							public void onLoadingStarted(String s, View view) {
								Toast.makeText(context, "后台正在为你保存...", Toast.LENGTH_LONG).show();
							}

							@Override
							public void onLoadingFailed(String s, View view, FailReason failReason) {

							}

							@Override
							public void onLoadingComplete(String s, View view, Bitmap bitmap) {
								String outPath = Common.sdPicSave + File.separator + "二维码名片.jpg";
								try {
									BitmapUtil.storeImage(bitmap, outPath);
									Toast.makeText(context, "成功保存至" + outPath, Toast.LENGTH_LONG).show();
								} catch (Exception e) {
									e.printStackTrace();
								}
							}

							@Override
							public void onLoadingCancelled(String s, View view) {

							}
						});
					}
						break;
					// 删除
					case 2:
						Message msg = new Message();
						msg.what = Common.MSG_WHAT_MESSAGE_DELETE;
						msg.obj = message;
						HandlerManager.getManager().getChatHandler().sendMessage(msg);
						Toast.makeText(context, "已删除", Toast.LENGTH_LONG).show();
						break;
					default:
						break;
					}
				} else if (messageType.equals(Common.MESSAGE_TYPE_voice)) {
					switch (position) {
					// 转发
					case 0:
						Intent intent = new Intent(context, ForwardActivity.class);
						Bundle bundle = new Bundle();
						bundle.putSerializable("ForwardMessage", message);
						intent.putExtras(bundle);
						context.startActivity(intent);
						break;
					// 删除
					case 1:
						Message msg = new Message();
						msg.what = Common.MSG_WHAT_MESSAGE_DELETE;
						msg.obj = message;
						HandlerManager.getManager().getChatHandler().sendMessage(msg);
						Toast.makeText(context, "已删除", Toast.LENGTH_LONG).show();
						break;
					default:
						break;
					}
				}else if(messageType.equals(Common.MESSAGE_TYPE_survey)){
					switch (position){
						case 0:
							Message msg = new Message();
							msg.what = Common.MSG_WHAT_MESSAGE_DELETE;
							msg.obj = message;
							HandlerManager.getManager().getChatHandler().sendMessage(msg);
							Toast.makeText(context, "已删除", Toast.LENGTH_LONG).show();
							break;
						case 1:
							Intent intent = new Intent(context, ForwardActivity.class);
							Bundle bundle = new Bundle();
							bundle.putSerializable("ForwardMessage", message);
							intent.putExtras(bundle);
							context.startActivity(intent);
							break;
					}
				}else{
					switch (position) {
						case 0:
							Message msg = new Message();
							msg.what = Common.MSG_WHAT_MESSAGE_DELETE;
							msg.obj = message;
							HandlerManager.getManager().getChatHandler().sendMessage(msg);
							Toast.makeText(context, "已删除", Toast.LENGTH_LONG).show();
							break;
					}
				}
				ChatMessagHandleDialog.this.dismiss();
			}
		});
	}

	@SuppressLint("NewApi")
	private void copy(String content) {
		ClipboardManager c = (ClipboardManager) context.getSystemService(context.CLIPBOARD_SERVICE);
		c.setPrimaryClip(ClipData.newPlainText("MessageCopy", content));
	}
}
