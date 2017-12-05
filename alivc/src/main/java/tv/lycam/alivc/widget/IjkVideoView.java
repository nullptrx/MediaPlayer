package tv.lycam.alivc.widget;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Surface;
import android.view.TextureView;

import java.io.IOException;
import java.util.Map;

import tv.danmaku.ijk.media.player.IMediaPlayer;
import tv.danmaku.ijk.media.player.IjkMediaPlayer;
import tv.danmaku.ijk.media.player.IjkTimedText;
import tv.lycam.alivc.PlayerState;
import tv.lycam.alivc.utils.MeasureHelper;

/**
 * @author 诸葛不亮
 * @version 1.0
 * @description
 */

public class IjkVideoView extends TextureView implements TextureView.SurfaceTextureListener {
    public static final String TAG = "IjkVideoView";
    private Uri mUri;
    private Map<String, String> mHeaders;
    private IjkMediaPlayer mMediaPlayer;
    private int mSeekWhenPrepared, mCurrentBufferPercentage;
    private int mCurrentState, mTargetState;
    private Context mContext;
    private IMediaPlayer.OnCompletionListener mOnCompletionListener;
    private IMediaPlayer.OnPreparedListener mOnPreparedListener;
    private IMediaPlayer.OnErrorListener mOnErrorListener;
    private IMediaPlayer.OnInfoListener mOnInfoListener;
    private MeasureHelper mMeasureHelper;

    public IjkVideoView(Context context) {
        this(context, null);
    }

    public IjkVideoView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public IjkVideoView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initRender(context);
    }

    //--------------------
    // Layout & Measure
    //--------------------
    public void setVideoSize(int videoWidth, int videoHeight) {
        if (videoWidth > 0 && videoHeight > 0) {
            mMeasureHelper.setVideoSize(videoWidth, videoHeight);
            requestLayout();
        }
    }

    public void setVideoSampleAspectRatio(int videoSarNum, int videoSarDen) {
        if (videoSarNum > 0 && videoSarDen > 0) {
            mMeasureHelper.setVideoSampleAspectRatio(videoSarNum, videoSarDen);
            requestLayout();
        }
    }

    public void setVideoRotation(int degree) {
        mMeasureHelper.setVideoRotation(degree);
        setRotation(degree);
    }

    public void setAspectRatio(int aspectRatio) {
        mMeasureHelper.setAspectRatio(aspectRatio);
        requestLayout();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        mMeasureHelper.doMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(mMeasureHelper.getMeasuredWidth(), mMeasureHelper.getMeasuredHeight());
    }


    private void initRender(Context context) {
        mContext = context;
        mMeasureHelper = new MeasureHelper(this);
        setSurfaceTextureListener(this);
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
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int width, int height) {

    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
        return true;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {

    }

    /**
     * Sets video path.
     *
     * @param path the path of the video.
     */
    public void setVideoPath(String path) {
        if (path != null) {
            setVideoURI(Uri.parse(path));
        }
    }

    /**
     * Sets video URI.
     *
     * @param uri the URI of the video.
     */
    public void setVideoURI(Uri uri) {
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
        openVideo();
        requestLayout();
        invalidate();
    }

    private void openVideo() {
        if (mUri == null) {
            return;
        }
        release(false);
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
            mMediaPlayer.prepareAsync();
            mCurrentState = PlayerState.CURRENT_STATE_PREPAREING;
        } catch (IOException | RuntimeException ex) {
            Log.w(TAG, "Unable to open content: " + mUri, ex);
            mCurrentState = PlayerState.CURRENT_STATE_ERROR;
            mTargetState = PlayerState.CURRENT_STATE_ERROR;
            mErrorListener.onError(mMediaPlayer, MediaPlayer.MEDIA_ERROR_UNKNOWN, 0);
        }
    }


    /*
    * release the media player in any state
    */
    public void release(boolean cleartargetstate) {
        if (mMediaPlayer != null) {
            mMediaPlayer.reset();
            mMediaPlayer.release();
            mMediaPlayer = null;
            // REMOVED: mPendingSubtitleTracks.clear();
            mCurrentState = PlayerState.CURRENT_STATE_NORMAL;
            if (cleartargetstate) {
                mTargetState = PlayerState.CURRENT_STATE_NORMAL;
            }
        }
        AudioManager am = (AudioManager) mContext.getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
        if (am != null) {
            am.abandonAudioFocus(null);
        }
    }

    public void start() {
        if (isInPlaybackState()) {
            mMediaPlayer.start();
            mCurrentState = PlayerState.CURRENT_STATE_PLAYING;
        }
        mTargetState = PlayerState.CURRENT_STATE_PLAYING;
        AudioManager am = (AudioManager) mContext.getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
        if (am != null) {
            am.requestAudioFocus(null, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
        }
    }

    public void pause() {
        if (isInPlaybackState()) {
            if (mMediaPlayer.isPlaying()) {
                mMediaPlayer.pause();
                mCurrentState = PlayerState.CURRENT_STATE_PAUSE;
            }
        }
        mTargetState = PlayerState.CURRENT_STATE_PAUSE;
    }

    public void suspend() {
        release(false);
    }

    public void resume() {
        openVideo();
    }

    public int getDuration() {
        if (isInPlaybackState()) {
            return (int) mMediaPlayer.getDuration();
        }

        return -1;
    }

    public int getCurrentPosition() {
        if (isInPlaybackState()) {
            return (int) mMediaPlayer.getCurrentPosition();
        }
        return 0;
    }

    public void seekTo(int msec) {
        if (isInPlaybackState()) {
            mMediaPlayer.seekTo(msec);
            mSeekWhenPrepared = 0;
        } else {
            mSeekWhenPrepared = msec;
        }
    }

    public boolean isPlaying() {
        return isInPlaybackState() && mMediaPlayer.isPlaying();
    }

    public int getBufferPercentage() {
        if (mMediaPlayer != null) {
            return mCurrentBufferPercentage;
        }
        return 0;
    }

    private boolean isInPlaybackState() {
        return (mMediaPlayer != null &&
                mCurrentState != PlayerState.CURRENT_STATE_ERROR &&
                mCurrentState != PlayerState.CURRENT_STATE_NORMAL &&
                mCurrentState != PlayerState.CURRENT_STATE_PREPAREING);
    }


    IMediaPlayer.OnVideoSizeChangedListener mSizeChangedListener =
            new IMediaPlayer.OnVideoSizeChangedListener() {
                public void onVideoSizeChanged(IMediaPlayer mp, int width, int height, int sarNum, int sarDen) {
                    int videoWidth = mp.getVideoWidth();
                    int videoHeight = mp.getVideoHeight();
                    int videoSarNum = mp.getVideoSarNum();
                    int videoSarDen = mp.getVideoSarDen();
                    if (videoWidth != 0 && videoHeight != 0) {
                        if (mMeasureHelper != null) {
                            mMeasureHelper.setVideoSize(videoWidth, videoHeight);
                            mMeasureHelper.setVideoSampleAspectRatio(videoSarNum, videoSarDen);
                            requestLayout();
                        }
                    }
                }
            };

    IMediaPlayer.OnPreparedListener mPreparedListener = new IMediaPlayer.OnPreparedListener() {
        public void onPrepared(IMediaPlayer mp) {
            int videoWidth = mp.getVideoWidth();
            int videoHeight = mp.getVideoHeight();
            int videoSarNum = mp.getVideoSarNum();
            int videoSarDen = mp.getVideoSarDen();
            if (videoWidth != 0 && videoHeight != 0) {
                if (mMeasureHelper != null) {
                    mMeasureHelper.setVideoSize(videoWidth, videoHeight);
                    mMeasureHelper.setVideoSampleAspectRatio(videoSarNum, videoSarDen);
                    requestLayout();
                }
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
            if (mTargetState == PlayerState.CURRENT_STATE_PLAYING) {
                start();
            }
        }
    };

    private IMediaPlayer.OnCompletionListener mCompletionListener =
            new IMediaPlayer.OnCompletionListener() {
                public void onCompletion(IMediaPlayer mp) {
                    mCurrentState = PlayerState.CURRENT_STATE_AUTO_COMPLETE;
                    mTargetState = PlayerState.CURRENT_STATE_AUTO_COMPLETE;
                    if (mOnCompletionListener != null) {
                        mOnCompletionListener.onCompletion(mMediaPlayer);
                    }
                }
            };

    private IMediaPlayer.OnInfoListener mInfoListener =
            new IMediaPlayer.OnInfoListener() {

                private int mBufferStartPosition = -1;

                public boolean onInfo(IMediaPlayer mp, int arg1, int arg2) {
                    if (mOnInfoListener != null) {
                        mOnInfoListener.onInfo(mp, arg1, arg2);
                    }
                    switch (arg1) {
                        case IMediaPlayer.MEDIA_INFO_VIDEO_TRACK_LAGGING:
                            Log.d(TAG, "MEDIA_INFO_VIDEO_TRACK_LAGGING:");
                            break;
                        case IMediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START:
                            Log.d(TAG, "MEDIA_INFO_VIDEO_RENDERING_START:");
                            break;
                        case IMediaPlayer.MEDIA_INFO_BUFFERING_START:
                            Log.d(TAG, "MEDIA_INFO_BUFFERING_START:");
                            break;
                        case IMediaPlayer.MEDIA_INFO_BUFFERING_END:
                            Log.d(TAG, "MEDIA_INFO_BUFFERING_END:");
                            break;
                        case IMediaPlayer.MEDIA_INFO_NETWORK_BANDWIDTH:
                            Log.d(TAG, "MEDIA_INFO_NETWORK_BANDWIDTH: " + arg2);
                            break;
                        case IMediaPlayer.MEDIA_INFO_BAD_INTERLEAVING:
                            Log.d(TAG, "MEDIA_INFO_BAD_INTERLEAVING:");
                            break;
                        case IMediaPlayer.MEDIA_INFO_NOT_SEEKABLE:
                            Log.d(TAG, "MEDIA_INFO_NOT_SEEKABLE:");
                            break;
                        case IMediaPlayer.MEDIA_INFO_METADATA_UPDATE:
                            Log.d(TAG, "MEDIA_INFO_METADATA_UPDATE:");
                            break;
                        case IMediaPlayer.MEDIA_INFO_UNSUPPORTED_SUBTITLE:
                            Log.d(TAG, "MEDIA_INFO_UNSUPPORTED_SUBTITLE:");
                            break;
                        case IMediaPlayer.MEDIA_INFO_SUBTITLE_TIMED_OUT:
                            Log.d(TAG, "MEDIA_INFO_SUBTITLE_TIMED_OUT:");
                            break;
                        case IMediaPlayer.MEDIA_INFO_VIDEO_ROTATION_CHANGED:
                            Log.d(TAG, "MEDIA_INFO_VIDEO_ROTATION_CHANGED: " + arg2);
                            break;
                        case IMediaPlayer.MEDIA_INFO_AUDIO_RENDERING_START:
                            Log.d(TAG, "MEDIA_INFO_AUDIO_RENDERING_START:");
                            break;
                        default:
                            break;
                    }
                    return true;
                }
            };

    private IMediaPlayer.OnErrorListener mErrorListener =
            new IMediaPlayer.OnErrorListener() {
                public boolean onError(IMediaPlayer mp, int framework_err, int impl_err) {
                    Log.d(TAG, "Error: " + framework_err + "," + impl_err);
                    mCurrentState = PlayerState.CURRENT_STATE_ERROR;
                    mTargetState = PlayerState.CURRENT_STATE_ERROR;

                    /* If an error handler has been supplied, use it and finish. */
                    if (mOnErrorListener != null) {
                        if (mOnErrorListener.onError(mMediaPlayer, framework_err, impl_err)) {
                            return true;
                        }
                    }

                    return true;
                }
            };

    private IMediaPlayer.OnBufferingUpdateListener mBufferingUpdateListener =
            new IMediaPlayer.OnBufferingUpdateListener() {
                public void onBufferingUpdate(IMediaPlayer mp, int percent) {
                    mCurrentBufferPercentage = percent;
                }
            };

    private IMediaPlayer.OnSeekCompleteListener mSeekCompleteListener = new IMediaPlayer.OnSeekCompleteListener() {

        @Override
        public void onSeekComplete(IMediaPlayer mp) {
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
