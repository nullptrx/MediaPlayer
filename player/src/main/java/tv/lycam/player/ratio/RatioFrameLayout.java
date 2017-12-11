package tv.lycam.player.ratio;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

import tv.lycam.player.R;


/**
 * Created by Modificator on 2015/8/22.
 */
public class RatioFrameLayout extends FrameLayout {

    private static final int DEFAULT_RATIO_WIDTH = 16;
    private static final int DEFAULT_RATIO_HEIGHT = 9;
    /**
     * 以哪边为参考，默认为宽
     */
    ReferenceType reference = ReferenceType.WIDTH;
    /**
     * 宽的比例
     */
    double ratioWidth = DEFAULT_RATIO_WIDTH;
    /**
     * 高的比例
     */
    double ratioHeight = DEFAULT_RATIO_HEIGHT;
    /**
     * 判断是否竖屏, 默认竖屏
     */
    protected boolean mIsPortrait = true;
    // 是否启用高宽比例控制, 默认true
    private boolean ratioEnabled = true;


    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

    }

    public RatioFrameLayout(Context context) {
        this(context, null);
    }

    public RatioFrameLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RatioFrameLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.RatioLayout, defStyleAttr, 0);
        //获取参考边
        reference = typedArray.getInt(R.styleable.RatioLayout_reference, 0) == 0 ? ReferenceType.WIDTH : ReferenceType.HEIGHT;
        //获取宽比例
        ratioWidth = typedArray.getFloat(R.styleable.RatioLayout_ratioWidth, DEFAULT_RATIO_WIDTH);
        //获取高比例
        ratioHeight = typedArray.getFloat(R.styleable.RatioLayout_ratioHeight, DEFAULT_RATIO_HEIGHT);

        ratioEnabled = typedArray.getBoolean(R.styleable.RatioLayout_ratioEnabled, true);

        typedArray.recycle();

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (!mIsPortrait || !ratioEnabled) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            return;
        }
        /**
         * 如果以宽慰基准边则宽不变，高按比例得出具体数值，反之亦然
         */
        setMeasuredDimension(View.getDefaultSize(0, reference == ReferenceType.WIDTH ? widthMeasureSpec :
                        (int) (heightMeasureSpec / ratioHeight * ratioWidth)),
                View.getDefaultSize(0, reference == ReferenceType.HEIGHT ? heightMeasureSpec :
                        (int) (widthMeasureSpec / ratioWidth * ratioHeight)));

        int childSpec = reference == ReferenceType.WIDTH ? getMeasuredWidth() : getMeasuredHeight();
        /**
         * 获取非基准边的尺寸
         */
        int measureSpec = reference == ReferenceType.HEIGHT ? MeasureSpec.makeMeasureSpec(
                (int) (childSpec / ratioHeight * ratioWidth), MeasureSpec.EXACTLY) :
                MeasureSpec.makeMeasureSpec(
                        (int) (childSpec / ratioWidth * ratioHeight), MeasureSpec.EXACTLY);

        super.onMeasure(reference == ReferenceType.WIDTH ? widthMeasureSpec : measureSpec, reference == ReferenceType.HEIGHT ? heightMeasureSpec : measureSpec);
    }

    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mIsPortrait = newConfig.orientation == Configuration.ORIENTATION_PORTRAIT;
    }
}
