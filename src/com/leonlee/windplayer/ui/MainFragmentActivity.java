package com.leonlee.windplayer.ui;


import io.vov.vitamio.LibsChecker;

import com.leonlee.windplayer.R;
import com.leonlee.windplayer.adapter.NavigationAdapter;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.content.res.Configuration;
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
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewConfiguration;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.RadioButton;

public class MainFragmentActivity extends FragmentActivity{
	private ViewPager mPager;
	//private RadioButton mRadioFile;
	//private RadioButton mRadioOnline;
	//private ActionBar mActionBar;
	
	private String TAG = "MainFragmentActivity";
	
	//navigation drawer
	private String[] mContentList;
	private DrawerLayout mDrawerLayout;
	private ListView mDrawerList;
	private ActionBarDrawerToggle mDrawerToggle;
	private NavigationAdapter mNavAdapter;
	
	//action menu
	private Menu mActionMenu;
	
	private boolean mShowActionMenu = true;

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
		//mRadioFile = (RadioButton)findViewById(R.id.radio_file);
		//mRadioOnline = (RadioButton)findViewById(R.id.radio_online);
		
		//mRadioFile.setOnClickListener(this);
		//mRadioOnline.setOnClickListener(this);
		mPager.setOnPageChangeListener(mPagerListener);
		
		mPager.setAdapter(mAdapter);
		
		//navigation drawer
		mContentList = getResources().getStringArray(R.array.content_list);
		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		
		// set a custom shadow that overlays the main content when the drawer opens
		mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
		
		// set up the drawer's list view with items and click listener
		mDrawerList = (ListView) findViewById(R.id.left_drawer);
		mNavAdapter = new NavigationAdapter(getApplicationContext(),
		        R.layout.drawer_list_item,
		        mContentList);
		mDrawerList.setAdapter(mNavAdapter);
		mDrawerList.setOnItemClickListener(new DrawerItemClickListener());
		
		// enable ActionBar app icon to behave as action to toggle nav drawer
		getActionBar().setDisplayHomeAsUpEnabled(true);
		getActionBar().setHomeButtonEnabled(true);
		
		mDrawerToggle = new ActionBarDrawerToggle(
		        this,
		        mDrawerLayout,
		        R.drawable.ic_drawer,
		        R.string.drawer_open,
		        R.string.drawer_close) {
		    public void onDrawerClosed(View view) {
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            public void onDrawerOpened(View drawerView) {
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
		};
		mDrawerLayout.setDrawerListener(mDrawerToggle);
		
		if (arg0 == null) {
		    selectItem(0);
		}
	}
	
	@Override
    public boolean onPrepareOptionsMenu(Menu menu) {
	    boolean drawerOpen = mDrawerLayout.isDrawerOpen(mDrawerList);
	    
	    boolean showMenu = (!drawerOpen) && mShowActionMenu;
	    setActionMenuVisible(menu, showMenu);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // The action bar home/up action should open or close the drawer.
        // ActionBarDrawerToggle will take care of this.
        if (mDrawerToggle.onOptionsItemSelected(item)) {
           return true;
        }
        return super.onOptionsItemSelected(item);
    }
    
    /**
     * set action menu visibility
     */
    private void setActionMenuVisible(Menu menu, boolean visible) {
        if (menu != null) {
            menu.findItem(R.id.action_search).setVisible(visible);
            menu.findItem(R.id.action_delete).setVisible(visible);
            menu.findItem(R.id.action_camera).setVisible(visible);
        }
    }

    /**
	 * The click listner for ListView in the navigation drawer
	 */
	private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            selectItem(position);
        }
    }
	
	private void selectItem(int position) {
	    mPager.setCurrentItem(position);
	    
	    mDrawerList.setItemChecked(position, true);
	    mDrawerLayout.closeDrawer(mDrawerList);
	    getActionBar().setTitle(mContentList[position]);
	}

    //@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_action_menu, menu);
        
        mActionMenu = menu;
        return super.onCreateOptionsMenu(menu);
    }
    
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        
        // Pass any configuration change to the drawer toggls
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

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
			return 3;
		}
		
		@Override
		public Fragment getItem(int position) {
			Fragment result = null;
			switch (position) {
			case 2:
				// online video
				result = new FragmentOnline(mDrawerToggle);
				break;
				
			case 1:
			    result = new FragmentTVLive(mDrawerToggle);
			    break;
				
			case 0:
				// local video
				result = new FragmentFile();
				break;
				
			default:
                break;
			}
			return result;
		}
	};
	
	private ViewPager.SimpleOnPageChangeListener mPagerListener = 
	        new ViewPager.SimpleOnPageChangeListener() {

                @Override
                public void onPageSelected(int position) {
                    mDrawerList.setItemChecked(position, true);
                    getActionBar().setTitle(mContentList[position]);
                    
                    mShowActionMenu = (position == 0);
                    setActionMenuVisible(mActionMenu, mShowActionMenu);
                    /*switch (position) {
                    case 0:
                        getActionBar().setTitle(mContentList[position]);
                        //mRadioFile.setChecked(true);
                        break;
                    case 1:
                        //mActionBar.setTitle(R.string.title_online);
                        //mRadioOnline.setChecked(true);
                        break;

                    default:
                        break;
                    }*/
                }
	};

    //@Override
    /*public void onClick(View v) {
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
    }*/

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
