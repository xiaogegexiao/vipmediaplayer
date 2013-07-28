package com.mx.vipmediaplayer.model;

import java.util.ArrayList;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * Online video class
 * 
 * @author Xiao Mei
 * @weibo http://weibo.com/u/1675796095
 * 
 */

@DatabaseTable(tableName = "online_live")
public class OnlineVideo {
    @DatabaseField(generatedId = true)
	public String id;
	/** Title of online video */
    @DatabaseField
	public String title;
	/** description */
    @DatabaseField
	public String desc;
	/** LOGO */
    @DatabaseField
	public int iconId = 0;
    @DatabaseField
	public String icon_url;
	/** play url link */
    @DatabaseField
	public String url;
	/** backup url links */
	public ArrayList<String> backup_url;
	/** whether this is a category */
	@DatabaseField
	public boolean is_category = false;
	/** 0 indicates video 1 indicates online tv */
	@DatabaseField
	public int category;
	/** the level of current video */
	@DatabaseField
	public int level = 1;

	public OnlineVideo() {
	}
	
	public OnlineVideo(String title, int iconId, int category) {
		this.title = title;
		this.iconId = iconId;
		this.category = category;
	}

	public OnlineVideo(String title, int iconId, int category, String url) {
		this.title = title;
		this.iconId = iconId;
		this.category = category;
		this.url = url;
	}
}
