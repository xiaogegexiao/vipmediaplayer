package com.mx.vipmediaplayer.database;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import android.content.ContentValues;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.UpdateBuilder;
import com.mx.vipmediaplayer.Logger;

/**
 * Database helper class
 * 
 * @author Xiao Mei
 * @weibo http://weibo.com/u/1675796095
 * @email tss_chs@126.com
 * 
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
public class DatabaseHelper<T> {

    /** 新增一条记录 */
    public int create(T po) {
        SqliteHelperOrm db = new SqliteHelperOrm();
        try {
            Dao dao = db.getDao(po.getClass());
            return dao.create(po);
        } catch (SQLException e) {
            Logger.e(e);
        } finally {
            if (db != null)
                db.close();
        }
        return -1;
    }

    public boolean exists(T po, Map<String, Object> where) {
        SqliteHelperOrm db = new SqliteHelperOrm();
        try {
            Dao dao = db.getDao(po.getClass());
            if (dao.queryForFieldValues(where).size() > 0) {
                return true;
            }
        } catch (SQLException e) {
            Logger.e(e);
        } finally {
            if (db != null)
                db.close();
        }
        return false;
    }

    public int createIfNotExists(T po, Map<String, Object> where) {
        SqliteHelperOrm db = new SqliteHelperOrm();
        try {
            Dao dao = db.getDao(po.getClass());
            if (dao.queryForFieldValues(where).size() < 1) {
                return dao.create(po);
            }
        } catch (SQLException e) {
            Logger.e(e);
        } finally {
            if (db != null)
                db.close();
        }
        return -1;
    }

    /** 查询一条记录 */
    public List<T> queryForEq(Class<T> c, String fieldName, Object value) {
        SqliteHelperOrm db = new SqliteHelperOrm();
        try {
            Dao dao = db.getDao(c);
            return dao.queryForEq(fieldName, value);
        } catch (SQLException e) {
            Logger.e(e);
        } finally {
            if (db != null)
                db.close();
        }
        return new ArrayList<T>();
    }

    /** 删除一条记录 */
    public int remove(T po) {
        SqliteHelperOrm db = new SqliteHelperOrm();
        try {
            Dao dao = db.getDao(po.getClass());
            return dao.delete(po);
        } catch (SQLException e) {
            Logger.e(e);
        } finally {
            if (db != null)
                db.close();
        }
        return -1;
    }

    /**
     * 根据特定条件更新特定字段
     * 
     * @param c
     * @param values
     * @param columnName where字段
     * @param value where值
     * @return
     */
    public int update(Class<T> c, ContentValues values, String columnName, Object value) {
        SqliteHelperOrm db = new SqliteHelperOrm();
        try {
            Dao dao = db.getDao(c);
            UpdateBuilder<T, Long> updateBuilder = dao.updateBuilder();
            updateBuilder.where().eq(columnName, value);
            for (String key : values.keySet()) {
                updateBuilder.updateColumnValue(key, values.get(key));
            }
            return updateBuilder.update();
        } catch (SQLException e) {
            Logger.e(e);
        } finally {
            if (db != null)
                db.close();
        }
        return -1;
    }

    /** 更新一条记录 */
    public int update(T po) {
        SqliteHelperOrm db = new SqliteHelperOrm();
        try {

            Dao dao = db.getDao(po.getClass());
            return dao.update(po);
        } catch (SQLException e) {
            Logger.e(e);
        } finally {
            if (db != null)
                db.close();
        }
        return -1;
    }

    /** 查询所有记录 */
    public List<T> queryForAll(Class<T> c) {
        SqliteHelperOrm db = new SqliteHelperOrm();
        try {
            Dao dao = db.getDao(c);
            return dao.queryForAll();
        } catch (SQLException e) {
            Logger.e(e);
        } finally {
            if (db != null)
                db.close();
        }
        return new ArrayList<T>();
    }
}
