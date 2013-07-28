package com.mx.vipmediaplayer;


import android.app.Application;
import android.content.Context;
import android.os.Environment;

import com.mx.vipmediaplayer.util.FileUtils;

/**
 * Application class
 * 
 * @author Xiao Mei
 * @weibo http://weibo.com/u/1675796095
 * @email tss_chs@126.com
 * 
 */
public class VIPMediaPlayerApplication extends Application {

    private static VIPMediaPlayerApplication mApplication;

    /** OPlayer SD卡缓存路径 */
    public static final String VIPPLAYER_CACHE_BASE = Environment.getExternalStorageDirectory() + "/vipplayer";
    /** 视频截图缓冲路径 */
    public static final String VIPPLAYER_VIDEO_THUMB = VIPPLAYER_CACHE_BASE + "/thumb/";
    /** 首次扫描 */
    public static final String PREF_KEY_FIRST = "application_first";

    @Override
    public void onCreate() {
        Logger.d("Application on Create ==================================================== ");
        super.onCreate();
        mApplication = this;

        init();
    }

    private void init() {
        //创建缓存目录
        FileUtils.createIfNoExists(VIPPLAYER_CACHE_BASE);
        FileUtils.createIfNoExists(VIPPLAYER_VIDEO_THUMB);
    }

    public static VIPMediaPlayerApplication getApplication() {
        return mApplication;
    }

    public static Context getContext() {
        return mApplication;
    }

    /** 销毁 */
    public void destory() {
        mApplication = null;
    }
}
