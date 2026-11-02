package com.example.btl_didong.data;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "user_history")
public class UserHistory {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public String categoryName; // Tên nhóm món (ví dụ: "Ăn vặt", "Bún", "Phở")
    public int restaurantId;    // ID quán ăn
    public String actionType;   // "SPUN" (Quay trúng), "VIEWED" (Xem chi tiết), "SAVED" (Lưu)
    public long timestamp;      // Thời gian thực hiện (System.currentTimeMillis())

    public UserHistory() {}

    public UserHistory(String categoryName, int restaurantId, String actionType, long timestamp) {
        this.categoryName = categoryName;
        this.restaurantId = restaurantId;
        this.actionType = actionType;
        this.timestamp = timestamp;
    }
}
