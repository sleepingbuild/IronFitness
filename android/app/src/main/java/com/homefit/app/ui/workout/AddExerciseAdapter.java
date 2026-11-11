package com.homefit.app.ui.workout;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.imageview.ShapeableImageView;
import com.homefit.app.R;
import com.homefit.app.data.local.model.CustomWorkoutItem;
import com.homefit.app.ui.common.MediaLoader;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AddExerciseAdapter extends RecyclerView.Adapter<AddExerciseAdapter.ViewHolder> {

    public interface OnSelectionChangeListener {
        void onSelectionChanged(int count);
    }

    public interface OnItemClickListener {
        void onItemClick(CustomWorkoutItem item, boolean isSelected, OnSelectionToggleCallback callback);
    }

    public interface OnSelectionToggleCallback {
        void onToggle(boolean shouldSelect);
    }

    private final List<CustomWorkoutItem> items = new ArrayList<>();
    private final Set<String> selectedIds = new HashSet<>();
    private final OnSelectionChangeListener selectionChangeListener;
    private OnItemClickListener itemClickListener;

    public AddExerciseAdapter(OnSelectionChangeListener selectionChangeListener) {
        this.selectionChangeListener = selectionChangeListener;
    }

    public void setOnItemClickListener(OnItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    public void setItems(List<CustomWorkoutItem> newItems) {
        this.items.clear();
        if (newItems != null) {
            this.items.addAll(newItems);
        }
        notifyDataSetChanged();
    }

    public List<CustomWorkoutItem> getSelectedItems() {
        List<CustomWorkoutItem> selected = new ArrayList<>();
        for (CustomWorkoutItem item : items) {
            if (selectedIds.contains(item.exerciseId)) {
                selected.add(item);
            }
        }
        return selected;
    }

    public void clearSelection() {
        selectedIds.clear();
        notifyDataSetChanged();
        if (selectionChangeListener != null) {
            selectionChangeListener.onSelectionChanged(0);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_custom_exercise, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CustomWorkoutItem item = items.get(position);
        holder.tvName.setText(item.exerciseName != null ? item.exerciseName : "");

        boolean isSelected = selectedIds.contains(item.exerciseId);

        if (isSelected) {
            holder.tvSubtitle.setText(item.getFormattedSubtitle());
            holder.tvSubtitle.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.hf_primary));
            holder.ivCheck.setImageResource(R.drawable.ic_check_circle_24);
            holder.ivCheck.setColorFilter(ContextCompat.getColor(holder.itemView.getContext(), R.color.hf_primary));
        } else {
            // Không hiển thị các con số mặc định (như 05:30, 50:15) khi chưa chọn. Hiển thị target/khu vực.
            holder.tvSubtitle.setText(item.target != null ? item.target : "Toàn thân");
            holder.tvSubtitle.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.hf_on_surface_variant));
            holder.ivCheck.setImageResource(R.drawable.ic_check_circle_24);
            holder.ivCheck.setColorFilter(ContextCompat.getColor(holder.itemView.getContext(), android.R.color.darker_gray));
        }

        MediaLoader.loadThumbnail(holder.ivThumb, "", "IMAGE");

        holder.itemView.setOnClickListener(v -> {
            int pos = holder.getBindingAdapterPosition();
            if (pos == RecyclerView.NO_POSITION) return;
            CustomWorkoutItem currentItem = items.get(pos);
            boolean currentlySelected = selectedIds.contains(currentItem.exerciseId);

            if (itemClickListener != null) {
                itemClickListener.onItemClick(currentItem, currentlySelected, shouldSelect -> {
                    if (shouldSelect) {
                        selectedIds.add(currentItem.exerciseId);
                    } else {
                        selectedIds.remove(currentItem.exerciseId);
                    }
                    notifyItemChanged(pos);
                    if (selectionChangeListener != null) {
                        selectionChangeListener.onSelectionChanged(selectedIds.size());
                    }
                });
            } else {
                if (currentlySelected) {
                    selectedIds.remove(currentItem.exerciseId);
                } else {
                    selectedIds.add(currentItem.exerciseId);
                }
                notifyItemChanged(pos);
                if (selectionChangeListener != null) {
                    selectionChangeListener.onSelectionChanged(selectedIds.size());
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ShapeableImageView ivThumb;
        TextView tvName, tvSubtitle;
        ImageView ivCheck;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivThumb = itemView.findViewById(R.id.ivThumb);
            tvName = itemView.findViewById(R.id.tvName);
            tvSubtitle = itemView.findViewById(R.id.tvSubtitle);
            ivCheck = itemView.findViewById(R.id.ivCheck);
        }
    }
}
