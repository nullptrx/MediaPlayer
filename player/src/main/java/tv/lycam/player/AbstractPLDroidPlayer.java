package tv.lycam.player;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.InflateException;
import android.view.View;
import android.view.WindowManager;

import com.pili.pldroid.player.PLMediaPlayer;
import com.pili.pldroid.player.widget.PLVideoTextureView;

import tv.lycam.player.ratio.RatioFrameLayout;
import tv.lycam.player.utils.CommonUtil;
import tv.lycam.player.utils.Debugger;

/**
 * @author 诸葛不亮
 * @version 1.0
 * @description
 */

public abstract class AbstractPLDroidPlayer extends RatioFrameLayout {

    public static final String TAG = "PLDroidPlayer";
    //日志打印
    protected boolean DEBUG = false;

    // 默认缩放模式
    protected int DEFAULT_ASPECTRATIO = PLVideoTextureView.ASPECT_RATIO_16_9;

    //上下文
    protected Context mContext;
    //屏幕宽度
    protected int mScreenWidth;

    //屏幕高度
    protected int mScreenHeight;
    // 七牛
    protected PLVideoTextureView mVideoView;

    public AbstractPLDroidPlayer(@NonNull Context context) {
        super(context);
        init(context);
    }

    public AbstractPLDroidPlayer(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public AbstractPLDroidPlayer(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
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
        mVideoView = findViewById(getPlayerId());
        if (mVideoView == null) {
            throw new InflateException("The layout has not include PLVideoTextureView!");
        }
        mVideoView.setOnPreparedListener(mOnBasePreparedListener);
        mVideoView.setOnInfoListener(mOnBaseInfoListener);
        mVideoView.setOnVideoSizeChangedListener(mOnBaseVideoSizeChangedListener);
        mVideoView.setOnBufferingUpdateListener(mOnBaseBufferingUpdateListener);
        mVideoView.setOnCompletionListener(mOnBaseCompletionListener);
        mVideoView.setOnErrorListener(mOnBaseErrorListener);
        mVideoView.setOnSeekCompleteListener(mOnBaseSeekCompleteListener);
        mVideoView.setDisplayAspectRatio(DEFAULT_ASPECTRATIO);
        mVideoView.setDebugLoggingEnabled(DEBUG);
    }

    private void initInflate(Context context) {
        try {
            View.inflate(context, getRootLayoutId(), this);
        } catch (InflateException e) {
            e.printStackTrace();
        }
    }

    /**
     * 当前UI
     */
    protected abstract int getRootLayoutId();

    protected abstract int getPlayerId();

    protected abstract void pause();

    protected abstract void start();

    protected abstract void stop();


    protected void onMediaPrepared(PLMediaPlayer plMediaPlayer, int what) {
    }

    protected void onMediaInfo(PLMediaPlayer plMediaPlayer, int what, int extra) {
    }

    protected void onMediaError(PLMediaPlayer plMediaPlayer, int errorCode) {
    }

    protected void onMediaCompletion(PLMediaPlayer plMediaPlayer) {
    }

    protected void onMediaSeekComplete(PLMediaPlayer plMediaPlayer) {
    }

    protected void onMediaBufferingUpdate(PLMediaPlayer plMediaPlayer, int percent) {
    }

    protected void onMediaVideoSizeChanged(PLMediaPlayer plMediaPlayer, int width, int height) {
    }

    private PLMediaPlayer.OnPreparedListener mOnBasePreparedListener = new PLMediaPlayer.OnPreparedListener() {
        @Override
        public void onPrepared(PLMediaPlayer plMediaPlayer, int what) {
            onMediaPrepared(plMediaPlayer, what);
            if (mOnPreparedListener != null) {
                mOnPreparedListener.onPrepared(plMediaPlayer, what);
            }
        }
    };

    private PLMediaPlayer.OnInfoListener mOnBaseInfoListener = new PLMediaPlayer.OnInfoListener() {
        @Override
        public boolean onInfo(PLMediaPlayer plMediaPlayer, int what, int extra) {
            Debugger.printfLog(TAG, "OnInfo, what = " + what + ", extra = " + extra);
            switch (what) {
                case PLMediaPlayer.MEDIA_INFO_BUFFERING_START:
                    break;
                case PLMediaPlayer.MEDIA_INFO_BUFFERING_END:
                    break;
                case PLMediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START:
                    Debugger.printfLog(TAG, "First video render time: " + extra + "ms");
                    break;
                case PLMediaPlayer.MEDIA_INFO_AUDIO_RENDERING_START:
                    Debugger.printfLog(TAG, "First audio render time: " + extra + "ms");
                    break;
                case PLMediaPlayer.MEDIA_INFO_VIDEO_FRAME_RENDERING:
                    Debugger.printfLog(TAG, "video frame rendering, ts = " + extra);
                    break;
                case PLMediaPlayer.MEDIA_INFO_AUDIO_FRAME_RENDERING:
                    Debugger.printfLog(TAG, "audio frame rendering, ts = " + extra);
                    break;
                case PLMediaPlayer.MEDIA_INFO_VIDEO_GOP_TIME:
                    Debugger.printfLog(TAG, "Gop Time: " + extra);
                    break;
                case PLMediaPlayer.MEDIA_INFO_SWITCHING_SW_DECODE:
                    Debugger.printfLog(TAG, "Hardware decoding failure, switching software decoding!");
                    break;
                case PLMediaPlayer.MEDIA_INFO_METADATA:
                    Debugger.printfLog(TAG, mVideoView.getMetadata().toString());
                    break;
                case PLMediaPlayer.MEDIA_INFO_VIDEO_BITRATE:
                case PLMediaPlayer.MEDIA_INFO_VIDEO_FPS:
                    updateStatInfo();
                    break;
                case PLMediaPlayer.MEDIA_INFO_CONNECTED:
                    Debugger.printfLog(TAG, "Connected !");
                    break;
                default:
                    break;
            }
            onMediaInfo(plMediaPlayer, what, extra);
            if (mOnInfoListener != null) {
                mOnInfoListener.onInfo(plMediaPlayer, what, extra);
            }
            return true;
        }
    };

    protected void updateStatInfo() {
//        long bitrate = mVideoView.getVideoBitrate() / 1024;
//        final String stat = bitrate + "kbps, " + mVideoView.getVideoFps() + "fps";
//        runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//                mStatInfoTextView.setText(stat);
//            }
//        });
    }

    private PLMediaPlayer.OnErrorListener mOnBaseErrorListener = new PLMediaPlayer.OnErrorListener() {
        @Override
        public boolean onError(PLMediaPlayer mp, int errorCode) {
            //Log.e(TAG, "Error happened, errorCode = " + errorCode);
            switch (errorCode) {
                case PLMediaPlayer.ERROR_CODE_IO_ERROR:
                    /**
                     * SDK will do reconnecting automatically
                     */
                    //showToastTips("IO Error !");
                    return false;
                case PLMediaPlayer.ERROR_CODE_OPEN_FAILED:
                    //showToastTips("failed to open player !");
                    break;
                case PLMediaPlayer.ERROR_CODE_SEEK_FAILED:
                    //showToastTips("failed to seek !");
                    break;
                default:
                    //showToastTips("unknown error !");
                    break;
            }
//            finish();
            onMediaError(mp, errorCode);
            if (mOnErrorListener != null) {
                mOnErrorListener.onError(mp, errorCode);
            }
            return true;
        }
    };

    private PLMediaPlayer.OnCompletionListener mOnBaseCompletionListener = new PLMediaPlayer.OnCompletionListener() {
        @Override
        public void onCompletion(PLMediaPlayer plMediaPlayer) {
            onMediaCompletion(plMediaPlayer);
            if (mOnCompletionListener != null) {
                mOnCompletionListener.onCompletion(plMediaPlayer);
            }
        }
    };

    private PLMediaPlayer.OnBufferingUpdateListener mOnBaseBufferingUpdateListener = new PLMediaPlayer.OnBufferingUpdateListener() {
        @Override
        public void onBufferingUpdate(PLMediaPlayer plMediaPlayer, int percent) {
            onMediaBufferingUpdate(plMediaPlayer, percent);
            if (mOnBufferingUpdateListener != null) {
                mOnBufferingUpdateListener.onBufferingUpdate(plMediaPlayer, percent);
            }
        }
    };

    private PLMediaPlayer.OnVideoSizeChangedListener mOnBaseVideoSizeChangedListener = new PLMediaPlayer.OnVideoSizeChangedListener() {
        @Override
        public void onVideoSizeChanged(PLMediaPlayer plMediaPlayer, int width, int height) {
            if (width > height) {
                mVideoView.setSplitMode(PLVideoTextureView.SPLIT_MODE_NONE);
            } else {
                mVideoView.setSplitMode(PLVideoTextureView.SPLIT_MODE_VERTICAL);
            }
            onMediaVideoSizeChanged(plMediaPlayer, width, height);
            if (mOnVideoSizeChangedListener != null) {
                mOnVideoSizeChangedListener.onVideoSizeChanged(plMediaPlayer, width, height);
            }
        }
    };

    private PLMediaPlayer.OnSeekCompleteListener mOnBaseSeekCompleteListener = new PLMediaPlayer.OnSeekCompleteListener() {
        @Override
        public void onSeekComplete(PLMediaPlayer plMediaPlayer) {
            onMediaSeekComplete(plMediaPlayer);
        }
    };

//    private MediaController.OnClickSpeedAdjustListener mOnClickSpeedAdjustListener = new MediaController.OnClickSpeedAdjustListener() {
//        @Override
//        public void onClickNormal() {
//            // 0x0001/0x0001 = 2
//            mVideoView.setPlaySpeed(0X00010001);
//        }
//
//        @Override
//        public void onClickFaster() {
//            // 0x0002/0x0001 = 2
//            mVideoView.setPlaySpeed(0X00020001);
//        }
//
//        @Override
//        public void onClickSlower() {
//            // 0x0001/0x0002 = 0.5
//            mVideoView.setPlaySpeed(0X00010002);
//        }
//    };

    private PLMediaPlayer.OnPreparedListener mOnPreparedListener;
    private PLMediaPlayer.OnInfoListener mOnInfoListener;
    private PLMediaPlayer.OnErrorListener mOnErrorListener;
    private PLMediaPlayer.OnCompletionListener mOnCompletionListener;
    private PLMediaPlayer.OnBufferingUpdateListener mOnBufferingUpdateListener;
    private PLMediaPlayer.OnVideoSizeChangedListener mOnVideoSizeChangedListener;

    public void setOnPreparedListener(PLMediaPlayer.OnPreparedListener onPreparedListener) {
        mOnPreparedListener = onPreparedListener;
    }

    public void setOnInfoListener(PLMediaPlayer.OnInfoListener onInfoListener) {
        mOnInfoListener = onInfoListener;
    }

    public void setOnErrorListener(PLMediaPlayer.OnErrorListener onErrorListener) {
        mOnErrorListener = onErrorListener;
    }

    public void setOnCompletionListener(PLMediaPlayer.OnCompletionListener onCompletionListener) {
        mOnCompletionListener = onCompletionListener;
    }

    public void setOnBufferingUpdateListener(PLMediaPlayer.OnBufferingUpdateListener onBufferingUpdateListener) {
        mOnBufferingUpdateListener = onBufferingUpdateListener;
    }

    public void setOnVideoSizeChangedListener(PLMediaPlayer.OnVideoSizeChangedListener onVideoSizeChangedListener) {
        mOnVideoSizeChangedListener = onVideoSizeChangedListener;
    }
}
