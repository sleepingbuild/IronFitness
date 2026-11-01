package com.homefit.app.ui.common;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.view.ViewCompat;
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
        if (view == null) {
            return;
        }
        WindowInsetsControllerCompat controller = ViewCompat.getWindowInsetsController(view);
        if (controller != null) {
            controller.hide(WindowInsetsCompat.Type.ime());
        }
    }
}
