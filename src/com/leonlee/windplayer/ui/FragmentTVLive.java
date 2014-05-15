package com.leonlee.windplayer.ui;

import java.util.ArrayList;

import com.leonlee.windplayer.R;
import com.leonlee.windplayer.adapter.TVListAdapter;
import com.leonlee.windplayer.po.OnlineVideo;
import com.leonlee.windplayer.util.XmlReaderHelper;

import android.app.ActionBar;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;

public class FragmentTVLive extends FragmentBase {
    private GridView mGridView;
    
    private String TAG = "FragmentTVLive";
    
    private TVListAdapter mAdapter;
    
    private ArrayList<OnlineVideo> mTVList;
    
    //action bar
    private ActionBar mActionBar;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.gridview_tv_live, container, false);
        
        mGridView = (GridView) v.findViewById(R.id.tv_gridview);
        
        mTVList = XmlReaderHelper.getAllCategory(getActivity());
        mAdapter = new TVListAdapter(getActivity(), R.layout.gridview_tv_item, mTVList);
        mGridView.setAdapter(mAdapter);
        return v;
    }
    
    
}
