package com.example.wildattend;

public class ClassItem {
    private String classCode;
    private String classDesc;
    private String startTime;
    private String endTime;
    private String classColor;
    private String classRoom;

    public ClassItem(String classCode, String classDesc, String startTime, String endTime, String classColor, String classRoom) {
        this.classCode = classCode;
        this.classDesc = classDesc;
        this.startTime = startTime;
        this.endTime = endTime;
        this.classColor = classColor;
        this.classRoom = classRoom;
    }

    // Getters and setters
    public String getClassCode() { return classCode; }
    public String getClassDesc() { return classDesc; }
    public String getStartTime() { return startTime; }
    public String getEndTime() { return endTime; }
    public String getClassColor() { return classColor; }
    public String getClassRoom() { return classRoom; }

}
