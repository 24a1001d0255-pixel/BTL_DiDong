package com.example.btl_didong;

import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;

public class SurveyActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_survey);

        Chip chipWalk = findViewById(R.id.chipWalk);
        Chip chipBudget60 = findViewById(R.id.chipBudget60);
        MaterialButton btnSubmit = findViewById(R.id.btnSubmitSurvey);

        if (chipWalk != null) chipWalk.setChecked(true);
        if (chipBudget60 != null) chipBudget60.setChecked(true);

        btnSubmit.setOnClickListener(v -> {
            SharedPreferences prefs = getSharedPreferences("food_picker_prefs", MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();

            boolean isWalking = chipWalk != null && chipWalk.isChecked();
            float defaultRadius = isWalking ? 1.5f : 3.0f;
            editor.putFloat("pref_radius", defaultRadius);
            editor.putBoolean("survey_completed", true);
            editor.apply();

            setResult(RESULT_OK);
            finish();
        });
    }
}
