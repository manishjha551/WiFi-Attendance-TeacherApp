package com.example.teacherapp;

import android.app.Application;

public class TeacherDetails extends Application {
    private String teacherID;

    public String getTeacherID() { return teacherID; }

    public void setTeacherID(String teacherID) { this.teacherID = teacherID; }

    @Override
    public void onCreate() {
        super.onCreate();
    }
}
