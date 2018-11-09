package com.onlinedoctor.util;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

/**
 * Created by wds on 15/12/13.
 */
public class EditTextUtil {
    /**
     * EditText保留小数点后两位
     */
    public static void valueAfterDot(final EditText edittext, final int len) {
        edittext.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start,
                                      int before, int count) {

                //删除“.”后面超过2位后的数据
                if (s.toString().contains(".")) {
                    if (s.length() - 1 - s.toString().indexOf(".") > len) {
                        s = s.toString().subSequence(0,
                                s.toString().indexOf(".") + len + 1);
                        edittext.setText(s);
                        edittext.setSelection(s.length()); //光标移到最后
                    }
                }
                //如果"."在起始位置,则起始位置自动补0
                if (s.toString().trim().substring(0).equals(".")) {
                    s = "0" + s;
                    edittext.setText(s);
                    edittext.setSelection(2);
                }

                //如果起始位置为0,且第二位跟的不是".",则无法后续输入
                if (s.toString().startsWith("0")
                        && s.toString().trim().length() > 1) {
                    if (!s.toString().substring(1, 2).equals(".")) {
                        edittext.setText(s.subSequence(0, 1));
                        edittext.setSelection(1);
                        return;
                    }
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {
            }
            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

}
