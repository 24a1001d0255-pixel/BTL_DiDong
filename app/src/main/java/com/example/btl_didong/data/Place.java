package com.example.btl_didong.data;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "places")
public class Place {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public String name;         // Tên quán hoặc Khu ẩm thực
    public String mode;         // "SOLO" (Đi 1 mình) hoặc "GROUP" (Đi nhóm)
    public String category;     // Chủng loại: "Bún bò", "Chợ đêm", "Lẩu nướng"...
    public String flavors;      // Khẩu vị, cách nhau bởi dấu phẩy: "cay,ngọt,ăn vặt"
    public int priceMin;        // Giá tối thiểu (VNĐ/người)
    public int priceMax;        // Giá tối đa (VNĐ/người)
    public double lat;          // Vĩ độ GPS
    public double lng;          // Kinh độ GPS
    public String address;      // Địa chỉ chi tiết
    public String includes;     // Danh sách món (dùng cho GROUP): "Trà sữa, Xiên bẩn..."
    public boolean walkFriendly; // Có phù hợp đi bộ không (true/false)

    // Bổ sung các trường cho chế độ "Đi nhóm" (Mode.GROUP)
    public boolean isGroupFriendly = true; // Đánh giá quán có không gian cho nhóm hay không
    public String placeType = "SINGLE";     // "SINGLE" (Một quán chung) hoặc "CLUSTER" (Cụm quán / Chợ đêm / Phố ăn vặt)
    public String categoriesList = "";      // Danh sách món ăn có tại cụm/quán: "Trà sữa, Bánh mì, Xiên bẩn, Đồ uống"
}
