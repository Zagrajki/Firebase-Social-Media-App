package com.example.firebaseappphoto.models;

public class ModelAchievement {

    private String name;
    private boolean isAchieved = false;

    public ModelAchievement() {
    }

    public ModelAchievement(String name, boolean isAchieved) {
        this.name = name;
        this.isAchieved = isAchieved;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isAchieved() {
        return isAchieved;
    }

    public void setAchieved(boolean achieved) {
        isAchieved = achieved;
    }
}
