package com.shubhamt10.snapster;

public class ResultUser {

    private String name;
    private String username;
    private String url;
    private String uid;

    public ResultUser(String name, String username, String url, String uid) {
        this.name = name;
        this.username = username;
        this.url = url;
        this.uid = uid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }
}
