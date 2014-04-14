package com.leonlee.windplayer.business;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.leonlee.windplayer.database.SQLiteHelper;
import com.leonlee.windplayer.database.TableColumns.FilesColumns;
import com.leonlee.windplayer.po.PFile;

public final class FileBusiness {
    private static final String TABLE_NAME = "files";
    private static final String TAG = "FileBusiness";
    
	//get sorted files list
	public static ArrayList<PFile> getAllSortFiles (final Context ctx) {
		ArrayList<PFile> result = new ArrayList<PFile>();
		SQLiteHelper sqlite = new SQLiteHelper(ctx);
		SQLiteDatabase db = sqlite.getReadableDatabase();
		Cursor c = null;
		try {
		    c = db.rawQuery("SELECT " + FilesColumns.COL_ID + "," +
		            FilesColumns.COL_TITLE + "," +
		            FilesColumns.COL_TITLE_PINYIN + "," +
		            FilesColumns.COL_PATH + "," +
		            FilesColumns.COL_DURATION + "," +
		            FilesColumns.COL_POSITION + "," +
		            FilesColumns.COL_LAST_ACCESS_TIME + "," +
		            FilesColumns.COL_THUMB + "," +
		            FilesColumns.COL_IS_AUDIO + "," +
		            FilesColumns.COL_FILE_SIZE + " FROM files", null);
		    while (c.moveToNext()) {
		        PFile pf = new PFile();
		        int index = 0;
		        pf._id = c.getLong(index++);
		        pf.title = c.getString(index++);
		        pf.title_pinyin = c.getString(index++);
		        pf.path = c.getString(index++);
		        pf.duration = c.getInt(index++);
		        pf.position = c.getInt(index++);
		        pf.last_access_time = c.getLong(index++);
		        pf.thumb = c.getString(index++);
		        pf.is_audio = (c.getInt(index++) == 1);
		        pf.file_size = c.getLong(index++);
		        result.add(pf);
		    }
		} finally {
		    if (c != null)
		        c.close();
		}
		
		db.close();
		
		Collections.sort(result, new Comparator<PFile>() {

            @Override
            public int compare(PFile f1, PFile f2) {
                char c1 = f1.title_pinyin.charAt(0);
                char c2 = f2.title_pinyin.charAt(0);
                
                return c1 == c2 ? 0 : (c1 > c2 ? 1 : -1);
            }
        });
		return result;
	}
	
	//rename file name in db
	public static void renameFile(final Context ctx, final PFile p) {
	    SQLiteHelper sqlite = new SQLiteHelper(ctx);
	    SQLiteDatabase db = sqlite.getWritableDatabase();
	    try {
	        ContentValues values = new ContentValues();
	        values.put(FilesColumns.COL_TITLE, p.title);
	        values.put(FilesColumns.COL_TITLE_PINYIN, p.title_pinyin);
	        values.put(FilesColumns.COL_PATH, p.path);
	        db.update(TABLE_NAME, values, FilesColumns.COL_ID + " = ?",
	                new String[] {p._id + ""});
	    } catch (Exception e) {
	        e.printStackTrace();
	    } finally {
	        try {
	            db.close();
	        } catch (Exception e) {
	            
	        }
	    }
	}
	
	//delete the file in db
	public static int deleteFile(final Context ctx, final PFile p) {
	    SQLiteHelper sqlite = new SQLiteHelper(ctx);
        SQLiteDatabase db = sqlite.getWritableDatabase();
        
        int result = -1;
        try {
            result = db.delete(TABLE_NAME, FilesColumns.COL_ID + " = ?", new String[] {p._id + ""});
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                db.close();
            } catch (Exception e) {
                
            }
        }
        return result;
	}
}
