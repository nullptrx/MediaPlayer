package tv.lycam.player.widget;

import android.content.Context;
import android.graphics.Matrix;
import android.graphics.SurfaceTexture;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.Surface;
import android.view.TextureView;

import com.alivc.player.AliVcMediaPlayer;
import com.alivc.player.MediaPlayer;
import com.alivc.player.ScalableType;
import com.alivc.player.ScaleManager;
import com.alivc.player.Size;

import javax.microedition.khronos.opengles.GL10;

import tv.lycam.player.callback.IMediaStatus;
import tv.lycam.player.PlayerState;
import tv.lycam.player.utils.Debugger;

/**
 * @author 诸葛不亮
 * @version 1.0
 * @description
 */

public class AliVideoView extends IVideoView implements TextureView.SurfaceTextureListener {

    public static final String TAG = "AliVideoView";
    // 默认缩放模式
    protected MediaPlayer.VideoScalingMode DEFAULT_ASPECTRATIO = MediaPlayer.VideoScalingMode.VIDEO_SCALING_MODE_SCALE_TO_FIT;


    private AliVcMediaPlayer mMediaPlayer;

    private ScaleManager mScaleManager;
    // 变换
    private ScalableType mScalableType = ScalableType.NONE;

    // 当前播放状态
    protected int mCurrentState = PlayerState.CURRENT_STATE_NORMAL;
    // 备份缓存前的播放状态
    protected int mBackUpPlayingBufferState = -1;
    // 是否直播流
    protected boolean isLiveStream;
    protected String mStreamUrl;
    // 判断是否播放过
    private boolean hadPlay;

    public AliVideoView(Context context) {
        this(context, null);
    }

    public AliVideoView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AliVideoView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initVodPlayer(context);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        setSurfaceTextureListener(this);
    }

    @Override
    protected void onDetachedFromWindow() {
        destroy();
        super.onDetachedFromWindow();
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int width, int height) {
        if (mMediaPlayer != null) {
            Surface surface = new Surface(surfaceTexture);
            mMediaPlayer.setVideoSurface(surface);
            surface.release();
        }
        int vWidth = getWidth();
        int vHeight = getHeight();
        Size viewSize = new Size(vWidth, vHeight);
        Size videoSize = new Size(width, height);
        mScaleManager = new ScaleManager(viewSize, videoSize);
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
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
        return true;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {
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
        mMediaPlayer.disableNativeLog();

        setStateAndUi(PlayerState.CURRENT_STATE_NORMAL);
    }

    //准备完成时触发
    private AliVcMediaPlayer.MediaPlayerPreparedListener mOnBasePreparedListener = new AliVcMediaPlayer.MediaPlayerPreparedListener() {
        @Override
        public void onPrepared() {
//            if (mCurrentState != PlayerState.CURRENT_STATE_PREPAREING) return;
            if (mIMediaStatus != null) {
                mIMediaStatus.onMediaPrepared();
            }
            mMediaPlayer.play();
            setStateAndUi(PlayerState.CURRENT_STATE_PLAYING);
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
            if (mIMediaStatus != null) {
                mIMediaStatus.onMediaInfo(what, extra);
            }
        }

    };

    //错误发生时触发，错误码见接口文档
    private AliVcMediaPlayer.MediaPlayerErrorListener mOnBaseErrorListener = new AliVcMediaPlayer.MediaPlayerErrorListener() {
        @Override
        public void onError(int errorCode, String msg) {
            setStateAndUi(PlayerState.CURRENT_STATE_ERROR);
            mMediaPlayer.stop();
            if (mIMediaStatus != null) {
                mIMediaStatus.onMediaError(errorCode, msg);
            }
        }
    };

    //视频正常播放完成时触发
    private AliVcMediaPlayer.MediaPlayerCompletedListener mOnBaseCompletedListener = new AliVcMediaPlayer.MediaPlayerCompletedListener() {
        @Override
        public void onCompleted() {
            setStateAndUi(PlayerState.CURRENT_STATE_AUTO_COMPLETE);
            if (mIMediaStatus != null) {
                mIMediaStatus.onMediaCompleted();
            }
        }
    };

    private AliVcMediaPlayer.MediaPlayerBufferingUpdateListener mOnBaseBufferingUpdateListener = new AliVcMediaPlayer.MediaPlayerBufferingUpdateListener() {
        @Override
        public void onBufferingUpdateListener(int percent) {
            if (mIMediaStatus != null) {
                mIMediaStatus.onMediaBufferingUpdate(percent);
            }
        }
    };

    private AliVcMediaPlayer.MediaPlayerVideoSizeChangeListener mOnBaseVideoSizeChangeListener = new AliVcMediaPlayer.MediaPlayerVideoSizeChangeListener() {
        @Override
        public void onVideoSizeChange(int width, int height) {
            int videoWidth = mMediaPlayer.getVideoWidth();
            int videoHeight = mMediaPlayer.getVideoHeight();
            setVideoSize(videoWidth, videoHeight);
            if (mIMediaStatus != null) {
                mIMediaStatus.onMediaVideoSizeChange(width, height);
            }
        }
    };

    private AliVcMediaPlayer.MediaPlayerSeekCompleteListener mOnBaseSeekCompleteListener = new AliVcMediaPlayer.MediaPlayerSeekCompleteListener() {
        @Override
        public void onSeekCompleted() {
            if (mIMediaStatus != null) {
                mIMediaStatus.onMediaSeekCompleted();
            }
        }
    };

    //首帧显示时触发
    private MediaPlayer.MediaPlayerFrameInfoListener mOnBaseFrameInfoListener = new MediaPlayer.MediaPlayerFrameInfoListener() {
        @Override
        public void onFrameInfoListener() {
            if (mIMediaStatus != null) {
                mIMediaStatus.onMediaFrameInfo();
            }
        }
    };

    private MediaPlayer.MediaPlayerStoppedListener mOnBaseStoppedListener = new MediaPlayer.MediaPlayerStoppedListener() {
        @Override
        public void onStopped() {
            setStateAndUi(PlayerState.CURRENT_STATE_NORMAL);
        }
    };

    protected void setStateAndUi(int state) {
        if (state != PlayerState.CURRENT_STATE_NORMAL) {
            hadPlay = true;
        }
        mCurrentState = state;
        if (mIMediaStatus != null) {
            mIMediaStatus.setStateAndUi(state);
        }
    }

    /**
     * 处理镜像旋转
     * 注意，暂停时
     */
    protected void resolveTransform() {
        if (mScaleManager != null) {
            Matrix scaleMatrix = mScaleManager.getScaleMatrix(mScalableType);
            setTransform(scaleMatrix);
            invalidate();
        }
    }

    public void setScalableType(ScalableType scalableType) {
        this.mScalableType = scalableType;
        resolveTransform();
    }

    public void setIMediaStatus(IMediaStatus iMediaStatus) {
        mIMediaStatus = iMediaStatus;
    }

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
        if (mCurrentState == PlayerState.CURRENT_STATE_PLAYING) {
            if (mMediaPlayer != null) {
                mMediaPlayer.pause();
                setStateAndUi(PlayerState.CURRENT_STATE_PAUSE);
            }
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

    private void prepareAndPlay() {
        if (TextUtils.isEmpty(mStreamUrl)) {
            return;
        }
        if (mMediaPlayer != null) {
            mMediaPlayer.setMediaType(isLiveStream ? MediaPlayer.MediaType.Live : MediaPlayer.MediaType.Vod);
            mMediaPlayer.prepareAndPlay(mStreamUrl);
            setStateAndUi(PlayerState.CURRENT_STATE_PREPAREING);
        }
    }

    @Override
    public void stop() {
        if (mMediaPlayer != null) {
            mMediaPlayer.stop();
            setStateAndUi(PlayerState.CURRENT_STATE_NORMAL);
        }
    }

    @Override
    public void destroy() {
        stop();
        if (mMediaPlayer != null) {
            mMediaPlayer.releaseVideoSurface();
            mMediaPlayer.stop();
            mMediaPlayer.destroy();
        }
    }

    @Override
    public void start() {
        if (mMediaPlayer != null && hadPlay) {
            mMediaPlayer.stop();
        }
        prepareAndPlay();
    }

    @Override
    public void enableNativeLog() {
        if (mMediaPlayer != null) {
            mMediaPlayer.enableNativeLog();
        }
    }

    @Override
    public void disableNativeLog() {
        if (mMediaPlayer != null) {
            mMediaPlayer.disableNativeLog();
        }
    }

    @Override
    public int getCurrentPosition() {
        return mMediaPlayer != null ? mMediaPlayer.getCurrentPosition() : 0;
    }

    @Override
    public int getDuration() {
        return mMediaPlayer != null ? mMediaPlayer.getDuration() : 0;
    }

    @Override
    public int getBufferPosition() {
        return mMediaPlayer != null ? mMediaPlayer.getBufferPosition() : 0;
    }

    @Override
    public boolean isLiveMode() {
        return isLiveStream;
    }

    @Override
    public void setMuteMode(boolean muteMode) {
        if (mMediaPlayer != null) {
            mMediaPlayer.setMuteMode(muteMode);
        }
    }

    @Override
    public String getVideoPath() {
        return mStreamUrl != null ? mStreamUrl : "";
    }

    @Override
    public boolean isPlaying() {
        return mMediaPlayer != null && mMediaPlayer.isPlaying();
    }

    @Override
    public void seekTo(int time) {
        if (mMediaPlayer != null) {
            mMediaPlayer.seekTo(time);
        }
    }

    /**
     * {@link MediaPlayer.VideoScalingMode}
     *
     * @param scalingMode
     * @return
     */
    public void setVideoScalingMode(MediaPlayer.VideoScalingMode scalingMode) {
        if (mMediaPlayer != null) {
            mMediaPlayer.setVideoScalingMode(scalingMode);
        }
    }


}
