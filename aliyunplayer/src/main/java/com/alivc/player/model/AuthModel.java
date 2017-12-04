package com.alivc.player.model;

public class AuthModel {
    private String logURL;
    private SwithList switchList;

    public SwithList getSwitchList() {
        return this.switchList;
    }

    public void setSwitchList(SwithList switchList) {
        this.switchList = switchList;
    }

    public String getLogURL() {
        return this.logURL;
    }

    public void setLogURL(String logURL) {
        this.logURL = logURL;
    }
}
