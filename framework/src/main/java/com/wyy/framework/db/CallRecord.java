package com.wyy.framework.db;

import org.litepal.crud.LitePalSupport;

/**
 * 通话记录
 */
public class CallRecord extends LitePalSupport {

    //媒体类型
    public static final int MEDIA_TYPE_AUDIO = 0;
    public static final int MEDIA_TYPE_VIDEO = 1;

    //通话状态
    public static final int CALL_STATUS_UN_ANSWER = 0;
    public static final int CALL_STATUS_DIAL = 1;
    public static final int CALL_STATUS_ANSWER = 2;

    //媒体类型
    private int mediaType;

    //通话状态
    private int callStatus;
    //用户Id
    private String userId;
    //通话时间
    private long callTime;

    public int getMediaType() {
        return mediaType;
    }

    public void setMediaType(int mediaType) {
        this.mediaType = mediaType;
    }

    public int getCallStatus() {
        return callStatus;
    }

    public void setCallStatus(int callStatus) {
        this.callStatus = callStatus;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public long getCallTime() {
        return callTime;
    }

    public void setCallTime(long callTime) {
        this.callTime = callTime;
    }
}
