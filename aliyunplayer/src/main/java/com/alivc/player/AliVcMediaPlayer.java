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
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceView;

import com.aliyun.aliyunplayer.BuildConfig;

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
    private String mKey;
    private HandlerThread mMediaThread;
    private MediaPlayerPcmDataListener mPcmDataListener;
    private TBMPlayer mPlayer;
    private MediaPlayerPreparedListener mPreparedListener;
    private MediaPlayerSeekCompleteListener mSeekCompleteListener;
    private int mSeekPosition;
    private int mStatus;
    private MediaPlayerStoppedListener mStopListener;
    private Surface mSurface;
    private Handler mUIStatusHandler;
    private String mUrl;
    private VideoAdjust mVA;
    private MediaPlayerVideoSizeChangeListener mVideoSizeChangeListener;

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
        FLT_VIDEO_DECODE_FPS("dec-fps", MediaPlayer.PROP_DOUBLE_VIDEO_DECODE_FRAMES_PER_SECOND),
        FLT_VIDEO_OUTPUT_FSP("out-fps", MediaPlayer.PROP_DOUBLE_VIDEO_OUTPUT_FRAMES_PER_SECOND),
        FLT_FFP_PLAYBACK_RATE("plybk-rate", 10003),
        INT64_SELECT_VIDEO_STREAM("select-v", MediaPlayer.FFP_PROP_INT64_SELECTED_VIDEO_STREAM),
        INT64_SELECT_AUDIO_STREAM("select_a", MediaPlayer.FFP_PROP_INT64_SELECTED_AUDIO_STREAM),
        INT64_VIDEO_DECODER("v-dec", MediaPlayer.FFP_PROP_INT64_VIDEO_DECODER),
        INT64_AUDIO_DECODER("a-dec", MediaPlayer.FFP_PROP_INT64_AUDIO_DECODER),
        INT64_VIDEO_CACHE_DURATION("vcache-dur", "sec", MediaPlayer.FFP_PROP_INT64_VIDEO_CACHED_DURATION),
        INT64_AUDIO_CACHE_DURATION("acache-dur", "sec", MediaPlayer.FFP_PROP_INT64_AUDIO_CACHED_DURATION),
        INT64_VIDEO_CACHE_BYTES("vcache-bytes", MediaPlayer.FFP_PROP_INT64_VIDEO_CACHED_BYTES),
        INT64_AUDIO_CACHE_BYTES("acache-bytes", MediaPlayer.FFP_PROP_INT64_AUDIO_CACHED_BYTES),
        INT64_VIDEO_CACHE_PACKETS("vcache-pkts", MediaPlayer.FFP_PROP_INT64_VIDEO_CACHED_PACKETS),
        INT64_AUDIO_CACHE_PACKETS("acache-pkts", MediaPlayer.FFP_PROP_INT64_AUDIO_CACHED_PACKETS),
        DOUBLE_CREATE_PLAY_TIME("create_player", MediaPlayer.FFP_PROP_DOUBLE_CREATE_PLAY_TIME),
        DOUBLE_OPEN_FORMAT_TIME("open-url", MediaPlayer.FFP_PROP_DOUBLE_OPEN_FORMAT_TIME),
        DOUBLE_FIND_STREAM_TIME("find-stream", MediaPlayer.FFP_PROP_DOUBLE_FIND_STREAM_TIME),
        DOUBLE_OPEN_STREAM_TIME("open-stream", MediaPlayer.FFP_PROP_DOUBLE_OPEN_STREAM_TIME);
        
        private int mIndex;
        private String mName;
        private String mSuffix;

        private PropertyName(String name, int index) {
            this.mName = name;
            this.mIndex = index;
            this.mSuffix = new String(BuildConfig.FLAVOR);
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
            return new String(BuildConfig.FLAVOR);
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
                if (AliVcMediaPlayer.this.mPlayer != null) {
                    if (AliVcMediaPlayer.this.mDownloadBytes < 0) {
                        AliVcMediaPlayer.this.mDownloadBytes = AliVcMediaPlayer.this.getPropertyLong(20022, 0);
                        AliVcMediaPlayer.this.mDownLoadDuration = AliVcMediaPlayer.this.getPropertyLong(MediaPlayer.FFP_PROP_INT64_DOWNLOAD_DURATION, 0);
                        return;
                    }
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
        VcPlayerLog.d(TAG, "prepare");
        isCanStart = new AtomicBoolean(false);
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
        this.cachetMaxDuration = 0;
        this.cachetMaxSize = 0;
        this.executor = Executors.newSingleThreadScheduledExecutor();
        setSurface(surface);
        this.mVA = new VideoAdjust(context);
        this.mUIStatusHandler = new UIStatusHandler(this);
        this.mMediaThread = new HandlerThread("media_thread");
        this.mMediaThread.setName("media_control_1");
        this.mMediaThread.start();
        this.mHandler = new MediaThreadHandler(this.mMediaThread.getLooper(), this);
        VcPlayerLog.d(TAG, "ThreadManage: media thread id =  " + this.mMediaThread.getId());
    }

    private void handlMediaMesssage(Message msg) {
        VcPlayerLog.d(TAG, "mHandler: handleMessage =  " + msg.what);
        switch (msg.what) {
            case 1:
                if (this.mStatus == 3 && this.mStatus != 1) {
                    this.isEOS = false;
                    doHandlePrepare();
                    return;
                }
                return;
            case 2:
                if (this.mStatus == 1 || this.mStatus == 4) {
                    VcPlayerLog.d(TAG, "play");
                    startReportHeart();
                    _play();
                    return;
                }
                VcPlayerLog.d(TAG, "play , illegalStatus result = ");
                return;
            case 3:
                if (this.mStatus == 3) {
                    VcPlayerLog.e(TAG, "stop , mStatus == STOPPED return result = ");
                    return;
                }
                VcPlayerLog.d(TAG, "stop.");
                _stop();
                stopReportHeart();
                return;
            case 4:
                VcPlayerLog.d(TAG, "pause");
                _pause();
                stopReportHeart();
                return;
            case 5:
                VcPlayerLog.d(TAG, "destroy");
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
                    VcPlayerLog.d(TAG, "mVA destroy");
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
                    VcPlayerLog.e(TAG, "prepareAndPlay , mStatus == PREPARED return result = ");
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
                VcPlayerLog.d(TAG, "CMD_PREPARE_AND_START prepare");
                isCanStart = new AtomicBoolean(false);
                int result = _prepare();
                VcPlayerLog.d(TAG, "CMD_PREPARE_AND_START _prepare result = " + result);
                if (result == 0) {
                    VcPlayerLog.d(TAG, "CMD_PREPARE_AND_START play");
                    _play();
                    startReportHeart();
                    return;
                }
                VcPlayerLog.d(TAG, "CMD_PREPARE_AND_START prepare fail");
                return;
            default:
                return;
        }
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
            String customData = BuildConfig.FLAVOR + msg.obj;
            VcPlayerLog.v(TAG, "receive message : what = " + what + " , arg0 = " + arg0 + " , arg1 = " + arg1);
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
                        if (this.mInfoListener != null) {
                            this.mInfoListener.onInfo(MediaPlayer.MEDIA_INFO_BUFFERING_START, 0);
                        }
                    }
                    if (arg0 == 21) {
                        if (this.mInfoListener != null) {
                            this.mInfoListener.onInfo(MediaPlayer.MEDIA_INFO_BUFFERING_END, 0);
                        }
                    }
                    if (arg0 == 22) {
                        if (this.mInfoListener != null) {
                            this.mInfoListener.onInfo(MediaPlayer.MEDIA_INFO_BUFFERING_PROGRESS, arg1);
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
                    userInfo.put("videoTime", BuildConfig.FLAVOR + arg1);
                    userInfo.put("infoType", BuildConfig.FLAVOR + infoType);
                    if (arg0 == 5 || arg0 == 3 || arg0 == 8) {
                        userInfo.put("costTime", customData);
                    } else if (arg0 == 2) {
                        userInfo.put("seekTime", arg1 + BuildConfig.FLAVOR);
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
    }

    public void resume() {
        play();
    }

    public void play() {
        VcPlayerLog.d(TAG, "play , sendMessage CMD_PLAY result = ");
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
        long videoTime = getPropertyLong(MediaPlayer.FFP_PROP_INT64_AUDIO_LASTPTS, 0);
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
        VcPlayerLog.d(TAG, "MPlayer: send stop message.");
        Message msg = this.mHandler.obtainMessage();
        msg.what = 3;
        VcPlayerLog.d(TAG, "stop , sendMessage = CMD_STOP result = " + this.mHandler.sendMessage(msg));
    }

    public void pause() {
        Message msg = this.mHandler.obtainMessage();
        msg.what = 4;
        this.mHandler.sendMessage(msg);
    }

    private void _pause() {
        if (this.mPlayer != null) {
            this.mPlayer.pause(PAUSE_BUFFERING_TIME);
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
            VcPlayerLog.d(TAG, "prepareAndPlay , status = " + this.mStatus);
            Message msg = this.mHandler.obtainMessage();
            msg.what = 8;
            boolean result = this.mHandler.sendMessage(msg);
            return;
        }
        this.mUIStatusHandler.sendEmptyMessage(20);
        VcPlayerLog.e(TAG, "prepareAndPlay , mStatus == checkAuth return result = ");
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
        return MediaPlayer.VERSION_ID;
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

    public static void init(Context context) {
        sContext = context.getApplicationContext();
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
        return BuildConfig.FLAVOR;
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
                    startReportHeart();
                    return;
                case 2:
                    return;
                case 3:
                    return;
                case 4:
                    if (!this.isEOS) {
                        return;
                    }
                    return;
                case 5:
                    return;
                case 6:
                    if (!this.isEOS) {
                        stopReportHeart();
                        return;
                    }
                    return;
                case 7:
                    return;
                case 8:
                    return;
                case 9:
                    stopReportHeart();
                    return;
                case 10:
                    if (videoTime == 0) {
                        return;
                    } else if (videoTime > 0) {
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
        VcPlayerLog.e(TAG, "stop , mStatus == STOPPED return null ");
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
        for (propertyId = MediaPlayer.PROP_DOUBLE_VIDEO_DECODE_FRAMES_PER_SECOND; propertyId <= 10003; propertyId++) {
            infoMap = getPropertyInfo(propertyId, infoMap);
        }
        for (propertyId = MediaPlayer.FFP_PROP_DOUBLE_CREATE_PLAY_TIME; propertyId <= MediaPlayer.FFP_PROP_DOUBLE_1st_VFRAME_SHOW_TIME; propertyId++) {
            infoMap = getPropertyInfo(propertyId, infoMap);
        }
        for (propertyId = MediaPlayer.FFP_PROP_INT64_SELECTED_VIDEO_STREAM; propertyId <= 20022; propertyId++) {
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
        if (propertyId <= 10003 && MediaPlayer.PROP_DOUBLE_VIDEO_DECODE_FRAMES_PER_SECOND <= propertyId) {
            infoMap.put(PropertyName.getName(propertyId), Double.toString(getPropertyDouble(propertyId, 0.0d)).concat(PropertyName.getSuffixName(propertyId)));
        }
        if (propertyId >= MediaPlayer.FFP_PROP_DOUBLE_CREATE_PLAY_TIME && MediaPlayer.FFP_PROP_DOUBLE_OPEN_STREAM_TIME >= propertyId) {
            infoMap.put(PropertyName.getName(propertyId), Double.toString(getPropertyDouble(propertyId, 0.0d)));
        }
        if (propertyId <= 20022 && MediaPlayer.FFP_PROP_INT64_SELECTED_VIDEO_STREAM <= propertyId) {
            String strCnv;
            long intgerVaule = getPropertyLong(propertyId, 0);
            String strVaule = PropertyName.getName(propertyId);
            if (propertyId == MediaPlayer.FFP_PROP_INT64_VIDEO_CACHED_BYTES || propertyId == MediaPlayer.FFP_PROP_INT64_AUDIO_CACHED_BYTES) {
                strCnv = formatedSize(intgerVaule);
            } else if (propertyId == MediaPlayer.FFP_PROP_INT64_VIDEO_CACHED_DURATION || propertyId == MediaPlayer.FFP_PROP_INT64_AUDIO_CACHED_DURATION) {
                strCnv = formatedDurationMilli(intgerVaule);
            } else if (propertyId != MediaPlayer.FFP_PROP_INT64_VIDEO_DECODER) {
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
