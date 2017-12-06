package tv.lycam.player;

public interface PlayerState {
    //正常
    int CURRENT_STATE_NORMAL = 0;
    //准备中
    int CURRENT_STATE_PREPAREING = 1;
    //播放中
    int CURRENT_STATE_PLAYING = 2;
    //开始缓冲
    int CURRENT_STATE_PLAYING_BUFFERING_START = 3;
    //暂停
    int CURRENT_STATE_PAUSE = 5;
    //自动播放结束
    int CURRENT_STATE_AUTO_COMPLETE = 6;
    //错误状态
    int CURRENT_STATE_ERROR = 7;

}