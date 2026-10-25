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

    // Lấy danh sách các Danh mục (category) để đưa lên Vòng quay Màn 2
    @Query("SELECT DISTINCT category FROM places WHERE mode = :mode AND priceMin <= :budgetMax AND priceMax >= :budgetMin")
    List<String> findCategories(String mode, int budgetMin, int budgetMax);

    // Lấy danh sách quán ăn cụ thể thuộc 1 Category sau khi đã quay trúng
    @Query("SELECT * FROM places WHERE mode = :mode AND category = :category AND priceMin <= :budgetMax AND priceMax >= :budgetMin")
    List<Place> findPlaces(String mode, String category, int budgetMin, int budgetMax);

    // Truy vấn "Một quán chung" (placeType == 'SINGLE' & isGroupFriendly == 1)
    @Query("SELECT * FROM places WHERE mode = 'GROUP' AND placeType = 'SINGLE' AND isGroupFriendly = 1 AND priceMax <= :budgetMax")
    List<Place> findSingleGroupPlaces(int budgetMax);

    // Truy vấn "Cụm quán gần" (placeType == 'CLUSTER')
    @Query("SELECT * FROM places WHERE mode = 'GROUP' AND placeType = 'CLUSTER' AND priceMax <= :budgetMax")
    List<Place> findClusterGroupPlaces(int budgetMax);
}
