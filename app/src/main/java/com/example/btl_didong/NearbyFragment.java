package com.example.btl_didong;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.btl_didong.data.AppDatabase;
import com.example.btl_didong.data.Place;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;

public class NearbyFragment extends Fragment {

    private RecyclerView rvNearby;
    private MaterialButton btnRandomPickNearby;
    private MaterialButton btnReloadNearby;

    private double userLat = 20.9890; // Định Công, Hoàng Mai, Hà Nội
    private double userLng = 105.8320;

    private List<Place> currentPlacesList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_nearby, container, false);

        rvNearby = view.findViewById(R.id.rvNearbyRestaurants);
        btnRandomPickNearby = view.findViewById(R.id.btnRandomPickNearby);
        btnReloadNearby = view.findViewById(R.id.btnReloadNearby);

        // Đồng bộ tọa độ theo vị trí đã chọn ở trang Home
        syncLocationFromHomePreferences();

        if (rvNearby != null) {
            rvNearby.setLayoutManager(new LinearLayoutManager(getContext()));
        }

        // Xin quyền GPS
        checkAndRequestLocationPermission();

        // Nút Reload làm mới danh sách theo vị trí gõ ở trang Home
        if (btnReloadNearby != null) {
            btnReloadNearby.setOnClickListener(v -> {
                syncLocationFromHomePreferences();
                loadPlacesData();
                if (getContext() != null) {
                    SharedPreferences prefs = getContext().getSharedPreferences("food_picker_prefs", Context.MODE_PRIVATE);
                    String currentAddr = prefs.getString("pref_user_address", "Vị trí hiện tại");
                    Toast.makeText(getContext(), "🔄 Đã làm mới danh sách quán theo: " + currentAddr, Toast.LENGTH_SHORT).show();
                }
            });
        }

        // Bấm "Chọn món ngẫu nhiên"
        if (btnRandomPickNearby != null) {
            btnRandomPickNearby.setOnClickListener(v -> {
                if (!currentPlacesList.isEmpty()) {
                    int randomIndex = (int) (Math.random() * currentPlacesList.size());
                    Place randomPlace = currentPlacesList.get(randomIndex);
                    Restaurant r = new Restaurant(
                            randomPlace.name,
                            getFoodImageUrl(randomPlace.category),
                            4.8f,
                            String.format(Locale.US, "%.1f km", calculateDistanceKm(userLat, userLng, randomPlace.lat, randomPlace.lng)),
                            (randomPlace.priceMin / 1000) + "k–" + (randomPlace.priceMax / 1000) + "k",
                            randomPlace.category != null ? randomPlace.category : "Món Việt",
                            randomPlace.address != null ? randomPlace.address : "Hoàng Mai"
                    );
                    showPlaceDetailDialog(r);
                }
            });
        }

        loadPlacesData();

        return view;
    }

    private void syncLocationFromHomePreferences() {
        if (getContext() == null) return;
        SharedPreferences prefs = getContext().getSharedPreferences("food_picker_prefs", Context.MODE_PRIVATE);
        float savedLat = prefs.getFloat("pref_user_lat", 20.9890f);
        float savedLng = prefs.getFloat("pref_user_lng", 105.8320f);
        if (savedLat != 0 && savedLng != 0) {
            userLat = savedLat;
            userLng = savedLng;
        }
    }

    private void checkAndRequestLocationPermission() {
        if (getContext() != null && ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1001);
        }
    }

    private void loadPlacesData() {
        Context ctx = getContext() != null ? getContext().getApplicationContext() : null;
        if (ctx == null) return;

        syncLocationFromHomePreferences();

        Executors.newSingleThreadExecutor().execute(() -> {
            AppDatabase db = AppDatabase.get(ctx);
            List<Place> allPlaces = db.placeDao().findPlacesExcludingRecent("SOLO", 300000, new ArrayList<>());

            // Sắp xếp tăng dần theo khoảng cách GPS thực tế từ vị trí ở trang Home
            Collections.sort(allPlaces, new Comparator<Place>() {
                @Override
                public int compare(Place o1, Place o2) {
                    double d1 = calculateDistanceKm(userLat, userLng, o1.lat, o1.lng);
                    double d2 = calculateDistanceKm(userLat, userLng, o2.lat, o2.lng);
                    return Double.compare(d1, d2);
                }
            });

            currentPlacesList = allPlaces;
            final List<Place> finalPlaces = allPlaces;

            new Handler(Looper.getMainLooper()).post(() -> {
                if (!isAdded() || getContext() == null) return;
                displayPlacesOnList(finalPlaces);
            });
        });
    }

    private void displayPlacesOnList(List<Place> places) {
        List<Restaurant> list = new ArrayList<>();

        for (Place p : places) {
            double dist = calculateDistanceKm(userLat, userLng, p.lat, p.lng);
            int minutes = (int) (dist * 8 + 5);

            Restaurant r = new Restaurant(
                    p.name,
                    getFoodImageUrl(p.category),
                    4.8f,
                    String.format(Locale.US, "%.1f km · %d phút", dist, minutes),
                    (p.priceMin / 1000) + "k–" + (p.priceMax / 1000) + "k",
                    p.category != null ? p.category : "Món Việt",
                    "Hoàng Mai"
            );
            list.add(r);
        }

        if (rvNearby != null) {
            RestaurantAdapter adapter = new RestaurantAdapter(list, restaurant -> {
                showPlaceDetailDialog(restaurant);
            });
            rvNearby.setAdapter(adapter);
        }
    }

    private double calculateDistanceKm(double lat1, double lon1, double lat2, double lon2) {
        if (lat1 == 0 || lon1 == 0 || lat2 == 0 || lon2 == 0) return 1.2;
        double R = 6371; // Bán kính Trái Đất (km)
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
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

    @Override
    public void onResume() {
        super.onResume();
        syncLocationFromHomePreferences();
        loadPlacesData();
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
