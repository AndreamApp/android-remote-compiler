package com.andreamapp.compiler.bean;

/**
 * Created by Andream on 2017/2/10.
 * Website: http://andream.com.cn
 * Email: me@andream.com.cn
 */

public class SolutionStatus extends BaseBean{
    public static final int STATUS_TYPE_PROCESSING = 2;
    public static final int STATUS_TYPE_ACCEPTED = 0;
    public static final int STATUS_TYPE_ERROR = 1;

    public static final String STATUS_COLOR_PROCESSING = "fff0f0f0";
    public static final String STATUS_COLOR_ACCEPTED = "ff2ff02f";
    public static final String STATUS_COLOR_ERROR = "fff02f2f";

    private boolean processing;
    private String solutionStatus;
    private String additionalInfo;
    private int statusType;
    private String statusColor;

    private String time;
    private String memory;

    public SolutionStatus(boolean processing, String solutionStatus, String additionalInfo, int statusType, String statusColor) {
        this.processing = processing;
        this.solutionStatus = solutionStatus;
        this.additionalInfo = additionalInfo;
        this.statusType = statusType;
        this.statusColor = statusColor;
    }

    public boolean isProcessing() {
        return processing;
    }

    public String getSolutionStatus() {
        return solutionStatus;
    }

    public String getAdditionalInfo() {
        return additionalInfo;
    }

    public int getStatusType() {
        return statusType;
    }

    public String getStatusColor() {
        return statusColor;
    }

    public String getTime() {
        return time;
    }

    public String getMemory() {
        return memory;
    }
}
