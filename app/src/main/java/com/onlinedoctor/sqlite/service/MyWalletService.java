package com.onlinedoctor.sqlite.service;

import com.onlinedoctor.pojo.mine.MyWallet;

/**
 * Created by wds on 15/10/4.
 */
public interface MyWalletService {
    public void insert(MyWallet myWallet);
    public void update(MyWallet myWallet);
    public void delete(int id);
    public MyWallet get(int id);
    public boolean isEmpty();
}
