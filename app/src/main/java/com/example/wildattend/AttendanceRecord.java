package com.example.wildattend;

import java.util.Date;

public class AttendanceRecord {

    private String userId;
    private String message;
    private String status;
    private Date timeIn;
    private Date timeOut;
    private String className;

    // Required no-argument constructor
    public AttendanceRecord() {
    }

    public AttendanceRecord(String userId, String message, String status, Date timeIn, Date timeOut, String className) {
        this.userId = userId;
        this.message = message;
        this.status = status;
        this.timeIn = timeIn;
        this.timeOut = timeOut;
        this.className = className;
    }

    // Getters and setters
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Date getTimeIn() {
        return timeIn;
    }

    public void setTimeIn(Date timeIn) {
        this.timeIn = timeIn;
    }

    public Date getTimeOut() {
        return timeOut;
    }

    public void setTimeOut(Date timeOut) {
        this.timeOut = timeOut;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }
}
