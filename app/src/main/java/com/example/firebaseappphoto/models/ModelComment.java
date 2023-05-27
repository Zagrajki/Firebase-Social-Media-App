package com.example.firebaseappphoto.models;

public class ModelComment {
    private String cid, comment, timestamp, uid;

    public ModelComment() {
    }

    public ModelComment(String cid, String comment, String timestamp, String uid) {
        this.cid = cid;
        this.comment = comment;
        this.timestamp = timestamp;
        this.uid = uid;
    }

    public String getCid() {
        return cid;
    }

    public void setCid(String cid) {
        this.cid = cid;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
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
