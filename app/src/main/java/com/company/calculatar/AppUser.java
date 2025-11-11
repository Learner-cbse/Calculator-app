package com.company.calculatar;

public class AppUser {
    private String uid;
    private String name;
    private String avatar;

    public AppUser() {}

    public AppUser(String uid, String name, String avatar) {
        this.uid = uid;
        this.name = name;
        this.avatar = avatar;
    }

    public String getUid() {
        return uid;
    }

    public String getName() {
        return name;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }
}
