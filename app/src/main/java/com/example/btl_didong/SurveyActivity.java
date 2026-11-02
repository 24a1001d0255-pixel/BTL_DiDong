package com.example.btl_didong;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import java.util.ArrayList;
import java.util.List;

public class SurveyActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_survey);

        // Taste chips
        Chip chipSpicy = findViewById(R.id.chipSpicy);
        Chip chipSalty = findViewById(R.id.chipSalty);
        Chip chipSweet = findViewById(R.id.chipSweet);
        Chip chipLight = findViewById(R.id.chipLight);
        Chip chipSourSweet = findViewById(R.id.chipSourSweet);

        // Budget chips
        Chip chipBudget30 = findViewById(R.id.chipBudget30);
        Chip chipBudget60 = findViewById(R.id.chipBudget60);
        Chip chipBudget100 = findViewById(R.id.chipBudget100);
        Chip chipBudgetAbove100 = findViewById(R.id.chipBudgetAbove100);

        // Radius / Transport chips
        Chip chipWalk = findViewById(R.id.chipWalk);
        Chip chipBike = findViewById(R.id.chipBike);
        Chip chipFar = findViewById(R.id.chipFar);

        // Avoid chips
        Chip chipAvoidGarlic = findViewById(R.id.chipAvoidGarlic);
        Chip chipAvoidSeafood = findViewById(R.id.chipAvoidSeafood);
        Chip chipAvoidOrgan = findViewById(R.id.chipAvoidOrgan);
        Chip chipAvoidFat = findViewById(R.id.chipAvoidFat);
        Chip chipAvoidVegetarian = findViewById(R.id.chipAvoidVegetarian);

        // Buttons
        MaterialButton btnSubmit = findViewById(R.id.btnSubmitSurvey);
        MaterialButton btnSkip = findViewById(R.id.btnSkipSurvey);

        if (btnSubmit != null) {
            btnSubmit.setOnClickListener(v -> {
                SharedPreferences prefs = getSharedPreferences("food_picker_prefs", MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();

                // 1. Budget
                int budgetMax = 60000;
                if (chipBudget30 != null && chipBudget30.isChecked()) {
                    budgetMax = 30000;
                } else if (chipBudget60 != null && chipBudget60.isChecked()) {
                    budgetMax = 60000;
                } else if (chipBudget100 != null && chipBudget100.isChecked()) {
                    budgetMax = 100000;
                } else if (chipBudgetAbove100 != null && chipBudgetAbove100.isChecked()) {
                    budgetMax = 200000;
                }

                // 2. Transport & Radius
                float radius = 2.0f;
                if (chipWalk != null && chipWalk.isChecked()) {
                    radius = 1.0f;
                } else if (chipBike != null && chipBike.isChecked()) {
                    radius = 2.0f;
                } else if (chipFar != null && chipFar.isChecked()) {
                    radius = 5.0f;
                }

                // 3. Tastes
                List<String> tastes = new ArrayList<>();
                if (chipSpicy != null && chipSpicy.isChecked()) tastes.add("Cay");
                if (chipSalty != null && chipSalty.isChecked()) tastes.add("Đậm đà / Mặn");
                if (chipSweet != null && chipSweet.isChecked()) tastes.add("Ngọt");
                if (chipLight != null && chipLight.isChecked()) tastes.add("Thanh nhẹ");
                if (chipSourSweet != null && chipSourSweet.isChecked()) tastes.add("Chua ngọt");

                // 4. Avoid items
                List<String> avoids = new ArrayList<>();
                if (chipAvoidGarlic != null && chipAvoidGarlic.isChecked()) avoids.add("Không ăn hành / Tỏi");
                if (chipAvoidSeafood != null && chipAvoidSeafood.isChecked()) avoids.add("Tránh Hải sản");
                if (chipAvoidOrgan != null && chipAvoidOrgan.isChecked()) avoids.add("Tránh Nội tạng");
                if (chipAvoidFat != null && chipAvoidFat.isChecked()) avoids.add("Tránh đồ béo / Nhiều dầu mỡ");
                if (chipAvoidVegetarian != null && chipAvoidVegetarian.isChecked()) avoids.add("Đồ chay / Thanh đạm");

                editor.putInt("pref_budget", budgetMax);
                editor.putFloat("pref_radius", radius);
                editor.putString("pref_selected_tastes", TextUtils.join(", ", tastes));
                editor.putString("pref_avoid_items", TextUtils.join(", ", avoids));
                editor.putBoolean("pref_avoid_vegetarian", chipAvoidVegetarian != null && chipAvoidVegetarian.isChecked());
                editor.putBoolean("survey_completed", true);
                editor.apply();

                setResult(RESULT_OK);
                finish();
            });
        }

        if (btnSkip != null) {
            btnSkip.setOnClickListener(v -> {
                SharedPreferences prefs = getSharedPreferences("food_picker_prefs", MODE_PRIVATE);
                prefs.edit().putBoolean("survey_completed", true).apply();
                setResult(RESULT_CANCELED);
                finish();
            });
        }
    }
}
