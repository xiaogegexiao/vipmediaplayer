package com.mx.vipmediaplayer.ui;

import android.app.Service;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.mx.vipmediaplayer.R;

/**
 * Menu ListView class
 * 
 * @author Xiao Mei
 * @weibo http://weibo.com/u/1675796095
 * @email tss_chs@126.com
 * 
 */
public class MenuListView extends ListView implements
        android.widget.AdapterView.OnItemClickListener {

    public static final int POSITION_PLAYLIST = 0;
    public static final int POSITION_LOCALVIDEO = 1;
    public static final int POSITION_LOCALAUDIO = 2;

    public static MenuItem[] datalist = {
            new MenuItem(POSITION_PLAYLIST, 0, R.string.playlist),
            new MenuItem(POSITION_LOCALVIDEO, 0, R.string.localvideo),
            new MenuItem(POSITION_LOCALAUDIO, 0, R.string.localmusic) };

    private OnMenuClickListener mMenuClickListenr = null;
    private LayoutInflater mInflator = null;

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams)this.getLayoutParams();
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    public MenuListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initViews(context);
    }

    public MenuListView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MenuListView(Context context) {
        super(context);
        initViews(context);
    }

    private void initViews(Context ctx) {
        mInflator = (LayoutInflater) ctx
                .getSystemService(Service.LAYOUT_INFLATER_SERVICE);
        this.setOnItemClickListener(this);
        this.setAdapter(mAdapter);
        mAdapter.notifyDataSetChanged();
    }

    BaseAdapter mAdapter = new BaseAdapter() {

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = mInflator.inflate(R.layout.menu_item, null);
            }

            MenuItem mi = datalist[position];
            ImageView ivicon = (ImageView) convertView
                    .findViewById(R.id.IVIcon);
            TextView tvtext = (TextView) convertView.findViewById(R.id.TVText);
            convertView.setTag(datalist[position]);
            if (mi.iconResId != 0)
                ivicon.setImageResource(mi.iconResId);
            if (mi.textId != 0)
                tvtext.setText(mi.textId);
            else
                tvtext.setText("null");
            return convertView;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public Object getItem(int arg0) {
            if (datalist != null) {
                try {
                    return datalist[arg0];
                } catch (Exception e) {
                    return null;
                }
            }
            return null;
        }

        @Override
        public int getCount() {
            return datalist.length;
        }
    };

    public void setOnMenuClickListener(OnMenuClickListener argListener) {
        this.mMenuClickListenr = argListener;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position,
            long id) {
        switch (position) {
        case POSITION_PLAYLIST:
            if (mMenuClickListenr != null)
                mMenuClickListenr.onPlaylistClicked();
            break;
        case POSITION_LOCALVIDEO:
            if (mMenuClickListenr != null)
                mMenuClickListenr.onLocalVideoClicked();
            break;
        case POSITION_LOCALAUDIO:
            if (mMenuClickListenr != null)
                mMenuClickListenr.onLocalAudioClicked();
            break;
        }
    }

    public interface OnMenuClickListener {
        public void onPlaylistClicked();

        public void onLocalVideoClicked();

        public void onLocalAudioClicked();
    }

    public static class MenuItem {
        public MenuItem(int argPosition, int argIconResId, int argTextId) {
            position = argPosition;
            iconResId = argIconResId;
            textId = argTextId;
        }

        public int position = -1;
        public int iconResId = 0;
        public int textId = 0;
    }
}
