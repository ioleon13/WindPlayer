package com.leonlee.windplayer.ui;

import java.io.File;
import java.util.ArrayList;

import com.leonlee.windplayer.R;
import com.leonlee.windplayer.po.PFile;
import com.leonlee.windplayer.util.FileUtils;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class SearchActivity extends Activity {
    private String TAG = "SearchActivity";
    
    private FileAdapter mAdapter;
    
    private ListView mListView;
    
    private class FileAdapter extends ArrayAdapter<PFile> {
        private static final int mRid = R.layout.fragment_file_item;

        public FileAdapter(Context context, ArrayList<PFile> list) {
            super(context, mRid, list);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final PFile f = getItem(position);
            if (convertView == null) {
                final LayoutInflater mInflater = getLayoutInflater();
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
            ((TextView)convertView.findViewById(R.id.file_size)).setText(fileSize);
            return convertView;
        }
        
    }
}
