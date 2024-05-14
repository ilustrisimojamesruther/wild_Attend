package com.example.wildattend;

public class FacultyControl {
    private Faculty faculty;

    public FacultyControl(Faculty faculty) {
        this.faculty = faculty;
    }

    public void displayFacultyDetails() {
        System.out.println("Faculty Details:");
        System.out.println("Department: " + faculty.getDepartment());
        System.out.println("Courses Taught: " + faculty.getCoursesTaught());
        System.out.println("Available: " + faculty.isAvailable());
    }
}
