package com.homefit.app.ui.library;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;

import com.homefit.app.R;

/** Biểu tượng mặc định theo slug nhóm cơ, dùng khi server không có iconUrl. */
public final class CategoryIcons {

    private CategoryIcons() {
    }

    @DrawableRes
    public static int forSlug(@NonNull String slug) {
        switch (slug) {
            case "abs":
                return R.drawable.ic_category_abs;
            case "chest":
                return R.drawable.ic_category_chest;
            case "legs":
                return R.drawable.ic_category_legs;
            case "full_body":
                return R.drawable.ic_category_full_body;
            default:
                return R.drawable.ic_category_default;
        }
    }
}
