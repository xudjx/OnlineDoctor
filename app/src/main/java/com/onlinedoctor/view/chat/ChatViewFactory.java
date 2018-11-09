package com.onlinedoctor.view.chat;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.onlinedoctor.activity.R;
import com.onlinedoctor.application.MyApplication;
import com.onlinedoctor.pojo.message_content.LinkPatientMsgDO;

import java.util.HashMap;

/**
 * Created by xuweidong on 15/9/20.
 */
public class ChatViewFactory {

    private static ChatViewFactory instance;

    private HashMap<String, ViewHolder> cacheView = new HashMap<>();
    private LayoutInflater inflater;

    private ChatViewFactory(){
        inflater = (LayoutInflater)MyApplication.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public static ChatViewFactory getInstance(){
        if(instance == null){
            instance = new ChatViewFactory();
        }
        return instance;
    }

    public View createLinkContentView(String messageGuid, LinkPatientMsgDO content, int layoutId){
        ViewHolder viewHolder = null;
        if(cacheView.containsKey(messageGuid)){
            viewHolder = cacheView.get(messageGuid);
        }else{
            View convertView = (View)inflater.inflate(layoutId, null);
            viewHolder = new ViewHolder(convertView);
            viewHolder.titleView.setText(content.getTitle());
            viewHolder.decView.setText(content.getDescription());
            ImageLoader.getInstance().displayImage(content.getImageUrl(), viewHolder.linkImage);
            cacheView.put(messageGuid, viewHolder);
        }
        return viewHolder.view;
    }

    public void clearCache(){
        cacheView.clear();
    }

    public static class ViewHolder{
        public View view;
        public TextView titleView;
        public TextView decView;
        public ImageView linkImage;

        public ViewHolder(View view){
            this.view = view;
            titleView =  ((TextView)(view.findViewById(R.id.link_title_left)));
            decView = ((TextView)(view.findViewById(R.id.link_description_left)));
            linkImage = ((ImageView) (view.findViewById(R.id.link_image_left)));
        }
    }
}
