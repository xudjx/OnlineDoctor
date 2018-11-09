package com.onlinedoctor.activity.mine.fee;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import com.google.gson.Gson;
import com.onlinedoctor.activity.R;
import com.onlinedoctor.adapter.mine.SelfDefinedFeesAdapter;
import com.onlinedoctor.log.Logger;
import com.onlinedoctor.net.HandlerManager;
import com.onlinedoctor.net.mine.SelfDefinedFeeNetManager;
import com.onlinedoctor.pojo.Common;
import com.onlinedoctor.pojo.mine.SelfDefinedFee;
import com.onlinedoctor.sqlite.dao.SelfDefinedFeesImpl;
import com.umeng.analytics.MobclickAgent;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class SelfDefinedFeesActivity extends Activity {
    private ListView listView = null;
    private TextView add_tv;
    private ImageView bg_iv;
    private String TAG = "SelfDefinedFeesActivity";
    private SelfDefinedFeesAdapter sdfAdapter = null;
    private SelfDefinedFeesImpl sdfImpl = null;
    private List<SelfDefinedFee> sdfList = new ArrayList();
    private Context mContext = this;
    private FrameLayout progressBarHolder;
    private RelativeLayout relLayout = null;

    private int cdTime = 10000;
    private long currentTime = 0;

    private DecimalFormat decimalFormat=new DecimalFormat("0.00");


    private final Handler handler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case SelfDefinedFeeNetManager.DOWNLOAD_SUCCESSFUL:
                    try {
                        JSONObject jo = new JSONObject(msg.obj.toString());
                        String data = jo.getString("data");
                        Log.d("data", data);
                        Gson gson = new Gson();
                        ChargingItem[] list = gson.fromJson(data, ChargingItem[].class);

                        sdfImpl.clean();
                        int size = list.length;
                        for (int i = 0; i < size; i++){
                            if (list[i].getIsDelete() == 0){ //表示在服务器中该条目没有被删除
                                SelfDefinedFee sdf = new SelfDefinedFee();
                                sdf.setDescription(list[i].getDescription());

                                //服务起发过来的fee单位为［分］,而客户端保存的是［元］,因此需要做转化
                                double localFee = ((double)list[i].getFee())/100;
                                sdf.setFee(decimalFormat.format(localFee)+"");

                                sdf.setGlobal_id(list[i].getId());
                                sdf.setTitle(list[i].getTitle());
                                sdf.setUpdate_time(list[i].getTime());
                                sdfImpl.add(sdf);
                            }
                        }
                        refleshListViewFromLocalDB();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    break;

                case SelfDefinedFeeNetManager.DELETE_SUCCESSFUL:

                    break;
                case Common.MSG_WHAT_MINE_CHARGING:
                    finish();

                    break;
                case SelfDefinedFeeNetManager.REQUEST_FAIL:
                    Toast.makeText(mContext, "自定义收费请求失败", Toast.LENGTH_SHORT).show();
                    break;
                default:

                    break;
            }
            progressBarHolder.setVisibility(View.GONE);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.self_defined_fees);
        TextView title_tv = (TextView) findViewById(R.id.actionbar_common_backable_title_tv);
        title_tv.setText("自定义收费列表");
        add_tv = (TextView) findViewById(R.id.actionbar_common_backable_right_tv);
        add_tv.setText("添加");

        sdfImpl = new SelfDefinedFeesImpl(SelfDefinedFeesActivity.this, null);

        listView = (ListView) findViewById(R.id.listview);
        bg_iv = (ImageView) findViewById(R.id.bg_iv);
        sdfAdapter = new SelfDefinedFeesAdapter(mContext,
                R.layout.item_labels_2, sdfList);
        listView.setAdapter(sdfAdapter);

        progressBarHolder = (FrameLayout) findViewById(R.id.progressBarHolder);

        relLayout = (RelativeLayout) findViewById(R.id.relLayout);
        //每次进入到该activity的界面的时候，从服务器上同步数据到本地
        progressBarHolder.setVisibility(View.VISIBLE);
        SelfDefinedFeeNetManager.download(handler);

        clickListener();
    }

    @Override
    protected void onResume() {
        Log.d(TAG, "onResume");
        super.onResume();
        refleshListViewFromLocalDB();
        MobclickAgent.onResume(this);
    }


    @Override
    protected void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
    }

    private void clickListener() {
        listView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View v, int position,
                                    long id) {
                Intent fromIntent = getIntent();
                if (fromIntent.getBooleanExtra("check", false) == false) {
                    Log.d(TAG, "点击事件");
                    Intent intent = new Intent();
                    intent.setClass(mContext, SelfDefinedFeeDetail.class);
                    intent.putExtra("id", sdfList.get(position).getId());
                    intent.putExtra("title", sdfList.get(position).getTitle());
                    intent.putExtra("description", sdfList.get(position).getDescription());
                    intent.putExtra("fee", sdfList.get(position).getFee());
                    intent.putExtra("global_id", sdfList.get(position).getGlobal_id());
                    Log.d("global_id", Long.toString(sdfList.get(position).getGlobal_id()));
                    intent.putExtra("update_time", sdfList.get(position).getUpdate_time());
                    intent.putExtra("flag", 1);
                    startActivity(intent);
                } else {
                    long time = System.currentTimeMillis();
                    if ((time - currentTime) > cdTime) {
                        //从chat里跳转过来的，需要发送到服务器check
                        Log.d(TAG, "想服务器check该自定义收费");
                        String patientId = fromIntent.getStringExtra("patientId");
                        HandlerManager handlerManager = HandlerManager.getManager();
                        if (handlerManager.getChatHandler() != null) {
                            currentTime = System.currentTimeMillis();
                            progressBarHolder.setVisibility(View.VISIBLE);
                            SelfDefinedFeeNetManager.request(handlerManager.getChatHandler(), handler,
                                    sdfList.get(position), patientId);
                        }
                    }
                }
            }
        });


        listView.setOnItemLongClickListener(new OnItemLongClickListener() {

            @Override
            public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
                                           final int position, long id) {
                Log.d(TAG, "长按事件");
                final String[] items = {"删除"};
                new AlertDialog.Builder(mContext).setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0:
                                refleshListViewFromLocalDB();
                                SelfDefinedFee sdf = sdfList.get(position);
                                Log.d(TAG, "从本地db中删除该自定义收费");
                                sdfImpl.delete(sdf);
                                Log.d(TAG, "更新调查问卷的listview");
                                sdfList.remove(position);
                                sdfAdapter.notifyDataSetChanged();
                                dialog.dismiss();
                                Log.d(TAG, "通知服务器删除该自定义收费");
                                progressBarHolder.setVisibility(View.VISIBLE);
                                SelfDefinedFeeNetManager.delete(sdf, handler);

                                if (sdfList.isEmpty()) {
                                    bg_iv.setVisibility(View.VISIBLE);
                                    listView.setVisibility(View.GONE);
                                    //relLayout.setBackgroundColor(getResources().getColor(R.color.white));
                                } else {
                                    bg_iv.setVisibility(View.GONE);
                                    listView.setVisibility(View.VISIBLE);
                                    //relLayout.setBackgroundColor(getResources().getColor(R.color.withcard_background));
                                }

                                break;
                            default:
                                break;
                        }
                    }
                }).show();
                return true;
            }
        });

        add_tv.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, SelfDefinedFeeDetail.class);
                intent.putExtra("flag", 2);
                startActivity(intent);
            }
        });
    }

    //从本地DB中读取所有listview数据刷到界面上
    private void refleshListViewFromLocalDB() {
        sdfList.clear();
        sdfList.addAll(sdfImpl.getAll());
        Logger.d("sdfList.size = ", Integer.toString(sdfList.size()));
        sdfAdapter.notifyDataSetChanged();
        if (sdfList.isEmpty()) {
            bg_iv.setVisibility(View.VISIBLE);
            listView.setVisibility(View.GONE);
            //relLayout.setBackgroundColor(getResources().getColor(R.color.white));
        } else {
            bg_iv.setVisibility(View.GONE);
            listView.setVisibility(View.VISIBLE);
            //relLayout.setBackgroundColor(getResources().getColor(R.color.withcard_background));
        }
    }
}

class ChargingItem implements Serializable {
    private long id;
    private String title;
    private String description;
    private int fee;
    private String doctorId;
    private long time;
    private int isDelete;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public float getFee() {
        return fee;
    }

    public void setFee(int fee) {
        this.fee = fee;
    }

    public String getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(String doctorId) {
        this.doctorId = doctorId;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public int getIsDelete() {
        return isDelete;
    }

    public void setIsDelete(int isDelete) {
        this.isDelete = isDelete;
    }
}