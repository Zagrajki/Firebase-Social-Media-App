package com.example.firebaseappphoto.models;

public class ModelNotification {
    private String nid, notification, pidOrUid, timestamp, type, uid;

    public ModelNotification() {
    }

    public ModelNotification(String nid, String notification, String pidOrUid, String timestamp, String type, String uid) {
        this.nid = nid;
        this.notification = notification;
        this.pidOrUid = pidOrUid;
        this.timestamp = timestamp;
        this.type = type;
        this.uid = uid;
    }

    public String getNid() {
        return nid;
    }

    public void setNid(String nid) {
        this.nid = nid;
    }

    public String getNotification() {
        return notification;
    }

    public void setNotification(String notification) {
        this.notification = notification;
    }

    public String getPidOrUid() {
        return pidOrUid;
    }

    public void setPidOrUid(String pidOrUid) {
        this.pidOrUid = pidOrUid;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }
}
