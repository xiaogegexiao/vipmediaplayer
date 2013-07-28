package com.mx.vipmediaplayer.util;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.mx.vipmediaplayer.model.VIPMedia;
import com.mx.vipmediaplayer.model.VIPMediaBitmap;
import com.mx.vipmediaplayer.services.MediaScanService;

/**
 * Image Load class
 * 
 * @author Xiao Mei
 * @weibo http://weibo.com/u/1675796095
 * @email tss_chs@126.com
 * 
 */
public class ImageLoadUtils {

    static ThreadPoolExecutor threadPool = new ThreadPoolExecutor(20, 40, 3,
            TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(50),
            new ThreadPoolExecutor.DiscardOldestPolicy());

    public static class LoadImgThread extends Thread {

        private VIPMedia media;
        private MethodHandler<VIPMediaBitmap> handler;

        public LoadImgThread(VIPMedia argmedia,
                MethodHandler<VIPMediaBitmap> arghandler) {
            media = argmedia;
            handler = arghandler;
        }

        @Override
        public void run() {
            MediaScanService.extractThumbnail(media);
            Bitmap bt = BitmapFactory.decodeFile(media.thumb_path);
            VIPMediaBitmap tb = new VIPMediaBitmap(bt, media);
            handler.process(tb);
        }
    }

    public static void readBitmapAsync(VIPMedia media,
            MethodHandler<VIPMediaBitmap> handler) {
        Bitmap bt = readImg(media);
        if (bt == null) {
            LoadImgThread thread = new LoadImgThread(media, handler);
            threadPool.execute(thread);
        } else {
            if (handler != null) {
                handler.process(new VIPMediaBitmap(bt, media));
            }
        }
    }

    public static Bitmap readImg(VIPMedia media) {
        return ImageBuffer.readImg(media);
    }
}
