package com.yetse.yedownloadutil.dao;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Tse on 2016/1/8.
 * Email Via imyetse@gmail.com
 */
public class DBHelper extends SQLiteOpenHelper {
    private static final String SQL_NAME = "dl.db";
    private static final int DOWNLOAD_VERSION = 1;

    public DBHelper(Context context) {
        super(context, SQL_NAME, null, DOWNLOAD_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table " + DownloadDao.DL_TABLE_NAME + "(" + DownloadDao.ID + " integer PRIMARY KEY AUTOINCREMENT, " +
                DownloadDao.THREAD_ID + " integer, " +
                DownloadDao.START_POS + " integer, " +
                DownloadDao.END_POS + " integer, " +
                DownloadDao.COMPELETE_SIZE + " integer," +
                DownloadDao.URL + " char)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
