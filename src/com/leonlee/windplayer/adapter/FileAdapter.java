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
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

public class FileAdapter extends ArrayAdapter<PFile> {
    private Context mContext;
    private int mRid;
    private boolean mIsSelectMode = false;
    
    private String TAG = "FileAdapter";

    public FileAdapter(Context context, int id, ArrayList<PFile> list) {
        super(context, id, list);
        mRid = id;
        mContext = context;
    }
    
    public void setSelectMode(boolean isSelectMode) {
        mIsSelectMode = isSelectMode;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final PFile f = getItem(position);
        
        if (convertView == null) {
            final LayoutInflater mInflater = LayoutInflater.from(mContext);
            convertView = mInflater.inflate(mRid, null);
        }
        
        if (f.is_audio) {
            ((ImageView)convertView.findViewById(R.id.thumbnail))
                .setImageResource(R.drawable.default_thumbnail_music);
        } else {
            if (f.thumb != null) {
                /*Bitmap bitmap = ThumbnailUtils.createVideoThumbnail(getActivity(),
                        f.thumb, Video.Thumbnails.MICRO_KIND);*/
                try {
                    File fileThumb = new File(f.thumb);
                    if (fileThumb.exists() && fileThumb.canRead()) {
                        ((ImageView)convertView.findViewById(R.id.thumbnail))
                            .setImageURI(Uri.parse(f.thumb));
                    } else {
                        Log.i(TAG, "thumbnail file: " + f.thumb + " is not exist or not readable");
                        ((ImageView)convertView.findViewById(R.id.thumbnail))
                            .setImageResource(R.drawable.default_thumbnail);
                    }
                } catch (Exception e) {
                    Log.e(TAG, "file io exception");
                }
                
            } else {
                ((ImageView)convertView.findViewById(R.id.thumbnail))
                    .setImageResource(R.drawable.default_thumbnail);
            }
            
        }
        
        ((TextView)convertView.findViewById(R.id.title)).setText(f.title);
        
        //show file size
        String fileSize = FileUtils.showFileSize(f.file_size);
        fileSize += "   " + f.resolution;
        ((TextView)convertView.findViewById(R.id.file_size)).setText(fileSize);
        
        //show file duration
        String duration = DateUtils.formatElapsedTime(f.duration);
        ((TextView)convertView.findViewById(R.id.file_duration)).setText(duration);
        
        //show checkbox
        if (mIsSelectMode) {
            ((CheckBox)convertView.findViewById(R.id.select_check)).setVisibility(View.VISIBLE);
            //((CheckBox)convertView.findViewById(R.id.select_check)).setChecked(checked);
        }
        return convertView;
    }
}
