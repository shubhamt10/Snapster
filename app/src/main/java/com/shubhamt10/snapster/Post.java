package com.shubhamt10.snapster;

import com.google.firebase.Timestamp;

import java.util.ArrayList;

public class Post {

    private String url;
    private String uploaderUid;
    private String caption;
    private String username;
    private ArrayList<String> likes;
    private Timestamp timestamp;

    public Post() {

    }

    public Post(String url, String uploaderUid, String caption, String username) {
        this.url = url;
        this.uploaderUid = uploaderUid;
        this.caption = caption;
        this.username = username;
        this.timestamp = Timestamp.now();
        likes = new ArrayList<>();
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUploaderUid() {
        return uploaderUid;
    }

    public void setUploaderUid(String uploaderUid) {
        this.uploaderUid = uploaderUid;
    }

    public String getCaption() {
        return caption;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }

    public ArrayList<String> getLikes() {
        return likes;
    }

    public void setLikes(ArrayList<String> likes) {
        this.likes = likes;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
