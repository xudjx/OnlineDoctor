package com.onlinedoctor.sqlite.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

import com.onlinedoctor.pojo.Common;
import com.onlinedoctor.pojo.mine.MyWallet;
import com.onlinedoctor.sqlite.DBOpenHelper;
import com.onlinedoctor.sqlite.service.MyWalletService;

import java.util.List;

/**
 * Created by wds on 15/10/4.
 */
public class MyWalletServiceImpl implements MyWalletService {

    private DBOpenHelper dbHelper;
    private static final String TABLE = "my_wallet_tb";

    public MyWalletServiceImpl(Context context, List<String> updateSql) {
        dbHelper = DBOpenHelper.createInstance();
    }

    @Override
    public void insert(MyWallet myWallet) {
        long id = -1;
        SQLiteDatabase db=null;
        String sql = "insert into my_wallet_tb(total, available, unavailable) values(?,?,?)";
        db = dbHelper.getWritableDatabase();
        SQLiteStatement stat = db.compileStatement(sql);
        stat.bindDouble(1, myWallet.getTotal());
        stat.bindDouble(2, myWallet.getAvailable());
        stat.bindDouble(3, myWallet.getUnavailable());
        id = stat.executeInsert();
        myWallet.setId((int)id);
    }

    @Override
    public void update(MyWallet myWallet) {
        Boolean flag = false;
        SQLiteDatabase db = null;
        db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("total", myWallet.getTotal());
        values.put("available", myWallet.getAvailable());
        values.put("unavailable", myWallet.getUnavailable());
        db.update(TABLE, values, "id = ?", new String[]{Integer.toString(myWallet.getId())});
        db.close();

    }

    @Override
    public void delete(int id) {
        SQLiteDatabase db=null;
        db = dbHelper.getWritableDatabase();
        db.delete(TABLE, "id=?", new String[]{Integer.toString(id)});
        db.close();
    }

    @Override
    public MyWallet get(int id) {
        SQLiteDatabase db = null;
        MyWallet myWallet = null;
        db = dbHelper.getReadableDatabase();
        String sql = "select * from my_wallet_tb where id = ?";
        Cursor cursor = db.rawQuery(sql, new String[] {Integer.toString(id)});
        while(cursor.moveToNext()){
            float total = cursor.getFloat(cursor.getColumnIndex("total"));
            float available = cursor.getFloat(cursor.getColumnIndex("available"));
            float unavailable = cursor.getFloat(cursor.getColumnIndex("unavailable"));
            myWallet = new MyWallet();
            myWallet.setTotal(total);
            myWallet.setAvailable(available);
            myWallet.setUnavailable(unavailable);
            myWallet.setId(id);
        }
        cursor.close();
        db.close();
        return myWallet;
    }

    @Override
    public boolean isEmpty() {
        SQLiteDatabase db = null;
        MyWallet myWallet= new MyWallet();
        db = dbHelper.getReadableDatabase();
        String sql = "select * from my_wallet_tb";
        Cursor cursor = db.rawQuery(sql,null);
        while(cursor.moveToNext()){
            return false; //表不为空
        }
        return true; //表为空
    }
}
