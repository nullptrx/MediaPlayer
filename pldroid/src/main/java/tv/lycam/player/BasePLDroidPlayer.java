package tv.lycam.player;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.WindowManager;

import com.pili.pldroid.player.AVOptions;
import com.pili.pldroid.player.PLMediaPlayer;

import java.util.regex.Pattern;

import tv.lycam.player.utils.AudioMngHelper;
import tv.lycam.player.utils.CommonUtil;

/**
 * @author 诸葛不亮
 * @version 1.0
 * @description
 */

public abstract class BasePLDroidPlayer extends AbstractPLDroidPlayer {
    // 常量
    protected static final Pattern rtmpUrlPattern = Pattern.compile("^rtmp://([^/:]+)(:(\\d+))*/([^/]+)(/(.*))*$");
    // 界面逻辑
    protected String mStreamUrl;
    // 判断是否seekto完成
//    protected boolean mSeekCompleted = true;

    public BasePLDroidPlayer(@NonNull Context context) {
        super(context);
    }

    public BasePLDroidPlayer(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public BasePLDroidPlayer(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMediaCompletion(PLMediaPlayer plMediaPlayer) {
        super.onMediaCompletion(plMediaPlayer);
    }

    @Override
    protected void onMediaSeekComplete(PLMediaPlayer plMediaPlayer) {
        super.onMediaSeekComplete(plMediaPlayer);
    }

    @Override
    public void pause() {
        if (mVideoView != null) {
            mVideoView.pause();
        }
    }

    @Override
    public void start() {
        if (TextUtils.isEmpty(mStreamUrl)) {
            return;
        }
        if (mVideoView != null) {
            mVideoView.start();
        }
        Activity activity = CommonUtil.getActivityContext(mContext);
        if (activity != null) {
            activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
    }

    @Override
    public void stop() {
        Activity activity = CommonUtil.getActivityContext(mContext);
        if (activity != null) {
            activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
        if (mVideoView != null) {
            mVideoView.stopPlayback();
        }
    }

    public void seekTo(long time) {
        if (mVideoView != null) {
            mVideoView.seekTo(time);
        }
    }

    public static AVOptions getDefaultAVOptions() {
        AVOptions options = new AVOptions();
        // the unit of timeout is ms
        options.setInteger(AVOptions.KEY_PREPARE_TIMEOUT, 10 * 1000);
        // 1 -> hw codec enable, 0 -> disable [recommended]
        options.setInteger(AVOptions.KEY_MEDIACODEC, 0);
//        boolean cache = getIntent().getBooleanExtra("cache", false);
//        if (!mIsLiveStreaming && cache) {
//            options.setString(AVOptions.KEY_CACHE_DIR, Config.DEFAULT_CACHE_DIR);
//        }
        return options;
    }

    /**
     * 当前播放的是否是直播
     */
    protected boolean isLive(String url) {
        return url != null && rtmpUrlPattern.matcher(url).matches();
    }

    public BasePLDroidPlayer setVideoPath(String url) {
        mStreamUrl = url;
        if (TextUtils.isEmpty(url)) {
            return this;
        }
        if (mVideoView != null) {
            AVOptions options = getDefaultAVOptions();
            options.setInteger(AVOptions.KEY_LIVE_STREAMING, isLive(url) ? 1 : 0);
            mVideoView.setAVOptions(options);
            mVideoView.setVideoPath(url);
        }
        return this;
    }

    /**
     * ASPECT_RATIO_ORIGIN = 0;
     * ASPECT_RATIO_FIT_PARENT = 1;
     * ASPECT_RATIO_PAVED_PARENT = 2;
     * ASPECT_RATIO_16_9 = 3;
     * ASPECT_RATIO_4_3 = 4;
     *
     * @param displayAspectRatio
     * @return
     */
    public BasePLDroidPlayer setDisplayAspectRatio(int displayAspectRatio) {
        if (mVideoView != null) {
            mVideoView.setDisplayAspectRatio(displayAspectRatio);
        }
        return this;
    }

    /**
     * 0,90,270,360
     *
     * @param rotation
     * @return
     */
    public BasePLDroidPlayer setDisplayOrientation(int rotation) {
        if (mVideoView != null) {
            mVideoView.setDisplayOrientation(rotation);
        }
        return this;
    }

    public boolean onVolumeKeyDown(int keyCode) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                AudioMngHelper.instance(mContext).subVoiceSystem();
                return true;
            case KeyEvent.KEYCODE_VOLUME_UP:
                AudioMngHelper.instance(mContext).addVoiceSystem();
                return true;
            case KeyEvent.KEYCODE_VOLUME_MUTE:
                break;
        }
        return false;
    }

    public boolean isPlaying() {
        return mVideoView != null && mVideoView.isPlaying();
    }


}
