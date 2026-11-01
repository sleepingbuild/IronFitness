package com.homefit.app.ui.common;

import android.view.View;
import android.view.Window;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.fragment.app.Fragment;

/** Tiện ích giao diện dùng chung. */
public final class UiUtils {

    private UiUtils() {
    }

    /** Chữ trong ô nhập, không bao giờ null. */
    @NonNull
    public static String textOf(@NonNull TextView view) {
        CharSequence text = view.getText();
        return text == null ? "" : text.toString();
    }

    /** Ẩn bàn phím của fragment đang hiển thị (nếu có). */
    public static void hideKeyboard(@NonNull Fragment fragment) {
        View view = fragment.getView();
        if (view == null || fragment.getActivity() == null) {
            return;
        }
        Window window = fragment.requireActivity().getWindow();
        WindowInsetsControllerCompat controller = WindowCompat.getInsetsController(window, view);
        controller.hide(WindowInsetsCompat.Type.ime());
    }
}
