package com.yetse.yedownloadutil.dao;

import java.io.Serializable;

/**
 * Created by Tse on 2016/1/8.
 * Email Via imyetse@gmail.com
 */
public class DownloadInfoBean implements Serializable {


    private static final long serialVersionUID = -5081491645265373040L;

    public DownloadInfoBean() {
    }

    /**
     * @param threadId      线程id
     * @param startPos      该线程开始位置
     * @param endPos        该线程结束位置
     * @param compeleteSize 该线程完成的大小
     * @param url           下载链接
     */
    public DownloadInfoBean(int threadId, int startPos, int endPos,
                            int compeleteSize, String url) {
        this.threadId = threadId;
        this.startPos = startPos;
        this.endPos = endPos;
        this.compeleteSize = compeleteSize;
        this.url = url;
    }

    private int threadId;

    private int startPos;

    private int endPos;

    private int compeleteSize;

    private String url;

    public int getThreadId() {
        return threadId;
    }

    public void setThreadId(int threadId) {
        this.threadId = threadId;
    }

    public int getStartPos() {
        return startPos;
    }

    public void setStartPos(int startPos) {
        this.startPos = startPos;
    }

    public int getEndPos() {
        return endPos;
    }

    public void setEndPos(int endPos) {
        this.endPos = endPos;
    }

    public int getCompeleteSize() {
        return compeleteSize;
    }

    public void setCompeleteSize(int compeleteSize) {
        this.compeleteSize = compeleteSize;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public String toString() {
        return "DownloadInfo [threadId=" + threadId + ", startPos=" + startPos
                + ", endPos=" + endPos + ", compeleteSize=" + compeleteSize
                + "]";
    }
}
