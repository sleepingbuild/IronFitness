package com.homefit.app.ui.workout;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.homefit.app.R;
import com.homefit.app.data.local.model.CustomWorkout;

import java.util.ArrayList;
import java.util.List;

public class CustomWorkoutAdapter extends RecyclerView.Adapter<CustomWorkoutAdapter.ViewHolder> {

    public interface OnCustomWorkoutClickListener {
        void onStartWorkout(CustomWorkout workout);
        void onEditWorkout(CustomWorkout workout);
        void onDeleteWorkout(CustomWorkout workout);
    }

    private final List<CustomWorkout> items = new ArrayList<>();
    private final OnCustomWorkoutClickListener listener;

    public CustomWorkoutAdapter(OnCustomWorkoutClickListener listener) {
        this.listener = listener;
    }

    public void setItems(List<CustomWorkout> newItems) {
        this.items.clear();
        if (newItems != null) {
            this.items.addAll(newItems);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_custom_workout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CustomWorkout workout = items.get(position);
        holder.tvTitle.setText(workout.title);
        int duration = workout.calculateDurationMin();
        int count = workout.items != null ? workout.items.size() : 0;
        holder.tvSubtitle.setText(duration + " phút • " + count + " bài tập");

        holder.btnStart.setOnClickListener(v -> {
            if (listener != null) listener.onStartWorkout(workout);
        });

        holder.btnEdit.setOnClickListener(v -> {
            if (listener != null) listener.onEditWorkout(workout);
        });

        holder.btnMenu.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(v.getContext(), v);
            popup.getMenu().add("Sửa");
            popup.getMenu().add("Xóa");
            popup.setOnMenuItemClickListener(item -> {
                if ("Sửa".equals(item.getTitle())) {
                    if (listener != null) listener.onEditWorkout(workout);
                    return true;
                } else if ("Xóa".equals(item.getTitle())) {
                    if (listener != null) listener.onDeleteWorkout(workout);
                    return true;
                }
                return false;
            });
            popup.show();
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvSubtitle;
        MaterialButton btnStart, btnEdit;
        ImageButton btnMenu;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvSubtitle = itemView.findViewById(R.id.tvSubtitle);
            btnStart = itemView.findViewById(R.id.btnStart);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnMenu = itemView.findViewById(R.id.btnMenu);
        }
    }
}
