package com.leonlee.windplayer.ui;

import java.io.File;
import java.util.ArrayList;

import com.leonlee.windplayer.R;
import com.leonlee.windplayer.adapter.FileAdapter;
import com.leonlee.windplayer.business.MediaBusiness;
import com.leonlee.windplayer.po.PFile;
import com.leonlee.windplayer.provider.SuggestionProvider;
import com.leonlee.windplayer.util.FileUtils;

import android.app.ActionBar;
import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.SearchRecentSuggestions;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class SearchActivity extends Activity implements OnItemClickListener {
    private String TAG = "SearchActivity";
    
    private FileAdapter mAdapter;
    
    private ListView mListView;
    
    private ArrayList<PFile> mFileArray;
    
    private ArrayList<PFile> queryList;
    
    protected View mLoadingLayout;
    
    private int mSearchCount = 0;
    
    private String mSearchString;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        
        //get search string
        String strSearchParameter = getIntent().getStringExtra(SearchManager.QUERY);
        mSearchString = strSearchParameter != null ? strSearchParameter.trim() : strSearchParameter;
                
        setContentView(R.layout.search_result_list);
        mListView = (ListView) findViewById(R.id.search_result_list);
        mListView.setOnItemClickListener(this);
        mListView.setItemsCanFocus(true);
        mListView.setFocusable(true);
        mListView.setClickable(true);
        
        mLoadingLayout = (View) findViewById(R.id.loading);
        
        setTitle("");
        
        //start search task async
        new SearchTask().execute(mSearchString);
        
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case android.R.id.home:
            // The user clicked on the Messaging icon in the action bar. Take them back from
            // wherever they came from
            finish();
            return true;
        }
        return false;
    }
    
    //Search task
    private class SearchTask extends AsyncTask<String, Void, ArrayList<PFile>> {

        @Override
        protected ArrayList<PFile> doInBackground(String... params) {
            queryList = MediaBusiness.SearchByString(
                    getApplicationContext(), params[0], FragmentFile.getFileArray());
            mSearchCount = queryList.size();
            return null;
        }

        @Override
        protected void onPostExecute(ArrayList<PFile> result) {
            // TODO Auto-generated method stub
            super.onPostExecute(result);
            setTitle(getString(R.string.search_result_title, mSearchCount));
            showList(queryList);
            
            // ListView seems to want to reject the setFocusable until such time
            // as the list is not empty.  Set it here and request focus.  Without
            // this the arrow keys (and trackball) fail to move the selection.
            mListView.setFocusable(true);
            mListView.setFocusableInTouchMode(true);
            mListView.requestFocus();

            // Remember the query if there are actual results
            if (mSearchCount > 0) {
                SearchRecentSuggestions recent = new SearchRecentSuggestions(
                        getApplicationContext(), SuggestionProvider.AUTHORITY, SuggestionProvider.MODE);
                if (recent != null) {
                    Log.d(TAG, "save query string: "+ mSearchString + ", line 2: " +
                            getString(R.string.search_history, mSearchCount));
                    recent.saveRecentQuery(
                            mSearchString,
                            getString(R.string.search_history, mSearchCount));
                }
            }
        }

        @Override
        protected void onPreExecute() {
            // TODO Auto-generated method stub
            super.onPreExecute();
            mLoadingLayout.setVisibility(View.VISIBLE);
            mListView.setVisibility(View.GONE);
        }
        
    }
    
    private void showList(ArrayList<PFile> fileList) {
        mAdapter = new FileAdapter(getApplicationContext(), R.layout.fragment_file_item, fileList);
        mListView.setAdapter(mAdapter);
        
        mLoadingLayout.setVisibility(View.GONE);
        mListView.setVisibility(View.VISIBLE);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        final PFile f = mAdapter.getItem(position);
        Intent intent = new Intent(getApplicationContext(), WindPlayerActivity.class);
        //intent.putExtra("path", f.path);
        intent.setData(Uri.parse(f.path));
        startActivity(intent);
    }
}
