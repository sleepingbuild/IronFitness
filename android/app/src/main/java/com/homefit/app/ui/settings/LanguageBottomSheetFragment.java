package com.homefit.app.ui.settings;

import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.os.LocaleListCompat;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.homefit.app.R;

public class LanguageBottomSheetFragment extends BottomSheetDialogFragment {

    public interface OnLanguageChangedListener {
        void onLanguageSelected(String languageCode);
    }

    private OnLanguageChangedListener listener;

    public void setOnLanguageChangedListener(OnLanguageChangedListener listener) {
        this.listener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bottom_sheet_language, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        SettingsStore store = new SettingsStore(requireContext());
        String currentLang = store.getLanguageCode();

        LinearLayout btnVi = view.findViewById(R.id.btnLangVi);
        LinearLayout btnEn = view.findViewById(R.id.btnLangEn);
        LinearLayout btnKo = view.findViewById(R.id.btnLangKo);
        LinearLayout btnJa = view.findViewById(R.id.btnLangJa);

        ImageView ivViCheck = view.findViewById(R.id.ivViCheck);
        ImageView ivEnCheck = view.findViewById(R.id.ivEnCheck);
        ImageView ivKoCheck = view.findViewById(R.id.ivKoCheck);
        ImageView ivJaCheck = view.findViewById(R.id.ivJaCheck);

        if (ivViCheck != null) ivViCheck.setVisibility("vi".equals(currentLang) ? View.VISIBLE : View.GONE);
        if (ivEnCheck != null) ivEnCheck.setVisibility("en".equals(currentLang) ? View.VISIBLE : View.GONE);
        if (ivKoCheck != null) ivKoCheck.setVisibility("ko".equals(currentLang) ? View.VISIBLE : View.GONE);
        if (ivJaCheck != null) ivJaCheck.setVisibility("ja".equals(currentLang) ? View.VISIBLE : View.GONE);

        if (btnVi != null) btnVi.setOnClickListener(v -> selectLanguage("vi"));
        if (btnEn != null) btnEn.setOnClickListener(v -> selectLanguage("en"));
        if (btnKo != null) btnKo.setOnClickListener(v -> selectLanguage("ko"));
        if (btnJa != null) btnJa.setOnClickListener(v -> selectLanguage("ja"));
    }

    private void selectLanguage(String code) {
        SettingsStore store = new SettingsStore(requireContext());
        store.setLanguageCode(code);

        LocaleListCompat appLocale = LocaleListCompat.forLanguageTags(code);
        AppCompatDelegate.setApplicationLocales(appLocale);

        if (listener != null) {
            listener.onLanguageSelected(code);
        }

        dismiss();
        if (getActivity() != null) {
            android.content.Intent intent = new android.content.Intent(requireContext(), com.homefit.app.MainActivity.class);
            intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK | android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }
    }
}
