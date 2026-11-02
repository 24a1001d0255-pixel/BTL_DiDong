package com.example.btl_didong;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.example.btl_didong.data.SeedLoader;
import org.osmdroid.config.Configuration;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // OsmDroid Configuration Setup (Tối ưu hóa chạy trên cả Máy ảo và Máy thật)
        Context ctx = getApplicationContext();
        Configuration.getInstance().load(ctx, ctx.getSharedPreferences("osmdroid", Context.MODE_PRIVATE));
        Configuration.getInstance().setUserAgentValue(getPackageName());
        Configuration.getInstance().setOsmdroidBasePath(ctx.getCacheDir());
        Configuration.getInstance().setOsmdroidTileCache(ctx.getCacheDir());

        setContentView(R.layout.activity_main);

        // Ask for Notification permission on Android 13+ (API 33+)
        checkAndRequestNotificationPermission();

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setOnItemSelectedListener(navListener);

        if (savedInstanceState == null) {
            HomeFragment homeFragment = new HomeFragment();
            handleNotificationIntent(getIntent(), homeFragment);
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, homeFragment).commit();
        }

        SeedLoader.loadSeedDataIfNeeded(this);

        // Check if survey has been completed on first run
        SharedPreferences prefs = getSharedPreferences("food_picker_prefs", MODE_PRIVATE);
        boolean surveyCompleted = prefs.getBoolean("survey_completed", false);
        if (!surveyCompleted) {
            Intent intent = new Intent(this, SurveyActivity.class);
            startActivity(intent);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        HomeFragment homeFragment = new HomeFragment();
        handleNotificationIntent(intent, homeFragment);
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, homeFragment).commit();
    }

    private void handleNotificationIntent(Intent intent, HomeFragment fragment) {
        if (intent != null && intent.hasExtra("OPEN_PLACE_NAME")) {
            Bundle args = new Bundle();
            args.putString("OPEN_PLACE_NAME", intent.getStringExtra("OPEN_PLACE_NAME"));
            args.putString("OPEN_PLACE_CATEGORY", intent.getStringExtra("OPEN_PLACE_CATEGORY"));
            fragment.setArguments(args);
        }
    }

    private void checkAndRequestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, 2002);
            }
        }
    }

    private final BottomNavigationView.OnItemSelectedListener navListener =
            item -> {
                Fragment selectedFragment = null;
                int itemId = item.getItemId();

                if (itemId == R.id.nav_home) {
                    selectedFragment = new HomeFragment();
                } else if (itemId == R.id.nav_nearby) {
                    selectedFragment = new NearbyFragment();
                } else if (itemId == R.id.nav_saved) {
                    selectedFragment = new SavedFragment();
                } else if (itemId == R.id.nav_profile) {
                    selectedFragment = new ProfileFragment();
                }

                if (selectedFragment != null) {
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container, selectedFragment).commit();
                }
                return true;
            };
}
