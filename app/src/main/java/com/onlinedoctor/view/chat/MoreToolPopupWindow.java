package com.onlinedoctor.view.chat;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;

import com.onlinedoctor.activity.chats.ChatActivity;
import com.onlinedoctor.activity.R;
import com.onlinedoctor.adapter.chats.ChatToolsAdapter;
import com.onlinedoctor.adapter.mine.MyPagerAdapter;
import com.onlinedoctor.pojo.patient.Patient;

import java.util.ArrayList;

/**
 * Created by xuweidong on 15/9/19.
 */
public class MoreToolPopupWindow extends PopupWindow{

    private Context context;

    private View moreItemView;

    private Patient currentPatient = null;

    public MoreToolPopupWindow(Context context, int heightDp, Patient currentPatient) {
        super(context);
        this.context = context;
        this.currentPatient = currentPatient;
        LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        moreItemView = inflater.inflate(R.layout.chatting_more_view, null);
        ViewPager chatMorePager = (ViewPager) moreItemView.findViewById(R.id.chating_more_viewpager);
        ImageView page0 = (ImageView) moreItemView.findViewById(R.id.page0);
        ImageView page1 = (ImageView) moreItemView.findViewById(R.id.page1);
        page1.setVisibility(View.GONE);

        View view1 = inflater.inflate(R.layout.chatting_more_view_page0, null);
        View view2 = inflater.inflate(R.layout.chatting_more_view_page1, null);
        final ArrayList<View> pageViews = new ArrayList<View>();
        pageViews.add(getGridView(view1, 1));
        //pageViews.add(getGridView(view2, 2));
        MyPagerAdapter pagerAdapter = new MyPagerAdapter(pageViews, (ChatActivity)context);
        chatMorePager.setAdapter(pagerAdapter);
        chatMorePager.setOnPageChangeListener(new MyOnPageChangeListener(page0, page1));

        this.setContentView(moreItemView);
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

    private View getGridView(View view, int pageFlag) {
        GridView temp = (GridView) view.findViewById(R.id.chat_tool_grid);
        ChatToolsAdapter imageAdapter = null;
        if (pageFlag == 1) {
            imageAdapter = new ChatToolsAdapter(context, R.array.chat_more_titles1, R.array.chat_more_icons1, currentPatient.getPatientId());
        } else if (pageFlag == 2) {
            imageAdapter = new ChatToolsAdapter(context, R.array.chat_more_titles2, R.array.chat_more_icons2, currentPatient.getPatientId());
        }
        temp.setAdapter(imageAdapter);
        return temp;
    }

    public class MyOnPageChangeListener implements ViewPager.OnPageChangeListener {

        ImageView mPage0 = null;
        ImageView mPage1 = null;

        public MyOnPageChangeListener(ImageView mPage0, ImageView mPage1) {
            super();
            this.mPage0 = mPage0;
            this.mPage1 = mPage1;
        }

        public void onPageSelected(int page) {

            // 翻页时当前page,改变当前状态园点图片
            switch (page) {
                case 0:
                    mPage0.setImageDrawable(context.getResources().getDrawable(R.drawable.page_now));
                    mPage1.setImageDrawable(context.getResources().getDrawable(R.drawable.page));
                    break;
                case 1:
                    mPage1.setImageDrawable(context.getResources().getDrawable(R.drawable.page_now));
                    mPage0.setImageDrawable(context.getResources().getDrawable(R.drawable.page));
                    break;
            }
        }

        public void onPageScrolled(int arg0, float arg1, int arg2) {
        }

        public void onPageScrollStateChanged(int arg0) {
        }
    }
}
