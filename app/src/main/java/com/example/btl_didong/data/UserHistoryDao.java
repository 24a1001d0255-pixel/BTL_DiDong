package com.example.btl_didong.data;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface UserHistoryDao {

    @Insert
    void insert(UserHistory history);

    @Query("SELECT * FROM user_history ORDER BY timestamp DESC LIMIT 50")
    List<UserHistory> getAllHistory();

    // Lấy các nhóm món được tương tác nhiều nhất trong 7 ngày qua
    @Query("SELECT categoryName FROM user_history WHERE timestamp >= :sinceTimestamp GROUP BY categoryName ORDER BY COUNT(*) DESC")
    List<String> getTopCategoriesSince(long sinceTimestamp);

    // Lấy các nhóm món đã tương tác/ăn trong 24 giờ qua (để loại trừ tránh lặp)
    @Query("SELECT DISTINCT categoryName FROM user_history WHERE timestamp >= :sinceTimestamp")
    List<String> getRecentCategoriesSince(long sinceTimestamp);

    @Query("DELETE FROM user_history")
    void clearHistory();
}
