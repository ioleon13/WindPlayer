package com.leonlee.windplayer.database;

import com.leonlee.windplayer.database.TableColumns.FilesColumns;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.webkit.WebChromeClient.CustomViewCallback;

public class SQLiteHelper extends SQLiteOpenHelper {
	/** the name of database */
	private final static String NAME = "windplayer.db";
	private final static int DATABASE_VERSION = 1;
	private final static String SQL_CREATE_FILES = "CREATE TABLE \"files\" (" + //
		FilesColumns.COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," + //
		FilesColumns.COL_TITLE + " TEXT," + //
	    FilesColumns.COL_TITLE_PINYIN + " TEXT," + //
	    FilesColumns.COL_PATH + " TEXT," + //
	    FilesColumns.COL_DURATION + " INTEGER," + //
	    FilesColumns.COL_POSITION + " TEXT," + //
	    FilesColumns.COL_LAST_ACCESS_TIME + " INTEGER," + //
	    FilesColumns.COL_THUMB + " TEXT," + //
	    FilesColumns.COL_IS_AUDIO + " INTEGER," + //
	    FilesColumns.COL_FILE_SIZE + " INTEGER," + //
	    FilesColumns.COL_WIDTH + " INTEGER," + //
	    FilesColumns.COL_HEIGHT + " INTEGER" + //
	    ")";
	
	private String TAG = "SQLiteHelper";
	
	public SQLiteHelper(Context context, String name, CursorFactory factory, int version) {
		super(context, name, factory, version);
	}
	
	public SQLiteHelper(Context context) {
		super(context, NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
	    Log.d(TAG, "create table sql:" + SQL_CREATE_FILES);
		db.execSQL(SQL_CREATE_FILES);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("drop table if exists files");
		onCreate(db);
	}

	public boolean isEmpty() {
		String count = exeQuery("SELECT COUNT(id) FROM files");
		return count == null || "0".equals(count);
	}
	
	/**
	 * update
	 * 
	 * @param table
	 * @param values
	 * @param whereClause
	 * @param whereArgs
	 * @return
	 */
	public boolean update(String table, ContentValues values, String whereClause, String... whereArgs) {
	    filterWhereArgs(whereArgs);
	    SQLiteDatabase db = getWritableDatabase();
	    final int affectedRows = db.update(table, values, whereClause, whereArgs);
	    closeDb(db, null);
	    return affectedRows > 0;
	}
	
	/**
     * insert
     * 
     * @param table
     * @param values
     * @return
     */
	public long insert(String table, ContentValues values) {
	    long result = 0L;
	    SQLiteDatabase db = getWritableDatabase();
	    result = db.insert(table, null, values);
	    closeDb(db, null);
	    return result;
	}
	
	/**
     * insert or update
     * 
     * @param table
     * @param values
     * @param whereClause
     * @param whereArgs
     * @return
     */
	public boolean insertOrUpdate(String table, ContentValues values, String whereClause, String... whereArgs) {
	    filterWhereArgs(whereArgs);
	    SQLiteDatabase db =  getReadableDatabase();
	    Cursor c = db.query(table, null, whereClause, whereArgs, null, null, null);
	    int count = c.getCount();
	    closeDb(db, c);
	    
	    if (count > 0) {
	        return update(table, values, whereClause, whereArgs);
	    } else {
	        return insert(table, values) > 0L;
	    }
	}
	
	/**
     * delete
     * 
     * @param table
     * @param whereClause
     * @param whereArgs
     * @return
     */
	public boolean delete(String table, String whereClause, String... whereArgs) {
	    boolean result = true;
	    filterWhereArgs(whereArgs);
	    SQLiteDatabase db = getWritableDatabase();
	    
	    try {
	        result = db.delete(table, whereClause, whereArgs) > 0;
	    } catch (SQLException e) {
	        result = false;
	    } catch (Exception e) {
	        result = false;
	    } finally {
	        closeDb(db, null);
	    }
	    return result;
	}
	
	/**
	 * query
	 * 
	 * @param sql
	 * @param whereArgs
	 * @return
	 */
	public String exeQuery(String sql, String... whereArgs) {
		String result = "";
		filterWhereArgs(whereArgs);
		SQLiteDatabase db = getWritableDatabase();
		Cursor c = db.rawQuery(sql, whereArgs);
		if (c.moveToNext()) {
			result = c.getString(0);
		}
		
		closeDb(db, c);
		return result;
	}
	
	/**
     * query first Nth records
     * 
     * @param columnCount
     * @param sql
     * @param whereArgs
     * @return
     */
	public String[] exeQuery(int columnCount, String sql, String... whereArgs) {
	    filterWhereArgs(whereArgs);
	    String[] result = new String[columnCount];
	    SQLiteDatabase db = getWritableDatabase();
	    Cursor c = db.rawQuery(sql, whereArgs);
	    if (c.moveToNext()) {
	        int dbCC = c.getColumnCount();
	        if (columnCount > dbCC)
	            columnCount = dbCC;
	        
	        int index = 0;
	        while (columnCount > index) {
	            result[index] = c.getString(index);
	            index++;
	        }
	    }
	    
	    closeDb(db, c);
	    return result;
	}
	
	/**
     * exist
     */
	public boolean exist(String sql, String... whereArgs) {
	    boolean result = false;
	    filterWhereArgs(whereArgs);
	    SQLiteDatabase db = getWritableDatabase();
	    Cursor c = db.rawQuery(sql, whereArgs);
	    if (c.moveToNext()) {
	        result = true;
	    }
	    closeDb(db, c);
	    return result;
	}
	
	/**
	 * filter whereArgs has null
	 * 
	 * @param whereArgs
	 * @return
	 */
	private void filterWhereArgs(String... whereArgs) {
		if (whereArgs != null && whereArgs.length > 0) {
			for (int i = 0, j = whereArgs.length; i < j; ++i) {
				if (whereArgs[i] == null)
					whereArgs[i] = "";
			}
		}
	}
	
	/**
	 * close database
	 */
	public void closeDb(SQLiteDatabase db, Cursor c) {
		if (c != null)
			c.close();
		if (db != null)
			db.close();
		
		close();
	}
}
