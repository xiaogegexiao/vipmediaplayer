package com.mx.vipmediaplayer.ui;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.mx.vipmediaplayer.R;
import com.mx.vipmediaplayer.model.VIPMedia;
import com.mx.vipmediaplayer.model.VIPMediaBitmap;
import com.mx.vipmediaplayer.services.MediaScanService;
import com.mx.vipmediaplayer.util.ImageLoadUtils;
import com.mx.vipmediaplayer.util.MethodHandler;
/**
 * Content GridView class
 * 
 * @author Xiao Mei
 * @weibo http://weibo.com/u/1675796095
 * @email tss_chs@126.com
 * 
 */
public class ContentGridView extends GridView implements AdapterView.OnItemClickListener{

    private List<VIPMedia> mList = new ArrayList<VIPMedia>();
    private LayoutInflater mInflater = null;
    private Context context = null;
    private int filetype = MediaScanService.SCAN_INVALID_FILE_TYPE;
    private int horizontalspacing = 0, columnwidth = 0;
    private Activity activity;

    private Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case 0:
                mAdapter.notifyDataSetChanged();
                break;
            case 1:
                if (filetype != MediaScanService.SCAN_INVALID_FILE_TYPE) {
                    if (msg.obj != null)
                        mList.add((VIPMedia) msg.obj);
                    mAdapter.notifyDataSetChanged();
                }
                break;
            }
        }
    };
    
    public void setActivity(Activity activity) {
        this.activity = activity;
    }
    
    public void setFileType(int argfiletype) {
        this.filetype = argfiletype;
    }

    public Handler getHandler() {
        return mHandler;
    }

    public void setList(List<VIPMedia> arglist) {
        mList = arglist;
    }

    public ContentGridView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initView(context);
    }
    
    public ContentGridView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ContentGridView(Context context) {
        super(context);
        initView(context);
    }

    private void initView(Context ctx) {
        context = ctx;
        mInflater = (LayoutInflater) ctx.getApplicationContext()
                .getSystemService(Service.LAYOUT_INFLATER_SERVICE);
        mList.clear();
        this.setAdapter(mAdapter);
        mAdapter.notifyDataSetChanged();
        /**
         * try to define the spacing and column width
         * column width define 10 times of spcaing
         * with 3 columns and 2 spacings and 2 paddings(one left, one right)
         *             ********** ********** **********
         *             *        * *        * *        *
         *             *        * *        * *        *
         *             *        * *        * *        *
         *             ********** ********** **********
         */
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        horizontalspacing = (int)Math.ceil(dm.widthPixels / 34d);
        columnwidth = (int)Math.floor((dm.widthPixels - 4 * horizontalspacing) / 3d);
        setHorizontalSpacing(horizontalspacing);
        setVerticalSpacing(horizontalspacing);
        setColumnWidth(columnwidth);
        setNumColumns(3);
        setStretchMode(GridView.STRETCH_COLUMN_WIDTH);
        setPadding(horizontalspacing, 0, horizontalspacing, 0);
        setOnItemClickListener(this);
    }

    private BaseAdapter mAdapter = new BaseAdapter() {

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.content_griditem, null);
            }
            
            final View cv = convertView;
            VIPMedia item = mList.get(position);
            convertView.setTag(item);
            ImageView image = (ImageView) convertView
                    .findViewById(R.id.IVThumbNail);
            TextView text = (TextView) convertView.findViewById(R.id.TVDesc);
            Bitmap bt = ImageLoadUtils.readImg(item);
            text.setText(item.title);
            if (bt != null) {
                image.setImageBitmap(bt);
            } else {
                ImageLoadUtils.readBitmapAsync(item,
                        new MethodHandler<VIPMediaBitmap>() {
                            public void process(VIPMediaBitmap para) {
                                Message msg = mMethodHandler.obtainMessage(0,
                                        cv);
                                Bundle data = new Bundle();
                                data.putString("path", para.getVipmedia().path);
                                msg.setData(data);
                                msg.sendToTarget();
                            }
                        });
            }

            return convertView;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public Object getItem(int position) {
            if (mList != null) {
                try {
                    return mList.get(position);
                } catch (Exception e) {
                    return null;
                }
            }
            return null;
        }

        @Override
        public int getCount() {
            if (mList != null)
                return mList.size();
            return 0;
        }
    };

    private Handler mMethodHandler = new Handler() {
        public void handleMessage(Message msg) {

            View view = (View) msg.obj;
            VIPMedia item = null;
            try {
                item = (VIPMedia) view.getTag();
            } catch (Exception e) {
            }
            String filepath = null;
            try {
                filepath = msg.getData().getString("path");
            } catch (Exception e) {
            }
            if (view != null && item != null && item.path.equals(filepath)) {
                ((ImageView) view.findViewById(R.id.IVThumbNail))
                        .setImageBitmap(ImageLoadUtils.readImg(item));
            }
        }
    };

    @Override
    public void onItemClick(AdapterView<?> arg0, View view, int arg2, long arg3) {
        final VIPMedia f = (VIPMedia) view.getTag();
        Intent intent = new Intent(activity, VideoPlayerActivity.class);
        intent.putExtra("path", f.path);
        intent.putExtra("title", f.title);
        activity.startActivity(intent);        
    }
}
