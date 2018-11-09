package com.onlinedoctor.activity.tools.clinic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.onlinedoctor.activity.R;
import com.onlinedoctor.log.Logger;
import com.onlinedoctor.util.JsonUtil;
import com.onlinedoctor.util.SharedpreferenceManager;

public class OfficeHoursActivity extends Activity {

    private TextView finish_tv;
    private final String TAG = "OfficeHoursActivity";
    private TextView back_tv;
    private Context mContext = this;
    public static final int WEEK_DAY_NUM = 7;
    public static final int DAY_PART_NUM = 3;
    //	private SharedPreferences sPref = MyApplication.context.getSharedPreferences("clinic_plan", MODE_PRIVATE);
//	private SharedPreferences.Editor spfEditor = sPref.edit();
    public static final String KEY_CLINIC_JSON = "keyClinicJson";
    private SharedpreferenceManager spManager = SharedpreferenceManager.getInstance();
    private HashMap<Integer, List<TextView>> week_plan = new LinkedHashMap<Integer, List<TextView>>();
    private List<TextView> week_day = new ArrayList<TextView>();
    private int[] am = {R.id.clinic_mo_am_ccb,
            R.id.clinic_tu_am_ccb,
            R.id.clinic_we_am_ccb,
            R.id.clinic_th_am_ccb,
            R.id.clinic_fr_am_ccb,
            R.id.clinic_sa_am_ccb,
            R.id.clinic_su_am_ccb};

    private int[] pm = {R.id.clinic_mo_pm_ccb,
            R.id.clinic_tu_pm_ccb,
            R.id.clinic_we_pm_ccb,
            R.id.clinic_th_pm_ccb,
            R.id.clinic_fr_pm_ccb,
            R.id.clinic_sa_pm_ccb,
            R.id.clinic_su_pm_ccb};

    private int[] night = {R.id.clinic_mo_night_ccb,
            R.id.clinic_tu_night_ccb,
            R.id.clinic_we_night_ccb,
            R.id.clinic_th_night_ccb,
            R.id.clinic_fr_night_ccb,
            R.id.clinic_sa_night_ccb,
            R.id.clinic_su_night_ccb};
    private int[] days = {R.id.mon,
            R.id.tues,
            R.id.web,
            R.id.thur,
            R.id.fri,
            R.id.sat,
            R.id.sun};

    private final Handler handler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            Logger.d(TAG, "handleMessage");
            super.handleMessage(msg);
            switch (msg.what) {
                case OfficeHoursNetManager.GET_SUCCESSFUL: {
                    Logger.d(TAG, "GET_SUCCESSFUL");
                    String result = msg.obj.toString();
                    try {
                        JSONObject jo = new JSONObject(result).getJSONObject("data");
                        String data = jo.toString();
                        Log.d("data", data);
                        if (!data.equals("null")) {
                            long time = jo.getLong("time");
                            Log.d("time", Long.toString(time));
                            //spfEditor.putLong("time", time);
                            String clinic_info = jo.getString("clinicInfo");
                            Log.d("clinic_info", clinic_info);
                            //spfEditor.putString("jsonStr", clinic_info);
                            spManager.setOne(KEY_CLINIC_JSON, clinic_info);
                        }
                        convertFromJson();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    break;
                }

                case OfficeHoursNetManager.GET_FAIL:
                {
                    String connect_server_fail = getResources().getString(R.string.connect_server_fail);
                    Toast.makeText(mContext, connect_server_fail, Toast.LENGTH_SHORT).show();
                    convertFromJson();
                    break;
                }
                case OfficeHoursNetManager.UPDATE_SUCCESSFUL: {

                    long time = (Long) msg.obj;
                    //spfEditor.putLong("time", time);
                    Log.d("成功update， time ＝ ", Long.toString(time));
                    break;
                }
                case OfficeHoursNetManager.UPDATE_FAIL: {

                    break;
                }
                default:

                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.clinic_plan);
        finish_tv = (TextView) findViewById(R.id.actionbar_common_backable_right_tv);
        finish_tv.setText("");
        TextView title_tv = (TextView) findViewById(R.id.actionbar_common_backable_title_tv);
        title_tv.setText("门诊时间");

        back_tv = (TextView) findViewById(R.id.actionbar_common_backable_left_tv);

        init();
        Logger.d(TAG, "before netManager");
        OfficeHoursNetManager.get(handler);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //spfEditor.commit();
    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        //back();
        save();
        finish();
    }

    private void init() {
        Logger.d(TAG, "init");
        week_plan.clear();
        for (int i = 0; i < WEEK_DAY_NUM; i++) {
            ArrayList<TextView> arrayList = new ArrayList<TextView>();
            arrayList.add((TextView) findViewById(am[i]));
            arrayList.add((TextView) findViewById(pm[i]));
            arrayList.add((TextView) findViewById(night[i]));
            week_plan.put(Integer.valueOf(i), arrayList);
            week_day.add((TextView) findViewById(days[i]));
        }
        Logger.d(TAG, "after init");
        //设置textview的监听
        for (int i = 0; i < WEEK_DAY_NUM; i++) {
            List<TextView> arrayList = week_plan.get(i);
            for (int j = 0; j < DAY_PART_NUM; j++) {
                final TextView tv = arrayList.get(j);


                tv.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View arg0) {
                        LayoutInflater inflater = LayoutInflater.from(mContext);
                        LinearLayout linearLayout = (LinearLayout) inflater.inflate(R.layout.one_line_edittext, null);
                        final EditText et = (EditText) linearLayout.findViewById(R.id.modify_name_cname_et);
//						final Dialog dialog = new AlertDialog.Builder(mContext).create();
//						dialog.show();
//						dialog.setCancelable(true);
//						dialog.getWindow().setContentView(linearLayout);
//						dialog.setTitle("请输入");

                        //final EditText et = new EditText(mContext);
                        et.setText(tv.getText());
                        et.setSelection(tv.getText().length());
                        new AlertDialog.Builder(mContext)
                                .setTitle("请输入门诊地址")
                                .setView(linearLayout)
                                .setPositiveButton("确定", new DialogInterface.OnClickListener() {

                                    @Override
                                    public void onClick(DialogInterface dialog,
                                                        int which) {
                                        String text = et.getText().toString();
                                        tv.setText(text);
                                        checkSetBlack();
                                        //save();
                                    }
                                })
                                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        checkSetBlack();
                                        dialog.dismiss();
                                    }
                                })
                                .show();


                    }
                });
            }
        }

//        //设置保存按钮监听
//        finish_tv.setOnClickListener(new OnClickListener() {
//
//            @Override
//            public void onClick(View arg0) {
//                save();
//            }
//        });

        //返回时检测是否更改，如果更改，提示保存
        back_tv.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                //back();
                save();
                finish();
            }
        });
    }

    private void save() {
        String jsonStr = convert2Json();
        Log.d("jsonStr", jsonStr);
        OfficeHoursNetManager.update(jsonStr, handler);
        //spfEditor.putString("jsonStr", jsonStr);
        spManager.setOne(KEY_CLINIC_JSON, jsonStr);
        //finish();
    }

    private void back() {
        if (!spManager.getOne(KEY_CLINIC_JSON).equals(convert2Json())) {
            AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
            builder.setMessage("是否保存编辑?");
            builder.setTitle("提示");
            builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    save();
                }

            });
            builder.setNegativeButton("退出", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                    finish();
                }
            });
            builder.create().show();
        } else {
            finish();
        }
    }

    private String convert2Json() {
        String ret;
        WeekPlan wp = new WeekPlan();
        for (int i = 0; i < WEEK_DAY_NUM; i++) {
            List<TextView> arrayList = week_plan.get(i);
            String am_plan = (String) arrayList.get(0).getText();
            String pm_plan = (String) arrayList.get(1).getText();
            String night_plan = (String) arrayList.get(2).getText();
            DayPlan dp = new DayPlan(am_plan, pm_plan, night_plan);

            wp.addDayPlan(dp);
        }

        ret = JsonUtil.objectToJson(wp);
        return ret;
    }


    private void convertFromJson() {
        //spfEditor.commit();
        //String jsonStr = sPref.getString("jsonStr", null);
        String jsonStr = spManager.getOne(KEY_CLINIC_JSON);
        if (jsonStr == null)
            return;
        Log.d("jsonStr", jsonStr);
        try {
            JSONObject jo = new JSONObject(jsonStr);
            String data = jo.getString("wpList");
            Log.d("data", data.toString());
            Gson gson = new Gson();
            DayPlan[] dpArray = gson.fromJson(data, DayPlan[].class);
            int size = dpArray.length;
            for (int i = 0; i < size; i++) {
                List<TextView> arrayList = week_plan.get(i);
                String am_plan = dpArray[i].getAm_plan();
                String pm_plan = dpArray[i].getPm_plan();
                String night_plan = dpArray[i].getNight_plan();
                arrayList.get(0).setText(am_plan);
                arrayList.get(1).setText(pm_plan);
                arrayList.get(2).setText(night_plan);
            }

            checkSetBlack();

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void checkSetBlack() {
        for (int i = 0; i < WEEK_DAY_NUM; i++) {
            String am_plan = week_plan.get(i).get(0).getText().toString();
            String pm_plan = week_plan.get(i).get(1).getText().toString();
            String night_plan = week_plan.get(i).get(2).getText().toString();
            if (!(am_plan.isEmpty() && pm_plan.isEmpty() && night_plan.isEmpty())) {
                week_day.get(i).setTextColor(getResources().getColor(R.color.black));
            }else{
                week_day.get(i).setTextColor(getResources().getColor(R.color.gray_font));
            }
        }
    }

    public static String convertFromJson2String(final String jsonStr) {
        String ret = "";
//		String jsonStr = spManager.getOne(KEY_CLINIC_JSON);
        if (jsonStr == null)
            return ret;
        Log.d("jsonStr", jsonStr);
        try {
            JSONObject jo = new JSONObject(jsonStr);
            String data = jo.getString("wpList");
            Log.d("data", data.toString());
            Gson gson = new Gson();
            DayPlan[] dpArray = gson.fromJson(data, DayPlan[].class);
            int size = dpArray.length;
            String[] weekday = {"周一", "周二", "周三", "周四", "周五", "周六", "周日"};
            String[] dayPart = {"上午", "中午", "晚上"};
            for (int i = 0; i < size; i++) {
                String am_plan = dpArray[i].getAm_plan();
                String pm_plan = dpArray[i].getPm_plan();
                String night_plan = dpArray[i].getNight_plan();
                if (!(am_plan.isEmpty() || am_plan == null))
                    ret = ret + weekday[i] + dayPart[0] + am_plan + "\n";
                if (!(pm_plan.isEmpty() || pm_plan == null))
                    ret = ret + weekday[i] + dayPart[1] + pm_plan + "\n";
                if (!(night_plan.isEmpty() || night_plan == null))
                    ret = ret + weekday[i] + dayPart[2] + night_plan + "\n";
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return ret;
    }
}

class DayPlan {
    private String am_plan;
    private String pm_plan;
    private String night_plan;

    public DayPlan(String am_plan, String pm_plan, String night_plan) {
        super();
        this.am_plan = am_plan;
        this.pm_plan = pm_plan;
        this.night_plan = night_plan;
    }

    public String getAm_plan() {
        return am_plan;
    }

    public void setAm_plan(String am_plan) {
        this.am_plan = am_plan;
    }

    public String getPm_plan() {
        return pm_plan;
    }

    public void setPm_plan(String pm_plan) {
        this.pm_plan = pm_plan;
    }

    public String getNight_plan() {
        return night_plan;
    }

    public void setNight_plan(String night_plan) {
        this.night_plan = night_plan;
    }
}

class WeekPlan {
    private List<DayPlan> wpList = new ArrayList<DayPlan>();

    public void addDayPlan(DayPlan dp) {
        wpList.add(dp);
    }
}
