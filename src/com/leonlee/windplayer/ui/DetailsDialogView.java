package com.leonlee.windplayer.ui;

import java.util.ArrayList;
import java.util.Date;

import com.leonlee.windplayer.R;
import com.leonlee.windplayer.po.PFile;
import com.leonlee.windplayer.util.FileUtils;
import com.leonlee.windplayer.util.StringUtils;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class DetailsDialogView {
    private String TAG = "DetailsDialogView";
    
    private Dialog mDialog;
    private PFile mPFile = null;
    private Context mContext = null;
    private DetailsAdapter mAdapter;
    
    public DetailsDialogView(Context ctx, PFile p) {
        mContext = ctx;
        mPFile = p;
    }
    
    public void show() {
        setDetails();
        mDialog.show();
    }
    
    public void hide() {
        mDialog.hide();
    }
    
    private void setDetails() {
        if (mPFile != null && mContext != null) {
            mAdapter = new DetailsAdapter();
            
            ListView detailsview = (ListView) LayoutInflater.from(mContext)
                    .inflate(R.layout.details_list, null, false);
            detailsview.setAdapter(mAdapter);
            
            mDialog = new AlertDialog.Builder(mContext)
                .setView(detailsview)
                .setTitle(R.string.file_info)
                .setPositiveButton(R.string.file_info_close, new DialogInterface.OnClickListener() {
                    
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mDialog.dismiss();
                    }
                }).create();
        } else {
            Log.e(TAG, "mPFile is null or mContext is null");
        }
    }
    
    private class DetailsAdapter extends BaseAdapter {
        private final ArrayList<String> mItems;
        
        @SuppressWarnings("unused")
        public DetailsAdapter() {
            mItems = new ArrayList<String>(8);
            
            //title
            String valueTitle = mContext.getString(R.string.file_title);
            valueTitle += FileUtils.getFileNameNoEx(mPFile.title);
            mItems.add(valueTitle);
            
            //created time
            String valueTime = mContext.getString(R.string.file_create_time);
            valueTime += StringUtils.formateDate(new Date(mPFile.added_time),
                    "yyyy-MM-dd hh:mm:ss");
            mItems.add(valueTime);
            
            //modified time
            String valueTimeModify = mContext.getString(R.string.file_modify_time);
            valueTimeModify += StringUtils.formateDate(new Date(mPFile.last_access_time),
                    "yyyy-MM-dd hh:mm:ss");
            mItems.add(valueTimeModify);
            
            //type
            String valueType = mContext.getString(R.string.file_type);
            //valueType += mPFile.is_audio ? "Audio" : "Video";
            valueType += mPFile.mime_type;
            mItems.add(valueType);
            
            //resolution
            if (!mPFile.is_audio) {
                String valueResolution = mContext.getString(R.string.file_resolution);
                //valueResolution += String.format("%dx%d", mPFile.width, mPFile.height);
                valueResolution += mPFile.resolution;
                mItems.add(valueResolution);
            }
            
            //duration
            String valueDuration = mContext.getString(R.string.file_duration);
            long elapseTime = (long)mPFile.duration;
            Log.i(TAG, "duration:" + mPFile.duration + ", long duration: " + elapseTime);
            valueDuration += DateUtils.formatElapsedTime(elapseTime);
            mItems.add(valueDuration);
            
            //size
            String valueSize = mContext.getString(R.string.file_size);
            valueSize += FileUtils.showFileSize(mPFile.file_size);
            mItems.add(valueSize);
            
            //path
            String valuePath = mContext.getString(R.string.file_path);
            valuePath += mPFile.path;
            mItems.add(valuePath);
        }

        @Override
        public int getCount() {
            // TODO Auto-generated method stub
            return mItems.size();
        }

        @Override
        public Object getItem(int position) {
            // TODO Auto-generated method stub
            return mItems.get(position);
        }

        @Override
        public long getItemId(int position) {
            // TODO Auto-generated method stub
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            TextView tv;
            if (convertView == null) {
                tv = (TextView) LayoutInflater.from(mContext)
                        .inflate(R.layout.details_item, parent, false);
            } else {
                tv = (TextView) convertView;
            }
            
            tv.setText(mItems.get(position));
            return tv;
        }
    };
}
