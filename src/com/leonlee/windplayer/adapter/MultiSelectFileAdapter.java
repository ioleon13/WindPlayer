package com.leonlee.windplayer.adapter;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;

import com.leonlee.windplayer.R;
import com.leonlee.windplayer.po.PFile;

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

    public MultiSelectFileAdapter(Context context, int id, ArrayList<PFile> list) {
        super(context, id, list);
        // TODO Auto-generated constructor stub
    }
    
    public void setListView(ListView view) {
        mListView = view;
    }
    
    public void setFileArray(ArrayList<PFile> fileArray) {
        mFileArray = fileArray;
    }
    
    public boolean isSelectMode() {
        return mIsSelectMode;
    }
    
    /**
     * 
     */
    public void updateCheckState(View view, PFile f) {
        boolean isChecked = 
                ((CheckBox)view.findViewById(R.id.select_check)).isChecked();
        ((CheckBox)view.findViewById(R.id.select_check)).setChecked(!isChecked);
        notifyDataSetChanged();
        if (isSelectMode() && (view != null)) {
            if (f != null) {
                if (!isChecked) {
                    mSelectSet.add(f);
                } else {
                    mSelectSet.remove(f);
                }
                mSelectedCnt.setText(Integer.toString(mSelectSet.size()));
            }
        }
        updateSelectTitle();
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
        notifyDataSetChanged();
        updateSelectTitle();
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
}
