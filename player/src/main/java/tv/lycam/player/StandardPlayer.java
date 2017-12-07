package tv.lycam.player;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import tv.lycam.player.utils.AudioMngHelper;
import tv.lycam.player.utils.NetWatchdog;
import tv.lycam.player.widget.ENDownloadView;


/**
 * @author 诸葛不亮
 * @version 1.0
 * @description
 */

public class StandardPlayer extends BaseControllerPlayer {
    //亮度dialog
    protected Dialog mBrightnessDialog;

    //音量dialog
    protected Dialog mVolumeDialog;

    //触摸进度dialog
    protected Dialog mProgressDialog;

    //触摸进度条的progress
    protected ProgressBar mDialogProgressBar;

    //音量进度条的progress
    protected ProgressBar mDialogVolumeProgressBar;

    //亮度文本
    protected TextView mBrightnessDialogTv;

    //触摸移动显示文本
    protected TextView mDialogSeekTime;

    //触摸移动显示全部时间
    protected TextView mDialogTotalTime;

    //触摸移动方向icon
    protected ImageView mDialogIcon;

    //封面
    protected View mThumbImageView;
    //封面父布局
    protected RelativeLayout mThumbImageViewLayout;
    //top
    protected View mTopContainerView;
    // top parent
    protected RelativeLayout mTopContainerViewLayout;

    //loading view
    protected View mLoadingProgressBar;

    protected Drawable mBottomProgressDrawable;

    protected Drawable mBottomShowProgressDrawable;

    protected Drawable mBottomShowProgressThumbDrawable;

    protected Drawable mVolumeProgressDrawable;

    protected Drawable mDialogProgressBarDrawable;

    protected int mDialogProgressHighLightColor = -11;

    protected int mDialogProgressNormalColor = -11;

    public StandardPlayer(@NonNull Context context) {
        super(context);
    }

    public StandardPlayer(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public StandardPlayer(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void initCustomView() {
        super.initCustomView();
        //增加自定义ui

//        if (mBottomProgressDrawable != null) {
//            mBottomProgress.setProgressDrawable(mBottomProgressDrawable);
//        }

        if (mBottomShowProgressDrawable != null) {
            mProgressBar.setProgressDrawable(mBottomProgressDrawable);
        }

        if (mBottomShowProgressThumbDrawable != null) {
            mProgressBar.setThumb(mBottomShowProgressThumbDrawable);
        }

        mLoadingProgressBar = findViewById(R.id.lycam_player_loading);

        mThumbImageViewLayout = findViewById(R.id.lycam_player_thumb);

        if (mThumbImageViewLayout != null) {
            mThumbImageViewLayout.setVisibility(GONE);
        }
        if (mThumbImageView != null && mIsPortrait && mThumbImageViewLayout != null) {
            mThumbImageViewLayout.removeAllViews();
            resolveThumbImage(mThumbImageView);
        }

        mTopContainerViewLayout = findViewById(R.id.lycam_player_layout_top);

        if (mTopContainerViewLayout != null) {
            mTopContainerViewLayout.setVisibility(GONE);
        }
        if (mTopContainerView != null && mIsPortrait && mTopContainerViewLayout != null) {
            mTopContainerViewLayout.removeAllViews();
            resolveThumbImage(mTopContainerView);
        }
    }

    private void resolveThumbImage(View thumb) {
        if (mThumbImageViewLayout != null) {
            mThumbImageViewLayout.removeAllViews();
            mThumbImageViewLayout.addView(thumb);
            ViewGroup.LayoutParams layoutParams = thumb.getLayoutParams();
            layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT;
            layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
            thumb.setLayoutParams(layoutParams);
        }
        changeUiToNormal();
    }

    private void resolveTopContainer(View container) {
        if (mTopContainerViewLayout != null) {
            mTopContainerViewLayout.removeAllViews();
            mTopContainerViewLayout.addView(container);
            ViewGroup.LayoutParams layoutParams = container.getLayoutParams();
            layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
            layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
            container.setLayoutParams(layoutParams);
        }
    }

    @Override
    protected void showWifiDialog() {
        if (!NetWatchdog.hasNet(mContext)) {
            Toast.makeText(mContext, getResources().getString(R.string.no_net), Toast.LENGTH_LONG).show();
            return;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setMessage(getResources().getString(R.string.tips_not_wifi));
        builder.setPositiveButton(getResources().getString(R.string.tips_not_wifi_confirm), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                start();
            }
        });
        builder.setNegativeButton(getResources().getString(R.string.tips_not_wifi_cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.create().show();
    }

    /**
     * 触摸显示滑动进度dialog，如需要自定义继承重写即可，记得重写dismissProgressDialog
     */
    @Override
    public void showProgressDialog(float deltaX, String seekTime, int seekTimePosition, String totalTime, int totalTimeDuration) {
        if (mProgressDialog == null) {
            View localView = LayoutInflater.from(mContext).inflate(R.layout.video_progress_dialog, null, false);
            mDialogProgressBar = ((ProgressBar) localView.findViewById(R.id.duration_progressbar));
            if (mDialogProgressBarDrawable != null) {
                mDialogProgressBar.setProgressDrawable(mDialogProgressBarDrawable);
            }
            mDialogSeekTime = ((TextView) localView.findViewById(R.id.tv_current));
            mDialogTotalTime = ((TextView) localView.findViewById(R.id.tv_duration));
            mDialogIcon = ((ImageView) localView.findViewById(R.id.duration_image_tip));
            mProgressDialog = new Dialog(mContext, R.style.video_style_dialog_progress);
            mProgressDialog.setContentView(localView);
            Window window = mProgressDialog.getWindow();
            if (window != null) {
                window.addFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
                window.addFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL);
                window.addFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                window.setLayout(getWidth(), getHeight());
                if (mDialogProgressNormalColor != -11) {
                    mDialogTotalTime.setTextColor(mDialogProgressNormalColor);
                }
                if (mDialogProgressHighLightColor != -11) {
                    mDialogSeekTime.setTextColor(mDialogProgressHighLightColor);
                }
                WindowManager.LayoutParams localLayoutParams = window.getAttributes();
                localLayoutParams.gravity = Gravity.TOP;
                localLayoutParams.width = getWidth();
                localLayoutParams.height = getHeight();
                int location[] = new int[2];
                getLocationOnScreen(location);
                localLayoutParams.x = location[0];
                localLayoutParams.y = location[1];
                window.setAttributes(localLayoutParams);
            }
        }
        if (!mProgressDialog.isShowing()) {
            mProgressDialog.show();
        }

        mDialogSeekTime.setText(seekTime);
        mDialogTotalTime.setText(" / " + totalTime);
        if (totalTimeDuration > 0)
            mDialogProgressBar.setProgress(seekTimePosition * 100 / totalTimeDuration);
        if (deltaX > 0) {
            mDialogIcon.setBackgroundResource(R.drawable.video_forward_icon);
        } else {
            mDialogIcon.setBackgroundResource(R.drawable.video_backward_icon);
        }
    }

    @Override
    public void dismissProgressDialog() {
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
            mProgressDialog = null;
        }
    }

    /**
     * 触摸音量dialog，如需要自定义继承重写即可，记得重写dismissVolumeDialog
     */
    @Override
    public void showVolumeDialog(float deltaY, int volumePercent) {
        if (mVolumeDialog == null) {
            View localView = LayoutInflater.from(mContext).inflate(R.layout.video_volume_dialog, null);
            mDialogVolumeProgressBar = ((ProgressBar) localView.findViewById(R.id.volume_progressbar));
            if (mVolumeProgressDrawable != null) {
                mDialogVolumeProgressBar.setProgressDrawable(mVolumeProgressDrawable);
            }
            mVolumeDialog = new Dialog(mContext, R.style.video_style_dialog_progress);
            mVolumeDialog.setContentView(localView);
            Window window = mVolumeDialog.getWindow();
            if (window != null) {
                window.addFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
                window.addFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL);
                window.addFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                window.setLayout(-2, -2);
                WindowManager.LayoutParams localLayoutParams = window.getAttributes();
                localLayoutParams.gravity = Gravity.TOP | Gravity.LEFT;
                localLayoutParams.width = getWidth();
                localLayoutParams.height = getHeight();
                int location[] = new int[2];
                getLocationOnScreen(location);
                localLayoutParams.x = location[0];
                localLayoutParams.y = location[1];
                window.setAttributes(localLayoutParams);
            }
        }
        if (!mVolumeDialog.isShowing()) {
            mVolumeDialog.show();
        }

        mDialogVolumeProgressBar.setProgress(volumePercent);
    }

    @Override
    public void dismissVolumeDialog() {
        if (mVolumeDialog != null) {
            mVolumeDialog.dismiss();
            mVolumeDialog = null;
        }
    }

    /**
     * 触摸亮度dialog，如需要自定义继承重写即可，记得重写dismissBrightnessDialog
     */
    @Override
    public void showBrightnessDialog(float percent) {
        if (mBrightnessDialog == null) {
            View localView = LayoutInflater.from(mContext).inflate(R.layout.video_brightness, null);
            mBrightnessDialogTv = (TextView) localView.findViewById(R.id.video_brightness);
            mBrightnessDialog = new Dialog(mContext, R.style.video_style_dialog_progress);
            mBrightnessDialog.setContentView(localView);
            Window window = mBrightnessDialog.getWindow();
            if (window != null) {
                window.addFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
                window.addFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL);
                window.addFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                window.setLayout(-2, -2);
                WindowManager.LayoutParams localLayoutParams = window.getAttributes();
                localLayoutParams.gravity = Gravity.TOP | Gravity.RIGHT;
                localLayoutParams.width = getWidth();
                localLayoutParams.height = getHeight();
                int location[] = new int[2];
                getLocationOnScreen(location);
                localLayoutParams.x = location[0];
                localLayoutParams.y = location[1];
                window.setAttributes(localLayoutParams);
            }
        }
        if (!mBrightnessDialog.isShowing()) {
            mBrightnessDialog.show();
        }
        if (mBrightnessDialogTv != null)
            mBrightnessDialogTv.setText((int) (percent * 100) + "%");
    }

    @Override
    public void dismissBrightnessDialog() {
        if (mBrightnessDialog != null) {
            mBrightnessDialog.dismiss();
            mBrightnessDialog = null;
        }
    }

    public boolean onVolumeKeyDown(int keyCode) {
        int volume = AudioMngHelper.instance(mContext).get100CurrentVolume();
        showVolumeDialog(0, volume);
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

    @Override
    public void onClickUiToggle() {
//        if (mIfCurrentIsFullscreen && mLockCurScreen && mNeedLockFull) {
//            setViewShowState(mLockScreen, VISIBLE);
//            return;
//        }
        if (mCurrentState == PlayerState.CURRENT_STATE_PREPAREING) {
            if (mBottomContainer != null) {
                if (mBottomContainer.getVisibility() == View.VISIBLE) {
                    hideStatusBar();
                    changeUiToPrepareingClear();
                } else {
                    showStatusBar();
                    changeUiToPreparingShow();
                }
            }
        } else if (mCurrentState == PlayerState.CURRENT_STATE_PLAYING) {
            if (mBottomContainer != null) {
                if (mBottomContainer.getVisibility() == View.VISIBLE) {
                    hideStatusBar();
                    changeUiToPlayingClear();
                } else {
                    showStatusBar();
                    changeUiToPlayingShow();
                }
            }
        } else if (mCurrentState == PlayerState.CURRENT_STATE_PAUSE) {
            if (mBottomContainer != null) {
                if (mBottomContainer.getVisibility() == View.VISIBLE) {
                    hideStatusBar();
                    changeUiToPauseClear();
                } else {
                    showStatusBar();
                    changeUiToPauseShow();
                }
            }
        } else if (mCurrentState == PlayerState.CURRENT_STATE_AUTO_COMPLETE) {
            if (mBottomContainer != null) {
                if (mBottomContainer.getVisibility() == View.VISIBLE) {
                    hideStatusBar();
                    changeUiToCompleteClear();
                } else {
                    showStatusBar();
                    changeUiToCompleteShow();
                }
            }
        } else if (mCurrentState == PlayerState.CURRENT_STATE_PLAYING_BUFFERING_START) {
            if (mBottomContainer != null) {
                if (mBottomContainer.getVisibility() == View.VISIBLE) {
                    hideStatusBar();
                    changeUiToPlayingBufferingClear();
                } else {
                    showStatusBar();
                    changeUiToPlayingBufferingShow();
                }
            }
        }
    }

    protected void changeUiToPrepareingClear() {

        setViewShowState(mTopContainerViewLayout, INVISIBLE);
        setViewShowState(mBottomContainer, INVISIBLE);
        setViewShowState(mLoadingProgressBar, INVISIBLE);
        setViewShowState(mThumbImageViewLayout, INVISIBLE);

        if (mLoadingProgressBar instanceof ENDownloadView) {
            ((ENDownloadView) mLoadingProgressBar).reset();
        }
    }

    protected void changeUiToPlayingClear() {
        changeUiToClear();
    }

    protected void changeUiToPauseClear() {
        changeUiToClear();
    }

    protected void changeUiToPlayingBufferingClear() {

        setViewShowState(mTopContainerViewLayout, INVISIBLE);
        setViewShowState(mBottomContainer, INVISIBLE);
        setViewShowState(mLoadingProgressBar, VISIBLE);
        setViewShowState(mThumbImageViewLayout, INVISIBLE);

        if (mLoadingProgressBar instanceof ENDownloadView) {
            ENDownloadView enDownloadView = (ENDownloadView) mLoadingProgressBar;
            if (enDownloadView.getCurrentState() == ENDownloadView.STATE_PRE) {
                ((ENDownloadView) mLoadingProgressBar).start();
            }
        }
        updateStartImage();
    }

    protected void changeUiToClear() {

        setViewShowState(mTopContainerViewLayout, INVISIBLE);
        setViewShowState(mBottomContainer, INVISIBLE);
        setViewShowState(mLoadingProgressBar, INVISIBLE);
        setViewShowState(mThumbImageViewLayout, INVISIBLE);

        if (mLoadingProgressBar instanceof ENDownloadView) {
            ((ENDownloadView) mLoadingProgressBar).reset();
        }
    }

    protected void changeUiToCompleteClear() {

        setViewShowState(mTopContainerViewLayout, INVISIBLE);
        setViewShowState(mBottomContainer, INVISIBLE);
        setViewShowState(mLoadingProgressBar, INVISIBLE);
        setViewShowState(mThumbImageViewLayout, VISIBLE);

        if (mLoadingProgressBar instanceof ENDownloadView) {
            ((ENDownloadView) mLoadingProgressBar).reset();
        }
        updateStartImage();
    }

    @Override
    public void hideAllWidget() {
        setViewShowState(mBottomContainer, INVISIBLE);
        setViewShowState(mTopContainerViewLayout, INVISIBLE);
    }

    @Override
    protected void changeUiToNormal() {
        setViewShowState(mTopContainerViewLayout, INVISIBLE);
        setViewShowState(mBottomContainer, INVISIBLE);
        setViewShowState(mLoadingProgressBar, INVISIBLE);
        setViewShowState(mThumbImageViewLayout, VISIBLE);

        updateStartImage();
        if (mLoadingProgressBar instanceof ENDownloadView) {
            ((ENDownloadView) mLoadingProgressBar).reset();
        }
    }

    @Override
    protected void changeUiToPreparingShow() {
        setViewShowState(mTopContainerViewLayout, VISIBLE);
        setViewShowState(mBottomContainer, VISIBLE);
        setViewShowState(mLoadingProgressBar, VISIBLE);
        setViewShowState(mThumbImageViewLayout, INVISIBLE);

        if (mLoadingProgressBar != null && mLoadingProgressBar instanceof ENDownloadView) {
            ENDownloadView enDownloadView = (ENDownloadView) mLoadingProgressBar;
            if (enDownloadView.getCurrentState() == ENDownloadView.STATE_PRE) {
                ((ENDownloadView) mLoadingProgressBar).start();
            }
        }
    }

    @Override
    protected void changeUiToPlayingShow() {
        setViewShowState(mTopContainerViewLayout, VISIBLE);
        setViewShowState(mBottomContainer, VISIBLE);
        setViewShowState(mLoadingProgressBar, INVISIBLE);
        setViewShowState(mThumbImageViewLayout, INVISIBLE);

        if (mLoadingProgressBar instanceof ENDownloadView) {
            ((ENDownloadView) mLoadingProgressBar).reset();
        }
        updateStartImage();
    }

    @Override
    protected void changeUiToPauseShow() {
        setViewShowState(mTopContainerViewLayout, VISIBLE);
        setViewShowState(mBottomContainer, VISIBLE);
        setViewShowState(mLoadingProgressBar, INVISIBLE);
        setViewShowState(mThumbImageViewLayout, INVISIBLE);

        if (mLoadingProgressBar instanceof ENDownloadView) {
            ((ENDownloadView) mLoadingProgressBar).reset();
        }
        updateStartImage();
    }

    @Override
    protected void changeUiToError() {
        setViewShowState(mTopContainerViewLayout, INVISIBLE);
        setViewShowState(mBottomContainer, INVISIBLE);
        setViewShowState(mLoadingProgressBar, INVISIBLE);
        setViewShowState(mThumbImageViewLayout, INVISIBLE);

        if (mLoadingProgressBar instanceof ENDownloadView) {
            ((ENDownloadView) mLoadingProgressBar).reset();
        }
        updateStartImage();
    }

    @Override
    protected void changeUiToCompleteShow() {
        setViewShowState(mTopContainerViewLayout, VISIBLE);
        setViewShowState(mBottomContainer, VISIBLE);
        setViewShowState(mLoadingProgressBar, INVISIBLE);
        setViewShowState(mThumbImageViewLayout, VISIBLE);

        if (mLoadingProgressBar instanceof ENDownloadView) {
            ((ENDownloadView) mLoadingProgressBar).reset();
        }
        updateStartImage();
    }

    @Override
    protected void changeUiToPlayingBufferingShow() {
        setViewShowState(mTopContainerViewLayout, VISIBLE);
        setViewShowState(mBottomContainer, VISIBLE);
        setViewShowState(mLoadingProgressBar, VISIBLE);
        setViewShowState(mThumbImageViewLayout, INVISIBLE);

        if (mLoadingProgressBar instanceof ENDownloadView) {
            ENDownloadView enDownloadView = (ENDownloadView) mLoadingProgressBar;
            if (enDownloadView.getCurrentState() == ENDownloadView.STATE_PRE) {
                ((ENDownloadView) mLoadingProgressBar).start();
            }
        }
    }

    /**
     * 定义开始按键显示
     */
    protected void updateStartImage() {
    }

    protected void setViewShowState(View view, int visibility) {
        if (view != null) {
            view.setVisibility(visibility);
        }
    }

    /**
     * 封面布局
     */
    public RelativeLayout getThumbImageViewLayout() {
        return mThumbImageViewLayout;
    }

    /***
     * 设置封面
     */
    public void setThumbImageView(View view) {
        if (mThumbImageViewLayout != null) {
            mThumbImageView = view;
            resolveThumbImage(view);
        }
    }

    /***
     * 设置container
     */
    public void setTopContainerView(View view) {
        if (mTopContainerViewLayout != null) {
            mTopContainerView = view;
            resolveTopContainer(view);
        }
    }

    private void showStatusBar() {
        if (!mIsPortrait) {
            setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        }
    }

    private void hideStatusBar() {
        if (!mIsPortrait) {
            setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);
        }
    }

}
