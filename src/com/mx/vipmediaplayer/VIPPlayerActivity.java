package com.mx.vipmediaplayer;

import io.vov.vitamio.LibsChecker;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.mx.vipmediaplayer.model.VIPMedia;
import com.mx.vipmediaplayer.services.MediaScanService;
import com.mx.vipmediaplayer.services.MediaScanService.IMediaScanObserver;
import com.mx.vipmediaplayer.services.MediaScanService.MediaScanServiceBinder;
import com.mx.vipmediaplayer.ui.ContentGridView;
import com.mx.vipmediaplayer.ui.ContentListView;
import com.mx.vipmediaplayer.ui.MenuListView;
import com.mx.vipmediaplayer.ui.MenuListView.OnMenuClickListener;
import com.mx.vipmediaplayer.util.FileUtils;

/**
 * main activity class of this application
 * 
 * @author Xiao Mei
 * @weibo http://weibo.com/u/1675796095
 * @email tss_chs@126.com
 * 
 */
public class VIPPlayerActivity extends Activity implements OnTouchListener,
        View.OnClickListener, OnMenuClickListener, IMediaScanObserver, ContentListView.OnOnlineVideoClickListener{
    
    private boolean videoscanfinished = false;
    private boolean audioscanfinished = false;
    
    private Object writelock = new Object();
    private List<VIPMedia> videoList = new ArrayList<VIPMedia>();
    private List<VIPMedia> audioList = new ArrayList<VIPMedia>();
    
    private int currentContent = MenuListView.POSITION_PLAYLIST;
    
    public static final int MSG_SHOW_CONTENT = 1;

    public static final int SNAP_VELOCITY = 200;

    private int screenWidth;

    private int leftEdgeForContent = 0;
    private int rightEdgeForContent;

    private int menuPadding = 280;

    private ImageView mVIPSwitcher;
    /**
     * View of Content.
     */
    private View content;

    /**
     * View of Menu
     */
    private View menu;

    private MenuListView menuListView;
    
    private ContentGridView contentGridView;
    private ContentListView contentListView;

    /**
     * menu布局的参数，通过此参数来更改leftMargin的值。
     */
    private RelativeLayout.LayoutParams contentParams;

    /**
     * menu layout parameters
     */
    private RelativeLayout.LayoutParams menuParams;
    
    /**
     * 记录手指按下时的横坐标。
     */
    private float xDown;

    /**
     * 记录手指移动时的横坐标。
     */
    private float xOldMove = 0;
    private float xMove = 0;

    private boolean mDirection = false;

    /**
     * 记录手机抬起时的横坐标。
     */
    private float xUp;

    /**
     * menu当前是显示还是隐藏。只有完全显示或隐藏menu时才会更改此值，滑动过程中此值无效。
     */
    private boolean isMenuVisible;
    private boolean isContentMoving = false;

    /**
     * 用于计算手指滑动的速度。
     */
    private VelocityTracker mVelocityTracker;
    
    private Handler mHandler = new Handler(){
        
    };
    
    private MediaScanService mMediaScanService;
    private ServiceConnection mMediaScanServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Logger.d("service unbinded ================================================ ");
            mMediaScanService.deleteObserver(VIPPlayerActivity.this);
            mMediaScanService = null;
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Logger.d("service binded ================================================ ");
            mMediaScanService = ((MediaScanServiceBinder) service).getService();
            mMediaScanService.addObserver(VIPPlayerActivity.this);
            // Toast.makeText(ComponentServiceActivity.this, "Service绑定成功!",
            // Toast.LENGTH_SHORT).show();
        }
    };
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        
        if (!LibsChecker.checkVitamioLibs(this))
            return;
        
        setContentView(R.layout.activity_main);
        
        initValues();
        content.setOnTouchListener(this);
        new DataTask().execute();

        bindService(
                new Intent(getApplicationContext(), MediaScanService.class),
                mMediaScanServiceConnection, Context.BIND_AUTO_CREATE);
        VipPreference pref = new VipPreference(VIPPlayerActivity.this);
        if (pref.getBoolean(VIPMediaPlayerApplication.PREF_KEY_FIRST,
                true)) {
            Logger.d("first time =================== ");
            getApplicationContext().startService(
                    new Intent(getApplicationContext(),
                            MediaScanService.class).putExtra(
                            MediaScanService.EXTRA_DIRECTORY,
                            Environment.getExternalStorageDirectory()
                                    .getAbsolutePath()));
        }
        onPlaylistClicked();
    }

    /**
     * 初始化一些关键性数据。包括获取屏幕的宽度，给content布局重新设置宽度，给menu布局重新设置宽度和偏移距离等。
     */
    private void initValues() {
        WindowManager window = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        screenWidth = window.getDefaultDisplay().getWidth();

        mVIPSwitcher = (ImageView) findViewById(R.id.IVSwitcher);
        mVIPSwitcher.setOnClickListener(this);
        contentListView = (ContentListView) findViewById(R.id.contentlist);
        contentListView.setActivity(this);
        contentListView.setOnlineVideoClickListener(this);
        
        contentGridView = (ContentGridView) findViewById(R.id.contentgrid);
        contentGridView.setActivity(this);
        
        menuListView = (MenuListView) findViewById(R.id.menulist);
        menuListView.setOnMenuClickListener(this);
        content = findViewById(R.id.content);
        menu = findViewById(R.id.menu);
        menuParams = (RelativeLayout.LayoutParams) menu.getLayoutParams();
        menuParams.width = screenWidth - menuPadding;
        menu.setLayoutParams(menuParams);
        
        leftEdgeForContent = 0;
        // 左边缘的值赋值为menu宽度的负数
        rightEdgeForContent = menuPadding - screenWidth;
        
        contentParams = (RelativeLayout.LayoutParams) content.getLayoutParams();
        // 将menu的宽度设置为屏幕宽度减去menuPadding
        contentParams.width = screenWidth;
        // menu的leftMargin设置为左边缘的值，这样初始化时menu就变为不可见
        // 将content的宽度设置为屏幕宽度
        contentParams.rightMargin = leftEdgeForContent;
        content.setLayoutParams(contentParams);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (isContentMoving)
            return true;
        createVelocityTracker(event);
        switch (event.getAction()) {
        case MotionEvent.ACTION_DOWN:
            // 手指按下时，记录按下时的横坐标
            xDown = event.getRawX();
            break;
        case MotionEvent.ACTION_MOVE:
            // 手指移动时，对比按下时的横坐标，计算出移动的距离，来调整menu的leftMargin值，从而显示和隐藏menu
            xOldMove = xMove;
            xMove = event.getRawX();
            mDirection = xMove - xOldMove > 0
                    || (xMove - xOldMove == 0 && mDirection);
            int distanceX = (int) (xMove - xDown);
            if (isMenuVisible) {
                contentParams.rightMargin = rightEdgeForContent - distanceX;
            } else {
                contentParams.rightMargin = leftEdgeForContent - distanceX;
            }
            if (contentParams.rightMargin > leftEdgeForContent) {
                contentParams.rightMargin = leftEdgeForContent;
            } else if (contentParams.rightMargin < rightEdgeForContent) {
                contentParams.rightMargin = rightEdgeForContent;
            }
            content.setLayoutParams(contentParams);
            break;
        case MotionEvent.ACTION_UP:
            // 手指抬起时，进行判断当前手势的意图，从而决定是滚动到menu界面，还是滚动到content界面
            xUp = event.getRawX();
            if (wantToShowMenu()) {
                if (shouldScrollToMenu()) {
                    scrollToMenu();
                } else {
                    scrollToContent();
                }
            } else if (wantToShowContent()) {
                if (shouldScrollToContent()) {
                    scrollToContent();
                } else {
                    scrollToMenu();
                }
            }
            recycleVelocityTracker();
            break;
        }
        return true;
    }

    /**
     * 判断当前手势的意图是不是想显示content。如果手指移动的距离是负数，且当前menu是可见的，则认为当前手势是想要显示content。
     * 
     * @return 当前手势想显示content返回true，否则返回false。
     */
    private boolean wantToShowContent() {
        return xUp - xDown < 0 && isMenuVisible;
    }

    /**
     * 判断当前手势的意图是不是想显示menu。如果手指移动的距离是正数，且当前menu是不可见的，则认为当前手势是想要显示menu。
     * 
     * @return 当前手势想显示menu返回true，否则返回false。
     */
    private boolean wantToShowMenu() {
        return xUp - xDown > 0 && !isMenuVisible;
    }

    /**
     * 判断是否应该滚动将menu展示出来。如果手指移动距离大于屏幕的1/2，或者手指移动速度大于SNAP_VELOCITY，
     * 就认为应该滚动将menu展示出来。
     * 
     * @return 如果应该滚动将menu展示出来返回true，否则返回false。
     */
    private boolean shouldScrollToMenu() {
        return mDirection /* || xUp - xDown > screenWidth / 2 */
        /* || getScrollVelocity() > SNAP_VELOCITY */;
    }

    /**
     * 判断是否应该滚动将content展示出来。如果手指移动距离加上menuPadding大于屏幕的1/2，
     * 或者手指移动速度大于SNAP_VELOCITY， 就认为应该滚动将content展示出来。
     * 
     * @return 如果应该滚动将content展示出来返回true，否则返回false。
     */
    private boolean shouldScrollToContent() {
        return !mDirection /* || xDown - xUp + menuPadding > screenWidth / 2 */
        /* || getScrollVelocity() > SNAP_VELOCITY */;
    }

    /**
     * 将屏幕滚动到menu界面，滚动速度设定为30.
     */
    private void scrollToMenu() {
        new ContentScrollTask().execute(30);
        // new ScrollTask().execute(30);
    }

    /**
     * 将屏幕滚动到content界面，滚动速度设定为-30.
     */
    private void scrollToContent() {
        new ContentScrollTask().execute(-30);
        // new ScrollTask().execute(-30);
    }

    /**
     * 创建VelocityTracker对象，并将触摸content界面的滑动事件加入到VelocityTracker当中。
     * 
     * @param event
     *            content界面的滑动事件
     */
    private void createVelocityTracker(MotionEvent event) {
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }
        mVelocityTracker.addMovement(event);
    }

    /**
     * 获取手指在content界面滑动的速度。
     * 
     * @return 滑动速度，以每秒钟移动了多少像素值为单位。
     */
    private int getScrollVelocity() {
        mVelocityTracker.computeCurrentVelocity(1000);
        int velocity = (int) mVelocityTracker.getXVelocity();
        return Math.abs(velocity);
    }

    /**
     * 回收VelocityTracker对象。
     */
    private void recycleVelocityTracker() {
        mVelocityTracker.recycle();
        mVelocityTracker = null;
    }

    class ContentScrollTask extends AsyncTask<Integer, Integer, Integer> {

        @Override
        protected Integer doInBackground(Integer... speed) {
            isContentMoving = true;
            int contentRightMargin = contentParams.rightMargin;
            while (true) {
                contentRightMargin -= speed[0];
                if (contentRightMargin < rightEdgeForContent) {
                    contentRightMargin = rightEdgeForContent;
                    break;
                } else if (contentRightMargin > leftEdgeForContent) {
                    contentRightMargin = leftEdgeForContent;
                    break;
                }
                publishProgress(contentRightMargin);
                sleep(20);
            }
            if (speed[0] > 0) {
                isMenuVisible = true;
            } else {
                isMenuVisible = false;
            }
            return contentRightMargin;
        }

        @Override
        protected void onPostExecute(Integer result) {
            contentParams.rightMargin = result;
            content.setLayoutParams(contentParams);
            isContentMoving = false;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            contentParams.rightMargin = values[0];
            content.setLayoutParams(contentParams);
        }

    }

    // class ScrollTask extends AsyncTask<Integer, Integer, Integer> {
    //
    // @Override
    // protected Integer doInBackground(Integer... speed) {
    // int leftMargin = menuParams.leftMargin;
    // // 根据传入的速度来滚动界面，当滚动到达左边界或右边界时，跳出循环。
    // while (true) {
    // leftMargin = leftMargin + speed[0];
    // if (leftMargin > rightEdge) {
    // leftMargin = rightEdge;
    // break;
    // }
    // if (leftMargin < leftEdge) {
    // leftMargin = leftEdge;
    // break;
    // }
    // publishProgress(leftMargin);
    // // 为了要有滚动效果产生，每次循环使线程睡眠20毫秒，这样肉眼才能够看到滚动动画。
    // sleep(20);
    // }
    // if (speed[0] > 0) {
    // isMenuVisible = true;
    // } else {
    // isMenuVisible = false;
    // }
    // return leftMargin;
    // }
    //
    // @Override
    // protected void onProgressUpdate(Integer... leftMargin) {
    // menuParams.leftMargin = leftMargin[0];
    // menu.setLayoutParams(menuParams);
    // }
    //
    // @Override
    // protected void onPostExecute(Integer leftMargin) {
    // menuParams.leftMargin = leftMargin;
    // menu.setLayoutParams(menuParams);
    // }
    // }

    /**
     * 使当前线程睡眠指定的毫秒数。
     * 
     * @param millis
     *            指定当前线程睡眠多久，以毫秒为单位
     */
    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
        case R.id.IVSwitcher:
            if (isMenuVisible) {
                scrollToContent();
            } else {
                scrollToMenu();
            }
            break;
        }
    }

    @Override
    public void onPlaylistClicked() {
        if (contentParams.rightMargin < 0)
            scrollToContent();
        contentGridView.setVisibility(View.GONE);
        contentListView.setVisibility(View.VISIBLE);
    }

    @Override
    public void onLocalVideoClicked() {
        if (contentParams.rightMargin < 0)
            scrollToContent();
        contentGridView.setVisibility(View.VISIBLE);
        contentListView.setVisibility(View.GONE);
        contentGridView.setList(videoList);
        contentGridView.getHandler().sendEmptyMessage(0);
    }

    @Override
    public void onLocalAudioClicked() {
        if (contentParams.rightMargin < 0)
            scrollToContent();
        contentGridView.setVisibility(View.VISIBLE);
        contentListView.setVisibility(View.GONE);
        contentGridView.setList(audioList);
        contentGridView.getHandler().sendEmptyMessage(0);
    }

    @Override
    public void update(int flag, VIPMedia media, int scanfiletype) {
        switch (flag) {
        case MediaScanService.SCAN_STATUS_START:
            synchronized (writelock) {
                if(scanfiletype == MediaScanService.SCAN_INVALID_FILE_TYPE) {
                    videoList.clear();
                    audioList.clear();
                } else if(scanfiletype == MediaScanService.SCAN_VIDEO_FILE_TYPE) {
                    videoList.clear();
                } else if (scanfiletype == MediaScanService.SCAN_AUDIO_FILE_TYPE) {
                    audioList.clear();
                }
            }
            contentGridView.getHandler().sendEmptyMessage(0);
            break;
        case MediaScanService.SCAN_STATUS_END:// 扫描完成
            contentGridView.getHandler().sendEmptyMessage(0);
            break;
        case MediaScanService.SCAN_STATUS_RUNNING:// 扫到一个文件
            synchronized (writelock) {
                if(media.filetype == MediaScanService.SCAN_VIDEO_FILE_TYPE) {
                    videoList.add(media);
                } else if(media.filetype == MediaScanService.SCAN_AUDIO_FILE_TYPE) {
                    audioList.add(media);
                }                
            }
            contentGridView.getHandler().sendEmptyMessage(0);
            break;
        }
    }
    
    private class DataTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... params) {
            synchronized (writelock) {
                videoList.clear();
                videoList.addAll(FileUtils.getAllSortFiles(MediaScanService.SCAN_VIDEO_FILE_TYPE));
                
                audioList.clear();
                audioList.addAll(FileUtils.getAllSortFiles(MediaScanService.SCAN_AUDIO_FILE_TYPE));
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void arg) {
            super.onPostExecute(arg);
            contentGridView.getHandler().sendEmptyMessage(0);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            this.unbindService(mMediaScanServiceConnection);
        } catch (Exception e) {
        }
    }

    @Override
    public void clearAndLoad(String url) {
        
    }

}
