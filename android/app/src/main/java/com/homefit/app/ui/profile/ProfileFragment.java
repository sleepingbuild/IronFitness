package com.homefit.app.ui.profile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;

import androidx.annotation.ColorRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.homefit.app.R;
import com.homefit.app.data.local.entity.UserProfileEntity;
import com.homefit.app.data.remote.ErrorCodes;
import com.homefit.app.databinding.FragmentProfileBinding;
import com.homefit.app.ui.auth.AuthValidator;
import com.homefit.app.ui.common.AfterTextChanged;
import com.homefit.app.ui.common.Event;
import com.homefit.app.ui.common.SnackbarHelper;
import com.homefit.app.ui.common.UiUtils;
import com.homefit.app.util.BmiCalculator;
import com.homefit.app.util.ErrorMessages;
import com.homefit.app.util.Resource;

import java.text.NumberFormat;
import java.util.Locale;

/**
 * Hồ sơ cá nhân: họ tên, chiều cao, cân nặng, BMI tính trực tiếp khi gõ, lưu và đăng xuất (W2-TV6-03).
 * Chiều cao và cân nặng là tùy chọn; để trống thì chỉ không có BMI.
 */
public class ProfileFragment extends Fragment {

    private FragmentProfileBinding binding;
    private ProfileViewModel viewModel;

    private final NumberFormat inputFormat = createInputFormat();
    private final NumberFormat bmiFormat = createBmiFormat();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(ProfileViewModel.class);

        binding.etName.addTextChangedListener(new AfterTextChanged(() -> binding.tilName.setError(null)));
        binding.etHeight.addTextChangedListener(new AfterTextChanged(() -> {
            binding.tilHeight.setError(null);
            renderBmi();
        }));
        binding.etWeight.addTextChangedListener(new AfterTextChanged(() -> {
            binding.tilWeight.setError(null);
            renderBmi();
        }));
        binding.etWeight.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                save();
                return true;
            }
            return false;
        });
        binding.btnSave.setOnClickListener(v -> save());
        binding.btnLogout.setOnClickListener(v -> confirmLogout());

        viewModel.getProfile().observe(getViewLifecycleOwner(), this::bindProfile);
        viewModel.getSaving().observe(getViewLifecycleOwner(), this::setSaving);
        viewModel.getSaveResult().observe(getViewLifecycleOwner(), this::onSaveResult);
        renderBmi();
    }

    private void bindProfile(@Nullable UserProfileEntity profile) {
        if (profile == null) {
            return;
        }
        binding.tvDisplayName.setText(profile.displayName);
        binding.tvEmail.setText(profile.email);
        binding.tvPremium.setVisibility(profile.isPremium ? View.VISIBLE : View.GONE);
        if (!viewModel.isFormPrefilled()) {
            binding.etName.setText(profile.displayName);
            binding.etHeight.setText(formatInput(profile.heightCm));
            binding.etWeight.setText(formatInput(profile.weightKg));
            viewModel.setFormPrefilled(true);
        }
    }

    private void save() {
        String name = UiUtils.textOf(binding.etName).trim();
        String heightText = UiUtils.textOf(binding.etHeight).trim();
        String weightText = UiUtils.textOf(binding.etWeight).trim();
        Double height = parseDecimal(heightText);
        Double weight = parseDecimal(weightText);

        int nameError = AuthValidator.nameError(name);
        boolean heightInvalid = !heightText.isEmpty()
                && (height == null || !BmiCalculator.isValidHeight(height));
        boolean weightInvalid = !weightText.isEmpty()
                && (weight == null || !BmiCalculator.isValidWeight(weight));

        binding.tilName.setError(nameError == 0 ? null : getString(nameError));
        binding.tilHeight.setError(heightInvalid
                ? getString(R.string.profile_error_height,
                (int) BmiCalculator.MIN_HEIGHT_CM, (int) BmiCalculator.MAX_HEIGHT_CM)
                : null);
        binding.tilWeight.setError(weightInvalid
                ? getString(R.string.profile_error_weight,
                (int) BmiCalculator.MIN_WEIGHT_KG, (int) BmiCalculator.MAX_WEIGHT_KG)
                : null);
        if (nameError != 0 || heightInvalid || weightInvalid) {
            return;
        }
        UiUtils.hideKeyboard(this);
        viewModel.save(name, height, weight);
    }

    private void setSaving(@Nullable Boolean saving) {
        boolean busy = Boolean.TRUE.equals(saving);
        binding.progress.setVisibility(busy ? View.VISIBLE : View.GONE);
        binding.btnSave.setEnabled(!busy);
        binding.btnSave.setText(busy ? R.string.profile_saving : R.string.profile_save);
    }

    private void onSaveResult(@Nullable Event<Resource<UserProfileEntity>> event) {
        if (event == null) {
            return;
        }
        Resource<UserProfileEntity> resource = event.getContentIfNotHandled();
        if (resource == null) {
            return;
        }
        if (resource.isSuccess()) {
            SnackbarHelper.show(this, getString(R.string.profile_saved));
        } else if (ErrorCodes.UNAUTHORIZED.equals(resource.code)) {
            viewModel.logout();
            goToLogin();
        } else {
            String error = ErrorMessages.toText(requireContext(), resource);
            SnackbarHelper.show(this, resource.data != null
                    ? getString(R.string.profile_saved_local_only, error)
                    : error);
        }
    }

    private void confirmLogout() {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.profile_logout_confirm_title)
                .setMessage(R.string.profile_logout_confirm_message)
                .setNegativeButton(R.string.action_cancel, null)
                .setPositiveButton(R.string.profile_logout, (dialog, which) -> {
                    viewModel.logout();
                    goToLogin();
                })
                .show();
    }

    private void goToLogin() {
        NavHostFragment.findNavController(this).navigate(R.id.action_global_login);
    }

    /** Cập nhật thẻ BMI theo chữ đang gõ. Chỉ hiện khi cả hai số đều hợp lệ. */
    private void renderBmi() {
        if (binding == null) {
            return;
        }
        Double height = parseDecimal(UiUtils.textOf(binding.etHeight));
        Double weight = parseDecimal(UiUtils.textOf(binding.etWeight));
        boolean valid = height != null && weight != null
                && BmiCalculator.isValidHeight(height) && BmiCalculator.isValidWeight(weight);
        if (!valid) {
            binding.bmiResult.setVisibility(View.GONE);
            binding.tvBmiHint.setVisibility(View.VISIBLE);
            return;
        }
        double bmi = BmiCalculator.calculate(height, weight);
        BmiCalculator.Category category = BmiCalculator.classify(bmi);
        binding.tvBmiValue.setText(bmiFormat.format(bmi));
        binding.tvBmiCategory.setText(categoryLabel(category));
        binding.tvBmiCategory.setTextColor(ContextCompat.getColor(requireContext(), categoryColor(category)));
        binding.bmiResult.setVisibility(View.VISIBLE);
        binding.tvBmiHint.setVisibility(View.GONE);
    }

    @StringRes
    private static int categoryLabel(@NonNull BmiCalculator.Category category) {
        switch (category) {
            case UNDERWEIGHT:
                return R.string.bmi_underweight;
            case OVERWEIGHT:
                return R.string.bmi_overweight;
            case OBESE:
                return R.string.bmi_obese;
            case NORMAL:
            default:
                return R.string.bmi_normal;
        }
    }

    @ColorRes
    private static int categoryColor(@NonNull BmiCalculator.Category category) {
        switch (category) {
            case UNDERWEIGHT:
                return R.color.hf_bmi_underweight;
            case OVERWEIGHT:
                return R.color.hf_bmi_overweight;
            case OBESE:
                return R.color.hf_bmi_obese;
            case NORMAL:
            default:
                return R.color.hf_bmi_normal;
        }
    }

    /** Đọc số thập phân, chấp nhận cả dấu phẩy và dấu chấm. Trả null nếu trống hoặc sai. */
    @Nullable
    private static Double parseDecimal(@NonNull String raw) {
        String text = raw.trim().replace(',', '.');
        if (text.isEmpty()) {
            return null;
        }
        try {
            double value = Double.parseDouble(text);
            return Double.isNaN(value) || Double.isInfinite(value) ? null : value;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    @NonNull
    private String formatInput(@Nullable Double value) {
        return value == null ? "" : inputFormat.format(value);
    }

    private static NumberFormat createInputFormat() {
        NumberFormat format = NumberFormat.getNumberInstance(Locale.getDefault());
        format.setGroupingUsed(false);
        format.setMinimumFractionDigits(0);
        format.setMaximumFractionDigits(1);
        return format;
    }

    private static NumberFormat createBmiFormat() {
        NumberFormat format = NumberFormat.getNumberInstance(Locale.getDefault());
        format.setGroupingUsed(false);
        format.setMinimumFractionDigits(1);
        format.setMaximumFractionDigits(1);
        return format;
    }

    @Override
    public void onDestroyView() {
        binding = null;
        super.onDestroyView();
    }
}
