package com.leonlee.windplayer.ui;

import java.util.ArrayList;

import com.leonlee.windplayer.R;
import com.leonlee.windplayer.po.OnlineVideo;
import com.leonlee.windplayer.util.FileUtils;
import com.leonlee.windplayer.util.XmlReaderHelper;

import android.R.animator;
import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnKeyListener;
import android.view.ViewGroup;
import android.webkit.WebSettings.PluginState;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class FragmentOnline extends FragmentBase implements OnItemClickListener {
    private WebView mWebView;
    private ListView mListView;
    
    private View mLoading;
    private TextView mUrl;
    
    /** tvs or videos*/
    private final static ArrayList<OnlineVideo> root = new ArrayList<OnlineVideo>();
    private final static ArrayList<OnlineVideo> rootCreate = new ArrayList<OnlineVideo>();
    private ArrayList<OnlineVideo> tvs;
    private final static ArrayList<OnlineVideo> videos = new ArrayList<OnlineVideo>();
    private int level = 1;
    private DataAdapter mAdapter;
    
    
    private String TAG = "FragmentOnline";
    
    private final static int REQUEST_COMPLETE = 1;
    
    public static final String DISPLAY_NAME = "display_name";
    public static final String IS_STREAM = "is_stream";
    
    //action bar
    private ActionBar mActionBar;
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_online, container, false);
        mListView = (ListView) v.findViewById(android.R.id.list);
        mWebView = (WebView) v.findViewById(R.id.webview);
        
        mLoading = v.findViewById(R.id.loading);
        mUrl = (TextView) v.findViewById(R.id.url);
        
        mListView.setOnItemClickListener(this);
        initWebView();
        mAdapter = new DataAdapter(getActivity());
        mListView.setAdapter(new DataAdapter(getActivity()));
        
        mActionBar = getActivity().getActionBar();

        return v;
    }
    
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
        case REQUEST_COMPLETE:
            if (data != null) {
                Bundle bundle = data.getExtras();
                if (bundle != null) {
                    Boolean complete = bundle.getBoolean("Complete", false);
                    if (complete && level == 4) {
                        Log.i(TAG, "play complete. 4 --> 3");
                        level = 3;
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

    private void initWebView() {
        mWebView.setScrollBarStyle(View.SCROLLBARS_OUTSIDE_OVERLAY);
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.getSettings().setPluginState(PluginState.ON);
        
        mWebView.setWebViewClient(new WebViewClient() {

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                mLoading.setVisibility(View.GONE);
                mUrl.setVisibility(View.GONE);
                mWebView.setVisibility(View.VISIBLE);
            };

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                Log.i(TAG, "url: " + url);
                if (FileUtils.isVideoOrAudio(url)) {
                    Intent intent = new Intent(getActivity(), WindPlayerActivity.class);
                    intent.putExtra("path", url);
                    startActivity(intent);
                    return true;
                }
                return false;
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                // TODO Auto-generated method stub
                super.onPageStarted(view, url, favicon);
                mUrl.setText(url);
                mUrl.setVisibility(View.VISIBLE);
                mLoading.setVisibility(View.VISIBLE);
            };
            
        });
        
        mWebView.setOnKeyListener(new OnKeyListener() {
            
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((keyCode == KeyEvent.KEYCODE_BACK) && mWebView != null && mWebView.canGoBack()) {
                    mWebView.goBack();
                    return true;
                }
                return false;
            }
        });
    }
    
    static {
        root.add(new OnlineVideo("电视直播", R.drawable.logo_cntv, 1));
        root.add(new OnlineVideo("视频网站", R.drawable.logo_youku, 0));
        
        rootCreate.add(new OnlineVideo("电视直播", R.drawable.logo_cntv, 1));
        rootCreate.add(new OnlineVideo("视频网站", R.drawable.logo_youku, 0));
        
        videos.add(new OnlineVideo("优酷视频", R.drawable.logo_youku, 0, "http://3g.youku.com"));
        
        videos.add(new OnlineVideo("搜狐视频", R.drawable.logo_sohu, 0, "http://m.tv.sohu.com"));
        
        videos.add(new OnlineVideo("乐视TV", R.drawable.logo_letv, 0, "http://m.letv.com"));
        
        videos.add(new OnlineVideo("爱奇艺", R.drawable.logo_iqiyi, 0, "http://3g.iqiyi.com"));
        
        videos.add(new OnlineVideo("PPTV", R.drawable.logo_pptv, 0, "http://m.pptv.com"));
        
        videos.add(new OnlineVideo("腾讯视频", R.drawable.logo_qq, 0, "http://3g.v.qq.com"));
        
        videos.add(new OnlineVideo("56.com", R.drawable.logo_56, 0, "http://m.56.com"));
        
        videos.add(new OnlineVideo("新浪视频", R.drawable.logo_sina, 0, "http://video.sina.com"));
        
        videos.add(new OnlineVideo("土豆视频", R.drawable.logo_tudou, 0, "http://m.tudou.com"));
    }
    
    private class DataAdapter extends ArrayAdapter<OnlineVideo> {
        private static final int mRid = R.layout.fragment_online_item;
        
        public DataAdapter(Context context) {
            super(context, mRid, rootCreate);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final OnlineVideo item = getItem(position);
            if (convertView == null) {
                final LayoutInflater mInflater = getActivity().getLayoutInflater();
                convertView = mInflater.inflate(R.layout.fragment_online_item, null);
            }
            
            ImageView thumbnail = (ImageView) convertView.findViewById(R.id.thumbnail);
            if (item.iconId > 0)
                thumbnail.setImageResource(item.iconId);
            else
                thumbnail.setImageDrawable(null);
            
            ((TextView) convertView.findViewById(R.id.title)).setText(item.title);
            
            return convertView;
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        final OnlineVideo item = mAdapter.getItem(position);
        
        switch (level) {
        case 1:         //root
            level = 2;
            Log.i(TAG, "level: 1 -> 2");
            if (position == 0) { // live tv
                if (tvs == null)
                    tvs = XmlReaderHelper.getAllCategory(getActivity());
                replaceArrayAdapter(tvs);
            } else {            // vod
                replaceArrayAdapter(videos);
            }
            mListView.setAdapter(mAdapter);
            break;
            
        case 2:         //next level
            level = 3;
            Log.i(TAG, "level: 2 -> 3");
            if (item.id != null) { //live tv
                replaceArrayAdapter(XmlReaderHelper.getVideoUrls(getActivity(), item.id));
                mListView.setAdapter(mAdapter);
            } else {
                clearAndLoad(item.url);
            }
            break;
            
        case 3:
            level = 4;
            Log.i(TAG, "level: 3 -> 4");
            Intent intent = new Intent(getActivity(), WindPlayerActivity.class);
            intent.setData(Uri.parse(item.url));
            intent.putExtra(DISPLAY_NAME, item.title);
            intent.putExtra(IS_STREAM, true);
            startActivityForResult(intent, REQUEST_COMPLETE);
            break;

        default:
            break;
        }
    }
    
    @Override
    public boolean onBackPressed() {
        switch (level) {
        case 1:
            return super.onBackPressed();
            
        case 2:
            level = 1;
            Log.i(TAG, "onBackPressed 2 -> 1, first item=" + root.get(0).title);
            replaceArrayAdapter(root);
            break;
            
        case 3:
            level = 2;
            Log.i(TAG, "onBackPressed goto live or vod  3 -> 2");
            if (mListView == null || mListView.getVisibility() == View.VISIBLE) {
                replaceArrayAdapter(tvs);
            } else {
                switchWebviewToListview();
            }
            break;
            
        case 4:
            level = 3;
            Log.i(TAG, "level:  4 -> 3");
            switchWebviewToListview();
            break;

        default:
            break;
        }
        
        mListView.setAdapter(mAdapter);
        return true;
    }
    
    private void replaceArrayAdapter(ArrayList<OnlineVideo> newlist) {
        if (newlist == null)
            newlist = new ArrayList<OnlineVideo>();
        
        mAdapter.clear();
        for (OnlineVideo ov : newlist) {
            mAdapter.add(ov);
        }
    }
    
    private void switchWebviewToListview() {
        mWebView.clearView();
        mUrl.setVisibility(View.GONE);
        mListView.setVisibility(View.VISIBLE);
        mWebView.setVisibility(View.GONE);
        mLoading.setVisibility(View.GONE);
    }

    private void clearAndLoad(String url) {
        mLoading.setVisibility(View.VISIBLE);
        mWebView.setVisibility(View.GONE);
        mListView.setVisibility(View.GONE);
        mWebView.clearView();
        mWebView.clearHistory();
        mWebView.loadUrl(url);
    }
}
