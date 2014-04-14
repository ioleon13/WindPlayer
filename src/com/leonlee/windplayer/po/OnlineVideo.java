package com.leonlee.windplayer.po;

import java.util.ArrayList;

public class OnlineVideo {
    public String id;
    public String title;
    public String desc;
    public int iconId = 0;
    public String icon_url;
    public String url;
    public ArrayList<String> backup_url;
    public boolean is_category = false;
    
    // 1-Ö±²¥£¬0-µã²¥
    public int category;
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
