package com.leonlee.windplayer.adapter;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.Executors;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
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
import android.widget.Toast;

import com.leonlee.windplayer.R;
import com.leonlee.windplayer.po.PFile;
import com.leonlee.windplayer.util.AsyncDeleteTask;
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
    
    private final static int RUN_ASYNCTASK_NUM = 1;
    
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
        
        mSelectedCnt.setText(Integer.toString(getCheckedCount()));
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
    
    private int getCheckedCount() {
        int count = 0;
        for (int i = 0; i < mFileArray.size(); ++i) {
            if (isSelectedStates.get(i))
                count++;
        }
        return count;
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
            initSelectedStates();
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
                mSelectedCnt.setText(Integer.toString(getCheckedCount()));
            }
            
            mode.setCustomView(mSelectActionBarView);
            return true;
        }
        
        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            int checkedCount = getCheckedCount();
            
            switch (item.getItemId()) {
            case R.id.action_confirm:
                if (checkedCount > 0 && checkedCount == getCount()) {
                    mIsSelectAll = true;
                } else {
                    mIsSelectAll = false;
                }
                Log.d(TAG, "IsSelectAll=" + mIsSelectAll);
                
                //confirm delete
                confirmDelete(checkedCount);
                mode.finish();
                break;
                
            case R.id.select_all:
                if (checkedCount > 0 && checkedCount == getCount()) {
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
    private void confirmDelete(int checkedCount) {
        if (checkedCount > 0) {
            confirmDeleteDialog(checkedCount);
        } else {
            Toast.makeText(mContext, R.string.no_file_del, Toast.LENGTH_SHORT).show();
        }
    }
    
    private void confirmDeleteDialog(int checkedCount) {
        View contents = View.inflate(mContext, R.layout.delete_file_msg_dialog_view, null);
        TextView msg = (TextView) contents.findViewById(R.id.message);
        msg.setText(mContext.getString(R.string.delete_msg, checkedCount));
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle(R.string.confirm_dialog_title)
               .setIconAttribute(android.R.attr.alertDialogIcon)
               .setCancelable(true)
               .setPositiveButton(R.string.file_delete, new DialogInterface.OnClickListener() {
                
                   @Override
                   public void onClick(DialogInterface dialog, int which) {
                       AsyncDeleteTask task = new AsyncDeleteTask(mContext, mSelectSet, mFileArray);
                       
                       task.executeOnExecutor(Executors.newFixedThreadPool(RUN_ASYNCTASK_NUM), "");
                   }
               })
               .setNegativeButton(R.string.no, null)
               .setView(contents)
               .show();
    }
    
    /**
     * unselect all
     */
    private void unSelectAll() {
        initSelectedStates();
        mSelectedCnt.setText(Integer.toString(0));
        mSelectSet.clear();
        notifyDataSetChanged();
        updateSelectTitle();
    }
    
    /**
     * select all
     */
    private void selectAll() {
        mSelectedCnt.setText(Integer.toString(getCount()));
        
        mSelectSet.clear();
        for (int i = 0; i < mFileArray.size(); ++i) {
            isSelectedStates.put(i, true);
            mSelectSet.add(mFileArray.get(i));
        }
        
        updateSelectTitle();
        notifyDataSetChanged();
    }
    
    /**
     * update select title "Select All" <-> "UnSelect All"
     */
    public void updateSelectTitle() {
        if (isSelectMode() && mSelectMenuItem != null) {
            if (getCheckedCount() > 0 && getCheckedCount() == getCount()) {
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
    
    /**
     * save view holder
     */
    static class ViewHolder {
        ImageView image;
        TextView title;
        TextView file_size;
        TextView duration;
        CheckBox check_box;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final PFile f = getItem(position);
        final int itemId = position;
        ViewHolder holder;
        
        if (convertView == null) {
            final LayoutInflater mInflater = LayoutInflater.from(mContext);
            convertView = mInflater.inflate(mRid, null);
            holder = new ViewHolder();
            holder.image = (ImageView)convertView.findViewById(R.id.thumbnail);
            holder.title = (TextView)convertView.findViewById(R.id.title);
            holder.file_size = (TextView)convertView.findViewById(R.id.file_size);
            holder.duration = (TextView)convertView.findViewById(R.id.file_duration);
            holder.check_box = (CheckBox)convertView.findViewById(R.id.select_check);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        
        if (f.is_audio) {
            holder.image.setImageResource(R.drawable.default_thumbnail_music);
        } else {
            if (f.thumb != null) {
                /*Bitmap bitmap = ThumbnailUtils.createVideoThumbnail(getActivity(),
                        f.thumb, Video.Thumbnails.MICRO_KIND);*/
                try {
                    File fileThumb = new File(f.thumb);
                    if (fileThumb.exists() && fileThumb.canRead()) {
                        holder.image.setImageURI(Uri.parse(f.thumb));
                    } else {
                        Log.i(TAG, "thumbnail file: " + f.thumb + " is not exist or not readable");
                        holder.image.setImageResource(R.drawable.default_thumbnail);
                    }
                } catch (Exception e) {
                    Log.e(TAG, "file io exception");
                }
                
            } else {
                holder.image.setImageResource(R.drawable.default_thumbnail);
            }
            
        }
        
        holder.title.setText(f.title);
        
        //show file size
        String fileSize = FileUtils.showFileSize(f.file_size);
        fileSize += "   " + f.resolution;
        holder.file_size.setText(fileSize);
        
        //show file duration
        String duration = DateUtils.formatElapsedTime(f.duration);
        holder.duration.setText(duration);
        
        //show checkbox
        if (mIsSelectMode) {
            CheckBox checkBox = holder.check_box;
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
                    
                    mSelectedCnt.setText(Integer.toString(getCheckedCount()));
                    updateSelectTitle();
                }
            });
            
            if (position < isSelectedStates.size()) {
                checkBox.setChecked(isSelectedStates.get(position));
            }
        } else {
            holder.check_box.setVisibility(View.GONE);
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
            mSelectedCnt.setText(Integer.toString(getCheckedCount()));
        }
        updateSelectTitle();
    }
}
