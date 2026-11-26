package com.homefit.app.ui.onboarding;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.card.MaterialCardView;
import com.homefit.app.MainActivity;
import com.homefit.app.R;
import com.homefit.app.data.local.AppDatabase;
import com.homefit.app.data.local.entity.UserProfileEntity;
import com.homefit.app.data.prefs.OnboardingStore;
import com.homefit.app.databinding.ActivityOnboardingBinding;
import com.homefit.app.util.AppExecutors;

import java.util.Locale;

public class OnboardingActivity extends AppCompatActivity {

    private ActivityOnboardingBinding binding;
    private OnboardingStore onboardingStore;

    private int currentStep = 1;
    private String selectedGoal = "fatloss";
    private String selectedArea = "fullbody";
    private String selectedLevel = "beginner";
    private int selectedDays = 4;
    private String selectedGender = "Nam";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityOnboardingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            Insets bars = insets.getInsets(
                    WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.displayCutout());
            binding.llTopHeader.setPadding(
                    binding.llTopHeader.getPaddingLeft(),
                    bars.top + (int) (8 * getResources().getDisplayMetrics().density),
                    binding.llTopHeader.getPaddingRight(),
                    binding.llTopHeader.getPaddingBottom()
            );
            binding.cvBottomBar.setPadding(0, 0, 0, bars.bottom);
            return WindowInsetsCompat.CONSUMED;
        });

        onboardingStore = new OnboardingStore(this);

        setupListeners();
        updateStepUi();
    }

    private void setupListeners() {
        binding.btnOnboardingBack.setOnClickListener(v -> {
            if (currentStep > 1) {
                currentStep--;
                updateStepUi();
            } else {
                finish();
            }
        });

        binding.btnStepNext.setOnClickListener(v -> {
            if (currentStep < 4) {
                currentStep++;
                updateStepUi();
            } else {
                finishOnboarding();
            }
        });

        // Step 1: Goals
        binding.cardGoalFatLoss.setOnClickListener(v -> {
            selectedGoal = "fatloss";
            updateGoalSelection();
        });
        binding.cardGoalMuscle.setOnClickListener(v -> {
            selectedGoal = "muscle";
            updateGoalSelection();
        });
        binding.cardGoalAbs.setOnClickListener(v -> {
            selectedGoal = "abs";
            updateGoalSelection();
        });
        binding.cardGoalKeepFit.setOnClickListener(v -> {
            selectedGoal = "keepfit";
            updateGoalSelection();
        });

        // Step 2: Priority Areas
        binding.cardAreaFullbody.setOnClickListener(v -> {
            selectedArea = "fullbody";
            updateAreaSelection();
        });
        binding.cardAreaAbs.setOnClickListener(v -> {
            selectedArea = "abs";
            updateAreaSelection();
        });
        binding.cardAreaChestArms.setOnClickListener(v -> {
            selectedArea = "chest_arms";
            updateAreaSelection();
        });
        binding.cardAreaLegsGlutes.setOnClickListener(v -> {
            selectedArea = "legs_glutes";
            updateAreaSelection();
        });

        // Step 3: Level & Days
        binding.cardLevelBeginner.setOnClickListener(v -> {
            selectedLevel = "beginner";
            updateLevelSelection();
        });
        binding.cardLevelIntermediate.setOnClickListener(v -> {
            selectedLevel = "intermediate";
            updateLevelSelection();
        });
        binding.cardLevelAdvanced.setOnClickListener(v -> {
            selectedLevel = "advanced";
            updateLevelSelection();
        });

        binding.btnDays3.setOnClickListener(v -> setDays(3));
        binding.btnDays4.setOnClickListener(v -> setDays(4));
        binding.btnDays5.setOnClickListener(v -> setDays(5));
        binding.btnDays6.setOnClickListener(v -> setDays(6));

        // Step 4: Gender & Inputs
        binding.btnGenderMale.setOnClickListener(v -> setGender("Nam"));
        binding.btnGenderFemale.setOnClickListener(v -> setGender("Nữ"));
        binding.btnGenderOther.setOnClickListener(v -> setGender("Khác"));

        TextWatcher realTimeWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                recalculateStep4Summary();
            }
            @Override
            public void afterTextChanged(Editable s) {}
        };

        binding.etOnboardingAge.addTextChangedListener(realTimeWatcher);
        binding.etOnboardingHeight.addTextChangedListener(realTimeWatcher);
        binding.etOnboardingWeight.addTextChangedListener(realTimeWatcher);
        binding.etOnboardingTargetWeight.addTextChangedListener(realTimeWatcher);

        setDays(selectedDays);
        setGender(selectedGender);
    }

    private void updateOptionButton(com.google.android.material.button.MaterialButton button, boolean isSelected) {
        if (button == null) return;
        int primaryColor = getColor(R.color.hf_primary);
        int whiteColor = getColor(R.color.white);
        int strokeWidthPx = (int) (1.5f * getResources().getDisplayMetrics().density);

        if (isSelected) {
            button.setBackgroundTintList(android.content.res.ColorStateList.valueOf(primaryColor));
            button.setTextColor(whiteColor);
            button.setStrokeColor(android.content.res.ColorStateList.valueOf(primaryColor));
            button.setStrokeWidth(strokeWidthPx);
        } else {
            button.setBackgroundTintList(android.content.res.ColorStateList.valueOf(whiteColor));
            button.setTextColor(primaryColor);
            button.setStrokeColor(android.content.res.ColorStateList.valueOf(primaryColor));
            button.setStrokeWidth(strokeWidthPx);
        }
        button.setTypeface(null, android.graphics.Typeface.BOLD);
    }

    private void setDays(int days) {
        selectedDays = days;
        updateOptionButton(binding.btnDays3, days == 3);
        updateOptionButton(binding.btnDays4, days == 4);
        updateOptionButton(binding.btnDays5, days == 5);
        updateOptionButton(binding.btnDays6, days == 6);
    }

    private void setGender(String gender) {
        selectedGender = gender;
        updateOptionButton(binding.btnGenderMale, "Nam".equals(gender));
        updateOptionButton(binding.btnGenderFemale, "Nữ".equals(gender));
        updateOptionButton(binding.btnGenderOther, "Khác".equals(gender));
        recalculateStep4Summary();
    }

    private void updateStepUi() {
        binding.tvStepIndicator.setText("Bước " + currentStep + " / 4");
        binding.pbStepProgress.setProgress(currentStep * 25);

        binding.llStep1Container.setVisibility(currentStep == 1 ? View.VISIBLE : View.GONE);
        binding.llStep2Container.setVisibility(currentStep == 2 ? View.VISIBLE : View.GONE);
        binding.llStep3Container.setVisibility(currentStep == 3 ? View.VISIBLE : View.GONE);
        binding.llStep4Container.setVisibility(currentStep == 4 ? View.VISIBLE : View.GONE);

        if (currentStep == 4) {
            binding.btnStepNext.setText("HOÀN THÀNH HỒ SƠ 🎯");
            recalculateStep4Summary();
        } else {
            binding.btnStepNext.setText("TIẾP TỤC ➔");
        }
    }

    private void updateGoalSelection() {
        highlightCard(binding.cardGoalFatLoss, "fatloss".equals(selectedGoal));
        highlightCard(binding.cardGoalMuscle, "muscle".equals(selectedGoal));
        highlightCard(binding.cardGoalAbs, "abs".equals(selectedGoal));
        highlightCard(binding.cardGoalKeepFit, "keepfit".equals(selectedGoal));
    }

    private void updateAreaSelection() {
        highlightCard(binding.cardAreaFullbody, "fullbody".equals(selectedArea));
        highlightCard(binding.cardAreaAbs, "abs".equals(selectedArea));
        highlightCard(binding.cardAreaChestArms, "chest_arms".equals(selectedArea));
        highlightCard(binding.cardAreaLegsGlutes, "legs_glutes".equals(selectedArea));
    }

    private void updateLevelSelection() {
        highlightCard(binding.cardLevelBeginner, "beginner".equals(selectedLevel));
        highlightCard(binding.cardLevelIntermediate, "intermediate".equals(selectedLevel));
        highlightCard(binding.cardLevelAdvanced, "advanced".equals(selectedLevel));
    }

    private void highlightCard(MaterialCardView card, boolean isSelected) {
        if (card == null) return;
        int primaryColor = getColor(R.color.hf_primary);
        int defaultBorderColor = getColor(R.color.hf_outline);
        card.setStrokeColor(isSelected ? primaryColor : defaultBorderColor);
        card.setStrokeWidth(isSelected ? 6 : 2);
    }

    private void recalculateStep4Summary() {
        try {
            int age = Integer.parseInt(binding.etOnboardingAge.getText().toString().trim());
            float height = Float.parseFloat(binding.etOnboardingHeight.getText().toString().trim());
            float weight = Float.parseFloat(binding.etOnboardingWeight.getText().toString().trim());
            float targetWeight = Float.parseFloat(binding.etOnboardingTargetWeight.getText().toString().trim());

            float hM = height / 100f;
            float bmi = (hM > 0) ? (weight / (hM * hM)) : 22f;

            double bmr;
            if ("Nữ".equalsIgnoreCase(selectedGender)) {
                bmr = 10 * weight + 6.25 * height - 5 * age - 161;
            } else {
                bmr = 10 * weight + 6.25 * height - 5 * age + 5;
            }
            int targetKcal = (int) Math.round(bmr * 1.35);

            float diff = Math.abs(weight - targetWeight);
            int weeks = Math.max(2, (int) Math.ceil(diff / 0.5f));

            binding.tvCalcBmi.setText(String.format(Locale.US, "%.1f", bmi));
            binding.tvCalcCalories.setText(String.format(Locale.US, "%,d", targetKcal));
            binding.tvCalcWeeks.setText(weeks + " tuần");
        } catch (Exception ignored) {}
    }

    private void finishOnboarding() {
        int age = 25;
        float height = 170f;
        float weight = 65f;
        float targetWeight = 60f;

        try {
            age = Integer.parseInt(binding.etOnboardingAge.getText().toString().trim());
            height = Float.parseFloat(binding.etOnboardingHeight.getText().toString().trim());
            weight = Float.parseFloat(binding.etOnboardingWeight.getText().toString().trim());
            targetWeight = Float.parseFloat(binding.etOnboardingTargetWeight.getText().toString().trim());
        } catch (Exception ignored) {}

        onboardingStore.saveOnboardingData(
                selectedGoal, selectedArea, selectedLevel, selectedDays,
                selectedGender, age, height, weight, targetWeight
        );

        final float finalHeight = height;
        final float finalWeight = weight;
        AppExecutors.diskIO().execute(() -> {
            AppDatabase db = AppDatabase.getInstance(getApplicationContext());
            UserProfileEntity profile = db.userProfileDao().getNow();
            if (profile == null) profile = new UserProfileEntity();
            profile.heightCm = (double) finalHeight;
            profile.weightKg = (double) finalWeight;
            db.userProfileDao().upsert(profile);

            AppExecutors.mainThread().execute(() -> {
                Toast.makeText(this, "Đã lưu hồ sơ cá nhân hóa thành công! 🎉", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            });
        });
    }

    @Override
    protected void onDestroy() {
        binding = null;
        super.onDestroy();
    }
}
