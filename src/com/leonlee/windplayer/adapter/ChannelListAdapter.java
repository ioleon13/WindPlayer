package com.leonlee.windplayer.adapter;

import java.util.List;

import com.leonlee.windplayer.R;
import com.leonlee.windplayer.adapter.TVListAdapter.ViewHolder;
import com.leonlee.windplayer.po.OnlineVideo;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class ChannelListAdapter extends ArrayAdapter<OnlineVideo> {
    
    private Context mContext;
    private int mRid;
    
    public ChannelListAdapter(Context context, int resource, List<OnlineVideo> objects) {
        super(context, resource, objects);
        
        mContext = context;
        mRid = resource;
    }
    
    /**
     * save view holder
     */
    static class ViewHolder {
        ImageView image_channel;
        TextView title;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final OnlineVideo item = getItem(position);
        ViewHolder holder;
        
        if (convertView == null) {
            final LayoutInflater mInflator = LayoutInflater.from(mContext);
            convertView = mInflator.inflate(mRid, null);
            holder = new ViewHolder();
            holder.image_channel = (ImageView) convertView.findViewById(R.id.thumbnail);
            holder.title = (TextView) convertView.findViewById(R.id.title);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder)convertView.getTag();
        }
        
        holder.title.setText(item.title);
        
        if (item.iconId > 0)
            holder.image_channel.setImageResource(item.iconId);
        else
            holder.image_channel.setImageDrawable(null);
        
        return convertView;
    }
    
    
}
