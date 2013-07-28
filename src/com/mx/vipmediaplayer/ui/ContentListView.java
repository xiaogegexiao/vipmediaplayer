package com.mx.vipmediaplayer.ui;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.mx.vipmediaplayer.R;
import com.mx.vipmediaplayer.model.OnlineVideo;
import com.mx.vipmediaplayer.ui.helper.XmlReaderHelper;


/**
 * Content ListView class
 * 
 * @author Xiao Mei
 * @weibo http://weibo.com/u/1675796095
 * @email tss_chs@126.com
 * 
 */
public class ContentListView extends ListView implements AdapterView.OnItemClickListener{
    private List<OnlineVideo> mList = new ArrayList<OnlineVideo>();
    
    private Activity activity = null;
    private Context context = null;
    private LayoutInflater mInflater;
    
    private int level = 1;
    
    private final static ArrayList<OnlineVideo> root = new ArrayList<OnlineVideo>();
    private ArrayList<OnlineVideo> tvs;
    private final static ArrayList<OnlineVideo> videos = new ArrayList<OnlineVideo>();
    
    private OnOnlineVideoClickListener mOnlineVideoClickListener = null;
    
    static {

        // private final static String[] CATEGORY = { "电视直播", "视频网站" };
        root.add(new OnlineVideo("Live TV", R.drawable.logo_live, 1));
        root.add(new OnlineVideo("Video Sites", R.drawable.logo_youku, 0));

        videos.add(new OnlineVideo("Youku", R.drawable.logo_youku, 0,
                "http://3g.youku.com"));
        videos.add(new OnlineVideo("Sohu", R.drawable.logo_sohu, 0,
                "http://m.tv.sohu.com"));
        videos.add(new OnlineVideo("LETV", R.drawable.logo_letv, 0,
                "http://m.letv.com"));
        videos.add(new OnlineVideo("IQIYI", R.drawable.logo_iqiyi, 0,
                "http://3g.iqiyi.com/"));
        videos.add(new OnlineVideo("PPTV", R.drawable.logo_pptv, 0,
                "http://m.pptv.com/"));
        videos.add(new OnlineVideo("Tencent", R.drawable.logo_qq, 0,
                "http://3g.v.qq.com/"));
        videos.add(new OnlineVideo("56.com", R.drawable.logo_56, 0,
                "http://m.56.com/"));
        videos.add(new OnlineVideo("Sina", R.drawable.logo_sina, 0,
                "http://video.sina.cn/"));
        videos.add(new OnlineVideo("Tomato", R.drawable.logo_tudou, 0,
                "http://m.tudou.com"));
    }

    public ContentListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initView(context);
    }

    public ContentListView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ContentListView(Context context) {
        super(context);
        initView(context);
    }
    
    public void setActivity(Activity activity) {
        this.activity = activity;
    }
    
    public void setOnlineVideoClickListener(OnOnlineVideoClickListener listener) {
        mOnlineVideoClickListener = listener;
    }

    private void initView(Context ctx) {
        context = ctx;
        mInflater = (LayoutInflater) ctx.getApplicationContext()
                .getSystemService(Service.LAYOUT_INFLATER_SERVICE);
        mList.clear();
        this.setAdapter(mAdapter);
        mAdapter.notifyDataSetChanged();
        this.setOnItemClickListener(this);
        mAdapter.replace(root);
        level = 1;
        mAdapter.notifyDataSetChanged();
    }
    
    private DataAdapter mAdapter = new DataAdapter();
    
    public class DataAdapter extends BaseAdapter {
        
        private List datalist;

        @Override
        public int getCount() {
            if (datalist != null)
                return datalist.size();
            return 0;
        }

        @Override
        public Object getItem(int position) {
            try {
                return datalist.get(position);
            }catch (Exception e) {
            }
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final OnlineVideo item = (OnlineVideo)getItem(position);
            if (item == null) return null;
            if (convertView == null) {
                convertView = mInflater
                        .inflate(R.layout.content_listitem, null);
            }
            ImageView thumbnail = (ImageView) convertView
                    .findViewById(R.id.thumbnail);
            if (item.iconId > 0)
                thumbnail.setImageResource(item.iconId);
            else
                thumbnail.setImageDrawable(null);
            ((TextView) convertView.findViewById(R.id.title))
                    .setText(item.title);

            return convertView;
        }
        
        public void replace(ArrayList<OnlineVideo> list) {
            if (list == null)
                list = new ArrayList<OnlineVideo>();
            datalist = list;
        }
    };

    @Override
    public void onItemClick(AdapterView<?> parent, View arg1, int position, long arg3) {
        final OnlineVideo item = (OnlineVideo)mAdapter.getItem(position);
        switch (level) {
        case 1:// level 1
            level = 2;
            if (position == 0) {
                // live tv
                if (tvs == null)
                    tvs = XmlReaderHelper.getAllCategory(activity);
                mAdapter.replace(tvs);
            } else {
                // online video
                mAdapter.replace(videos);
            }
            mAdapter.notifyDataSetChanged();
            break;
        case 2:// level 2
            level = 3;
            if (item.id != null) {
                // live tv
                mAdapter.replace(XmlReaderHelper.getVideos(activity, item.id));
                mAdapter.notifyDataSetChanged();
            } else {
                if (mOnlineVideoClickListener != null)
                    mOnlineVideoClickListener.clearAndLoad(item.url);
            }
            break;
        case 3: // level 3
            level = 4;
            // clearAndLoad(item.url);
            Intent intent = new Intent(activity, VideoPlayerActivity.class);
            intent.putExtra("path", item.url);
            intent.putExtra("title", item.title);
            activity.startActivity(intent);
            break;
        case 4: // level 4
            level = 4;
            Intent anotherIntent = new Intent(activity,
                    VideoPlayerActivity.class);
            anotherIntent.putExtra("path", item.url);
            anotherIntent.putExtra("title", item.title);
            activity.startActivity(anotherIntent);
            break;
        }
    }
    
    public static interface OnOnlineVideoClickListener {
        public void clearAndLoad(String url);
    }
}
