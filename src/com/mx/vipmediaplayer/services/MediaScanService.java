package com.mx.vipmediaplayer.services;

import io.vov.vitamio.ThumbnailUtils;
import io.vov.vitamio.provider.MediaStore;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;

import com.mx.vipmediaplayer.Logger;
import com.mx.vipmediaplayer.VIPMediaPlayerApplication;
import com.mx.vipmediaplayer.VipPreference;
import com.mx.vipmediaplayer.database.DatabaseHelper;
import com.mx.vipmediaplayer.model.VIPMedia;
import com.mx.vipmediaplayer.util.ConvertUtils;
import com.mx.vipmediaplayer.util.FileUtils;
import com.mx.vipmediaplayer.util.PinyinUtils;
import com.mx.vipmediaplayer.util.StringUtils;

/**
 * Media Scan service class
 * 
 * @author Xiao Mei
 * @weibo http://weibo.com/u/1675796095
 * @email tss_chs@126.com
 * 
 */
public class MediaScanService extends Service implements Runnable {

    private static final String SERVICE_NAME = "com.mx.vipmediaplayer.services.MediaScannerService";
    /** scan directory*/
    public static final String EXTRA_DIRECTORY = "scan_directory";
    /** scan file */
    public static final String EXTRA_FILE_PATH = "scan_file";
    public static final String EXTRA_MIME_TYPE = "mimetype";
    public static final String EXTRA_FILE_TYPE = "file_type";
    
    public static final int SCAN_INVALID_FILE_TYPE = -1;
    /*
     * scan video files
     */
    public static final int SCAN_VIDEO_FILE_TYPE = 0;
    
    /**
     * scan audio files
     */
    public static final int SCAN_AUDIO_FILE_TYPE = 1;

    public static final int SCAN_STATUS_NORMAL = -1;
    /** 开始扫描 */
    public static final int SCAN_STATUS_START = 0;
    /** 正在扫描 扫描到一个视频文件 */
    public static final int SCAN_STATUS_RUNNING = 1;
    /** 扫描完成 */
    public static final int SCAN_STATUS_END = 2;
    /**  */
    private ArrayList<IMediaScanObserver> observers = new ArrayList<IMediaScanObserver>();
    private ConcurrentHashMap<String, String> mScanMap = new ConcurrentHashMap<String, String>();

    /** 当前状态 */
    private volatile int mServiceStatus = SCAN_STATUS_NORMAL;
    private DatabaseHelper<VIPMedia> mDbHelper;
    private Map<String, Object> mDbWhere = new HashMap<String, Object>(2);
    private int scanfiletype = SCAN_INVALID_FILE_TYPE;

    @Override
    public void onCreate() {
        super.onCreate();

        mDbHelper = new DatabaseHelper<VIPMedia>();
    }

    /** 是否正在运行 */
    public static boolean isRunning() {
        ActivityManager manager = (ActivityManager) VIPMediaPlayerApplication
                .getContext().getSystemService(Context.ACTIVITY_SERVICE);
        for (RunningServiceInfo service : manager
                .getRunningServices(Integer.MAX_VALUE)) {
            if (SERVICE_NAME.equals(service.service.getClassName()))
                return true;
        }
        return false;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Logger.d("start service ============================================== ");
        if (intent != null)
            parseIntent(intent);

        return super.onStartCommand(intent, flags, startId);
    }

    /** 解析Intent */
    private void parseIntent(final Intent intent) {
        final Bundle arguments = intent.getExtras();
        if (arguments != null) {
            scanfiletype = arguments.getInt(EXTRA_FILE_TYPE, SCAN_INVALID_FILE_TYPE);
            if (arguments.containsKey(EXTRA_DIRECTORY)) {
                String directory = arguments.getString(EXTRA_DIRECTORY);
                Logger.i("onStartCommand:" + directory);
                // 扫描文件夹
                if (!mScanMap.containsKey(directory))
                    mScanMap.put(directory, "");
            } else if (arguments.containsKey(EXTRA_FILE_PATH)) {
                // 单文件
                String filePath = arguments.getString(EXTRA_FILE_PATH);
                Logger.i("onStartCommand:" + filePath);
                if (!StringUtils.isEmpty(filePath)) {
                    if (!mScanMap.containsKey(filePath))
                        mScanMap.put(filePath,
                                arguments.getString(EXTRA_MIME_TYPE));
                    // scanFile(filePath, arguments.getString(EXTRA_MIME_TYPE));
                }
            }
        }

        if (mServiceStatus == SCAN_STATUS_NORMAL
                || mServiceStatus == SCAN_STATUS_END) {
            new Thread(this).start();
            // scan();
        }
    }

    @Override
    public void run() {
        scan();
    }

    /** 扫描 */
    private void scan() {
        mServiceStatus = SCAN_STATUS_START;
        // 开始扫描
        notifyObservers(SCAN_STATUS_START, null);

        Set<Entry<String, String>> mSet = mScanMap.entrySet();
        for (Entry<String, String> mEntry : mSet) {
            mServiceStatus = SCAN_STATUS_RUNNING;
            String path = mEntry.getKey();
            String mimeType = mEntry.getValue();
            if ("".equals(mimeType)) {
                scanDirectory(path);
            } else {
                scanFile(path, mimeType);
            }

            // 任务之间歇息一秒
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Logger.e(e);
            }
        }
        mScanMap.clear();

        // 全部扫描完成
        notifyObservers(SCAN_STATUS_END, null);
        mServiceStatus = SCAN_STATUS_END;

        // 第一次扫描
        VipPreference pref = new VipPreference(this);
        if (pref.getBoolean(VIPMediaPlayerApplication.PREF_KEY_FIRST, true))
            pref.putBooleanAndCommit(VIPMediaPlayerApplication.PREF_KEY_FIRST,
                    false);

        // 停止服务
        stopSelf();
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            for (IMediaScanObserver s : observers) {
                if (s != null) {
                    s.update(msg.what, (VIPMedia) msg.obj, scanfiletype);
                }
            }
        }
    };

    /** 扫描文件 */
    private void scanFile(String path, String mimeType) {
        if (scanfiletype == SCAN_INVALID_FILE_TYPE) {
            if (FileUtils.isVideo(path))
                save(new VIPMedia(path, mimeType, SCAN_VIDEO_FILE_TYPE));
            else if (FileUtils.isAudio(path))
                save(new VIPMedia(path, mimeType, SCAN_AUDIO_FILE_TYPE));
        } else if (FileUtils.isVideo(path)
                && scanfiletype == SCAN_VIDEO_FILE_TYPE)
            save(new VIPMedia(path, mimeType, SCAN_VIDEO_FILE_TYPE));
        else if (FileUtils.isAudio(path)
                && scanfiletype == SCAN_AUDIO_FILE_TYPE)
            save(new VIPMedia(path, mimeType, SCAN_AUDIO_FILE_TYPE));
    }

    /** 扫描文件夹 */
    private void scanDirectory(String path) {
        eachAllMedias(new File(path));
    }

    /** 递归查找视频 */
    private void eachAllMedias(File f) {
        if (f != null && f.exists() && f.isDirectory()) {
            File[] files = f.listFiles();
            if (files != null) {
                for (File file : f.listFiles()) {
                    // Logger.i(f.getAbsolutePath());
                    if (file.isDirectory()) {
                        // 忽略.开头的文件夹
                        if (!file.getAbsolutePath().startsWith("."))
                            eachAllMedias(file);
                    } else if (file.exists() && file.canRead()
                            ) {
                        if (scanfiletype == SCAN_INVALID_FILE_TYPE) {
                            if(FileUtils.isVideo(file)) save(new VIPMedia(file, SCAN_VIDEO_FILE_TYPE));
                            else if(FileUtils.isAudio(file)) save(new VIPMedia(file, SCAN_AUDIO_FILE_TYPE));
                        } else if(FileUtils.isVideo(file) && scanfiletype == SCAN_VIDEO_FILE_TYPE) {
                            save(new VIPMedia(file, SCAN_VIDEO_FILE_TYPE));    
                        } else if (FileUtils.isAudio(file) && scanfiletype == SCAN_AUDIO_FILE_TYPE) {
                            save(new VIPMedia(file, SCAN_AUDIO_FILE_TYPE));
                        }
                    }
                }
            }
        }
    }

    /**
     * 保存入库
     * 
     * @throws FileNotFoundException
     */
    private void save(VIPMedia media) {
        mDbWhere.put("path", media.path);
        mDbWhere.put("last_modify_time", media.last_modify_time);
        // 检测
        if (!mDbHelper.exists(media, mDbWhere)) {
            try {
                if (media.title != null && media.title.length() > 0)
                    media.title_key = PinyinUtils.chineneToSpell(media.title
                            .charAt(0) + "");
            } catch (Exception ex) {
                Logger.e(ex);
            }
            media.last_access_time = System.currentTimeMillis();

            // 提取缩略图
            extractThumbnail(media);
            media.mime_type = FileUtils.getMimeType(media.path);

            // 入库
            mDbHelper.create(media);

            // 扫描到一个
            notifyObservers(SCAN_STATUS_RUNNING, media);
        }
    }

    /** 提取生成缩略图 */
    public static void extractThumbnail(VIPMedia media) {
        final Context ctx = VIPMediaPlayerApplication.getContext();
        // ThumbnailUtils.
        Bitmap bitmap = ThumbnailUtils.createVideoThumbnail(ctx, media.path,
                MediaStore.Video.Thumbnails.MINI_KIND);
        try {
            if (bitmap == null) {
                // 缩略图创建失败
                bitmap = Bitmap.createBitmap(
                        ThumbnailUtils.TARGET_SIZE_MINI_THUMBNAIL_WIDTH,
                        ThumbnailUtils.TARGET_SIZE_MINI_THUMBNAIL_HEIGHT,
                        Bitmap.Config.RGB_565);
            }

            media.width = bitmap.getWidth();
            media.height = bitmap.getHeight();

            // 缩略图
            bitmap = ThumbnailUtils.extractThumbnail(bitmap, ConvertUtils
                    .dipToPX(ctx,
                            ThumbnailUtils.TARGET_SIZE_MICRO_THUMBNAIL_WIDTH),
                    ConvertUtils.dipToPX(ctx,
                            ThumbnailUtils.TARGET_SIZE_MICRO_THUMBNAIL_HEIGHT),
                    ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
            if (bitmap != null) {
                // 将缩略图存到视频当前路径
                File thum = new File(
                        VIPMediaPlayerApplication.VIPPLAYER_VIDEO_THUMB, UUID
                                .randomUUID().toString());
                media.thumb_path = thum.getAbsolutePath();
                // thum.createNewFile();
                FileOutputStream iStream = new FileOutputStream(thum);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 85, iStream);
                iStream.close();
            }

            // 入库

        } catch (Exception ex) {
            Logger.e(ex);
        } finally {
            if (bitmap != null)
                bitmap.recycle();

        }
    }

    // ~~~ 状态改变

    /** 通知状态改变 */
    private void notifyObservers(int flag, VIPMedia media) {
        mHandler.sendMessage(mHandler.obtainMessage(flag, media));
    }

    /** 增加观察者 */
    public void addObserver(IMediaScanObserver s) {
        synchronized (this) {
            if (!observers.contains(s)) {
                observers.add(s);
            }
        }
    }

    /** 删除观察者 */
    public synchronized void deleteObserver(IMediaScanObserver s) {
        observers.remove(s);
    }

    /** 删除所有观察者 */
    public synchronized void deleteObservers() {
        observers.clear();
    }

    public interface IMediaScanObserver {
        /**
         * 
         * @param flag
         *            0 开始扫描 1 正在扫描 2 扫描完成
         * @param file
         *            扫描到的视频文件
         *            @param scanfiletype
         *            0 indicates Video file, 1 indicates audio file
         */
        public void update(int flag, VIPMedia media, int scanfiletype);
    }

    // ~~~ Binder

    private final MediaScanServiceBinder mBinder = new MediaScanServiceBinder();

    public class MediaScanServiceBinder extends Binder {
        public MediaScanService getService() {
            return MediaScanService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

}
