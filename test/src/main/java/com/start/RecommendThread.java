package com.start;

import android.os.Handler;

public class RecommendThread extends Thread {
    static long preCommitTime = 0;
    static long preLoginTime = 0;
    Handler handler;
    int recommend_id;
    String sign;
    String user;
    int user_id;

    public RecommendThread(Handler handler, int user_id, int recommend_id, String user, String sign) {
        this.handler = handler;
        this.user_id = user_id;
        this.recommend_id = recommend_id;
        this.user = user;
        this.sign = sign;
    }

    public void run() {
        super.run();
    }
}
