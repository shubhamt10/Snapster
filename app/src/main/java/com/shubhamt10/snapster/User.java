package com.shubhamt10.snapster;

import java.util.ArrayList;

public class User {

    private String uid;
    private String name;
    private String username;
    private String email;
    private String gender;
    private String displayPictureUrl;
    private String bio;
    private ArrayList<String> followers;
    private ArrayList<String> following;
    private ArrayList<String> postUrls;

    public User(){

    }

    public User(String uid, String name, String username, String email, String gender, String displayPictureUrl, String bio) {
        this.uid = uid;
        this.name = name;
        this.username = username;
        this.email = email;
        this.gender = gender;
        this.displayPictureUrl = displayPictureUrl;
        this.bio = bio;
        this.followers = new ArrayList<>();
        this.following = new ArrayList<>();
        this.postUrls = new ArrayList<>();
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getDisplayPictureUrl() {
        return displayPictureUrl;
    }

    public void setDisplayPictureUrl(String displayPictureUrl) {
        this.displayPictureUrl = displayPictureUrl;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public ArrayList<String> getFollowers() {
        return followers;
    }

    public void setFollowers(ArrayList<String> followers) {
        this.followers = followers;
    }

    public ArrayList<String> getFollowing() {
        return following;
    }

    public void setFollowing(ArrayList<String> following) {
        this.following = following;
    }

    public ArrayList<String> getPostUrls() {
        return postUrls;
    }

    public void setPostUrls(ArrayList<String> postUrls) {
        this.postUrls = postUrls;
    }
}
