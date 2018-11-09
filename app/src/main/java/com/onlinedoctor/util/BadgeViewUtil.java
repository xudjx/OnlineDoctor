package com.onlinedoctor.util;

import android.graphics.Color;
import android.os.Handler;
import android.os.Message;

import com.onlinedoctor.log.Logger;
import com.onlinedoctor.pojo.Common;
import com.onlinedoctor.view.BadgeView;

/**
 * Created by wds on 15/9/20.
 */
public class BadgeViewUtil {

    public static void commonSetBadgeView(BadgeView badgeView, int count){
        badgeView.setText(String.valueOf(count));
        badgeView.setTextSize(12);
        badgeView.setTextColor(Color.WHITE);
        if (count > 0 && count < 100) {
            badgeView.show();
        } else if (count >= 100) {
            badgeView.setText("99+");
            badgeView.show();
        } else {
            badgeView.hide();
        }
    }



    public static void paymentSetBadgeView(BadgeView badgeView){

        int count = 0;
        SharedpreferenceManager preManager = SharedpreferenceManager.getInstance();
        String unReadPayItemNumStr = preManager.getOne("keyUnReadPayItemNum");
        if(unReadPayItemNumStr.equals("")){
            count = 0;
        }else{
            count = Integer.parseInt(unReadPayItemNumStr);
        }

        commonSetBadgeView(badgeView, count);
    }
}
