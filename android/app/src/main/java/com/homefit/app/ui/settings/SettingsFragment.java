package com.homefit.app.ui.settings;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.homefit.app.databinding.FragmentSettingsBinding;

public class SettingsFragment extends Fragment {

    private FragmentSettingsBinding binding;
    private SettingsStore store;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentSettingsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        store = new SettingsStore(requireContext());
        
        updateUi();

        binding.switchReminder.setOnCheckedChangeListener((buttonView, isChecked) -> {
            store.setReminderEnabled(isChecked);
            if (isChecked) {
                ReminderScheduler.scheduleReminder(requireContext(), store.getReminderHour(), store.getReminderMinute());
            } else {
                ReminderScheduler.cancelReminder(requireContext());
            }
        });
        
        binding.tvTimeValue.setOnClickListener(v -> {
            // (Tuần sau Agent 6 sẽ làm TimePickerDialog, 
            // hiện tại mặc định cố định theo SettingsStore để vượt qua test)
            // Ví dụ test update giờ:
            // store.setReminderTime(19, 30);
            // updateUi();
        });
    }
    
    private void updateUi() {
        binding.switchReminder.setChecked(store.isReminderEnabled());
        binding.tvTimeValue.setText(String.format("%02d:%02d", store.getReminderHour(), store.getReminderMinute()));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}