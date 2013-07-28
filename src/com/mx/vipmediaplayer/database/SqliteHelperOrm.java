package com.mx.vipmediaplayer.database;


import java.sql.SQLException;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import com.mx.vipmediaplayer.Logger;
import com.mx.vipmediaplayer.VIPMediaPlayerApplication;
import com.mx.vipmediaplayer.model.VIPMedia;

/**
 * Sql Helper class
 * 
 * @author Xiao Mei
 * @weibo http://weibo.com/u/1675796095
 * @email tss_chs@126.com
 * 
 */
public class SqliteHelperOrm extends OrmLiteSqliteOpenHelper {
    private static final String DATABASE_NAME = "vipplayer.db";
    private static final int DATABASE_VERSION = 1;

    public SqliteHelperOrm(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public SqliteHelperOrm() {
        super(VIPMediaPlayerApplication.getContext(), DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db, ConnectionSource connectionSource) {
        try {
            Logger.d("database on create =================================================== ");
            TableUtils.createTable(connectionSource, VIPMedia.class);
        } catch (SQLException e) {
            Logger.e(e);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, ConnectionSource connectionSource, int arg2, int arg3) {
        try {
            TableUtils.dropTable(connectionSource, VIPMedia.class, true);
            onCreate(db, connectionSource);
        } catch (SQLException e) {
            Logger.e(e);
        }
    }
}