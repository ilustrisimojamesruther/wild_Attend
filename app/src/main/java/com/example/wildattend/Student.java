package com.example.wildattend;

import java.util.List;

public class Student {
    private String program;
    private List<String> classesTaken;
    private boolean timedIn;
    private boolean timedOut;

    public Student(String program, List<String> classesTaken, boolean timedIn, boolean timedOut) {
        this.program = program;
        this.classesTaken = classesTaken;
        this.timedIn = timedIn;
        this.timedOut = timedOut;
    }

    public String getProgram() {
        return program;
    }

    public List<String> getClassesTaken() {
        return classesTaken;
    }

    public boolean isTimedIn() {
        return timedIn;
    }

    public boolean isTimedOut() {
        return timedOut;
    }
}

