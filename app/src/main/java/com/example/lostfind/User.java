package com.example.lostfind;
public class User {
    public String uid;
    public String name;
    public String email;

    public User() {}  // Firebase용 빈 생성자

    public User(String uid, String name, String email) {
        this.uid = uid;
        this.name = name;
        this.email = email;
    }
}