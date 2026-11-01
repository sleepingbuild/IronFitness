package com.homefit.app.ui.common;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.material.snackbar.Snackbar;
import com.homefit.app.R;

/** Hiện Snackbar, tự đặt phía trên thanh điều hướng dưới khi thanh này đang hiện. */
public final class SnackbarHelper {

    private SnackbarHelper() {
    }

    public static void show(@NonNull Fragment fragment, @NonNull String message) {
        View view = fragment.getView();
        if (view == null || fragment.getActivity() == null) {
            return;
        }
        Snackbar snackbar = Snackbar.make(view, message, Snackbar.LENGTH_LONG);
        View nav = fragment.requireActivity().findViewById(R.id.bottomNav);
        if (nav != null && nav.getVisibility() == View.VISIBLE) {
            snackbar.setAnchorView(nav);
        }
        snackbar.show();
    }
}
