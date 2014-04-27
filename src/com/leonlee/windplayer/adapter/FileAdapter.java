package com.leonlee.windplayer.adapter;

import java.io.File;
import java.util.ArrayList;

import com.leonlee.windplayer.R;
import com.leonlee.windplayer.po.PFile;
import com.leonlee.windplayer.util.FileUtils;

import android.content.Context;
import android.net.Uri;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

public class FileAdapter extends ArrayAdapter<PFile> {
    protected Context mContext;
    protected int mRid;
    protected boolean mIsSelectMode = false;
    private OnClickListener mCheckClickListener;
    
    private String TAG = "FileAdapter";

    public FileAdapter(Context context, int id, ArrayList<PFile> list) {
        super(context, id, list);
        mRid = id;
        mContext = context;
    }
    
    public void setCheckClickListener(OnClickListener listener) {
        mCheckClickListener = listener;
    }
}
