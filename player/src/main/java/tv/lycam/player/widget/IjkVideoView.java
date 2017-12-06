package tv.lycam.player.widget;

import android.content.Context;
import android.graphics.Matrix;
import android.graphics.SurfaceTexture;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.support.annotation.IntRange;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Surface;
import android.view.TextureView;

import com.alivc.player.ScalableType;
import com.alivc.player.ScaleManager;
import com.alivc.player.Size;

import java.io.IOException;
import java.util.Map;

import tv.danmaku.ijk.media.player.IMediaPlayer;
import tv.danmaku.ijk.media.player.IjkMediaPlayer;
import tv.danmaku.ijk.media.player.IjkTimedText;
import tv.lycam.player.PlayerState;
import tv.lycam.player.utils.Debugger;

/**
 * @author 诸葛不亮
 * @version 1.0
 * @description
 */

public class IjkVideoView extends IVideoView implements TextureView.SurfaceTextureListener {
    public static final String TAG = "IjkVideoView";
    private Uri mUri;
    private Map<String, String> mHeaders;
    private IjkMediaPlayer mMediaPlayer;
    private int mSeekWhenPrepared, mCurrentBufferPercentage;
    private int mCurrentState = PlayerState.CURRENT_STATE_NORMAL;
    // 备份缓存前的播放状态
    protected int mBackUpPlayingBufferState = -1;
    private Context mContext;
    private IMediaPlayer.OnCompletionListener mOnCompletionListener;
    private IMediaPlayer.OnPreparedListener mOnPreparedListener;
    private IMediaPlayer.OnErrorListener mOnErrorListener;
    private IMediaPlayer.OnInfoListener mOnInfoListener;
    private ScaleManager mScaleManager;
    // 变换
    private ScalableType mScalableType = ScalableType.NONE;
    private boolean isLiveStream;

    public IjkVideoView(Context context) {
        this(context, null);
    }

    public IjkVideoView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public IjkVideoView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initRender(context);
        setStateAndUi(PlayerState.CURRENT_STATE_NORMAL);
    }

    private void initRender(Context context) {
        mContext = context;
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

    private IjkMediaPlayer createPlayer() {
        IjkMediaPlayer mediaPlayer = new IjkMediaPlayer();
        mediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec", 0);
        mediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "opensles", 0);
        mediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "overlay-format", IjkMediaPlayer.SDL_FCC_RV32);
        mediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "framedrop", 5);
        mediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "start-on-prepared", 1);

        mediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "http-detect-range-support", 0);

        mediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "max-buffer-size", 1024 * 400);
        // 超时时间
        // https://www.ffmpeg.org/ffmpeg-protocols.html#http
        // https://ffmpeg.org/ffmpeg-protocols.html#toc-rtmp
        mediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "timeout", 60_000_000);
        // 重连次数
//                    mediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "reconnect", 5);

        mediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "analyzeduration", 1000);
//                    mediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "fflags", "nobuffer");
        mediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "infbuf", 1);
        mediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "probesize", "5000");

//      mediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec_mpeg4", 1);

        mediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_CODEC, "skip_loop_filter", 0);
//            mediaPlayer.setLooping(true);
        mediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "sync", "ext");
//                mediaPlayer.setSpeed(1.03f);
        mediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "enable-accurate-seek", 1);

//      mediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "min-frames", 20);
//      mediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_CODEC, "skip_frame", 5);
//      mediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "packet-buffering", 0);
        return mediaPlayer;
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int width, int height) {
        if (mMediaPlayer != null) {
            Surface surface = new Surface(surfaceTexture);
            mMediaPlayer.setSurface(surface);
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
            Surface surface = new Surface(surfaceTexture);
            mMediaPlayer.setSurface(surface);
            surface.release();
        }
        resolveTransform();
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
        if (mMediaPlayer != null)
            mMediaPlayer.setDisplay(null);
        return true;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {
        resolveTransform();
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

    @Override
    public void setScalableType(ScalableType scalableType) {
        this.mScalableType = scalableType;
        resolveTransform();
    }

    @Override
    protected void setStateAndUi(int state) {
        mCurrentState = state;
        if (mIMediaStatus != null) {
            mIMediaStatus.setStateAndUi(state);
        }
    }

    @Override
    public void setDefaultDecoder(@IntRange(from = 0, to = 1) int decoderType) {
        if (mMediaPlayer != null) {
            mMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec", decoderType);
        }
    }

    /**
     * Sets video path.
     *
     * @param url the path of the video.
     */
    @Override
    public void setVideoPath(String url) {
        boolean live = isLive(url);
        setVideoPath(url, live);
    }

    @Override
    public void setVideoPath(String url, boolean isLiveStream) {
        this.isLiveStream = isLiveStream;
        if (url != null) {
            setVideoURI(Uri.parse(url));
        }
    }

    /**
     * Sets video URI.
     *
     * @param uri the URI of the video.
     */
    private void setVideoURI(Uri uri) {
        setVideoURI(uri, null);
    }

    /**
     * Sets video URI using specific headers.
     *
     * @param uri     the URI of the video.
     * @param headers the headers for the URI request.
     *                Note that the cross domain redirection is allowed by default, but that can be
     *                changed with key/value pairs through the headers parameter with
     *                "android-allow-cross-domain-redirect" as the key and "0" or "1" as the value
     *                to disallow or allow cross domain redirection.
     */
    private void setVideoURI(Uri uri, Map<String, String> headers) {
        mUri = uri;
        mHeaders = headers;
        mSeekWhenPrepared = 0;
        initIjkPlayer();
        requestLayout();
        invalidate();
    }

    private void initIjkPlayer() {
        if (mUri == null) {
            return;
        }
        release();
        try {
            mCurrentBufferPercentage = 0;
            mMediaPlayer = createPlayer();
            mMediaPlayer.setOnPreparedListener(mPreparedListener);
            mMediaPlayer.setOnVideoSizeChangedListener(mSizeChangedListener);
            mMediaPlayer.setOnCompletionListener(mCompletionListener);
            mMediaPlayer.setOnErrorListener(mErrorListener);
            mMediaPlayer.setOnInfoListener(mInfoListener);
            mMediaPlayer.setOnBufferingUpdateListener(mBufferingUpdateListener);
            mMediaPlayer.setOnSeekCompleteListener(mSeekCompleteListener);
            mMediaPlayer.setOnTimedTextListener(mOnTimedTextListener);
            mMediaPlayer.setDataSource(mContext, mUri, mHeaders);
            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mMediaPlayer.setScreenOnWhilePlaying(true);
            SurfaceTexture texture = getSurfaceTexture();
            if (texture != null) {
                Surface surface = new Surface(texture);
                mMediaPlayer.setSurface(surface);
                surface.release();
            }
            setStateAndUi(PlayerState.CURRENT_STATE_NORMAL);
        } catch (IOException | RuntimeException ex) {
            Log.w(TAG, "Unable to open content: " + mUri, ex);
            setStateAndUi(PlayerState.CURRENT_STATE_ERROR);
            mErrorListener.onError(mMediaPlayer, MediaPlayer.MEDIA_ERROR_UNKNOWN, 0);
        }
    }


    /*
    * release the media player in any state
    */
    private void release() {
        if (mMediaPlayer != null) {
            mMediaPlayer.reset();
            mMediaPlayer.release();
            mMediaPlayer = null;
            // REMOVED: mPendingSubtitleTracks.clear();
            mCurrentState = PlayerState.CURRENT_STATE_NORMAL;
        }
        AudioManager am = (AudioManager) mContext.getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
        if (am != null) {
            am.abandonAudioFocus(null);
        }
    }

    private void stopPlayback() {
        if (mMediaPlayer != null) {
            mMediaPlayer.stop();
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
    }

    @Override
    public void start() {
        if (mUri == null || TextUtils.isEmpty(mUri.getPath())) {
            return;
        }
        if (mMediaPlayer != null) {
            mMediaPlayer.prepareAsync();
            setStateAndUi(PlayerState.CURRENT_STATE_PREPAREING);
            AudioManager am = (AudioManager) mContext.getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
            if (am != null) {
                am.requestAudioFocus(null, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
            }
        }
    }

    @Override
    public void pause() {
        if (mCurrentState == PlayerState.CURRENT_STATE_PLAYING) {
            if (mMediaPlayer.isPlaying()) {
                mMediaPlayer.pause();
                mSeekWhenPrepared = (int) mMediaPlayer.getCurrentPosition();
                setStateAndUi(PlayerState.CURRENT_STATE_PAUSE);
            }
        }
    }

    @Override
    public void resume() {
        if (mCurrentState == PlayerState.CURRENT_STATE_PAUSE) {
            initIjkPlayer();
            if (mMediaPlayer != null) {
                mMediaPlayer.prepareAsync();
            }
        }
    }

    @Override
    public void stop() {
        if (mMediaPlayer != null) {
            stopPlayback();
            setStateAndUi(PlayerState.CURRENT_STATE_NORMAL);

            AudioManager am = (AudioManager) mContext.getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
            if (am != null) {
                am.abandonAudioFocus(null);
            }
        }
    }

    @Override
    public void destroy() {
        stopPlayback();
    }

    @Override
    public void enableNativeLog() {
        IjkMediaPlayer.native_setLogLevel(IjkMediaPlayer.IJK_LOG_DEBUG);
    }

    @Override
    public void disableNativeLog() {
        IjkMediaPlayer.native_setLogLevel(IjkMediaPlayer.IJK_LOG_SILENT);

    }

    public int getDuration() {
        if (mMediaPlayer != null) {
            return (int) mMediaPlayer.getDuration();
        }

        return -1;
    }

    @Override
    public int getBufferPosition() {
        if (mMediaPlayer != null) {
            return mCurrentBufferPercentage;
        }
        return 0;
    }

    @Override
    public boolean isLiveMode() {
        return isLiveStream;
    }

    @Override
    public void setMuteMode(boolean muteMode) {
        if (mMediaPlayer != null) {
            int volume = muteMode ? 0 : 1;
            mMediaPlayer.setVolume(volume, volume);
        }
    }

    @Override
    public String getVideoPath() {
        return mUri != null ? mUri.getPath() : "";
    }

    public int getCurrentPosition() {
        if (mMediaPlayer != null) {
            return (int) mMediaPlayer.getCurrentPosition();
        }
        return 0;
    }

    public void seekTo(int msec) {
        if (mMediaPlayer != null) {
            mMediaPlayer.seekTo(msec);
            mSeekWhenPrepared = 0;
        } else {
            mSeekWhenPrepared = msec;
        }
    }

    public boolean isPlaying() {
        return mMediaPlayer != null && mMediaPlayer.isPlaying();
    }

    private IMediaPlayer.OnVideoSizeChangedListener mSizeChangedListener =
            new IMediaPlayer.OnVideoSizeChangedListener() {
                public void onVideoSizeChanged(IMediaPlayer mp, int width, int height, int sarNum, int sarDen) {
                    int videoWidth = mp.getVideoWidth();
                    int videoHeight = mp.getVideoHeight();
                    int videoSarNum = mp.getVideoSarNum();
                    int videoSarDen = mp.getVideoSarDen();
                    setVideoSize(videoWidth, videoHeight);
                    setVideoSampleAspectRatio(videoSarNum, videoSarDen);
                    if (mIMediaStatus != null) {
                        mIMediaStatus.onMediaVideoSizeChange(width, height);
                    }
                }
            };

    private IMediaPlayer.OnPreparedListener mPreparedListener = new IMediaPlayer.OnPreparedListener() {
        public void onPrepared(IMediaPlayer mp) {
            int videoWidth = mp.getVideoWidth();
            int videoHeight = mp.getVideoHeight();
            int videoSarNum = mp.getVideoSarNum();
            int videoSarDen = mp.getVideoSarDen();
            if (videoWidth != 0 && videoHeight != 0) {
                setVideoSize(videoWidth, videoHeight);
                setVideoSampleAspectRatio(videoSarNum, videoSarDen);
            }
//            mHudViewHolder.updateLoadCost(mPrepareEndTime - mPrepareStartTime);

            // Get the capabilities of the player for this stream
            // REMOVED: Metadata

            if (mOnPreparedListener != null) {
                mOnPreparedListener.onPrepared(mMediaPlayer);
            }

            int seekToPosition = mSeekWhenPrepared;  // mSeekWhenPrepared may be changed after seekTo() call
            if (seekToPosition != 0) {
                seekTo(seekToPosition);
            }
            if (mIMediaStatus != null) {
                mIMediaStatus.onMediaPrepared();
            }
            mMediaPlayer.start();

            setStateAndUi(PlayerState.CURRENT_STATE_PLAYING);

        }
    };

    private IMediaPlayer.OnInfoListener mInfoListener = new IMediaPlayer.OnInfoListener() {

        @Override
        public boolean onInfo(IMediaPlayer mp, int what, int extra) {

            Debugger.printfLog(TAG, "OnInfo, what = " + what + ", extra = " + extra);
            switch (what) {
                case IMediaPlayer.MEDIA_INFO_BUFFERING_START:
                    mBackUpPlayingBufferState = mCurrentState;
                    //避免在onPrepared之前就进入了buffering，导致一只loading
                    if (mCurrentState != PlayerState.CURRENT_STATE_PREPAREING && mCurrentState > 0)
                        setStateAndUi(PlayerState.CURRENT_STATE_PLAYING_BUFFERING_START);
                    break;
                case IMediaPlayer.MEDIA_INFO_BUFFERING_END:
                    if (mBackUpPlayingBufferState != -1) {
                        if (mCurrentState != PlayerState.CURRENT_STATE_PREPAREING && mCurrentState > 0)
                            setStateAndUi(mBackUpPlayingBufferState);

                        mBackUpPlayingBufferState = -1;
                    }
                    break;
                case IMediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START:
                    Debugger.printfLog(TAG, "First video render time: " + extra + "ms");
                    break;
                default:
                    break;
            }
            if (mIMediaStatus != null) {
                mIMediaStatus.onMediaInfo(what, extra);
            }
            if (mOnInfoListener != null) {
                mOnInfoListener.onInfo(mp, what, extra);
            }
            return true;
        }
    };

    private IMediaPlayer.OnErrorListener mErrorListener =
            new IMediaPlayer.OnErrorListener() {
                public boolean onError(IMediaPlayer mp, int framework_err, int impl_err) {
                    setStateAndUi(PlayerState.CURRENT_STATE_ERROR);
                    mMediaPlayer.stop();
                    if (mIMediaStatus != null) {
                        String msg = null;
                        switch (framework_err) {
                            case IMediaPlayer.MEDIA_ERROR_UNKNOWN:
                                msg = "unknown";
                                break;
                            case IMediaPlayer.MEDIA_ERROR_SERVER_DIED:
                                msg = "server died";
                                break;
                            case IMediaPlayer.MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK:
                                msg = "not valid for progressive playback";
                                break;
                            case IMediaPlayer.MEDIA_ERROR_IO:
                                msg = "error io";
                                break;
                            case IMediaPlayer.MEDIA_ERROR_MALFORMED:
                                msg = "malformed";
                                break;
                            case IMediaPlayer.MEDIA_ERROR_UNSUPPORTED:
                                msg = "unsupported";
                                break;
                            case IMediaPlayer.MEDIA_ERROR_TIMED_OUT:
                                msg = "timed out";
                                break;
                        }
                        mIMediaStatus.onMediaError(framework_err, msg);
                    }

                    /* If an error handler has been supplied, use it and finish. */
                    if (mOnErrorListener != null) {
                        if (mOnErrorListener.onError(mMediaPlayer, framework_err, impl_err)) {
                            return true;
                        }
                    }

                    return true;
                }
            };

    private IMediaPlayer.OnCompletionListener mCompletionListener =
            new IMediaPlayer.OnCompletionListener() {
                public void onCompletion(IMediaPlayer mp) {
                    setStateAndUi(PlayerState.CURRENT_STATE_AUTO_COMPLETE);
                    if (mIMediaStatus != null) {
                        mIMediaStatus.onMediaCompleted();
                    }
                    if (mOnCompletionListener != null) {
                        mOnCompletionListener.onCompletion(mMediaPlayer);
                    }
                }
            };

    private IMediaPlayer.OnBufferingUpdateListener mBufferingUpdateListener =
            new IMediaPlayer.OnBufferingUpdateListener() {
                public void onBufferingUpdate(IMediaPlayer mp, int percent) {
                    mCurrentBufferPercentage = percent;
                    if (mIMediaStatus != null) {
                        mIMediaStatus.onMediaBufferingUpdate(percent);
                    }
                }
            };

    private IMediaPlayer.OnSeekCompleteListener mSeekCompleteListener = new IMediaPlayer.OnSeekCompleteListener() {

        @Override
        public void onSeekComplete(IMediaPlayer mp) {
            if (mIMediaStatus != null) {
                mIMediaStatus.onMediaSeekCompleted();
            }
        }
    };


    private IMediaPlayer.OnTimedTextListener mOnTimedTextListener = new IMediaPlayer.OnTimedTextListener() {
        @Override
        public void onTimedText(IMediaPlayer mp, IjkTimedText text) {
        }
    };

    /**
     * Register a callback to be invoked when the media file
     * is loaded and ready to go.
     *
     * @param l The callback that will be run
     */
    public void setOnPreparedListener(IMediaPlayer.OnPreparedListener l) {
        mOnPreparedListener = l;
    }

    /**
     * Register a callback to be invoked when the end of a media file
     * has been reached during playback.
     *
     * @param l The callback that will be run
     */
    public void setOnCompletionListener(IMediaPlayer.OnCompletionListener l) {
        mOnCompletionListener = l;
    }

    /**
     * Register a callback to be invoked when an error occurs
     * during playback or setup.  If no listener is specified,
     * or if the listener returned false, VideoView will inform
     * the user of any errors.
     *
     * @param l The callback that will be run
     */
    public void setOnErrorListener(IMediaPlayer.OnErrorListener l) {
        mOnErrorListener = l;
    }

    /**
     * Register a callback to be invoked when an informational event
     * occurs during playback or setup.
     *
     * @param l The callback that will be run
     */
    public void setOnInfoListener(IMediaPlayer.OnInfoListener l) {
        mOnInfoListener = l;
    }

    /**
     * 设置声音
     */
    public void setVolume(float leftVolume, float rightVolume) {
        if (null != mMediaPlayer) {
            mMediaPlayer.setVolume(leftVolume, rightVolume);
        }
    }

}
