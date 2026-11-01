package com.homefit.app.ui.library;

import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.core.widget.ImageViewCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.color.MaterialColors;
import com.homefit.app.data.local.entity.CategoryEntity;
import com.homefit.app.databinding.ItemCategoryBinding;

import java.util.Objects;

/** Lưới nhóm cơ. Dùng iconUrl của server nếu có, ngược lại dùng biểu tượng theo slug. */
public class CategoryAdapter extends ListAdapter<CategoryEntity, CategoryAdapter.Holder> {

    public interface OnCategoryClick {
        void onClick(@NonNull CategoryEntity category);
    }

    private static final DiffUtil.ItemCallback<CategoryEntity> DIFF = new DiffUtil.ItemCallback<CategoryEntity>() {
        @Override
        public boolean areItemsTheSame(@NonNull CategoryEntity a, @NonNull CategoryEntity b) {
            return a.id == b.id;
        }

        @Override
        public boolean areContentsTheSame(@NonNull CategoryEntity a, @NonNull CategoryEntity b) {
            return a.name.equals(b.name) && a.slug.equals(b.slug)
                    && Objects.equals(a.iconUrl, b.iconUrl)
                    && a.sortOrder == b.sortOrder && a.updatedAt == b.updatedAt;
        }
    };

    private final OnCategoryClick listener;

    public CategoryAdapter(@NonNull OnCategoryClick listener) {
        super(DIFF);
        this.listener = listener;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new Holder(ItemCategoryBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        holder.bind(getItem(position), listener);
    }

    static class Holder extends RecyclerView.ViewHolder {

        private final ItemCategoryBinding binding;

        Holder(@NonNull ItemCategoryBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(@NonNull CategoryEntity category, @NonNull OnCategoryClick listener) {
            binding.tvName.setText(category.name);
            ImageView icon = binding.ivIcon;
            int fallback = CategoryIcons.forSlug(category.slug);
            String url = category.iconUrl;
            Glide.with(icon).clear(icon);
            if (url != null && !url.trim().isEmpty()) {
                ImageViewCompat.setImageTintList(icon, null);
                Glide.with(icon).load(url).placeholder(fallback).error(fallback).into(icon);
            } else {
                icon.setImageResource(fallback);
                int primary = MaterialColors.getColor(icon, androidx.appcompat.R.attr.colorPrimary);
                ImageViewCompat.setImageTintList(icon, ColorStateList.valueOf(primary));
            }
            binding.getRoot().setOnClickListener(v -> listener.onClick(category));
        }
    }
}
