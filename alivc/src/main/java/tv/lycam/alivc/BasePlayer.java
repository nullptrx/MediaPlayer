package tv.lycam.alivc;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.KeyEvent;

import com.alivc.player.MediaPlayer;

import java.util.regex.Pattern;

import tv.lycam.alivc.utils.AudioMngHelper;


/**
 * @author 诸葛不亮
 * @version 1.0
 * @description
 */

public abstract class BasePlayer extends AbstractPlayer {
    // 常量
    protected static final Pattern rtmpUrlPattern = Pattern.compile("^rtmp://([^/:]+)(:(\\d+))*/([^/]+)(/(.*))*$");
    // 界面逻辑
    // 判断是否seekto完成
//    protected boolean mSeekCompleted = true;

    protected boolean mPausing;

    public BasePlayer(@NonNull Context context) {
        super(context);
    }

    public BasePlayer(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public BasePlayer(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMediaCompleted() {
    }

    @Override
    protected void onMediaSeekCompleted() {
        super.onMediaSeekCompleted();
    }

    public void seekTo(int time) {
        if (mMediaPlayer != null) {
            mMediaPlayer.seekTo(time);
        }
    }

    public void seekToAccurate(int time) {
        if (mMediaPlayer != null) {
            mMediaPlayer.seekToAccurate(time);
        }
    }

    /**
     * 当前播放的是否是直播
     */
    protected boolean isLive(String url) {
        return url != null && rtmpUrlPattern.matcher(url).matches();
    }

    /**
     * {@link MediaPlayer.VideoScalingMode}
     *
     * @param scalingMode
     * @return
     */
    public BasePlayer setVideoScalingMode(MediaPlayer.VideoScalingMode scalingMode) {
        if (mMediaPlayer != null) {
            mMediaPlayer.setVideoScalingMode(scalingMode);
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
        return mMediaPlayer != null && mMediaPlayer.isPlaying();
    }

    public void savePlayerState() {
        if (mMediaPlayer.isPlaying()) {
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

    public void start() {
        mMediaPlayer.stop();
        prepareAndPlay();
    }

    public void stop() {
        super.stop();
    }

    @Override
    public void destroy() {
        stop();
        super.destroy();
    }

    public boolean isLiveMode() {
        return isLiveStream;
    }

    public void setMute(boolean mute) {
        if (mMediaPlayer != null) {
            mMediaPlayer.setMuteMode(mute);
        }
    }

    public String getVideoPath() {
        return mStreamUrl;
    }
}
