package com.example.btl_didong;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

public class ProfileFragment extends Fragment {

    private LinearLayout layoutGuestBanner;
    private LinearLayout layoutUserCard;
    private TextView tvUserName;
    private TextView tvUserEmail;
    private TextView tvLocationStatus;
    private LinearLayout containerHistoryItems;
    private SharedPreferences prefs;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        if (getContext() != null) {
            prefs = getContext().getSharedPreferences("food_picker_prefs", Context.MODE_PRIVATE);
        }

        layoutGuestBanner = view.findViewById(R.id.layoutGuestBanner);
        layoutUserCard = view.findViewById(R.id.layoutUserCard);
        tvUserName = view.findViewById(R.id.tvUserName);
        tvUserEmail = view.findViewById(R.id.tvUserEmail);
        tvLocationStatus = view.findViewById(R.id.tvLocationStatus);
        containerHistoryItems = view.findViewById(R.id.containerHistoryItems);

        MaterialButton btnOpenAuthDialog = view.findViewById(R.id.btnOpenAuthDialog);
        MaterialButton btnLogout = view.findViewById(R.id.btnLogout);
        TextView btnClearHistory = view.findViewById(R.id.btnClearHistory);
        ChipGroup cgTastePreferences = view.findViewById(R.id.cgTastePreferences);

        RelativeLayout itemLocation = view.findViewById(R.id.itemLocationSetting);
        RelativeLayout itemAppSetting = view.findViewById(R.id.itemAppSettings);
        RelativeLayout itemPrivacy = view.findViewById(R.id.itemPrivacy);

        updateUserUI();
        updateLocationStatusUI();
        loadSpinHistory();
        setupTastePreferences(cgTastePreferences);

        if (btnOpenAuthDialog != null) {
            btnOpenAuthDialog.setOnClickListener(v -> showAuthDialog());
        }

        if (btnLogout != null) {
            btnLogout.setOnClickListener(v -> {
                if (prefs != null) {
                    prefs.edit().putBoolean("is_logged_in", false).remove("user_email").apply();
                }
                Toast.makeText(getContext(), "Đã đăng xuất tài khoản!", Toast.LENGTH_SHORT).show();
                updateUserUI();
            });
        }

        if (btnClearHistory != null) {
            btnClearHistory.setOnClickListener(v -> {
                if (prefs != null) {
                    prefs.edit().remove("spin_history").apply();
                }
                if (containerHistoryItems != null) {
                    containerHistoryItems.removeAllViews();
                }
                Toast.makeText(getContext(), "Đã xóa lịch sử chọn món!", Toast.LENGTH_SHORT).show();
            });
        }

        if (itemLocation != null) {
            itemLocation.setOnClickListener(v -> showLocationDialog());
        }

        if (itemAppSetting != null) {
            itemAppSetting.setOnClickListener(v -> showAppSettingsDialog());
        }

        if (itemPrivacy != null) {
            itemPrivacy.setOnClickListener(v -> showPrivacyPolicyDialog());
        }

        return view;
    }

    private void updateUserUI() {
        if (prefs == null) return;
        boolean isLoggedIn = prefs.getBoolean("is_logged_in", false);
        String userEmail = prefs.getString("user_email", "user@gmail.com");

        if (isLoggedIn) {
            if (layoutGuestBanner != null) layoutGuestBanner.setVisibility(View.GONE);
            if (layoutUserCard != null) layoutUserCard.setVisibility(View.VISIBLE);
            if (tvUserName != null) {
                String name = userEmail.contains("@") ? userEmail.split("@")[0] : userEmail;
                tvUserName.setText("Người dùng: " + name);
            }
            if (tvUserEmail != null) tvUserEmail.setText(userEmail);
        } else {
            if (layoutGuestBanner != null) layoutGuestBanner.setVisibility(View.VISIBLE);
            if (layoutUserCard != null) layoutUserCard.setVisibility(View.GONE);
        }
    }

    private void updateLocationStatusUI() {
        if (prefs == null || tvLocationStatus == null) return;
        boolean isGpsOn = prefs.getBoolean("pref_gps_enabled", true);
        tvLocationStatus.setText(isGpsOn ? "Đang bật" : "Đã tắt");
        tvLocationStatus.setTextColor(getResources().getColor(isGpsOn ? R.color.forest_green : R.color.brown_accent));
    }

    private void showLocationDialog() {
        if (getContext() == null) return;
        boolean isGpsOn = prefs != null && prefs.getBoolean("pref_gps_enabled", true);

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("📍 Cài đặt Vị trí GPS");
        builder.setMessage("Định vị vị trí GPS giúp ứng dụng tự động quét các quán ăn gần nhất trong bán kính 1.5km - 5km xung quanh bạn.\n\nTrạng thái hiện tại: " + (isGpsOn ? "ĐANG BẬT" : "ĐÃ TẮT"));

        builder.setPositiveButton(isGpsOn ? "Tắt định vị" : "Bật định vị", (dialog, which) -> {
            if (prefs != null) {
                prefs.edit().putBoolean("pref_gps_enabled", !isGpsOn).apply();
            }
            updateLocationStatusUI();
            Toast.makeText(getContext(), "Đã cập nhật trạng thái vị trí!", Toast.LENGTH_SHORT).show();
        });

        builder.setNegativeButton("Đóng", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    private void showAppSettingsDialog() {
        if (getContext() == null) return;

        LinearLayout layout = new LinearLayout(getContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(40, 30, 40, 30);

        CheckBox cbNotif = new CheckBox(getContext());
        cbNotif.setText("Nhận thông báo gợi ý món ăn mới");
        cbNotif.setChecked(prefs == null || prefs.getBoolean("pref_notif", true));

        TextView tvLangTitle = new TextView(getContext());
        tvLangTitle.setText("\nNgôn ngữ hiển thị:");
        tvLangTitle.setTextSize(14f);
        tvLangTitle.setTypeface(null, android.graphics.Typeface.BOLD);

        RadioGroup rgLang = new RadioGroup(getContext());
        RadioButton rbVi = new RadioButton(getContext());
        rbVi.setText("Tiếng Việt (Việt Nam)");
        rbVi.setChecked(true);

        RadioButton rbEn = new RadioButton(getContext());
        rbEn.setText("English (Hoa Kỳ)");

        rgLang.addView(rbVi);
        rgLang.addView(rbEn);

        layout.addView(cbNotif);
        layout.addView(tvLangTitle);
        layout.addView(rgLang);

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("⚙️ Cài đặt ứng dụng");
        builder.setView(layout);
        builder.setPositiveButton("Lưu cài đặt", (dialog, which) -> {
            if (prefs != null) {
                prefs.edit().putBoolean("pref_notif", cbNotif.isChecked()).apply();
            }
            Toast.makeText(getContext(), "Đã lưu cấu hình ứng dụng!", Toast.LENGTH_SHORT).show();
        });
        builder.setNegativeButton("Hủy", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    private void showPrivacyPolicyDialog() {
        if (getContext() == null) return;

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("🔒 Quyền riêng tư & Bảo mật");
        builder.setMessage("Ứng dụng FoodPicker cam kết bảo vệ dữ liệu cá nhân của bạn:\n\n" +
                "1. Vị trí GPS của bạn chỉ được sử dụng trong bộ nhớ tạm để tính toán khoảng cách tới các quán ăn gần nhất.\n" +
                "2. Lịch sử chọn món và bộ sưu tập chỉ được lưu trữ riêng tư trên thiết bị của bạn.\n" +
                "3. Chúng tôi không chia sẻ thông tin cá nhân cho bên thứ ba.\n\n" +
                "Bạn có toàn quyền xóa lịch sử chọn món và dữ liệu tài khoản bất cứ lúc nào.");

        builder.setPositiveButton("Đã hiểu", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    private void showAuthDialog() {
        if (getContext() == null) return;

        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_auth, null);
        AlertDialog dialog = new AlertDialog.Builder(getContext(), android.R.style.Theme_DeviceDefault_Light_Dialog_NoActionBar)
                .setView(dialogView)
                .create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        TextView tvTitle = dialogView.findViewById(R.id.tvAuthTitle);
        EditText etEmail = dialogView.findViewById(R.id.etAuthEmail);
        EditText etPassword = dialogView.findViewById(R.id.etAuthPassword);
        MaterialButton btnAuth = dialogView.findViewById(R.id.btnDoAuth);
        TextView tvToggle = dialogView.findViewById(R.id.tvToggleAuthMode);
        ImageView btnClose = dialogView.findViewById(R.id.btnCloseAuth);

        final boolean[] isLoginMode = {true};

        btnClose.setOnClickListener(v -> dialog.dismiss());

        tvToggle.setOnClickListener(v -> {
            isLoginMode[0] = !isLoginMode[0];
            if (isLoginMode[0]) {
                tvTitle.setText("Đăng nhập tài khoản");
                btnAuth.setText("Đăng nhập");
                tvToggle.setText("Chưa có tài khoản? Đăng ký ngay");
            } else {
                tvTitle.setText("Đăng ký tài khoản mới");
                btnAuth.setText("Đăng ký");
                tvToggle.setText("Đã có tài khoản? Đăng nhập");
            }
        });

        btnAuth.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String pass = etPassword.getText().toString().trim();

            if (email.isEmpty() || pass.isEmpty()) {
                Toast.makeText(getContext(), "Vui lòng nhập đầy đủ Email và Mật khẩu!", Toast.LENGTH_SHORT).show();
                return;
            }

            if (prefs != null) {
                prefs.edit()
                        .putBoolean("is_logged_in", true)
                        .putString("user_email", email)
                        .apply();
            }

            Toast.makeText(getContext(), (isLoginMode[0] ? "Đăng nhập" : "Đăng ký") + " thành công!", Toast.LENGTH_SHORT).show();
            updateUserUI();
            dialog.dismiss();
        });

        dialog.show();
    }

    private void loadSpinHistory() {
        if (containerHistoryItems == null || prefs == null) return;
        containerHistoryItems.removeAllViews();

        String history = prefs.getString("spin_history", "");
        if (history.isEmpty()) {
            TextView emptyTv = new TextView(getContext());
            emptyTv.setText("Chưa có lịch sử quay món");
            emptyTv.setTextSize(13f);
            emptyTv.setTextColor(getResources().getColor(R.color.text_muted));
            emptyTv.setPadding(0, 10, 0, 10);
            containerHistoryItems.addView(emptyTv);
            return;
        }

        String[] entries = history.split(";");
        for (int i = 0; i < Math.min(5, entries.length); i++) {
            String entry = entries[i];
            String[] parts = entry.split(" - ");
            String foodName = parts.length > 0 ? parts[0] : entry;
            String timeStr = parts.length > 1 ? parts[1] : "Gần đây";

            LinearLayout row = new LinearLayout(getContext());
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(android.view.Gravity.CENTER_VERTICAL);
            row.setPadding(0, 12, 0, 12);

            ImageView imgIcon = new ImageView(getContext());
            imgIcon.setImageResource(R.drawable.ic_map);
            imgIcon.setColorFilter(getResources().getColor(R.color.forest_green));
            imgIcon.setBackgroundColor(0xFFE8F0EE);
            imgIcon.setPadding(12, 12, 12, 12);

            LinearLayout.LayoutParams imgLp = new LinearLayout.LayoutParams(72, 72);
            imgIcon.setLayoutParams(imgLp);

            LinearLayout textCol = new LinearLayout(getContext());
            textCol.setOrientation(LinearLayout.VERTICAL);
            LinearLayout.LayoutParams textLp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
            textLp.setMargins(24, 0, 0, 0);
            textCol.setLayoutParams(textLp);

            TextView tvFood = new TextView(getContext());
            tvFood.setText(foodName);
            tvFood.setTextSize(15f);
            tvFood.setTypeface(null, android.graphics.Typeface.BOLD);
            tvFood.setTextColor(getResources().getColor(R.color.text_dark));

            TextView tvSub = new TextView(getContext());
            tvSub.setText("Quán gần bạn");
            tvSub.setTextSize(12f);
            tvSub.setTextColor(getResources().getColor(R.color.text_muted));

            textCol.addView(tvFood);
            textCol.addView(tvSub);

            TextView tvTime = new TextView(getContext());
            tvTime.setText(timeStr);
            tvTime.setTextSize(12f);
            tvTime.setTextColor(getResources().getColor(R.color.text_muted));

            row.addView(imgIcon);
            row.addView(textCol);
            row.addView(tvTime);

            containerHistoryItems.addView(row);
        }
    }

    private void setupTastePreferences(ChipGroup cg) {
        if (cg == null || prefs == null) return;

        int childCount = cg.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View child = cg.getChildAt(i);
            if (child instanceof Chip) {
                Chip chip = (Chip) child;
                String key = "pref_taste_" + chip.getText().toString();
                chip.setChecked(prefs.getBoolean(key, i == 0)); // default first checked

                chip.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    if (prefs != null) {
                        prefs.edit().putBoolean(key, isChecked).apply();
                    }
                });
            }
        }
    }
}
