package com.leonlee.windplayer.po;

public final class PFile {
	public long _id;				//视频文件id
	public String title;			//视频文件标题
	public String title_pinyin;		//标题的拼音
	public String path;				//视频文件路径
	public long added_time;         //加入时间，单位：ms
	public long last_access_time;	//最后一次访问时间，单位：ms
	public int duration;			//视频长度，单位：s
	public int position;			//视频播放进度
	public String thumb;			//视频文件缩略图
	public boolean is_audio;        //是否为纯音频
	public long file_size;          //文件大小，Byte
	public String mime_type;        //视频类型
	public String resolution;       //视频分辨率，W x H
	public int width;               //视频宽度   
	public int height;              //视频高度
	public boolean is_favorite;     //是否加入收藏
}
