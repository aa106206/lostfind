package com.example.lostfind;

public class ShopItem {    private int imageResId; // 이미지 리소스 ID (예: R.drawable.gift1)
    private String title;
    private int price;

    public ShopItem(int imageResId, String title, int price) {
        this.imageResId = imageResId;
        this.title = title;
        this.price = price;
    }

    // Getter 메소드들
    public int getImageResId() {
        return imageResId;
    }

    public String getTitle() {
        return title;
    }

    public int getPrice() {
        return price;
    }
}
