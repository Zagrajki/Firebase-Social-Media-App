package com.example.firebaseappphoto.models;

public class ModelUser {

    private String name, profilePhoto, status, uid;

    public ModelUser() {

    }

    public ModelUser(String name, String profilePhoto, String status, String uid) {
        this.name = name;
        this.profilePhoto = profilePhoto;
        this.status = status;
        this.uid = uid;
    }

    public String getProfilePhoto() {
        return profilePhoto;
    }

    public void setProfilePhoto(String profilePhoto) {
        this.profilePhoto = profilePhoto;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }
}
