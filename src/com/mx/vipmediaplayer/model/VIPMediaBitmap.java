package com.mx.vipmediaplayer.model;

import android.graphics.Bitmap;

/**
 * VIP Media & Bitmap pack class
 * 
 * @author Xiao Mei
 * @weibo http://weibo.com/u/1675796095
 * @email tss_chs@126.com
 * 
 */
public class VIPMediaBitmap {
    private Bitmap img;
    private VIPMedia vipmedia;

    public VIPMediaBitmap(Bitmap img, VIPMedia argmedia) {
        this.img = img;
        this.vipmedia = argmedia;
    }

    public Bitmap getImg() {
        return img;
    }

    public void setImg(Bitmap img) {
        this.img = img;
    }

    public VIPMedia getVipmedia() {
        return vipmedia;
    }

    public void setVipmedia(VIPMedia vipmedia) {
        this.vipmedia = vipmedia;
    }

    /**
     * 一个像素(int)占四个byte
     * 
     * @return
     */
    public int getImgSize() {
        if (img != null) {
            return img.getWidth() * img.getHeight() * 4;
        }
        return 0;
    }

    @Override
    public boolean equals(Object o) {
        if (o.getClass() == this.getClass())
            return this.vipmedia.path
                    .equals(((VIPMediaBitmap) o).getVipmedia().path);
        return false;
    }
}
