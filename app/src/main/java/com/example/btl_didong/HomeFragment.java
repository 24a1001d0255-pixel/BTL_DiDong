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
import androidx.core.content.ContextCompat;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import com.bumptech.glide.Glide;
import com.example.btl_didong.data.AppDatabase;
import com.example.btl_didong.data.Place;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.slider.Slider;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;

public class HomeFragment extends Fragment {
    private SpinningWheelView wheelView;
    private List<String> currentItems = new ArrayList<>(Arrays.asList("Phở", "Cơm", "Bánh mì", "Đồ nướng", "Ăn vặt", "Đồ uống", "Bún", "Lẩu"));
    private String currentMode = "SOLO";
    private String selectedGroupOption = "SINGLE"; // "SINGLE" (Một quán chung) hoặc "CLUSTER" (Cụm quán gần)
    private int groupPeopleCount = 4;
    private int currentBudgetMax = 80000;
    private float currentRadius = 2.0f;
    private String lastSelectedFood = "";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        NestedScrollView scrollView = (NestedScrollView) view;
        wheelView = view.findViewById(R.id.spinningWheel);
        MaterialButton btnSpin = view.findViewById(R.id.btnSpin);
        MaterialButton btnSpinAgain = view.findViewById(R.id.btnSpinAgain);
        TextView btnEditWheel = view.findViewById(R.id.btnEditWheel);
        TextView tvGroupCount = view.findViewById(R.id.tvGroupCount);
        MaterialButton btnLocationDistance = view.findViewById(R.id.btnLocationDistance);
        MaterialButtonToggleGroup toggleGroupMode = view.findViewById(R.id.toggleGroupMode);
        MaterialButton btnSolo = view.findViewById(R.id.btnSolo);
        MaterialButton btnGroup = view.findViewById(R.id.btnGroup);
        Slider sliderBudget = view.findViewById(R.id.sliderBudget);
        Slider sliderRadius = view.findViewById(R.id.sliderRadius);

        // Group Extra Controls
        LinearLayout layoutGroupExtraControls = view.findViewById(R.id.layoutGroupExtraControls);
        TextView tvPeopleCount = view.findViewById(R.id.tvPeopleCount);
        MaterialButton btnPeopleMinus = view.findViewById(R.id.btnPeopleMinus);
        MaterialButton btnPeoplePlus = view.findViewById(R.id.btnPeoplePlus);
        MaterialButtonToggleGroup toggleGroupType = view.findViewById(R.id.toggleGroupType);
        MaterialButton btnSingleOption = view.findViewById(R.id.btnSingleOption);
        MaterialButton btnClusterOption = view.findViewById(R.id.btnClusterOption);
        MaterialButton btnFindGroupOption = view.findViewById(R.id.btnFindGroupOption);

        LinearLayout layoutResultSection = view.findViewById(R.id.layoutResultSection);
        TextView tvSelectedFoodName = view.findViewById(R.id.tvSelectedFoodName);
        TextView tvNearbySectionTitle = view.findViewById(R.id.tvNearbySectionTitle);
        TextView tvBadgeCount = view.findViewById(R.id.tvBadgeCount);
        LinearLayout containerNearbyPlaces = view.findViewById(R.id.containerNearbyPlaces);

        updateGroupCount(tvGroupCount);

        if (wheelView != null) {
            wheelView.setItems(currentItems);
        }

        if (btnLocationDistance != null) {
            btnLocationDistance.setOnClickListener(v -> {
                Intent intent = new Intent(getActivity(), SurveyActivity.class);
                startActivity(intent);
            });
        }

        if (sliderBudget != null) {
            sliderBudget.addOnChangeListener((slider, value, fromUser) -> currentBudgetMax = (int) value);
        }

        if (sliderRadius != null) {
            sliderRadius.addOnChangeListener((slider, value, fromUser) -> currentRadius = value);
        }

        // People Counter logic
        if (btnPeopleMinus != null) {
            btnPeopleMinus.setOnClickListener(v -> {
                if (groupPeopleCount > 2) {
                    groupPeopleCount--;
                    if (tvPeopleCount != null) tvPeopleCount.setText(String.valueOf(groupPeopleCount));
                }
            });
        }
        if (btnPeoplePlus != null) {
            btnPeoplePlus.setOnClickListener(v -> {
                if (groupPeopleCount < 20) {
                    groupPeopleCount++;
                    if (tvPeopleCount != null) tvPeopleCount.setText(String.valueOf(groupPeopleCount));
                }
            });
        }

        // Mode Toggle
        toggleGroupMode.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                if (checkedId == R.id.btnSolo) {
                    currentMode = "SOLO";
                    if (layoutGroupExtraControls != null) layoutGroupExtraControls.setVisibility(View.GONE);

                    btnSolo.setBackgroundColor(getResources().getColor(R.color.white));
                    btnSolo.setTextColor(getResources().getColor(R.color.forest_green));
                    btnGroup.setBackgroundColor(getResources().getColor(android.R.color.transparent));
                    btnGroup.setTextColor(getResources().getColor(R.color.text_muted));

                    currentItems = new ArrayList<>(Arrays.asList("Phở", "Cơm", "Bánh mì", "Đồ uống", "Ăn vặt", "Đồ nướng", "Bún", "Lẩu"));
                } else if (checkedId == R.id.btnGroup) {
                    currentMode = "GROUP";
                    if (layoutGroupExtraControls != null) layoutGroupExtraControls.setVisibility(View.VISIBLE);

                    btnGroup.setBackgroundColor(getResources().getColor(R.color.white));
                    btnGroup.setTextColor(getResources().getColor(R.color.forest_green));
                    btnSolo.setBackgroundColor(getResources().getColor(android.R.color.transparent));
                    btnSolo.setTextColor(getResources().getColor(R.color.text_muted));

                    currentItems = new ArrayList<>(Arrays.asList("Chợ đêm", "Lẩu nướng", "Buffet", "Lẩu thái", "Bò nướng", "Hải sản", "Xiên bẩn", "Trà sữa"));
                }
                if (wheelView != null) {
                    wheelView.setItems(currentItems);
                }
                updateGroupCount(tvGroupCount);
            }
        });

        // Group Option Toggle: SINGLE vs CLUSTER
        if (toggleGroupType != null) {
            toggleGroupType.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
                if (isChecked) {
                    if (checkedId == R.id.btnSingleOption) {
                        selectedGroupOption = "SINGLE";
                        btnSingleOption.setBackgroundColor(getResources().getColor(R.color.forest_green_light));
                        btnSingleOption.setTextColor(getResources().getColor(R.color.forest_green));
                        btnClusterOption.setBackgroundColor(getResources().getColor(R.color.white));
                        btnClusterOption.setTextColor(getResources().getColor(R.color.text_muted));
                    } else if (checkedId == R.id.btnClusterOption) {
                        selectedGroupOption = "CLUSTER";
                        btnClusterOption.setBackgroundColor(getResources().getColor(R.color.forest_green_light));
                        btnClusterOption.setTextColor(getResources().getColor(R.color.forest_green));
                        btnSingleOption.setBackgroundColor(getResources().getColor(R.color.white));
                        btnSingleOption.setTextColor(getResources().getColor(R.color.text_muted));
                    }
                }
            });
        }

        // Action: Find option for Group
        if (btnFindGroupOption != null) {
            btnFindGroupOption.setOnClickListener(v -> {
                checkAndRequestLocationPermission();
                if (layoutResultSection != null) layoutResultSection.setVisibility(View.VISIBLE);
                if (tvSelectedFoodName != null) {
                    tvSelectedFoodName.setText(selectedGroupOption.equals("SINGLE") ? "Một quán chung cho " + groupPeopleCount + " người" : "Cụm quán / Khu ẩm thực");
                }
                if (tvNearbySectionTitle != null) {
                    tvNearbySectionTitle.setText(selectedGroupOption.equals("SINGLE") ? "Các quán rộng rãi cho nhóm" : "Cụm quán / Chợ đêm nổi bật");
                }

                loadAndDisplayGroupOptionPlaces(containerNearbyPlaces, tvBadgeCount);

                if (scrollView != null && layoutResultSection != null) {
                    scrollView.post(() -> scrollView.smoothScrollTo(0, layoutResultSection.getTop()));
                }
            });
        }

        View.OnClickListener spinAction = v -> {
            checkAndRequestLocationPermission();

            if (currentItems != null && !currentItems.isEmpty()) {
                int selectedIndex = (int) (Math.random() * currentItems.size());
                lastSelectedFood = currentItems.get(selectedIndex);

                if (wheelView != null) {
                    wheelView.spinWheel(selectedIndex);
                }

                saveSpinToHistory(lastSelectedFood);

                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    showWinnerWinnerPopup(lastSelectedFood, () -> {
                        if (layoutResultSection != null) {
                            layoutResultSection.setVisibility(View.VISIBLE);
                        }
                        if (tvSelectedFoodName != null) {
                            tvSelectedFoodName.setText(lastSelectedFood);
                        }
                        if (tvNearbySectionTitle != null) {
                            tvNearbySectionTitle.setText("Quán bán " + lastSelectedFood + " gần bạn");
                        }

                        loadAndDisplayPlaces(lastSelectedFood, containerNearbyPlaces, tvBadgeCount);

                        if (scrollView != null && layoutResultSection != null) {
                            scrollView.post(() -> scrollView.smoothScrollTo(0, layoutResultSection.getTop()));
                        }
                    });
                }, 3200);
            }
        };

        btnSpin.setOnClickListener(spinAction);
        if (btnSpinAgain != null) {
            btnSpinAgain.setOnClickListener(spinAction);
        }

        if (btnEditWheel != null) {
            btnEditWheel.setOnClickListener(v -> showCustomEditWheelDialog(tvGroupCount));
        }

        return view;
    }

    private void saveSpinToHistory(String foodName) {
        if (getContext() == null) return;
        SharedPreferences prefs = getContext().getSharedPreferences("food_picker_prefs", Context.MODE_PRIVATE);
        String history = prefs.getString("spin_history", "");
        String timeStr = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());
        String entry = foodName + " - " + timeStr;
        if (!history.isEmpty()) {
            history = entry + ";" + history;
        } else {
            history = entry;
        }
        prefs.edit().putString("spin_history", history).apply();
    }

    private void showWinnerWinnerPopup(String foodName, Runnable onDismissAction) {
        if (getContext() == null) return;

        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_spin_winner, null);
        AlertDialog dialog = new AlertDialog.Builder(getContext(), android.R.style.Theme_DeviceDefault_Light_Dialog_NoActionBar)
                .setView(dialogView)
                .create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        TextView tvWinnerFoodName = dialogView.findViewById(R.id.tvWinnerFoodName);
        MaterialButton btnSee = dialogView.findViewById(R.id.btnSeeWinnerPlaces);

        if (tvWinnerFoodName != null) {
            tvWinnerFoodName.setText(foodName);
        }

        btnSee.setOnClickListener(v -> {
            dialog.dismiss();
            if (onDismissAction != null) {
                onDismissAction.run();
            }
        });

        dialog.show();
    }

    private void checkAndRequestLocationPermission() {
        if (getContext() != null && ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1001);
        }
    }

    private void updateGroupCount(TextView tvGroupCount) {
        if (tvGroupCount != null) {
            tvGroupCount.setText(currentItems.size() + " nhóm món");
        }
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
        } else if (lower.contains("hải sản")) {
            return "https://images.unsplash.com/photo-1615141982883-c7ad0e69fd62?w=500";
        } else {
            return "https://images.unsplash.com/photo-1504674900247-0877df9cc836?w=500";
        }
    }

    private void loadAndDisplayGroupOptionPlaces(LinearLayout container, TextView tvBadge) {
        if (container == null) return;

        Executors.newSingleThreadExecutor().execute(() -> {
            AppDatabase db = AppDatabase.get(requireContext());
            List<Place> places;

            if ("SINGLE".equals(selectedGroupOption)) {
                places = db.placeDao().findSingleGroupPlaces(currentBudgetMax * groupPeopleCount);
            } else {
                places = db.placeDao().findClusterGroupPlaces(currentBudgetMax * groupPeopleCount);
            }

            if (places.isEmpty()) {
                places = db.placeDao().findSingleGroupPlaces(500000);
            }

            final List<Place> finalPlaces = places;
            new Handler(Looper.getMainLooper()).post(() -> {
                container.removeAllViews();
                if (tvBadge != null) {
                    tvBadge.setText(finalPlaces.size() + " địa điểm");
                }

                for (Place p : finalPlaces) {
                    View cardView = LayoutInflater.from(getContext()).inflate(R.layout.item_restaurant_card, container, false);

                    TextView tvName = cardView.findViewById(R.id.tvRestaurantName);
                    TextView tvRating = cardView.findViewById(R.id.tvRestaurantRating);
                    TextView tvSub = cardView.findViewById(R.id.tvRestaurantSub);
                    TextView tag1 = cardView.findViewById(R.id.tag1);
                    TextView tag2 = cardView.findViewById(R.id.tag2);
                    TextView tagMatch = cardView.findViewById(R.id.tagMatch);
                    ImageView imgRestaurant = cardView.findViewById(R.id.imgRestaurant);

                    if (tvName != null) tvName.setText(p.name);
                    if (tvRating != null) tvRating.setText("4.8");
                    if (tvSub != null) {
                        if ("CLUSTER".equals(p.placeType)) {
                            tvSub.setText("Gồm có: " + (p.categoriesList != null ? p.categoriesList : "Trà sữa, Bánh mì, Ăn vặt"));
                        } else {
                            tvSub.setText("1.2 km • Sức chứa " + (groupPeopleCount + 2) + "+ người • " + (p.priceMin / 1000) + "k–" + (p.priceMax / 1000) + "k");
                        }
                    }

                    if (tag1 != null) tag1.setText("CLUSTER".equals(p.placeType) ? "Cụm quán" : "Một quán chung");
                    if (tag2 != null) tag2.setText("Cho nhóm");
                    if (tagMatch != null) tagMatch.setText("Phù hợp nhóm " + groupPeopleCount + " người");

                    if (imgRestaurant != null) {
                        Glide.with(HomeFragment.this)
                                .load(getFoodImageUrl(p.category != null ? p.category : p.name))
                                .centerCrop()
                                .placeholder(R.drawable.ic_launcher_background)
                                .into(imgRestaurant);
                    }

                    cardView.setOnClickListener(v -> showPlaceDetailDialog(p, p.category != null ? p.category : p.name));

                    container.addView(cardView);
                }
            });
        });
    }

    private void loadAndDisplayPlaces(String foodName, LinearLayout container, TextView tvBadge) {
        if (container == null) return;

        Executors.newSingleThreadExecutor().execute(() -> {
            AppDatabase db = AppDatabase.get(requireContext());
            List<Place> places = db.placeDao().findPlaces(currentMode, foodName, 0, currentBudgetMax);
            if (places.isEmpty()) {
                places = db.placeDao().findPlaces(currentMode, "Phở", 0, 200000);
            }

            final List<Place> finalPlaces = places;
            new Handler(Looper.getMainLooper()).post(() -> {
                container.removeAllViews();
                if (tvBadge != null) {
                    tvBadge.setText(finalPlaces.size() + " quán");
                }

                for (Place p : finalPlaces) {
                    View cardView = LayoutInflater.from(getContext()).inflate(R.layout.item_restaurant_card, container, false);

                    TextView tvName = cardView.findViewById(R.id.tvRestaurantName);
                    TextView tvRating = cardView.findViewById(R.id.tvRestaurantRating);
                    TextView tvSub = cardView.findViewById(R.id.tvRestaurantSub);
                    TextView tag1 = cardView.findViewById(R.id.tag1);
                    TextView tag2 = cardView.findViewById(R.id.tag2);
                    TextView tagMatch = cardView.findViewById(R.id.tagMatch);
                    ImageView imgRestaurant = cardView.findViewById(R.id.imgRestaurant);

                    if (tvName != null) tvName.setText(p.name);
                    if (tvRating != null) tvRating.setText("4.8");
                    if (tvSub != null) tvSub.setText("1.2 km • 16 phút • " + (p.priceMin / 1000) + "k–" + (p.priceMax / 1000) + "k");
                    if (tag1 != null) tag1.setText(p.category != null ? p.category : foodName);
                    if (tag2 != null) tag2.setText("Món Việt");
                    if (tagMatch != null) tagMatch.setText("Khớp " + foodName);

                    if (imgRestaurant != null) {
                        Glide.with(HomeFragment.this)
                                .load(getFoodImageUrl(p.category != null ? p.category : foodName))
                                .centerCrop()
                                .placeholder(R.drawable.ic_launcher_background)
                                .into(imgRestaurant);
                    }

                    cardView.setOnClickListener(v -> showPlaceDetailDialog(p, foodName));

                    container.addView(cardView);
                }
            });
        });
    }

    private void showPlaceDetailDialog(Place place, String foodName) {
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
        TextView tvDishName = dialogView.findViewById(R.id.tvDetailDishName);
        MaterialButton btnMaps = dialogView.findViewById(R.id.btnOpenGoogleMaps);
        MaterialButton btnShopee = dialogView.findViewById(R.id.btnOpenShopeeFood);
        MaterialButton btnGrab = dialogView.findViewById(R.id.btnOpenGrabFood);

        if (tvPlaceName != null) tvPlaceName.setText(place.name);
        if (tvDetailSub != null) tvDetailSub.setText("1.2 km • 16 phút • " + (place.priceMin / 1000) + "k–" + (place.priceMax / 1000) + "k");
        if (tvDishName != null) tvDishName.setText(foodName);

        if (btnMaps != null) {
            btnMaps.setOnClickListener(v -> {
                String uriStr = "https://www.google.com/maps/search/?api=1&query=" + Uri.encode(place.name + " " + place.address);
                Intent mapsIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(uriStr));
                startActivity(mapsIntent);
            });
        }

        if (btnShopee != null) {
            btnShopee.setOnClickListener(v -> {
                String shopeeUrl = "https://shopeefood.vn/ho-chi-minh/tim-kiem?keyword=" + Uri.encode(foodName);
                Intent shopeeIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(shopeeUrl));
                startActivity(shopeeIntent);
            });
        }

        if (btnGrab != null) {
            btnGrab.setOnClickListener(v -> {
                String grabUrl = "https://food.grab.com/vn/vi/restaurants?search=" + Uri.encode(foodName);
                Intent grabIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(grabUrl));
                startActivity(grabIntent);
            });
        }

        dialog.show();
    }

    private void showCustomEditWheelDialog(TextView tvGroupCount) {
        if (getContext() == null) return;

        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_edit_wheel, null);
        AlertDialog dialog = new AlertDialog.Builder(getContext(), android.R.style.Theme_DeviceDefault_Light_Dialog_NoActionBar)
                .setView(dialogView)
                .create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        LinearLayout container = dialogView.findViewById(R.id.containerWheelItems);
        ImageView btnClose = dialogView.findViewById(R.id.btnCloseDialog);
        MaterialButton btnSave = dialogView.findViewById(R.id.btnSaveWheel);

        List<View> rowViews = new ArrayList<>();

        for (int i = 0; i < currentItems.size(); i++) {
            View rowView = LayoutInflater.from(getContext()).inflate(R.layout.item_wheel_edit_row, container, false);
            TextView tvIndex = rowView.findViewById(R.id.tvIndex);
            EditText etItem = rowView.findViewById(R.id.etWheelItem);
            ImageView btnDelete = rowView.findViewById(R.id.btnDeleteItem);

            tvIndex.setText(String.valueOf(i + 1));
            etItem.setText(currentItems.get(i));

            btnDelete.setOnClickListener(v -> {
                if (container.getChildCount() > 2) {
                    container.removeView(rowView);
                    rowViews.remove(rowView);
                    reindexRows(container);
                }
            });

            container.addView(rowView);
            rowViews.add(rowView);
        }

        btnClose.setOnClickListener(v -> dialog.dismiss());

        btnSave.setOnClickListener(v -> {
            List<String> newItems = new ArrayList<>();
            for (View rowView : rowViews) {
                if (rowView.getParent() != null) {
                    EditText etItem = rowView.findViewById(R.id.etWheelItem);
                    String val = etItem.getText().toString().trim();
                    if (!val.isEmpty()) {
                        newItems.add(val);
                    }
                }
            }

            if (newItems.size() >= 2) {
                currentItems = newItems;
                if (wheelView != null) {
                    wheelView.setItems(currentItems);
                }
                updateGroupCount(tvGroupCount);
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    private void reindexRows(LinearLayout container) {
        for (int i = 0; i < container.getChildCount(); i++) {
            View row = container.getChildAt(i);
            TextView tvIndex = row.findViewById(R.id.tvIndex);
            if (tvIndex != null) {
                tvIndex.setText(String.valueOf(i + 1));
            }
        }
    }
}
