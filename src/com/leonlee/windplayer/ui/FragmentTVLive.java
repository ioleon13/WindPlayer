package com.leonlee.windplayer.ui;

import java.util.ArrayList;

import com.leonlee.windplayer.R;
import com.leonlee.windplayer.adapter.ChannelListAdapter;
import com.leonlee.windplayer.adapter.TVListAdapter;
import com.leonlee.windplayer.po.OnlineVideo;
import com.leonlee.windplayer.util.XmlReaderHelper;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.ListView;

@SuppressLint("ValidFragment")
public class FragmentTVLive extends FragmentBase implements OnItemClickListener {
    private GridView mGridView;
    private ListView mListView;
    
    private String TAG = "FragmentTVLive";
    
    private TVListAdapter mAdapter;
    private ChannelListAdapter mChannelAdapter;
    
    private ArrayList<OnlineVideo> mTVList;
    
    //action bar
    private ActionBar mActionBar;
    private ActionBarDrawerToggle mDrawerToggle;
    
    private int mLevel = 1;
    
    public static final String DISPLAY_NAME = "display_name";
    public static final String IS_STREAM = "is_stream";
    private final static int REQUEST_COMPLETE = 1;
    
    public FragmentTVLive() {
        super();
    }

    public FragmentTVLive(ActionBarDrawerToggle mDrawerToggle) {
        super();
        this.mDrawerToggle = mDrawerToggle;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.gridview_tv_live, container, false);
        
        mGridView = (GridView) v.findViewById(R.id.tv_gridview);
        
        mListView = (ListView) v.findViewById(android.R.id.list);
        mListView.setOnItemClickListener(this);
        
        mTVList = XmlReaderHelper.getAllCategory(getActivity());
        mAdapter = new TVListAdapter(getActivity(), R.layout.gridview_tv_item, mTVList);
        mGridView.setAdapter(mAdapter);
        mGridView.setOnItemClickListener(this);
        
        mActionBar = getActivity().getActionBar();
        return v;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        switch (mLevel) {
        case 1:
            mLevel = 2;
            Log.i(TAG, "level: 1 -> 2");
            final OnlineVideo itemCategory = mAdapter.getItem(position);
            if (mChannelAdapter != null)
                mChannelAdapter.clear();
            
            mChannelAdapter = new ChannelListAdapter(getActivity(),
                    R.layout.fragment_online_item,
                    XmlReaderHelper.getVideoUrls(getActivity(), itemCategory.id));
            
            mActionBar.setTitle(itemCategory.title);
            setToggleIconEnabled(false);
            
            mListView.setAdapter(mChannelAdapter);
            mGridView.setVisibility(View.GONE);
            mListView.setVisibility(View.VISIBLE);
            break;
            
        case 2:
            mLevel = 3;
            Log.i(TAG, "level: 2 -> 3");
            final OnlineVideo itemChannel = mChannelAdapter.getItem(position);
            Intent intent = new Intent(getActivity(), WindPlayerActivity.class);
            intent.setData(Uri.parse(itemChannel.url));
            intent.putExtra(DISPLAY_NAME, itemChannel.title);
            intent.putExtra(IS_STREAM, true);
            startActivityForResult(intent, REQUEST_COMPLETE);
            break;

        default:
            break;
        }
        
    }
    
    @Override
    public boolean onBackPressed() {
        switch (mLevel) {
        case 1:
            return super.onBackPressed();
            
        case 2:
            mLevel = 1;
            Log.i(TAG, "onBackPressed 2 -> 1");
            mActionBar.setTitle(R.string.title_tv);
            setToggleIconEnabled(true);
            mListView.setVisibility(View.GONE);
            mGridView.setVisibility(View.VISIBLE);
            break;
            
        case 3:
            mLevel = 2;
            Log.i(TAG, "onBackPressed 3 -> 2");
            break;

        default:
            break;
        }
        
        return true;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
        case REQUEST_COMPLETE:
            if (data != null) {
                Bundle bundle = data.getExtras();
                if (bundle != null) {
                    Boolean complete = bundle.getBoolean("Complete", false);
                    if (complete && mLevel == 3) {
                        Log.i(TAG, "play complete. 3 --> 2");
                        mLevel = 2;
                    }
                } else {
                Log.e(TAG, "getExtras is null");
                }
            }
            break;

        default:
            break;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case android.R.id.home:
            // The user clicked on the Messaging icon in the action bar. Take them back from
            // wherever they came from
            getActivity().onBackPressed();
            return true;
        }
        return false;
    }

    private void setToggleIconEnabled(boolean enable) {
        if (mDrawerToggle != null) {
            mDrawerToggle.setDrawerIndicatorEnabled(enable);
        }
    }
}
