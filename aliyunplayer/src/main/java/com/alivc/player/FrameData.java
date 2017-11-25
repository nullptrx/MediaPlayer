package com.alivc.player;

public class FrameData {
    private int mRotate;
    private byte[] mYuvData;

    public FrameData(byte[] yuvdata, int rotate) {
        this.mYuvData = yuvdata;
        this.mRotate = rotate;
    }

    public byte[] getYuvData() {
        return this.mYuvData;
    }

    public void setYuvData(byte[] mYuvData) {
        this.mYuvData = mYuvData;
    }

    public int getRotate() {
        return this.mRotate;
    }

    public void setRotate(int mRotate) {
        this.mRotate = mRotate;
    }
}
