package com.example.wildattend;

import java.util.List;

public class Faculty {
    private String department;
    private List<String> coursesTaught;
    private boolean available;

    public Faculty(String department, List<String> coursesTaught, boolean available) {
        this.department = department;
        this.coursesTaught = coursesTaught;
        this.available = available;
    }

    public String getDepartment() {
        return department;
    }

    public List<String> getCoursesTaught() {
        return coursesTaught;
    }

    public boolean isAvailable() {
        return available;
    }
}
