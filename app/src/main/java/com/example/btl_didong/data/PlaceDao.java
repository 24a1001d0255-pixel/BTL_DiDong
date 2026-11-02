package com.example.btl_didong.data;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface PlaceDao {

    // Thêm danh sách quán ăn vào Database
    @Insert
    void insertAll(List<Place> places);

    // Đếm tổng số quán đang có (Dùng để kiểm tra xem đã nạp data chưa)
    @Query("SELECT COUNT(*) FROM places")
    int count();

    // Xóa toàn bộ dữ liệu quán ăn cũ để nạp danh sách mới
    @Query("DELETE FROM places")
    void clearAllPlaces();

    // Lấy danh sách các Danh mục (category) để đưa lên Vòng quay Màn 2
    @Query("SELECT DISTINCT category FROM places WHERE mode = :mode AND priceMin <= :budgetMax AND priceMax >= :budgetMin")
    List<String> findCategories(String mode, int budgetMin, int budgetMax);

    // Lấy danh sách quán ăn cụ thể thuộc 1 Category sau khi đã quay trúng
    @Query("SELECT * FROM places WHERE mode = :mode AND (category LIKE '%' || :category || '%' OR name LIKE '%' || :category || '%') AND priceMin <= :budgetMax")
    List<Place> findPlaces(String mode, String category, int budgetMax);

    // Truy vấn gợi ý cá nhân hóa dựa trên Ngân sách tối đa và danh sách nhóm món
    @Query("SELECT * FROM places WHERE priceMax <= :maxPrice AND category IN (:preferredCategories) ORDER BY id DESC LIMIT 10")
    List<Place> getPersonalizedSuggestions(int maxPrice, List<String> preferredCategories);

    // Truy vấn danh sách quán gợi ý loại trừ món vừa ăn trong 24h
    @Query("SELECT * FROM places WHERE mode = :mode AND priceMax <= :budgetMax AND category NOT IN (:recentlyEatenCategories)")
    List<Place> findPlacesExcludingRecent(String mode, int budgetMax, List<String> recentlyEatenCategories);

    // Truy vấn "Một quán chung" (placeType == 'SINGLE' & isGroupFriendly == 1)
    @Query("SELECT * FROM places WHERE mode = 'GROUP' AND placeType = 'SINGLE' AND isGroupFriendly = 1 AND priceMax <= :budgetMax")
    List<Place> findSingleGroupPlaces(int budgetMax);

    // Truy vấn "Cụm quán gần" (placeType == 'CLUSTER')
    @Query("SELECT * FROM places WHERE mode = 'GROUP' AND placeType = 'CLUSTER' AND priceMax <= :budgetMax")
    List<Place> findClusterGroupPlaces(int budgetMax);

    // Lấy toàn bộ danh sách các quán ăn
    @Query("SELECT * FROM places")
    List<Place> getAllPlaces();
}
