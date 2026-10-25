package com.example.btl_didong;

public class SavedItem {
    private String id;
    private String title;
    private String subtitle;
    private String imageUrl;
    private String rating;
    private String type; // "FOOD", "RESTAURANT", "LIST"

    public SavedItem(String id, String title, String subtitle, String imageUrl, String rating, String type) {
        this.id = id;
        this.title = title;
        this.subtitle = subtitle;
        this.imageUrl = imageUrl;
        this.rating = rating;
        this.type = type;
    }

    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getSubtitle() { return subtitle; }
    public String getImageUrl() { return imageUrl; }
    public String getRating() { return rating; }
    public String getType() { return type; }
}
