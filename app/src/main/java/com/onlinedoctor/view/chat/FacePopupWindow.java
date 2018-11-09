package com.onlinedoctor.view.chat;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.support.v4.view.ViewPager;
import android.text.SpannableString;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;

import com.onlinedoctor.activity.R;
import com.onlinedoctor.face.FaceAdapter;
import com.onlinedoctor.face.FaceViewPagerAdapter;
import com.onlinedoctor.face.ChatEmoji;
import com.onlinedoctor.face.FaceConversionUtil;
import com.onlinedoctor.view.FaceRelativeLayout;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by xuweidong on 15/9/17.
 */
public class FacePopupWindow extends PopupWindow{


    private View faceItemView;

    private static Context mContext;

    /**
     * 表情页的监听事件
     */
    private static FaceRelativeLayout.OnCorpusSelectedListener mListener;

    /**
     * 显示表情页的viewpager
     */
    private ViewPager vp_face;

    /**
     * 表情页界面集合
     */
    private ArrayList<View> pageViews;

    /**
     * 游标显示布局
     */
    private LinearLayout layout_point;

    /**
     * 游标点集合
     */
    private ArrayList<ImageView> pointViews;

    /**
     * 表情集合
     */
    private List<List<ChatEmoji>> emojis;

    /**
     * 表情区域
     */
   private View view;

    /**
     * 表情数据填充器
     */
    private static List<FaceAdapter> faceAdapters;

    /**
     * 当前表情页
     */
    private static int current = 0;

    public FacePopupWindow(Activity context, FaceItemClickListener faceItemClickListener, int heightDp) {
        super(context);
        mContext = context.getBaseContext();
        LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        faceItemView = inflater.inflate(R.layout.custom_facerelativelayout, null);
        emojis = FaceConversionUtil.getInstace().emojiLists;
        Log.i("emoji size: ", String.valueOf(emojis.size()));
        Init_View(faceItemView);
        Init_viewPager(faceItemClickListener);
        Init_Point();
        Init_Data();

        this.setContentView(faceItemView);
        this.setWidth(RelativeLayout.LayoutParams.FILL_PARENT);
        this.setHeight(heightDp);
        this.setInputMethodMode(PopupWindow.INPUT_METHOD_NOT_NEEDED);
        this.setFocusable(false);
        this.setBackgroundDrawable(new BitmapDrawable());
        this.setOutsideTouchable(true);
        this.setAnimationStyle(R.style.AnimBottom);
        this.setTouchInterceptor(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_OUTSIDE) {
                    return true;
                }
                return false;
            }
        });
    }

    /**
     * 初始化控件
     */
    private void Init_View(View view) {
        this.vp_face = (ViewPager) view.findViewById(R.id.vp_contains);
        this.layout_point = (LinearLayout) view.findViewById(R.id.iv_image);
        this.view = view.findViewById(R.id.ll_facechoose);
    }

    /**
     * 初始化显示表情的viewpager
     */
    private void Init_viewPager(FaceItemClickListener faceItemClickListener) {
        pageViews = new ArrayList<View>();
        // 左侧添加空页
        View nullView1 = new View(mContext);
        // 设置透明背景
        nullView1.setBackgroundColor(Color.TRANSPARENT);
        pageViews.add(nullView1);

        // 中间添加表情页
        faceAdapters = new ArrayList<FaceAdapter>();
        for (int i = 0; i < emojis.size(); i++) {
            GridView view = new GridView(mContext);
            FaceAdapter adapter = new FaceAdapter(mContext, emojis.get(i));
            view.setAdapter(adapter);
            faceAdapters.add(adapter);
            view.setOnItemClickListener(faceItemClickListener);
            view.setNumColumns(7);
            view.setBackgroundColor(Color.TRANSPARENT);
            view.setHorizontalSpacing(1);
            view.setVerticalSpacing(1);
            view.setStretchMode(GridView.STRETCH_COLUMN_WIDTH);
            view.setCacheColorHint(0);
            view.setPadding(5, 0, 5, 0);
            view.setSelector(new ColorDrawable(Color.TRANSPARENT));
            view.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.FILL_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT));
            view.setGravity(Gravity.CENTER);
            pageViews.add(view);
        }

        // 右侧添加空页面
        View nullView2 = new View(mContext);
        // 设置透明背景
        nullView2.setBackgroundColor(Color.TRANSPARENT);
        pageViews.add(nullView2);
        Log.i("faceView ", "view size: " + pageViews.size());
    }

    /**
     * 初始化游标
     */
    private void Init_Point() {

        pointViews = new ArrayList<ImageView>();
        ImageView imageView;
        for (int i = 0; i < pageViews.size(); i++) {
            imageView = new ImageView(mContext);
            imageView.setBackgroundResource(R.drawable.d1);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(new ViewGroup.LayoutParams(
                    RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT));
            layoutParams.leftMargin = 10;
            layoutParams.rightMargin = 10;
            layoutParams.width = RelativeLayout.LayoutParams.WRAP_CONTENT;
            layoutParams.height = RelativeLayout.LayoutParams.WRAP_CONTENT;
            layout_point.addView(imageView, layoutParams);
            if (i == 0 || i == pageViews.size() - 1) {
                imageView.setVisibility(View.GONE);
            }
            if (i == 1) {
                imageView.setBackgroundResource(R.drawable.d2);
            }
            pointViews.add(imageView);

        }
    }

    /**
     * 填充数据
     */
    private void Init_Data() {
        vp_face.setAdapter(new FaceViewPagerAdapter(pageViews));

        vp_face.setCurrentItem(1);
        current = 0;
        vp_face.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            @Override
            public void onPageSelected(int arg0) {
                current = arg0 - 1;
                // 描绘分页点
                draw_Point(arg0);
                // 如果是第一屏或者是最后一屏禁止滑动，其实这里实现的是如果滑动的是第一屏则跳转至第二屏，如果是最后一屏则跳转到倒数第二屏.
                if (arg0 == pointViews.size() - 1 || arg0 == 0) {
                    if (arg0 == 0) {
                        vp_face.setCurrentItem(arg0 + 1);// 第二屏 会再次实现该回调方法实现跳转.
                        pointViews.get(1).setBackgroundResource(R.drawable.d2);
                    } else {
                        vp_face.setCurrentItem(arg0 - 1);// 倒数第二屏
                        pointViews.get(arg0 - 1).setBackgroundResource(R.drawable.d2);
                    }
                }
            }

            @Override
            public void onPageScrolled(int arg0, float arg1, int arg2) {

            }

            @Override
            public void onPageScrollStateChanged(int arg0) {

            }
        });

    }

    /**
     * 绘制游标背景
     */
    public void draw_Point(int index) {
        for (int i = 1; i < pointViews.size(); i++) {
            if (index == i) {
                pointViews.get(i).setBackgroundResource(R.drawable.d2);
            } else {
                pointViews.get(i).setBackgroundResource(R.drawable.d1);
            }
        }
    }

    public static class FaceItemClickListener implements AdapterView.OnItemClickListener{

        private EditText messageEditText;

        public FaceItemClickListener(EditText editText){
            this.messageEditText = editText;
        }

        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            ChatEmoji emoji = (ChatEmoji) faceAdapters.get(current).getItem(i);
            if (emoji.getId() == R.drawable.face_del_icon) {
                int selection = messageEditText.getSelectionStart();
                String text = messageEditText.getText().toString();
                if (selection > 0) {
                    String text2 = text.substring(selection - 1);
                    if ("]".equals(text2)) {
                        int start = text.lastIndexOf("[");
                        int end = selection;
                        messageEditText.getText().delete(start, end);
                        return;
                    }
                    messageEditText.getText().delete(selection - 1, selection);
                }
            }
            if (!TextUtils.isEmpty(emoji.getCharacter())) {
                if (mListener != null)
                    mListener.onCorpusSelected(emoji);
                SpannableString spannableString = FaceConversionUtil.getInstace().addFace(mContext, emoji.getId(),
                        emoji.getCharacter());
                messageEditText.append(spannableString);
            }
        }
    }
}
