package com.alivc.player.model;

public class Switch {
    private String functionName;
    private String state;
    private String switchId;

    public String getFunctionName() {
        return this.functionName;
    }

    public void setFunctionName(String functionName) {
        this.functionName = functionName;
    }

    public String getState() {
        return this.state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getSwitchId() {
        return this.switchId;
    }

    public void setSwitchId(String switchId) {
        this.switchId = switchId;
    }
}
