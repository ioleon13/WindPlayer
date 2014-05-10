package com.leonlee.windplayer.adapter;

import java.util.List;

import com.leonlee.windplayer.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

public class NavigationAdapter extends ArrayAdapter<String> {
    
    private Context mContext;
    private int mRid;

    public NavigationAdapter(Context context, int resource, String[] objects) {
        super(context, resource, objects);
        mContext = context;
        mRid = resource;
    }
    
    /**
     * save view holder
     */
    static class ViewHolder {
        ImageView image;
        TextView title;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final String content = getItem(position);
        ViewHolder holder;
        
        if (convertView == null) {
            final LayoutInflater mInflator = LayoutInflater.from(mContext);
            convertView = mInflator.inflate(mRid, null);
            holder = new ViewHolder();
            holder.image = (ImageView) convertView.findViewById(R.id.content_icon);
            holder.title = (TextView) convertView.findViewById(R.id.content_text);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder)convertView.getTag();
        }
        
        return convertView;
    }

}
