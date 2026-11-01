package com.homefit.app.ui.common;

import android.text.Editable;
import android.text.TextWatcher;

import androidx.annotation.NonNull;

/** TextWatcher chỉ quan tâm sự kiện sau khi chữ thay đổi. */
public class AfterTextChanged implements TextWatcher {

    private final Runnable action;

    public AfterTextChanged(@NonNull Runnable action) {
        this.action = action;
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        // không dùng
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        // không dùng
    }

    @Override
    public void afterTextChanged(Editable s) {
        action.run();
    }
}
