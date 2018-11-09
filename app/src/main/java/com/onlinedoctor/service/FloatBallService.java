package com.onlinedoctor.service;


import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.IBinder;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.onlinedoctor.activity.R;


public class FloatBallService extends Service implements OnKeyListener{

	private WindowManager windowManager;
	
	private View ballView;
	private View menuView;
	private RelativeLayout menuLayout;
	private Button floatImage;
	private PopupWindow pop;
	private RelativeLayout menuTop;
	ListView areaCheckListView;
	
	private boolean is_move = false;
	//private boolean ismoving = false;
	
	@Override
	public void onCreate() {
		super.onCreate();
		//加载辅助球布局
		ballView = LayoutInflater.from(this).inflate(R.layout.floatball, null);
		floatImage = (Button)ballView.findViewById(R.id.float_image);
		setUpFloatMenuView();
		createView();
	}
	
	
	/**
	 * 窗口菜单初始化
	 */
	
	private void setUpFloatMenuView(){
		menuView = LayoutInflater.from(this).inflate(R.layout.floatmenu, null);
		menuLayout = (RelativeLayout)menuView.findViewById(R.id.menu);
		menuTop = (RelativeLayout)menuView.findViewById(R.id.lay_main);
		TextView dabiaoqian = (TextView) menuView.findViewById(R.id.btn_home_screen);
		TextView kanbingli = (TextView) menuView.findViewById(R.id.btn_setting);
		TextView tianbeizhu = (TextView) menuView.findViewById(R.id.btn_favor);
		TextView miandarao = (TextView) menuView.findViewById(R.id.btn_lock_screen);
		//menuLayout.setOnClickListener(this);
		//menuLayout.setOnKeyListener(this);
		dabiaoqian.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				if(pop!=null && pop.isShowing()){
					pop.dismiss();
				}
				Toast.makeText(getApplicationContext(), "打标签", Toast.LENGTH_SHORT).show();
			}
		
		});
		kanbingli.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				if(pop!=null && pop.isShowing()){
					pop.dismiss();
				}
				Toast.makeText(getApplicationContext(), "看病历", Toast.LENGTH_SHORT).show();
			}
		
		});
		tianbeizhu.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				if(pop!=null && pop.isShowing()){
					pop.dismiss();
				}
				Toast.makeText(getApplicationContext(), "填备注", Toast.LENGTH_SHORT).show();
			}
		
		});
		miandarao.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				if(pop!=null && pop.isShowing()){
					pop.dismiss();
				}
				Toast.makeText(getApplicationContext(), "免打扰", Toast.LENGTH_SHORT).show();
			}
		
		});
		menuTop.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				if(pop!=null && pop.isShowing()){
					pop.dismiss();
				}
			}
		
		});
		menuLayout.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				if(pop!=null && pop.isShowing()){
					pop.dismiss();
				}
			}
		
		});
	}
	

	/**
	 * 通过MyApplication创建view，并初始化显示参数
	 */
	private void createView() {
		
		windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
		
		final WindowManager.LayoutParams params = new WindowManager.LayoutParams(
				WindowManager.LayoutParams.WRAP_CONTENT,
				WindowManager.LayoutParams.WRAP_CONTENT,
				WindowManager.LayoutParams.TYPE_PHONE,
				WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
				PixelFormat.TRANSLUCENT);
		
		params.gravity = Gravity.TOP | Gravity.LEFT;
		params.x = 0;
		params.y = 100;
		
		windowManager.addView(ballView, params);
		Log.d("floatball", "addView already");
		try {
			floatImage.setOnTouchListener(new View.OnTouchListener() {
				private WindowManager.LayoutParams paramsF = params;
				private int initialX;
				private int initialY;
				private float initialTouchX;
				private float initialTouchY;

				@Override 
				public boolean onTouch(View v, MotionEvent event) {
					switch (event.getAction()) {
					case MotionEvent.ACTION_DOWN:

						// Get current time in nano seconds.
						is_move = false;
						initialX = paramsF.x;
						initialY = paramsF.y;
						initialTouchX = event.getRawX();
						initialTouchY = event.getRawY();
						break;
					case MotionEvent.ACTION_UP:
						break;
					case MotionEvent.ACTION_MOVE:
						is_move = true;
						paramsF.x = initialX + (int) (event.getRawX() - initialTouchX);
						paramsF.y = initialY + (int) (event.getRawY() - initialTouchY);
						windowManager.updateViewLayout(ballView, paramsF);
						break;
					}
					return is_move;
				}
			});
		} catch (Exception e) {
			// TODO: handle exception
		}
		
		//注册点击事件监听器
		floatImage.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				DisplayMetrics dm = getResources().getDisplayMetrics();
				pop = new PopupWindow(menuView, dm.widthPixels, dm.heightPixels);
				pop.showAtLocation(ballView, Gravity.CENTER, 0, 0);
				pop.update();
			}
		});
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}


	@Override
	public void onDestroy(){
		windowManager.removeView(ballView);
	}

	@Override
	public boolean onKey(View v, int keyCode, KeyEvent event) {
		Toast.makeText(getApplicationContext(), "keyCode:"+keyCode, 1000).show();
		switch (keyCode) {
		case KeyEvent.KEYCODE_HOME:
			pop.dismiss();
			break;
		default:
			break;
		}
		return true;
	}
	
}
