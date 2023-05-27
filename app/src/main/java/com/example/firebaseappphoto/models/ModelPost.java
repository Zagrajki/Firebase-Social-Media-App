package com.example.firebaseappphoto.models;

public class ModelPost {

    private String averageRate, countComments, countRates, photo,  pid, postsDescription, timestamp, uid;

    public ModelPost() {
    }

    public ModelPost(String averageRate, String countComments, String countRates, String photo, String pid,
                     String postsDescription, String timestamp, String uid) {
        this.averageRate = averageRate;
        this.countComments = countComments;
        this.countRates = countRates;
        this.photo = photo;
        this.pid = pid;
        this.postsDescription = postsDescription;
        this.timestamp = timestamp;
        this.uid = uid;
    }

    public String getAverageRate() {
        return averageRate;
    }

    public void setAverageRate(String averageRate) {
        this.averageRate = averageRate;
    }

    public String getCountComments() {
        return countComments;
    }

    public void setCountComments(String countComments) {
        this.countComments = countComments;
    }

    public String getCountRates() {
        return countRates;
    }

    public void setCountRates(String countRates) {
        this.countRates = countRates;
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public String getPid() {
        return pid;
    }

    public void setPid(String pid) {
        this.pid = pid;
    }

    public String getPostsDescription() {
        return postsDescription;
    }

    public void setPostsDescription(String postsDescription) {
        this.postsDescription = postsDescription;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }
}
