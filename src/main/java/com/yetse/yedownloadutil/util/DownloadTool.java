package com.yetse.yedownloadutil.util;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.util.Pair;

import com.squareup.okhttp.Call;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.yetse.yedownloadutil.I.YDLListener;
import com.yetse.yedownloadutil.dao.DownloadDao;
import com.yetse.yedownloadutil.dao.DownloadInfoBean;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;

/**
 * 如果有多个下载  要创建多个累对象
 * 有一些数据对象不能是共享的
 * <p/>
 * Created by Tse on 2016/1/8.
 * Email Via imyetse@gmail.com
 */
public class DownloadTool {
    private static final String TAG = DownloadTool.class.getSimpleName();
    private YDLListener listener;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case DownloadRequest.DOWNLOAD_START:
                    if (listener != null) {
                        listener.onStart(fileSize);
                    }
                    break;
                case DownloadRequest.DOWNLOAD_FAIL:
                    if (listener != null) {
                        listener.onFaile(msg.obj.toString());
                    }
                    break;
                case DownloadRequest.DOWNLOAD_SUCCESS:
                case DownloadRequest.DOWNLOAD_PROCESS:
                    synchronized (this) {
                        globalCompleteSize += msg.arg1;
                    }
                    if (listener != null) {
                        int percent = (int) (((double) globalCompleteSize / (double) fileSize) * 100);
                        listener.onProcess(percent);
                        if (percent == 100) {
                            listener.onSuccess();
                            //完成下载后删除数据库中的记录
                            compelete();
                        }
                    }
                    break;
            }
        }
    };
    /**
     * 用来下载的线程数 默认是两条线程数
     */
    private int threadCount = 2;
    /**
     * 下载的链接
     */
    private String dlURL;

    /**
     * 下载的路径
     */
    private String localPath;

    /**
     * 下载的文件名
     */
    private String fileName;

    /**
     * 整个文件的大小
     */
    private int fileSize;


    /**
     * 整个文件完成的大小
     */
    private int globalCompleteSize;

    /**
     * 是否显示在状态栏
     */
    private boolean isShowNotify;

    /**
     * 下载信息
     */
    private List<DownloadInfoBean> downloadInfoBeanList;

    /**
     * HTTP headers to be included with the download request.
     */
    private List<Pair<String, String>> mRequestHeaders = new ArrayList<>();

    /**
     * 这个实例是所有类对象共享的
     * 载数据库操作类
     */
    private static DownloadDao downloadDao;

    /**
     * 用枚举器来表示下载的状态
     */
    private enum DLState {
        Ready, Downloading, Pause, Delete
    }

    /**
     * 当前的下载状态
     */
    private DLState currentState = DLState.Ready;

    public DownloadTool(String url, String localPath, String fileName,
                        Context context) {
        super();
        this.dlURL = url;
        this.localPath = localPath;
        this.fileName = fileName;
        downloadDao = new DownloadDao(context);
    }

    /**
     * 在开始下载前 要进行准备
     * 比如查看数据库  看看之前是否下载了一半
     */
    private void ready() {
        Log.e(TAG, "ready");
        downloadInfoBeanList = downloadDao.getInfos(dlURL);
        if (downloadInfoBeanList.size() == 0) {
            //数据库没有记录
            FirstDownload();
        } else {
            File file = new File(localPath + "/" + fileName);
            if (!file.exists()) {
                //文件被删了之类的情况 重新下载
                FirstDownload();
            } else {
                //继续下载
                fileSize = downloadInfoBeanList.get(downloadInfoBeanList.size() - 1).getEndPos();
                for (DownloadInfoBean bean : downloadInfoBeanList) {
                    globalCompleteSize += bean.getCompeleteSize();
                }
                Log.e(TAG, "globalCompleteSize:" + globalCompleteSize);
            }
        }
    }

    /**
     * 开始下载
     */
    public void start(YDLListener listener) {
        Log.e(TAG, "start");
        // 下载之前首先异步线程调用ready方法获得文件大小信息，之后调用开始方法

        new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... arg0) {
                ready();
                return null;
            }

            @Override
            protected void onPostExecute(Void result) {
                Log.e(TAG, "downloadedSize::" + globalCompleteSize);
                if (downloadInfoBeanList != null) {
                    if (currentState == DLState.Downloading) {
                        return;
                    }
                    currentState = DLState.Downloading;
                    download();
                }
            }
        }.execute();
        this.listener = listener;
    }

    private void download() {
        for (final DownloadInfoBean info : downloadInfoBeanList) {
            Log.e(TAG, "startThread threadid:" + info.getThreadId());
            final int startPos = info.getStartPos();
            final int endPos = info.getEndPos();
            final int compeleteSize = info.getCompeleteSize();
            final int threadId = info.getThreadId();
            final int totalThreadSize = endPos - startPos + 1;
            final String url = info.getUrl();

            OkHttpClient okHttpClient = new OkHttpClient();
            Request.Builder builder = new Request.Builder();
            builder.url(dlURL);
            builder.addHeader("Range", "bytes="
                    + (startPos + compeleteSize) + "-" + endPos);
            for (Pair<String, String> header : mRequestHeaders) {
                //添加头信息
                builder.addHeader(header.first, header.second);
            }
            Request request = builder.build();
            Call call = okHttpClient.newCall(request);
            call.enqueue(new Callback() {
                @Override
                public void onFailure(Request request, IOException e) {
                    sendMessage(DownloadRequest.DOWNLOAD_FAIL, 0, fileSize, e.getMessage().toString());
                }

                @Override
                public void onResponse(Response response) throws IOException {
                    RandomAccessFile randomAccessFile = null;
                    InputStream is = null;
                    int tCompleteSize = compeleteSize;
                    try {
                        randomAccessFile = new RandomAccessFile(localPath
                                + File.separator + fileName, "rwd");
                        randomAccessFile.seek(startPos + tCompleteSize);
                        //开始下载 通知ui线程
                        sendMessage(DownloadRequest.DOWNLOAD_START, 0, fileSize, null);
                        is = response.body().byteStream();
                        byte[] buffer = new byte[1024];
                        int length = -1;
                        while ((length = is.read(buffer)) != -1) {
                            randomAccessFile.write(buffer, 0, length);
                            tCompleteSize += length;
                            //下载过程 通知ui线程更新
                            sendMessage(DownloadRequest.DOWNLOAD_PROCESS, length, fileSize, url);
                            // 当程序不再是下载状态的时候，纪录当前的下载进度
                            if ((currentState != DLState.Downloading)
                                    || (tCompleteSize >= totalThreadSize)) {
                                downloadDao.updataInfos(threadId, tCompleteSize, url);
                                break;
                            }
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                        //有可能是网络断了之类的问题
                        downloadDao.updataInfos(threadId, compeleteSize, url);
                    } finally {
                        try {
                            if (is != null) {
                                is.close();
                            }
                            randomAccessFile.close();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            });


        }
    }

    /**
     * 暂停下载
     */
    public void pause() {
        this.currentState = DLState.Pause;
        downloadDao.closeDb();
    }

    /**
     * 删除下载
     */
    public void delete() {
        this.currentState = DLState.Delete;
        compelete();
        //删除下载的文件
        new File(localPath + File.separator + fileName).delete();
    }

    public void compelete() {
        downloadDao.delete(dlURL);
        downloadDao.closeDb();
    }

    /**
     * @param state  更新的状态
     * @param arg1   已经下载的大小
     * @param arg2   文件的总大小
     * @param object 文件的下载地址
     */
    private void sendMessage(int state, int arg1, int arg2, Object object) {
        Message msg_st = Message.obtain();
        msg_st.what = state;
        msg_st.arg1 = arg1;
        msg_st.arg2 = arg2;
        msg_st.obj = object;
        handler.sendMessage(msg_st);
    }

    /**
     * 下载的时候是否现在是状态栏
     *
     * @param show
     */
    public void isShowNotify(boolean show) {
        this.isShowNotify = show;
    }

    /**
     * headers to include in the http
     *
     * @param headers
     */
    public void setmRequestHeaders(List<Pair<String, String>> headers) {
        this.mRequestHeaders = headers;
    }


    /**
     * 设置线程数 默认是2
     *
     * @param count
     */
    public void setThreadCount(int count) {
        this.threadCount = count;
    }

    /**
     * 第一次下载进行数据的初始化
     */
    private void FirstDownload() {
        Log.e(TAG, "FirstDownload");
        OkHttpClient okHttpClient = new OkHttpClient();
        final Request request = new Request.Builder().url(dlURL).build();
        final Call call = okHttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                Log.e(TAG, "------>onFailure:" + e.getMessage().toString());
            }

            @Override
            public void onResponse(Response response) throws IOException {
                fileSize = (int) response.body().contentLength();
                Log.e(TAG, "FirstDownload fileSize:" + fileSize);
                Log.e(TAG, "FirstDownload Headers:" + response.headers());
                Log.e(TAG, "FirstDownload Request:" + response.request().toString());
                Log.e(TAG, "FirstDownload Response:" + response.toString());
                File fileParent = new File(localPath);
                if (!fileParent.exists()) {
                    fileParent.mkdir();
                }
                File file = new File(localPath, fileName);
                if (!file.exists())
                    file.createNewFile();
                //访问本地文件
                //多线程下载 要用到RandomAccessFile
                RandomAccessFile accessFile = new RandomAccessFile(file, "rwd");
                accessFile.setLength(fileSize);
                accessFile.close();

                int range = fileSize / threadCount;
                downloadInfoBeanList = new ArrayList<DownloadInfoBean>();
                for (int i = 0; i < threadCount - 1; i++) {
                    DownloadInfoBean info = new DownloadInfoBean(i, i * range, (i + 1) * range
                            - 1, 0, dlURL);
                    downloadInfoBeanList.add(info);
                }
                //这个是总下载信息
                DownloadInfoBean info = new DownloadInfoBean(threadCount - 1, (threadCount - 1)
                        * range, fileSize - 1, 0, dlURL);
                downloadInfoBeanList.add(info);
                downloadDao.insertInfos(downloadInfoBeanList);
            }
        });

    }


}
