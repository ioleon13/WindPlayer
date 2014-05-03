package com.leonlee.windplayer.ui;

import java.lang.reflect.Field;

import io.vov.vitamio.LibsChecker;

import com.leonlee.windplayer.R;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewConfiguration;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.RadioButton;

public class MainFragmentActivity extends FragmentActivity implements OnClickListener{
	private ViewPager mPager;
	private RadioButton mRadioFile;
	private RadioButton mRadioOnline;
	//private ActionBar mActionBar;
	
	private String TAG = "MainFragmentActivity";
	
	//navigation drawer
	private String[] mContentList;
	private DrawerLayout mDrawerLayout;
	private ListView mDrawerList;

	@SuppressLint("NewApi")
    @Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		if (!LibsChecker.checkVitamioLibs(this)) {
            Log.e(TAG, "checkVitamioLibs is failed");
            return;
        }
		
		setContentView(R.layout.fragment_pager);
		mPager = (ViewPager)findViewById(R.id.pager);
		mRadioFile = (RadioButton)findViewById(R.id.radio_file);
		mRadioOnline = (RadioButton)findViewById(R.id.radio_online);
		
		mRadioFile.setOnClickListener(this);
		mRadioOnline.setOnClickListener(this);
		mPager.setOnPageChangeListener(mPagerListener);
		
		mPager.setAdapter(mAdapter);
		
		//navigation drawer
		mContentList = getResources().getStringArray(R.array.content_list);
		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		mDrawerList = (ListView) findViewById(R.id.left_drawer);
		mDrawerList.setAdapter(new ArrayAdapter<String>(this,
		        R.layout.drawer_list_item, mContentList));
		
		//forceShowOverflowMenu();
		
		/*mActionBar = getActionBar();
		mActionBar.setTitle(R.string.title_file);
		mActionBar.setDisplayHomeAsUpEnabled(true);
		mActionBar.show();*/
	}

    //@Override
    /*public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_action_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }*/
    
    /**
     * force show overflow menu
     */
    /*private void forceShowOverflowMenu() {
        try {
            ViewConfiguration config = ViewConfiguration.get(this);
            Field menuKeyField = ViewConfiguration.class.getDeclaredField("sHasPermanentMenuKey");
            if (menuKeyField != null) {
                menuKeyField.setAccessible(true);
                menuKeyField.setBoolean(config, false);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }*/

    private FragmentPagerAdapter mAdapter = new FragmentPagerAdapter(getSupportFragmentManager()) {
		
		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return 2;
		}
		
		@Override
		public Fragment getItem(int position) {
			Fragment result = null;
			switch (position) {
			case 1:
				// online video
				result = new FragmentOnline();
				break;
			case 0:
			default:
				// local video
				result = new FragmentFile();
				break;
			}
			return result;
		}
	};
	
	private ViewPager.SimpleOnPageChangeListener mPagerListener = 
	        new ViewPager.SimpleOnPageChangeListener() {

                @Override
                public void onPageSelected(int position) {
                    switch (position) {
                    case 0:
                        //mActionBar.setTitle(R.string.title_file);
                        mRadioFile.setChecked(true);
                        break;
                    case 1:
                        //mActionBar.setTitle(R.string.title_online);
                        mRadioOnline.setChecked(true);
                        break;

                    default:
                        break;
                    }
                }
	    
	};

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
        case R.id.radio_file:
            //mActionBar.setTitle(R.string.title_file);
            mPager.setCurrentItem(0);
            break;
            
        case R.id.radio_online:
            //mActionBar.setTitle(R.string.title_online);
            mPager.setCurrentItem(1);
            break;

        default:
            break;
        }
    }

    @Override
    public void onBackPressed() {
        if (getFragmentByPosition(mPager.getCurrentItem()).onBackPressed())
            return;
        else
            super.onBackPressed();
    }
    
    /**
     * find fragment
     */
    private FragmentBase getFragmentByPosition(int position) {
        return (FragmentBase) getSupportFragmentManager().findFragmentByTag("android:switcher:"
                + mPager.getId() + ":" + position);
    }
}
