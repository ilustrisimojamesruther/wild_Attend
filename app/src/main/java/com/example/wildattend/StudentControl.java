package com.example.wildattend;

public class StudentControl {
    private Student student;

    public StudentControl(Student student) {
        this.student = student;
    }

    public void displayStudentDetails() {
        System.out.println("Student Details:");
        System.out.println("Program: " + student.getProgram());
        System.out.println("Classes Taken: " + student.getClassesTaken());
        System.out.println("Timed In: " + student.isTimedIn());
        System.out.println("Timed Out: " + student.isTimedOut());
    }
}
