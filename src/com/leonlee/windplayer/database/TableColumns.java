package com.leonlee.windplayer.database;

public final class TableColumns {
	public static class FilesColumns {
		public static final String COL_ID = "id";
		/** 视频标题 */
		public static final String COL_TITLE = "title";
		/** 视频标题拼音 */
		public static final String COL_TITLE_PINYIN = "title_pinyin";
		/** 视频路径 */
		public static final String COL_PATH = "path";
		/** 最后一次访问时间 */
		public static final String COL_LAST_ACCESS_TIME = "last_access_time";
		/** 视频时长 */
		public static final String COL_DURATION = "duration";
		/** 视频播放进度 */
		public static final String COL_POSITION = "position";
		/** 视频缩略图 */
		public static final String COL_THUMB = "thumb";
		/** 是否纯音频 */
		public static final String COL_IS_AUDIO = "is_audio";
		/** 文件大小，byte*/
		public static final String COL_FILE_SIZE = "file_size";
		/** 视频宽度 */
		public static final String COL_WIDTH = "width";
		/** 视频高度 */
		public static final String COL_HEIGHT = "height";
	}
}
