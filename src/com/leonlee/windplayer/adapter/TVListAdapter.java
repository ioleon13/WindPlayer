package com.leonlee.windplayer.adapter;

import java.util.List;

import com.leonlee.windplayer.R;
import com.leonlee.windplayer.adapter.NavigationAdapter.ViewHolder;
import com.leonlee.windplayer.po.OnlineVideo;

import android.content.Context;
import android.content.res.TypedArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class TVListAdapter extends ArrayAdapter<OnlineVideo> {
    
    private Context mContext;
    private int mRid;
    
    private TypedArray mImgs;
    private TypedArray mColors;
    
    public TVListAdapter(Context context, int resource, List<OnlineVideo> objects) {
        super(context, resource, objects);
        mContext = context;
        mRid = resource;
        mImgs = context.getResources().obtainTypedArray(R.array.tv_category_img_list);
        mColors = context.getResources().obtainTypedArray(R.array.tv_category_color_list);
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
        final OnlineVideo item = getItem(position);
        ViewHolder holder;
        
        if (convertView == null) {
            final LayoutInflater mInflator = LayoutInflater.from(mContext);
            convertView = mInflator.inflate(mRid, null);
            holder = new ViewHolder();
            holder.image = (ImageView) convertView.findViewById(R.id.tv_image_item);
            holder.title = (TextView) convertView.findViewById(R.id.tv_title_item);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder)convertView.getTag();
        }
        
        holder.title.setText(item.title);
        
        int nImgIndex = position % mImgs.length();
        int nColorIndex = position % mColors.length();
        holder.image.setImageDrawable(mImgs.getDrawable(nImgIndex));
        convertView.setBackgroundColor(mColors.getColor(nColorIndex, 0));
        
        return convertView;
    }
    
    
}
