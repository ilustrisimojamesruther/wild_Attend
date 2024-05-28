package com.example.wildattend;

public class ClassItem {
    private String classCode;
    private String classDesc;
    private String startTime;
    private String endTime;

    public ClassItem(String classCode, String classDesc, String startTime, String endTime) {
        this.classCode = classCode;
        this.classDesc = classDesc;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    // Getters and setters
    public String getClassCode() { return classCode; }
    public String getClassDesc() { return classDesc; }
    public String getStartTime() { return startTime; }
    public String getEndTime() { return endTime; }
}
