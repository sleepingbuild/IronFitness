package com.homefit.app.ui.workout;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.homefit.app.R;
import com.homefit.app.data.local.entity.CategoryEntity;
import com.homefit.app.databinding.ItemCategoryChipBinding;

public class CategoryChipAdapter extends ListAdapter<CategoryEntity, CategoryChipAdapter.Holder> {

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
            return a.name.equals(b.name);
        }
    };

    private final OnCategoryClick listener;
    private long selectedCategoryId = -1;

    public CategoryChipAdapter(@NonNull OnCategoryClick listener) {
        super(DIFF);
        this.listener = listener;
    }

    public void setSelectedCategoryId(long id) {
        this.selectedCategoryId = id;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new Holder(ItemCategoryChipBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        CategoryEntity category = getItem(position);
        holder.bind(category, category.id == selectedCategoryId, listener);
    }

    static class Holder extends RecyclerView.ViewHolder {
        private final ItemCategoryChipBinding binding;

        Holder(@NonNull ItemCategoryChipBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(@NonNull CategoryEntity category, boolean isSelected, @NonNull OnCategoryClick listener) {
            binding.tvChipName.setText(category.name);
            binding.tvChipName.setBackgroundResource(
                    isSelected ? R.drawable.bg_chip_selected : R.drawable.bg_chip_unselected);
            binding.getRoot().setOnClickListener(v -> listener.onClick(category));
        }
    }
}
