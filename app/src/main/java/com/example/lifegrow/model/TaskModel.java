package com.example.lifegrow.model;

public class TaskModel {
    private String id;
    private String name;
    private String category;
    private String status;

    // Empty constructor for Firestore
    public TaskModel() {
    }

    public TaskModel(String id, String name, String category) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.status=status;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getCategory() {
        return category;
    }

    public void setId(String id) {
        this.id = id;
    }



    public String getStatus() {
        return status;
    }
}
