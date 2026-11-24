package com.homefit.app.ui.settings;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.homefit.app.R;

public class AppearanceBottomSheetFragment extends BottomSheetDialogFragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bottom_sheet_appearance, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        SettingsStore store = new SettingsStore(requireContext());
        int currentMode = store.getNightMode();

        LinearLayout btnSystem = view.findViewById(R.id.btnThemeSystem);
        LinearLayout btnLight = view.findViewById(R.id.btnThemeLight);
        LinearLayout btnDark = view.findViewById(R.id.btnThemeDark);

        ImageView ivSystemCheck = view.findViewById(R.id.ivSystemCheck);
        ImageView ivLightCheck = view.findViewById(R.id.ivLightCheck);
        ImageView ivDarkCheck = view.findViewById(R.id.ivDarkCheck);

        if (ivSystemCheck != null) ivSystemCheck.setVisibility(currentMode == AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM ? View.VISIBLE : View.GONE);
        if (ivLightCheck != null) ivLightCheck.setVisibility(currentMode == AppCompatDelegate.MODE_NIGHT_NO ? View.VISIBLE : View.GONE);
        if (ivDarkCheck != null) ivDarkCheck.setVisibility(currentMode == AppCompatDelegate.MODE_NIGHT_YES ? View.VISIBLE : View.GONE);

        if (btnSystem != null) btnSystem.setOnClickListener(v -> selectThemeMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM));
        if (btnLight != null) btnLight.setOnClickListener(v -> selectThemeMode(AppCompatDelegate.MODE_NIGHT_NO));
        if (btnDark != null) btnDark.setOnClickListener(v -> selectThemeMode(AppCompatDelegate.MODE_NIGHT_YES));
    }

    private void selectThemeMode(int mode) {
        SettingsStore store = new SettingsStore(requireContext());
        store.setNightMode(mode);

        AppCompatDelegate.setDefaultNightMode(mode);
        dismiss();
    }
}
