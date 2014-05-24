package com.leonlee.windplayer.ui;

import com.leonlee.windplayer.R;

import io.vov.vitamio.LibsChecker;
import io.vov.vitamio.MediaPlayer;
import io.vov.vitamio.MediaPlayer.OnCompletionListener;
import io.vov.vitamio.MediaPlayer.OnErrorListener;
import io.vov.vitamio.MediaPlayer.OnInfoListener;
import io.vov.vitamio.widget.MediaController;
import io.vov.vitamio.widget.VideoView;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
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
    implements OnCompletionListener, OnInfoListener, OnErrorListener{
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
	
	//is streaming
	private boolean mIsStreaming = false; 
	
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
		
		//path = getIntent().getStringExtra("path");
		Intent intent = getIntent();
		if (intent != null) {
		    path = intent.getData().toString();
		    title = intent.getStringExtra(FragmentOnline.DISPLAY_NAME);
		    mIsStreaming = intent.getBooleanExtra(FragmentOnline.IS_STREAM, false);
		}
		
		if (TextUtils.isEmpty(path))
			path = Environment.getExternalStorageDirectory() + "DCIM/Camera/MOV041.mp4";
		
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
		
		mMediaController = new MediaController(this);
		mVideoView.setMediaController(mMediaController);
		mVideoView.requestFocus();
		
		mGestureDetector = new GestureDetector(this, new MyGestureListener());
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
}
