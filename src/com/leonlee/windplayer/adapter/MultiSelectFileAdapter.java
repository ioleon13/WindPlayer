package com.leonlee.windplayer.adapter;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.leonlee.windplayer.R;
import com.leonlee.windplayer.po.PFile;
import com.leonlee.windplayer.util.FileUtils;

public class MultiSelectFileAdapter extends FileAdapter {
    private String TAG = "MultiSelectFileAdapter";
    
    //action mode, select actionbar
    private MenuItem mSelectMenuItem;
    private ActionMode mActionMode;
    private View mSelectActionBarView;
    private TextView mSelectedCnt;
    private ArrayList<PFile> mSelectSet = new ArrayList<PFile>();
    private static boolean mIsSelectAll = false;
    
    private ListView mListView;
    
    private ArrayList<PFile> mFileArray;
    
    //save selected state
    private static HashMap<Integer, Boolean> isSelectedStates;
    private void initSelectedStates() {
        isSelectedStates = new HashMap<Integer, Boolean>();
        for (int i = 0; i < mFileArray.size(); ++i) {
            isSelectedStates.put(i, false);
        }
    }

    public MultiSelectFileAdapter(Context context, int id, ArrayList<PFile> list) {
        super(context, id, list);
        mFileArray = list;
        initSelectedStates();
    }
    
    public void setListView(ListView view) {
        mListView = view;
    }
    
    public boolean isSelectMode() {
        return mIsSelectMode;
    }
    
    /**
     * 
     */
    public void updateCheckState(View view, PFile f, int position) {
        if (view == null || f == null) {
            Log.e(TAG, "updateCheckState view or f was null");
            return;
        }
        boolean isChecked = 
                ((CheckBox)view.findViewById(R.id.select_check)).isChecked();
        ((CheckBox)view.findViewById(R.id.select_check)).setChecked(!isChecked);
        
        if (isChecked) {
            mSelectSet.remove(f);
            isSelectedStates.put(position, false);
        } else {
            mSelectSet.add(f);
            isSelectedStates.put(position, true);
        }
        
        mSelectedCnt.setText(Integer.toString(mSelectSet.size()));
        updateSelectTitle();
        notifyDataSetChanged();
    }

    /**
     * start multiselect delete actionmode
     */
    public void startSelectMode() {
        mActionMode = mListView.startActionMode(mCallback);
        setSelectMode(true);
    }
    
    /**
     * actionmode callback
     */
    private ActionMode.Callback mCallback = new ActionMode.Callback() {
        
        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            // TODO Auto-generated method stub
            return true;
        }
        
        @Override
        public void onDestroyActionMode(ActionMode mode) {
            Log.d(TAG, "click to exit action mode");
            mSelectSet.clear();
            mSelectActionBarView = null;
            mSelectedCnt = null;
            setSelectMode(false);
        }
        
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            MenuInflater inflater = ((Activity) mContext).getMenuInflater();
            inflater.inflate(R.menu.multi_select_menu, menu);
            mSelectMenuItem = menu.findItem(R.id.select_all);
            MenuItem item = menu.findItem(R.id.action_confirm);
            item.setTitle(mContext.getString(R.string.delete_confirm));
            mSelectSet.clear();
            
            //select actionbar view
            if (mSelectActionBarView == null) {
                mSelectActionBarView = LayoutInflater.from((Activity) mContext)
                        .inflate(R.layout.multi_select_actionbar, null);
                mSelectedCnt = (TextView)mSelectActionBarView.findViewById(R.id.selected_count);
                mSelectedCnt.setText(Integer.toString(mSelectSet.size()));
            }
            
            mode.setCustomView(mSelectActionBarView);
            return true;
        }
        
        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            switch (item.getItemId()) {
            case R.id.action_confirm:
                if (mSelectSet.size() > 0 && mSelectSet.size() == mFileArray.size()) {
                    mIsSelectAll = true;
                } else {
                    mIsSelectAll = false;
                }
                Log.d(TAG, "IsSelectAll=" + mIsSelectAll);
                
                //confirm delete
                confirmDelete(mSelectSet);
                mode.finish();
                break;
                
            case R.id.select_all:
                if (mSelectSet.size() > 0 && mSelectSet.size() == mFileArray.size()) {
                    unSelectAll();
                } else {
                    selectAll();
                }
                break;

            default:
                break;
            }
            return true;
        }
    };
    
    /**
     * confirm delete
     */
    private void confirmDelete(ArrayList<PFile> selectedList) {
        
    }
    
    /**
     * unselect all
     */
    private void unSelectAll() {
        mSelectSet.clear();
        mSelectedCnt.setText(Integer.toString(mSelectSet.size()));
        notifyDataSetChanged();
        updateSelectTitle();
    }
    
    /**
     * select all
     */
    private void selectAll() {
        int count = mFileArray.size();
        mSelectedCnt.setText(Integer.toString(count));
        mSelectSet = mFileArray;
        for (int i = 0; i < mFileArray.size(); ++i) {
            isSelectedStates.put(i, true);
        }
        
        updateSelectTitle();
        notifyDataSetChanged();
    }
    
    /**
     * update select title "Select All" <-> "UnSelect All"
     */
    public void updateSelectTitle() {
        if (isSelectMode() && mSelectMenuItem != null) {
            if (mSelectSet.size() > 0 && mSelectSet.size() == mFileArray.size()) {
                mSelectMenuItem.setTitle(mContext.getString(R.string.menu_select_none));
            } else {
                mSelectMenuItem.setTitle(mContext.getString(R.string.muti_select_all));
            }
        }
    }
    
    private void setSelectMode(boolean isSelectMode) {
        if (mIsSelectMode != isSelectMode) {
            mIsSelectMode = isSelectMode;
            Log.d(TAG, "adapter set select mode: " + isSelectMode);
            notifyDataSetChanged();
        }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final PFile f = getItem(position);
        final int itemId = position;
        
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
            CheckBox checkBox = (CheckBox)convertView.findViewById(R.id.select_check);
            checkBox.setVisibility(View.VISIBLE);
        
            /*checkBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
                
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    Log.d(TAG, "check box was clicked");
                    updateCheckBoxClick(isChecked, f, itemId);
                }
            });*/
            checkBox.setOnClickListener(new OnClickListener() {
                
                @Override
                public void onClick(View arg0) {
                    if (isSelectedStates.get(itemId)) {
                        mSelectSet.remove(f);
                        isSelectedStates.put(itemId, false);
                    } else {
                        mSelectSet.add(f);
                        isSelectedStates.put(itemId, true);
                    }
                    
                    mSelectedCnt.setText(Integer.toString(mSelectSet.size()));
                    updateSelectTitle();
                }
            });
            
            if (position < isSelectedStates.size()) {
                checkBox.setChecked(isSelectedStates.get(position));
            }
        } else {
            ((CheckBox)convertView.findViewById(R.id.select_check)).setVisibility(View.GONE);
        }
        return convertView;
    }
    
    private void updateCheckBoxClick(boolean isChecked, PFile f, int itemid) {
        if (isSelectMode()) {
            if (isChecked) {
                mSelectSet.add(f);
                isSelectedStates.put(itemid, true);
            } else {
                mSelectSet.remove(f);
                isSelectedStates.put(itemid, false);
            }
            mSelectedCnt.setText(Integer.toString(mSelectSet.size()));
        }
        updateSelectTitle();
    }
}
