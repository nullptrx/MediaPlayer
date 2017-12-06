package tv.lycam.player;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

import com.pili.pldroid.player.PLMediaPlayer;

import java.util.Locale;

import tv.lycam.player.utils.AudioMngHelper;
import tv.lycam.player.utils.CommonUtil;

/**
 * @author 诸葛不亮
 * @version 1.0
 * @description
 */

public abstract class PLDroidControllerPlayer extends BasePLDroidPlayer {
    // 常量
    private static final int ROOT_LAYOUT_ID = R.layout.lycam_player_pldroid;
    private static final int PLAYER_ID = R.id.lycam_player_pldroid;
    private static final int IC_MEDIA_PAUSE_ID = R.drawable.lycam_player_media_pause;
    private static final int IC_MEDIA_PLAY_ID = R.drawable.lycam_player_media_start;
    private static final int PAUSE_BUTTON_ID = R.id.lycam_player_pause;
    private static final int MEDIACONTROLLER_PROGRESS_ID = R.id.lycam_player_progress;
    private static final int END_TIME_ID = R.id.lycam_player_duration;
    private static final int CURRENT_TIME_ID = R.id.lycam_player_time_current;
    private static final int BOTTOM_CONTAINER_ID = R.id.lycam_player_layout_bottom;
    private static final int TOP_CONTAINER_ID = R.id.lycam_player_layout_top;
    private static final int sDefaultTimeout = 3000;
    private static final int SEEK_TO_POST_DELAY_MILLIS = 200;

    private static final int FADE_OUT = 1;
    private static final int SHOW_PROGRESS = 2;
    // ui
    protected ImageButton mPauseButton;
    protected SeekBar mProgressBar;
    protected TextView mEndTime, mCurrentTime;
    //顶部和底部区域
    protected ViewGroup mTopContainer, mBottomContainer;
    // 界面逻辑
    private long mDuration;
    private boolean mShowing;
    private boolean mDragging;
    private boolean mInstantSeeking = false;
    private Runnable mLastSeekBarRunnable;

    public PLDroidControllerPlayer(@NonNull Context context) {
        super(context);
    }

    public PLDroidControllerPlayer(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public PLDroidControllerPlayer(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
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
        return PLAYER_ID;
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
        return IC_MEDIA_PAUSE_ID;
    }

    /**
     * 播放按钮图标
     *
     * @return
     */
    protected int getIconMediaPlayId() {
        return IC_MEDIA_PLAY_ID;
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

    @Override
    protected void onMediaPrepared(PLMediaPlayer plMediaPlayer, int what) {
        super.onMediaPrepared(plMediaPlayer, what);
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

        mEndTime = findViewById(getEndTimeLabelId());
        mCurrentTime = findViewById(getCurrentTimeLabelId());

        mBottomContainer = findViewById(getBottomContainerId());
        initCustomView();
    }

    /**
     * 初始化自定义的View及逻辑
     */
    protected void initCustomView() {
    }


    private void disableUnsupportedButtons() {
        try {
            if (mPauseButton != null && !mVideoView.canPause())
                mPauseButton.setEnabled(false);
        } catch (IncompatibleClassChangeError ex) {
        }
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

    /****Controller show/hide Listener  END****/

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            long pos;
            switch (msg.what) {
                case FADE_OUT:
                    hideController();
                    break;
                case SHOW_PROGRESS:
                    if (!isPlaying()) {
                        return;
                    }
                    pos = setProgress();
                    if (pos == -1) {
                        return;
                    }
                    if (!mDragging && mShowing) {
                        msg = obtainMessage(SHOW_PROGRESS);
                        sendMessageDelayed(msg, 1000 - (pos % 1000));
                        updatePausePlay();
                    }
                    break;
            }
        }
    };

    private long setProgress() {
        if (mVideoView == null || mDragging) {
            return 0;
        }

        long position = mVideoView.getCurrentPosition();
        long duration = mVideoView.getDuration();
        if (mProgressBar != null) {
            if (duration > 0) {
                long pos = 1000L * position / duration;
                mProgressBar.setProgress((int) pos);
            }
            int percent = mVideoView.getBufferPercentage();
            mProgressBar.setSecondaryProgress(percent * 10);
        }

        mDuration = duration;

        if (mEndTime != null)
            mEndTime.setText(generateTime(mDuration));
        if (mCurrentTime != null)
            mCurrentTime.setText(generateTime(position));

        return position;
    }

    private static String generateTime(long position) {
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

    @Override
    public boolean performClick() {
        return super.performClick();
    }

    @Override
    public boolean onTrackballEvent(MotionEvent ev) {
        showController(sDefaultTimeout);
        return false;
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        int keyCode = event.getKeyCode();
        if (event.getRepeatCount() == 0
                && (keyCode == KeyEvent.KEYCODE_HEADSETHOOK
                || keyCode == KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE || keyCode == KeyEvent.KEYCODE_SPACE)) {
            doPauseResume();
            showController(sDefaultTimeout);
            if (mPauseButton != null)
                mPauseButton.requestFocus();
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_MEDIA_STOP) {
            if (mVideoView.isPlaying()) {
                mVideoView.pause();
                updatePausePlay();
            }
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_BACK
                || keyCode == KeyEvent.KEYCODE_MENU) {
            hideController();
//            return true;
        } else {
            showController(sDefaultTimeout);
        }
        return super.dispatchKeyEvent(event);
    }

    private OnClickListener mPauseListener = new OnClickListener() {
        public void onClick(View v) {
            doPauseResume();
            showController(sDefaultTimeout);
        }
    };

    private void updatePausePlay() {
        if (mPauseButton == null)
            return;

        if (mVideoView.isPlaying())
            mPauseButton.setImageResource(IC_MEDIA_PAUSE_ID);
        else
            mPauseButton.setImageResource(IC_MEDIA_PLAY_ID);
    }

    private void doPauseResume() {
        if (mPauseButton == null)
            return;
        if (mVideoView.isPlaying()) {
            mPauseButton.setImageResource(IC_MEDIA_PAUSE_ID);
            mVideoView.pause();
        } else {
            mVideoView.start();
            mPauseButton.setImageResource(IC_MEDIA_PLAY_ID);
        }
    }

    private SeekBar.OnSeekBarChangeListener mSeekListener = new SeekBar.OnSeekBarChangeListener() {

        public void onStartTrackingTouch(SeekBar bar) {
            mDragging = true;
            showController(3600000);
            mHandler.removeMessages(SHOW_PROGRESS);
            if (mInstantSeeking)
                AudioMngHelper.instance(mContext).setStreamMute(true);
        }

        public void onProgressChanged(SeekBar bar, int progress, boolean fromuser) {
            if (!fromuser) {
                return;
            }


            final long newposition = (long) (mDuration * progress) / 1000;
            String time = generateTime(newposition);
            if (mInstantSeeking) {
                mHandler.removeCallbacks(mLastSeekBarRunnable);
                mLastSeekBarRunnable = new Runnable() {
                    @Override
                    public void run() {
                        seekTo(newposition);
                    }
                };
                mHandler.postDelayed(mLastSeekBarRunnable, SEEK_TO_POST_DELAY_MILLIS);
            }
            if (mCurrentTime != null)
                mCurrentTime.setText(time);
        }

        public void onStopTrackingTouch(SeekBar bar) {
            if (!mInstantSeeking)
                seekTo(mDuration * bar.getProgress() / 1000);

            showController(sDefaultTimeout);
            mHandler.removeMessages(SHOW_PROGRESS);
            AudioMngHelper.instance(mContext).setStreamMute(false);
            mDragging = false;
            mHandler.sendEmptyMessageDelayed(SHOW_PROGRESS, 1000);
        }
    };

    /**
     * Show the controller on screen. It will go away automatically after
     * 'timeout' milliseconds of inactivity.
     *
     * @param timeout The timeout in milliseconds. Use 0 to showController the controller until hideController() is called.
     */
    private void showController(int timeout) {
        if (!mShowing) {
            if (mPauseButton != null)
                mPauseButton.requestFocus();
            disableUnsupportedButtons();

            updateControllerVisibility(View.VISIBLE);
            mShowing = true;
            if (mShownListener != null)
                mShownListener.onShown();
        }
        updatePausePlay();
        mHandler.sendEmptyMessage(SHOW_PROGRESS);

        if (timeout != 0) {
            mHandler.removeMessages(FADE_OUT);
            mHandler.sendMessageDelayed(mHandler.obtainMessage(FADE_OUT),
                    timeout);
        }
    }


    protected void updateControllerVisibility(int visiblility) {
        setViewShowState(mBottomContainer, visiblility);
    }

    public void hideController() {
        if (mShowing) {
            mHandler.removeMessages(SHOW_PROGRESS);
            updateControllerVisibility(View.GONE);
            mShowing = false;
            if (mHiddenListener != null)
                mHiddenListener.onHidden();
        }
    }

    protected void setViewShowState(View view, int visibility) {
        if (view != null) {
            view.setVisibility(visibility);
        }
    }

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
    protected boolean mIsTouchWiget = true;

    //是否支持全屏滑动触摸有效
    protected boolean mIsTouchWigetFull = true;

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
        performClick();
        showController(sDefaultTimeout);
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
                        || (mIsTouchWiget && mIsPortrait)) {
                    if (!mChangePosition && !mChangeVolume && !mBrightness) {
                        touchSurfaceMoveFullLogic(absDeltaX, absDeltaY);
                    }
                }
                touchSurfaceMove(deltaX, deltaY, y);

                break;
            case MotionEvent.ACTION_UP:

                touchSurfaceUp();

//                startProgressTimer();

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
//            cancelProgressTimer();
            if (absDeltaX >= mThreshold) {
                //防止全屏虚拟按键
                int screenWidth = CommonUtil.getScreenWidth(getContext());
                if (Math.abs(screenWidth - mDownX) > mSeekEndOffset) {
                    mChangePosition = true;
                    mDownPosition = (int) mVideoView.getCurrentPosition();
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
            int totalTimeDuration = (int) mVideoView.getDuration();
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
//            int duration = (int) mVideoView.getDuration();
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

//            int duration = mVideoView.getDuration();
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
}
