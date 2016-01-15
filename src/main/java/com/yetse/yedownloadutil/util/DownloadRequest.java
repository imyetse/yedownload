package com.yetse.yedownloadutil.util;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.util.Pair;

import com.yetse.yedownloadutil.I.YDLListener;
import com.yetse.yedownloadutil.dao.DownloadDao;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Tse on 2016/1/8.
 * Email Via imyetse@gmail.com
 */
public class DownloadRequest {
    public static final int DOWNLOAD_START = 101;
    public static final int DOWNLOAD_FAIL = 102;
    public static final int DOWNLOAD_PROCESS = 103;
    public static final int DOWNLOAD_SUCCESS = 104;


    public static class DLBuilder {
        private DownloadTool downloadTool;
        private List<Pair<String, String>> mRequestHeaders = new ArrayList<>();

        public DLBuilder(String localPath, String fileName, String url, Context context) {
            downloadTool = new DownloadTool(url, localPath, fileName, context);
        }

        public DLBuilder setThreadCount(int count) {
            downloadTool.setThreadCount(count);
            return this;
        }

        public DLBuilder isShowNotify(boolean show) {
            downloadTool.isShowNotify(show);
            return this;
        }

        public DLBuilder setToken(String token) {
            addRequestHeader("Authorization", token);
            return this;
        }

        /**
         * Add an HTTP header to be included with the download request.  The header will be added to
         * the end of the list.
         *
         * @param header HTTP header name
         * @param value  header value
         * @return this object
         * @see <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec4.html#sec4.2">HTTP/1.1
         * Message Headers</a>
         */
        public DLBuilder addRequestHeader(String header, String value) {
            if (header == null) {
                throw new NullPointerException("header cannot be null");
            }
            if (header.contains(":")) {
                throw new IllegalArgumentException("header may not contain ':'");
            }
            if (value == null) {
                value = "";
            }
            mRequestHeaders.add(Pair.create(header, value));
            return this;
        }

        public DownloadTool build() {
            downloadTool.setmRequestHeaders(mRequestHeaders);
            return downloadTool;
        }
    }
}
