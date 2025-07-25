package com.example.lifegrow.ui.calendar;

import com.google.firebase.Timestamp;
import com.google.firebase.Timestamp;

public class Task {
    private String name;
    private String color;
    private Timestamp startDateTime;
    private Timestamp endDateTime;

    public Task() { }

    public Task(String name, String color, Timestamp startDateTime, Timestamp endDateTime) {
        this.name = name;
        this.color = color;
        this.startDateTime = startDateTime;
        this.endDateTime = endDateTime;
    }

    public String getName() { return name; }
    public String getColor() { return color; }
    public Timestamp getStartDateTime() { return startDateTime; }
    public Timestamp getEndDateTime() { return endDateTime; }
}
