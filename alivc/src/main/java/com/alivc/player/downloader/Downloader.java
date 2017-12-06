package com.alivc.player.downloader;

import android.content.Context;
import java.util.Random;

public class Downloader {
    private final int mPlayerId = new Random().nextInt(10000000);

    private native void mpClearDownloadMedias();

    private native int mpInit(Class<Downloader> cls, Class<DownloaderCallback> cls2);

    private native void mpPauseDownloadMedia();

    private native void mpResumeDownloadMedia();

    private native void mpSetDownloadPwd(String str);

    private native void mpSetDownloadSourceKey(String str, int i);

    private static native void mpSetEncryptFile(String str, Context context);

    private native void mpSetSaveM3u8Path(String str, String str2);

    private native void mpSetSaveMp4Path(String str);

    private native int mpStartDownloadMedia(String str, String str2, int i);

    private native void mpStopDownloadMedia();

    static {
        System.loadLibrary("ffmpeg");
        System.loadLibrary("curl");
        System.loadLibrary("tbDownloader");
    }

    public Downloader(IDownloadHandler handler) {
        DownloaderCallback.setPlayingHandler(this.mPlayerId, handler);
        mpInit(Downloader.class, DownloaderCallback.class);
    }

    public int getPlayerId() {
        return this.mPlayerId;
    }

    public static void setEncryptFile(String filepath, Context context) {
        mpSetEncryptFile(filepath, context);
    }

    public void setDownloadPwd(String pwd) {
        mpSetDownloadPwd(pwd);
    }

    public void setDownloadSourceKey(String key, int circleCount) {
        mpSetDownloadSourceKey(key, circleCount);
    }

    public void setSaveMp4Path(String saveFilePath) {
        mpSetSaveMp4Path(saveFilePath);
    }

    public void setSaveM3u8Path(String m3u8Name, String saveDir) {
        mpSetSaveM3u8Path(m3u8Name, saveDir);
    }

    public int startDownloadMedia(String sourceUrl, String sourceFormat, int m3u8Index) {
        return mpStartDownloadMedia(sourceUrl, sourceFormat, m3u8Index);
    }

    public void pauseDownloadMedia() {
        mpPauseDownloadMedia();
    }

    public void resumeDownloadMedia() {
        mpResumeDownloadMedia();
    }

    public void stopDownloadMedia() {
        mpStopDownloadMedia();
        DownloaderCallback.removePlayingHandler(this.mPlayerId);
    }

    public void clearDownloadMedias() {
        mpClearDownloadMedias();
    }
}
