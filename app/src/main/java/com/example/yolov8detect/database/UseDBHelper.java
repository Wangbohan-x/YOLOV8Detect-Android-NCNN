package com.example.yolov8detect.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.yolov8detect.entiy.User;

import java.util.ArrayList;
import java.util.List;


public class UseDBHelper extends SQLiteOpenHelper {
    private static final String DB_NAME = "user.db";
    private static final String TABLE_NAME = "user_info";
    private static final int DB_VERSION = 1;
    private static UseDBHelper mHelper = null;
    //创建读写实例
    private SQLiteDatabase mRDB = null;
    private SQLiteDatabase mWDB = null;

    // 为什么传入context
    // 单例模式
    private UseDBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    //利用单例模式，构造静态方法获取唯一实例
    public static UseDBHelper getInstance(Context context) {
        if (mHelper == null) {
            mHelper = new UseDBHelper(context);
        }
        return mHelper;
    }

    //打开数据库的读连接
    public SQLiteDatabase openReadLink() {
        if (mRDB == null || !mRDB.isOpen()) {
            mRDB = mHelper.getReadableDatabase();
        }
        return mRDB;
    }

    //打开数据库的写连接
    public SQLiteDatabase openWriteLink() {
        if (mWDB == null || !mWDB.isOpen()) {
            mWDB = mHelper.getWritableDatabase();
        }
        return mWDB;
    }

    //关闭数据库连接
    public void closeLink(){
        if(mRDB != null && mRDB.isOpen()){
            mRDB.close();
            mRDB = null;
        }
        if(mWDB != null && mWDB.isOpen()){
            mWDB.close();
            mWDB = null;
        }
    }

    //创建数据库，执行
    @Override
    public void onCreate(SQLiteDatabase db) {
        //字段之间有逗号，结束的括号后面有分号
        String sql = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " (" +
                "_id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL," +
                "phone VARCHAR NOT NULL," +
                "pws VARCHAR NOT NULL," +
                "name VARCHAR NOT NULL);";
        db.execSQL(sql);
    }

    //数据库版本更新
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    //以上是初始化操作
    //插入
    public long insert(User user){
        //初始化插入的数据类型
        ContentValues values = new ContentValues();
        values.put("phone", user.phone);
        values.put("pws", user.pws);
        values.put("name", user.name);
        //插入数据
        //如果插入数据时没有内容，会导致无法插入，第二个参数就是给系统一列，进行插入，防止无内容插入
        return mWDB.insert(TABLE_NAME, null, values);
    }


    public long deleteById(String name){
       return mWDB.delete(TABLE_NAME, "name=?", new String[]{name});
    }

    public long Update(User user){
        ContentValues values = new ContentValues();
        values.put("phone", user.phone);
        values.put("pws", user.pws);
        values.put("name", user.name);
        return mWDB.update(TABLE_NAME, values, "name=?", new String[]{user.name});
    }

    public List<User> queryALL(){
        List<User> list = new ArrayList<>();
        Cursor cursor =  mRDB.query(TABLE_NAME, null, null, null, null, null, null);
        while (cursor.moveToNext()){
            User user = new User();
            user.id = cursor.getInt(0);
            user.phone = cursor.getString(1);
            user.pws = cursor.getString(2);
            user.name = cursor.getString(3);
            list.add(user);
        }
        return list;
    }

    public List<User> queryByName(String name){
        List<User> list = new ArrayList<>();
        Cursor cursor =  mRDB.query(TABLE_NAME, null, "name = ?", new String[]{name}, null, null, null);
        while (cursor.moveToNext()){
            User user = new User();
            user.id = cursor.getInt(0);
            user.phone = cursor.getString(1);
            user.pws = cursor.getString(2);
            user.name = cursor.getString(3);
            list.add(user);
        }
        return list;
    }
}
