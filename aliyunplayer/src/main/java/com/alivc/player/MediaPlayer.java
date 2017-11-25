package com.alivc.player;

import android.graphics.Bitmap;
import android.view.Surface;
import java.util.List;
import java.util.Map;

public interface MediaPlayer {
    public static final int ALIVC_ERR_EXTRA_DEFAULT = 0;
    public static final int ALIVC_ERR_EXTRA_OPEN_FAILED = 5;
    public static final int ALIVC_ERR_EXTRA_PREPARE_FAILED = 2;
    public static final int ALIVC_ERR_LOADING_TIMEOUT = 511;
    public static final int ALIVC_ERR_READD = 510;
    public static final int ALIYUN_ERR_DOWNLOAD_ALREADY_ADDED = 8010;
    public static final int ALIYUN_ERR_DOWNLOAD_GET_KEY = 8005;
    public static final int ALIYUN_ERR_DOWNLOAD_INVALID_INPUTFILE = 8003;
    public static final int ALIYUN_ERR_DOWNLOAD_INVALID_SAVE_PATH = 8008;
    public static final int ALIYUN_ERR_DOWNLOAD_INVALID_URL = 8006;
    public static final int ALIYUN_ERR_DOWNLOAD_NETWORK_TIMEOUT = 8002;
    public static final int ALIYUN_ERR_DOWNLOAD_NO_ENCRYPT_PIC = 8004;
    public static final int ALIYUN_ERR_DOWNLOAD_NO_MATCH = 8011;
    public static final int ALIYUN_ERR_DOWNLOAD_NO_MEMORY = 8007;
    public static final int ALIYUN_ERR_DOWNLOAD_NO_NETWORK = 8001;
    public static final int ALIYUN_ERR_DOWNLOAD_NO_PERMISSION = 8009;
    public static final int FFP_PROPV_DECODER_AVCODEC = 1;
    public static final int FFP_PROPV_DECODER_MEDIACODEC = 2;
    public static final int FFP_PROPV_DECODER_UNKNOWN = 0;
    public static final int FFP_PROPV_DECODER_VIDEOTOOLBOX = 3;
    public static final int FFP_PROP_DOUBLE_1st_ADECODE_TIME = 18009;
    public static final int FFP_PROP_DOUBLE_1st_AFRAME_SHOW_TIME = 18005;
    public static final int FFP_PROP_DOUBLE_1st_APKT_GET_TIME = 18007;
    public static final int FFP_PROP_DOUBLE_1st_VDECODE_TIME = 18008;
    public static final int FFP_PROP_DOUBLE_1st_VFRAME_SHOW_TIME = 18004;
    public static final int FFP_PROP_DOUBLE_1st_VPKT_GET_TIME = 18006;
    public static final int FFP_PROP_DOUBLE_CREATE_PLAY_TIME = 18000;
    public static final int FFP_PROP_DOUBLE_DECODE_TYPE = 18010;
    public static final int FFP_PROP_DOUBLE_DISCARD_VFRAME_CNT = 18013;
    public static final int FFP_PROP_DOUBLE_END = 10003;
    public static final int FFP_PROP_DOUBLE_FIND_STREAM_TIME = 18002;
    public static final int FFP_PROP_DOUBLE_FIRST_VIDEO_DECODE_TIME = 18082;
    public static final int FFP_PROP_DOUBLE_FIRST_VIDEO_RENDER_TIME = 18083;
    public static final int FFP_PROP_DOUBLE_HTTP_OPEN_DURATION = 18060;
    public static final int FFP_PROP_DOUBLE_HTTP_OPEN_RTYCNT = 18061;
    public static final int FFP_PROP_DOUBLE_HTTP_REDIRECT_CNT = 18062;
    public static final int FFP_PROP_DOUBLE_LIVE_DISCARD_CNT = 18012;
    public static final int FFP_PROP_DOUBLE_LIVE_DISCARD_DURATION = 18011;
    public static final int FFP_PROP_DOUBLE_OPEN_FORMAT_TIME = 18001;
    public static final int FFP_PROP_DOUBLE_OPEN_STREAM_TIME = 18003;
    public static final int FFP_PROP_DOUBLE_PLAYBACK_RATE = 10003;
    public static final int FFP_PROP_DOUBLE_RTMP_NEGOTIATION_DURATION = 18042;
    public static final int FFP_PROP_DOUBLE_RTMP_OPEN_DURATION = 18040;
    public static final int FFP_PROP_DOUBLE_RTMP_OPEN_RTYCNT = 18041;
    public static final int FFP_PROP_DOUBLE_TCP_CONNECT_TIME = 18080;
    public static final int FFP_PROP_DOUBLE_TCP_DNS_TIME = 18081;
    public static final int FFP_PROP_INT64_AUDIO_CACHED_BYTES = 20008;
    public static final int FFP_PROP_INT64_AUDIO_CACHED_DURATION = 20006;
    public static final int FFP_PROP_INT64_AUDIO_CACHED_PACKETS = 20010;
    public static final int FFP_PROP_INT64_AUDIO_DECODER = 20004;
    public static final int FFP_PROP_INT64_AUDIO_DOWNLOAD_PLAY_DIFF = 20017;
    public static final int FFP_PROP_INT64_AUDIO_FIRST_DROP_COUNT = 20018;
    public static final int FFP_PROP_INT64_AUDIO_LASTPTS = 20014;
    public static final int FFP_PROP_INT64_AUDIO_RENDERBUFFER_COUNT = 20015;
    public static final int FFP_PROP_INT64_BUFFERING_COUNT = 20019;
    public static final int FFP_PROP_INT64_DOWNLOAD_DURATION = 20021;
    public static final int FFP_PROP_INT64_DOWNLOAD_SIZE = 20022;
    public static final int FFP_PROP_INT64_DOWNLOAD_SPEED = 20020;
    public static final int FFP_PROP_INT64_END = 20022;
    public static final int FFP_PROP_INT64_SELECTED_AUDIO_STREAM = 20002;
    public static final int FFP_PROP_INT64_SELECTED_VIDEO_STREAM = 20001;
    public static final int FFP_PROP_INT64_VIDEO_CACHED_BYTES = 20007;
    public static final int FFP_PROP_INT64_VIDEO_CACHED_DURATION = 20005;
    public static final int FFP_PROP_INT64_VIDEO_CACHED_PACKETS = 20009;
    public static final int FFP_PROP_INT64_VIDEO_DECODER = 20003;
    public static final int FFP_PROP_INT64_VIDEO_DOWNLOAD_DIFF = 20012;
    public static final int FFP_PROP_INT64_VIDEO_DOWNLOAD_PLAY_DIFF = 20011;
    public static final int FFP_PROP_INT64_VIDEO_LASTPTS = 20013;
    public static final int FFP_PROP_INT64_VIDEO_RENDERBUFFER_COUNT = 20016;
    public static final int FFP_PROP_STRING_CDN_ERROR = 20103;
    public static final int FFP_PROP_STRING_CDN_IP = 20100;
    public static final int FFP_PROP_STRING_CDN_VIA = 20102;
    public static final int FFP_PROP_STRING_EAGLE_ID = 20101;
    public static final int FFP_PROP_STRING_OPEN_TIME_STR = 20104;
    public static final int MEDIA_AUTHORIZE_FAILED = -1004;
    public static final int MEDIA_ERROR_TIMEOUT = -1003;
    public static final int MEDIA_ERROR_UNKNOW = -1001;
    public static final int MEDIA_ERROR_UNSUPPORTED = -1002;
    public static final int MEDIA_INFO_BUFFERING_END = 102;
    public static final int MEDIA_INFO_BUFFERING_PROGRESS = 105;
    public static final int MEDIA_INFO_BUFFERING_START = 101;
    public static final int MEDIA_INFO_NETWORK_ERROR = 104;
    public static final int MEDIA_INFO_TRACKING_LAGGING = 103;
    public static final int MEDIA_INFO_UNKNOW = 100;
    public static final int MEDIA_INFO_VIDEO_RENDERING_START = 3;
    public static final int PAUSED = 4;
    public static final int PLAYING = 2;
    public static final int PREPARED = 1;
    public static final int PROP_DOUBLE_VIDEO_DECODE_FRAMES_PER_SECOND = 10001;
    public static final int PROP_DOUBLE_VIDEO_OUTPUT_FRAMES_PER_SECOND = 10002;
    public static final int STOPPED = 3;
    public static final String VERSION_ID = "3.2.0";

    public interface MediaPlayerBufferingUpdateListener {
        void onBufferingUpdateListener(int i);
    }

    public interface MediaPlayerCompletedListener {
        void onCompleted();
    }

    public interface MediaPlayerErrorListener {
        void onError(int i, String str);
    }

    public interface MediaPlayerFrameInfoListener {
        void onFrameInfoListener();
    }

    public interface MediaPlayerInfoListener {
        void onInfo(int i, int i2);
    }

    public interface MediaPlayerPcmDataListener {
        void onPcmData(byte[] bArr, int i);
    }

    public interface MediaPlayerPreparedListener {
        void onPrepared();
    }

    public interface MediaPlayerSeekCompleteListener {
        void onSeekCompleted();
    }

    public interface MediaPlayerStoppedListener {
        void onStopped();
    }

    public interface MediaPlayerVideoSizeChangeListener {
        void onVideoSizeChange(int i, int i2);
    }

    public enum MediaType {
        Live,
        Vod
    }

    public enum VideoScalingMode {
        VIDEO_SCALING_MODE_SCALE_TO_FIT(0),
        VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING(1);
        
        private int mode;

        private VideoScalingMode(int mode) {
            this.mode = mode;
        }
    }

    void destroy();

    void disableNativeLog();

    void enableNativeLog();

    Map<String, String> getAllDebugInfo();

    int getBufferPosition();

    List<VideoNativeLog> getCurrNatvieLog();

    int getCurrentPosition();

    int getDuration();

    int getErrorCode();

    String getErrorDesc();

    double getPropertyDouble(int i, double d);

    long getPropertyLong(int i, long j);

    String getPropertyString(int i, String str);

    String getSDKVersion();

    int getScreenBrightness();

    int getVideoHeight();

    int getVideoWidth();

    int getVolume();

    boolean isPlaying();

    void pause();

    void play();

    void prepare(String str, int i, int i2, String str2, int i3);

    void prepareAndPlay(String str);

    void prepareToPlay(String str);

    void releaseVideoSurface();

    void reset();

    void resume();

    void seekTo(int i);

    void seekToAccurate(int i);

    void setBufferingUpdateListener(MediaPlayerBufferingUpdateListener mediaPlayerBufferingUpdateListener);

    void setCirclePlay(boolean z);

    void setCompletedListener(MediaPlayerCompletedListener mediaPlayerCompletedListener);

    void setErrorListener(MediaPlayerErrorListener mediaPlayerErrorListener);

    void setInfoListener(MediaPlayerInfoListener mediaPlayerInfoListener);

    void setMaxBufferDuration(int i);

    void setMediaType(MediaType mediaType);

    void setMuteMode(boolean z);

    void setPcmDataListener(MediaPlayerPcmDataListener mediaPlayerPcmDataListener);

    void setPlaySpeed(float f);

    void setPlayingCache(boolean z, String str, int i, long j);

    void setPreparedListener(MediaPlayerPreparedListener mediaPlayerPreparedListener);

    void setScreenBrightness(int i);

    void setSeekCompleteListener(MediaPlayerSeekCompleteListener mediaPlayerSeekCompleteListener);

    void setStoppedListener(MediaPlayerStoppedListener mediaPlayerStoppedListener);

    void setSurfaceChanged();

    void setTimeout(int i);

    void setVideoScalingMode(VideoScalingMode videoScalingMode);

    void setVideoSizeChangeListener(MediaPlayerVideoSizeChangeListener mediaPlayerVideoSizeChangeListener);

    void setVideoSurface(Surface surface);

    void setVolume(int i);

    Bitmap snapShot();

    void stop();
}
