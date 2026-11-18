package com.example.btl_didong;

public class Restaurant {
    private String name;
    private String imageUrl;
    private float rating;
    private String distance;
    private String priceRange;
    private String tag1;
    private String tag2;

    public Restaurant(String name, String imageUrl, float rating, String distance, String priceRange) {
        this.name = name;
        this.imageUrl = imageUrl;
        this.rating = rating;
        this.distance = distance;
        this.priceRange = priceRange;
        this.tag1 = "Món Việt";
        this.tag2 = "Đặc sản";
    }

    public Restaurant(String name, String imageUrl, float rating, String distance, String priceRange, String tag1, String tag2) {
        this.name = name;
        this.imageUrl = imageUrl;
        this.rating = rating;
        this.distance = distance;
        this.priceRange = priceRange;
        this.tag1 = tag1;
        this.tag2 = tag2;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public float getRating() { return rating; }
    public void setRating(float rating) { this.rating = rating; }

    public String getDistance() { return distance; }
    public void setDistance(String distance) { this.distance = distance; }

    public String getPriceRange() { return priceRange; }
    public void setPriceRange(String priceRange) { this.priceRange = priceRange; }

    public String getTag1() { return tag1; }
    public void setTag1(String tag1) { this.tag1 = tag1; }

    public String getTag2() { return tag2; }
    public void setTag2(String tag2) { this.tag2 = tag2; }
}
