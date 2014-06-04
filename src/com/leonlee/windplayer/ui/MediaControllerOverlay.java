/**
 * 
 */
package com.leonlee.windplayer.ui;

import com.leonlee.windplayer.R;
import com.leonlee.windplayer.util.StringUtils;

import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
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
    private boolean mDragging = false;
    
    private int mDuration = 0;
    
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
        mFavoriteBtn.setOnClickListener(favoriteListener);
        
        //center views
        mLoadingView = mRoot.findViewById(R.id.controller_video_loading);
        mLoadingText = (TextView) mRoot.findViewById(R.id.video_loading_text);
        mErrorView = (TextView) mRoot.findViewById(R.id.controller_error_text);
        mPlayPauseReplayView = (ImageButton) mRoot.findViewById(R.id.mediacontroller_play_pause);
        
        //bottom views
        mCurrentPosition = (TextView) mRoot.findViewById(R.id.controller_time_current);
        mProgress = (SeekBar) mRoot.findViewById(R.id.controller_seekbar);
        mProgress.setOnSeekBarChangeListener(mSeekListener);
        mEndTime = (TextView) mRoot.findViewById(R.id.controller_time_total);
        
        //control buttons
        mPrevVideoView = (ImageButton) mRoot.findViewById(R.id.controller_prev_video);
        mPrevVideoView.setOnClickListener(prevListener);
        
        mRewButton = (ImageButton) mRoot.findViewById(R.id.controller_rewind);
        mRewButton.setOnClickListener(rewindListener);
        
        mStopButton = (ImageButton) mRoot.findViewById(R.id.controller_stop);
        mStopButton.setOnClickListener(stopListener);
        
        mFfwdButton = (ImageButton) mRoot.findViewById(R.id.controller_fastforward);
        mFfwdButton.setOnClickListener(fastforwardListener);
        
        mNextVideoView = (ImageButton) mRoot.findViewById(R.id.controller_next_video);
        mNextVideoView.setOnClickListener(nextListener);
        
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
    public void setTimes(int currentTime, int totalTime) {
        mDuration = totalTime;
        if (mProgress != null) {
            if (totalTime > 0) {
                int pos = 1000 * currentTime / totalTime;
                mProgress.setProgress(pos);
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
        mProgress.setEnabled(enable);
        mPrevVideoView.setClickable(enable);
        mRewButton.setClickable(enable);
        mStopButton.setClickable(enable);
        mFfwdButton.setClickable(enable);
        mNextVideoView.setClickable(enable);
    }

    @Override
    public void setControlButtonEnableForStop(boolean enable) {
        mNextVideoView.setEnabled(enable);
        mPrevVideoView.setEnabled(enable);
    }

    @Override
    public void clearPlayState() {
        mPlayPauseReplayView.setVisibility(View.INVISIBLE);
        mLoadingView.setVisibility(View.INVISIBLE);
        show();
    }

    @Override
    public void showFfwdButton(boolean show) {
        if (!show) {
            mFfwdButton.setImageResource(R.drawable.ic_vidcontrol_ffwd_false);
            mFfwdButton.setClickable(false);
        } else {
            mFfwdButton.setImageResource(R.drawable.ic_vidcontrol_ffwd);
            mFfwdButton.setClickable(true);
        }
    }

    @Override
    public void showRewButton(boolean show) {
        if (!show) {
            mRewButton.setImageResource(R.drawable.ic_vidcontrol_rew_false);
            mRewButton.setClickable(false);
        } else {
            mRewButton.setImageResource(R.drawable.ic_vidcontrol_rew);
            mRewButton.setClickable(true);
        }
    }
    
    public void showStopButton(boolean show) {
        if (!show) {
            mStopButton.setImageResource(R.drawable.ic_vidcontrol_stop_unable);
            mStopButton.setClickable(false);
        } else {
            mStopButton.setImageResource(R.drawable.ic_vidcontrol_stop);
            mStopButton.setClickable(true);
        }
    }
    
    public void showNextPrevBtn(boolean show) {
        if (!show) {
            mNextVideoView.setVisibility(View.GONE);
            mPrevVideoView.setVisibility(View.GONE);
        } else {
            mNextVideoView.setVisibility(View.VISIBLE);
            mNextVideoView.setVisibility(View.VISIBLE);
        }
    }
    
    public void setMediaTitle(String title) {
        if (mMediaTitle != null)
            mMediaTitle.setText(title);
        
        if (mSurplusPower != null)
            mSurplusPower.setText("98%");
        
        if (mCurrentTime != null)
            mCurrentTime.setText("18:00");
    }

    @Override
    public void setStopButton(boolean enable) {
        mStopButton.setEnabled(enable);
    }

    @Override
    public void timeBarEnable(boolean enable) {
        mProgress.setEnabled(enable);
    }

    @Override
    public void setLiveMode() {
        mRewButton.setImageResource(R.drawable.ic_vidcontrol_rew_false);
        mRewButton.setClickable(false);
        mFfwdButton.setImageResource(R.drawable.ic_vidcontrol_ffwd_false);
        mFfwdButton.setClickable(false);
        mProgress.setEnabled(false);
        mIsLiveMode = true;
    }
    
    public void resetTime() {
        setTimes(0, 0);
    }

    @Override
    public void onClick(View v) {
        if (mListener != null) {
            if (v == mPlayPauseReplayView) {
                if (mState == State.ENDED) {
                    if (mCanReplay)
                        mListener.onReplay();
                } else if (mState == State.PLAYING || mState == State.PAUSED) {
                    mListener.onPlayPause();
                }
            }
        }
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

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (mHidden) {
            show();
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (super.onTouchEvent(event))
            return true;
        
        if (mHidden) {
            show();
            return true;
        }
        
        switch (event.getAction()) {
        case MotionEvent.ACTION_DOWN:
            cancelHiding();
            if (mState == State.PAUSED || mState == State.PLAYING) {
                if (mListener != null)
                    mListener.onPlayPause();
            }
            break;
            
        case MotionEvent.ACTION_UP:
            maybeStartHiding();
            break;

        default:
            break;
        }
        
        return true;
    }
    
    public void hideToShow() {
        maybeStartHiding();
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
    
    public void showLoadingView(boolean show) {
        mLoadingView.setVisibility(show ? View.VISIBLE : View.GONE);
    }
    
    public void setLoadingText(String text) {
        mLoadingText.setText(text);
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
        Log.e(TAG, "maybeStartHiding");
        cancelHiding();
        
        Log.e(TAG, "start hiding after 2.5s, mState=" + mState);
        if (mState == State.PLAYING) {
            handler.postDelayed(startHidingRunnable, 2500);
        }
    }
    
    private void cancelHiding() {
        handler.removeCallbacks(startHidingRunnable);
        mRoot.setAnimation(null);
    }
    
    private void startHiding() {
        startHideAnimation(mCenterView);
        startHideAnimation(mProgress);
    }
    
    private void startHideAnimation(View view) {
        if (view.getVisibility() == View.VISIBLE) {
            view.startAnimation(hideAnimation);
        }
    }
    
    //button click listener
    private View.OnClickListener prevListener = new View.OnClickListener() {
        
        @Override
        public void onClick(View v) {
            mListener.onPrev();
        }
    };
    
    private View.OnClickListener rewindListener = new View.OnClickListener() {
        
        @Override
        public void onClick(View v) {
            mListener.onRew();
        }
    };
    
    private View.OnClickListener stopListener = new View.OnClickListener() {
        
        @Override
        public void onClick(View v) {
            mListener.onStopVideo();
        }
    };
    
    private View.OnClickListener fastforwardListener = new View.OnClickListener() {
        
        @Override
        public void onClick(View v) {
            mListener.onFfwd();
        }
    };
    
    private View.OnClickListener nextListener = new View.OnClickListener() {
        
        @Override
        public void onClick(View v) {
            mListener.onNext();
        }
    };
    
    private View.OnClickListener favoriteListener = new View.OnClickListener() {
        
        @Override
        public void onClick(View v) {
            mListener.onFavoriteVideo();
        }
    };
    
    //seekbar change listener
    private OnSeekBarChangeListener mSeekListener = new OnSeekBarChangeListener() {
        
        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            maybeStartHiding();
            int endPosition = (mDuration * seekBar.getProgress()) / 1000;
            
            if (mListener != null)
                mListener.onSeekEnd(endPosition);
        }
        
        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            cancelHiding();
            mDragging = true;
            if (mListener != null)
                mListener.onSeekStart();
        }
        
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress,
                boolean fromUser) {
            if (!fromUser)
                return;
            cancelHiding();
            
            int newPosition = (mDuration * progress) / 1000;
            
            if (mListener != null)
                mListener.onSeekMove(newPosition);
        }
    };
}
