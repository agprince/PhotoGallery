package com.agprince.android.photogallery;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.os.MessageQueue;

import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class ThumbnailDownloader<T> extends HandlerThread {
    private static final String TAG = "ThumbnailDownloader";
    private static final int MESSAGE_DOWNLOAD = 0;
    private Handler mReqquestHandler;
    private ConcurrentMap<T, String> mRequestMap = new ConcurrentHashMap<>();

    private Boolean mHasQuit = false;

    public ThumbnailDownloader() {
        super(TAG);
    }

    @Override
    protected void onLooperPrepared() {
        mReqquestHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == MESSAGE_DOWNLOAD) {
                    T target = (T) msg.obj;
                    LogUtil.d("get a url :" + mRequestMap.get(target));

                    handleRequest(target);

                }
            }
        };

    }

    private void handleRequest(final T target) {

        final String url = mRequestMap.get(target);

        if (url == null) {
            return;
        }

        try {
            byte[] bytes = new FlickrFetchr().getUrlBytes(url);
            Bitmap bitmap = BitmapFactory.decodeByteArray(bytes,0,bytes.length);
            LogUtil.d("bitmap create ");

        } catch (IOException e) {
            e.printStackTrace();
            LogUtil.e("bitmap creat erro ",e);
        }

    }

    @Override
    public boolean quit() {
        mHasQuit = true;
        return super.quit();
    }

    public void queueThumbnail(T target, String url) {

        LogUtil.d("Got a url : " + url);
        if (url == null) {
            mRequestMap.remove(target);
        } else {
            mRequestMap.put(target, url);
            mReqquestHandler.obtainMessage(MESSAGE_DOWNLOAD, target).sendToTarget();

        }

    }
}
