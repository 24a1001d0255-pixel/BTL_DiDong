package com.example.btl_didong;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.button.MaterialButton;
import java.util.ArrayList;
import java.util.List;

public class NearbyFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_nearby, container, false);

        RecyclerView rvNearby = view.findViewById(R.id.rvNearbyRestaurants);
        if (rvNearby != null) {
            rvNearby.setLayoutManager(new LinearLayoutManager(getContext()));

            List<Restaurant> list = new ArrayList<>();
            list.add(new Restaurant("Phở Nhã", "https://images.unsplash.com/photo-1582878826629-29b7ad1cdc43?w=500", 4.9f, "0.8 km · 12 phút", "45k–75k", "Phở", "Món Việt"));
            list.add(new Restaurant("Bếp Việt 1988", "https://images.unsplash.com/photo-1546069901-ba9599a7e63c?w=500", 4.8f, "1.2 km · 16 phút", "40k–90k", "Cơm", "Món Việt"));
            list.add(new Restaurant("Tiệm Ăn Vặt & Trà", "https://images.unsplash.com/photo-1565299624946-b28f40a0ae38?w=500", 4.6f, "1.4 km · 15 phút", "25k–65k", "Ăn vặt", "Đồ uống"));
            list.add(new Restaurant("Bánh Mì Góc Phố", "https://images.unsplash.com/photo-1509722747041-616f39b57569?w=500", 4.7f, "1.6 km · 18 phút", "20k–40k", "Bánh mì", "Ăn nhanh"));
            list.add(new Restaurant("Bún Quê", "https://images.unsplash.com/photo-1569718212165-3a8278d5f624?w=500", 4.7f, "1.9 km · 20 phút", "40k–70k", "Bún", "Món Việt"));

            RestaurantAdapter adapter = new RestaurantAdapter(list, restaurant -> {
                showPlaceDetailDialog(restaurant);
            });
            rvNearby.setAdapter(adapter);
        }

        return view;
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
                String uriStr = "https://www.google.com/maps/search/?api=1&query=" + Uri.encode(place.getName());
                Intent mapsIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(uriStr));
                startActivity(mapsIntent);
            });
        }

        if (btnShopee != null) {
            btnShopee.setOnClickListener(v -> {
                String shopeeUrl = "https://shopeefood.vn/ho-chi-minh/tim-kiem?keyword=" + Uri.encode(place.getTag1());
                Intent shopeeIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(shopeeUrl));
                startActivity(shopeeIntent);
            });
        }

        if (btnGrab != null) {
            btnGrab.setOnClickListener(v -> {
                String grabUrl = "https://food.grab.com/vn/vi/restaurants?search=" + Uri.encode(place.getTag1());
                Intent grabIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(grabUrl));
                startActivity(grabIntent);
            });
        }

        dialog.show();
    }
}
