package com.onlinedoctor.activity.mine.wallet;

import org.json.JSONException;
import org.json.JSONObject;

import com.onlinedoctor.activity.R;
import com.onlinedoctor.net.MyWalletNetManager;
import com.onlinedoctor.view.CommonActionbarBackableRelativeLayout;
import com.umeng.analytics.MobclickAgent;

import android.app.Activity;
import android.content.Intent;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class CashActivity extends Activity {

    private CommonActionbarBackableRelativeLayout title;
    private TextView saveTextView;
    private LinearLayout alipay_item, mobile_item, bank_item;
    private TextView alipay_account, mobile_account, bank_account;
    private ImageView alipay_tick, mobile_tick, bank_tick;
    private LinearLayout add_alipay, add_mobile, add_bank;
    private Button btn_withdraw;
    private TextView tv_bank;
    private ImageView img_bank;
    private FrameLayout progressBarHolder;
    int current_chosen = -1;// 0 for alipay; 1 for mobile; 2 for bank
    String guidCellphone, cellphone, guidBankcard, bankcard, bankname,
            bankcardOwner, guidAlipay, alipay;

    Handler mhandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            progressBarHolder.setVisibility(View.GONE);
            if (msg.what == MyWalletNetManager._msg_get_withdraw_type) {
                try {
                    JSONObject jo = new JSONObject(msg.obj.toString());
                    guidCellphone = jo.isNull("guidCellphone") ? null : jo.getString("guidCellphone");
                    cellphone = jo.isNull("cellphone") ? null : jo.getString("cellphone");
                    guidBankcard = jo.isNull("guidBankcard") ? null : jo.getString("guidBankcard");
                    bankcard = jo.isNull("bankcard") ? null : jo.getString("bankcard");
                    bankname = jo.isNull("bankname") ? null : jo.getString("bankname");
                    bankcardOwner = jo.isNull("bankcardOwner") ? null : jo.getString("bankcardOwner");
                    guidAlipay = jo.isNull("guidAlipay") ? null : jo.getString("guidAlipay");
                    alipay = jo.isNull("alipay") ? null : jo.getString("alipay");
                    if (guidAlipay != null && !guidAlipay.equals("")) {
                        alipay_account.setText(alipay);
                        alipay_item.setVisibility(View.VISIBLE);
                        add_alipay.setVisibility(View.GONE);
                    } else {
                        alipay_item.setVisibility(View.GONE);
                        add_alipay.setVisibility(View.VISIBLE);
                    }
                    if (guidCellphone != null && !guidCellphone.equals("")) {
                        mobile_account.setText(cellphone);
                        mobile_item.setVisibility(View.VISIBLE);
                        add_mobile.setVisibility(View.GONE);
                    } else {
                        mobile_item.setVisibility(View.GONE);
                        add_mobile.setVisibility(View.VISIBLE);
                    }
                    if (guidBankcard != null && !guidBankcard.equals("")) {
                        bank_account.setText(bankcard);
                        tv_bank.setText(bankname);

                        TypedArray images = getResources().obtainTypedArray(R.array.bank_icon);
                        String[] names = getResources().getStringArray(R.array.bank);

                        for (int i = 0; i < names.length; i++) {
                            if (names[i].equals(bankname)) {
                                img_bank.setImageDrawable(images.getDrawable(i));
                            }
                        }
                        bank_item.setVisibility(View.VISIBLE);
                        add_bank.setVisibility(View.GONE);
                    } else {
                        bank_item.setVisibility(View.GONE);
                        add_bank.setVisibility(View.VISIBLE);
                    }
                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                    Toast.makeText(CashActivity.this, "无法解析服务器数据",
                            Toast.LENGTH_SHORT).show();
                }
            } else if (msg.what == MyWalletNetManager._msg_fail) {
                String tipInfo = getResources().getString(R.string.wallet_fail_request);
                Toast.makeText(CashActivity.this, tipInfo, Toast.LENGTH_SHORT).show();
            } else if (msg.what == MyWalletNetManager._msg_unknown_exception) {
                String tipInfo = getResources().getString(R.string.wallet_unknown_error);
                Toast.makeText(CashActivity.this, tipInfo, Toast.LENGTH_SHORT).show();
            }  else if (msg.what == MyWalletNetManager._msg_invaild_alipay) {
                String tipInfo = getResources().getString(R.string.wallet_invaild_alipay);
                Toast.makeText(CashActivity.this, tipInfo, Toast.LENGTH_SHORT).show();
            }
        }
    };

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.frag_cash);
        title = (CommonActionbarBackableRelativeLayout) findViewById(R.id.title);
        ((TextView) title.findViewById(R.id.actionbar_common_backable_title_tv))
                .setText("提现");
        saveTextView = ((TextView) findViewById(R.id.actionbar_common_backable_right_tv));
        saveTextView.setText("编辑");
        saveTextView.setVisibility(View.GONE);
        alipay_item = (LinearLayout) findViewById(R.id.alipay_item);
        mobile_item = (LinearLayout) findViewById(R.id.mobile_item);
        bank_item = (LinearLayout) findViewById(R.id.bank_item);

        alipay_account = (TextView) findViewById(R.id.alipay_account);
        mobile_account = (TextView) findViewById(R.id.mobile_account);
        bank_account = (TextView) findViewById(R.id.bank_account);

        alipay_tick = (ImageView) findViewById(R.id.alipay_tick);
        mobile_tick = (ImageView) findViewById(R.id.mobile_tick);
        bank_tick = (ImageView) findViewById(R.id.bank_tick);

        add_alipay = (LinearLayout) findViewById(R.id.add_alipay);
        add_mobile = (LinearLayout) findViewById(R.id.add_mobile);
        add_bank = (LinearLayout) findViewById(R.id.add_bank);

        btn_withdraw = (Button) findViewById(R.id.withdraw);

        tv_bank = (TextView) findViewById(R.id.tv_bank);

        progressBarHolder = (FrameLayout) findViewById(R.id.progressBarHolder);

        img_bank = (ImageView) findViewById(R.id.img_bank);


        btn_withdraw.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                Intent intent = new Intent(CashActivity.this,
                        WithdrawActivity.class);
                switch (current_chosen) {
                    case 0:
                        intent.putExtra("guid", guidAlipay);
                        intent.putExtra("type", "支付宝(" + alipay + ")");
                        startActivity(intent);
                        break;
                    case 1:
                        intent.putExtra("guid", guidCellphone);
                        intent.putExtra("type", "手机充值(" + cellphone + ")");
                        startActivity(intent);
                        break;
                    case 2:
                        intent.putExtra("guid", guidBankcard);
                        intent.putExtra("type", bankname + "(" + bankcard + ")");
                        startActivity(intent);
                        break;
                    default:
                        break;
                }
            }

        });

        saveTextView.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                Intent intent = new Intent();
                switch (current_chosen) {
                    case 0:
                        intent.setClass(CashActivity.this, AddAlipayActivity.class);
                        intent.putExtra("alipay", alipay);
                        startActivity(intent);
                        break;
                    case 1:
                        intent.setClass(CashActivity.this, AddMobileActivity.class);
                        intent.putExtra("cellphone", cellphone);
                        startActivity(intent);
                        break;
                    case 2:
                        intent.setClass(CashActivity.this, AddBankActivity.class);
                        intent.putExtra("bankcard", bankcard);
                        intent.putExtra("bankname", bankname);
                        intent.putExtra("bankcardOwner", bankcardOwner);
                        startActivity(intent);
                        break;
                }

            }
        });

        alipay_item.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                current_chosen = 0;
                alipay_tick.setVisibility(View.VISIBLE);
                mobile_tick.setVisibility(View.GONE);
                bank_tick.setVisibility(View.GONE);
                btn_withdraw.setBackground(getResources().getDrawable(R.drawable.shape_blue));
                btn_withdraw.setEnabled(true);
                saveTextView.setVisibility(View.VISIBLE);
            }

        });

        mobile_item.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                current_chosen = 1;
                alipay_tick.setVisibility(View.GONE);
                mobile_tick.setVisibility(View.VISIBLE);
                bank_tick.setVisibility(View.GONE);
                btn_withdraw.setBackground(getResources().getDrawable(R.drawable.shape_blue));
                btn_withdraw.setEnabled(true);
                saveTextView.setVisibility(View.VISIBLE);
            }

        });

        bank_item.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                current_chosen = 2;
                alipay_tick.setVisibility(View.GONE);
                mobile_tick.setVisibility(View.GONE);
                bank_tick.setVisibility(View.VISIBLE);
                btn_withdraw.setBackground(getResources().getDrawable(R.drawable.shape_blue));
                btn_withdraw.setEnabled(true);
                saveTextView.setVisibility(View.VISIBLE);
            }

        });

        add_alipay.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CashActivity.this,
                        AddAlipayActivity.class);
                startActivity(intent);
            }

        });

        add_mobile.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CashActivity.this,
                        AddMobileActivity.class);
                startActivity(intent);
            }

        });

        add_bank.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

                Intent intent = new Intent(CashActivity.this,
                        AddBankActivity.class);
                startActivity(intent);
            }

        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        btn_withdraw.setBackground(getResources().getDrawable(R.drawable.gray_blue));
        btn_withdraw.setEnabled(false);
        alipay_item.setVisibility(View.GONE);
        mobile_item.setVisibility(View.GONE);
        bank_item.setVisibility(View.GONE);

        add_alipay.setVisibility(View.GONE);
        add_mobile.setVisibility(View.GONE);
        add_bank.setVisibility(View.GONE);
        alipay_tick.setVisibility(View.GONE);
        mobile_tick.setVisibility(View.GONE);
        bank_tick.setVisibility(View.GONE);
        progressBarHolder.setVisibility(View.VISIBLE);
        MyWalletNetManager.getWithdrawType(mhandler);
        MobclickAgent.onResume(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
    }
}
