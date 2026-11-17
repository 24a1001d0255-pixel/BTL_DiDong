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
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.btl_didong.data.AppDatabase;
import com.example.btl_didong.data.Place;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;

public class SavedFragment extends Fragment {

    private RecyclerView rvSavedFoods;
    private RecyclerView rvSavedPlaces;
    private LinearLayout containerCustomLists;

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

        rvSavedFoods = view.findViewById(R.id.rvSavedFoods);
        rvSavedPlaces = view.findViewById(R.id.rvSavedPlaces);
        LinearLayout contentSavedLists = view.findViewById(R.id.contentSavedLists);
        containerCustomLists = view.findViewById(R.id.containerCustomLists);
        MaterialButton btnCreateNewList = view.findViewById(R.id.btnCreateNewList);

        // List 1 & List 2 Views
        LinearLayout itemCustomList1 = view.findViewById(R.id.itemCustomList1);
        LinearLayout itemCustomList2 = view.findViewById(R.id.itemCustomList2);
        TextView btnPrivacyBadge1 = view.findViewById(R.id.btnPrivacyBadge1);
        TextView btnPrivacyBadge2 = view.findViewById(R.id.btnPrivacyBadge2);
        TextView tvListCount1 = view.findViewById(R.id.tvListCount1);
        TextView tvListCount2 = view.findViewById(R.id.tvListCount2);
        TextView tvListName1 = view.findViewById(R.id.tvListName1);
        TextView tvListName2 = view.findViewById(R.id.tvListName2);
        MaterialButton btnAddPlaces1 = view.findViewById(R.id.btnAddPlaces1);
        MaterialButton btnAddPlaces2 = view.findViewById(R.id.btnAddPlaces2);

        int activeColor = getResources().getColor(R.color.forest_green);
        int mutedColor = getResources().getColor(R.color.text_muted);

        // Cả Tab 1 (Món đã lưu) và Tab 2 (Quán) đều dùng LinearLayoutManager thẻ nằm ngang giống nhau
        if (rvSavedFoods != null) {
            rvSavedFoods.setLayoutManager(new LinearLayoutManager(getContext()));
        }

        if (rvSavedPlaces != null) {
            rvSavedPlaces.setLayoutManager(new LinearLayoutManager(getContext()));
        }

        // Setup Privacy Toggle Badges
        if (btnPrivacyBadge1 != null) {
            btnPrivacyBadge1.setOnClickListener(v -> togglePrivacyBadge(btnPrivacyBadge1));
        }
        if (btnPrivacyBadge2 != null) {
            btnPrivacyBadge2.setOnClickListener(v -> togglePrivacyBadge(btnPrivacyBadge2));
        }

        // Setup Add Places Buttons
        if (btnAddPlaces1 != null) {
            btnAddPlaces1.setOnClickListener(v -> {
                String listName = tvListName1 != null ? tvListName1.getText().toString() : "Quán trưa văn phòng";
                showAddPlacesToListDialog(listName, tvListCount1);
            });
        }
        if (btnAddPlaces2 != null) {
            btnAddPlaces2.setOnClickListener(v -> {
                String listName = tvListName2 != null ? tvListName2.getText().toString() : "Cuối tuần đi nhóm";
                showAddPlacesToListDialog(listName, tvListCount2);
            });
        }

        // Bấm vào Danh sách 1 hoặc Danh sách 2 để xem các quán trong danh sách đó
        if (itemCustomList1 != null) {
            itemCustomList1.setOnClickListener(v -> {
                String listName = tvListName1 != null ? tvListName1.getText().toString() : "Quán trưa văn phòng";
                showCustomListDetailDialog(listName);
            });
            itemCustomList1.setOnLongClickListener(v -> {
                String listName = tvListName1 != null ? tvListName1.getText().toString() : "Quán trưa văn phòng";
                showDeleteListConfirmationDialog(itemCustomList1, listName);
                return true;
            });
        }
        if (itemCustomList2 != null) {
            itemCustomList2.setOnClickListener(v -> {
                String listName = tvListName2 != null ? tvListName2.getText().toString() : "Cuối tuần đi nhóm";
                showCustomListDetailDialog(listName);
            });
            itemCustomList2.setOnLongClickListener(v -> {
                String listName = tvListName2 != null ? tvListName2.getText().toString() : "Cuối tuần đi nhóm";
                showDeleteListConfirmationDialog(itemCustomList2, listName);
                return true;
            });
        }

        // Load dữ liệu các món/quán ăn đã lưu từ SharedPreferences & Room DB
        loadSavedItemsData();

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

    private void showDeleteListConfirmationDialog(View listRowView, String listName) {
        if (getContext() == null || listName == null) return;

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("🗑️ Xóa danh sách");
        builder.setMessage("Bạn có chắc chắn muốn xóa danh sách '" + listName + "' không?");

        builder.setPositiveButton("Có", (dialog, which) -> {
            if (listRowView != null && listRowView.getParent() != null) {
                ((ViewGroup) listRowView.getParent()).removeView(listRowView);
            }

            // Xóa khỏi SharedPreferences
            SharedPreferences prefs = getContext().getSharedPreferences("food_picker_prefs", Context.MODE_PRIVATE);
            prefs.edit().remove("list_items_" + listName).apply();

            Toast.makeText(getContext(), "🗑️ Đã xóa danh sách: " + listName, Toast.LENGTH_SHORT).show();
        });

        builder.setNegativeButton("Không", (dialog, which) -> dialog.dismiss());

        builder.show();
    }

    private void showCustomListDetailDialog(String listName) {
        if (getContext() == null) return;
        Context ctx = getContext().getApplicationContext();

        Executors.newSingleThreadExecutor().execute(() -> {
            SharedPreferences prefs = ctx.getSharedPreferences("food_picker_prefs", Context.MODE_PRIVATE);
            String listItemsStr = prefs.getString("list_items_" + listName, "");

            AppDatabase db = AppDatabase.get(ctx);
            List<Place> allDbPlaces = db.placeDao().getAllPlaces();

            List<Restaurant> restaurantsInList = new ArrayList<>();

            for (Place p : allDbPlaces) {
                if (listItemsStr.contains(p.name)) {
                    Restaurant r = new Restaurant(
                            p.name,
                            getFoodImageUrl(p.category),
                            4.8f,
                            "1.2 km · 16 phút",
                            (p.priceMin / 1000) + "k–" + (p.priceMax / 1000) + "k",
                            p.category != null ? p.category : "Món Việt",
                            p.address != null ? p.address : "Hoàng Mai"
                    );
                    restaurantsInList.add(r);
                }
            }

            if (restaurantsInList.isEmpty()) {
                restaurantsInList.add(new Restaurant("Phở bò gia truyền Định Công", "https://images.unsplash.com/photo-1582878826629-29b7ad1cdc43?w=500", 4.9f, "0.8 km · 12 phút", "45k–75k", "Phở", "Món Việt"));
                restaurantsInList.add(new Restaurant("Cơm tấm A Vương Đền Lừ", "https://images.unsplash.com/photo-1546069901-ba9599a7e63c?w=500", 4.8f, "1.2 km · 16 phút", "40k–90k", "Cơm", "Món Việt"));
                restaurantsInList.add(new Restaurant("Bánh mì chảo Tân Mai", "https://images.unsplash.com/photo-1509722747041-616f39b57569?w=500", 4.7f, "1.6 km · 18 phút", "20k–40k", "Bánh mì", "Ăn nhanh"));
            }

            final List<Restaurant> finalList = restaurantsInList;

            new Handler(Looper.getMainLooper()).post(() -> {
                if (!isAdded() || getContext() == null) return;

                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setTitle("📁 Danh sách: " + listName);

                String[] names = new String[finalList.size()];
                for (int i = 0; i < finalList.size(); i++) {
                    names[i] = finalList.get(i).getName() + " (" + finalList.get(i).getPriceRange() + ")";
                }

                builder.setItems(names, (dialog, which) -> {
                    showPlaceDetailDialog(finalList.get(which));
                });

                builder.setPositiveButton("Đóng", (dialog, which) -> dialog.dismiss());
                builder.show();
            });
        });
    }

    private void togglePrivacyBadge(TextView badge) {
        if (badge == null || getContext() == null) return;
        String text = badge.getText().toString();
        if (text.contains("Công khai")) {
            badge.setText("🔒 Riêng tư");
            badge.setTextColor(getResources().getColor(R.color.text_muted));
            badge.setBackgroundColor(0xFFEFE8DF);
            Toast.makeText(getContext(), "Đã chuyển thành RIÊNG TƯ 🔒", Toast.LENGTH_SHORT).show();
        } else {
            badge.setText("🌐 Công khai");
            badge.setTextColor(getResources().getColor(R.color.forest_green));
            badge.setBackgroundColor(getResources().getColor(R.color.forest_green_light));
            Toast.makeText(getContext(), "Đã chuyển thành CÔNG KHAI 🌐", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        loadSavedItemsData();
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

            // 1. Tải danh sách Quán đã lưu (Saved Places)
            for (Place p : allDbPlaces) {
                if (savedRestaurants.contains(p.name)) {
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
                    foodPlacesList.add(r);
                }
            }

            // 2. Tải các món ăn được lưu từ Vòng quay
            if (!savedFoods.isEmpty()) {
                String[] foods = savedFoods.split(";");
                for (String f : foods) {
                    if (!f.trim().isEmpty()) {
                        boolean alreadyAdded = false;
                        for (Restaurant r : foodPlacesList) {
                            if (r.getName().equalsIgnoreCase(f.trim())) {
                                alreadyAdded = true;
                                break;
                            }
                        }
                        if (!alreadyAdded) {
                            foodPlacesList.add(new Restaurant(
                                    f.trim(),
                                    getFoodImageUrl(f.trim()),
                                    4.8f,
                                    "1.2 km · 16 phút",
                                    "30k–60k",
                                    f.trim(),
                                    "Hoàng Mai"
                            ));
                        }
                    }
                }
            }

            // Mẫu khởi tạo ban đầu nếu chưa lưu quán nào
            if (foodPlacesList.isEmpty()) {
                foodPlacesList.add(new Restaurant("Phở Sốt Vang Nguyễn An Ninh", "https://images.unsplash.com/photo-1582878826629-29b7ad1cdc43?w=500", 4.8f, "1.2 km · 16 phút", "35k–50k", "Phở", "Nguyễn An Ninh"));
                foodPlacesList.add(new Restaurant("Lẩu riêu cua bắp bò Đền Lừ", "https://images.unsplash.com/photo-1541832676-9b763b0239ab?w=500", 4.8f, "1.2 km · 16 phút", "90k–160k", "Lẩu", "Hồ Đền Lừ"));
                foodPlacesList.add(new Restaurant("Ốc luộc & Nem chua rán Hồ Đền Lừ", "https://images.unsplash.com/photo-1565299624946-b28f40a0ae38?w=500", 4.8f, "1.2 km · 16 phút", "30k–60k", "Ăn vặt", "Hồ Đền Lừ"));
                foodPlacesList.add(new Restaurant("Trà sữa Leyao Hoàng Mai", "https://images.unsplash.com/photo-1517701550927-30cf4ba1dba5?w=500", 4.8f, "1.2 km · 16 phút", "20k–30k", "Trà sữa", "Tân Mai"));
            }

            if (placeList.isEmpty()) {
                placeList.add(new Restaurant("Phở Sốt Vang Nguyễn An Ninh", "https://images.unsplash.com/photo-1582878826629-29b7ad1cdc43?w=500", 4.8f, "1.2 km · 16 phút", "35k–50k", "Phở", "Nguyễn An Ninh"));
                placeList.add(new Restaurant("Lẩu riêu cua bắp bò Đền Lừ", "https://images.unsplash.com/photo-1541832676-9b763b0239ab?w=500", 4.8f, "1.2 km · 16 phút", "90k–160k", "Lẩu", "Hồ Đền Lừ"));
                placeList.add(new Restaurant("Ốc luộc & Nem chua rán Hồ Đền Lừ", "https://images.unsplash.com/photo-1565299624946-b28f40a0ae38?w=500", 4.8f, "1.2 km · 16 phút", "30k–60k", "Ăn vặt", "Hồ Đền Lừ"));
                placeList.add(new Restaurant("Trà sữa Leyao Hoàng Mai", "https://images.unsplash.com/photo-1517701550927-30cf4ba1dba5?w=500", 4.8f, "1.2 km · 16 phút", "20k–30k", "Trà sữa", "Tân Mai"));
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

        LinearLayout.LayoutParams imgLp = new LinearLayout.LayoutParams(76, 76);
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

        LinearLayout subRow = new LinearLayout(getContext());
        subRow.setOrientation(LinearLayout.HORIZONTAL);
        subRow.setGravity(android.view.Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams subLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        subLp.setMargins(0, 8, 0, 0);
        subRow.setLayoutParams(subLp);

        TextView tvCount = new TextView(getContext());
        tvCount.setText("0 mục");
        tvCount.setTextSize(12f);
        tvCount.setTextColor(getResources().getColor(R.color.text_muted));
        LinearLayout.LayoutParams countLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        countLp.setMargins(0, 0, 16, 0);
        tvCount.setLayoutParams(countLp);

        TextView badge = new TextView(getContext());
        badge.setText("🌐 Công khai");
        badge.setTextSize(11f);
        badge.setTypeface(null, android.graphics.Typeface.BOLD);
        badge.setTextColor(getResources().getColor(R.color.forest_green));
        badge.setBackgroundColor(getResources().getColor(R.color.forest_green_light));
        badge.setPadding(20, 8, 20, 8);
        badge.setClickable(true);
        badge.setFocusable(true);

        badge.setOnClickListener(v -> togglePrivacyBadge(badge));

        subRow.addView(tvCount);
        subRow.addView(badge);

        textCol.addView(tvTitle);
        textCol.addView(subRow);

        MaterialButton btnAdd = new MaterialButton(getContext());
        btnAdd.setText("+");
        btnAdd.setTextSize(18f);
        btnAdd.setTypeface(null, android.graphics.Typeface.BOLD);
        btnAdd.setTextColor(getResources().getColor(R.color.white));
        btnAdd.setBackgroundColor(getResources().getColor(R.color.forest_green));
        btnAdd.setPadding(0, 0, 0, 0);

        LinearLayout.LayoutParams btnLp = new LinearLayout.LayoutParams(76, 76);
        btnLp.setMargins(16, 0, 0, 0);
        btnAdd.setLayoutParams(btnLp);

        btnAdd.setOnClickListener(v -> showAddPlacesToListDialog(listName, tvCount));

        row.addView(imgIcon);
        row.addView(textCol);
        row.addView(btnAdd);

        row.setOnClickListener(v -> showCustomListDetailDialog(listName));
        row.setOnLongClickListener(v -> {
            showDeleteListConfirmationDialog(row, listName);
            return true;
        });

        container.addView(row);
    }

    private void showAddPlacesToListDialog(String listName, TextView tvCount) {
        if (getContext() == null) return;
        Context ctx = getContext().getApplicationContext();

        Executors.newSingleThreadExecutor().execute(() -> {
            SharedPreferences prefs = ctx.getSharedPreferences("food_picker_prefs", Context.MODE_PRIVATE);
            String savedRestaurants = prefs.getString("saved_restaurants", "");

            AppDatabase db = AppDatabase.get(ctx);
            List<Place> allDbPlaces = db.placeDao().getAllPlaces();

            List<Restaurant> availableList = new ArrayList<>();

            // Tải tất cả các quán ăn trong bộ sưu tập đã lưu của người dùng
            for (Place p : allDbPlaces) {
                if (savedRestaurants.contains(p.name)) {
                    Restaurant r = new Restaurant(
                            p.name,
                            getFoodImageUrl(p.category),
                            4.8f,
                            "1.2 km · 16 phút",
                            (p.priceMin / 1000) + "k–" + (p.priceMax / 1000) + "k",
                            p.category != null ? p.category : "Món Việt",
                            p.address != null ? p.address : "Hoàng Mai"
                    );
                    availableList.add(r);
                }
            }

            // Nếu bộ sưu tập người dùng còn trống, nạp toàn bộ các quán mẫu từ database
            if (availableList.isEmpty()) {
                for (Place p : allDbPlaces) {
                    Restaurant r = new Restaurant(
                            p.name,
                            getFoodImageUrl(p.category),
                            4.8f,
                            "1.5 km · 17 phút",
                            (p.priceMin / 1000) + "k–" + (p.priceMax / 1000) + "k",
                            p.category != null ? p.category : "Món Việt",
                            p.address != null ? p.address : "Hoàng Mai"
                    );
                    availableList.add(r);
                }
            }

            // Đọc lại danh sách các quán đã được chọn vào listName này
            Set<String> selectedSet = new HashSet<>();
            String existingSelected = prefs.getString("list_items_" + listName, "");
            if (!existingSelected.isEmpty()) {
                String[] items = existingSelected.split(";");
                for (String item : items) {
                    if (!item.trim().isEmpty()) {
                        selectedSet.add(item.trim());
                    }
                }
            }

            final List<Restaurant> finalAvailableList = availableList;
            final Set<String> finalInitialSelected = selectedSet;

            new Handler(Looper.getMainLooper()).post(() -> {
                if (!isAdded() || getContext() == null) return;

                View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_add_to_list, null, false);
                AlertDialog dialog = new AlertDialog.Builder(getContext(), android.R.style.Theme_DeviceDefault_Light_Dialog_NoActionBar)
                        .setView(dialogView)
                        .create();

                if (dialog.getWindow() != null) {
                    dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
                }

                TextView tvTitle = dialogView.findViewById(R.id.tvAddDialogTitle);
                ImageView btnClose = dialogView.findViewById(R.id.btnCloseAddDialog);
                RecyclerView rvPlaces = dialogView.findViewById(R.id.rvAddListPlaces);
                MaterialButton btnConfirm = dialogView.findViewById(R.id.btnConfirmAddToList);

                if (tvTitle != null) {
                    tvTitle.setText("➕ Chọn quán / món cho " + listName);
                }

                if (btnClose != null) {
                    btnClose.setOnClickListener(v -> dialog.dismiss());
                }

                AddToListAdapter adapter = new AddToListAdapter(finalAvailableList, finalInitialSelected, restaurant -> {
                    // Khi nhấn vào thẻ quán ➔ Hiện THÔNG TIN QUÁN (Hình 3)
                    showPlaceDetailDialog(restaurant);
                });

                if (rvPlaces != null) {
                    rvPlaces.setLayoutManager(new LinearLayoutManager(getContext()));
                    rvPlaces.setAdapter(adapter);
                }

                if (btnConfirm != null) {
                    btnConfirm.setOnClickListener(v -> {
                        Set<String> newSelectedSet = adapter.getSelectedNames();
                        StringBuilder sb = new StringBuilder();
                        for (String name : newSelectedSet) {
                            sb.append(name).append(";");
                        }

                        prefs.edit().putString("list_items_" + listName, sb.toString()).apply();

                        if (tvCount != null) {
                            tvCount.setText(newSelectedSet.size() + " mục");
                        }

                        Toast.makeText(getContext(), "🎉 Đã cập nhật " + newSelectedSet.size() + " quán/món vào '" + listName + "'", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    });
                }

                dialog.show();

                if (dialog.getWindow() != null && getActivity() != null) {
                    DisplayMetrics metrics = new DisplayMetrics();
                    getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);
                    int width = (int) (metrics.widthPixels * 0.92);
                    dialog.getWindow().setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT);
                }
            });
        });
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
                String uriStr = "https://www.google.com/maps/search/?api=1&query=" + Uri.encode(place.getName() + " Hoàng Mai");
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
