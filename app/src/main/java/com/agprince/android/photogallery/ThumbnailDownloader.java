package com.agprince.android.photogallery;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.os.MessageQueue;
import android.support.v4.util.LruCache;

import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class ThumbnailDownloader<T> extends HandlerThread {
    private static final String TAG = "ThumbnailDownloader";
    private static final int MESSAGE_DOWNLOAD = 0;
    private Handler mReqquestHandler;
    private Handler mResponseHandler;
    private ThumbnailDownloadListener<T> mThumbnailDownloadListener;
    private LruCache<String, Bitmap> mLruCache;
    private ConcurrentMap<T, String> mRequestMap = new ConcurrentHashMap<>();

    private Boolean mHasQuit = false;

    public ThumbnailDownloader(Handler responseHandler) {
        super(TAG);
        mResponseHandler = responseHandler;
        long maxSzie = Runtime.getRuntime().maxMemory() / 1024;
        int cachSize = (int) (maxSzie / 8);
        mLruCache = new LruCache<String, Bitmap>(cachSize) {
            @Override
            protected int sizeOf(String key, Bitmap value) {
                return value.getRowBytes() * value.getHeight() / 1024;

            }
        };

    }

    public interface ThumbnailDownloadListener<T> {
        void onThumbnailDownloaded(T target, Bitmap thumbnail);
    }

    public void setOnThumbnailDownloadListener(ThumbnailDownloadListener listener) {
        mThumbnailDownloadListener = listener;

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
            if (mLruCache.get(url) == null) {
                byte[] bytes = new FlickrFetchr().getUrlBytes(url);
                final Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                mLruCache.put(url, bitmap);
            }

            final Bitmap bitmap = mLruCache.get(url);

            LogUtil.d("bitmap create ");
            mResponseHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (mRequestMap.get(target) != url || mHasQuit) {
                        return;
                    }
                    mRequestMap.remove(target);
                    mThumbnailDownloadListener.onThumbnailDownloaded(target, bitmap);
                }
            });

        } catch (IOException e) {
            e.printStackTrace();
            LogUtil.e("bitmap creat erro ", e);
        }

    }

    @Override
    public boolean quit() {
        mHasQuit = true;
        return super.quit();
    }

    public void clearQueue() {
        mReqquestHandler.removeMessages(MESSAGE_DOWNLOAD);
        mRequestMap.clear();
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
