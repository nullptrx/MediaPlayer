package tv.lycam.alivc.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.TextureView;

import java.util.regex.Pattern;

import tv.lycam.alivc.utils.MeasureHelper;

/**
 * @author 诸葛不亮
 * @version 1.0
 * @description
 */

public class IVideoView extends TextureView {
    // 常量
    protected static final Pattern rtmpUrlPattern = Pattern.compile("^rtmp://([^/:]+)(:(\\d+))*/([^/]+)(/(.*))*$");

    private MeasureHelper mMeasureHelper;

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
            setRotation(degree);
        }
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
     * 当前播放的是否是直播
     */
    protected boolean isLive(String url) {
        return url != null && rtmpUrlPattern.matcher(url).matches();
    }
}
