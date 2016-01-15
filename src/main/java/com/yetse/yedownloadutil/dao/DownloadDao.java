package com.yetse.yedownloadutil.dao;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * 要考虑一下线程安全的问题
 *
 * Created by Tse on 2016/1/8.
 * Email Via imyetse@gmail.com
 */
public class DownloadDao {
    public static final String ID = "_id";
    public static final String DL_TABLE_NAME = "download_info";
    public static final String THREAD_ID = "thread_id";
    public static final String START_POS = "start_pos";
    public static final String END_POS = "end_pos";
    public static final String COMPELETE_SIZE = "compelete_size";
    public static final String URL = "url";
    private DBHelper dbHelper;

    public DownloadDao(Context context) {
        dbHelper = new DBHelper(context);
    }

    /**
     * 保存下载的信息
     *
     * @param infos 所有线程的下载信息
     */
    public void insertInfos(List<DownloadInfoBean> infos) {
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        for (DownloadInfoBean infoBean : infos) {
            String sql = "insert into " + DL_TABLE_NAME +
                    "(" + THREAD_ID + "," + START_POS + "," + END_POS + "," + COMPELETE_SIZE + "," + URL + ") " +
                    "values (?,?,?,?,?)";
            Object[] objects
                    = {infoBean.getThreadId(), infoBean.getStartPos(), infoBean.getEndPos(), infoBean.getCompeleteSize(), infoBean.getUrl()};
            database.execSQL(sql, objects);
        }

    }

    /**
     * 获取指定链接之前的没下载完的记录
     *
     * @param url 下载链接
     * @return
     */
    public List<DownloadInfoBean> getInfos(String url) {
        List<DownloadInfoBean> infoBeans = new ArrayList<>();
        SQLiteDatabase sqLiteDatabase = dbHelper.getWritableDatabase();
        String sql = "select " + THREAD_ID + ", " + START_POS + ", " + END_POS + ", " + COMPELETE_SIZE + ", " + URL +
                " from " + DL_TABLE_NAME + " where " + URL + "=?";
        Cursor cursor = sqLiteDatabase.rawQuery(sql, new String[]{url});
        while (cursor.moveToNext()) {
            DownloadInfoBean bean = new DownloadInfoBean(cursor.getInt(0), cursor.getInt(1),
                    cursor.getInt(2), cursor.getInt(3), cursor.getString(4));
            infoBeans.add(bean);
        }
        return infoBeans;
    }


    /**
     * 更新数据库中的下载信息
     *
     * @param threadId      某次下载链接的某个下载线程
     * @param compeleteSize 某个下载线程完成的下载字节
     * @param urlstr        下载的链接
     */
    public void updataInfos(int threadId, int compeleteSize, String urlstr) {
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        String sql = "update " + DL_TABLE_NAME + " set " + COMPELETE_SIZE + "=? where " + THREAD_ID + "=? and " + URL + "=?";
        Object[] bindArgs = {compeleteSize, threadId, urlstr};
        database.execSQL(sql, bindArgs);
    }

    /**
     * 关闭数据库
     */
    public void closeDb() {
        dbHelper.close();
    }

    /**
     * 下载完成后删除数据库中的数据
     */
    public void delete(String url) {
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        database.delete(DL_TABLE_NAME, URL + "=?", new String[]{url});
    }
}
