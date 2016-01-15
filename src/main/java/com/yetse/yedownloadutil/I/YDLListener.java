package com.yetse.yedownloadutil.I;

/**
 * Created by Tse on 2016/1/8.
 * Email Via imyetse@gmail.com
 */
public interface YDLListener {
    /**
     * 正在下载
     * @param percent 下载的进度 百分比
     */
    void onProcess(int percent);

    /**
     * 下载成功
     */
    void onSuccess();

    /**
     * 下载失败
     */
    void onFaile(String error);

    /**
     * 开始下载
     */
    void onStart(int fileSize);
}

