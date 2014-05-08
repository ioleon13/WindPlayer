package com.leonlee.windplayer.ui;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;

import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;

import com.leonlee.windplayer.R;
import com.leonlee.windplayer.adapter.FileAdapter;
import com.leonlee.windplayer.adapter.MultiSelectFileAdapter;
import com.leonlee.windplayer.business.FileBusiness;
import com.leonlee.windplayer.business.MediaBusiness;
import com.leonlee.windplayer.database.SQLiteHelper;
import com.leonlee.windplayer.database.TableColumns.FilesColumns;
import com.leonlee.windplayer.po.PFile;
import com.leonlee.windplayer.provider.SuggestionProvider;
import com.leonlee.windplayer.util.FileUtils;
import com.leonlee.windplayer.util.PinyinUtils;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.SearchRecentSuggestions;
import android.text.InputFilter;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.SearchView.OnQueryTextListener;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Toast;

public class FragmentFile extends FragmentBase implements OnItemClickListener {
    private String TAG = "FragmentFile";
	private MultiSelectFileAdapter mAdapter;
	private TextView first_letter_overlay;
	private ImageView alphabet_scroller;
	
	//show sdcard available size
	private TextView mSDCardAvailable;
	
	//hide the position char
	private boolean hidden = true;
	private Handler handler;
	private Runnable startHidingRunnable;
	
	//action bar
	private ActionBar mActionBar;
	private SearchView mSearchView;
	private MenuItem mSearchItem;
	
	private static ArrayList<PFile> mFileArray;
	
	public static ArrayList<PFile> getFileArray() {
	    return mFileArray;
	}
	
	private ArrayList<PFile> queryList;

	//click to play
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		final PFile f = mAdapter.getItem(position);
		
		if (mAdapter.isSelectMode()) {
		    mAdapter.updateCheckState(view, f, position);
		} else {
		    Intent intent = new Intent(getActivity(), WindPlayerActivity.class);
		    //intent.putExtra("path", f.path);
		    intent.setData(Uri.parse(f.path));
		    startActivity(intent);
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = super.onCreateView(inflater, container, savedInstanceState);
		
		//action bar
		forceShowOverflowMenu();
		mActionBar = getActivity().getActionBar();
		if (mActionBar != null) {
		    mActionBar.setTitle(R.string.title_file);
	        //mActionBar.setDisplayHomeAsUpEnabled(true);
	        mActionBar.show();
		}
		
		first_letter_overlay = (TextView) v.findViewById(R.id.first_letter_overlay);
		alphabet_scroller = (ImageView) v.findViewById(R.id.alphabet_scroller);
		alphabet_scroller.setClickable(true);
		alphabet_scroller.setOnTouchListener(asOnTouch);
		mListView.setOnItemClickListener(this);
		mListView.setOnCreateContextMenuListener(OnListViewMenu);
		mSDCardAvailable = (TextView)v.findViewById(R.id.sd_block);
		
		//load the data
		/*if (new SQLiteHelper(getActivity()).isEmpty())
		    new ScanVideoTask().execute();
		else
		    new DataTask().execute();*/
		new DataTask().execute();
		
		//start hiding handler
		handler = new Handler();
		startHidingRunnable = new Runnable() {
            
            @Override
            public void run() {
                startHiding();
            }
        };
        
        //save search suggestion
        //saveSearchSuggestion();
        
		return v;
	}
	
	@Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        //inflater.inflate(R.menu.main_action_menu, menu);
        
        //Get the SearchView and set configuration
        mSearchItem = menu.findItem(R.id.action_search);
        SearchManager searchManager = (SearchManager) getActivity().getSystemService(Context.SEARCH_SERVICE);
        if (searchManager != null) {
            mSearchView = (SearchView) mSearchItem.getActionView();
            if (mSearchView != null) {
                mSearchView.setSearchableInfo(searchManager.getSearchableInfo(getActivity().getComponentName()));
                mSearchView.setIconifiedByDefault(false);
                mSearchView.setQueryHint(getActivity().getString(R.string.search_hint));
                mSearchView.setOnQueryTextListener(queryTextListener);
            } else {
                Log.e(TAG, "SearchView is null");
            }
        } else {
            Log.e(TAG, "searchManager is null");
        }
        
        //listen the collapse event
        /**MenuItem menuItem = menu.findItem(R.id.action_search);
        MenuItemCompat.setOnActionExpandListener(menuItem, new OnActionExpandListener() {
            
            @Override
            public boolean onMenuItemActionExpand(MenuItem arg0) {
                // TODO Auto-generated method stub
                return false;
            }
            
            @Override
            public boolean onMenuItemActionCollapse(MenuItem arg0) {
                Log.d(TAG, "search menu collapse, clean search");
                showList(mFileArray);
                return false;
            }
        });*/
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.action_delete:
            mAdapter.startSelectMode();
            break;

        default:
            return true;
        }
        
        return false;
    }
    

    /**
     * force show overflow menu
     */
    private void forceShowOverflowMenu() {
        try {
            ViewConfiguration config = ViewConfiguration.get(getActivity());
            Field menuKeyField = ViewConfiguration.class.getDeclaredField("sHasPermanentMenuKey");
            if (menuKeyField != null) {
                menuKeyField.setAccessible(true);
                menuKeyField.setBoolean(config, false);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     * save search suggestion
     */
    private void saveSearchSuggestion() {
        Intent intent = getActivity().getIntent();
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            Log.d(TAG, "save search string: " + query);
            SearchRecentSuggestions suggestions = new SearchRecentSuggestions(getActivity(),
                    SuggestionProvider.AUTHORITY, SuggestionProvider.MODE);
            suggestions.saveRecentQuery(query, null);
        }
    }
    
    OnQueryTextListener queryTextListener = new OnQueryTextListener() {
        
        @SuppressLint("NewApi")
        @Override
        public boolean onQueryTextSubmit(String query) {
            //TO DO: startactivity to show search result
            Log.d(TAG, "query string is: " + query + ", start a search activity");
            Intent intent = new Intent();
            intent.setClass(getActivity(), SearchActivity.class);
            intent.putExtra(SearchManager.QUERY, query);
            startActivity(intent);
            mSearchItem.collapseActionView();
            //new SearchTask().execute(query);
            return false;
        }
        
        @Override
        public boolean onQueryTextChange(String newText) {
            // TODO Auto-generated method stub
            return false;
        }
    };
	
	//hiding position char
	private void maybeStartHiding() {
	    cancelHiding();
	    handler.postDelayed(startHidingRunnable, 2500);
	}
	
	private void startHiding() {
	    alphabet_scroller.setPressed(false);
        first_letter_overlay.setVisibility(View.GONE);
	}
	
	private void cancelHiding() {
	    handler.removeCallbacks(startHidingRunnable);
	}
	
	//file operation menu
	ListView.OnCreateContextMenuListener OnListViewMenu = new ListView.OnCreateContextMenuListener() {

        @Override
        public void onCreateContextMenu(ContextMenu menu, View v,
                ContextMenuInfo menuInfo) {
            menu.setHeaderTitle(R.string.file_oper);
            menu.add(0, 0, 0, R.string.file_rename);
            menu.add(0, 1, 0, R.string.file_delete);
            menu.add(0, 2, 0, R.string.file_info);
        }
	    
	};
	
	@Override
    public boolean onContextItemSelected(MenuItem item) {
        ContextMenuInfo info = item.getMenuInfo();
        AdapterView.AdapterContextMenuInfo contextMenuInfo = (AdapterView.AdapterContextMenuInfo) info;
        int position = contextMenuInfo.position;
        switch (item.getItemId()) {
        case 0:
            renameFile(mAdapter, mAdapter.getItem(position), position);
            break;
            
        case 1:
            deleteFile(mAdapter, mAdapter.getItem(position), position);
            break;
            
        case 2:
            DetailsDialogView details = new DetailsDialogView(getActivity(),
                    mAdapter.getItem(position));
            details.show();
            break;

        default:
            break;
        }
        return super.onContextItemSelected(item);
    }
	
	//rename file
	private void renameFile(final FileAdapter adapter, final PFile f,
	        final int position) {
	    final EditText et = new EditText(getActivity());
	    et.setFilters(new InputFilter[] {new InputFilter.LengthFilter(100)});
	    et.setText(f.title);
	    new AlertDialog.Builder(getActivity())
	    .setTitle(R.string.file_rename)
	    .setIcon(android.R.drawable.ic_dialog_info)
	    .setView(et)
	    .setNegativeButton(android.R.string.yes,
	            new DialogInterface.OnClickListener() {
                    
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String name = et.getText().toString().trim();
                        if (name == null
                                || name.trim().equals("")
                                || name.trim().equals(f.title)) {
                            return;
                        }
                        
                        try {
                            File fromFile = new File(f.path);
                            File newFile = new File(fromFile.getParent(), name.trim());
                            if (newFile.exists()) {
                                Toast.makeText(getActivity(),
                                        R.string.file_rename_exists,
                                        Toast.LENGTH_LONG).show();
                            } else if(fromFile.renameTo(newFile)) {
                                f.title = name;
                                f.path = newFile.getPath();
                                //FileBusiness.renameFile(getActivity(), f);
                                MediaBusiness.renameFile(getActivity(), f);
                                adapter.notifyDataSetChanged();
                            }
                        } catch (SecurityException se) {
                            Toast.makeText(getActivity(),
                                    R.string.file_rename_failed,
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                }).setPositiveButton(android.R.string.no, null).show();
	}
	
	//delete file
	private void deleteFile(final FileAdapter adapter, final PFile f, final int position) {
	    new AlertDialog.Builder(getActivity())
	            .setIcon(android.R.drawable.ic_dialog_alert)
	            .setTitle(R.string.file_delete)
	            .setMessage(getString(R.string.file_delete_confirm, f.title))
	            .setNegativeButton(android.R.string.yes,
	                    new DialogInterface.OnClickListener() {
                            
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                try {
                                    File file = new File(f.path);
                                    if (file.canRead() && file.exists())
                                        file.delete();
                                    
                                    //FileBusiness.deleteFile(getActivity(), f);
                                    MediaBusiness.deleteFile(getActivity(), f);
                                    adapter.remove(f);
                                    adapter.notifyDataSetChanged();
                                } catch (Exception e) {
                                    
                                }
                            }
                        }).setPositiveButton(android.R.string.no, null).show();
	}
	
	private class DataTask extends AsyncTask<Void, Void, ArrayList<PFile>> {

		@Override
		protected ArrayList<PFile> doInBackground(Void... arg0) {
			//return FileBusiness.getAllSortFiles(getActivity());
		    mFileArray = MediaBusiness.getAllSortFile(getActivity());
		    return mFileArray;
		}

		@Override
		protected void onPostExecute(ArrayList<PFile> result) {
			super.onPostExecute(result);
			
			//mAdapter = new FileAdapter(getActivity(), FileBusiness.getAllSortFiles(getActivity()));
			showList(mFileArray);
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			mLoadingLayout.setVisibility(View.VISIBLE);
			mListView.setVisibility(View.GONE);
		}
		
	}
	
	//Search task
	private class SearchTask extends AsyncTask<String, Void, ArrayList<PFile>> {

        @Override
        protected ArrayList<PFile> doInBackground(String... params) {
            queryList = MediaBusiness.SearchByString(getActivity(), params[0], mFileArray);
            return null;
        }

        @Override
        protected void onPostExecute(ArrayList<PFile> result) {
            // TODO Auto-generated method stub
            super.onPostExecute(result);
            showList(queryList);
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
	    mAdapter = new MultiSelectFileAdapter(getActivity(), R.layout.fragment_file_item, fileList);
	    //mAdapter.setCheckClickListener(checkClickListener);
	    mAdapter.setListView(mListView);
        mListView.setAdapter(mAdapter);
        
        mLoadingLayout.setVisibility(View.GONE);
        mListView.setVisibility(View.VISIBLE);
	}
	
	//scan sdcard
	private class ScanVideoTask extends AsyncTask<Void, File, ArrayList<PFile>> {
	    private ProgressDialog progressDialog;
	    private ArrayList<File> files = new ArrayList<File>();

		@Override
		protected ArrayList<PFile> doInBackground(Void... params) {
			scanAllMedias(Environment.getExternalStorageDirectory());
			
			//write to database
			SQLiteHelper sqlite = new SQLiteHelper(getActivity());
			SQLiteDatabase db = sqlite.getWritableDatabase();
			try {
			    db.beginTransaction();
			    
			    SQLiteStatement stat = db.compileStatement("INSERT INTO files("
			            + FilesColumns.COL_TITLE + ","
			            + FilesColumns.COL_TITLE_PINYIN + ","
			            + FilesColumns.COL_PATH + ","
			            + FilesColumns.COL_LAST_ACCESS_TIME + ","
			            + FilesColumns.COL_IS_AUDIO + ","
			            + FilesColumns.COL_FILE_SIZE + ","
			            + FilesColumns.COL_THUMB + ","
			            + FilesColumns.COL_WIDTH + ","
			            + FilesColumns.COL_HEIGHT
			            + ") VALUES(?,?,?,?,?,?,?,?,?)");
			    Log.d(TAG, "files size=" + files.size());
			    for (File f : files) {		        
			        String name = FileUtils.getFileNameNoEx(f.getName());
			        int is_audio = FileUtils.isAudio(f) ? 1 : 0;
			        
			        /*PFile pf = new PFile();
			        if (is_audio != 1) {
			            pf = FileUtils.getThumbnailAndWH(getActivity(), f);
			        }*/
			        
			        Log.d(TAG, "prepare to execute. file name=" + name + ", file path=" + f.getPath());
			        int index = 1;
			        stat.bindString(index++, f.getName());
			        stat.bindString(index++, PinyinUtils.chineneToSpell(name));
			        stat.bindString(index++, f.getPath());
			        stat.bindLong(index++, System.currentTimeMillis());
			        stat.bindLong(index++, is_audio);
			        stat.bindLong(index++, f.length());
			        stat.bindString(index++, "");
			        stat.bindLong(index++, 540);
			        stat.bindLong(index++, 320);
			        stat.execute();
			    }
			    db.setTransactionSuccessful();
			} catch (BadHanyuPinyinOutputFormatCombination e) {
			    e.printStackTrace();
			} catch (Exception e) {
			    e.printStackTrace();
			} finally {
			    db.endTransaction();
			    db.close();
			}
			
			return FileBusiness.getAllSortFiles(getActivity());
		}

		@Override
		protected void onProgressUpdate(File... values) {
			File f = values[0];
			Log.d(TAG, "onProgressUpdate file info :" + f.getName());
			//files.add(f);
			progressDialog.setMessage(f.getName());
		}
		
		
		//scan all video file
		public void scanAllMedias(File f) {
			if (f != null && f.exists() && f.isDirectory()) {
				if (f.listFiles() != null) {
					for (File file : f.listFiles()) {
						if (file.isDirectory()) {
							scanAllMedias(file);
						} else if (file.exists() && file.canRead() && FileUtils.isVideoOrAudio(file)) {
						    Log.d(TAG, "scan a file: " + file.getName().toString());
						    files.add(file);
							publishProgress(file);
						}
					}
				} else {
				    Log.d(TAG, "has not files, the path is :" + f.getPath().toString());
				}
			} else {
			    Log.d(TAG, "did not exist or is not a directory, the path is: " + f.getPath().toString());
			}
		}

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(getActivity());
            progressDialog.setMessage(getString(R.string.scaninfo));
            progressDialog.show();
        }

        @Override
        protected void onPostExecute(ArrayList<PFile> result) {
            super.onPostExecute(result);
            mAdapter = new MultiSelectFileAdapter(getActivity(), R.layout.fragment_file_item, result);
            mListView.setAdapter(mAdapter);
            progressDialog.dismiss();
        }
	}
	
	/**
	 * show A-Z
	 */
	private OnTouchListener asOnTouch = new OnTouchListener() {
        
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                alphabet_scroller.setPressed(true);
                first_letter_overlay.setVisibility(View.VISIBLE);
                //To show the char
                showPositionChar(event.getY());
                hidden = false;
                Log.i(TAG, "set hidden to false");
                maybeStartHiding();
                break;
                
            case MotionEvent.ACTION_UP:
                /*alphabet_scroller.setPressed(false);
                first_letter_overlay.setVisibility(View.GONE);*/
                break;
                
            case MotionEvent.ACTION_MOVE:
                //To show the char
                showPositionChar(event.getY());
                maybeStartHiding();
                break;

            default:
                break;
            }
            return false;
        }
    };
    
    /**
     * show the char
     * 
     * @param y
     */
    private void showPositionChar(float y) {
        int height = alphabet_scroller.getHeight();
        float charHeight = height / 28.0f;
        char c = 'A';
        if (y < 0)
            y = 0;
        else if (y > height)
            y = height;
        
        int index = (int) (y/charHeight) - 1;
        if (index < 0)
            index = 0;
        else if (index > 25)
            index = 25;
        
        String key = String.valueOf((char) (c+index));
        first_letter_overlay.setText(key);
        
        int position = 0;
        if (index == 0)
            mListView.setSelection(0);
        else if (index ==25)
            mListView.setSelection(mAdapter.getCount() - 1);
        else {
            for (; position < mAdapter.getCount(); ++position) {
                if (mAdapter.getItem(position).title_pinyin.startsWith(key)) {
                    mListView.setSelection(position);
                    break;
                }
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        
        //show sdcard available size
        mSDCardAvailable.setText(FileUtils.showSizeAvailable());
    }

    @Override
    public void onPause() {
        boolean wasHidden = hidden;
        hidden = true;
        Log.i(TAG, "onPause() set hidden to true");
        if (wasHidden != hidden) {
            startHiding();
        }
        super.onPause();
    }

    @Override
    public void onDestroyView() {
        Log.i(TAG, "onDestroyView() set hidden to true");
        super.onDestroyView();
    }
}
