package tv.lycam.player.single;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import tv.lycam.player.PlayerState;
import tv.lycam.player.R;
import tv.lycam.player.utils.AudioMngHelper;
import tv.lycam.player.utils.CommonUtil;
import tv.lycam.player.widget.IVideoView;


/**
 * @author 诸葛不亮
 * @version 1.0
 * @description
 */

public abstract class BaseControllerPlayer extends BasePlayer {
    // 常量
    private static final int ROOT_LAYOUT_ID = R.layout.lycam_player;
    private static final int SURFACE_ID = R.id.lycam_player_surface_container;
    private static final int IC_MEDIA_PAUSE_ID = R.drawable.lycam_player_media_pause;
    private static final int IC_MEDIA_START_ID = R.drawable.lycam_player_media_start;
    private static final int IC_MEDIA_STOP_ID = R.drawable.lycam_player_media_stop;
    private static final int PAUSE_BUTTON_ID = R.id.lycam_player_pause;
    private static final int MEDIACONTROLLER_PROGRESS_ID = R.id.lycam_player_progress;
    private static final int END_TIME_ID = R.id.lycam_player_duration;
    private static final int CURRENT_TIME_ID = R.id.lycam_player_time_current;
    private static final int BOTTOM_CONTAINER_ID = R.id.lycam_player_layout_bottom;
    private static final int TOP_CONTAINER_ID = R.id.lycam_player_layout_top;
    private static final int BOTTOM_PROGRESS_ID = R.id.lycam_player_layout_progress;
    private int mDismissControlTime = 2500;

    // ui
    protected ImageView mPauseButton;
    protected SeekBar mProgressBar;
    protected TextView mTotalTime, mCurrentTime;
    //顶部和底部区域
    protected ViewGroup mBottomContainer, mBottomProgressContainer;
    // 界面逻辑
    private long mDuration;
    private boolean mShowing;
    private boolean mDragging;
    private boolean mInstantSeeking = false;
    protected boolean isForbidAdjustVolumn = false;
    protected boolean isForbidAdjustBrightness = false;
    protected boolean isForbidChangePosition = false;

    //进度定时器
    protected Timer updateProcessTimer;

    //触摸显示消失定时
    protected Timer mDismissControlViewTimer;

    //定时器任务
    protected ProgressTimerTask mProgressTimerTask;

    //触摸显示消失定时任务
    protected DismissControlViewTimerTask mDismissControlViewTimerTask;

    //手指放下的位置
    protected int mDownPosition;

    //手势调节音量的大小
    protected int mGestureDownVolume;

    //手势偏差值
    protected int mThreshold = 80;

    //手动改变滑动的位置
    protected int mSeekTimePosition;

    //手动滑动的起始偏移位置
    protected int mSeekEndOffset;

    //触摸的X
    protected float mDownX;

    //触摸的Y
    protected float mDownY;

    //移动的Y
    protected float mMoveY;

    //亮度
    protected float mBrightnessData = -1;

    //触摸滑动进度的比例系数
    protected float mSeekRatio = 1;

    //触摸的是否进度条
    protected boolean mTouchingProgressBar = false;

    //是否改变音量
    protected boolean mChangeVolume = false;

    //是否改变播放进度
    protected boolean mChangePosition = false;

    //触摸显示虚拟按键
    protected boolean mShowVKey = false;

    //是否改变亮度
    protected boolean mBrightness = false;

    //是否首次触摸
    protected boolean mFirstTouch = false;

    //是否隐藏虚拟按键
    protected boolean mHideKey = true;

    //是否需要显示流量提示
    protected boolean mNeedShowWifiTip = true;

    //是否支持非全屏滑动触摸有效
    protected boolean mIsTouchWidget = true;

    //是否支持全屏滑动触摸有效
    protected boolean mIsTouchWigetFull = true;

    // 忽略所有touch事件
    private boolean mIgnoreAllEvent = false;

    public BaseControllerPlayer(@NonNull Context context) {
        super(context);
    }

    public BaseControllerPlayer(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public BaseControllerPlayer(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void init(Context context) {
        super.init(context);
        mSeekEndOffset = CommonUtil.dip2px(context, 50);
    }

    protected int getRootLayoutId() {
        return ROOT_LAYOUT_ID;
    }

    protected int getPlayerId() {
        return SURFACE_ID;
    }

    @Override
    public void onFinishInflate() {
        initControllerView();
        super.onFinishInflate();
    }

    /**
     * 暂停按钮图标
     *
     * @return
     */
    protected int getIconMediaPauseId() {
        if (isLiveMode()) {
            return IC_MEDIA_STOP_ID;
        }
        return IC_MEDIA_PAUSE_ID;
    }

    /**
     * 播放按钮图标
     *
     * @return
     */
    protected int getIconMediaStartId() {
        return IC_MEDIA_START_ID;
    }

    /**
     * 暂停/播放 按钮id
     *
     * @return
     */
    protected int getPauseButtonId() {
        return PAUSE_BUTTON_ID;
    }

    /**
     * 总时长文本id
     *
     * @return
     */
    protected int getEndTimeLabelId() {
        return END_TIME_ID;
    }


    /**
     * 当前播放位置文本id
     *
     * @return
     */
    protected int getCurrentTimeLabelId() {
        return CURRENT_TIME_ID;
    }

    /**
     * 进度条 id
     *
     * @return
     */
    protected int getProgressId() {
        return MEDIACONTROLLER_PROGRESS_ID;
    }

    /**
     * 底部container id
     *
     * @return
     */
    protected int getBottomContainerId() {
        return BOTTOM_CONTAINER_ID;
    }


    /**
     * 顶部Container id
     *
     * @return
     */
    protected int getTopContainerId() {
        return TOP_CONTAINER_ID;
    }

    /**
     * Progress Container id
     *
     * @return
     */
    protected int getProgressContainerId() {
        return BOTTOM_PROGRESS_ID;
    }

    @Override
    public void onMediaFrameInfo() {
        super.onMediaFrameInfo();
        showPause();
        setProgress();
    }

    /**
     * 初始化布局控件
     */
    private void initControllerView() {
        // By default these are hidden.
        mPauseButton = findViewById(getPauseButtonId());
        if (mPauseButton != null) {
            mPauseButton.requestFocus();
            mPauseButton.setOnClickListener(mPauseListener);
        }

        mProgressBar = findViewById(getProgressId());
        if (mProgressBar != null) {
            if (mProgressBar instanceof SeekBar) {
                SeekBar seeker = (SeekBar) mProgressBar;
                seeker.setOnSeekBarChangeListener(mSeekListener);
                seeker.setThumbOffset(1);
            }
            mProgressBar.setMax(1000);
        }

        mTotalTime = findViewById(getEndTimeLabelId());
        mCurrentTime = findViewById(getCurrentTimeLabelId());

        mBottomContainer = findViewById(getBottomContainerId());

        mBottomProgressContainer = findViewById(getProgressContainerId());
        initCustomView();
    }

    /**
     * 初始化自定义的View及逻辑
     */
    protected void initCustomView() {
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        cancelProgressTimer();
        cancelDismissControlViewTimer();
    }

    /****Controller show/hide Listener  START****/

    public interface OnShownListener {
        void onShown();
    }

    private OnShownListener mShownListener;

    public void setOnShownListener(OnShownListener l) {
        mShownListener = l;
    }

    public interface OnHiddenListener {
        void onHidden();
    }

    private OnHiddenListener mHiddenListener;

    public void setOnHiddenListener(OnHiddenListener l) {
        mHiddenListener = l;
    }

    private class ProgressTimerTask extends TimerTask {
        @Override
        public void run() {
            if (mCurrentState == PlayerState.CURRENT_STATE_PLAYING || mCurrentState == PlayerState.CURRENT_STATE_PAUSE) {
                post(new Runnable() {
                    @Override
                    public void run() {
                        setProgress();
                    }
                });
            }
        }
    }

    private class DismissControlViewTimerTask extends TimerTask {

        @Override
        public void run() {
            if (mCurrentState != PlayerState.CURRENT_STATE_NORMAL
                    && mCurrentState != PlayerState.CURRENT_STATE_ERROR
                    && mCurrentState != PlayerState.CURRENT_STATE_AUTO_COMPLETE) {
                if (mContext != null) {
                    ((Activity) mContext).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            hideAllWidget();
                            if (mHideKey && !mIsPortrait && mShowVKey) {
                                CommonUtil.hideNavKey(mContext);
                            }
                        }
                    });
                }
            }
        }
    }

    protected void startProgressTimer() {
        cancelProgressTimer();
        updateProcessTimer = new Timer();
        mProgressTimerTask = new ProgressTimerTask();
        updateProcessTimer.schedule(mProgressTimerTask, 0, 300);
    }

    protected void startProgressTimer(long delay) {
        cancelProgressTimer();
        updateProcessTimer = new Timer();
        mProgressTimerTask = new ProgressTimerTask();
        updateProcessTimer.schedule(mProgressTimerTask, delay, 300);
    }


    protected void cancelProgressTimer() {
        if (updateProcessTimer != null) {
            updateProcessTimer.cancel();
            updateProcessTimer = null;
        }
        if (mProgressTimerTask != null) {
            mProgressTimerTask.cancel();
            mProgressTimerTask = null;
        }

    }

    protected void startDismissControlViewTimer() {
        cancelDismissControlViewTimer();
        mDismissControlViewTimer = new Timer();
        mDismissControlViewTimerTask = new DismissControlViewTimerTask();
        mDismissControlViewTimer.schedule(mDismissControlViewTimerTask, mDismissControlTime);
    }

    protected void cancelDismissControlViewTimer() {
        if (mDismissControlViewTimer != null) {
            mDismissControlViewTimer.cancel();
            mDismissControlViewTimer = null;
        }
        if (mDismissControlViewTimerTask != null) {
            mDismissControlViewTimerTask.cancel();
            mDismissControlViewTimerTask = null;
        }

    }

    @Override
    public void onMediaPrepared() {
        startProgressTimer(300);
    }

    @Override
    public void onMediaSeekCompleted() {
        super.onMediaSeekCompleted();
        startDismissControlViewTimer();
        startProgressTimer();
    }

    @Override
    public void onMediaBufferingUpdate(int percent) {
        super.onMediaBufferingUpdate(percent);
    }

    private long setProgress() {
        if (mDragging) {
            return 0;
        }

        int position = getCurrentPosition();
        int duration = getDuration();
        if (mProgressBar != null) {
            if (duration > 0) {
                long pos = 1000L * position / duration;
                mProgressBar.setProgress((int) pos);
            }
            int percent = getBufferPosition();
            mProgressBar.setSecondaryProgress(percent * 10);
        }

        mDuration = duration;

        if (mTotalTime != null)
            mTotalTime.setText(generateTime(mDuration));
        if (mCurrentTime != null)
            mCurrentTime.setText(generateTime(position));

        return position;
    }

    private String generateTime(long position) {
        int totalSeconds = (int) (position / 1000);

        int seconds = totalSeconds % 60;
        int minutes = (totalSeconds / 60) % 60;
        int hours = totalSeconds / 3600;

        if (hours > 0) {
            return String.format(Locale.US, "%02d:%02d:%02d", hours, minutes,
                    seconds);
        } else {
            return String.format(Locale.US, "%02d:%02d", minutes, seconds);
        }
    }


    private OnClickListener mPauseListener = new OnClickListener() {
        public void onClick(View v) {
            if (TextUtils.isEmpty(getVideoPath())) {
                //Toast.makeText(getActivityContext(), getResources().getString(R.string.no_url), Toast.LENGTH_SHORT).show();
                return;
            }
            if (mCurrentState == PlayerState.CURRENT_STATE_NORMAL || mCurrentState == PlayerState.CURRENT_STATE_ERROR) {
                if (!getVideoPath().startsWith("file") && !CommonUtil.isWifiConnected(getContext())
                        && mNeedShowWifiTip) {
                    showWifiDialog();
                    return;
                }
                doResume();
            } else if (mCurrentState == PlayerState.CURRENT_STATE_PLAYING) {
                doPause();
            } else if (mCurrentState == PlayerState.CURRENT_STATE_PAUSE) {
                doResume();
                setStateAndUi(PlayerState.CURRENT_STATE_PLAYING);
            } else if (mCurrentState == PlayerState.CURRENT_STATE_AUTO_COMPLETE) {
                doResume();
            }
        }
    };

    @Override
    public void resume() {
        super.resume();
    }

    private void updatePausePlay() {
        if (mPauseButton == null)
            return;
        Activity activity = CommonUtil.getActivityContext(mContext);
        if (activity != null) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (mCurrentState == PlayerState.CURRENT_STATE_PLAYING)
                        mPauseButton.setImageResource(getIconMediaPauseId());
                    else
                        mPauseButton.setImageResource(getIconMediaStartId());
                }
            });
        }

    }

    private void showPause() {
        if (mPauseButton == null)
            return;
        Activity activity = CommonUtil.getActivityContext(mContext);
        if (activity != null) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mPauseButton.setImageResource(getIconMediaPauseId());
                }
            });
        }
    }

    private void showPlay() {
        if (mPauseButton == null)
            return;
        Activity activity = CommonUtil.getActivityContext(mContext);
        if (activity != null) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mPauseButton.setImageResource(getIconMediaStartId());
                }
            });
        }
    }

    private void doPause() {
        if (mPauseButton == null)
            return;
        pause();
        Activity activity = CommonUtil.getActivityContext(mContext);
        if (activity != null) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mPauseButton.setImageResource(getIconMediaStartId());
                }
            });
        }
    }

    private void doResume() {
        if (mPauseButton == null)
            return;
        if (mCurrentState == PlayerState.CURRENT_STATE_NORMAL || mCurrentState == PlayerState.CURRENT_STATE_AUTO_COMPLETE
                || mCurrentState == PlayerState.CURRENT_STATE_ERROR) {
            this.start();
        } else {
            resume();
        }
        Activity activity = CommonUtil.getActivityContext(mContext);
        if (activity != null) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mPauseButton.setImageResource(getIconMediaPauseId());
                }
            });
        }
    }

    private SeekBar.OnSeekBarChangeListener mSeekListener = new SeekBar.OnSeekBarChangeListener() {

        public void onStartTrackingTouch(SeekBar bar) {
            mDragging = true;
            cancelDismissControlViewTimer();
            if (mInstantSeeking)
                AudioMngHelper.instance(mContext).setStreamMute(true);
        }

        public void onProgressChanged(SeekBar bar, int progress, boolean fromuser) {
            if (!fromuser) {
                return;
            }
            cancelProgressTimer();

            final int newposition = (int) ((mDuration * progress) / 1000);
            String time = generateTime(newposition);
            if (mInstantSeeking) {
                seekTo(newposition);
            }
            if (mCurrentTime != null)
                mCurrentTime.setText(time);
        }

        public void onStopTrackingTouch(SeekBar bar) {
            if (!mInstantSeeking) {
                seekTo((int) (mDuration * bar.getProgress() / 1000));
            }
            AudioMngHelper.instance(mContext).setStreamMute(false);
            mDragging = false;
        }
    };

    protected void setViewShowState(View view, int visibility) {
        if (view != null) {
            view.setVisibility(visibility);
        }
    }

    /**
     * 双击
     */
    protected GestureDetector gestureDetector = new GestureDetector(getContext(), new GestureDetector.SimpleOnGestureListener() {
        @Override
        public boolean onDoubleTap(MotionEvent e) {
            touchDoubleUp();
            return super.onDoubleTap(e);
        }
    });

    /**
     * 双击
     * 如果不需要，重载为空方法即可
     */
    protected void touchDoubleUp() {

    }

    /**
     * 亮度、进度、音频
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mIgnoreAllEvent) {
            cancelDismissControlViewTimer();
            cancelProgressTimer();
            return super.onTouchEvent(event);
        }
        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:

                touchSurfaceDown(x, y);

                break;
            case MotionEvent.ACTION_MOVE:
                float deltaX = x - mDownX;
                float deltaY = y - mDownY;
                float absDeltaX = Math.abs(deltaX);
                float absDeltaY = Math.abs(deltaY);
                if ((!mIsPortrait && mIsTouchWigetFull)
                        || (mIsTouchWidget && mIsPortrait)) {
                    if (!mChangePosition && !mChangeVolume && !mBrightness) {
                        touchSurfaceMoveFullLogic(absDeltaX, absDeltaY);
                    }
                }
                touchSurfaceMove(deltaX, deltaY, y);

                break;
            case MotionEvent.ACTION_UP:
                startDismissControlViewTimer();

                touchSurfaceUp();

                startProgressTimer();

                //不要和隐藏虚拟按键后，滑出虚拟按键冲突
                if (mHideKey && mShowVKey) {
                    return true;
                }
                break;
        }
        gestureDetector.onTouchEvent(event);

        return true;
    }

    protected void touchSurfaceMoveFullLogic(float absDeltaX, float absDeltaY) {

        int curWidth = mIsPortrait ? mScreenWidth : mScreenHeight;

        if (absDeltaX > mThreshold || absDeltaY > mThreshold) {
            cancelProgressTimer();
            if (absDeltaX >= mThreshold) {
                //防止全屏虚拟按键
                int screenWidth = CommonUtil.getScreenWidth(getContext());
                if (Math.abs(screenWidth - mDownX) > mSeekEndOffset) {
                    mChangePosition = true;
                    mDownPosition = getCurrentPosition();
                } else {
                    mShowVKey = true;
                }
            } else {
                int screenHeight = CommonUtil.getScreenHeight(getContext());
                boolean noEnd = Math.abs(screenHeight - mDownY) > mSeekEndOffset;
                if (mFirstTouch) {
                    mBrightness = (mDownX < curWidth * 0.5f) && noEnd;
                    mFirstTouch = false;
                }
                if (!mBrightness) {
                    mChangeVolume = noEnd;
                    mGestureDownVolume = AudioMngHelper.instance(mContext).getSystemCurrentVolume();
                }
                mShowVKey = !noEnd;
            }
        }

        // 提供方法控制是否执行以下动作
        int playerState = getPlayerState();
        if (playerState != PlayerState.CURRENT_STATE_PLAYING) {
            mChangePosition = false;
        }

        if (isLiveMode() || isForbidChangePosition) {
            mChangePosition = false;
        }

        if (isForbidAdjustBrightness) {
            mBrightness = false;
        }

        if (isForbidAdjustVolumn) {
            mChangeVolume = false;
        }

    }

    protected void touchSurfaceDown(float x, float y) {
        mTouchingProgressBar = true;
        mDownX = x;
        mDownY = y;
        mMoveY = 0;
        mChangeVolume = false;
        mChangePosition = false;
        mShowVKey = false;
        mBrightness = false;
        mFirstTouch = true;
    }

    protected void touchSurfaceMove(float deltaX, float deltaY, float y) {

        int curHeight = mIsPortrait ? mScreenHeight : mScreenWidth;
        int curWidth = mIsPortrait ? mScreenWidth : mScreenHeight;

        if (mChangePosition) {
            int totalTimeDuration = getDuration();
            mSeekTimePosition = (int) (mDownPosition + (deltaX * totalTimeDuration / curWidth) / mSeekRatio);
            if (mSeekTimePosition > totalTimeDuration)
                mSeekTimePosition = totalTimeDuration;
            String seekTime = CommonUtil.stringForTime(mSeekTimePosition);
            String totalTime = CommonUtil.stringForTime(totalTimeDuration);
            showProgressDialog(deltaX, seekTime, mSeekTimePosition, totalTime, totalTimeDuration);
        } else if (mChangeVolume) {

            deltaY = -deltaY;
            int max = AudioMngHelper.instance(mContext).getSystemMaxVolume();
            int deltaV = (int) (max * deltaY * 3 / curHeight);
            AudioMngHelper.instance(mContext).setVoice100(mGestureDownVolume + deltaV);
            int volumePercent = (int) (mGestureDownVolume * 100 / max + deltaY * 3 * 100 / curHeight);
            showVolumeDialog(-deltaY, volumePercent);
        } else if (!mChangePosition && mBrightness) {
            if (Math.abs(deltaY) > mThreshold) {
                float percent = (-deltaY / curHeight);
                onBrightnessSlide(percent);
                mDownY = y;
            }
        }
    }


    protected void touchSurfaceUp() {
//        if (mChangePosition) {
//            int duration = mMediaPlayer.getDuration();
//            int progress = mSeekTimePosition * 100 / (duration == 0 ? 1 : duration);
//            if (mProgressBar != null)
//                mProgressBar.setProgress(progress);
//        }
        if (!mChangePosition && !mChangeVolume && !mBrightness) {
            onClickUiToggle();
        }

        mTouchingProgressBar = false;
        dismissProgressDialog();
        dismissVolumeDialog();
        dismissBrightnessDialog();
        if (mChangePosition) {
            seekTo(mSeekTimePosition);

//            int duration = mMediaPlayer.getDuration();
//            int progress = mSeekTimePosition * 100 / (duration == 0 ? 1 : duration);
//            if (mProgressBar != null) {
//                mProgressBar.setProgress(progress);
//            }
//            if (mVideoAllCallBack != null && isCurrentMediaListener()) {
//                Debuger.printfLog("onTouchScreenSeekPosition");
//                mVideoAllCallBack.onTouchScreenSeekPosition(mOriginUrl, mTitle, this);
//            }
        } else if (mBrightness) {
//            if (mVideoAllCallBack != null && isCurrentMediaListener()) {
//                Debuger.printfLog("onTouchScreenSeekLight");
//                mVideoAllCallBack.onTouchScreenSeekLight(mOriginUrl, mTitle, this);
//            }
        } else if (mChangeVolume) {
//            if (mVideoAllCallBack != null && isCurrentMediaListener()) {
//                Debuger.printfLog("onTouchScreenSeekVolume");
//                mVideoAllCallBack.onTouchScreenSeekVolume(mOriginUrl, mTitle, this);
//            }
        }
    }

    /**
     * 滑动改变亮度
     *
     * @param percent
     */
    protected void onBrightnessSlide(float percent) {
        mBrightnessData = ((Activity) (mContext)).getWindow().getAttributes().screenBrightness;
        if (mBrightnessData <= 0.00f) {
            mBrightnessData = 0.50f;
        } else if (mBrightnessData < 0.01f) {
            mBrightnessData = 0.01f;
        }
        WindowManager.LayoutParams lpa = ((Activity) (mContext)).getWindow().getAttributes();
        lpa.screenBrightness = mBrightnessData + percent;
        if (lpa.screenBrightness > 1.0f) {
            lpa.screenBrightness = 1.0f;
        } else if (lpa.screenBrightness < 0.01f) {
            lpa.screenBrightness = 0.01f;
        }
        showBrightnessDialog(lpa.screenBrightness);
        ((Activity) (mContext)).getWindow().setAttributes(lpa);
    }

    /************************* 继承之后可自定义ui与显示隐藏 *************************/

    protected abstract void showWifiDialog();

    protected abstract void showProgressDialog(float deltaX,
                                               String seekTime, int seekTimePosition,
                                               String totalTime, int totalTimeDuration);

    protected abstract void dismissProgressDialog();

    protected abstract void showVolumeDialog(float deltaY, int volumePercent);

    protected abstract void dismissVolumeDialog();

    protected abstract void showBrightnessDialog(float percent);

    protected abstract void dismissBrightnessDialog();

    protected abstract void onClickUiToggle();

    protected abstract void hideAllWidget();

    protected abstract void changeUiToNormal();

    protected abstract void changeUiToPreparingShow();

    protected abstract void changeUiToPlayingShow();

    protected abstract void changeUiToPauseShow();

    protected abstract void changeUiToError();

    protected abstract void changeUiToCompleteShow();

    protected abstract void changeUiToPlayingBufferingShow();

    /**
     * 处理控制显示
     *
     * @param state
     */
    protected void resolveUIState(int state) {
        switch (state) {
            case PlayerState.CURRENT_STATE_NORMAL:
                changeUiToNormal();
                cancelDismissControlViewTimer();
                break;
            case PlayerState.CURRENT_STATE_PREPAREING:
                changeUiToPreparingShow();
                startDismissControlViewTimer();
                break;
            case PlayerState.CURRENT_STATE_PLAYING:
                changeUiToPlayingShow();
                startDismissControlViewTimer();
                break;
            case PlayerState.CURRENT_STATE_PAUSE:
                changeUiToPauseShow();
                cancelDismissControlViewTimer();
                break;
            case PlayerState.CURRENT_STATE_ERROR:
                changeUiToError();
                break;
            case PlayerState.CURRENT_STATE_AUTO_COMPLETE:
                showPlay();
                changeUiToCompleteShow();
                cancelDismissControlViewTimer();
                break;
            case PlayerState.CURRENT_STATE_PLAYING_BUFFERING_START:
                changeUiToPlayingBufferingShow();
                break;
        }
    }

    @Override
    public void setStateAndUi(int state) {
        super.setStateAndUi(state);

        switch (state) {
            case PlayerState.CURRENT_STATE_NORMAL:
                disableProgressBar();
                break;
            case PlayerState.CURRENT_STATE_PREPAREING:
                disableProgressBar();
                resetProgressAndTime();
                break;
            case PlayerState.CURRENT_STATE_PLAYING:
                showPause();
                enableProgressBar();
                startProgressTimer();
                break;
            case PlayerState.CURRENT_STATE_PAUSE:
                showPlay();
                disableProgressBar();
                startProgressTimer();
                break;
            case PlayerState.CURRENT_STATE_AUTO_COMPLETE:
                disableProgressBar();
                cancelProgressTimer();
                if (mProgressBar != null) {
                    mProgressBar.setProgress(1000);
                }
                if (mCurrentTime != null && mTotalTime != null) {
                    mCurrentTime.setText(mTotalTime.getText());
                }
                break;
        }
        resolveUIState(state);
    }

    private void enableProgressBar() {
        if (mProgressBar != null) {
            mProgressBar.setEnabled(true);
        }
    }

    private void disableProgressBar() {
        if (mProgressBar != null) {
            mProgressBar.setEnabled(false);
        }
    }

    protected void resetProgressAndTime() {
        if (mProgressBar == null || mTotalTime == null || mCurrentTime == null) {
            return;
        }
        mProgressBar.setProgress(0);
        mProgressBar.setSecondaryProgress(0);
        mCurrentTime.setText(CommonUtil.stringForTime(0));
        mTotalTime.setText(CommonUtil.stringForTime(0));

    }

    public void setVideoPath(String url) {
        setVideoPath(url, IVideoView.isLive(url));
    }

    @Override
    public void setVideoPath(String url, boolean isLiveStream) {
        super.setVideoPath(url, isLiveStream);
        if (mBottomProgressContainer != null) {
            mBottomProgressContainer.setVisibility(isLiveStream ? INVISIBLE : VISIBLE);
        }
    }

    /**
     * 禁用声音调节
     * forbid adjust volumn, if true.
     *
     * @param forbidAdjustVolumn
     */
    public void setForbidAdjustVolumn(boolean forbidAdjustVolumn) {
        isForbidAdjustVolumn = forbidAdjustVolumn;
    }

    /**
     * 禁用亮度调节
     * forbid adjust brightness, if true.
     *
     * @param forbidAdjustBrightness
     */
    public void setForbidAdjustBrightness(boolean forbidAdjustBrightness) {
        isForbidAdjustBrightness = forbidAdjustBrightness;
    }

    /**
     * 禁用进度调节
     *
     * @param forbidChangePosition
     */
    public void setForbidChangePosition(boolean forbidChangePosition) {
        isForbidChangePosition = forbidChangePosition;
    }

    public void setTouchWidget(boolean touchWidget) {
        mIsTouchWidget = touchWidget;
    }

    public void setTouchWigetFull(boolean touchWigetFull) {
        mIsTouchWigetFull = touchWigetFull;
    }

    public void setNeedShowWifiTip(boolean needShowWifiTip) {
        mNeedShowWifiTip = needShowWifiTip;
    }

    /**
     * 忽略所有touch事件
     *
     * @param ignoreAllEvent
     */
    public void setIgnoreAllEvent(boolean ignoreAllEvent) {
        mIgnoreAllEvent = ignoreAllEvent;
    }
}
