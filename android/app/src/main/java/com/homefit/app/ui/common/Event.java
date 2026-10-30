package com.homefit.app.ui.common;

import androidx.annotation.Nullable;

/**
 * Bọc giá trị LiveData chỉ nên xử lý một lần (hiện Snackbar, điều hướng),
 * để xoay màn hình không bị xử lý lại.
 */
public class Event<T> {

    private final T content;
    private boolean handled;

    public Event(@Nullable T content) {
        this.content = content;
    }

    /** Trả về nội dung ở lần gọi đầu, các lần sau trả null. */
    @Nullable
    public T getContentIfNotHandled() {
        if (handled) {
            return null;
        }
        handled = true;
        return content;
    }
}
