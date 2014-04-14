package com.leonlee.windplayer.ui;

import com.leonlee.windplayer.R;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

public class FragmentBase extends Fragment {
	protected ListView mListView;
	protected View mLoadingLayout;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_file, container, false);
		mListView = (ListView)v.findViewById(R.id.list);
		mLoadingLayout = v.findViewById(R.id.loading);
		return v;
	}
	
	public boolean onBackPressed() {
	    return false;
	}

}
