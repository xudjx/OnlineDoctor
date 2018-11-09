package com.onlinedoctor.activity.mine.wallet;

import com.onlinedoctor.activity.R;
import com.onlinedoctor.net.MyWalletNetManager;
import com.onlinedoctor.view.CommonActionbarBackableRelativeLayout;
import com.umeng.analytics.MobclickAgent;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class AddBankActivity extends Activity implements TextWatcher {
    private TextView kh;
    private RelativeLayout yh_rl;
    private TextView yh;
    private TextView name;
    private Button btn_add;
    private Context mContext = this;
    private CommonActionbarBackableRelativeLayout title;

    private FrameLayout progressBarHolder;

    Handler mhandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            progressBarHolder.setVisibility(View.GONE);
            if (msg.what == MyWalletNetManager._msg_update_bank) {
                progressBarHolder.setVisibility(View.GONE);
                AddBankActivity.this.finish();
            } else if (msg.what == MyWalletNetManager._msg_fail) {
                String tipInfo = getResources().getString(R.string.wallet_fail_request);
                Toast.makeText(mContext, tipInfo, Toast.LENGTH_SHORT).show();
            } else if (msg.what == MyWalletNetManager._msg_unknown_exception) {
                String tipInfo = getResources().getString(R.string.wallet_unknown_error);
                Toast.makeText(mContext, tipInfo, Toast.LENGTH_SHORT).show();
            }else if (msg.what == MyWalletNetManager._msg_invaild_bankcard) {
                String tipInfo = getResources().getString(R.string.wallet_invalid_bankcard);
                Toast.makeText(mContext, tipInfo, Toast.LENGTH_SHORT).show();
            }
        }
    };


    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.frag_add_bank);
        kh = (TextView) findViewById(R.id.kh);
        yh = (TextView) findViewById(R.id.yh);
        yh_rl = (RelativeLayout) findViewById(R.id.yh_rl);
        name = (TextView) findViewById(R.id.xm);
        btn_add = (Button) findViewById(R.id.add);
        progressBarHolder = (FrameLayout) findViewById(R.id.progressBarHolder);

        btn_add.setEnabled(false);

        title = (CommonActionbarBackableRelativeLayout) findViewById(R.id.title);
        ((TextView) findViewById(R.id.actionbar_common_backable_title_tv))
                .setText("银行账户");
        ((TextView) findViewById(R.id.actionbar_common_backable_right_tv))
                .setVisibility(View.GONE);

        yh_rl.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                Intent intent = new Intent(AddBankActivity.this,
                        ChooseBankActivity.class);
                startActivityForResult(intent, 0);
            }
        });

        Intent intent = getIntent();
        if (intent.getStringExtra("bankcard") != null) {
            kh.setText(intent.getStringExtra("bankcard"));
            name.setText(intent.getStringExtra("bankcardOwner"));
            yh.setText(intent.getStringExtra("bankname"));
//            btn_add.setEnabled(true);
//            btn_add.setBackground(getResources().getDrawable(R.drawable.shape_blue));

        }

        kh.addTextChangedListener(this);
        yh.addTextChangedListener(this);
        name.addTextChangedListener(this);

        btn_add.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                progressBarHolder.setVisibility(View.VISIBLE);
                MyWalletNetManager.updateBank(kh.getText().toString(), yh.getText().toString(), name.getText().toString(), mhandler);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        super.onActivityResult(requestCode, resultCode, data);
        // requestCode标示请求的标示 resultCode表示有数据
        if (requestCode == 0 && resultCode == RESULT_OK) {
            yh.setText(data.getStringExtra("bank"));
        }
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count,
                                  int after) {
        // TODO Auto-generated method stub
        //if(kh.getText().toString().contains("*")) kh.setText("");

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        // TODO Auto-generated method stub

    }

    @Override
    public void afterTextChanged(Editable s) {
        /**
         * Button有效的条件:
         * 1. 设置银行
         * 2. 卡号长度 > 0
         * 3. 卡号有效
         * 4. 设置姓名
         */
        if (kh.getText().length() > 0 &&
                checkBankCard(kh.getText().toString()) &&
                yh.getText().length() > 0 &&
                name.getText().length() > 0) {
            btn_add.setEnabled(true);
            btn_add.setBackground(getResources().getDrawable(R.drawable.shape_blue));
        } else {
            btn_add.setEnabled(false);
            btn_add.setBackground(getResources().getDrawable(R.drawable.gray_blue));
        }
    }

    /**
     * 从不含校验位的银行卡卡号采用 Luhm 校验算法获得校验位
     *
     * @param
     * @return
     */
    private boolean checkBankCard(String cardId) {
        char bit = getBankCardCheckCode(cardId
                .substring(0, cardId.length() - 1));
        if (bit == 'N') {
            return false;
        }
        return cardId.charAt(cardId.length() - 1) == bit;
    }

    private char getBankCardCheckCode(String nonCheckCodeCardId) {
        if (nonCheckCodeCardId == null
                || nonCheckCodeCardId.trim().length() == 0
                || !nonCheckCodeCardId.matches("\\d+")) {
            // 如果传的不是数据返回N
            return 'N';
        }
        char[] chs = nonCheckCodeCardId.trim().toCharArray();
        int luhmSum = 0;
        for (int i = chs.length - 1, j = 0; i >= 0; i--, j++) {
            int k = chs[i] - '0';
            if (j % 2 == 0) {
                k *= 2;
                k = k / 10 + k % 10;
            }
            luhmSum += k;
        }
        return (luhmSum % 10 == 0) ? '0' : (char) ((10 - luhmSum % 10) + '0');
    }

    @Override
    protected void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
    }
}
