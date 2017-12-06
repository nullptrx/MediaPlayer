package com.alivc.player;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceView;

import com.alivc.player.logreport.BufferEvent;
import com.alivc.player.logreport.BufferEvent.BufferEventArgs;
import com.alivc.player.logreport.BufferResumeEvent;
import com.alivc.player.logreport.BufferResumeEvent.BufferResumeEventArgs;
import com.alivc.player.logreport.BufferingToLocalEvent;
import com.alivc.player.logreport.BufferingToLocalEvent.BufferingToLocalFinishArgs;
import com.alivc.player.logreport.BufferingToLocalEvent.BufferingToLocalStartArgs;
import com.alivc.player.logreport.DelayEvent;
import com.alivc.player.logreport.DelayEvent.DelayEventArgs;
import com.alivc.player.logreport.DownloadEvent;
import com.alivc.player.logreport.DownloadEvent.DownloadEventArgs;
import com.alivc.player.logreport.EOSEvent;
import com.alivc.player.logreport.ErrorEvent;
import com.alivc.player.logreport.ErrorEvent.ErrorEventArgs;
import com.alivc.player.logreport.PauseEvent;
import com.alivc.player.logreport.PauseResumeEvent;
import com.alivc.player.logreport.PauseResumeEvent.PauseResumeEventArgs;
import com.alivc.player.logreport.PlayEvent;
import com.alivc.player.logreport.PlayEvent.DefinitionPlayMode;
import com.alivc.player.logreport.PlayEvent.PlayEventArgs;
import com.alivc.player.logreport.PublicPraram;
import com.alivc.player.logreport.ReportEvent;
import com.alivc.player.logreport.ReportEvent.ReportEventArgs;
import com.alivc.player.logreport.SeekCompleteEvent;
import com.alivc.player.logreport.SeekCompleteEvent.SeekCompleteEventArgs;
import com.alivc.player.logreport.SeekEvent;
import com.alivc.player.logreport.SeekEvent.SeekEventArgs;
import com.alivc.player.logreport.StartPlayEvent;
import com.alivc.player.logreport.StopEvent;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class AliVcMediaPlayer implements MediaPlayer {
    public static final int AUTH_INTERVAL = 3000;
    private static final int CMD_DESTROY = 5;
    private static final int CMD_PAUSE = 4;
    private static final int CMD_PLAY = 2;
    private static final int CMD_PREPARE = 1;
    private static final int CMD_PREPARE_AND_START = 8;
    private static final int CMD_STOP = 3;
    public static final boolean ENABLE_AUTH = false;
    public static final boolean ENABLE_REPORT = false;
    public static final int INFO_INTERVAL = 5000;
    private static final int MAX_WAITE_COUNT = 5;
    private static final int PAUSE_BUFFERING_TIME = 30000;
    private static final String TAG = "AlivcPlayerJ";
    private static AtomicInteger WaiteForStartCount = new AtomicInteger(0);
    private static AtomicBoolean isCanStart = new AtomicBoolean(true);
    private static Context sContext = null;
    private static boolean sEnableLog = true;
    private int cachetMaxDuration;
    private long cachetMaxSize;
    ScheduledExecutorService executor;
    private boolean isEOS;
    private MediaPlayerBufferingUpdateListener mBufferingUpdateListener;
    private int mCircleCount;
    private MediaPlayerCompletedListener mCompleteListener;
    private int mDefaultDecoder;
    private long mDownLoadDuration;
    private long mDownloadBytes;
    private AliyunErrorCode mErrorCode;
    private MediaPlayerErrorListener mErrorListener;
    private MediaPlayerFrameInfoListener mFrameInfoListener;
    private Handler mHandler;
    private MediaPlayerInfoListener mInfoListener;
    private boolean mIsPublicParamIncome;
    private String mKey;
    private long mLastReportTime;
    private HandlerThread mMediaThread;
    private MediaPlayerPcmDataListener mPcmDataListener;
    private TBMPlayer mPlayer;
    private MediaPlayerPreparedListener mPreparedListener;
    private long mReportIndex;
    private MediaPlayerSeekCompleteListener mSeekCompleteListener;
    private int mSeekPosition;
    private int mStatus;
    private MediaPlayerStoppedListener mStopListener;
    private Surface mSurface;
    private Handler mUIStatusHandler;
    private String mUrl;
    private VideoAdjust mVA;
    private MediaPlayerVideoSizeChangeListener mVideoSizeChangeListener;
    private PublicPraram publicPraram;

    private static class MediaThreadHandler extends Handler {
        private WeakReference<AliVcMediaPlayer> weakPlayer;

        public MediaThreadHandler(Looper looper, AliVcMediaPlayer aliVcMediaPlayer) {
            super(looper);
            this.weakPlayer = new WeakReference(aliVcMediaPlayer);
        }

        public void handleMessage(Message msg) {
            AliVcMediaPlayer aliVcMediaPlayer = (AliVcMediaPlayer) this.weakPlayer.get();
            if (aliVcMediaPlayer != null) {
                aliVcMediaPlayer.handlMediaMesssage(msg);
            }
            super.handleMessage(msg);
        }
    }

    enum PropertyName {
        FLT_VIDEO_DECODE_FPS("dec-fps", 10001),
        FLT_VIDEO_OUTPUT_FSP("out-fps", 10002),
        FLT_FFP_PLAYBACK_RATE("plybk-rate", 10003),
        INT64_SELECT_VIDEO_STREAM("select-v", 20001),
        INT64_SELECT_AUDIO_STREAM("select_a", 20002),
        INT64_VIDEO_DECODER("v-dec", 20003),
        INT64_AUDIO_DECODER("a-dec", 20004),
        INT64_VIDEO_CACHE_DURATION("vcache-dur", "sec", 20005),
        INT64_AUDIO_CACHE_DURATION("acache-dur", "sec", 20006),
        INT64_VIDEO_CACHE_BYTES("vcache-bytes", 20007),
        INT64_AUDIO_CACHE_BYTES("acache-bytes", 20008),
        INT64_VIDEO_CACHE_PACKETS("vcache-pkts", 20009),
        INT64_AUDIO_CACHE_PACKETS("acache-pkts", 20010),
        DOUBLE_CREATE_PLAY_TIME("create_player", 18000),
        DOUBLE_OPEN_FORMAT_TIME("open-url", 18001),
        DOUBLE_FIND_STREAM_TIME("find-stream", 18002),
        DOUBLE_OPEN_STREAM_TIME("open-stream", 18003);

        private int mIndex;
        private String mName;
        private String mSuffix;

        private PropertyName(String name, int index) {
            this.mName = name;
            this.mIndex = index;
            this.mSuffix = new String("");
        }

        private PropertyName(String name, String suffix, int index) {
            this.mName = name;
            this.mIndex = index;
            this.mSuffix = suffix;
        }

        public static String getName(int index) {
            for (PropertyName p : values()) {
                if (p.getIndex() == index) {
                    return p.mName;
                }
            }
            return null;
        }

        public static String getSuffixName(int index) {
            for (PropertyName p : values()) {
                if (p.getIndex() == index) {
                    return p.mSuffix;
                }
            }
            return new String("");
        }

        public String getName() {
            return this.mName;
        }

        public int getIndex() {
            return this.mIndex;
        }
    }

    private static class UIStatusHandler extends Handler {
        private WeakReference<AliVcMediaPlayer> weakPlayer;

        public UIStatusHandler(AliVcMediaPlayer aliVcMediaPlayer) {
            super(Looper.getMainLooper());
            this.weakPlayer = new WeakReference(aliVcMediaPlayer);
        }

        public void handleMessage(Message msg) {
            AliVcMediaPlayer aliVcMediaPlayer = (AliVcMediaPlayer) this.weakPlayer.get();
            if (aliVcMediaPlayer != null) {
                aliVcMediaPlayer.handlUiStatusMesssage(msg);
            }
            super.handleMessage(msg);
        }
    }

    protected static void d(String tag, String message) {
        if (sEnableLog) {
            Log.d(tag, message);
        }
    }

    private void startReportHeart() {
        if (this.executor != null) {
            this.executor.shutdown();
            this.executor = null;
        }
        if (this.executor == null || this.executor.isShutdown()) {
            this.executor = Executors.newSingleThreadScheduledExecutor();
        }
        this.executor.scheduleAtFixedRate(new Runnable() {
            public void run() {
                AliVcMediaPlayer.this.mReportIndex = 1 + AliVcMediaPlayer.this.mReportIndex;
                VcPlayerLog.i("AlivcPlayerJ", "begin to send heart report index is " + AliVcMediaPlayer.this.mReportIndex);
                if (AliVcMediaPlayer.this.mPlayer != null) {
                    if (AliVcMediaPlayer.this.mReportIndex % 6 == 0) {
                        ReportEventArgs rargs = new ReportEventArgs();
                        rargs.interval = 30;
                        rargs.videoTimeStampMs = AliVcMediaPlayer.this.getVideoTime();
                        ReportEvent.sendEvent(rargs, AliVcMediaPlayer.this.publicPraram);
                        DelayEventArgs dargs = new DelayEventArgs();
                        dargs.videoDurationFromDownloadToRenderMs = AliVcMediaPlayer.this.getPropertyLong(20011, 0) / 1000;
                        dargs.audioDurationFromDownloadToRenderMs = AliVcMediaPlayer.this.getPropertyLong(20017, 0) / 1000;
                        DelayEvent.sendEvent(dargs, AliVcMediaPlayer.this.publicPraram);
                    }
                    if (AliVcMediaPlayer.this.mDownloadBytes < 0) {
                        AliVcMediaPlayer.this.mDownloadBytes = AliVcMediaPlayer.this.getPropertyLong(20022, 0);
                        AliVcMediaPlayer.this.mLastReportTime = System.currentTimeMillis();
                        AliVcMediaPlayer.this.mDownLoadDuration = AliVcMediaPlayer.this.getPropertyLong(20021, 0);
                        return;
                    }
                    DownloadEventArgs args = new DownloadEventArgs();
                    args.downloadBytes = AliVcMediaPlayer.this.getPropertyLong(20022, 0) - AliVcMediaPlayer.this.mDownloadBytes;
                    AliVcMediaPlayer.this.mDownloadBytes = AliVcMediaPlayer.this.mDownloadBytes + args.downloadBytes;
                    args.downloadDuration = System.currentTimeMillis() - AliVcMediaPlayer.this.mLastReportTime;
                    AliVcMediaPlayer.this.mLastReportTime = System.currentTimeMillis();
                    long downloadDuration = AliVcMediaPlayer.this.getPropertyLong(20021, 0) - AliVcMediaPlayer.this.mDownLoadDuration;
                    AliVcMediaPlayer.this.mDownLoadDuration = AliVcMediaPlayer.this.mDownLoadDuration + downloadDuration;
                    args.mediaBitRate = ((double) (args.downloadBytes * 8)) / (((double) downloadDuration) / 1000.0d);
                    VcPlayerLog.d("AlivcPlayerJ", "downloadBytes is " + args.downloadBytes + " downloadDuraion is " + args.downloadDuration + " mediaBitRate is " + args.mediaBitRate + " downloadDuration is " + downloadDuration);
                    DownloadEvent.sendEvent(args, AliVcMediaPlayer.this.publicPraram);
                }
            }
        }, 0, 5000, TimeUnit.MILLISECONDS);
    }

    private void stopReportHeart() {
        if (this.executor != null && !this.executor.isShutdown()) {
            this.executor.shutdown();
            this.executor = null;
        }
    }

    private void doHandlePrepare() {
        VcPlayerLog.d("AlivcPlayerJ", "prepare");
        isCanStart = new AtomicBoolean(false);
        if (!this.mIsPublicParamIncome) {
            this.publicPraram.changeRequestId();
        }
        _prepare();
    }

    public AliVcMediaPlayer(Context context, SurfaceView view) {
        this(context, view.getHolder().getSurface());
    }

    public AliVcMediaPlayer(Context context, Surface surface) {
        this.mUrl = null;
        this.mKey = null;
        this.mCircleCount = 10;
        this.mSurface = null;
        this.mPlayer = null;
        this.mErrorCode = AliyunErrorCode.ALIVC_SUCCESS;
        this.mStatus = 3;
        this.mPreparedListener = null;
        this.mCompleteListener = null;
        this.mInfoListener = null;
        this.mErrorListener = null;
        this.mVideoSizeChangeListener = null;
        this.mSeekCompleteListener = null;
        this.mStopListener = null;
        this.mBufferingUpdateListener = null;
        this.mFrameInfoListener = null;
        this.mPcmDataListener = null;
        this.mVA = null;
        this.mMediaThread = null;
        this.mHandler = null;
        this.mDefaultDecoder = 1;
        this.mSeekPosition = 0;
        this.isEOS = false;
        this.mDownloadBytes = -1;
        this.mReportIndex = 0;
        this.cachetMaxDuration = 0;
        this.cachetMaxSize = 0;
        this.publicPraram = null;
        this.mIsPublicParamIncome = false;
        this.executor = Executors.newSingleThreadScheduledExecutor();
        setSurface(surface);
        this.mVA = new VideoAdjust(context);
        this.mUIStatusHandler = new UIStatusHandler(this);
        this.mMediaThread = new HandlerThread("media_thread");
        this.mMediaThread.setName("media_control_1");
        this.mMediaThread.start();
        this.mHandler = new MediaThreadHandler(this.mMediaThread.getLooper(), this);
        VcPlayerLog.d("AlivcPlayerJ", "ThreadManage: media thread id =  " + this.mMediaThread.getId());
    }

    private void handlMediaMesssage(Message msg) {
        VcPlayerLog.d("AlivcPlayerJ", "mHandler: handleMessage =  " + msg.what);
        switch (msg.what) {
            case 1:
                if (this.mStatus == 3 && this.mStatus != 1) {
                    this.isEOS = false;
                    StartPlayEvent.sendEvent(this.publicPraram);
                    doHandlePrepare();
                    return;
                }
                return;
            case 2:
                if (this.mStatus == 1 || this.mStatus == 4) {
                    VcPlayerLog.d("AlivcPlayerJ", "play");
                    startReportHeart();
                    _play();
                    return;
                }
                VcPlayerLog.d("AlivcPlayerJ", "play , illegalStatus result = ");
                return;
            case 3:
                if (this.mStatus == 3) {
                    VcPlayerLog.e("AlivcPlayerJ", "stop , mStatus == STOPPED return result = ");
                    return;
                }
                VcPlayerLog.d("AlivcPlayerJ", "stop.");
                _stop();
                stopReportHeart();
                return;
            case 4:
                VcPlayerLog.d("AlivcPlayerJ", "pause");
                _pause();
                stopReportHeart();
                return;
            case 5:
                VcPlayerLog.d("AlivcPlayerJ", "destroy");
                if (this.mPlayer != null) {
                    _stop();
                    this.mPlayer.release();
                }
                stopReportHeart();
                this.mHandler.getLooper().quit();
                this.mMediaThread.quit();
                this.mHandler = null;
                this.mUIStatusHandler = null;
                this.mMediaThread = null;
                if (this.mVA != null) {
                    VcPlayerLog.d("AlivcPlayerJ", "mVA destroy");
                    this.mVA.destroy();
                }
                this.mVA = null;
                this.mPlayer = null;
                return;
            case 8:
                if (this.mStatus == 2) {
                    return;
                }
                if (this.mStatus == 1 || this.mStatus == 4) {
                    VcPlayerLog.e("AlivcPlayerJ", "prepareAndPlay , mStatus == PREPARED return result = ");
                    startReportHeart();
                    _play();
                    return;
                }
                this.isEOS = false;
                while (!isCanStart.get() && WaiteForStartCount.get() < 5) {
                    try {
                        Thread.sleep(200);
                        WaiteForStartCount.incrementAndGet();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                VcPlayerLog.d("AlivcPlayerJ", "CMD_PREPARE_AND_START prepare");
                isCanStart = new AtomicBoolean(false);
                if (!this.mIsPublicParamIncome) {
                    this.publicPraram.changeRequestId();
                }
                int result = _prepare();
                VcPlayerLog.d("AlivcPlayerJ", "CMD_PREPARE_AND_START _prepare result = " + result);
                if (result == 0) {
                    VcPlayerLog.d("AlivcPlayerJ", "CMD_PREPARE_AND_START play");
                    _play();
                    StartPlayEvent.sendEvent(this.publicPraram);
                    startReportHeart();
                    return;
                }
                VcPlayerLog.d("AlivcPlayerJ", "CMD_PREPARE_AND_START prepare fail");
                return;
            default:
                return;
        }
    }

    public void setPublicParameter(PublicPraram parameter) {
        this.mIsPublicParamIncome = parameter != null;
        this.publicPraram = parameter;
    }

    private TBMPlayer getMPlayer() {
        if (this.mPlayer == null) {
            this.mPlayer = new TBMPlayer(this.mSurface, new IPlayingHandler() {
                public int onStatus(int what, int arg0, int arg1, String obj_id) {
                    AliVcMediaPlayer.this.mUIStatusHandler.sendMessage(AliVcMediaPlayer.this.mUIStatusHandler.obtainMessage(what, arg0, arg1, obj_id));
                    return 0;
                }

                public int onData(int what, int arg0, int arg1, byte[] data) {
                    AliVcMediaPlayer.this.mUIStatusHandler.sendMessage(AliVcMediaPlayer.this.mUIStatusHandler.obtainMessage(what, arg0, arg1, data));
                    return 0;
                }
            });
            this.mPlayer.setPlaySpeed(1.0f);
            if (this.publicPraram == null) {
                this.publicPraram = new PublicPraram(sContext);
            }
        }
        return this.mPlayer;
    }

    private void handlUiStatusMesssage(Message msg) {
        int what = msg.what;
        int arg0 = msg.arg1;
        int arg1 = msg.arg2;
        int size;
        if (what == 9) {
            if (this.mPcmDataListener != null) {
                size = arg0;
                byte[] msgData = (byte[]) msg.obj;
                this.mPcmDataListener.onPcmData(Arrays.copyOf(msgData, msgData.length), size);
            }
        } else if (what == 10) {
            size = arg0;
            savePic(Arrays.copyOf((byte[]) msg.obj, size), size, arg1);
        } else {
            String customData = "" + msg.obj;
            VcPlayerLog.v("AlivcPlayerJ", "receive message : what = " + what + " , arg0 = " + arg0 + " , arg1 = " + arg1);
            switch (what) {
                case 0:
                    if (arg0 != 5) {
                        return;
                    }
                    if (arg1 == 1) {
                        this.mErrorCode = AliyunErrorCode.ALIVC_ERROR_LOADING_TIMEOUT;
                        handleErrorReport();
                        if (this.mErrorListener != null) {
                            this.mErrorListener.onError(this.mErrorCode.getCode(), this.mErrorCode.getDescription(sContext));
                            return;
                        }
                        return;
                    } else if (arg1 == 2) {
                        this.mErrorCode = AliyunErrorCode.ALIVC_ERR_INVALID_INPUTFILE;
                        handleErrorReport();
                        if (this.mErrorListener != null) {
                            this.mErrorListener.onError(this.mErrorCode.getCode(), this.mErrorCode.getDescription(sContext));
                            return;
                        }
                        return;
                    } else {
                        this.mErrorCode = AliyunErrorCode.ALIVC_ERR_NO_SUPPORT_CODEC;
                        handleErrorReport();
                        if (this.mErrorListener != null) {
                            this.mErrorListener.onError(this.mErrorCode.getCode(), this.mErrorCode.getDescription(sContext));
                            return;
                        }
                        return;
                    }
                case 1:
                    if (arg0 == 20) {
                        BufferEventArgs args = new BufferEventArgs();
                        args.videoTimeStampMs = getVideoTime();
                        args.error_code = "0";
                        args.error_msg = this.mUrl;
                        BufferEvent.sendEvent(args, this.publicPraram);
                        if (this.mInfoListener != null) {
                            this.mInfoListener.onInfo(101, 0);
                        }
                    }
                    if (arg0 == 21) {
                        BufferResumeEventArgs args2 = new BufferResumeEventArgs();
                        args2.videoTimeStampMs = getVideoTime();
                        args2.costMs = System.currentTimeMillis() - BufferEvent.mLastBufferVideoTime;
                        BufferResumeEvent.sendEvent(args2, this.publicPraram);
                        BufferEvent.mLastBufferVideoTime = -1;
                        if (this.mInfoListener != null) {
                            this.mInfoListener.onInfo(102, 0);
                        }
                    }
                    if (arg0 == 22) {
                        if (this.mInfoListener != null) {
                            this.mInfoListener.onInfo(105, arg1);
                        }
                        if (this.mBufferingUpdateListener != null) {
                            this.mBufferingUpdateListener.onBufferingUpdateListener(arg1);
                        }
                    }
                    if (arg0 == 23) {
                        this.mErrorCode = AliyunErrorCode.ALIVC_ERROR_LOADING_TIMEOUT;
                        handleErrorReport();
                        if (this.mErrorListener != null) {
                            this.mErrorListener.onError(this.mErrorCode.getCode(), this.mErrorCode.getDescription(sContext));
                        }
                    }
                    if (arg0 == 8) {
                        this.mErrorCode = AliyunErrorCode.ALIVC_ERROR_LOADING_TIMEOUT;
                        handleErrorReport();
                        if (arg1 == 1) {
                            handleErrorReport();
                            if (this.mErrorListener != null) {
                                this.mErrorListener.onError(this.mErrorCode.getCode(), this.mErrorCode.getDescription(sContext));
                                return;
                            }
                            return;
                        } else if (arg1 == 2) {
                            this.mErrorCode = AliyunErrorCode.ALIVC_ERR_INVALID_INPUTFILE;
                            handleErrorReport();
                            if (this.mErrorListener != null) {
                                this.mErrorListener.onError(this.mErrorCode.getCode(), this.mErrorCode.getDescription(sContext));
                                return;
                            }
                            return;
                        } else {
                            return;
                        }
                    }
                    return;
                case 2:
                    if (arg0 == 18) {
                        if (arg1 == 1) {
                            this.mStatus = 3;
                            this.isEOS = true;
                            this.mSeekPosition = 0;
                            if (this.mCompleteListener != null) {
                                this.mCompleteListener.onCompleted();
                                return;
                            }
                            return;
                        }
                        return;
                    } else if (arg0 == 17) {
                        if (this.mSeekCompleteListener != null) {
                            this.mSeekCompleteListener.onSeekCompleted();
                            return;
                        }
                        return;
                    } else if (arg0 == 16 && this.mStopListener != null) {
                        this.mStopListener.onStopped();
                        return;
                    } else {
                        return;
                    }
                case 3:
                    if (arg0 == 3 && this.mPreparedListener != null) {
                        this.mPreparedListener.onPrepared();
                    }
                    if (arg0 == 5) {
                        this.mErrorCode = AliyunErrorCode.ALIVC_ERR_NO_SUPPORT_CODEC;
                        if (arg1 == 1) {
                            this.mErrorCode = AliyunErrorCode.ALIVC_ERROR_LOADING_TIMEOUT;
                        } else if (arg1 == 12) {
                            this.mErrorCode = AliyunErrorCode.ALIVC_ERR_DONWNLOAD_GET_KEY;
                        } else if (arg1 == 3) {
                            this.mErrorCode = AliyunErrorCode.ALIVC_ERR_NO_SUPPORT_CODEC;
                        }
                        handleErrorReport();
                        if (this.mErrorListener != null) {
                            this.mErrorListener.onError(this.mErrorCode.getCode(), this.mErrorCode.getDescription(sContext));
                        }
                    }
                    if (arg0 == 2) {
                        AliyunErrorCode errorCode = AliyunErrorCode.ALIVC_ERR_NO_SUPPORT_CODEC;
                        if (arg1 == 7) {
                            errorCode = AliyunErrorCode.ALIVC_ERROR_NO_INPUTFILE;
                        } else if (arg1 == 9) {
                            errorCode = AliyunErrorCode.ALIVC_ERR_NO_SUPPORT_CODEC;
                        } else if (arg1 == 8) {
                            errorCode = AliyunErrorCode.ALIVC_ERR_NO_MEMORY;
                        } else if (arg1 == 4 || arg1 == 2 || arg1 == 10) {
                            errorCode = AliyunErrorCode.ALIVC_ERR_INVALID_INPUTFILE;
                        }
                        this.mErrorCode = errorCode;
                        handleErrorReport();
                        if (this.mErrorListener != null) {
                            this.mErrorListener.onError(this.mErrorCode.getCode(), this.mErrorCode.getDescription(sContext));
                            return;
                        }
                        return;
                    }
                    return;
                case 5:
                    if (arg1 == 13) {
                        handleErrorReport();
                        if (this.mErrorListener != null) {
                            this.mErrorListener.onError(AliyunErrorCode.ALIVC_ERROR_DECODE_FAILED.getCode(), AliyunErrorCode.ALIVC_ERROR_DECODE_FAILED.getDescription(sContext));
                            return;
                        }
                        return;
                    }
                    return;
                case 6:
                    if (this.mVideoSizeChangeListener != null) {
                        this.mVideoSizeChangeListener.onVideoSizeChange(arg0, arg1);
                        return;
                    }
                    return;
                case 7:
                    final Map<String, String> userInfo = new HashMap();
                    int infoType = arg0;
                    userInfo.put("videoTime", "" + arg1);
                    userInfo.put("infoType", "" + infoType);
                    if (arg0 == 5 || arg0 == 3 || arg0 == 8) {
                        userInfo.put("costTime", customData);
                    } else if (arg0 == 2) {
                        userInfo.put("seekTime", arg1 + "");
                    }
                    new Thread(new Runnable() {
                        public void run() {
                            AliVcMediaPlayer.this.onInfoReport(userInfo);
                        }
                    }).start();
                    return;
                case 8:
                    if (this.mInfoListener != null) {
                        this.mInfoListener.onInfo(3, 0);
                    }
                    if (this.mFrameInfoListener != null) {
                        this.mFrameInfoListener.onFrameInfoListener();
                        return;
                    }
                    return;
                default:
                    return;
            }
        }
    }

    private void handleErrorReport() {
        if (getErrorCode() != AliyunErrorCode.ALIVC_SUCCESS.getCode()) {
            String errDescription = this.mErrorCode.getDescription(sContext);
            ErrorEventArgs args = new ErrorEventArgs();
            args.videoTimeStampMs = (long) getCurrentPosition();
            args.error_code = getErrorCode();
            args.error_msg = errDescription;
            args.cdnError = getPropertyString(20103, "");
            args.cdnVia = getPropertyString(20102, "");
            args.eagleId = getPropertyString(20101, "");
            ErrorEvent.sendEvent(args, this.publicPraram);
        }
    }

    private void savePic(byte[] yuvData, int size, int rotate) {
        int width = getVideoWidth();
        int height = getVideoHeight();
        byte[] nv21Data = new byte[yuvData.length];
        System.arraycopy(yuvData, 0, nv21Data, 0, width * height);
        for (int i = 0; i < (width * height) / 4; i++) {
            nv21Data[(width * height) + (i * 2)] = yuvData[(((width * height) * 5) / 4) + i];
            nv21Data[((width * height) + (i * 2)) + 1] = yuvData[(width * height) + i];
        }
        YuvImage yuvImage = new YuvImage(nv21Data, 17, width, height, null);
        ByteArrayOutputStream outputSteam = new ByteArrayOutputStream();
        yuvImage.compressToJpeg(new Rect(0, 0, width, height), 100, outputSteam);
        byte[] jpgData = outputSteam.toByteArray();
        SaveBitmap(BitmapFactory.decodeByteArray(jpgData, 0, jpgData.length, null));
    }

    public void SaveBitmap(Bitmap bmp) {
        File f = new File("/sdcard/" + System.currentTimeMillis() + ".jpg");
        try {
            f.createNewFile();
        } catch (IOException e) {
            VcPlayerLog.e("lfj1103", "在保存图片时出错：" + e.toString());
        }
        FileOutputStream fOut = null;
        try {
            fOut = new FileOutputStream(f);
        } catch (FileNotFoundException e2) {
            e2.printStackTrace();
        }
        bmp.compress(CompressFormat.JPEG, 100, fOut);
        try {
            fOut.flush();
        } catch (IOException e3) {
            e3.printStackTrace();
        }
        try {
            fOut.close();
        } catch (IOException e32) {
            e32.printStackTrace();
        }
    }

    private void illegalStatus() {
        this.mErrorCode = AliyunErrorCode.ALIVC_ERR_ILLEGALSTATUS;
        if (this.mErrorListener != null) {
            this.mErrorListener.onError(this.mErrorCode.getCode(), this.mErrorCode.getDescription(sContext));
        }
    }

    private void errNoSurface() {
        this.mErrorCode = AliyunErrorCode.ALIVC_ERR_NO_VIEW;
        if (this.mErrorListener != null) {
            this.mErrorListener.onError(AliyunErrorCode.ALIVC_ERR_NO_VIEW.getCode(), AliyunErrorCode.ALIVC_ERR_NO_VIEW.getDescription(sContext));
        }
    }

    private void setSurface(Surface surface) {
        this.mSurface = surface;
        getMPlayer();
    }

    private void _play() {
        if (this.mPlayer != null && this.mPlayer.start() == 0) {
            this.mStatus = 2;
        }
        this.publicPraram.setCdn_ip(getPropertyString(20100, "0.0.0.0"));
    }

    public void resume() {
        play();
    }

    public void play() {
        VcPlayerLog.d("AlivcPlayerJ", "play , sendMessage CMD_PLAY result = ");
        Message msg = this.mHandler.obtainMessage();
        msg.what = 2;
        this.mHandler.sendMessage(msg);
    }

    private void _stop() {
        this.mSeekPosition = 0;
        if (this.mPlayer != null) {
            this.mPlayer.stop();
            this.mStatus = 3;
            isCanStart = new AtomicBoolean(true);
            WaiteForStartCount = new AtomicInteger(0);
        }
    }

    private long getVideoTime() {
        long videoTime = getPropertyLong(20014, 0);
        if (videoTime < 0) {
            videoTime = 0;
        }
        return videoTime / 1000;
    }

    public void stop() {
        this.mHandler.removeMessages(4);
        this.mHandler.removeMessages(2);
        this.mHandler.removeMessages(1);
        this.mHandler.removeMessages(3);
        VcPlayerLog.d("AlivcPlayerJ", "MPlayer: send stop message.");
        Message msg = this.mHandler.obtainMessage();
        msg.what = 3;
        VcPlayerLog.d("AlivcPlayerJ", "stop , sendMessage = CMD_STOP result = " + this.mHandler.sendMessage(msg));
    }

    public void pause() {
        Message msg = this.mHandler.obtainMessage();
        msg.what = 4;
        this.mHandler.sendMessage(msg);
    }

    private void _pause() {
        if (this.mPlayer != null) {
            this.mPlayer.pause(30000);
            this.mStatus = 4;
        }
    }

    private int _prepare() {
        if (this.mPlayer == null) {
            return -1;
        }
        VcPlayerLog.d("lifujun download", "prepare url = " + this.mUrl + ", key = " + this.mKey + " ， count = " + this.mCircleCount);
        int result = this.mPlayer.prepare(this.mUrl, this.mSeekPosition, this.mDefaultDecoder, this.mKey, this.mCircleCount);
        this.mStatus = 1;
        this.mSeekPosition = 0;
        return result;
    }

    public void setDefaultDecoder(int defaultDecoder) {
        this.mDefaultDecoder = defaultDecoder;
    }

    public void prepareToPlay(String url) {
        setUrl(url);
        this.mKey = null;
        this.mCircleCount = 10;
        Message msg = this.mHandler.obtainMessage();
        msg.what = 1;
        this.mHandler.sendMessage(msg);
    }

    public void prepareAndPlay(String url) {
        if (checkAuth()) {
            setUrl(url);
            this.mKey = null;
            this.mCircleCount = 10;
            VcPlayerLog.d("AlivcPlayerJ", "prepareAndPlay , status = " + this.mStatus);
            Message msg = this.mHandler.obtainMessage();
            msg.what = 8;
            boolean result = this.mHandler.sendMessage(msg);
            return;
        }
        this.mUIStatusHandler.sendEmptyMessage(20);
        VcPlayerLog.e("AlivcPlayerJ", "prepareAndPlay , mStatus == checkAuth return result = ");
    }

    public void prepare(String url, int start_ms, int decoderType, String videoKey, int circleCount) {
        setUrl(url);
        this.mKey = videoKey;
        this.mCircleCount = circleCount;
        this.mSeekPosition = start_ms;
        this.mDefaultDecoder = decoderType;
        Message msg = this.mHandler.obtainMessage();
        msg.what = 1;
        this.mHandler.sendMessage(msg);
    }

    public int getDuration() {
        if (this.mPlayer != null) {
            return this.mPlayer.getTotalDuration();
        }
        return 0;
    }

    public int getCurrentPosition() {
        if (this.mPlayer == null) {
            return 0;
        }
        if (this.isEOS) {
            return getDuration();
        }
        return this.mPlayer.getCurrentPosition();
    }

    public int getBufferPosition() {
        if (this.mPlayer != null) {
            return this.mPlayer.getBufferPosition();
        }
        return 0;
    }

    public void seekTo(int msc) {
        if (this.mPlayer != null) {
            if (this.mStatus == 3) {
                this.mSeekPosition = msc;
            } else {
                this.mPlayer.seek_to(msc);
            }
            this.mSeekPosition = msc;
        }
    }

    public void seekToAccurate(int msc) {
        if (this.mPlayer != null) {
            if (this.mStatus == 3) {
                this.mSeekPosition = msc;
            } else {
                this.mPlayer.seek_to_accurate(msc);
            }
            this.mSeekPosition = msc;
        }
    }

    private void _reset() {
        setUrl(null);
        this.mErrorCode = AliyunErrorCode.ALIVC_SUCCESS;
        _stop();
    }

    public void reset() {
        this.mHandler.removeMessages(4);
        this.mHandler.removeMessages(2);
        this.mHandler.removeMessages(1);
        this.mHandler.removeMessages(3);
        _reset();
    }

    public int getErrorCode() {
        return this.mErrorCode.getCode();
    }

    public String getErrorDesc() {
        return this.mErrorCode.getDescription(sContext);
    }

    public String getSDKVersion() {
        return "3.2.2";
    }

    public int getVideoWidth() {
        if (this.mPlayer != null) {
            return this.mPlayer.getVideoWidth();
        }
        return 0;
    }

    public int getVideoHeight() {
        if (this.mPlayer != null) {
            return this.mPlayer.getVideoHeight();
        }
        return 0;
    }

    public boolean isPlaying() {
        if (this.mPlayer != null) {
            return this.mPlayer.isPlaying();
        }
        return false;
    }

    public void setVolume(int vol) {
        if (this.mVA != null) {
            this.mVA.SetVolumn((((float) vol) * 1.0f) / 100.0f);
        }
    }

    public int getVolume() {
        if (this.mVA != null) {
            return this.mVA.getVolume();
        }
        return 0;
    }

    public void setScreenBrightness(int brightness) {
        if (this.mVA != null) {
            this.mVA.setBrightness(brightness);
        }
    }

    public int getScreenBrightness() {
        if (this.mVA != null) {
            return this.mVA.getScreenBrightness();
        }
        return -1;
    }

    public void setPreparedListener(MediaPlayerPreparedListener listener) {
        this.mPreparedListener = listener;
    }

    public void setCompletedListener(MediaPlayerCompletedListener listener) {
        this.mCompleteListener = listener;
    }

    public void setInfoListener(MediaPlayerInfoListener listener) {
        this.mInfoListener = listener;
    }

    public void setErrorListener(MediaPlayerErrorListener listener) {
        this.mErrorListener = listener;
    }

    public void setStoppedListener(MediaPlayerStoppedListener stoppedListener) {
        this.mStopListener = stoppedListener;
    }

    public void setSeekCompleteListener(MediaPlayerSeekCompleteListener listener) {
        this.mSeekCompleteListener = listener;
    }

    public void setBufferingUpdateListener(MediaPlayerBufferingUpdateListener listener) {
        this.mBufferingUpdateListener = listener;
    }

    public void setVideoSizeChangeListener(MediaPlayerVideoSizeChangeListener listener) {
        this.mVideoSizeChangeListener = listener;
    }

    public void setPcmDataListener(MediaPlayerPcmDataListener listener) {
        this.mPcmDataListener = listener;
    }

    public void setFrameInfoListener(MediaPlayerFrameInfoListener listener) {
        this.mFrameInfoListener = listener;
    }

    private boolean checkAuth() {
        return true;
    }

    public static void init(Context context, String businessId) {
        sContext = context.getApplicationContext();
        PublicPraram.setBusinessId(businessId);
        PublicPraram.setSDkVersion("3.2.2");
        InformationReport.disableReport();
    }

    public static void init(Context context) {
        init(context, "");
    }

    public static void setUserId(String userId) {
        PublicPraram.setUserId(userId);
    }

    public void destroy() {
        setStoppedListener(null);
        setCompletedListener(null);
        setErrorListener(null);
        setInfoListener(null);
        setVideoSizeChangeListener(null);
        setPreparedListener(null);
        setBufferingUpdateListener(null);
        setSeekCompleteListener(null);
        if (this.mHandler != null) {
            Message msg = this.mHandler.obtainMessage();
            msg.what = 5;
            this.mHandler.sendMessage(msg);
        }
    }

    public double getPropertyDouble(int key, double defaultValue) {
        if (this.mPlayer != null) {
            return this.mPlayer.getPropertyDouble(key, defaultValue);
        }
        return 0.0d;
    }

    public long getPropertyLong(int key, long defaultValue) {
        if (this.mPlayer != null) {
            return this.mPlayer.getPropertyLong(key, defaultValue);
        }
        return 0;
    }

    public String getPropertyString(int key, String defaultValue) {
        if (this.mPlayer != null) {
            return this.mPlayer.getPropertyString(key, defaultValue);
        }
        return "";
    }

    private void onInfoReport(Map<String, String> userInfo) {
        int infoType = Integer.parseInt((String) userInfo.get("infoType"));
        int videoTime = Integer.parseInt((String) userInfo.get("videoTime"));
        if (videoTime < 0) {
            videoTime = (int) getVideoTime();
        }
        if (infoType > 0) {
            switch (infoType) {
                case 1:
                    PlayEventArgs args = new PlayEventArgs();
                    args.mode = DefinitionPlayMode.fixed;
                    args.videoTimeStempMs = (long) videoTime;
                    args.connectTimeMs = (long) getPropertyDouble(18001, 0.0d);
                    args.donwloadTimeMs = ((long) getPropertyDouble(18004, 0.0d)) - ((long) getPropertyDouble(18002, 0.0d));
                    args.ffprobeTimeMs = ((long) getPropertyDouble(18002, 0.0d)) - args.connectTimeMs;
                    args.videoWidth = getVideoWidth();
                    args.videoHeight = getVideoHeight();
                    if (TextUtils.isEmpty(this.mKey)) {
                        args.encrypted = "false";
                    } else {
                        args.encrypted = "true";
                    }
                    args.eagleId = getPropertyString(20101, "");
                    args.cdnVia = getPropertyString(20102, "");
                    args.openTime = getPropertyString(20104, "");
                    PlayEvent.sendEvent(args, this.publicPraram);
                    startReportHeart();
                    return;
                case 2:
                    SeekEventArgs args2 = new SeekEventArgs();
                    args2.fromTimeStampMs = (long) videoTime;
                    args2.toTimeStampMs = (long) this.mSeekPosition;
                    args2.cndVia = getPropertyString(20102, "");
                    args2.eagleId = getPropertyString(20101, "");
                    SeekEvent.sendEvent(args2, this.publicPraram);
                    return;
                case 3:
                    SeekCompleteEventArgs args3 = new SeekCompleteEventArgs();
                    args3.videoTimeStampMs = (long) videoTime;
                    args3.costMs = System.currentTimeMillis() - SeekEvent.mLastSeekVideoTime;
                    args3.cndVia = getPropertyString(20102, "");
                    args3.eagleId = getPropertyString(20101, "");
                    SeekCompleteEvent.sendEvent(args3, this.publicPraram);
                    SeekEvent.mLastSeekVideoTime = -1;
                    return;
                case 4:
                    if (!this.isEOS) {
                        PauseEvent.sendEvent((long) videoTime, this.publicPraram);
                        return;
                    }
                    return;
                case 5:
                    PauseResumeEventArgs args4 = new PauseResumeEventArgs();
                    args4.videoTimeStampMs = (long) videoTime;
                    args4.costMs = System.currentTimeMillis() - PauseEvent.mLastPauseVideoTime;
                    PauseResumeEvent.sendEvent(args4, this.publicPraram);
                    PauseEvent.mLastPauseVideoTime = -1;
                    return;
                case 6:
                    if (!this.isEOS) {
                        stopReportHeart();
                        if (!this.mIsPublicParamIncome) {
                            this.publicPraram.resetRequestId();
                        }
                        StopEvent.sendEvent((long) videoTime, this.publicPraram);
                        return;
                    }
                    return;
                case 7:
                    BufferEventArgs args5 = new BufferEventArgs();
                    args5.videoTimeStampMs = (long) videoTime;
                    args5.error_code = "";
                    args5.error_msg = "";
                    BufferEvent.sendEvent(args5, this.publicPraram);
                    return;
                case 8:
                    BufferResumeEventArgs args6 = new BufferResumeEventArgs();
                    args6.videoTimeStampMs = (long) videoTime;
                    args6.costMs = System.currentTimeMillis() - BufferEvent.mLastBufferVideoTime;
                    BufferResumeEvent.sendEvent(args6, this.publicPraram);
                    BufferEvent.mLastBufferVideoTime = -1;
                    return;
                case 9:
                    stopReportHeart();
                    EOSEvent.sendEvent((long) videoTime, this.publicPraram);
                    return;
                case 10:
                    if (videoTime == 0) {
                        BufferingToLocalStartArgs args7 = new BufferingToLocalStartArgs();
                        args7.cache_duration_ms = this.cachetMaxDuration * 1000;
                        args7.cache_size_mb = (int) this.cachetMaxSize;
                        BufferingToLocalEvent.sendEvent(args7, this.publicPraram);
                        return;
                    } else if (videoTime > 0) {
                        BufferingToLocalFinishArgs args8 = new BufferingToLocalFinishArgs();
                        args8.video_duration_ms = getDuration();
                        BufferingToLocalEvent.sendEvent(args8, this.publicPraram);
                        return;
                    } else {
                        return;
                    }
                default:
                    return;
            }
        }
    }

    public void setSurfaceChanged() {
        if (this.mPlayer != null) {
            this.mPlayer.setSurfaceChanged();
        }
    }

    public List<VideoNativeLog> getCurrNatvieLog() {
        List<VideoNativeLog> result = new ArrayList();
        if (this.mPlayer != null) {
            VideoNativeLog[] logs = this.mPlayer.getCurrNatvieLog();
            if (logs != null && logs.length > 0) {
                Collections.addAll(result, logs);
            }
        }
        return result;
    }

    public void enableNativeLog() {
        if (this.mPlayer != null) {
            this.mPlayer.enableNativeLog();
            VcPlayerLog.enableLog();
        }
    }

    public void disableNativeLog() {
        if (this.mPlayer != null) {
            this.mPlayer.disableNativeLog();
            VcPlayerLog.disableLog();
        }
    }

    public void setVideoSurface(Surface surface) {
        if (this.mPlayer != null) {
            this.mPlayer.setVideoSurface(surface);
        }
    }

    public void releaseVideoSurface() {
        if (this.mPlayer != null) {
            this.mPlayer.releaseVideoSurface();
        }
    }

    public void setTimeout(int timeout) {
        if (this.mPlayer != null) {
            this.mPlayer.setTimeout(timeout);
        }
    }

    public void setMaxBufferDuration(int duration) {
        if (this.mPlayer != null) {
            this.mPlayer.setDropBufferDuration(duration);
        }
    }

    public void setMediaType(MediaType type) {
        if (this.mPlayer != null) {
            this.mPlayer.setLivePlay(type == MediaType.Live ? 1 : 0);
        }
    }

    public void setVideoScalingMode(VideoScalingMode scalingMode) {
        if (this.mPlayer != null) {
            this.mPlayer.setVideoScalingMode(scalingMode.ordinal());
        }
    }

    public void setUrl(String url) {
        this.mUrl = url;
        this.publicPraram.setVideoUrl(url);
    }

    public Bitmap snapShot() {
        if (this.mPlayer == null) {
            return null;
        }
        if (this.mStatus == 4 || this.mStatus == 2) {
            FrameData data = this.mPlayer.snapShot();
            if (data == null || data.getYuvData() == null || data.getYuvData().length == 0 || getVideoWidth() == 0 || getVideoHeight() == 0) {
                return null;
            }
            int width = getVideoWidth();
            int height = getVideoHeight();
            int frameSize = width * height;
            byte[] yuvData = Arrays.copyOf(data.getYuvData(), (frameSize * 3) / 2);
            byte[] nv21Data = new byte[yuvData.length];
            System.arraycopy(yuvData, 0, nv21Data, 0, frameSize);
            for (int i = 0; i < frameSize / 4; i++) {
                nv21Data[(i * 2) + frameSize] = yuvData[((frameSize * 5) / 4) + i];
                nv21Data[((i * 2) + frameSize) + 1] = yuvData[frameSize + i];
            }
            YuvImage yuvImage = new YuvImage(nv21Data, 17, width, height, null);
            ByteArrayOutputStream outputSteam = new ByteArrayOutputStream();
            yuvImage.compressToJpeg(new Rect(0, 0, width, height), 100, outputSteam);
            byte[] jpgData = outputSteam.toByteArray();
            Bitmap bitmap = BitmapFactory.decodeByteArray(jpgData, 0, jpgData.length, null);
            if (bitmap != null) {
                return adjustPhotoRotation(bitmap, data.getRotate());
            }
            return bitmap;
        }
        VcPlayerLog.e("AlivcPlayerJ", "stop , mStatus == STOPPED return null ");
        return null;
    }

    public void setCirclePlay(boolean circlePlay) {
        if (this.mPlayer != null) {
            this.mPlayer.setCirclePlay(circlePlay);
        }
    }

    Bitmap adjustPhotoRotation(Bitmap source, int angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate((float) angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }

    public Map<String, String> getAllDebugInfo() {
        int propertyId;
        Map<String, String> infoMap = new HashMap();
        for (propertyId = 10001; propertyId <= 10003; propertyId++) {
            infoMap = getPropertyInfo(propertyId, infoMap);
        }
        for (propertyId = 18000; propertyId <= 18004; propertyId++) {
            infoMap = getPropertyInfo(propertyId, infoMap);
        }
        for (propertyId = 20001; propertyId <= 20022; propertyId++) {
            infoMap = getPropertyInfo(propertyId, infoMap);
        }
        return infoMap;
    }

    public void setPlaySpeed(float playSpeed) {
        if (this.mPlayer != null) {
            this.mPlayer.setPlaySpeed(playSpeed);
        }
    }

    public void setPlayingCache(boolean enable, String saveDir, int maxDuration, long maxSize) {
        if (this.mPlayer != null) {
            this.mPlayer.setPlayingCache(enable, saveDir, maxDuration, maxSize);
            if (enable) {
                this.cachetMaxDuration = maxDuration;
                this.cachetMaxSize = maxSize;
            }
        }
    }

    private Map<String, String> getPropertyInfo(int propertyId, Map<String, String> infoMap) {
        if (propertyId <= 10003 && 10001 <= propertyId) {
            infoMap.put(PropertyName.getName(propertyId), Double.toString(getPropertyDouble(propertyId, 0.0d)).concat(PropertyName.getSuffixName(propertyId)));
        }
        if (propertyId >= 18000 && 18003 >= propertyId) {
            infoMap.put(PropertyName.getName(propertyId), Double.toString(getPropertyDouble(propertyId, 0.0d)));
        }
        if (propertyId <= 20022 && 20001 <= propertyId) {
            String strCnv;
            long intgerVaule = getPropertyLong(propertyId, 0);
            String strVaule = PropertyName.getName(propertyId);
            if (propertyId == 20007 || propertyId == 20008) {
                strCnv = formatedSize(intgerVaule);
            } else if (propertyId == 20005 || propertyId == 20006) {
                strCnv = formatedDurationMilli(intgerVaule);
            } else if (propertyId != 20003) {
                strCnv = Long.toString(intgerVaule).concat(PropertyName.getSuffixName(propertyId));
            } else if (intgerVaule == 1) {
                strCnv = "AVCodec";
            } else if (intgerVaule == 2) {
                strCnv = "MediaCodec";
            } else {
                strCnv = Long.toString(intgerVaule).concat(PropertyName.getSuffixName(propertyId));
            }
            infoMap.put(strVaule, strCnv);
        }
        return infoMap;
    }

    private static String formatedDurationMilli(long duration) {
        if (duration >= 1000) {
            return String.format(Locale.US, "%.2f sec", new Object[]{Float.valueOf(((float) duration) / 1000.0f)});
        }
        return String.format(Locale.US, "%d msec", new Object[]{Long.valueOf(duration)});
    }

    private static String formatedSize(long bytes) {
        if (bytes >= 100000) {
            return String.format(Locale.US, "%.2f MB", new Object[]{Float.valueOf((((float) bytes) / 1000.0f) / 1000.0f)});
        } else if (bytes >= 100) {
            return String.format(Locale.US, "%.1f KB", new Object[]{Float.valueOf(((float) bytes) / 1000.0f)});
        } else {
            return String.format(Locale.US, "%d B", new Object[]{Long.valueOf(bytes)});
        }
    }

    public void setMuteMode(boolean on) {
        if (this.mPlayer == null) {
            return;
        }
        if (on) {
            this.mPlayer.setSteroVolume(0);
        } else {
            this.mPlayer.setSteroVolume(50);
        }
    }
}
