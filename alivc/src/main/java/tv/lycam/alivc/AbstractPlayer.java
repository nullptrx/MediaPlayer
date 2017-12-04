package tv.lycam.alivc;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Matrix;
import android.graphics.SurfaceTexture;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.InflateException;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import com.alivc.player.AliVcMediaPlayer;
import com.alivc.player.MediaPlayer;

import javax.microedition.khronos.opengles.GL10;

import tv.lycam.alivc.ratio.RatioFrameLayout;
import tv.lycam.alivc.utils.CommonUtil;
import tv.lycam.alivc.utils.Debugger;


/**
 * @author 诸葛不亮
 * @version 1.0
 * @description
 */

public abstract class AbstractPlayer extends RatioFrameLayout {

    public static final String TAG = "AlivcPlayer";
    //日志打印
    protected boolean DEBUG = true;

    // 默认缩放模式
    protected MediaPlayer.VideoScalingMode DEFAULT_ASPECTRATIO = MediaPlayer.VideoScalingMode.VIDEO_SCALING_MODE_SCALE_TO_FIT;

    //上下文
    protected Context mContext;
    //屏幕宽度
    protected int mScreenWidth;

    //屏幕高度
    protected int mScreenHeight;
    // 界面
    private TextureView mTextureView;
    private SurfaceView mSurfaceView;
    private ViewGroup mSurfaceContainer;

    protected AliVcMediaPlayer mMediaPlayer;

    protected String mStreamUrl;

    // 当前播放状态
    protected int mCurrentState = PlayerState.CURRENT_STATE_NORMAL;
    // 备份缓存前的播放状态
    protected int mBackUpPlayingBufferState = -1;
    // 变换
    private int mTransformSize = 0;
    // 是否直播流
    protected boolean isLiveStream;
    // 是否自动配置常亮属性
    private boolean isAutoKeepScreen = true;


    public AbstractPlayer(@NonNull Context context) {
        super(context);
        init(context);
    }

    public AbstractPlayer(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public AbstractPlayer(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private TextureView.SurfaceTextureListener mSurfaceTextureListener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int width, int height) {
            if (mMediaPlayer != null) {
                Surface surface = new Surface(surfaceTexture);
                mMediaPlayer.setVideoSurface(surface);
                surface.release();
            }
            resolveTransform();
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int width, int height) {
            if (mMediaPlayer != null) {
                mMediaPlayer.setSurfaceChanged();
            }
            resolveTransform();
        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            return true;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surface) {
            resolveTransform();
        }
    };

    private SurfaceHolder.Callback mSurfaceHolderCallback = new SurfaceHolder.Callback() {
        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            holder.setKeepScreenOn(true);
            if (mMediaPlayer != null) {
                mMediaPlayer.setVideoSurface(holder.getSurface());
            }
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            if (mMediaPlayer != null) {
                mMediaPlayer.setSurfaceChanged();
            }
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
        }
    };

    protected void init(Context context) {
        this.mContext = context;
        initInflate(mContext);
        if (isInEditMode())
            return;
        mScreenWidth = context.getResources().getDisplayMetrics().widthPixels;
        mScreenHeight = context.getResources().getDisplayMetrics().heightPixels;
        Activity activity = CommonUtil.getActivityContext(context);
        if (activity != null) {
            activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
        mSurfaceContainer = findViewById(getPlayerId());

        if (mSurfaceContainer == null) {
            throw new InflateException("The layout has not include player Container!");
        }
//        if (mTextureView != null) {
//            mTextureView.setSurfaceTextureListener(mSurfaceTextureListener);
//            mTextureView.setLayerType(TextureView.LAYER_TYPE_SOFTWARE, null);
//        } else if (mSurfaceView != null) {
//            mSurfaceView.getHolder().addCallback(mSurfaceHolderCallback);
//        }
        addTextureView();
//        addSurfaceView();
        initVodPlayer(mContext);

        setStateAndUi(PlayerState.CURRENT_STATE_NORMAL);
    }

    private void addTextureView() {
        if (mSurfaceContainer.getChildCount() > 0) {
            mSurfaceContainer.removeAllViews();
        }
        mTextureView = new TextureView(mContext);
//        mTextureView.setLayerType(TextureView.LAYER_TYPE_SOFTWARE, null);
        mTextureView.setSurfaceTextureListener(mSurfaceTextureListener);
        if (mSurfaceContainer instanceof RelativeLayout) {
            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
            mSurfaceContainer.addView(mTextureView, layoutParams);
        } else if (mSurfaceContainer instanceof FrameLayout) {
            FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            layoutParams.gravity = Gravity.CENTER;
            mSurfaceContainer.addView(mTextureView, layoutParams);
        }
    }

    private void addSurfaceView() {
        if (mSurfaceContainer.getChildCount() > 0) {
            mSurfaceContainer.removeAllViews();
        }
        mSurfaceView = new SurfaceView(mContext);
        mSurfaceView.setLayerType(TextureView.LAYER_TYPE_SOFTWARE, null);
        mSurfaceView.getHolder().addCallback(mSurfaceHolderCallback);
        if (mSurfaceContainer instanceof RelativeLayout) {
            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
            mSurfaceContainer.addView(mSurfaceView, layoutParams);
        } else if (mSurfaceContainer instanceof FrameLayout) {
            FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            layoutParams.gravity = Gravity.CENTER;
            mSurfaceContainer.addView(mSurfaceView, layoutParams);
        }
    }

    /**
     * 处理镜像旋转
     * 注意，暂停时
     */
    protected void resolveTransform() {
        if (mTextureView == null) {
            return;
        }
        switch (mTransformSize) {
            case 1: {
                Matrix transform = new Matrix();
                transform.setScale(-1, 1, mTextureView.getWidth() / 2, 0);
                mTextureView.setTransform(transform);
//                mChangeTransform.setText("左右镜像");
                mTextureView.invalidate();
            }
            break;
            case 2: {
                Matrix transform = new Matrix();
                transform.setScale(1, -1, 0, mTextureView.getHeight() / 2);
                mTextureView.setTransform(transform);
//                mChangeTransform.setText("上下镜像");
                mTextureView.invalidate();
            }
            break;
            case 0: {
                Matrix transform = new Matrix();
                transform.setScale(1, 1, mTextureView.getWidth() / 2, 0);
                mTextureView.setTransform(transform);
//                mChangeTransform.setText("旋转镜像");
                mTextureView.invalidate();
            }
            break;
        }
    }

    public void setmTransformSize(int transformSize) {
        if (mCurrentState == PlayerState.CURRENT_STATE_NORMAL) {
            return;
        }
        this.mTransformSize = transformSize;
        resolveTransform();
    }

    private int createTextureID() {
        int[] texture = new int[1];

        GLES20.glGenTextures(1, texture, 0);
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, texture[0]);
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_LINEAR);
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GL10.GL_TEXTURE_WRAP_S, GL10.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GL10.GL_TEXTURE_WRAP_T, GL10.GL_CLAMP_TO_EDGE);

        return texture[0];
    }

    private void initVodPlayer(Context context) {
        // 经测试，此处的textureid可随意数字，原因尚不知。
        if (mTextureView != null) {
            SurfaceTexture surfaceTexture = new SurfaceTexture(createTextureID());
            Surface surface = new Surface(surfaceTexture);
            mMediaPlayer = new AliVcMediaPlayer(context, surface);
            surface.release();
            surfaceTexture.release();
        } else {
            mMediaPlayer = new AliVcMediaPlayer(context, mSurfaceView);
        }

        //音频数据回调接口，在需要处理音频时使用，如拿到视频音频，然后绘制音柱。
        mMediaPlayer.setPcmDataListener(new MediaPlayer.MediaPlayerPcmDataListener() {
            @Override
            public void onPcmData(byte[] bytes, int i) {

            }
        });
        mMediaPlayer.setPreparedListener(mOnBasePreparedListener);
        mMediaPlayer.setInfoListener(mOnBaseInfoListener);
        mMediaPlayer.setVideoSizeChangeListener(mOnBaseVideoSizeChangeListener);
        mMediaPlayer.setBufferingUpdateListener(mOnBaseBufferingUpdateListener);
        mMediaPlayer.setCompletedListener(mOnBaseCompletedListener);
        mMediaPlayer.setErrorListener(mOnBaseErrorListener);
        mMediaPlayer.setSeekCompleteListener(mOnBaseSeekCompleteListener);
        mMediaPlayer.setFrameInfoListener(mOnBaseFrameInfoListener);
        mMediaPlayer.setStoppedListener(mOnBaseStoppedListener);
        mMediaPlayer.setVideoScalingMode(DEFAULT_ASPECTRATIO);
        if (DEBUG) {
            mMediaPlayer.enableNativeLog();
        } else {
            mMediaPlayer.disableNativeLog();
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
        this.mStreamUrl = url;
        this.isLiveStream = isLiveStream;
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
            setStateAndUi(PlayerState.CURRENT_STATE_PAUSE);
        }
    }

    public void resume() {
        if (mCurrentState == PlayerState.CURRENT_STATE_PAUSE) {
            if (mMediaPlayer != null) {
                mMediaPlayer.play();
                setStateAndUi(PlayerState.CURRENT_STATE_PLAYING);
            }
        }
    }

    protected void prepareAndPlay() {
        if (TextUtils.isEmpty(mStreamUrl)) {
            return;
        }
        if (mMediaPlayer != null) {
            mMediaPlayer.setMediaType(isLiveStream ? MediaPlayer.MediaType.Live : MediaPlayer.MediaType.Vod);
            mMediaPlayer.prepareAndPlay(mStreamUrl);
            setStateAndUi(PlayerState.CURRENT_STATE_PREPAREING);
        }
        if (isAutoKeepScreen) {
            Activity activity = CommonUtil.getActivityContext(mContext);
            if (activity != null) {
                activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            }
        }
    }

    protected void stop() {
        if (isAutoKeepScreen) {
            Activity activity = CommonUtil.getActivityContext(mContext);
            if (activity != null) {
                activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            }
        }

        if (mMediaPlayer != null) {
            mMediaPlayer.stop();
            setStateAndUi(PlayerState.CURRENT_STATE_NORMAL);
        }
    }

    protected void destroy() {
        if (mMediaPlayer != null) {
            mMediaPlayer.releaseVideoSurface();
            mMediaPlayer.stop();
            mMediaPlayer.destroy();
        }
    }


    protected void onMediaPrepared() {
    }

    protected void onMediaInfo(int what, int extra) {
    }

    protected void onMediaError(int errorCode, String msg) {
    }

    protected void onMediaCompleted() {
    }

    protected void onMediaSeekCompleted() {
    }

    // 更新进度时间
    protected void onMediaFrameInfo() {
    }

    protected void onMediaBufferingUpdate(int percent) {
    }

    protected void onMediaVideoSizeChange(int width, int height) {
    }

    //准备完成时触发
    private AliVcMediaPlayer.MediaPlayerPreparedListener mOnBasePreparedListener = new AliVcMediaPlayer.MediaPlayerPreparedListener() {
        @Override
        public void onPrepared() {
//            if (mCurrentState != PlayerState.CURRENT_STATE_PREPAREING) return;
            onMediaPrepared();
            mMediaPlayer.play();
            setStateAndUi(PlayerState.CURRENT_STATE_PLAYING);
            if (mOnPreparedListener != null) {
                mOnPreparedListener.onPrepared();
            }
        }
    };

    private AliVcMediaPlayer.MediaPlayerInfoListener mOnBaseInfoListener = new AliVcMediaPlayer.MediaPlayerInfoListener() {
        @Override
        public void onInfo(int what, int extra) {
            Debugger.printfLog(TAG, "OnInfo, what = " + what + ", extra = " + extra);
            switch (what) {
                case AliVcMediaPlayer.MEDIA_INFO_BUFFERING_START:
                    mBackUpPlayingBufferState = mCurrentState;
                    //避免在onPrepared之前就进入了buffering，导致一只loading
                    if (mCurrentState != PlayerState.CURRENT_STATE_PREPAREING && mCurrentState > 0)
                        setStateAndUi(PlayerState.CURRENT_STATE_PLAYING_BUFFERING_START);
                    break;
                case AliVcMediaPlayer.MEDIA_INFO_BUFFERING_END:
                    if (mBackUpPlayingBufferState != -1) {
                        if (mCurrentState != PlayerState.CURRENT_STATE_PREPAREING && mCurrentState > 0)
                            setStateAndUi(mBackUpPlayingBufferState);

                        mBackUpPlayingBufferState = -1;
                    }
                    break;
                case AliVcMediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START:
                    Debugger.printfLog(TAG, "First video render time: " + extra + "ms");
                    break;
                default:
                    break;
            }
            onMediaInfo(what, extra);
            if (mOnInfoListener != null) {
                mOnInfoListener.onInfo(what, extra);
            }
        }

    };

    //错误发生时触发，错误码见接口文档
    private AliVcMediaPlayer.MediaPlayerErrorListener mOnBaseErrorListener = new AliVcMediaPlayer.MediaPlayerErrorListener() {
        @Override
        public void onError(int errorCode, String msg) {
            setStateAndUi(PlayerState.CURRENT_STATE_ERROR);
            mMediaPlayer.stop();
            onMediaError(errorCode, msg);
            if (mOnErrorListener != null) {
                mOnErrorListener.onError(errorCode, msg);
            }
        }
    };

    //视频正常播放完成时触发
    private AliVcMediaPlayer.MediaPlayerCompletedListener mOnBaseCompletedListener = new AliVcMediaPlayer.MediaPlayerCompletedListener() {
        @Override
        public void onCompleted() {
            setStateAndUi(PlayerState.CURRENT_STATE_AUTO_COMPLETE);
            onMediaCompleted();
            if (mOnCompletedListener != null) {
                mOnCompletedListener.onCompleted();
            }
        }
    };

    private AliVcMediaPlayer.MediaPlayerBufferingUpdateListener mOnBaseBufferingUpdateListener = new AliVcMediaPlayer.MediaPlayerBufferingUpdateListener() {
        @Override
        public void onBufferingUpdateListener(int percent) {
            onMediaBufferingUpdate(percent);
            if (mOnBufferingUpdateListener != null) {
                mOnBufferingUpdateListener.onBufferingUpdateListener(percent);
            }
        }
    };

    private AliVcMediaPlayer.MediaPlayerVideoSizeChangeListener mOnBaseVideoSizeChangeListener = new AliVcMediaPlayer.MediaPlayerVideoSizeChangeListener() {
        @Override
        public void onVideoSizeChange(int width, int height) {
            onMediaVideoSizeChange(width, height);
            if (mOnVideoSizeChangeListener != null) {
                mOnVideoSizeChangeListener.onVideoSizeChange(width, height);
            }
        }
    };

    private AliVcMediaPlayer.MediaPlayerSeekCompleteListener mOnBaseSeekCompleteListener = new AliVcMediaPlayer.MediaPlayerSeekCompleteListener() {
        @Override
        public void onSeekCompleted() {
            onMediaSeekCompleted();
        }
    };

    //首帧显示时触发
    private MediaPlayer.MediaPlayerFrameInfoListener mOnBaseFrameInfoListener = new MediaPlayer.MediaPlayerFrameInfoListener() {
        @Override
        public void onFrameInfoListener() {
            onMediaFrameInfo();
            if (mOnFrameInfoListener != null) {
                mOnFrameInfoListener.onFrameInfoListener();
            }
        }
    };

    private MediaPlayer.MediaPlayerStoppedListener mOnBaseStoppedListener = new MediaPlayer.MediaPlayerStoppedListener() {
        @Override
        public void onStopped() {
            mCurrentState = PlayerState.CURRENT_STATE_NORMAL;
        }
    };


    private AliVcMediaPlayer.MediaPlayerPreparedListener mOnPreparedListener;
    private AliVcMediaPlayer.MediaPlayerInfoListener mOnInfoListener;
    private AliVcMediaPlayer.MediaPlayerErrorListener mOnErrorListener;
    private AliVcMediaPlayer.MediaPlayerCompletedListener mOnCompletedListener;
    private AliVcMediaPlayer.MediaPlayerBufferingUpdateListener mOnBufferingUpdateListener;
    private AliVcMediaPlayer.MediaPlayerVideoSizeChangeListener mOnVideoSizeChangeListener;
    private AliVcMediaPlayer.MediaPlayerFrameInfoListener mOnFrameInfoListener;


    public void setOnPreparedListener(AliVcMediaPlayer.MediaPlayerPreparedListener onPreparedListener) {
        mOnPreparedListener = onPreparedListener;
    }

    public void setOnInfoListener(AliVcMediaPlayer.MediaPlayerInfoListener onInfoListener) {
        mOnInfoListener = onInfoListener;
    }

    public void setOnErrorListener(AliVcMediaPlayer.MediaPlayerErrorListener onErrorListener) {
        mOnErrorListener = onErrorListener;
    }

    public void setOnCompletedListener(AliVcMediaPlayer.MediaPlayerCompletedListener onCompletedListener) {
        mOnCompletedListener = onCompletedListener;
    }

    public void setOnBufferingUpdateListener(AliVcMediaPlayer.MediaPlayerBufferingUpdateListener onBufferingUpdateListener) {
        mOnBufferingUpdateListener = onBufferingUpdateListener;
    }

    public void setOnVideoSizeChangeListener(AliVcMediaPlayer.MediaPlayerVideoSizeChangeListener onVideoSizeChangeListener) {
        mOnVideoSizeChangeListener = onVideoSizeChangeListener;
    }

    public void setOnFrameInfoListener(AliVcMediaPlayer.MediaPlayerFrameInfoListener onFrameInfoListener) {
        mOnFrameInfoListener = onFrameInfoListener;
    }

    protected void setStateAndUi(int state) {
        mCurrentState = state;
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


    public static int getWight(Context context) {
        DisplayMetrics dm = new DisplayMetrics();
        Activity activity = CommonUtil.getActivityContext(context);
        if (activity != null) {
            activity.getWindowManager().getDefaultDisplay().getMetrics(dm);
        }
        int screenWidth = dm.widthPixels;
        return screenWidth;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        addTextureView();
        int orientation = getResources().getConfiguration().orientation;
        if (orientation == Configuration.ORIENTATION_PORTRAIT) {                //转为竖屏了。
            //显示状态栏
            Activity activity = CommonUtil.getActivityContext(mContext);
            if (activity != null) {
                activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            }
            mTextureView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);

            //设置view的布局，宽高之类
            ViewGroup.LayoutParams surfaceViewLayoutParams = getLayoutParams();
            surfaceViewLayoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT;
            surfaceViewLayoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;

        } else if (orientation == Configuration.ORIENTATION_LANDSCAPE) {                //转到横屏了。
            //隐藏状态栏
            Activity activity = CommonUtil.getActivityContext(mContext);
            if (activity != null) {
                activity.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
            }
            mTextureView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_FULLSCREEN);
            //设置view的布局，宽高
            ViewGroup.LayoutParams surfaceViewLayoutParams = getLayoutParams();
            surfaceViewLayoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT;
            surfaceViewLayoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;

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
}
