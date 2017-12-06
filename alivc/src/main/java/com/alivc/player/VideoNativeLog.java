package com.alivc.player;

public class VideoNativeLog {
    private String mContent;
    private String mKey;
    private int mLevel;
    private String mTime;
    private String mType;

    public VideoNativeLog(String key, String tag, int level, String content, String time) {
        this.mKey = key;
        this.mType = tag;
        this.mLevel = level;
        this.mContent = content;
        this.mTime = time;
    }

    public String GetKey() {
        return this.mKey;
    }

    public String GetType() {
        return this.mType;
    }

    public int GetLevel() {
        return this.mLevel;
    }

    public String GetContent() {
        return this.mContent;
    }

    public String GetTime() {
        return this.mTime;
    }

    public void SetKey(String key) {
        this.mKey = key;
    }

    public void SetType(String type) {
        this.mType = type;
    }

    public void SetLevel(int level) {
        this.mLevel = level;
    }

    public void SetContent(String content) {
        this.mContent = content;
    }

    public void SetTime(String time) {
        this.mTime = time;
    }
}
