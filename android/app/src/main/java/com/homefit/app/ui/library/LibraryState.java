package com.homefit.app.ui.library;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.homefit.app.util.Resource;

import java.util.List;

/** Trạng thái hiển thị của một màn danh sách: đang tải, có dữ liệu, rỗng hoặc lỗi. */
public final class LibraryState {

    public enum Kind { LOADING, CONTENT, EMPTY, ERROR }

    @NonNull
    public final Kind kind;
    /** Chỉ có giá trị khi kind = ERROR. */
    @Nullable
    public final Resource<?> error;

    private LibraryState(@NonNull Kind kind, @Nullable Resource<?> error) {
        this.kind = kind;
        this.error = error;
    }

    /**
     * Quy tắc: có dữ liệu trong Room thì luôn hiển thị (kể cả khi tải mới lỗi, vì app offline-first);
     * chưa có dữ liệu thì xem kết quả đồng bộ để chọn tải, rỗng hay lỗi.
     *
     * @param list    danh sách từ Room, null nếu Room chưa trả về
     * @param refresh kết quả đồng bộ gần nhất, null nếu chưa đồng bộ lần nào
     */
    @NonNull
    public static LibraryState of(@Nullable List<?> list, @Nullable Resource<?> refresh) {
        if (list == null) {
            return new LibraryState(Kind.LOADING, null);
        }
        if (!list.isEmpty()) {
            return new LibraryState(Kind.CONTENT, null);
        }
        if (refresh == null) {
            return new LibraryState(Kind.EMPTY, null);
        }
        if (refresh.isLoading()) {
            return new LibraryState(Kind.LOADING, null);
        }
        if (refresh.isError()) {
            return new LibraryState(Kind.ERROR, refresh);
        }
        return new LibraryState(Kind.EMPTY, null);
    }
}
