package com.example.btl_didong;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.button.MaterialButton;
import java.util.ArrayList;
import java.util.List;

public class SavedFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_saved, container, false);

        LinearLayout tabSavedFoods = view.findViewById(R.id.tabSavedFoods);
        LinearLayout tabSavedPlaces = view.findViewById(R.id.tabSavedPlaces);
        LinearLayout tabSavedLists = view.findViewById(R.id.tabSavedLists);

        TextView tvTabSavedFoods = view.findViewById(R.id.tvTabSavedFoods);
        TextView tvTabSavedPlaces = view.findViewById(R.id.tvTabSavedPlaces);
        TextView tvTabSavedLists = view.findViewById(R.id.tvTabSavedLists);

        View indicatorTabSavedFoods = view.findViewById(R.id.indicatorTabSavedFoods);
        View indicatorTabSavedPlaces = view.findViewById(R.id.indicatorTabSavedPlaces);
        View indicatorTabSavedLists = view.findViewById(R.id.indicatorTabSavedLists);

        RecyclerView rvSavedFoods = view.findViewById(R.id.rvSavedFoods);
        RecyclerView rvSavedPlaces = view.findViewById(R.id.rvSavedPlaces);
        LinearLayout contentSavedLists = view.findViewById(R.id.contentSavedLists);
        LinearLayout containerCustomLists = view.findViewById(R.id.containerCustomLists);
        MaterialButton btnCreateNewList = view.findViewById(R.id.btnCreateNewList);

        LinearLayout itemCustomList1 = view.findViewById(R.id.itemCustomList1);
        LinearLayout itemCustomList2 = view.findViewById(R.id.itemCustomList2);
        TextView tvListName1 = view.findViewById(R.id.tvListName1);
        TextView tvListName2 = view.findViewById(R.id.tvListName2);

        int activeColor = getResources().getColor(R.color.forest_green);
        int mutedColor = getResources().getColor(R.color.text_muted);

        // Setup Tab 1: Saved Foods RecyclerView (Grid 2 columns)
        if (rvSavedFoods != null) {
            rvSavedFoods.setLayoutManager(new GridLayoutManager(getContext(), 2));
            List<SavedItem> foodList = new ArrayList<>();
            foodList.add(new SavedItem("1", "Phở", "Khoảng 60k/người", "https://images.unsplash.com/photo-1582878826629-29b7ad1cdc43?w=500", "4.9", "FOOD"));
            foodList.add(new SavedItem("2", "Cơm", "Khoảng 55k/người", "https://images.unsplash.com/photo-1546069901-ba9599a7e63c?w=500", "4.8", "FOOD"));
            foodList.add(new SavedItem("3", "Bánh mì", "Khoảng 30k/người", "https://images.unsplash.com/photo-1509722747041-616f39b57569?w=500", "4.7", "FOOD"));
            foodList.add(new SavedItem("4", "Đồ nướng", "Khoảng 150k/người", "https://images.unsplash.com/photo-1555939594-58d7cb561ad1?w=500", "4.6", "FOOD"));

            SavedAdapter foodAdapter = new SavedAdapter(foodList, item -> {
                Toast.makeText(getContext(), "Món đã lưu: " + item.getTitle(), Toast.LENGTH_SHORT).show();
            });
            rvSavedFoods.setAdapter(foodAdapter);
        }

        // Setup Tab 2: Saved Places RecyclerView
        if (rvSavedPlaces != null) {
            rvSavedPlaces.setLayoutManager(new LinearLayoutManager(getContext()));
            List<Restaurant> placeList = new ArrayList<>();
            placeList.add(new Restaurant("Phở Nhã", "https://images.unsplash.com/photo-1582878826629-29b7ad1cdc43?w=500", 4.9f, "0.8 km · 12 phút", "45k–75k", "Phở", "Món Việt"));
            placeList.add(new Restaurant("Bếp Việt 1988", "https://images.unsplash.com/photo-1546069901-ba9599a7e63c?w=500", 4.8f, "1.2 km · 16 phút", "40k–90k", "Cơm", "Món Việt"));
            placeList.add(new Restaurant("Bánh Mì Góc Phố", "https://images.unsplash.com/photo-1509722747041-616f39b57569?w=500", 4.7f, "1.6 km · 18 phút", "20k–40k", "Bánh mì", "Ăn nhanh"));
            placeList.add(new Restaurant("Nướng Ngõ Nhỏ", "https://images.unsplash.com/photo-1555939594-58d7cb561ad1?w=500", 4.6f, "2.3 km · 24 phút", "99k–199k", "Đồ nướng", "Lẩu"));
            placeList.add(new Restaurant("Bún Quê", "https://images.unsplash.com/photo-1569718212165-3a8278d5f624?w=500", 4.7f, "1.9 km · 20 phút", "40k–70k", "Bún", "Món Việt"));

            RestaurantAdapter placeAdapter = new RestaurantAdapter(placeList, restaurant -> {
                showPlaceDetailDialog(restaurant);
            });
            rvSavedPlaces.setAdapter(placeAdapter);
        }

        // Setup Tab 3 List Item Click Handlers
        if (itemCustomList1 != null) {
            itemCustomList1.setOnClickListener(v -> {
                String name = tvListName1 != null ? tvListName1.getText().toString() : "Quán trưa văn phòng";
                Toast.makeText(getContext(), "Mở danh sách: " + name, Toast.LENGTH_SHORT).show();
            });
        }

        if (itemCustomList2 != null) {
            itemCustomList2.setOnClickListener(v -> {
                String name = tvListName2 != null ? tvListName2.getText().toString() : "Cuối tuần đi nhóm";
                Toast.makeText(getContext(), "Mở danh sách: " + name, Toast.LENGTH_SHORT).show();
            });
        }

        // Tab click handlers
        if (tabSavedFoods != null) {
            tabSavedFoods.setOnClickListener(v -> {
                if (rvSavedFoods != null) rvSavedFoods.setVisibility(View.VISIBLE);
                if (rvSavedPlaces != null) rvSavedPlaces.setVisibility(View.GONE);
                if (contentSavedLists != null) contentSavedLists.setVisibility(View.GONE);

                tvTabSavedFoods.setTextColor(activeColor);
                tvTabSavedPlaces.setTextColor(mutedColor);
                tvTabSavedLists.setTextColor(mutedColor);

                indicatorTabSavedFoods.setVisibility(View.VISIBLE);
                indicatorTabSavedPlaces.setVisibility(View.INVISIBLE);
                indicatorTabSavedLists.setVisibility(View.INVISIBLE);
            });
        }

        if (tabSavedPlaces != null) {
            tabSavedPlaces.setOnClickListener(v -> {
                if (rvSavedFoods != null) rvSavedFoods.setVisibility(View.GONE);
                if (rvSavedPlaces != null) rvSavedPlaces.setVisibility(View.VISIBLE);
                if (contentSavedLists != null) contentSavedLists.setVisibility(View.GONE);

                tvTabSavedFoods.setTextColor(mutedColor);
                tvTabSavedPlaces.setTextColor(activeColor);
                tvTabSavedLists.setTextColor(mutedColor);

                indicatorTabSavedFoods.setVisibility(View.INVISIBLE);
                indicatorTabSavedPlaces.setVisibility(View.VISIBLE);
                indicatorTabSavedLists.setVisibility(View.INVISIBLE);
            });
        }

        if (tabSavedLists != null) {
            tabSavedLists.setOnClickListener(v -> {
                if (rvSavedFoods != null) rvSavedFoods.setVisibility(View.GONE);
                if (rvSavedPlaces != null) rvSavedPlaces.setVisibility(View.GONE);
                if (contentSavedLists != null) contentSavedLists.setVisibility(View.VISIBLE);

                tvTabSavedFoods.setTextColor(mutedColor);
                tvTabSavedPlaces.setTextColor(mutedColor);
                tvTabSavedLists.setTextColor(activeColor);

                indicatorTabSavedFoods.setVisibility(View.INVISIBLE);
                indicatorTabSavedPlaces.setVisibility(View.INVISIBLE);
                indicatorTabSavedLists.setVisibility(View.VISIBLE);
            });
        }

        if (btnCreateNewList != null) {
            btnCreateNewList.setOnClickListener(v -> showCreateNewListDialog(containerCustomLists));
        }

        return view;
    }

    private void showCreateNewListDialog(LinearLayout containerCustomLists) {
        if (getContext() == null) return;

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("➕ Tạo danh sách mới");

        final EditText input = new EditText(getContext());
        input.setHint("Nhập tên danh sách (vd: Ăn đêm, Cafe chill...)");
        input.setPadding(40, 30, 40, 30);
        builder.setView(input);

        builder.setPositiveButton("Tạo", (dialog, which) -> {
            String listName = input.getText().toString().trim();
            if (!listName.isEmpty()) {
                addCustomListRow(containerCustomLists, listName);
                Toast.makeText(getContext(), "🎉 Đã tạo danh sách: " + listName, Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Hủy", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private void addCustomListRow(LinearLayout container, String listName) {
        if (container == null || getContext() == null) return;

        LinearLayout row = new LinearLayout(getContext());
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(android.view.Gravity.CENTER_VERTICAL);
        row.setBackgroundResource(R.drawable.bg_card);
        row.setPadding(32, 32, 32, 32);

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, 0, 0, 24);
        row.setLayoutParams(lp);

        row.setClickable(true);
        row.setFocusable(true);

        ImageView imgIcon = new ImageView(getContext());
        imgIcon.setImageResource(R.drawable.ic_bookmark);
        imgIcon.setColorFilter(getResources().getColor(R.color.forest_green));
        imgIcon.setBackgroundColor(0xFFE8F0EE);
        imgIcon.setPadding(16, 16, 16, 16);

        LinearLayout.LayoutParams imgLp = new LinearLayout.LayoutParams(72, 72);
        imgIcon.setLayoutParams(imgLp);

        LinearLayout textCol = new LinearLayout(getContext());
        textCol.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams textLp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        textLp.setMargins(24, 0, 0, 0);
        textCol.setLayoutParams(textLp);

        TextView tvTitle = new TextView(getContext());
        tvTitle.setText(listName);
        tvTitle.setTextSize(15f);
        tvTitle.setTypeface(null, android.graphics.Typeface.BOLD);
        tvTitle.setTextColor(getResources().getColor(R.color.text_dark));

        TextView tvSub = new TextView(getContext());
        tvSub.setText("0 mục · Riêng tư");
        tvSub.setTextSize(12f);
        tvSub.setTextColor(getResources().getColor(R.color.text_muted));

        textCol.addView(tvTitle);
        textCol.addView(tvSub);

        TextView tvArrow = new TextView(getContext());
        tvArrow.setText(">");
        tvArrow.setTextSize(18f);
        tvArrow.setTextColor(getResources().getColor(R.color.text_muted));

        row.addView(imgIcon);
        row.addView(textCol);
        row.addView(tvArrow);

        row.setOnClickListener(v -> Toast.makeText(getContext(), "Mở danh sách: " + listName, Toast.LENGTH_SHORT).show());

        container.addView(row);
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
