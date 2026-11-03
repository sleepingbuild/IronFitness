package com.homefit.app.ui.settings;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;
import com.homefit.app.R;
import com.homefit.app.databinding.FragmentSettingsBinding;
import com.homefit.app.ui.common.SnackbarHelper;

import java.util.Locale;

/**
 * Màn Cài đặt nhắc tập hằng ngày và quản lý phiên đăng nhập (W2-TV6-06).
 */
public class SettingsFragment extends Fragment {

    private static final String TAG_TIME_PICKER = "reminder_time_picker";

    @Nullable
    private FragmentSettingsBinding binding;
    private SettingsViewModel viewModel;
    private boolean updatingSwitchFromState;

    private final ActivityResultLauncher<String> notificationPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (binding == null) {
                    return;
                }
                if (isGranted) {
                    viewModel.setReminderEnabled(true);
                    SnackbarHelper.show(SettingsFragment.this,
                            getString(R.string.settings_reminder_saved_on, formatCurrentTime()));
                } else {
                    updatingSwitchFromState = true;
                    binding.switchReminder.setChecked(false);
                    updatingSwitchFromState = false;
                    viewModel.setReminderEnabled(false);
                    SnackbarHelper.show(SettingsFragment.this, getString(R.string.settings_permission_denied));
                }
            });

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentSettingsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (binding == null) {
            return;
        }
        viewModel = new ViewModelProvider(this).get(SettingsViewModel.class);

        binding.switchReminder.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (updatingSwitchFromState) {
                return;
            }
            if (isChecked) {
                requestPermissionAndEnableReminder();
            } else {
                viewModel.setReminderEnabled(false);
                if (binding != null) {
                    SnackbarHelper.show(this, getString(R.string.settings_reminder_saved_off));
                }
            }
        });

        binding.btnChangeTime.setOnClickListener(v -> showTimePicker());
        binding.btnLogout.setOnClickListener(v -> confirmLogout());

        observeViewModel();
    }

    private void observeViewModel() {
        viewModel.getReminderEnabled().observe(getViewLifecycleOwner(), enabled -> {
            if (binding == null) {
                return;
            }
            boolean isOn = Boolean.TRUE.equals(enabled);
            updatingSwitchFromState = true;
            binding.switchReminder.setChecked(isOn);
            updatingSwitchFromState = false;
            updateReminderStatusText();
        });

        viewModel.getReminderHour().observe(getViewLifecycleOwner(), h -> updateReminderStatusText());
        viewModel.getReminderMinute().observe(getViewLifecycleOwner(), m -> updateReminderStatusText());

        viewModel.getProfile().observe(getViewLifecycleOwner(), profile -> {
            if (binding == null) {
                return;
            }
            if (profile != null) {
                String name = (profile.displayName != null && !profile.displayName.trim().isEmpty())
                        ? profile.displayName
                        : getString(R.string.settings_account_default_name);
                binding.tvAccountName.setText(name);
                binding.tvAccountEmail.setText(profile.email != null ? profile.email : "");
                binding.tvAccountPlan.setText(profile.isPremium
                        ? R.string.settings_account_plan_premium
                        : R.string.settings_account_plan_standard);
            } else {
                binding.tvAccountName.setText(R.string.settings_account_default_name);
                binding.tvAccountEmail.setText(R.string.settings_account_email_unknown);
                binding.tvAccountPlan.setText(R.string.settings_account_plan_standard);
            }
        });
    }

    private void updateReminderStatusText() {
        if (binding == null) {
            return;
        }
        String formattedTime = formatCurrentTime();
        binding.tvReminderTime.setText(formattedTime);
        boolean isOn = Boolean.TRUE.equals(viewModel.getReminderEnabled().getValue());
        binding.tvReminderStatus.setText(isOn
                ? getString(R.string.settings_reminder_status_on, formattedTime)
                : getString(R.string.settings_reminder_status_off));
    }

    @NonNull
    private String formatCurrentTime() {
        Integer h = viewModel.getReminderHour().getValue();
        Integer m = viewModel.getReminderMinute().getValue();
        int hour = h != null ? h : 18;
        int minute = m != null ? m : 0;
        return String.format(Locale.getDefault(), "%02d:%02d", hour, minute);
    }

    private void requestPermissionAndEnableReminder() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
                return;
            }
        }
        viewModel.setReminderEnabled(true);
        if (binding != null) {
            SnackbarHelper.show(this,
                    getString(R.string.settings_reminder_saved_on, formatCurrentTime()));
        }

        new com.homefit.app.notification.ReminderReceiver().onReceive(requireContext(), new android.content.Intent());
    }

    private void showTimePicker() {
        Integer h = viewModel.getReminderHour().getValue();
        Integer m = viewModel.getReminderMinute().getValue();
        int hour = h != null ? h : 18;
        int minute = m != null ? m : 0;
        boolean is24Hour = DateFormat.is24HourFormat(requireContext());

        MaterialTimePicker picker = new MaterialTimePicker.Builder()
                .setTimeFormat(is24Hour ? TimeFormat.CLOCK_24H : TimeFormat.CLOCK_12H)
                .setHour(hour)
                .setMinute(minute)
                .setTitleText(R.string.settings_reminder_picker_title)
                .build();

        picker.addOnPositiveButtonClickListener(v -> {
            viewModel.setReminderTime(picker.getHour(), picker.getMinute());
            if (binding != null && Boolean.TRUE.equals(viewModel.getReminderEnabled().getValue())) {
                SnackbarHelper.show(this,
                        getString(R.string.settings_reminder_saved_on, formatCurrentTime()));
            }
        });

        picker.show(getChildFragmentManager(), TAG_TIME_PICKER);
    }

    private void confirmLogout() {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.settings_logout_dialog_title)
                .setMessage(R.string.settings_logout_dialog_msg)
                .setNegativeButton(R.string.action_cancel, null)
                .setPositiveButton(R.string.settings_logout_confirm, (dialog, which) -> {
                    viewModel.logout();
                    NavHostFragment.findNavController(this).navigate(R.id.action_global_login);
                })
                .show();
    }

    @Override
    public void onDestroyView() {
        binding = null;
        super.onDestroyView();
    }


}