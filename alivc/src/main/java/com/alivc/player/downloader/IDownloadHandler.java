package com.alivc.player.downloader;

public interface IDownloadHandler {
    public static final int MSG_DOWNLOAD_STATUS = 19;
    public static final int k0DownloadError = 25;
    public static final int k0DownloadInfo = 26;
    public static final int k0DownloadProgress = 24;
    public static final int k1DownloadInfoCompleted = 12;
    public static final int k1DownloadInfoStarted = 13;
    public static final int k1DownloadInfoStoped = 14;
    public static final int k1DownloadM3u8IndexUpdate = 16;

    int onStatus(int i, int i2, int i3, String str);
}
