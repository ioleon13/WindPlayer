/**
 * 
 */
package com.leonlee.windplayer.ui;

import com.leonlee.windplayer.R;

import android.content.Context;
import android.os.Handler;
import android.view.View;
import android.widget.FrameLayout;
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
    
    //top controller view
    private TextView mMediaTitle, mCurrentTime, mSurplusPower;
    private ImageView mStarredView;
    
    //center view
    private View mCenterView;
    private ImageView mPlayPauseReplayView;
    private View mLoadingView;
    private TextView mErrorView;
    
    //bottom controller view
    private SeekBar mProgress;
    private TextView mEndTime, mCurrentPosition;
    private ImageView mStopButton;
    private ImageView mNextVideoView;
    private ImageView mPrevVideoView;
    private ImageView mFfwdButton;
    private ImageView mRewButton;
    
    private boolean mIsLiveMode = false;
    private boolean mHidden;
    private boolean mCanReplay = true;
    
    private final Handler handler;
    private final Runnable startHidingRunnable;
    private final Animation hideAnimation;

    public MediaControllerOverlay(Context context) {
        super(context);
        
        handler = new Handler();
        startHidingRunnable = new Runnable() {
                @Override
            public void run() {
                
            }
        };
        
        hideAnimation = AnimationUtils.loadAnimation(context, R.anim.controller_fadout);
        hideAnimation.setAnimationListener(this);
    }

    @Override
    public void setListener(Listener l) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setCanReplay(boolean canReplay) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public View getView() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void show() {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void showPlaying() {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void showPaused() {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void showEnded() {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void showLoading() {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void showErrorMessage(String message) {
        // TODO Auto-generated method stub
        
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
        // TODO Auto-generated method stub
        
    }

    @Override
    public void onAnimationRepeat(Animation arg0) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void onAnimationStart(Animation arg0) {
        // TODO Auto-generated method stub
        
    }

}
