/**
 * 
 */
package com.leonlee.windplayer.ui;

import com.leonlee.windplayer.R;
import com.leonlee.windplayer.util.StringUtils;

import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;

/**
 * @author liya
 *
 */
public class MediaControllerOverlay extends FrameLayout implements
        ControllerOverlay, OnClickListener, AnimationListener{
    
    protected enum State {
        PLAYING,
        PAUSED,
        ENDED,
        ERROR,
        LOADING
    }
    
    private static final String TAG = "MediaControllerOverlay";
    
    private Listener mListener;
    
    private View mRoot;
    
    //top controller view
    private TextView mMediaTitle, mCurrentTime, mSurplusPower;
    private ImageButton mFavoriteBtn;
    
    //center view
    private View mCenterView;
    private ImageButton mPlayPauseReplayView;
    private View mLoadingView;
    private TextView mLoadingText;
    private TextView mErrorView;
    
    //bottom controller view
    private SeekBar mProgress;
    private TextView mEndTime, mCurrentPosition;
    private ImageButton mStopButton;
    private ImageButton mNextVideoView;
    private ImageButton mPrevVideoView;
    private ImageButton mFfwdButton;
    private ImageButton mRewButton;
    
    private State mState;
    
    private boolean mIsLiveMode = false;
    private boolean mHidden = false;
    private boolean mCanReplay = true;
    
    private final Handler handler;
    private final Runnable startHidingRunnable;
    private final Animation hideAnimation;

    public MediaControllerOverlay(Context context) {
        super(context);
        
        mState = State.LOADING;
        
        initControllerView(context);
        
        handler = new Handler();
        startHidingRunnable = new Runnable() {
                @Override
            public void run() {
                startHiding();
            }
        };
        
        hideAnimation = AnimationUtils.loadAnimation(context, R.anim.controller_fadout);
        hideAnimation.setAnimationListener(this);
    }
    
    private void initControllerView(Context context) {
        mRoot = ((LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE))
                .inflate(R.layout.media_controller_main, this);
        
        //media controller top views
        mMediaTitle = (TextView) mRoot.findViewById(R.id.controller_media_name);
        mCurrentTime = (TextView) mRoot.findViewById(R.id.controller_system_time);
        mSurplusPower = (TextView) mRoot.findViewById(R.id.controller_surplus_power);
        mFavoriteBtn = (ImageButton) mRoot.findViewById(R.id.controller_starred);
        
        //center views
        mLoadingView = mRoot.findViewById(R.id.controller_video_loading);
        mLoadingText = (TextView) mRoot.findViewById(R.id.video_loading_text);
        mErrorView = (TextView) mRoot.findViewById(R.id.controller_error_text);
        mPlayPauseReplayView = (ImageButton) mRoot.findViewById(R.id.mediacontroller_play_pause);
        
        //bottom views
        mCurrentPosition = (TextView) mRoot.findViewById(R.id.controller_time_current);
        mProgress = (SeekBar) mRoot.findViewById(R.id.controller_seekbar);
        mEndTime = (TextView) mRoot.findViewById(R.id.controller_time_total);
        
        //control buttons
        mPrevVideoView = (ImageButton) mRoot.findViewById(R.id.controller_prev_video);
        mRewButton = (ImageButton) mRoot.findViewById(R.id.controller_rewind);
        mStopButton = (ImageButton) mRoot.findViewById(R.id.controller_stop);
        mFfwdButton = (ImageButton) mRoot.findViewById(R.id.controller_fastforward);
        mNextVideoView = (ImageButton) mRoot.findViewById(R.id.controller_next_video);
        
        hide();
    }

    @Override
    public void setListener(Listener l) {
        this.mListener = l;
    }

    @Override
    public void setCanReplay(boolean canReplay) {
        this.mCanReplay = canReplay;
    }

    @Override
    public View getView() {
        return this;
    }

    @Override
    public void show() {
        boolean wasHidden = mHidden;
        mHidden = false;
        
        updateViews();
        setVisibility(View.VISIBLE);
        setFocusable(false);
        
        if (mListener != null && wasHidden != mHidden) {
            mListener.onShown();
        }
        
        maybeStartHiding();
    }

    @Override
    public void showPlaying() {
        Log.d(TAG, "showPlaying");
        if (mIsLiveMode) {
            mRewButton.setImageResource(R.drawable.ic_vidcontrol_rew_false);
            mRewButton.setClickable(false);
            mFfwdButton.setImageResource(R.drawable.ic_vidcontrol_ffwd_false);
            mFfwdButton.setClickable(false);
        }
        
        mState = State.PLAYING;
        showCenterView(mPlayPauseReplayView);
    }

    @Override
    public void showPaused() {
        Log.d(TAG, "showPaused");
        mState = State.PAUSED;
        showCenterView(mPlayPauseReplayView);
    }

    @Override
    public void showEnded() {
        Log.d(TAG, "showEnded");
        mState = State.ENDED;
        if (mCanReplay)
            showCenterView(mPlayPauseReplayView);
    }

    @Override
    public void showLoading() {
        Log.d(TAG, "showLoading");
        mState = State.LOADING;
        showCenterView(mLoadingView);
    }

    @Override
    public void showErrorMessage(String message) {
        mState = State.ERROR;
        mErrorView.setText(message);
        showCenterView(mErrorView);
    }
    
    @Override
    public void setTimes(long currentTime, long totalTime) {
        if (mProgress != null) {
            if (totalTime > 0) {
                long pos = 1000L * currentTime / totalTime;
                mProgress.setProgress((int) pos);
            }
        }
        
        if (mCurrentPosition != null) {
            mCurrentPosition.setText(StringUtils.generateTime(currentTime));
        }
        
        if (mEndTime != null) {
            mEndTime.setText(StringUtils.generateTime(currentTime));
        }
    }

    @Override
    public void setControlButtonEnable(boolean enable) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setControlButtonEnableForStop(boolean enable) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void clearPlayState() {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void showFfwdButton(boolean show) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void showRewButton(boolean show) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setStopButton(boolean enable) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void timeBarEnable(boolean enable) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setLiveMode() {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void onAnimationEnd(Animation arg0) {
        hide();
    }

    @Override
    public void onAnimationRepeat(Animation arg0) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void onAnimationStart(Animation arg0) {
        // TODO Auto-generated method stub
        
    }

    public void hide() {
        boolean wasHidden = mHidden;
        mHidden = true;
        
        setVisibility(View.INVISIBLE);
        mRoot.setVisibility(View.INVISIBLE);
        
        setFocusable(true);
        requestFocus();
        
        if (mListener != null && wasHidden != mHidden) {
            mListener.onHidden();
        }
    }
    
    private void showCenterView(View view) {
        mCenterView = view;
        mErrorView.setVisibility(mCenterView == mErrorView ? View.VISIBLE : View.INVISIBLE);
        mLoadingView.setVisibility(mCenterView == mLoadingView ? View.VISIBLE : View.INVISIBLE);
        mPlayPauseReplayView.setVisibility(mCenterView == mPlayPauseReplayView ?
                View.VISIBLE : View.INVISIBLE);
        
        show();
    }
    
    private void updateViews() {
        if (mHidden)
            return;
        
        if (mIsLiveMode) {
            mPlayPauseReplayView.setImageResource(
                    mState == State.PAUSED ? R.drawable.ic_vidcontrol_play
                            : mState == State.PLAYING ? R.drawable.ic_vidcontrol_stop
                                    : R.drawable.ic_vidcontrol_reload);
        } else {
            mPlayPauseReplayView.setImageResource(
                    mState == State.PAUSED ? R.drawable.ic_vidcontrol_play
                            : mState == State.PLAYING ? R.drawable.ic_vidcontrol_pause
                                    : R.drawable.ic_vidcontrol_reload);
        }
        
        mPlayPauseReplayView.setVisibility((mState != State.LOADING
                && mState != State.ERROR
                && !(mState == State.ENDED && !mCanReplay)) ? View.VISIBLE : View.GONE);
        
        requestLayout();
    }
    
    private void maybeStartHiding() {
        cancelHiding();
        
        if (mState == State.PLAYING) {
            handler.postDelayed(startHidingRunnable, 2500);
        }
    }
    
    private void cancelHiding() {
        handler.removeCallbacks(startHidingRunnable);
        mRoot.setAnimation(null);
    }
    
    private void startHiding() {
        if (mRoot.getVisibility() == View.VISIBLE) {
            mRoot.setAnimation(hideAnimation);
        }
    }
}
