package com.leonlee.windplayer.ui;

import java.util.ArrayList;
import java.util.List;

import com.leonlee.windplayer.R;
import com.leonlee.windplayer.po.PFile;
import com.leonlee.windplayer.util.StringUtils;

import io.vov.vitamio.LibsChecker;
import io.vov.vitamio.MediaPlayer;
import io.vov.vitamio.MediaPlayer.OnCompletionListener;
import io.vov.vitamio.MediaPlayer.OnErrorListener;
import io.vov.vitamio.MediaPlayer.OnInfoListener;
import io.vov.vitamio.MediaPlayer.OnSeekCompleteListener;
import io.vov.vitamio.widget.MediaController;
import io.vov.vitamio.widget.VideoView;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.text.TextUtils;
import android.util.Log;
import android.view.Display;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

public class WindPlayerActivity extends Activity
    implements OnCompletionListener, OnInfoListener, OnErrorListener,
    ControllerOverlay.Listener, OnSeekCompleteListener{
    private String TAG = "WindPlayerActivity";
	private String path;
	private String title;
	private VideoView mVideoView;
	private View mVolumeBrightnessLayout;
	private ImageView mOperationBg;
	private ImageView mOperationPercent;
	private AudioManager mAudioManager;
	
	//the max volume
	private int mMaxVolume;
	
	//the current volume
	private int mVolume = -1;
	
	//the current brightness
	private float mBrightness = -1f;
	
	//the current zoom mode
	private int mLayout = VideoView.VIDEO_LAYOUT_ZOOM;
	
	private GestureDetector mGestureDetector;
	private MediaController mMediaController;
	
	private View mLoadingView;
	private TextView mLoadingText;
	
	private View mRootView;
	private MediaControllerOverlay mController;
	
	//is streaming
	private boolean mIsStreaming = false; 
	
	private boolean mFinishOnComplete = true;
	
	private int mLastSystemUiVis = 0;
	
	//seek bar was dragging
	private boolean mDragging = false;
	
	//seek bar was showing
	private boolean mShowing = false;
	
	private int mVideoPosition = 0;
	private int mBeforeSeekPosition = 0;
	
	private int mDuration = 0;
	
	private long mClickTime = 0;
	
	private boolean mIsLiveStream = false;
	private boolean mIsControlPaused = false;
	private boolean mIsStop = false;
	private boolean mHasPaused = false;
	
	private int mCurVideoIndex = 0;
	private boolean mIsChanged = false;
	
	//play list
	private ArrayList<PFile> mPlaylist;
	
	private boolean mIsFavorite = false;
	
	private enum SeekState {
	    SEEKFORWARD,
	    NOSEEK,
	    SEEKBACK
	}
	
	private SeekState mSeekState = SeekState.SEEKFORWARD;
	
	//handler and runable
	private Handler mHandler = new Handler();
	
	private final Runnable mProgressChecker = new Runnable() {
        
        @Override
        public void run() {
            int pos = setProgress();
            setCurrentTime();
            mHandler.postDelayed(mProgressChecker, 1000 - (pos % 1000));
        }
    };
    
    private final Runnable mPlayingChecker = new Runnable() {
        
        @Override
        public void run() {
            if (mVideoView != null && mVideoView.isPlaying()) {
                if (mIsLiveStream) {
                    mController.setLiveMode();
                }
                
                mController.showPlaying();
                mController.clearPlayState();
            } else if (mHasPaused) {
                if (mIsLiveStream) {
                    mController.setLiveMode();
                    mController.clearPlayState();
                }
                
                mController.showPaused();
            } else {
                mController.showLoading();
                mHandler.postDelayed(mPlayingChecker, 250);
            }
        }
    };
	
	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void setSystemUiVisibility(View rootView) {
	    rootView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
	            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
	            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_wind_player);
		mRootView = findViewById(R.id.wind_player_root);
		
		//path = getIntent().getStringExtra("path");
		Intent intent = getIntent();
		if (intent != null) {
		    path = intent.getData().toString();
		    mIsFavorite = intent.getBooleanExtra("favorite", false);
		    title = intent.getStringExtra(FragmentOnline.DISPLAY_NAME);
		    mIsStreaming = intent.getBooleanExtra(FragmentOnline.IS_STREAM, false);
		    mFinishOnComplete = intent.getBooleanExtra(MediaStore.EXTRA_FINISH_ON_COMPLETION, true);
		}
		
		if (TextUtils.isEmpty(path))
			path = Environment.getExternalStorageDirectory() + "DCIM/Camera/MOV041.mp4";
		
		if (TextUtils.isEmpty(title)) {
		    List<String> paths = Uri.parse(path).getPathSegments();
	        title = paths == null || paths.isEmpty() ? "null" : paths.get(paths.size() - 1);
		}
		
		mVideoView = (VideoView)findViewById(R.id.surface_view);
		mVolumeBrightnessLayout = findViewById(R.id.volume_operation_brightness);
		mOperationBg = (ImageView)findViewById(R.id.operation_bg);
		mOperationPercent = (ImageView)findViewById(R.id.operation_percent);
		mLoadingView = findViewById(R.id.video_loading);
		mLoadingText = (TextView)findViewById(R.id.video_loading_text);
		
		mAudioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
		mMaxVolume = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
		
		Log.d(TAG, "setVideoPath: " + path.toString());
		mVideoView.setVideoPath(path);
		mVideoView.setOnCompletionListener(this);
		mVideoView.setOnInfoListener(this);
		
		//media controller overlay
		mController = new MediaControllerOverlay(getApplicationContext());
		((ViewGroup)mRootView).addView(mController.getView());
		mController.setListener(this);
		mController.setCanReplay(!mFinishOnComplete);
		mController.setMediaTitle(title);
		
		mPlaylist = FragmentFile.getFileArray();
		
		if (mPlaylist != null) {
		    for(int i = 0; i < mPlaylist.size(); ++i) {
		        if (path.equalsIgnoreCase(mPlaylist.get(i).path)) {
		            mCurVideoIndex = i;
		            break;
		        }
		    }
		}
		
		if (mPlaylist == null || mPlaylist.size() == 1 || mIsStreaming) {
		    mController.showNextPrevBtn(false);
		} else {
		    mController.showNextPrevBtn(true);
		}
		
		mVideoView.setOnTouchListener(new View.OnTouchListener() {
            
            @Override
            public boolean onTouch(View arg0, MotionEvent arg1) {
                mController.show();
                return false;
            }
        });
		
		//register battery changed broadcast receiver
		registerReceiver(batteryChangedRecv, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
		
		setOnSystemUiVisibilityChangeListener();
		
		mGestureDetector = new GestureDetector(this, new MyGestureListener());
	}
	
	@Override
    protected void onDestroy() {
        unregisterReceiver(batteryChangedRecv);
        super.onDestroy();
    }

    private void setOnSystemUiVisibilityChangeListener() {
	    mVideoView.setOnSystemUiVisibilityChangeListener(
	            new View.OnSystemUiVisibilityChangeListener() {
                    
                    @Override
                    public void onSystemUiVisibilityChange(int visibility) {
                        int diff = mLastSystemUiVis ^ visibility;
                        mLastSystemUiVis = visibility;
                        if ((diff & View.SYSTEM_UI_FLAG_HIDE_NAVIGATION) != 0
                                && (visibility & View.SYSTEM_UI_FLAG_HIDE_NAVIGATION) == 0) {
                            mController.show();
                        }
                    }
                });
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.wind_player, menu);
		return true;
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if(mGestureDetector.onTouchEvent(event))
			return true;
		
		switch(event.getAction() & MotionEvent.ACTION_MASK) {
		case MotionEvent.ACTION_UP:
			endGesture();
			break;
		}
		
		return super.onTouchEvent(event);
	}
	
	private void endGesture() {
		mVolume = -1;
		mBrightness = -1f;
		
		//todo hide
		mHideHandler.removeMessages(0);
		mHideHandler.sendEmptyMessageDelayed(0, 2500);
	}
	
	//set time to hide
	@SuppressLint("HandlerLeak")
	private Handler mHideHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			mVolumeBrightnessLayout.setVisibility(View.GONE);
		}
		
	};
	
	//class to handle gesture listener
	private class MyGestureListener extends SimpleOnGestureListener {

		@Override
		public boolean onDoubleTap(MotionEvent e) {
			if (mLayout == VideoView.VIDEO_LAYOUT_ZOOM)
				mLayout = VideoView.VIDEO_LAYOUT_ORIGIN;
			else
				mLayout++;
			
			if (mVideoView != null)
				mVideoView.setVideoLayout(mLayout, 0);
			
			return true;
		}

		@Override
		public boolean onScroll(MotionEvent e1, MotionEvent e2,
				float distanceX, float distanceY) {
			float mOldX = e1.getX(), mOldY = e1.getY();
			int y = (int)e2.getRawY();
			Display display = getWindowManager().getDefaultDisplay();
			int windowWidth = display.getWidth();
			int windowHeight = display.getHeight();
			
			if (mOldX > windowWidth * 4.0 / 5)  //right scroll
				onVolumeSlide((mOldY - y) / windowHeight);
			else if (mOldX < windowWidth / 5.0) //left scroll
				onBrightnessSlide((mOldY - y) / windowHeight);
			/*else {								//other, hide the contorller
				mHideHandler.removeMessages(0);
				mHideHandler.sendEmptyMessageDelayed(0, 100);
			}*/
				
			return super.onScroll(e1, e2, distanceX, distanceY);
		}
		
	}
	
	//right scroll to change volume
	private void onVolumeSlide (float percent) {
		if (mVolume == -1) {
			mVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
			if (mVolume < 0)
				mVolume = 0;
			
			//show
			mOperationBg.setImageResource(R.drawable.video_volumn_bg);
			mVolumeBrightnessLayout.setVisibility(View.VISIBLE);
		}
		
		int index = (int) (percent * mMaxVolume) + mVolume;
		if (index > mMaxVolume)
			index = mMaxVolume;
		else if (index < 0)
			index = 0;
		
		//change the volume
		mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, index, 0);
		
		//change the ui
		ViewGroup.LayoutParams lp = mOperationPercent.getLayoutParams();
		lp.width = findViewById(R.id.operation_full).getLayoutParams().width * index / mMaxVolume;
		mOperationPercent.setLayoutParams(lp);
	}
	
	//left scroll to change brightness
	private void onBrightnessSlide (float percent) {
		if (mBrightness < 0) {
			mBrightness = getWindow().getAttributes().screenBrightness;
			if (mBrightness <= 0.00f)
				mBrightness = 0.50f;
			else if (mBrightness < 0.01f)
				mBrightness = 0.01f;
			
			//show
			mOperationBg.setImageResource(R.drawable.video_brightness_bg);
			mVolumeBrightnessLayout.setVisibility(View.VISIBLE);
		}
		
		//set screen brightness
		WindowManager.LayoutParams wlp = getWindow().getAttributes();
		wlp.screenBrightness = percent + mBrightness;
		if (wlp.screenBrightness > 1.0f)
			wlp.screenBrightness = 1.0f;
		else if (wlp.screenBrightness < 0.01f)
			wlp.screenBrightness = 0.01f;
		getWindow().setAttributes(wlp);
		
		ViewGroup.LayoutParams lp = mOperationPercent.getLayoutParams();
		lp.width = (int) (findViewById(R.id.operation_full).getLayoutParams().width * wlp.screenBrightness);
		mOperationPercent.setLayoutParams(lp);
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		if (mVideoView != null)
			mVideoView.setVideoLayout(mLayout, 0);
		super.onConfigurationChanged(newConfig);
	}

    @Override
    public void onCompletion(MediaPlayer mp) {
        finish();
    }

    @Override
    public void finish() {
        Intent intent = new Intent(getApplicationContext(), FragmentTVLive.class);
        intent.putExtra("Complete", true);
        Log.i(TAG, "set result: complete true");
        setResult(RESULT_OK, intent);
        super.finish();
    }
    
    //player control
    private boolean isPlaying() {
        return mVideoView != null && mVideoView.isPlaying();
    }
    
    private void startPlayer() {
        if (mVideoView != null) {
            mVideoView.start();
        }
        
        if (mController != null)
            mController.showPlaying();
        
        mHandler.post(mProgressChecker);
    }
    
    private void stopPlayer() {
        if (mVideoView != null) {
            mVideoView.pause();
        }
    }
    
    //update display name
    private void updateDisplayName() {
        if (!TextUtils.isEmpty(title)) {
            Log.i(TAG, "set file name: " + title);
            if (mMediaController != null)
                mMediaController.setFileName(title);
        }
    }
    
    //set seekbar time value
    private int setProgress() {
        if (mDragging || !mShowing || mVideoView == null)
            return 0;
        
        long pos = 0;
        
        if (mVideoView.isPlaying()) {
            pos = mVideoView.getCurrentPosition();
        } else {
            pos = mVideoPosition;
        }
        
        long duration = mVideoView.getDuration();
        mDuration = (int)duration;
        
        if (mIsControlPaused) {
            pos = mVideoPosition;
        } else {
            if (!mIsLiveStream) {
                if (mIsStreaming) {
                    switch (mSeekState) {
                    case SEEKFORWARD:
                        if (pos < mVideoPosition)
                            pos = mVideoPosition;
                        break;
                        
                    case SEEKBACK:
                        if (mVideoView.isPlaying() && pos < mBeforeSeekPosition) {
                            mSeekState = SeekState.NOSEEK;
                        }
                        
                        if (pos > mVideoPosition)
                            pos = mVideoPosition;
                        break;

                    default:
                        break;
                    }
                } else {
                    if (mVideoPosition > 0  && pos == 0)
                        pos = mVideoPosition;
                }
            }
        }
        
        mController.setTimes((int)pos, (int)duration);
        mVideoPosition = (int)pos;
        
        return (int)pos;
    }
    
    //set current time
    private void setCurrentTime() {
        mController.setCurrentTime(StringUtils.getCurrentTimeString());
    }
    
    //receive battery changed broadcast
    private BroadcastReceiver batteryChangedRecv = new BroadcastReceiver() {
        
        @Override
        public void onReceive(Context context, Intent intent) {
            if(Intent.ACTION_BATTERY_CHANGED.equals(intent.getAction())) {
                int level = intent.getIntExtra("level", 0);
                int scale = intent.getIntExtra("scale", 100);
                
                if (mController != null)
                    mController.setSurplusPower((level * 100 / scale) + "%");
            }
        }
    };
    
    //is need resume
    private boolean needResume = true;

    @Override
    public boolean onInfo(MediaPlayer mp, int what, int extra) {
        switch (what) {
        case MediaPlayer.MEDIA_INFO_BUFFERING_START:
            Log.i(TAG, "buffering start, player paused");
            // buffering start, player pause
            if (isPlaying()) {
                stopPlayer();
                needResume = true;
            }
            updateDisplayName();
            if (mIsStreaming)
                mLoadingView.setVisibility(View.VISIBLE);
            break;
            
        case MediaPlayer.MEDIA_INFO_BUFFERING_END:
            Log.i(TAG, "buffering end, start play");
            // buffering end, start play
            if (needResume)
                startPlayer();
            mLoadingView.setVisibility(View.GONE);
            break;
            
        case MediaPlayer.MEDIA_INFO_DOWNLOAD_RATE_CHANGED:
            //Log.i(TAG, "Download rate: " + extra + "KBps");
            String stringRate = getString(R.string.video_download_rate, extra);
            mLoadingText.setText(getString(R.string.video_layout_loading) + " " + stringRate);
            break;
            
        case MediaPlayer.MEDIA_INFO_NOT_SEEKABLE:
            mIsLiveStream = true;
            break;

        default:
            break;
        }
        return false;
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        Intent intent = new Intent(getApplicationContext(), FragmentTVLive.class);
        intent.putExtra("Complete", true);
        Log.i(TAG, "set result: on error");
        setResult(RESULT_OK, intent);
        
        return true;
    }

    @Override
    public void onPlayPause() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - mClickTime < 700) return;
        mClickTime = currentTime;
        
        if (mVideoView != null) {
            if (mVideoView.isPlaying()) {
                if (mIsLiveStream) {
                    mIsStop = true;
                    mVideoView.setVisibility(View.INVISIBLE);
                    mVideoPosition = 0;
                    mController.resetTime();
                } else {
                    pauseVideo();
                    mHasPaused = true;
                }
            } else {
                playVideo();
                mHasPaused = false;
            }
        }
    }

    @Override
    public void onShown() {
        mShowing = true;
        setProgress();
        
    }

    @Override
    public void onHidden() {
        mShowing = false;
    }

    @Override
    public void onReplay() {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void onStopVideo() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - mClickTime < 700) return;
        mClickTime = currentTime;
        
        if (mIsStop)
            return;
        
        mController.timeBarEnable(false);
        mController.showFfwdButton(false);
        mController.showRewButton(false);
        mController.showStopButton(false);
        
        if (mVideoView != null)
        mVideoView.setVisibility(View.INVISIBLE);
        stopPlaybackInRunnable();
        
        mController.showPaused();
        
        mVideoPosition = 0;
        setProgress();
        mIsStop = true;
    }

    @Override
    public void onNext() {
        nextVideo();
    }

    @Override
    public void onPrev() {
        prevVideo();
    }

    @Override
    public void onFfwd() {
        int pos = (int)mVideoView.getCurrentPosition();
        pos += 15000;
        if (pos > mDuration)
            pos = mDuration;
        
        Log.d(TAG, "onFfwd pos=" + pos);
        seek(pos);
        setProgress();
    }

    @Override
    public void onRew() {
        int pos = (int)mVideoView.getCurrentPosition();
        mBeforeSeekPosition = pos;
        pos -= 15000;
        if (pos <= 0)
            pos = 1;
        
        Log.d(TAG, "onPrev pos=" + pos);
        seek(pos);
        setProgress();
    }

    @Override
    public void onFavoriteVideo() {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void onSeekStart() {
        mDragging = true;
    }

    @Override
    public void onSeekMove(int time) {
        if (mController != null) {
            mController.setTimes(time, mDuration);
        }
    }

    @Override
    public void onSeekEnd(int time) {
        mDragging = false;
        mBeforeSeekPosition = mVideoPosition;
        mVideoPosition = time;
        seek(time);
    }
    
    private void pauseVideo() {
        if (!mIsLiveStream) {
            mIsControlPaused = true;
            
            if (mVideoView.isPlaying())
                mVideoPosition = (int) mVideoView.getCurrentPosition();
            
            mHandler.removeCallbacksAndMessages(null);
            mHandler.removeCallbacks(mProgressChecker);
            mVideoView.pause();
        }
        
        showControllerPaused();
    }
    
    private void showControllerPaused() {
        if (!mIsLiveStream) {
            mController.showPaused();
        } else {
            mController.clearPlayState();
        }
    }
    
    private void EnableControllButton() {
        if (mIsStop) {
            mController.setControlButtonEnable(false);
            mController.setControlButtonEnableForStop(true);
        } else {
            mController.setControlButtonEnable(true);
            mController.setControlButtonEnableForStop(true);
        }
        
        if (mIsLiveStream)
            mController.setLiveMode();
    }
    
    private void playVideo() {
        mIsControlPaused = false;
        mVideoView.start();
        mController.showPlaying();
        mController.showStopButton(true);
        mController.showFfwdButton(true);
        mController.showRewButton(true);

        mIsStop = false;
        mVideoView.setVisibility(View.VISIBLE);
        EnableControllButton();
        mHandler.post(mProgressChecker);
    }
    
    private void seek(int time) {
        if (mVideoView != null) {
            if ((int)mVideoView.getCurrentPosition() > time) {
                mSeekState = SeekState.SEEKBACK;
            } else if ((int)mVideoView.getCurrentPosition() < time) {
                mSeekState = SeekState.SEEKFORWARD;
            } else {
                mSeekState = SeekState.NOSEEK;
            }
            
            mVideoPosition = time;
            mVideoView.seekTo((long) time);
            
            if (mIsStreaming) {
                mController.showLoading();
                mHandler.removeCallbacks(mPlayingChecker);
                mHandler.postDelayed(mPlayingChecker, 250);
            }
            
            //setProgress();
        }
    }
    
    private void stopPlaybackInRunnable() {
        mIsStop = true;
        new Thread(new Runnable() {
            
            @Override
            public void run() {
                try {
                    if (mVideoView != null) {
                        mVideoView.stopPlayback();
                    }
                } catch (NullPointerException e) {
                    // TODO: handle exception
                }
            }
        }).start();
    }
    
    private void nextVideo() {
        if (mPlaylist != null) {
            if (mCurVideoIndex == mPlaylist.size() - 1) {
                mCurVideoIndex = 0;
            } else {
                ++mCurVideoIndex;
            }
            changeVideo();
        } else {
            if (mVideoView != null) {
                mVideoView.stopPlayback();
                mVideoView.setVideoPath(path);
                mVideoPosition = 0;
                mDuration = 0;
                mIsControlPaused = false;
                playVideo();
                mController.showPlaying();
                mController.clearPlayState();
                EnableControllButton();
            }
        }
    }
    
    private void prevVideo() {
        if (mPlaylist != null) {
            if (mCurVideoIndex <= 0) {
                mCurVideoIndex = mPlaylist.size() - 1;
            } else {
                --mCurVideoIndex;
            }
            changeVideo();
        } else {
            if (mVideoView != null) {
                mVideoView.stopPlayback();
                mVideoView.setVideoPath(path);
                mVideoPosition = 0;
                mDuration = 0;
                mIsControlPaused = false;
                playVideo();
                mController.showPlaying();
                mController.clearPlayState();
                EnableControllButton();
            }
        }
    }
    
    private void changeVideo() {
        if (mIsStreaming) {
            
        } else {
            mController.showLoading();
            mVideoView.stopPlayback();
            EnableControllButton();
            _changeVideo();
        }
    }
    
    private void _changeVideo() {
        mIsChanged = true;
        PFile pf = mPlaylist.get(mCurVideoIndex);
        path = pf.path;
        mVideoView.setVideoPath(path);
        List<String> paths = Uri.parse(path).getPathSegments();
        title = paths == null || paths.isEmpty() ? "null" : paths.get(paths.size() - 1);
        mController.setMediaTitle(title);
        mVideoPosition = 0;
        mDuration = 0;
        mIsControlPaused = false;
        playVideo();
        mController.showPlaying();
        mController.clearPlayState();
    }

    @Override
    public void onSeekComplete(MediaPlayer mp) {
        if (mIsControlPaused) {
            if (!mIsLiveStream)
                mController.showPaused();
            else
                mController.clearPlayState();
        } else {
            mController.showPlaying();
            mController.clearPlayState();
        }
    }
}
