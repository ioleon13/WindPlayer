package com.leonlee.windplayer.util;

import java.util.ArrayList;

import com.leonlee.windplayer.R;
import com.leonlee.windplayer.po.PFile;

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
    private int mTotalCount;
    private ProgressDialog mProgressDialog;
    private boolean mIsStop = false;
    
    public AsyncDeleteTask(Context ctx, ArrayList<PFile> list) {
        mContext = ctx;
        mDeleteList = list;
        mTotalCount = list.size();
        
        if (mProgressDialog == null) {
            createProgressDialog();
        }
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
        // TODO Auto-generated method stub
        return null;
    }

}
