package com.example.btl_didong;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.btl_didong.data.AppDatabase;
import com.example.btl_didong.data.Place;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

public class SavedFragment extends Fragment {

    private RecyclerView rvSavedFoods;
    private RecyclerView rvSavedPlaces;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_saved, container, false);

        LinearLayout tabSavedFoods = view.findViewById(R.id.tabSavedFoods);
        LinearLayout tabSavedPlaces = view.findViewById(R.id.tabSavedPlaces);

        TextView tvTabSavedFoods = view.findViewById(R.id.tvTabSavedFoods);
        TextView tvTabSavedPlaces = view.findViewById(R.id.tvTabSavedPlaces);

        View indicatorTabSavedFoods = view.findViewById(R.id.indicatorTabSavedFoods);
        View indicatorTabSavedPlaces = view.findViewById(R.id.indicatorTabSavedPlaces);

        rvSavedFoods = view.findViewById(R.id.rvSavedFoods);
        rvSavedPlaces = view.findViewById(R.id.rvSavedPlaces);

        int activeColor = getResources().getColor(R.color.forest_green);
        int mutedColor = getResources().getColor(R.color.text_muted);

        if (rvSavedFoods != null) {
            rvSavedFoods.setLayoutManager(new LinearLayoutManager(getContext()));
        }

        if (rvSavedPlaces != null) {
            rvSavedPlaces.setLayoutManager(new LinearLayoutManager(getContext()));
        }

        // Load dữ liệu các món/quán ăn đã lưu thực tế từ SharedPreferences & Room DB
        loadSavedItemsData();

        // Tab click handlers
        if (tabSavedFoods != null) {
            tabSavedFoods.setOnClickListener(v -> {
                if (rvSavedFoods != null) rvSavedFoods.setVisibility(View.VISIBLE);
                if (rvSavedPlaces != null) rvSavedPlaces.setVisibility(View.GONE);

                tvTabSavedFoods.setTextColor(activeColor);
                tvTabSavedPlaces.setTextColor(mutedColor);

                indicatorTabSavedFoods.setVisibility(View.VISIBLE);
                indicatorTabSavedPlaces.setVisibility(View.INVISIBLE);
            });
        }

        if (tabSavedPlaces != null) {
            tabSavedPlaces.setOnClickListener(v -> {
                if (rvSavedFoods != null) rvSavedFoods.setVisibility(View.GONE);
                if (rvSavedPlaces != null) rvSavedPlaces.setVisibility(View.VISIBLE);

                tvTabSavedFoods.setTextColor(mutedColor);
                tvTabSavedPlaces.setTextColor(activeColor);

                indicatorTabSavedFoods.setVisibility(View.INVISIBLE);
                indicatorTabSavedPlaces.setVisibility(View.VISIBLE);
            });
        }

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadSavedItemsData();
    }

    private boolean isItemSaved(String savedStr, String name) {
        if (savedStr == null || name == null) return false;
        String[] parts = savedStr.split(";");
        for (String part : parts) {
            if (part.trim().equalsIgnoreCase(name.trim())) {
                return true;
            }
        }
        return false;
    }

    private void loadSavedItemsData() {
        if (getContext() == null) return;
        Context ctx = getContext().getApplicationContext();

        Executors.newSingleThreadExecutor().execute(() -> {
            SharedPreferences prefs = ctx.getSharedPreferences("food_picker_prefs", Context.MODE_PRIVATE);
            String savedRestaurants = prefs.getString("saved_restaurants", "");
            String savedFoods = prefs.getString("saved_foods", "");

            AppDatabase db = AppDatabase.get(ctx);
            List<Place> allDbPlaces = db.placeDao().getAllPlaces();

            List<Restaurant> foodPlacesList = new ArrayList<>();
            List<Restaurant> placeList = new ArrayList<>();

            // 1. Tải danh sách Quán đã lưu (Saved Places) cho Tab 2 ("Quán")
            if (!savedRestaurants.isEmpty()) {
                for (Place p : allDbPlaces) {
                    if (isItemSaved(savedRestaurants, p.name)) {
                        Restaurant r = new Restaurant(
                                p.name,
                                getFoodImageUrl(p.category),
                                4.8f,
                                "1.2 km · 16 phút",
                                (p.priceMin / 1000) + "k–" + (p.priceMax / 1000) + "k",
                                p.category != null ? p.category : "Món Việt",
                                p.address != null ? p.address : "Hoàng Mai"
                        );
                        placeList.add(r);
                    }
                }
            }

            // 2. Tải các món ăn được lưu cho Tab 1 ("Món đã lưu")
            if (!savedFoods.isEmpty()) {
                String[] foods = savedFoods.split(";");
                for (String f : foods) {
                    String cleanFood = f.trim();
                    if (!cleanFood.isEmpty()) {
                        boolean alreadyAdded = false;
                        for (Restaurant r : foodPlacesList) {
                            if (r.getName().equalsIgnoreCase(cleanFood)) {
                                alreadyAdded = true;
                                break;
                            }
                        }
                        if (!alreadyAdded) {
                            foodPlacesList.add(new Restaurant(
                                    cleanFood,
                                    getFoodImageUrl(cleanFood),
                                    4.8f,
                                    "1.2 km · 16 phút",
                                    "30k–60k",
                                    cleanFood,
                                    "Hoàng Mai"
                            ));
                        }
                    }
                }
            }

            // Nạp sẵn danh sách Món đã lưu đẹp mắt ban đầu nếu chưa có món nào
            if (foodPlacesList.isEmpty()) {
                foodPlacesList.add(new Restaurant("Phở Sốt Vang Nguyễn An Ninh", "https://images.unsplash.com/photo-1582878826629-29b7ad1cdc43?w=500", 4.8f, "1.2 km · 16 phút", "35k–50k", "Phở", "Nguyễn An Ninh"));
                foodPlacesList.add(new Restaurant("Lẩu riêu cua bắp bò Đền Lừ", "https://images.unsplash.com/photo-1541832676-9b763b0239ab?w=500", 4.8f, "1.2 km · 16 phút", "90k–160k", "Lẩu", "Hồ Đền Lừ"));
                foodPlacesList.add(new Restaurant("Ốc luộc & Nem chua rán Hồ Đền Lừ", "https://images.unsplash.com/photo-1565299624946-b28f40a0ae38?w=500", 4.8f, "1.2 km · 16 phút", "30k–60k", "Ăn vặt", "Hồ Đền Lừ"));
                foodPlacesList.add(new Restaurant("Trà sữa Leyao Hoàng Mai", "https://images.unsplash.com/photo-1517701550927-30cf4ba1dba5?w=500", 4.8f, "1.2 km · 16 phút", "20k–30k", "Trà sữa", "Tân Mai"));

                StringBuilder initSavedFoods = new StringBuilder();
                for (Restaurant r : foodPlacesList) {
                    initSavedFoods.append(r.getName()).append(";");
                }
                prefs.edit().putString("saved_foods", initSavedFoods.toString()).apply();
            }

            final List<Restaurant> finalFoods = foodPlacesList;
            final List<Restaurant> finalPlaces = placeList;

            new Handler(Looper.getMainLooper()).post(() -> {
                if (!isAdded() || getContext() == null) return;

                if (rvSavedFoods != null) {
                    RestaurantAdapter foodAdapter = new RestaurantAdapter(finalFoods, restaurant -> {
                        showPlaceDetailDialog(restaurant);
                    });
                    rvSavedFoods.setAdapter(foodAdapter);
                }

                if (rvSavedPlaces != null) {
                    RestaurantAdapter placeAdapter = new RestaurantAdapter(finalPlaces, restaurant -> {
                        showPlaceDetailDialog(restaurant);
                    });
                    rvSavedPlaces.setAdapter(placeAdapter);
                }
            });
        });
    }

    private String getFoodImageUrl(String category) {
        if (category == null) return "https://images.unsplash.com/photo-1546069901-ba9599a7e63c?w=500";
        String lower = category.toLowerCase();
        if (lower.contains("phở")) {
            return "https://images.unsplash.com/photo-1582878826629-29b7ad1cdc43?w=500";
        } else if (lower.contains("cơm")) {
            return "https://images.unsplash.com/photo-1546069901-ba9599a7e63c?w=500";
        } else if (lower.contains("bánh mì")) {
            return "https://images.unsplash.com/photo-1509722747041-616f39b57569?w=500";
        } else if (lower.contains("bún")) {
            return "https://images.unsplash.com/photo-1569718212165-3a8278d5f624?w=500";
        } else if (lower.contains("nướng") || lower.contains("bbq") || lower.contains("bò")) {
            return "https://images.unsplash.com/photo-1555939594-58d7cb561ad1?w=500";
        } else if (lower.contains("lẩu") || lower.contains("buffet")) {
            return "https://images.unsplash.com/photo-1541832676-9b763b0239ab?w=500";
        } else if (lower.contains("vặt") || lower.contains("xiên") || lower.contains("chợ đêm") || lower.contains("cụm") || lower.contains("phố")) {
            return "https://images.unsplash.com/photo-1565299624946-b28f40a0ae38?w=500";
        } else if (lower.contains("uống") || lower.contains("trà") || lower.contains("sữa")) {
            return "https://images.unsplash.com/photo-1517701550927-30cf4ba1dba5?w=500";
        } else {
            return "https://images.unsplash.com/photo-1504674900247-0877df9cc836?w=500";
        }
    }

    private void showPlaceDetailDialog(Restaurant place) {
        if (getContext() == null) return;

        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_place_detail, null);
        AlertDialog dialog = new AlertDialog.Builder(getContext(), android.R.style.Theme_DeviceDefault_Light_Dialog_NoActionBar)
                .setView(dialogView)
                .create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        TextView tvPlaceName = dialogView.findViewById(R.id.tvDetailPlaceName);
        TextView tvDetailSub = dialogView.findViewById(R.id.tvDetailSub);
        TextView tvDetailRating = dialogView.findViewById(R.id.tvDetailRating);
        TextView tvDishName = dialogView.findViewById(R.id.tvDetailDishName);
        MaterialButton btnMaps = dialogView.findViewById(R.id.btnOpenGoogleMaps);
        MaterialButton btnShopee = dialogView.findViewById(R.id.btnOpenShopeeFood);
        MaterialButton btnGrab = dialogView.findViewById(R.id.btnOpenGrabFood);

        if (tvPlaceName != null) tvPlaceName.setText(place.getName());
        if (tvDetailRating != null) tvDetailRating.setText(place.getRating() + " điểm");
        if (tvDetailSub != null) tvDetailSub.setText(place.getDistance() + " • " + place.getPriceRange());
        if (tvDishName != null) tvDishName.setText(place.getTag1());

        if (btnMaps != null) {
            btnMaps.setOnClickListener(v -> {
                String searchTarget = place.getName() + " " + (place.getTag2() != null ? place.getTag2() : "Hoàng Mai") + " Hà Nội";
                Uri mapUri = Uri.parse("geo:20.9890,105.8320?q=" + Uri.encode(searchTarget));
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, mapUri);
                mapIntent.setPackage("com.google.android.apps.maps");
                try {
                    startActivity(mapIntent);
                } catch (Exception e) {
                    Intent webIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/maps/search/?api=1&query=" + Uri.encode(searchTarget)));
                    startActivity(webIntent);
                }
            });
        }

        if (btnShopee != null) {
            btnShopee.setOnClickListener(v -> {
                try {
                    Intent shopeeIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("shopeefood://food?keyword=" + Uri.encode(place.getTag1())));
                    startActivity(shopeeIntent);
                } catch (Exception e) {
                    Intent shopeeIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://shopeefood.vn/ho-chi-minh/tim-kiem?keyword=" + Uri.encode(place.getTag1())));
                    startActivity(shopeeIntent);
                }
            });
        }

        if (btnGrab != null) {
            btnGrab.setOnClickListener(v -> {
                try {
                    Intent grabIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("grab://open?screenType=FOOD&keyword=" + Uri.encode(place.getTag1())));
                    startActivity(grabIntent);
                } catch (Exception e) {
                    Intent grabIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://food.grab.com/vn/vi/restaurants?search=" + Uri.encode(place.getTag1())));
                    startActivity(grabIntent);
                }
            });
        }

        dialog.show();

        if (dialog.getWindow() != null && getActivity() != null) {
            DisplayMetrics metrics = new DisplayMetrics();
            getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);
            int width = (int) (metrics.widthPixels * 0.92);
            dialog.getWindow().setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }
}
