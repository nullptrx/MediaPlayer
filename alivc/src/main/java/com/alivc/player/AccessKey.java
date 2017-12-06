package com.alivc.player;

public class AccessKey {
    private String mAccessId;
    private String mAccessKey;

    public String getAccessId() {
        return this.mAccessId;
    }

    public void setAccessId(String accessId) {
        this.mAccessId = accessId;
    }

    public String getAccessKey() {
        return this.mAccessKey;
    }

    public void setAccessKey(String accessKey) {
        this.mAccessKey = accessKey;
    }

    public AccessKey(String accessId, String accessKey) {
        this.mAccessId = accessId;
        this.mAccessKey = accessKey;
    }
}
