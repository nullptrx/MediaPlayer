package tv.lycam.alivc;

import android.app.Activity;
import android.content.Context;
import android.graphics.Matrix;
import android.graphics.SurfaceTexture;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.InflateException;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.WindowManager;

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
    protected TextureView mTextureView;

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
        mTextureView = findViewById(getPlayerId());
        if (mTextureView == null) {
            throw new InflateException("The layout has not include PLVideoTextureView!");
        }
        mTextureView.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
            @Override
            public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
                if (mMediaPlayer == null) {

                } else {
                    Surface surfaces = new Surface(surface);
                    mMediaPlayer.setVideoSurface(surfaces);
                    surfaces.release();
                }
                resolveTransform();
            }

            @Override
            public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
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

            }
        });
        initVodPlayer(mContext);

        setStateAndUi(PlayerState.CURRENT_STATE_NORMAL);
    }

    /**
     * 处理镜像旋转
     * 注意，暂停时
     */
    protected void resolveTransform() {
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
        SurfaceTexture surfaceTexture = new SurfaceTexture(createTextureID());
        Surface surface = new Surface(surfaceTexture);
        mMediaPlayer = new AliVcMediaPlayer(context, surface);
        surface.release();
        surfaceTexture.release();

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
        Activity activity = CommonUtil.getActivityContext(mContext);
        if (activity != null) {
            activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
    }

    protected void stop() {
        Activity activity = CommonUtil.getActivityContext(mContext);
        if (activity != null) {
            activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
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
}
