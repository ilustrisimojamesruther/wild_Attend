package com.example.wildattend;

public class ClassItem {
    private String classCode;
    private String classDesc;
    private String startTime;
    private String endTime;
    private String classColor;
    private String classRoom;
    private boolean monday;
    private boolean tuesday;
    private boolean wednesday;
    private boolean thursday;
    private boolean friday;
    private boolean saturday;
    private boolean sunday;

    public int getDayOfWeek() {
        if (monday) return 1;
        if (tuesday) return 2;
        if (wednesday) return 3;
        if (thursday) return 4;
        if (friday) return 5;
        if (saturday) return 6;
        if (sunday) return 7;
        return Integer.MAX_VALUE; // Return a large value if no day is set
    }

    public ClassItem(String classCode, String classDesc, String startTime, String endTime, String classColor, String classRoom, boolean monday, boolean tuesday, boolean wednesday, boolean thursday, boolean friday, boolean saturday, boolean sunday) {
        this.classCode = classCode;
        this.classDesc = classDesc;
        this.startTime = startTime;
        this.endTime = endTime;
        this.classColor = classColor;
        this.classRoom = classRoom;
        this.monday = monday;
        this.tuesday = tuesday;
        this.wednesday = wednesday;
        this.thursday = thursday;
        this.friday = friday;
        this.saturday = saturday;
        this.sunday = sunday;
    }

    // Getters and setters
    public String getClassCode() { return classCode; }
    public String getClassDesc() { return classDesc; }
    public String getStartTime() { return startTime; }
    public String getEndTime() { return endTime; }
    public String getClassColor() { return classColor; }
    public String getClassRoom() { return classRoom; }

    public boolean isMonday() { return monday; }
    public boolean isTuesday() { return tuesday; }
    public boolean isWednesday() { return wednesday; }
    public boolean isThursday() { return thursday; }
    public boolean isFriday() { return friday; }
    public boolean isSaturday() { return saturday; }
    public boolean isSunday() { return sunday; }

}
