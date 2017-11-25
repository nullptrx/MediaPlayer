package com.alivc.player;

import android.content.Context;
import android.os.PowerManager.WakeLock;
import android.view.Surface;
import java.io.File;
import java.io.FileFilter;
import java.util.Random;
import java.util.regex.Pattern;

public class TBMPlayer {
    public static final int E_MP_FAILED = 2;
    public static final int E_MP_INVALID_ARGS = 6;
    public static final int E_MP_INVALID_OPERATE = 4;
    public static final int E_MP_NONE = 1;
    public static final int E_MP_OK = 0;
    public static final int E_MP_OUTOFMEM = 5;
    public static final int E_MP_UNKNOW = -1;
    public static final int E_MP_UNSUPPORT = 3;
    private boolean mEnableRender;
    private boolean mPaused;
    private int mPlayerId;
    private boolean mStarted;
    private WakeLock mWakeLock;

    private native FrameData mpCaptureFrame();

    private native void mpDisableNativeLog();

    private native void mpEnableNativeLog();

    private native int mpFoo();

    private native int mpGetBufferPosition();

    private static native int mpGetCircleCount(String str, String str2, String str3);

    private native VideoNativeLog[] mpGetCurrNatvieLog();

    private native int mpGetCurrentPosition();

    private static native String mpGetEncryptRand(String str);

    private static native String mpGetKey(String str, String str2, String str3);

    private native double mpGetPropertyDouble(int i, double d);

    private native long mpGetPropertyLong(int i, long j);

    private native String mpGetPropertyString(int i, String str);

    private static native String mpGetRand();

    private native int mpGetTotalDuration();

    private native int mpGetVideoHeight();

    private native int mpGetVideoWidth();

    private native int mpInit(Class<TBMPlayer> cls, Class<NDKCallback> cls2, Class<VideoNativeLog> cls3, Class<FrameData> cls4, Surface surface);

    private native boolean mpIsPlaying();

    private native int mpPause(int i);

    private native int mpPrepare(String str, int i, int i2, String str2, int i3);

    private native void mpRelease();

    private native void mpReleaseVideoSurface();

    private native int mpResume();

    private native int mpSeekTo(int i);

    private native int mpSeekToAccurate(int i);

    private native void mpSetCirclePlay(boolean z);

    private native int mpSetDecodeThreadNum(int i);

    private static native void mpSetDownloadMode(String str);

    private native void mpSetDropBufferDuration(int i);

    private static native void mpSetEncryptFile(String str, Context context);

    private native void mpSetLivePlay(int i);

    private native void mpSetPlaySpeed(float f);

    private native void mpSetPlayingDownload(boolean z, String str, int i, long j);

    private native void mpSetStereoVolume(int i);

    private native void mpSetSurfaceChanged();

    private native void mpSetTimeout(int i);

    private native void mpSetVideoScalingMode(int i);

    private native void mpSetVideoSurface(Surface surface);

    private native int mpStart();

    private native int mpStop();

    static {
        try {
            System.loadLibrary("ffmpeg");
        } catch (Throwable th) {
            VcPlayerLog.e("AlivcPlayer", "ffmepg.so not found.");
        }
        System.loadLibrary("curl");
        System.loadLibrary("tbDownloader");
        System.loadLibrary("tbSoundTempo");
        System.loadLibrary("tbMPlayer");
    }

    private int getNumCores() {
        try {
            return new File("/sys/devices/system/cpu/").listFiles(new FileFilter() {
                public boolean accept(File pathname) {
                    if (Pattern.matches("cpu[0-9]", pathname.getName())) {
                        return true;
                    }
                    return false;
                }
            }).length;
        } catch (Exception e) {
            e.printStackTrace();
            return 1;
        }
    }

    public TBMPlayer(Surface surface, IPlayingHandler handler) {
        this.mPaused = false;
        this.mStarted = false;
        this.mEnableRender = true;
        this.mWakeLock = null;
        this.mPlayerId = -1;
        this.mPlayerId = new Random().nextInt(10000000);
        NDKCallback.setPlayingHandler(getPlayerId(), handler);
        mpInit(TBMPlayer.class, NDKCallback.class, VideoNativeLog.class, FrameData.class, surface);
    }

    public TBMPlayer() {
        this.mPaused = false;
        this.mStarted = false;
        this.mEnableRender = true;
        this.mWakeLock = null;
        this.mPlayerId = -1;
    }

    private void acquireWakeLock() {
    }

    private void releaseWakeLock() {
    }

    public int prepare(String url, int start_ms, int decoderType, String videoKey, int circleCount) {
        int ms = 0;
        mpSetDecodeThreadNum(getNumCores());
        this.mPaused = false;
        if (start_ms >= 0) {
            ms = start_ms;
        }
        return mpPrepare(url, ms, decoderType, videoKey, circleCount);
    }

    public int start() {
        this.mPaused = false;
        this.mStarted = true;
        return mpStart();
    }

    public int stop() {
        this.mPaused = false;
        this.mStarted = false;
        return mpStop();
    }

    public int pause(int buffering_ms) {
        this.mPaused = true;
        return mpPause(buffering_ms);
    }

    public boolean paused() {
        return this.mPaused;
    }

    public int resume() {
        this.mPaused = false;
        return mpResume();
    }

    public void setVideoScalingMode(int mode) {
        mpSetVideoScalingMode(mode);
    }

    public void setVideoSurface(Surface surface) {
        mpSetVideoSurface(surface);
    }

    public void releaseVideoSurface() {
        mpReleaseVideoSurface();
    }

    public int getCurrentPosition() {
        return mpGetCurrentPosition();
    }

    public int getBufferPosition() {
        return mpGetBufferPosition();
    }

    public int getTotalDuration() {
        return mpGetTotalDuration();
    }

    public boolean isPlaying() {
        return this.mStarted && !this.mPaused;
    }

    public int getVideoWidth() {
        return mpGetVideoWidth();
    }

    public void release() {
        NDKCallback.removePlayingHandler(this.mPlayerId);
        mpRelease();
    }

    public void setTimeout(int timeout) {
        mpSetTimeout(timeout);
    }

    public void setDropBufferDuration(int duration) {
        mpSetDropBufferDuration(duration);
    }

    public void setLivePlay(int livePlay) {
        mpSetLivePlay(livePlay);
    }

    public int getVideoHeight() {
        return mpGetVideoHeight();
    }

    public void onResume() {
        acquireWakeLock();
        this.mEnableRender = true;
        resume();
    }

    public void onPause() {
        releaseWakeLock();
        this.mEnableRender = false;
        pause(10000);
    }

    public double getPropertyDouble(int key, double defaultValue) {
        return mpGetPropertyDouble(key, defaultValue);
    }

    public long getPropertyLong(int key, long defaultValue) {
        return mpGetPropertyLong(key, defaultValue);
    }

    public String getPropertyString(int key, String defaultValue) {
        return mpGetPropertyString(key, defaultValue);
    }

    public int seek_to(int ms) {
        return mpSeekTo(ms);
    }

    public int seek_to_accurate(int ms) {
        return mpSeekToAccurate(ms);
    }

    public void setSurfaceChanged() {
        mpSetSurfaceChanged();
    }

    public VideoNativeLog[] getCurrNatvieLog() {
        return mpGetCurrNatvieLog();
    }

    public void enableNativeLog() {
        mpEnableNativeLog();
    }

    public void disableNativeLog() {
        mpDisableNativeLog();
    }

    public int getPlayerId() {
        return this.mPlayerId;
    }

    public void setSteroVolume(int volume) {
        mpSetStereoVolume(volume);
    }

    public static String getClientRand() {
        return mpGetRand();
    }

    public static String getEncryptRand(String clientRand) {
        return mpGetEncryptRand(clientRand);
    }

    public static String getKey(String clientRand, String serverRand, String serverPlainText) {
        return mpGetKey(clientRand, serverRand, serverPlainText);
    }

    public static int getCircleCount(String clientRand, String serverRand, String complexity) {
        return mpGetCircleCount(clientRand, serverRand, complexity);
    }

    public static void setEncryptFile(String filepath, Context context) {
        mpSetEncryptFile(filepath, context);
    }

    public static void setDownloadMode(String downloadMode) {
        mpSetDownloadMode(downloadMode);
    }

    public void setPlayingCache(boolean enable, String saveDir, int maxDuration, long maxSize) {
        mpSetPlayingDownload(enable, saveDir, maxDuration, maxSize);
    }

    public void setPlaySpeed(float playSpeed) {
        mpSetPlaySpeed(playSpeed);
    }

    public FrameData snapShot() {
        return mpCaptureFrame();
    }

    public void setCirclePlay(boolean isCirclePlay) {
        mpSetCirclePlay(isCirclePlay);
    }
}
