package tv.lycam.player.widget;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.IntRange;
import android.util.AttributeSet;
import android.view.Surface;
import android.view.TextureView;

import com.alivc.player.ScalableType;

import java.util.regex.Pattern;

import tv.lycam.player.callback.IMediaStatus;
import tv.lycam.player.utils.MeasureHelper;

/**
 * @author 诸葛不亮
 * @version 1.0
 * @description
 */

public abstract class IVideoView extends TextureView {
    public static final int TYPE_ALIPLAYER = 1;
    public static final int TYPE_IJKPLAYER = 2;
    // 常量
    protected static final Pattern rtmpUrlPattern = Pattern.compile("^rtmp://([^/:]+)(:(\\d+))*/([^/]+)(/(.*))*$");

    private MeasureHelper mMeasureHelper;

    protected IMediaStatus mIMediaStatus;

    public IVideoView(Context context) {
        this(context, null);
    }

    public IVideoView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public IVideoView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mMeasureHelper = new MeasureHelper(this);
    }

    //--------------------
    // Layout & Measure
    //--------------------
    public void setVideoSize(int videoWidth, int videoHeight) {
        if (mMeasureHelper != null && videoWidth > 0 && videoHeight > 0) {
            mMeasureHelper.setVideoSize(videoWidth, videoHeight);
            requestLayout();
        }
    }

    public void setVideoSampleAspectRatio(int videoSarNum, int videoSarDen) {
        if (mMeasureHelper != null && videoSarNum > 0 && videoSarDen > 0) {
            mMeasureHelper.setVideoSampleAspectRatio(videoSarNum, videoSarDen);
            requestLayout();
        }
    }

    public void setVideoRotation(int degree) {
        if (mMeasureHelper != null) {
            mMeasureHelper.setVideoRotation(degree);
        }
        setRotation(degree);
    }

    public void setAspectRatio(int aspectRatio) {
        if (mMeasureHelper != null) {
            mMeasureHelper.setAspectRatio(aspectRatio);
            requestLayout();
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        mMeasureHelper.doMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(mMeasureHelper.getMeasuredWidth(), mMeasureHelper.getMeasuredHeight());
    }

    /**
     * 获取当前屏幕旋转角度
     *
     * @param activity
     * @return 0表示是竖屏; 90表示是左横屏; 180表示是反向竖屏; 270表示是右横屏
     */
    public static int getDisplayRotation(Activity activity) {
        if (activity == null)
            return 0;
        int rotation = activity.getWindowManager().getDefaultDisplay()
                .getRotation();
        switch (rotation) {
            case Surface.ROTATION_0:
                return 0;
            case Surface.ROTATION_90:
                return 90;
            case Surface.ROTATION_180:
                return 180;
            case Surface.ROTATION_270:
                return 270;
        }
        return 0;
    }

    /**
     * 当前播放的是否是直播
     */
    public static boolean isLive(String url) {
        return url != null && rtmpUrlPattern.matcher(url).matches();
    }

    /**
     * 设置缩放类型
     *
     * @param scalableType
     */
    public abstract void setScalableType(ScalableType scalableType);


    public void setVideoPath(String url) {
        boolean isLiveStream = isLive(url);
        setVideoPath(url, isLiveStream);
    }

    /**
     * 设置播放地址
     *
     * @param url
     * @param isLiveStream
     */
    public abstract void setVideoPath(String url, boolean isLiveStream);

    public void setIMediaStatus(IMediaStatus iMediaStatus) {
        mIMediaStatus = iMediaStatus;
    }

    /**
     * 设置播放器状态
     *
     * @param state
     */
    protected abstract void setStateAndUi(int state);

    /**
     * @param decoderType 解码器类型。0代表硬件解码器；1代表软件解码器。
     */
    public abstract void setDefaultDecoder(@IntRange(from = 0, to = 1) int decoderType);

    public abstract void pause();

    public abstract void resume();

    public abstract void start();

    public abstract void stop();

    public abstract void destroy();

    public abstract void enableNativeLog();

    public abstract void disableNativeLog();

    public abstract int getCurrentPosition();

    public abstract int getDuration();

    public abstract int getBufferPosition();

    public abstract boolean isLiveMode();

    public abstract void setMuteMode(boolean muteMode);

    public abstract String getVideoPath();

    public abstract boolean isPlaying();

    public abstract void seekTo(int time);
}
