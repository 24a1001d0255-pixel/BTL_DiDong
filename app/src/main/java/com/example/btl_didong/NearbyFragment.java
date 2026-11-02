package com.example.btl_didong;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.btl_didong.data.AppDatabase;
import com.example.btl_didong.data.Place;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.slider.Slider;

import org.osmdroid.api.IMapController;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;

public class NearbyFragment extends Fragment {

    private MapView mapView;
    private RecyclerView rvNearby;
    private Slider sliderRadiusNearby;
    private TextView tvRadiusValNearby;
    private TextView tvPlaceCountNearby;
    private MaterialButton btnRandomPickNearby;

    private double userLat = 20.9850; // Hoàng Mai, Hà Nội
    private double userLng = 105.8450;
    private float currentRadiusNearby = 2.0f;

    private List<Place> currentPlacesList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_nearby, container, false);

        mapView = view.findViewById(R.id.mapView);
        rvNearby = view.findViewById(R.id.rvNearbyRestaurants);
        sliderRadiusNearby = view.findViewById(R.id.sliderRadiusNearby);
        tvRadiusValNearby = view.findViewById(R.id.tvRadiusValNearby);
        tvPlaceCountNearby = view.findViewById(R.id.tvPlaceCountNearby);
        btnRandomPickNearby = view.findViewById(R.id.btnRandomPickNearby);

        if (mapView != null) {
            mapView.setTileSource(TileSourceFactory.MAPNIK);
            mapView.setMultiTouchControls(true);

            IMapController mapController = mapView.getController();
            mapController.setZoom(14.5);
            mapController.setCenter(new GeoPoint(userLat, userLng));
        }

        if (rvNearby != null) {
            rvNearby.setLayoutManager(new LinearLayoutManager(getContext()));
        }

        // 1 & 2. Xin quyền GPS
        checkAndRequestLocationPermission();

        // 3. Lấy tọa độ GPS hiện tại từ thiết bị
        fetchGPSCoordinates();

        // 6. Lắng nghe gạt thanh bán kính để lọc lại danh sách động
        if (sliderRadiusNearby != null) {
            sliderRadiusNearby.addOnChangeListener((slider, value, fromUser) -> {
                currentRadiusNearby = value;
                if (tvRadiusValNearby != null) {
                    tvRadiusValNearby.setText(String.format(Locale.US, "%.1f km", currentRadiusNearby));
                }
                loadPlacesData();
            });
        }

        // 7. Bấm "Chọn món ngẫu nhiên"
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

    private void fetchGPSCoordinates() {
        if (getContext() == null) return;
        try {
            if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                LocationManager lm = (LocationManager) getContext().getSystemService(Context.LOCATION_SERVICE);
                if (lm != null) {
                    Location loc = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    if (loc == null) {
                        loc = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                    }
                    if (loc != null) {
                        userLat = loc.getLatitude();
                        userLng = loc.getLongitude();
                        if (mapView != null) {
                            mapView.getController().setCenter(new GeoPoint(userLat, userLng));
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
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

        Executors.newSingleThreadExecutor().execute(() -> {
            AppDatabase db = AppDatabase.get(ctx);
            List<Place> allPlaces = db.placeDao().findPlacesExcludingRecent("SOLO", 300000, new ArrayList<>());

            // 6. Tính khoảng cách, lọc theo bán kính (currentRadiusNearby) và sắp xếp tăng dần theo khoảng cách GPS
            List<Place> filteredPlaces = new ArrayList<>();
            for (Place p : allPlaces) {
                double dist = calculateDistanceKm(userLat, userLng, p.lat, p.lng);
                if (dist <= currentRadiusNearby) {
                    filteredPlaces.add(p);
                }
            }

            if (filteredPlaces.isEmpty()) {
                filteredPlaces = allPlaces;
            }

            // Sắp xếp theo khoảng cách từ gần đến xa
            Collections.sort(filteredPlaces, new Comparator<Place>() {
                @Override
                public int compare(Place o1, Place o2) {
                    double d1 = calculateDistanceKm(userLat, userLng, o1.lat, o1.lng);
                    double d2 = calculateDistanceKm(userLat, userLng, o2.lat, o2.lng);
                    return Double.compare(d1, d2);
                }
            });

            currentPlacesList = filteredPlaces;
            final List<Place> finalPlaces = filteredPlaces;

            new Handler(Looper.getMainLooper()).post(() -> {
                if (!isAdded() || getContext() == null) return;
                if (tvPlaceCountNearby != null) {
                    tvPlaceCountNearby.setText(finalPlaces.size() + " quán");
                }
                displayPlacesOnMapAndList(finalPlaces);
            });
        });
    }

    private void displayPlacesOnMapAndList(List<Place> places) {
        if (mapView != null) {
            mapView.getOverlays().clear();
        }

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
                    p.address != null ? p.address : "Hoàng Mai"
            );
            list.add(r);

            if (mapView != null && p.lat != 0 && p.lng != 0) {
                GeoPoint point = new GeoPoint(p.lat, p.lng);
                Marker marker = new Marker(mapView);
                marker.setPosition(point);
                marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
                marker.setTitle(p.name);
                marker.setSnippet(String.format(Locale.US, "%.1f km • %dk–%dk", dist, p.priceMin / 1000, p.priceMax / 1000));

                marker.setOnMarkerClickListener((m, mv) -> {
                    showPlaceDetailDialog(r);
                    return true;
                });

                mapView.getOverlays().add(marker);
            }
        }

        if (mapView != null) {
            mapView.invalidate();
        }

        if (rvNearby != null) {
            RestaurantAdapter adapter = new RestaurantAdapter(list, restaurant -> {
                centerMapOnRestaurant(restaurant, places);
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

    private void centerMapOnRestaurant(Restaurant restaurant, List<Place> places) {
        if (mapView == null || restaurant == null) return;
        for (Place p : places) {
            if (p.name != null && p.name.equalsIgnoreCase(restaurant.getName())) {
                GeoPoint target = new GeoPoint(p.lat, p.lng);
                mapView.getController().animateTo(target);
                mapView.getController().setZoom(16.5);
                break;
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mapView != null) {
            mapView.onResume();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mapView != null) {
            mapView.onPause();
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
                String uriStr = "https://www.google.com/maps/search/?api=1&query=" + Uri.encode(place.getName() + " " + place.getTag2());
                Intent mapsIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(uriStr));
                startActivity(mapsIntent);
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
