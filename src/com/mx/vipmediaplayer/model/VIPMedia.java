package com.mx.vipmediaplayer.model;

import java.io.File;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * VIP video class
 * 
 * @author Xiao Mei
 * @weibo http://weibo.com/u/1675796095
 * @email tss_chs@126.com
 * 
 */
@DatabaseTable(tableName = "media")
public class VIPMedia {
    @DatabaseField(generatedId = true)
    public long _id;
    /** Video title */
    @DatabaseField
    public String title;
    /** Video title pinyin */
    @DatabaseField
    public String title_key;
    /** Video path */
    @DatabaseField
    public String path;
    /** Last time for accessing */
    @DatabaseField
    public long last_access_time;
    /** Last time for modifying */
    @DatabaseField
    public long last_modify_time;
    /** Video duration */
    @DatabaseField
    public int duration;
    /** Video current position */
    @DatabaseField
    public int position;
    /** Video thumb path */
    @DatabaseField
    public String thumb_path;
    /** Video file size */
    @DatabaseField
    public long file_size;
    /** Video width */
    @DatabaseField
    public int width;
    /** Video height */
    @DatabaseField
    public int height;
    /** 0 as local video, 1 as online video*/
    @DatabaseField
    public int filetype;
    /** MIME type */
    public String mime_type;

    /** 文件状态0 - 10 分别代表 下载 0-100% */
    public int status = -1;
    /** 文件临时大小 用于下载 */
    public long temp_file_size = -1L;

    public VIPMedia() {

    }

    public VIPMedia(File f, int type) {
        title = f.getName();
        path = f.getAbsolutePath();
        last_modify_time = f.lastModified();
        file_size = f.length();
        this.filetype = type;
    }

    public VIPMedia(String path, String mimeType, int type) {
        this(new File(path), type);
        this.mime_type = mimeType;
    }
}
