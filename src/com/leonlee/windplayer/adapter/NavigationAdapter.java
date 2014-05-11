package com.leonlee.windplayer.adapter;

import com.leonlee.windplayer.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
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
        
        holder.title.setText(content);
        switch (position) {
        case 0:
            holder.image.setImageResource(R.drawable.drawer_local_file);
            break;
            
        case 1:
            holder.image.setImageResource(R.drawable.drawer_tv);
            break;
            
        case 2:
            holder.image.setImageResource(R.drawable.drawer_vod);
            break;
            
        case 3:
            holder.image.setImageResource(R.drawable.drawer_setting);
            break;

        default:
            break;
        }
        
        return convertView;
    }

}
