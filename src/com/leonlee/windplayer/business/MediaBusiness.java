package com.leonlee.windplayer.business;

import java.util.ArrayList;

import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;
import android.util.Log;

import com.leonlee.windplayer.po.PFile;
import com.leonlee.windplayer.util.PinyinUtils;

public class MediaBusiness {
    private static final String TAG = "MediaBusiness";
    
    /**
     * get sorted media file list
     */
    public static ArrayList<PFile> getAllSortFile (final Context ctx) {
        ArrayList<PFile> result = new ArrayList<PFile>();
        //Media columns
        String[] mediaColumns = new String[] {
                MediaStore.Video.Media._ID,
                MediaStore.Video.Media.TITLE,
                MediaStore.Video.Media.DISPLAY_NAME,
                MediaStore.Video.Media.DATA,
                MediaStore.Video.Media.DATE_TAKEN,
                MediaStore.Video.Media.DATE_MODIFIED,
                MediaStore.Video.Media.DURATION,
                MediaStore.Video.Media.SIZE,
                MediaStore.Video.Media.MIME_TYPE,
                MediaStore.Video.Media.RESOLUTION
        };
        
        //thumbnail columns
        String[] thumbColumns = new String[] {
                MediaStore.Video.Thumbnails.DATA,
                MediaStore.Video.Thumbnails.VIDEO_ID
        };
        
        //get all video file
        ContentResolver resolver = ctx.getContentResolver();
        //sort
        String sortOrder = MediaStore.Video.Media.DISPLAY_NAME + " asc";
        Cursor cur = resolver.query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                mediaColumns, null, null, sortOrder);
        if (cur.moveToFirst()) {
            do {
                PFile pf = new PFile();
                pf._id = cur.getLong(cur.getColumnIndexOrThrow(MediaStore.Video.Media._ID));
                pf.title = cur.getString(cur.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME));
                try {
                    pf.title_pinyin = PinyinUtils.chineneToSpell(pf.title);
                } catch(BadHanyuPinyinOutputFormatCombination e) {
                    e.printStackTrace();
                }
                
                pf.path = cur.getString(cur.getColumnIndexOrThrow(MediaStore.Video.Media.DATA));
                pf.added_time =
                        cur.getLong(cur.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_TAKEN));
                pf.last_access_time =
                        cur.getLong(cur.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_MODIFIED)) * 1000;
                long duration = cur.getLong(cur.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION));
                pf.duration = (int) (duration / 1000);
                pf.file_size = cur.getLong(cur.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE));
                pf.mime_type = cur.getString(cur.getColumnIndexOrThrow(MediaStore.Video.Media.MIME_TYPE));
                pf.resolution = cur.getString(cur.getColumnIndexOrThrow(MediaStore.Video.Media.RESOLUTION));
                pf.is_audio = false;
                
                //query thumbnail path
                String selection = MediaStore.Video.Thumbnails.VIDEO_ID + "=?";
                String[] selectionArgs = new String[] {
                        pf._id+""
                };
                Cursor curThumb = resolver.query(MediaStore.Video.Thumbnails.EXTERNAL_CONTENT_URI,
                        thumbColumns, selection, selectionArgs, null);
                if (curThumb.moveToFirst()) {
                    pf.thumb = curThumb.getString(curThumb.getColumnIndexOrThrow(
                            MediaStore.Video.Thumbnails.DATA));
                }
                
                //close cursor
                curThumb.close();
                
                result.add(pf);
            } while (cur.moveToNext());
        }
        
        //close cursor
        cur.close();
        
        return result;
    }
    
    /**
     * rename file name in media store
     */
    public static void renameFile(final Context ctx, final PFile pf) {
        ContentResolver resolver = ctx.getContentResolver();
        ContentValues values = new ContentValues();
        String where = MediaStore.Video.Media._ID + "=?";
        String[] selectionArgs = new String[] {
                pf._id+""
        };
        values.put(MediaStore.Video.Media.DISPLAY_NAME, pf.title);
        values.put(MediaStore.Video.Media.DATA, pf.path);
        resolver.update(MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                values, where, selectionArgs);
    }
    
    /**
     * delete the file in media store
     */
    public static void deleteFile(final Context ctx, final PFile pf) {
        ContentResolver resolver = ctx.getContentResolver();
        String where = MediaStore.Video.Media._ID + "=?";
        String[] selectionArgs = new String[] {
                pf._id+""
        };
        resolver.delete(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, where, selectionArgs);
    }
    
    public static void deleteFile(final Context ctx, final int id) {
        ContentResolver resolver = ctx.getContentResolver();
        String where = MediaStore.Video.Media._ID + "=?";
        String[] selectionArgs = new String[] {
                id+""
        };
        resolver.delete(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, where, selectionArgs);
    }
    
    /**
     * query the file list
     */
    public static ArrayList<PFile> SearchByString(final Context ctx, final String str,
            final ArrayList<PFile> fileArray) {
        ArrayList<PFile> result = new ArrayList<PFile>();
        for (PFile pf : fileArray) {
            if (pf.title.toLowerCase().contains(str.toLowerCase()))
                result.add(pf);
        }
        return result;
    }
}
