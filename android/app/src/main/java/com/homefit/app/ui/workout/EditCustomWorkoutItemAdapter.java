package com.homefit.app.ui.workout;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.homefit.app.R;
import com.homefit.app.data.local.model.CustomWorkoutItem;

import java.util.ArrayList;
import java.util.List;

public class EditCustomWorkoutItemAdapter extends RecyclerView.Adapter<EditCustomWorkoutItemAdapter.ViewHolder> {

    public interface OnItemActionListener {
        void onItemDelete(int position, CustomWorkoutItem item);
        void onItemClickValue(int position, CustomWorkoutItem item);
    }

    private final List<CustomWorkoutItem> items = new ArrayList<>();
    private final OnItemActionListener listener;

    public EditCustomWorkoutItemAdapter(OnItemActionListener listener) {
        this.listener = listener;
    }

    public void setItems(List<CustomWorkoutItem> newItems) {
        this.items.clear();
        if (newItems != null) {
            this.items.addAll(newItems);
        }
        notifyDataSetChanged();
    }

    public List<CustomWorkoutItem> getItems() {
        return items;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_edit_custom_exercise, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        int pos = holder.getBindingAdapterPosition();
        if (pos == RecyclerView.NO_POSITION) pos = position;

        CustomWorkoutItem item = items.get(pos);
        item.order = pos;

        holder.tvOrderNumber.setText(String.valueOf(pos + 1));
        holder.tvName.setText(item.exerciseName != null ? item.exerciseName : "");
        holder.tvTarget.setText(item.target != null ? item.target : "Toàn thân");
        holder.chipValue.setText(item.getFormattedSubtitle());

        holder.chipValue.setOnClickListener(v -> {
            int currentPos = holder.getBindingAdapterPosition();
            if (currentPos != RecyclerView.NO_POSITION && listener != null) {
                listener.onItemClickValue(currentPos, items.get(currentPos));
            }
        });

        holder.btnDelete.setOnClickListener(v -> {
            int currentPos = holder.getBindingAdapterPosition();
            if (currentPos != RecyclerView.NO_POSITION && listener != null) {
                listener.onItemDelete(currentPos, items.get(currentPos));
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvOrderNumber, tvName, tvTarget;
        Chip chipValue;
        ImageButton btnDelete;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvOrderNumber = itemView.findViewById(R.id.tvOrderNumber);
            tvName = itemView.findViewById(R.id.tvName);
            tvTarget = itemView.findViewById(R.id.tvTarget);
            chipValue = itemView.findViewById(R.id.chipValue);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}
