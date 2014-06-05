package com.leonlee.windplayer.util;

import android.annotation.SuppressLint;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class StringUtils {
    private static final String DEFAULT_DATE_PATTERN = "yyyy-MM-dd";
    private static final String DEFAULT_DATETIME_PATTERN = "yyyy-MM-dd hh:mm:ss";
    private static final String DEFAULT_TIME_PATTERN = "kk:mm";
    public final static String EMPTY = "";
    
    /**
     * format date string
     */
    @SuppressLint("SimpleDateFormat")
    public static String formateDate(Date date, String pattern) {
        SimpleDateFormat format = new SimpleDateFormat(pattern);
        return format.format(date);
    }
    
    /**
     * format date string
     */
    public static String formatDate(Date date) {
        return formateDate(date, DEFAULT_DATE_PATTERN);
    }
    
    /**
     * get current date string
     */
    public static String getDateTime() {
        return formateDate(new Date(), DEFAULT_DATETIME_PATTERN);
    }
    
    /**
     * get current time string
     */
    public static String getCurrentTimeString() {
        return formateDate(new Date(), DEFAULT_TIME_PATTERN);
    }
    
    /**
     * formate date time string
     */
    public static String formatDateTime(Date date) {
        return formateDate(date, DEFAULT_DATETIME_PATTERN);
    }
    
    public static String join(final ArrayList<String> array, String separator) {
        StringBuffer result = new StringBuffer();
        if (array != null && array.size() > 0) {
            for (String str : array) {
                result.append(str);
                result.append(separator);
            }
            result.delete(result.length() - 1, result.length());
        }
        return result.toString();
    }
    
    public static boolean isEmpty(String str) {
        return str == null || str.length() == 0;
    }
    
    public static String generateTime(long time) {
        int totalSeconds = (int) (time / 1000);
        int seconds = totalSeconds % 60;
        int minutes = (totalSeconds / 60) % 60;
        int hours = totalSeconds / 3600;

        return hours > 0 ? String.format("%02d:%02d:%02d", hours, minutes, seconds) : String.format("%02d:%02d", minutes, seconds);
    }
}
