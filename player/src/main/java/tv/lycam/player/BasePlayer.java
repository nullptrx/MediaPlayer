package tv.lycam.player;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.InflateException;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import com.alivc.player.ScalableType;

import tv.lycam.player.callback.IMediaStatus;
import tv.lycam.player.ratio.RatioFrameLayout;
import tv.lycam.player.utils.CommonUtil;
import tv.lycam.player.widget.AliVideoView;
import tv.lycam.player.widget.IVideoView;
import tv.lycam.player.widget.IjkVideoView;


/**
 * @author 诸葛不亮
 * @version 1.0
 * @description
 */

public abstract class BasePlayer extends RatioFrameLayout implements IMediaStatus {
    private int mPlayerType;
    //上下文
    protected Context mContext;
    //屏幕宽度
    protected int mScreenWidth;
    //屏幕高度
    protected int mScreenHeight;

    private ViewGroup mSurfaceContainer;

    private IVideoView mMediaPlayer;

    private boolean mPausing;

    private IMediaStatus mMediaStatus;

    // 当前播放状态
    protected int mCurrentState = PlayerState.CURRENT_STATE_NORMAL;
    // 是否自动配置常亮属性
    private boolean isAutoKeepScreen = true;

    public BasePlayer(@NonNull Context context) {
        this(context, null);
    }

    public BasePlayer(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BasePlayer(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.StandardPlayer, defStyleAttr, 0);
        //获取参考边
        mPlayerType = typedArray.getInt(R.styleable.StandardPlayer_player, IVideoView.TYPE_ALIPLAYER);
        typedArray.recycle();
        init(context);
    }

    protected void init(Context context) {
        this.mContext = context;
        initInflate(mContext);
        if (isInEditMode())
            return;
        mScreenWidth = context.getResources().getDisplayMetrics().widthPixels;
        mScreenHeight = context.getResources().getDisplayMetrics().heightPixels;

        mSurfaceContainer = findViewById(getPlayerId());

        if (mSurfaceContainer == null) {
            throw new InflateException("The layout has not include player Container!");
        }
        addTextureView(mPlayerType);

    }

    private void addTextureView(@IntRange(from = IVideoView.TYPE_ALIPLAYER, to = IVideoView.TYPE_IJKPLAYER) int type) {
        if (mSurfaceContainer.getChildCount() > 0) {
            mSurfaceContainer.removeAllViews();
        }
        switch (type) {
            case IVideoView.TYPE_ALIPLAYER:
            default:
                mMediaPlayer = new AliVideoView(mContext);
                break;
            case IVideoView.TYPE_IJKPLAYER:
                mMediaPlayer = new IjkVideoView(mContext);
                break;
        }
        mMediaPlayer.setIMediaStatus(this);
        if (mSurfaceContainer instanceof RelativeLayout) {
            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
            mSurfaceContainer.addView(mMediaPlayer, layoutParams);
        } else if (mSurfaceContainer instanceof FrameLayout) {
            FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            layoutParams.gravity = Gravity.CENTER;
            mSurfaceContainer.addView(mMediaPlayer, layoutParams);
        }
    }

    /**
     * 切换播放器类型
     *
     * @param type
     */
    public void setPlayerType(@IntRange(from = IVideoView.TYPE_ALIPLAYER, to = IVideoView.TYPE_IJKPLAYER) int type) {
        mPlayerType = type;
        addTextureView(type);
    }

    public int getPlayerType() {
        return mPlayerType;
    }

    public void setScalableType(ScalableType scalableType) {
        if (mCurrentState == PlayerState.CURRENT_STATE_NORMAL) {
            return;
        }
        if (mMediaPlayer != null) {
            mMediaPlayer.setScalableType(scalableType);
        }
    }

    private void initInflate(Context context) {
        try {
            View.inflate(context, getRootLayoutId(), this);
        } catch (InflateException e) {
            e.printStackTrace();
        }
    }

    public int getPlayerState() {
        return this.mMediaPlayer != null ? this.mCurrentState : PlayerState.CURRENT_STATE_NORMAL;
    }

    /**
     * 当前UI
     */
    protected abstract int getRootLayoutId();

    protected abstract int getPlayerId();


    public void setVideoPath(String url, boolean isLiveStream) {
        if (mMediaPlayer != null) {
            mMediaPlayer.setVideoPath(url, isLiveStream);
        }
    }

    /**
     * @param decoderType 解码器类型。0代表硬件解码器；1代表软件解码器。
     */
    public void setDefaultDecoder(int decoderType) {
        if (mMediaPlayer != null) {
            mMediaPlayer.setDefaultDecoder(decoderType);
        }
    }

    public void pause() {
        if (mMediaPlayer != null) {
            mMediaPlayer.pause();
        }
    }

    public void resume() {
        if (mMediaPlayer != null && mCurrentState == PlayerState.CURRENT_STATE_PAUSE) {
            mMediaPlayer.resume();
        }
    }

    public void start() {
        if (mMediaPlayer != null) {
            mMediaPlayer.start();
        }
        if (isAutoKeepScreen) {
            Activity activity = CommonUtil.getActivityContext(mContext);
            if (activity != null) {
                activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            }
        }
    }

    public void stop() {
        if (isAutoKeepScreen) {
            Activity activity = CommonUtil.getActivityContext(mContext);
            if (activity != null) {
                activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            }
        }
        if (mMediaPlayer != null) {
            mMediaPlayer.stop();
        }
    }

    public void destroy() {
        if (mMediaPlayer != null) {
            mMediaPlayer.destroy();
        }
    }

    public void setMediaStatus(IMediaStatus mediaStatus) {
        mMediaStatus = mediaStatus;
    }

    public void onMediaPrepared() {
        if (mMediaStatus != null) {
            mMediaStatus.onMediaPrepared();
        }
    }

    public void onMediaInfo(int what, int extra) {
        if (mMediaStatus != null) {
            mMediaStatus.onMediaInfo(what, extra);
        }
    }

    public void onMediaError(int errorCode, String msg) {
        if (mMediaStatus != null) {
            mMediaStatus.onMediaError(errorCode, msg);
        }
    }

    public void onMediaCompleted() {
        if (mMediaStatus != null) {
            mMediaStatus.onMediaCompleted();
        }
    }

    public void onMediaSeekCompleted() {
        if (mMediaStatus != null) {
            mMediaStatus.onMediaSeekCompleted();
        }
    }

    // 更新进度时间
    public void onMediaFrameInfo() {
        if (mMediaStatus != null) {
            mMediaStatus.onMediaFrameInfo();
        }
    }

    public void onMediaBufferingUpdate(int percent) {
        if (mMediaStatus != null) {
            mMediaStatus.onMediaBufferingUpdate(percent);
        }
    }

    public void onMediaVideoSizeChange(int width, int height) {
        if (mMediaStatus != null) {
            mMediaStatus.onMediaVideoSizeChange(width, height);
        }
    }

    public void setStateAndUi(int state) {
        mCurrentState = state;
        if (mMediaStatus != null) {
            mMediaStatus.setStateAndUi(state);
        }
    }

    private int stateToSave;

    @Override
    public Parcelable onSaveInstanceState() {
        //begin boilerplate code that allows parent classes to save state
        Parcelable superState = super.onSaveInstanceState();

        SavedState ss = new SavedState(superState);
        //end

        ss.stateToSave = this.stateToSave;

        return ss;
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        //begin boilerplate code so parent classes can restore state
        if (!(state instanceof SavedState)) {
            super.onRestoreInstanceState(state);
            return;
        }

        SavedState ss = (SavedState) state;
        super.onRestoreInstanceState(ss.getSuperState());
        //end

        this.stateToSave = ss.stateToSave;
    }

    static class SavedState extends BaseSavedState {
        int stateToSave;

        SavedState(Parcelable superState) {
            super(superState);
        }

        private SavedState(Parcel in) {
            super(in);
            this.stateToSave = in.readInt();
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeInt(this.stateToSave);
        }

        //required field that makes Parcelables from a Parcel
        public static final Parcelable.Creator<SavedState> CREATOR =
                new Parcelable.Creator<SavedState>() {
                    public SavedState createFromParcel(Parcel in) {
                        return new SavedState(in);
                    }

                    public SavedState[] newArray(int size) {
                        return new SavedState[size];
                    }
                };
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        int orientation = getResources().getConfiguration().orientation;
        if (orientation == Configuration.ORIENTATION_PORTRAIT) {                //转为竖屏了。
            //显示状态栏
            Activity activity = CommonUtil.getActivityContext(mContext);
            if (activity != null) {
                activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            }
            if (mMediaPlayer != null) {
                ViewGroup.LayoutParams params = mMediaPlayer.getLayoutParams();
                params.width = ViewGroup.LayoutParams.WRAP_CONTENT;
                params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
                mMediaPlayer.setLayoutParams(params);
            }
        } else if (orientation == Configuration.ORIENTATION_LANDSCAPE) {                //转到横屏了。
            //隐藏状态栏
            Activity activity = CommonUtil.getActivityContext(mContext);
            if (activity != null) {
                activity.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
            }
            if (mMediaPlayer != null) {
                ViewGroup.LayoutParams params = mMediaPlayer.getLayoutParams();
                params.width = ViewGroup.LayoutParams.MATCH_PARENT;
                params.height = ViewGroup.LayoutParams.MATCH_PARENT;
                mMediaPlayer.setLayoutParams(params);
            }

        }
    }

    /**
     * 播放时常亮，停止取消常亮，默认true
     *
     * @param autoKeepScreen
     */
    public void setAutoKeepScreen(boolean autoKeepScreen) {
        isAutoKeepScreen = autoKeepScreen;
    }

    public void seekTo(int time) {
        if (mMediaPlayer != null) {
            mMediaPlayer.seekTo(time);
        }
    }

    public boolean isPlaying() {
        return mMediaPlayer != null && mMediaPlayer.isPlaying();
    }

    public void savePlayerState() {
        if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
            //we pause the player for not playing on the background
            // 不可见，暂停播放器
            pause();
            mPausing = true;
        }
    }

    public void restorePlayerState() {
        if (mPausing) {
            //we pause the player for not playing on the background
            // 不可见，暂停播放器
            resume();
            mPausing = false;
        }
    }

    public int getCurrentPosition() {
        return mMediaPlayer != null ? mMediaPlayer.getCurrentPosition() : 0;
    }

    public int getDuration() {
        return mMediaPlayer != null ? mMediaPlayer.getDuration() : 0;
    }

    public int getBufferPosition() {
        return mMediaPlayer != null ? mMediaPlayer.getBufferPosition() : 0;
    }

    public boolean isLiveMode() {
        return mMediaPlayer.isLiveMode();
    }

    public void setMute(boolean mute) {
        if (mMediaPlayer != null) {
            mMediaPlayer.setMuteMode(mute);
        }
    }

    public String getVideoPath() {
        return mMediaPlayer != null ? mMediaPlayer.getVideoPath() : "";
    }

    public void enableNativeLog() {
        if (mMediaPlayer != null) {
            mMediaPlayer.enableNativeLog();
        }
    }

    public void disableNativeLog() {
        if (mMediaPlayer != null) {
            mMediaPlayer.disableNativeLog();
        }
    }

}
