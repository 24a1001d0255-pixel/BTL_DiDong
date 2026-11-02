package com.example.btl_didong;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
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
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;
import com.example.btl_didong.data.AppDatabase;
import com.example.btl_didong.data.Place;
import com.example.btl_didong.data.UserHistory;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.slider.Slider;

import org.osmdroid.events.MapListener;
import org.osmdroid.events.ScrollEvent;
import org.osmdroid.events.ZoomEvent;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;

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
    private int currentBudgetMin = 0;
    private int currentBudgetMax = 100000;
    private float currentRadius = 2.0f;
    private String lastSelectedFood = "";

    private EditText etPriceMinBox;
    private EditText etPriceMaxBox;
    private MaterialButton btnBudget0_100;
    private MaterialButton btnBudget100_200;
    private MaterialButton btnBudget200_300;

    private Slider sliderRadius;
    private TextView tvRadiusVal;
    private TextView tvRadiusSubtitle;
    private TextView tvGroupCount;
    private MaterialButton btnLocationDistance;
    private TextView tvCurrentAddress;
    private EditText etSearchFoodBar;

    private SwipeRefreshLayout swipeRefreshHome;
    private LinearLayout layoutResultSection;
    private TextView tvSelectedFoodName;
    private TextView tvNearbySectionTitle;
    private TextView tvBadgeCount;
    private LinearLayout containerNearbyPlaces;

    // Tọa độ vị trí mặc định của người dùng (Chung Cư 96 Định Công, Hà Nội)
    private double userLat = 20.9890;
    private double userLng = 105.8320;

    private static class LocationItem {
        String name;
        String address;
        double lat;
        double lng;
        boolean isCustomTyped;

        LocationItem(String name, String address, double lat, double lng) {
            this.name = name;
            this.address = address;
            this.lat = lat;
            this.lng = lng;
            this.isCustomTyped = false;
        }

        LocationItem(String name, String address, boolean isCustomTyped) {
            this.name = name;
            this.address = address;
            this.isCustomTyped = isCustomTyped;
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        swipeRefreshHome = view.findViewById(R.id.swipeRefreshHome);
        NestedScrollView scrollView = view.findViewById(R.id.homeScrollView);
        wheelView = view.findViewById(R.id.spinningWheel);
        MaterialButton btnSpin = view.findViewById(R.id.btnSpin);
        MaterialButton btnSpinAgain = view.findViewById(R.id.btnSpinAgain);
        TextView btnEditWheel = view.findViewById(R.id.btnEditWheel);
        tvGroupCount = view.findViewById(R.id.tvGroupCount);
        btnLocationDistance = view.findViewById(R.id.btnLocationDistance);
        LinearLayout layoutLocationBar = view.findViewById(R.id.layoutLocationBar);
        tvCurrentAddress = view.findViewById(R.id.tvCurrentAddress);
        etSearchFoodBar = view.findViewById(R.id.etSearchFoodBar);

        MaterialButtonToggleGroup toggleGroupMode = view.findViewById(R.id.toggleGroupMode);
        MaterialButton btnSolo = view.findViewById(R.id.btnSolo);
        MaterialButton btnGroup = view.findViewById(R.id.btnGroup);

        etPriceMinBox = view.findViewById(R.id.etPriceMinBox);
        etPriceMaxBox = view.findViewById(R.id.etPriceMaxBox);
        btnBudget0_100 = view.findViewById(R.id.btnBudget0_100);
        btnBudget100_200 = view.findViewById(R.id.btnBudget100_200);
        btnBudget200_300 = view.findViewById(R.id.btnBudget200_300);

        sliderRadius = view.findViewById(R.id.sliderRadius);
        tvRadiusVal = view.findViewById(R.id.tvRadiusVal);
        tvRadiusSubtitle = view.findViewById(R.id.tvRadiusSubtitle);

        // Group Extra Controls
        LinearLayout layoutGroupExtraControls = view.findViewById(R.id.layoutGroupExtraControls);
        TextView tvPeopleCount = view.findViewById(R.id.tvPeopleCount);
        MaterialButton btnPeopleMinus = view.findViewById(R.id.btnPeopleMinus);
        MaterialButton btnPeoplePlus = view.findViewById(R.id.btnPeoplePlus);
        MaterialButtonToggleGroup toggleGroupType = view.findViewById(R.id.toggleGroupType);
        MaterialButton btnSingleOption = view.findViewById(R.id.btnSingleOption);
        MaterialButton btnClusterOption = view.findViewById(R.id.btnClusterOption);
        MaterialButton btnFindGroupOption = view.findViewById(R.id.btnFindGroupOption);

        layoutResultSection = view.findViewById(R.id.layoutResultSection);
        tvSelectedFoodName = view.findViewById(R.id.tvSelectedFoodName);
        tvNearbySectionTitle = view.findViewById(R.id.tvNearbySectionTitle);
        tvBadgeCount = view.findViewById(R.id.tvBadgeCount);
        containerNearbyPlaces = view.findViewById(R.id.containerNearbyPlaces);

        updateGroupCount(tvGroupCount);
        loadSavedAddress();

        // Xin quyền GPS và tự động lấy địa chỉ thực tế khi bật ứng dụng
        checkAndRequestLocationPermission();
        detectAndSetUserGPSLocation();

        if (wheelView != null) {
            wheelView.setItems(currentItems);
        }

        // Mở Dialog chọn/sửa vị trí khi bấm vào thanh vị trí
        if (layoutLocationBar != null) {
            layoutLocationBar.setOnClickListener(v -> showLocationPickerDialog());
        }

        // Tìm kiếm món ăn trực tiếp
        if (etSearchFoodBar != null) {
            etSearchFoodBar.addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                    String query = s.toString().trim();
                    if (!query.isEmpty()) {
                        lastSelectedFood = query;
                        if (layoutResultSection != null) layoutResultSection.setVisibility(View.VISIBLE);
                        if (tvSelectedFoodName != null) tvSelectedFoodName.setText(query);
                        if (tvNearbySectionTitle != null) tvNearbySectionTitle.setText("Kết quả tìm kiếm cho '" + query + "'");
                        loadAndDisplayPlaces(query, containerNearbyPlaces, tvBadgeCount);
                    }
                }
                @Override public void afterTextChanged(Editable s) {}
            });
        }

        // Cấu hình Nút Khoảng giá: 0-100k, 100k-200k, 200k-300k & Tự nhập giá
        setupBudgetRangeInputAndButtons();

        // Vuốt xuống ở trên cùng để Reload (Pull to Refresh)
        if (swipeRefreshHome != null) {
            swipeRefreshHome.setColorSchemeResources(R.color.forest_green);
            swipeRefreshHome.setOnRefreshListener(() -> {
                detectAndSetUserGPSLocation();
                applySurveyPreferences();
                if (!lastSelectedFood.isEmpty()) {
                    loadAndDisplayPlaces(lastSelectedFood, containerNearbyPlaces, tvBadgeCount);
                }
                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    if (swipeRefreshHome != null) {
                        swipeRefreshHome.setRefreshing(false);
                    }
                }, 1000);
            });
        }

        if (btnLocationDistance != null) {
            btnLocationDistance.setOnClickListener(v -> {
                Intent intent = new Intent(getActivity(), SurveyActivity.class);
                startActivity(intent);
            });
        }

        // Lọc theo Bán kính khi kéo slider (Cập nhật số ở trên ngay khi gạt)
        if (sliderRadius != null) {
            sliderRadius.addOnChangeListener((slider, value, fromUser) -> {
                currentRadius = value;
                if (tvRadiusVal != null) {
                    tvRadiusVal.setText(String.format(Locale.US, "%.1f km", currentRadius));
                }
                if (btnLocationDistance != null) {
                    btnLocationDistance.setText(String.format(Locale.US, "%.1f km", currentRadius));
                }
                if (tvRadiusSubtitle != null) {
                    tvRadiusSubtitle.setText(String.format(Locale.US, "Trong bán kính %.1f km · đã xếp theo đánh giá", currentRadius));
                }

                if (fromUser && getContext() != null) {
                    getContext().getSharedPreferences("food_picker_prefs", Context.MODE_PRIVATE)
                            .edit().putFloat("pref_radius", currentRadius).apply();

                    if (!lastSelectedFood.isEmpty() && containerNearbyPlaces != null) {
                        loadAndDisplayPlaces(lastSelectedFood, containerNearbyPlaces, tvBadgeCount);
                    }
                }
            });
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

                    applySurveyPreferences();
                } else if (checkedId == R.id.btnGroup) {
                    currentMode = "GROUP";
                    if (layoutGroupExtraControls != null) layoutGroupExtraControls.setVisibility(View.VISIBLE);

                    btnGroup.setBackgroundColor(getResources().getColor(R.color.white));
                    btnGroup.setTextColor(getResources().getColor(R.color.forest_green));
                    btnSolo.setBackgroundColor(getResources().getColor(android.R.color.transparent));
                    btnSolo.setTextColor(getResources().getColor(R.color.text_muted));

                    currentItems = new ArrayList<>(Arrays.asList("Chợ đêm", "Lẩu nướng", "Buffet", "Lẩu thái", "Bò nướng", "Hải sản", "Xiên bẩn", "Trà sữa"));
                    if (wheelView != null) {
                        wheelView.setItems(currentItems);
                    }
                    updateGroupCount(tvGroupCount);
                }
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
                recordUserHistory(lastSelectedFood, 0, "SPUN");

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

                        // Bắn Notification gợi ý sau khi quay trúng
                        if (getContext() != null) {
                            NotificationHelper.sendSuggestionNotification(getContext(), lastSelectedFood + " - Quán ngon gần bạn", lastSelectedFood, 1);
                        }

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

        applySurveyPreferences();

        // Xử lý Intent nếu ứng dụng được mở từ Notification
        handleNotificationLaunchExtra(getArguments(), containerNearbyPlaces, tvBadgeCount, scrollView);

        return view;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1001 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            detectAndSetUserGPSLocation();
        }
    }

    private void detectAndSetUserGPSLocation() {
        if (getContext() == null) return;
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            try {
                LocationManager lm = (LocationManager) getContext().getSystemService(Context.LOCATION_SERVICE);
                if (lm != null) {
                    Location loc = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    if (loc == null) {
                        loc = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                    }
                    if (loc != null) {
                        userLat = loc.getLatitude();
                        userLng = loc.getLongitude();

                        Executors.newSingleThreadExecutor().execute(() -> {
                            String detectedAddress = "Vị trí GPS của bạn";
                            try {
                                Geocoder geocoder = new Geocoder(getContext(), Locale.getDefault());
                                List<Address> addresses = geocoder.getFromLocation(userLat, userLng, 1);
                                if (addresses != null && !addresses.isEmpty()) {
                                    Address addr = addresses.get(0);
                                    String street = addr.getThoroughfare();
                                    String subLocality = addr.getSubLocality();
                                    String locality = addr.getLocality();

                                    if (street != null && !street.isEmpty()) {
                                        detectedAddress = street;
                                        if (subLocality != null && !subLocality.isEmpty()) detectedAddress += ", " + subLocality;
                                    } else if (subLocality != null && !subLocality.isEmpty()) {
                                        detectedAddress = subLocality;
                                    } else if (locality != null && !locality.isEmpty()) {
                                        detectedAddress = locality;
                                    } else if (addr.getAddressLine(0) != null) {
                                        detectedAddress = addr.getAddressLine(0);
                                    }
                                }
                            } catch (Exception e) {
                                detectedAddress = "96 Định Công, Hoàng Mai";
                            }

                            final String finalAddr = detectedAddress;
                            new Handler(Looper.getMainLooper()).post(() -> {
                                if (!isAdded() || getContext() == null) return;
                                if (tvCurrentAddress != null) {
                                    tvCurrentAddress.setText(finalAddr + " ˅");
                                }
                                SharedPreferences prefs = getContext().getSharedPreferences("food_picker_prefs", Context.MODE_PRIVATE);
                                prefs.edit()
                                        .putString("pref_user_address", finalAddr)
                                        .putFloat("pref_user_lat", (float) userLat)
                                        .putFloat("pref_user_lng", (float) userLng)
                                        .apply();

                                if (!lastSelectedFood.isEmpty() && containerNearbyPlaces != null) {
                                    loadAndDisplayPlaces(lastSelectedFood, containerNearbyPlaces, tvBadgeCount);
                                }
                            });
                        });
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void applyCustomTypedAddress(String customAddress, AlertDialog dialog) {
        if (getContext() == null || customAddress == null || customAddress.trim().isEmpty()) return;
        final String addressQuery = customAddress.trim();
        Toast.makeText(getContext(), "Đang chuyển vị trí sang: " + addressQuery, Toast.LENGTH_SHORT).show();

        Executors.newSingleThreadExecutor().execute(() -> {
            double newLat = userLat;
            double newLng = userLng;
            try {
                Geocoder geocoder = new Geocoder(getContext(), Locale.getDefault());
                List<Address> addresses = geocoder.getFromLocationName(addressQuery + ", Hà Nội", 1);
                if (addresses != null && !addresses.isEmpty()) {
                    Address a = addresses.get(0);
                    newLat = a.getLatitude();
                    newLng = a.getLongitude();
                }
            } catch (Exception ignored) {}

            final double finalLat = newLat;
            final double finalLng = newLng;

            new Handler(Looper.getMainLooper()).post(() -> {
                if (!isAdded() || getContext() == null) return;
                userLat = finalLat;
                userLng = finalLng;

                if (tvCurrentAddress != null) {
                    tvCurrentAddress.setText(addressQuery + " ˅");
                }

                SharedPreferences prefs = getContext().getSharedPreferences("food_picker_prefs", Context.MODE_PRIVATE);
                prefs.edit()
                        .putString("pref_user_address", addressQuery)
                        .putFloat("pref_user_lat", (float) userLat)
                        .putFloat("pref_user_lng", (float) userLng)
                        .apply();

                if (!lastSelectedFood.isEmpty() && containerNearbyPlaces != null) {
                    loadAndDisplayPlaces(lastSelectedFood, containerNearbyPlaces, tvBadgeCount);
                }

                if (dialog != null && dialog.isShowing()) {
                    dialog.dismiss();
                }
            });
        });
    }

    private void updateBookmarkUI(ImageView btn, boolean isSaved) {
        if (btn == null || getContext() == null) return;
        if (isSaved) {
            btn.setBackgroundResource(R.drawable.bg_bookmark_saved);
            btn.setColorFilter(getResources().getColor(R.color.white));
        } else {
            btn.setBackgroundResource(R.drawable.bg_bookmark_unsaved);
            btn.setColorFilter(getResources().getColor(R.color.forest_green));
        }
    }

    private void setupBudgetRangeInputAndButtons() {
        if (btnBudget0_100 == null || btnBudget100_200 == null || btnBudget200_300 == null) return;

        btnBudget0_100.setOnClickListener(v -> selectBudgetRange(0, 100000, btnBudget0_100, btnBudget100_200, btnBudget200_300));
        btnBudget100_200.setOnClickListener(v -> selectBudgetRange(100000, 200000, btnBudget100_200, btnBudget0_100, btnBudget200_300));
        btnBudget200_300.setOnClickListener(v -> selectBudgetRange(200000, 300000, btnBudget200_300, btnBudget0_100, btnBudget100_200));

        TextWatcher priceWatcher = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                try {
                    String minStr = etPriceMinBox.getText().toString().replaceAll("[^0-9]", "");
                    String maxStr = etPriceMaxBox.getText().toString().replaceAll("[^0-9]", "");

                    int parsedMin = minStr.isEmpty() ? 0 : Integer.parseInt(minStr);
                    int parsedMax = maxStr.isEmpty() ? 300000 : Integer.parseInt(maxStr);

                    if (parsedMin < 1000 && parsedMin > 0) parsedMin *= 1000;
                    if (parsedMax < 1000 && parsedMax > 0) parsedMax *= 1000;

                    currentBudgetMin = parsedMin;
                    currentBudgetMax = Math.max(parsedMin, parsedMax);

                    if (!lastSelectedFood.isEmpty() && containerNearbyPlaces != null) {
                        loadAndDisplayPlaces(lastSelectedFood, containerNearbyPlaces, tvBadgeCount);
                    }
                } catch (Exception ignored) {}
            }
            @Override public void afterTextChanged(Editable s) {}
        };

        if (etPriceMinBox != null) etPriceMinBox.addTextChangedListener(priceWatcher);
        if (etPriceMaxBox != null) etPriceMaxBox.addTextChangedListener(priceWatcher);
    }

    private void selectBudgetRange(int min, int max, MaterialButton activeBtn, MaterialButton un1, MaterialButton un2) {
        currentBudgetMin = min;
        currentBudgetMax = max;

        if (etPriceMinBox != null) etPriceMinBox.setText((min / 1000) + "k");
        if (etPriceMaxBox != null) etPriceMaxBox.setText((max / 1000) + "k");

        // Active styling
        activeBtn.setBackgroundColor(getResources().getColor(R.color.forest_green_light));
        activeBtn.setTextColor(getResources().getColor(R.color.forest_green));
        activeBtn.setStrokeColorResource(R.color.forest_green);

        // Inactive styling
        un1.setBackgroundColor(getResources().getColor(R.color.white));
        un1.setTextColor(getResources().getColor(R.color.text_muted));
        un1.setStrokeColorResource(R.color.stroke_color);

        un2.setBackgroundColor(getResources().getColor(R.color.white));
        un2.setTextColor(getResources().getColor(R.color.text_muted));
        un2.setStrokeColorResource(R.color.stroke_color);

        if (getContext() != null) {
            getContext().getSharedPreferences("food_picker_prefs", Context.MODE_PRIVATE)
                    .edit().putInt("pref_budget", currentBudgetMax).apply();
        }

        if (!lastSelectedFood.isEmpty() && containerNearbyPlaces != null) {
            loadAndDisplayPlaces(lastSelectedFood, containerNearbyPlaces, tvBadgeCount);
        }
    }

    private void loadSavedAddress() {
        if (getContext() == null) return;
        SharedPreferences prefs = getContext().getSharedPreferences("food_picker_prefs", Context.MODE_PRIVATE);
        String savedAddr = prefs.getString("pref_user_address", "Chung Cư 96 Định Công");
        if (tvCurrentAddress != null) {
            tvCurrentAddress.setText(savedAddr.endsWith(" ˅") ? savedAddr : savedAddr + " ˅");
        }
        userLat = prefs.getFloat("pref_user_lat", 20.9890f);
        userLng = prefs.getFloat("pref_user_lng", 105.8320f);
    }

    private void showLocationPickerDialog() {
        if (getContext() == null) return;

        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_location_picker, null, false);
        AlertDialog dialog = new AlertDialog.Builder(getContext(), android.R.style.Theme_DeviceDefault_Light_Dialog_NoActionBar)
                .setView(dialogView)
                .create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        ImageView btnClose = dialogView.findViewById(R.id.btnCloseLocDialog);
        EditText etSearchLoc = dialogView.findViewById(R.id.etSearchLocation);
        LinearLayout containerLoc = dialogView.findViewById(R.id.containerLocations);

        if (btnClose != null) btnClose.setOnClickListener(v -> dialog.dismiss());

        List<LocationItem> locationItems = new ArrayList<>();
        locationItems.add(new LocationItem("Chung Cư 96 Định Công", "0.06km • 96 Định Công, P.Phương Liệt, TP.Hà Nội", 20.9890, 105.8320));
        locationItems.add(new LocationItem("Cổng Phụ (Gần nhất)", "Chung Cư 96 Định Công, Hoàng Mai", 20.9892, 105.8322));
        locationItems.add(new LocationItem("Cổng Chính", "Chung Cư 96 Định Công, Hoàng Mai", 20.9888, 105.8318));
        locationItems.add(new LocationItem("96A Định Công", "0.06km • 96A Định Công, P.Phương Liệt, TP.Hà Nội", 20.9895, 105.8325));
        locationItems.add(new LocationItem("13-BT01 Imperial Plaza", "0.06km • 360 Giải Phóng, P.Phương Liệt, TP.Hà Nội", 20.9870, 105.8390));
        locationItems.add(new LocationItem("19ngo 122, Định Công", "122 Định Công, Hoàng Mai, TP.Hà Nội", 20.9880, 105.8310));
        locationItems.add(new LocationItem("Tòa Nhà A6 - Khu Sinh Viên Pháp Vân", "3km • Phường Yên Sở, TP.Hà Nội", 20.9750, 105.8420));
        locationItems.add(new LocationItem("Ô 19 Lô 3 Đền Lừ 1", "2.41km • P.Tương Mai, TP.Hà Nội", 20.9850, 105.8540));
        locationItems.add(new LocationItem("Bến Xe Giáp Bát", "0.53km • Giải Phóng, P.Hoàng Mai, TP.Hà Nội", 20.9810, 105.8420));

        populateLocationList(containerLoc, locationItems, dialog);

        if (etSearchLoc != null) {
            etSearchLoc.setOnEditorActionListener((v, actionId, event) -> {
                if (actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_ACTION_DONE ||
                        (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                    String query = etSearchLoc.getText().toString().trim();
                    if (!query.isEmpty()) {
                        applyCustomTypedAddress(query, dialog);
                        return true;
                    }
                }
                return false;
            });

            etSearchLoc.addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                    String query = s.toString().toLowerCase().trim();
                    List<LocationItem> filtered = new ArrayList<>();
                    if (!query.isEmpty()) {
                        filtered.add(new LocationItem("📍 Dùng địa chỉ gõ: " + s.toString().trim(), "Nhấn để chuyển định vị sang địa chỉ này", true));
                    }
                    for (LocationItem item : locationItems) {
                        if (item.name.toLowerCase().contains(query) || item.address.toLowerCase().contains(query)) {
                            filtered.add(item);
                        }
                    }
                    populateLocationList(containerLoc, filtered, dialog);
                }
                @Override public void afterTextChanged(Editable s) {}
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

    private void populateLocationList(LinearLayout container, List<LocationItem> items, AlertDialog dialog) {
        if (container == null || getContext() == null) return;
        container.removeAllViews();

        for (LocationItem loc : items) {
            LinearLayout row = new LinearLayout(getContext());
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(android.view.Gravity.CENTER_VERTICAL);
            row.setPadding(12, 16, 12, 16);
            row.setClickable(true);
            row.setFocusable(true);
            row.setBackgroundResource(android.R.drawable.list_selector_background);

            ImageView imgPin = new ImageView(getContext());
            imgPin.setImageResource(R.drawable.ic_map);
            imgPin.setColorFilter(getResources().getColor(R.color.forest_green));
            imgPin.setPadding(8, 8, 8, 8);
            LinearLayout.LayoutParams lpImg = new LinearLayout.LayoutParams(60, 60);
            imgPin.setLayoutParams(lpImg);

            LinearLayout col = new LinearLayout(getContext());
            col.setOrientation(LinearLayout.VERTICAL);
            LinearLayout.LayoutParams lpCol = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
            lpCol.setMargins(16, 0, 0, 0);
            col.setLayoutParams(lpCol);

            TextView tvName = new TextView(getContext());
            tvName.setText(loc.name);
            tvName.setTextSize(14f);
            tvName.setTypeface(null, android.graphics.Typeface.BOLD);
            tvName.setTextColor(getResources().getColor(R.color.text_dark));

            TextView tvAddr = new TextView(getContext());
            tvAddr.setText(loc.address);
            tvAddr.setTextSize(12f);
            tvAddr.setTextColor(getResources().getColor(R.color.text_muted));

            col.addView(tvName);
            col.addView(tvAddr);

            row.addView(imgPin);
            row.addView(col);

            row.setOnClickListener(v -> {
                if (loc.isCustomTyped) {
                    String rawTypedName = loc.name.replace("📍 Dùng địa chỉ gõ: ", "").trim();
                    applyCustomTypedAddress(rawTypedName, dialog);
                } else {
                    userLat = loc.lat;
                    userLng = loc.lng;

                    if (tvCurrentAddress != null) {
                        tvCurrentAddress.setText(loc.name + " ˅");
                    }

                    if (getContext() != null) {
                        SharedPreferences prefs = getContext().getSharedPreferences("food_picker_prefs", Context.MODE_PRIVATE);
                        prefs.edit()
                                .putString("pref_user_address", loc.name)
                                .putFloat("pref_user_lat", (float) userLat)
                                .putFloat("pref_user_lng", (float) userLng)
                                .apply();
                    }

                    if (!lastSelectedFood.isEmpty() && containerNearbyPlaces != null) {
                        loadAndDisplayPlaces(lastSelectedFood, containerNearbyPlaces, tvBadgeCount);
                    }

                    Toast.makeText(getContext(), "Đã đổi vị trí đến: " + loc.name, Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                }
            });

            container.addView(row);
        }
    }

    private void handleNotificationLaunchExtra(Bundle args, LinearLayout container, TextView tvBadge, NestedScrollView scrollView) {
        if (args != null && args.containsKey("OPEN_PLACE_NAME")) {
            String openName = args.getString("OPEN_PLACE_NAME");
            String openCat = args.getString("OPEN_PLACE_CATEGORY", "Phở");
            if (openName != null && !openName.isEmpty()) {
                lastSelectedFood = openCat != null ? openCat : "Phở";
                if (layoutResultSection != null) layoutResultSection.setVisibility(View.VISIBLE);
                if (tvSelectedFoodName != null) tvSelectedFoodName.setText(openName);
                if (tvNearbySectionTitle != null) tvNearbySectionTitle.setText("Địa điểm gợi ý cho bạn");

                loadAndDisplayPlaces(lastSelectedFood, container, tvBadge);

                if (scrollView != null && layoutResultSection != null) {
                    scrollView.post(() -> scrollView.smoothScrollTo(0, layoutResultSection.getTop()));
                }
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        loadSavedAddress();
        applySurveyPreferences();
    }

    private void recordUserHistory(String categoryName, int restaurantId, String actionType) {
        if (categoryName == null || categoryName.isEmpty()) return;
        Context ctx = getContext() != null ? getContext().getApplicationContext() : null;
        if (ctx == null) return;

        Executors.newSingleThreadExecutor().execute(() -> {
            AppDatabase db = AppDatabase.get(ctx);
            UserHistory history = new UserHistory(categoryName, restaurantId, actionType, System.currentTimeMillis());
            db.historyDao().insert(history);
        });
    }

    private void applySurveyPreferences() {
        if (getContext() == null) return;
        SharedPreferences prefs = getContext().getSharedPreferences("food_picker_prefs", Context.MODE_PRIVATE);

        // 1. Budget
        currentBudgetMax = prefs.getInt("pref_budget", 100000);

        // 2. Radius
        currentRadius = prefs.getFloat("pref_radius", 2.0f);
        if (sliderRadius != null) {
            sliderRadius.setValue(Math.max(0.5f, Math.min(5.0f, currentRadius)));
        }
        if (tvRadiusVal != null) {
            tvRadiusVal.setText(String.format(Locale.US, "%.1f km", currentRadius));
        }
        if (btnLocationDistance != null) {
            btnLocationDistance.setText(String.format(Locale.US, "%.1f km", currentRadius));
        }
        if (tvRadiusSubtitle != null) {
            tvRadiusSubtitle.setText(String.format(Locale.US, "Trong bán kính %.1f km · đã xếp theo đánh giá", currentRadius));
        }

        // 3. Smart Personalized Wheel Building (7-day history + 24h anti-repetition)
        Context ctx = getContext() != null ? getContext().getApplicationContext() : null;
        if (ctx == null) return;

        Executors.newSingleThreadExecutor().execute(() -> {
            AppDatabase db = AppDatabase.get(ctx);

            long now = System.currentTimeMillis();
            long sevenDaysAgo = now - (7L * 24 * 3600 * 1000);
            long twentyFourHoursAgo = now - (24L * 3600 * 1000);

            List<String> topCategories = db.historyDao().getTopCategoriesSince(sevenDaysAgo);
            List<String> recent24hCategories = db.historyDao().getRecentCategoriesSince(twentyFourHoursAgo);

            boolean avoidVegetarian = prefs.getBoolean("pref_avoid_vegetarian", false);
            String avoidItems = prefs.getString("pref_avoid_items", "");

            List<String> baseCategories;
            if ("SOLO".equals(currentMode)) {
                if (avoidVegetarian || avoidItems.contains("Đồ chay")) {
                    baseCategories = new ArrayList<>(Arrays.asList("Cơm chay", "Phở chay", "Bún chay", "Lẩu nấm", "Đồ uống", "Chè & Ăn vặt", "Bánh mì chay", "Gỏi cuốn"));
                } else {
                    baseCategories = new ArrayList<>(Arrays.asList("Phở", "Cơm", "Bánh mì", "Đồ nướng", "Ăn vặt", "Đồ uống", "Bún", "Lẩu"));
                }
            } else {
                baseCategories = new ArrayList<>(Arrays.asList("Chợ đêm", "Lẩu nướng", "Buffet", "Lẩu thái", "Bò nướng", "Hải sản", "Xiên bẩn", "Trà sữa"));
            }

            List<String> personalizedList = new ArrayList<>();

            for (String cat : topCategories) {
                if (baseCategories.contains(cat) && !recent24hCategories.contains(cat) && !personalizedList.contains(cat)) {
                    personalizedList.add(cat);
                }
            }

            for (String cat : baseCategories) {
                if (!recent24hCategories.contains(cat) && !personalizedList.contains(cat)) {
                    personalizedList.add(cat);
                }
            }

            for (String cat : baseCategories) {
                if (!personalizedList.contains(cat)) {
                    personalizedList.add(cat);
                }
            }

            if (personalizedList.size() > 8) {
                personalizedList = new ArrayList<>(personalizedList.subList(0, 8));
            }

            final List<String> finalItems = personalizedList;

            new Handler(Looper.getMainLooper()).post(() -> {
                if (!isAdded() || getContext() == null) return;
                currentItems = finalItems;
                if (wheelView != null) {
                    wheelView.setItems(currentItems);
                }
                if (tvGroupCount != null) {
                    tvGroupCount.setText(currentItems.size() + " nhóm món");
                }
            });
        });
    }

    // Công thức Haversine tính khoảng cách thực tế giữa 2 tọa độ GPS (km)
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

        Context ctx = getContext() != null ? getContext().getApplicationContext() : null;
        if (ctx == null) return;

        Executors.newSingleThreadExecutor().execute(() -> {
            AppDatabase db = AppDatabase.get(ctx);
            List<Place> rawPlaces;

            if ("SINGLE".equals(selectedGroupOption)) {
                rawPlaces = db.placeDao().findSingleGroupPlaces(currentBudgetMax * groupPeopleCount);
            } else {
                rawPlaces = db.placeDao().findClusterGroupPlaces(currentBudgetMax * groupPeopleCount);
            }

            if (rawPlaces.isEmpty()) {
                rawPlaces = db.placeDao().findSingleGroupPlaces(500000);
            }

            // Lọc theo khoảng cách bán kính (currentRadius) và Giá từ vị trí đã chọn
            List<Place> filtered = new ArrayList<>();
            for (Place p : rawPlaces) {
                double dist = calculateDistanceKm(userLat, userLng, p.lat, p.lng);
                if (dist <= currentRadius || filtered.size() < 2) {
                    filtered.add(p);
                }
            }

            final List<Place> finalPlaces = filtered;
            new Handler(Looper.getMainLooper()).post(() -> {
                if (!isAdded() || getContext() == null) return;
                container.removeAllViews();
                if (tvBadge != null) {
                    tvBadge.setText(finalPlaces.size() + " địa điểm");
                }

                for (Place p : finalPlaces) {
                    double dist = calculateDistanceKm(userLat, userLng, p.lat, p.lng);
                    int minutes = (int) (dist * 8 + 5);

                    View cardView = LayoutInflater.from(getContext()).inflate(R.layout.item_restaurant_card, container, false);

                    TextView tvName = cardView.findViewById(R.id.tvRestaurantName);
                    TextView tvRating = cardView.findViewById(R.id.tvRestaurantRating);
                    TextView tvSub = cardView.findViewById(R.id.tvRestaurantSub);
                    TextView tag1 = cardView.findViewById(R.id.tag1);
                    TextView tag2 = cardView.findViewById(R.id.tag2);
                    TextView tagMatch = cardView.findViewById(R.id.tagMatch);
                    ImageView imgRestaurant = cardView.findViewById(R.id.imgRestaurant);
                    ImageView btnSaveBookmark = cardView.findViewById(R.id.btnSaveBookmark);

                    if (tvName != null) tvName.setText(p.name);
                    if (tvRating != null) tvRating.setText("4.8");
                    if (tvSub != null) {
                        if ("CLUSTER".equals(p.placeType)) {
                            tvSub.setText("Gồm có: " + (p.categoriesList != null ? p.categoriesList : "Trà sữa, Bánh mì, Ăn vặt"));
                        } else {
                            tvSub.setText(String.format(Locale.US, "%.1f km • %d phút • Sức chứa %d+ người • %dk–%dk", dist, minutes, groupPeopleCount + 2, p.priceMin / 1000, p.priceMax / 1000));
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

                    if (btnSaveBookmark != null && getContext() != null) {
                        SharedPreferences prefs = getContext().getSharedPreferences("food_picker_prefs", Context.MODE_PRIVATE);
                        String savedStr = prefs.getString("saved_restaurants", "");
                        boolean isSaved = savedStr.contains(p.name);
                        updateBookmarkUI(btnSaveBookmark, isSaved);

                        btnSaveBookmark.setOnClickListener(v -> {
                            SharedPreferences sp = v.getContext().getSharedPreferences("food_picker_prefs", Context.MODE_PRIVATE);
                            String current = sp.getString("saved_restaurants", "");
                            boolean nowSaved;
                            if (!current.contains(p.name)) {
                                current = p.name + ";" + current;
                                nowSaved = true;
                                Toast.makeText(v.getContext(), "⭐ Đã lưu quán: " + p.name + " vào Bộ sưu tập!", Toast.LENGTH_SHORT).show();
                            } else {
                                current = current.replace(p.name + ";", "");
                                nowSaved = false;
                                Toast.makeText(v.getContext(), "Đã bỏ lưu: " + p.name, Toast.LENGTH_SHORT).show();
                            }
                            sp.edit().putString("saved_restaurants", current).apply();
                            updateBookmarkUI(btnSaveBookmark, nowSaved);
                        });
                    }

                    cardView.setOnClickListener(v -> showPlaceDetailDialog(p, p.category != null ? p.category : p.name));

                    container.addView(cardView);
                }
            });
        });
    }

    private void loadAndDisplayPlaces(String foodName, LinearLayout container, TextView tvBadge) {
        if (container == null) return;

        Context ctx = getContext() != null ? getContext().getApplicationContext() : null;
        if (ctx == null) return;

        Executors.newSingleThreadExecutor().execute(() -> {
            AppDatabase db = AppDatabase.get(ctx);
            List<Place> rawPlaces = db.placeDao().findPlaces(currentMode, foodName, currentBudgetMax);
            if (rawPlaces.isEmpty()) {
                rawPlaces = db.placeDao().findPlaces(currentMode, foodName, 300000);
            }
            if (rawPlaces.isEmpty()) {
                rawPlaces = db.placeDao().findPlaces(currentMode, "Phở", 300000);
            }

            // Lọc thực tế theo Khoảng Giá (currentBudgetMin -> currentBudgetMax) & Bán kính di chuyển (currentRadius) từ vị trí đã chọn
            List<Place> filteredPlaces = new ArrayList<>();
            for (Place p : rawPlaces) {
                double dist = calculateDistanceKm(userLat, userLng, p.lat, p.lng);
                if (p.priceMax >= currentBudgetMin && p.priceMin <= currentBudgetMax && dist <= currentRadius) {
                    filteredPlaces.add(p);
                }
            }

            if (filteredPlaces.isEmpty()) {
                for (Place p : rawPlaces) {
                    if (p.priceMin <= currentBudgetMax) {
                        filteredPlaces.add(p);
                    }
                }
            }
            if (filteredPlaces.isEmpty()) {
                filteredPlaces = rawPlaces;
            }

            final List<Place> finalPlaces = filteredPlaces;
            new Handler(Looper.getMainLooper()).post(() -> {
                if (!isAdded() || getContext() == null) return;
                container.removeAllViews();
                if (tvBadge != null) {
                    tvBadge.setText(finalPlaces.size() + " quán");
                }

                for (Place p : finalPlaces) {
                    double dist = calculateDistanceKm(userLat, userLng, p.lat, p.lng);
                    int minutes = (int) (dist * 8 + 5);

                    View cardView = LayoutInflater.from(getContext()).inflate(R.layout.item_restaurant_card, container, false);

                    TextView tvName = cardView.findViewById(R.id.tvRestaurantName);
                    TextView tvRating = cardView.findViewById(R.id.tvRestaurantRating);
                    TextView tvSub = cardView.findViewById(R.id.tvRestaurantSub);
                    TextView tag1 = cardView.findViewById(R.id.tag1);
                    TextView tag2 = cardView.findViewById(R.id.tag2);
                    TextView tagMatch = cardView.findViewById(R.id.tagMatch);
                    ImageView imgRestaurant = cardView.findViewById(R.id.imgRestaurant);
                    ImageView btnSaveBookmark = cardView.findViewById(R.id.btnSaveBookmark);

                    if (tvName != null) tvName.setText(p.name);
                    if (tvRating != null) tvRating.setText("4.8");
                    if (tvSub != null) tvSub.setText(String.format(Locale.US, "%.1f km • %d phút • %dk–%dk", dist, minutes, p.priceMin / 1000, p.priceMax / 1000));
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

                    if (btnSaveBookmark != null && getContext() != null) {
                        SharedPreferences prefs = getContext().getSharedPreferences("food_picker_prefs", Context.MODE_PRIVATE);
                        String savedStr = prefs.getString("saved_restaurants", "");
                        boolean isSaved = savedStr.contains(p.name);
                        updateBookmarkUI(btnSaveBookmark, isSaved);

                        btnSaveBookmark.setOnClickListener(v -> {
                            SharedPreferences sp = v.getContext().getSharedPreferences("food_picker_prefs", Context.MODE_PRIVATE);
                            String current = sp.getString("saved_restaurants", "");
                            boolean nowSaved;
                            if (!current.contains(p.name)) {
                                current = p.name + ";" + current;
                                nowSaved = true;
                                Toast.makeText(v.getContext(), "⭐ Đã lưu quán: " + p.name + " vào Bộ sưu tập!", Toast.LENGTH_SHORT).show();
                            } else {
                                current = current.replace(p.name + ";", "");
                                nowSaved = false;
                                Toast.makeText(v.getContext(), "Đã bỏ lưu: " + p.name, Toast.LENGTH_SHORT).show();
                            }
                            sp.edit().putString("saved_restaurants", current).apply();
                            updateBookmarkUI(btnSaveBookmark, nowSaved);
                        });
                    }

                    cardView.setOnClickListener(v -> showPlaceDetailDialog(p, foodName));

                    container.addView(cardView);
                }
            });
        });
    }

    private void showPlaceDetailDialog(Place place, String foodName) {
        recordUserHistory(foodName != null ? foodName : (place != null ? place.category : ""), place != null ? place.id : 0, "VIEWED");

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

        double dist = place != null ? calculateDistanceKm(userLat, userLng, place.lat, place.lng) : 1.2;
        int minutes = (int) (dist * 8 + 5);

        if (tvPlaceName != null && place != null) tvPlaceName.setText(place.name);
        if (tvDetailSub != null && place != null) tvDetailSub.setText(String.format(Locale.US, "%.1f km • %d phút • %dk–%dk", dist, minutes, place.priceMin / 1000, place.priceMax / 1000));
        if (tvDishName != null) tvDishName.setText(foodName);

        if (btnMaps != null) {
            btnMaps.setOnClickListener(v -> {
                String uriStr = "https://www.google.com/maps/search/?api=1&query=" + Uri.encode((place != null ? place.name + " " + place.address : foodName));
                Intent mapsIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(uriStr));
                startActivity(mapsIntent);
            });
        }

        if (btnShopee != null) {
            btnShopee.setOnClickListener(v -> {
                try {
                    Intent shopeeIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("shopeefood://food?keyword=" + Uri.encode(foodName)));
                    startActivity(shopeeIntent);
                } catch (Exception e) {
                    Intent shopeeIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://shopeefood.vn/ho-chi-minh/tim-kiem?keyword=" + Uri.encode(foodName)));
                    startActivity(shopeeIntent);
                }
            });
        }

        if (btnGrab != null) {
            btnGrab.setOnClickListener(v -> {
                try {
                    Intent grabIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("grab://open?screenType=FOOD&keyword=" + Uri.encode(foodName)));
                    startActivity(grabIntent);
                } catch (Exception e) {
                    Intent grabIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://food.grab.com/vn/vi/restaurants?search=" + Uri.encode(foodName)));
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
