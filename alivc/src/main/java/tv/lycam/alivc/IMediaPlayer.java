package tv.lycam.alivc;

/**
 * @author 诸葛不亮
 * @version 1.0
 * @description
 */

public interface IMediaPlayer {

    void onMediaPrepared();

    void onMediaInfo(int what, int extra);

    void onMediaError(int errorCode, String msg);

    void onMediaCompleted();

    void onMediaSeekCompleted();

    // 更新进度时间
    void onMediaFrameInfo();

    void onMediaBufferingUpdate(int percent);

    void onMediaVideoSizeChange(int width, int height);

    void setStateAndUi(int state);
}
