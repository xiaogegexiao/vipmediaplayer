package com.mx.vipmediaplayer.util;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.mx.vipmediaplayer.Logger;
import com.mx.vipmediaplayer.VIPMediaPlayerApplication;
import com.mx.vipmediaplayer.model.VIPMedia;
import com.mx.vipmediaplayer.model.VIPMediaBitmap;

/**
 * Image Buffer class
 * 
 * @author Xiao Mei
 * @weibo http://weibo.com/u/1675796095
 * @email tss_chs@126.com
 * 
 */

public class ImageBuffer {
//  private static Context context;
    private static File bufferFolder = new File(VIPMediaPlayerApplication.VIPPLAYER_VIDEO_THUMB);

    public final static int MaxBufferSize = (int) (5 * 1024 * 1024);
    private static List<File> bufferImgs;
    private static int curBufferSize;

    public final static int MaxMemorySize = (int) (10 * 1024 * 1024);
    private static List<VIPMediaBitmap> memoryImgs;
    public static int curMemorySize;

    static {
        initBuffer();
    }

    private static void initBuffer() {
        bufferImgs = new ArrayList<File>();
        File[] imgs = bufferFolder.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String filename) {
                if (filename.endsWith(".jpg") || filename.endsWith(".jpeg")
                        || filename.endsWith(".gif")
                        || filename.endsWith(".png")
                        || filename.endsWith(".bmp"))
                    return true;
                return false;
            }
        });
        curBufferSize = 0;
        for (File f : imgs) {
            bufferImgs.add(f);
            curBufferSize += f.length();
        }
        Collections.sort(bufferImgs, new FileTimeComparator());
        memoryImgs = new LinkedList<VIPMediaBitmap>();
        curMemorySize = 0;
    }

    /**
     * add a file to buffer
     */
    private static void addFile(File file) {
        bufferImgs.add(file);
        curBufferSize += file.length();
        if (curBufferSize > MaxBufferSize)
            deleteFileByTime();
    }

    /**
     * add a bitmap to memory
     */
    private static void addThumBitmap(VIPMediaBitmap ub) {
        memoryImgs.add(ub);
        curMemorySize += ub.getImgSize();
        if (curMemorySize > MaxMemorySize)
            deleteHalfMemoryImg();
    }

    /**
     * delete half files in buffer
     */
    private static void deleteFileByTime() {
        int halfCount = bufferImgs.size() / 2;
        for (int i = 0; i < halfCount; i++) {
            curBufferSize -= bufferImgs.get(0).length();
            bufferImgs.get(0).delete();
            bufferImgs.remove(0);
        }
    }

    /**
     * delete half reference in memory
     */
    public static void deleteHalfMemoryImg() {
        int halfCount = memoryImgs.size() / 2;
        for (int i = 0; i < halfCount; i++) {
            curMemorySize -= memoryImgs.get(0).getImgSize();
            VIPMediaBitmap u = memoryImgs.remove(0);
            u.getImg().recycle();
        }
    }

    private static Object readLock = new Object();

    private static final int MaxSingleFileSize = 400 * 1024;
    
    private static final int MaxSingleGIFFileSize = 2 * 1024 * 1024;

    /***
     * first try to read url from memory. if not exists then try to read it from
     * buffer. if still not exists then return null.
     */
    public static Bitmap readImg(VIPMedia media) {
        if (media.path == null || media.path.length() == 0)
            return null;
        try {
            synchronized (readLock) {
                VIPMediaBitmap ub = readImgFromMem(media.thumb_path);
                if (ub != null) {
                    Logger.d("Read from memory: " + media.thumb_path);
                    return ub.getImg();
                }
                File file = new File(media.thumb_path);
                if (!file.exists())
                    return null;
                if (file.length() > MaxSingleFileSize
                        && !media.thumb_path.toLowerCase().endsWith("gif")
                        || file.length() > MaxSingleGIFFileSize) {
                    Logger.d("File cannot be loaded, file size: "
                                    + file.length() + ", file thumpath:" + media.thumb_path);
                    return null;
                }
                Bitmap bt = BitmapFactory.decodeFile(media.thumb_path);
                // Bitmap bt = BitmapFactory.decodeFile(pathName);

                if (bt != null) {
                    if (bt.getWidth() <= 200 && bt.getHeight() <= 200) {
                        addThumBitmap(new VIPMediaBitmap(bt, media));
                        Logger.d("Read from file: " + media.thumb_path);
                    }
                } else {
                    deleteFileFromBuffer(media.thumb_path);
                    throw new Exception("Cannot decode " + media.thumb_path);
                }
                return bt;
            }
        } catch (OutOfMemoryError err) {
            Logger.d(err.getMessage());
        } catch (Exception e) {
            Logger.d("Error in reading " + media.thumb_path + ", Error: "
                            + e.getClass().toString() + " " + e.getMessage());
        }
        return null;
    }

    /**
     * try to read img from memory
     */
    private static VIPMediaBitmap readImgFromMem(String thumpath) {
        VIPMediaBitmap res = null;
        for (VIPMediaBitmap ub : memoryImgs)
            if (ub.getVipmedia().thumb_path.equals(thumpath))
                res = ub;
        if (res != null) {
            memoryImgs.remove(res);
            memoryImgs.add(res);
        }
        return res;
    }

    /**
     * read img async, try to download it if the img doesn't exist in local.
     */
//  public static void readBitmapAsync(String url,
//          MethodHandler<UrlBitmap> handler) {
//      Bitmap bt = readImg(url);
//      if (bt == null) {
//          LoadImgThread thread = new LoadImgThread(url, handler);
//          thread.start();
//      } else
//          handler.process(new UrlBitmap(bt, url));
//  }

    public static void deleteBitmap(String thumpath) {
        deleteFileFromMemory(thumpath);
        deleteFileFromBuffer(thumpath);
        File file = new File(thumpath);
        file.delete();
    }

    private static void deleteFileFromMemory(String thumpath) {
        for (VIPMediaBitmap ub : memoryImgs) {
            if (ub.getVipmedia().thumb_path.equals(thumpath)) {
                memoryImgs.remove(ub);
                curMemorySize -= ub.getImgSize();
                ub.getImg().recycle();
                break;
            }
        }
    }

    /**
     * delete specified file by url.
     * 
     * @param url
     */
    private static void deleteFileFromBuffer(String url) {
        String name = getNameFromUrl(url);
        File file = null;
        for (File f : bufferImgs)
            if (f.getName().equals(name)) {
                file = f;
                break;
            }
        if (file != null) {
            bufferImgs.remove(file);
            curBufferSize -= file.length();
            file.delete();
        }
    }

    /**
     * read img async, try to download it if the img doesn't exist in local.
     */
    // public static void readSameBitmapAsync(String url,
    // MethodHandler<UrlBitmap> handler) {
    // Bitmap bt = readImg(url);
    // if (bt == null) {
    // LoadSameImgThread thread = new LoadSameImgThread(url, handler);
    // ThreadPool.execute(thread);
    // } else
    // handler.process(new UrlBitmap(bt, url));
    // }

    private static Pattern FileNamePattern = Pattern.compile("[^\\d\\w\\._]+");

    private static String getNameFromUrl(String url) {
        String res = url;
        Matcher m = FileNamePattern.matcher(res);
        res = m.replaceAll("_");
        return res;
    }

    public static class FileTimeComparator implements Comparator<File> {

        public int compare(File object1, File object2) {
            long i1 = object1.lastModified();
            long i2 = object2.lastModified();
            if(i1 > i2)
                return -1;
            else if(i1 < i2)
                return 1;
            return 0;
        }
    }
}
