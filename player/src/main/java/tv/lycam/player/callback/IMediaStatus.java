package tv.lycam.player.callback;

/**
 * @author 诸葛不亮
 * @version 1.0
 * @description
 */

public interface IMediaStatus {

    /**
     * 视频准备成功
     */
    void onMediaPrepared();

    /**
     * 视频信息
     *
     * @param what
     * @param extra
     */
    void onMediaInfo(int what, int extra);

    /**
     * 视频出错
     *
     * @param errorCode
     * @param msg
     */
    void onMediaError(int errorCode, String msg);

    /**
     * 播放完成
     */
    void onMediaCompleted();

    /**
     * seek完成
     */
    void onMediaSeekCompleted();

    /**
     * aliplayer第一帧回调（更新进度时间）
     */
    void onMediaFrameInfo();

    /**
     * 缓冲进度回调
     *
     * @param percent
     */
    void onMediaBufferingUpdate(int percent);

    /**
     * 视频大小改变回调
     *
     * @param width
     * @param height
     */
    void onMediaVideoSizeChange(int width, int height);

    /**
     * 播放器播放状态回调
     *
     * @param state
     * @see tv.lycam.player.PlayerState
     */
    void setStateAndUi(int state);
}
