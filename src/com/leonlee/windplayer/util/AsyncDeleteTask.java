package com.leonlee.windplayer.util;

import java.io.File;
import java.util.ArrayList;

import com.leonlee.windplayer.R;
import com.leonlee.windplayer.adapter.MultiSelectFileAdapter;
import com.leonlee.windplayer.business.MediaBusiness;
import com.leonlee.windplayer.po.PFile;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.os.AsyncTask;
import android.util.Log;

public class AsyncDeleteTask extends AsyncTask<Object, Integer, Void> {
    private String TAG = "AsyncDeleteTask";
    
    private Context mContext;
    private ArrayList<PFile> mDeleteList;
    private ArrayList<PFile> mFileArray;
    private int mTotalCount;
    private ProgressDialog mProgressDialog;
    private boolean mIsStop = false;
    
    private OnDeletedListener mOnDeletedListener;
    
    public AsyncDeleteTask(Context ctx, ArrayList<PFile> delList, ArrayList<PFile> allList) {
        mContext = ctx;
        mDeleteList = delList;
        mTotalCount = delList.size();
        mFileArray = allList;
        
        if (mProgressDialog == null) {
            mProgressDialog = createProgressDialog();
        }
    }
    
    public void setOnDeletedListener(OnDeletedListener listener) {
        mOnDeletedListener = listener;
    }
    
    private ProgressDialog createProgressDialog() {
        ProgressDialog dialog = new ProgressDialog(mContext);
        dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        dialog.setTitle(mContext.getResources().getString(R.string.file_delete));
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(true);
        dialog.setOnCancelListener(new OnCancelListener() {
            
            @Override
            public void onCancel(DialogInterface dialog) {
                mIsStop = true;
            }
        });
        dialog.setMax(mTotalCount);
        return dialog;
    }

    @Override
    protected void onPreExecute() {
        Log.i(TAG, "AsyncDeleteTask start...");
        if (mProgressDialog != null) {
            mProgressDialog.show();
        }
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        int progress = values[0];
        mProgressDialog.setProgress(progress);
    }

    @Override
    protected Void doInBackground(Object... arg0) {
        int currentCount = 0;
        
        for (int i = 0; i < mTotalCount; ++i) {
            currentCount++;
            PFile f = mDeleteList.get(i);
            String filePath = f.path;
            File file = new File(filePath);
            if (file.canRead() && file.exists()) {
                file.delete();
            } else {
                Log.e(TAG, "File was not readable or not exist: " + filePath);
                continue;
            }
            
            //FileBusiness.deleteFile(getActivity(), f);
            MediaBusiness.deleteFile(mContext, f);
            
            if (mOnDeletedListener != null)
                mOnDeletedListener.onDeleted(this, f);
            
            publishProgress(currentCount);
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void result) {
        Log.i(TAG, "AsyncDeleteTask end...");
        
        mProgressDialog.dismiss();
    }

    public interface OnDeletedListener {
        void onDeleted(AsyncDeleteTask task, PFile f);
    }
}
