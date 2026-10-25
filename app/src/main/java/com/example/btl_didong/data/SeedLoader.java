package com.example.btl_didong.data;

import android.content.Context;
import com.google.gson.Gson;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executors;

public class SeedLoader {

    public static void loadSeedDataIfNeeded(Context context) {
        // Room KHÔNG CHẠY TRÊN MAIN THREAD -> Dùng Executors chạy ngầm
        Executors.newSingleThreadExecutor().execute(() -> {
            AppDatabase db = AppDatabase.get(context);
            PlaceDao dao = db.placeDao();

            // Nếu DB chưa có dòng nào -> Đọc JSON và Insert
            if (dao.count() == 0) {
                try {
                    InputStream is = context.getAssets().open("places_seed.json");
                    InputStreamReader reader = new InputStreamReader(is);
                    
                    // Dùng Gson biến chuỗi JSON thành Danh sách Place
                    Place[] placesArray = new Gson().fromJson(reader, Place[].class);
                    List<Place> placesList = Arrays.asList(placesArray);

                    // Chèn vào Database
                    dao.insertAll(placesList);
                    reader.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
