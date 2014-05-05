package com.leonlee.windplayer.ui;

import java.lang.reflect.Field;

import io.vov.vitamio.LibsChecker;

import com.leonlee.windplayer.R;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.GravityCompat;
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
import android.widget.AdapterView;
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
	private ActionBarDrawerToggle mDrawerToggle;

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
		
		// set a custom shadow that overlays the main content when the drawer opens
		mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
		
		// set up the drawer's list view with items and click listener
		mDrawerList = (ListView) findViewById(R.id.left_drawer);
		mDrawerList.setAdapter(new ArrayAdapter<String>(this,
		        R.layout.drawer_list_item, mContentList));
		mDrawerList.setOnItemClickListener(new DrawerItemClickListener());
		
		// enable ActionBar app icon to behave as action to toggle nav drawer
		getActionBar().setDisplayHomeAsUpEnabled(true);
		getActionBar().setHomeButtonEnabled(true);
		
		mDrawerToggle = new ActionBarDrawerToggle(this,
		        mDrawerLayout,
		        R.drawable.ic_drawer,
		        R.string.drawer_open,
		        R.string.drawer_close) {
		    public void onDrawerClosed(View view) {
		        Log.e(TAG, "drawer closed");
                //getActionBar().setTitle(mTitle);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            public void onDrawerOpened(View drawerView) {
                Log.e(TAG, "drawer opened");
                //getActionBar().setTitle(mDrawerTitle);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
		};
		mDrawerLayout.setDrawerListener(mDrawerToggle);
	}
	
	@Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // TODO Auto-generated method stub
        return super.onPrepareOptionsMenu(menu);
    }

    /**
	 * The click listner for ListView in the navigation drawer
	 */
	private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            
        }
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
