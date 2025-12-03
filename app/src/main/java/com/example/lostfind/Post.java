package com.example.lostfind;

import com.google.firebase.database.ServerValue;

public class Post {
    private String postId; // 게시물 고유 ID
    private String title;
    private String itemName;
    private String location;
    private String description;
    private String imageUrl;
    private String authorId;
    private String type; // "isLost" 또는 "isFound"
    private Object date;

    private double latitude;
    private double longitude;


    public Post() {}

    public Post(String postId, String title, String itemName, String location, String description, String imageUrl, String authorId, String type) {
        this.postId = postId;
        this.title = title;
        this.itemName = itemName;
        this.location = location;
        this.description = description;
        this.imageUrl = imageUrl;
        this.authorId = authorId;
        this.type = type;
        this.date = ServerValue.TIMESTAMP; // ★★★ date 필드에 서버 시간 할당 ★★★
    }

    public String getPostId() { return postId; }
    public String getTitle() { return title; }
    public String getItemName() { return itemName; }
    public String getLocation() { return location; }
    public String getDescription() { return description; }
    public String getImageUrl() { return imageUrl; }
    public String getAuthorId() { return authorId; }
    public String getType() { return type; }
    public Object getDate() { return date; }
    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }

}