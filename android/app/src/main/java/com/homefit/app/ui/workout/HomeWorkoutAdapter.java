package com.homefit.app.ui.workout;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.homefit.app.R;
import com.homefit.app.data.local.dto.LocalExerciseGroupDto;
import com.homefit.app.databinding.ItemHomeWorkoutBinding;

import java.util.ArrayList;
import java.util.List;

public class HomeWorkoutAdapter extends RecyclerView.Adapter<HomeWorkoutAdapter.ViewHolder> {

    private final List<LocalExerciseGroupDto> items = new ArrayList<>();
    private final OnItemClickListener listener;

    public interface OnItemClickListener {
        void onClick(LocalExerciseGroupDto group);
    }

    public HomeWorkoutAdapter(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void submitList(List<LocalExerciseGroupDto> list) {
        items.clear();
        if (list != null) {
            items.addAll(list);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemHomeWorkoutBinding binding = ItemHomeWorkoutBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        LocalExerciseGroupDto item = items.get(position);
        holder.bind(item);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemHomeWorkoutBinding binding;

        public ViewHolder(ItemHomeWorkoutBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
            binding.getRoot().setOnClickListener(v -> {
                int pos = getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION && listener != null) {
                    listener.onClick(items.get(pos));
                }
            });
        }

        public void bind(LocalExerciseGroupDto dto) {
            int count = dto.workouts != null ? dto.workouts.size() : 0;
            String diffStr = dto.difficulty != null ? dto.difficulty : "Medium";
            
            int duration = dto.duration_min;
            if (duration <= 0) {
                // Fake duration based on count and difficulty
                if (diffStr.equalsIgnoreCase("Easy")) duration = (int) (count * 1.0); // ~1 min per exercise
                else if (diffStr.equalsIgnoreCase("Medium")) duration = (int) (count * 1.5); // ~1.5 min per exercise
                else if (diffStr.equalsIgnoreCase("Hard")) duration = (int) (count * 2.0); // ~2 min per exercise
            }
            
            String title = dto.group_name + " " + diffStr;
            binding.tvName.setText(title);

            // Subtract Warmup/Cooldown blocks if generated via PopularWorkoutGenerator
            int mainBlockCount = count;
            if (count > 0 && dto.duration_min > 0) {
                // If it has multiple warmup and cool down blocks, main block is usually smaller
                int nonMainCount = 0;
                for (LocalExerciseGroupDto.LocalWorkoutDto w : dto.workouts) {
                    if ("Warm-up".equalsIgnoreCase(w.target) || "Cool-down".equalsIgnoreCase(w.target)) {
                        nonMainCount++;
                    }
                }
                if (nonMainCount > 0 && nonMainCount < count) {
                     mainBlockCount = count - nonMainCount;
                }
            }
            
            binding.tvDuration.setText(duration + " phút • " + mainBlockCount + (dto.duration_min > 0 ? " Bài tập / vòng" : " Bài tập"));
            
            // Thumbnail
            String groupKey = dto.group != null ? dto.group.toLowerCase() : "";
            int thumbColor = R.color.hf_thumb_fullbody;
            if (groupKey.contains("bung")) thumbColor = R.color.hf_thumb_abs;
            else if (groupKey.contains("nguc")) thumbColor = R.color.hf_thumb_chest;
            else if (groupKey.contains("tay")) thumbColor = R.color.hf_thumb_arms;
            else if (groupKey.contains("chan")) thumbColor = R.color.hf_thumb_legs;
            
            binding.cardThumb.setCardBackgroundColor(binding.getRoot().getContext().getColor(thumbColor));

            binding.llDifficulty.removeAllViews();
            int diffIcons = 2; // Default Medium
            int diffColor = R.color.hf_warning;

            if (diffStr.equalsIgnoreCase("Easy")) {
                diffIcons = 1;
                diffColor = R.color.hf_success;
            } else if (diffStr.equalsIgnoreCase("Hard")) {
                diffIcons = 3;
                diffColor = R.color.hf_danger;
            }

            for (int i = 0; i < 3; i++) {
                ImageView iv = new ImageView(binding.getRoot().getContext());
                if (i < diffIcons) {
                    iv.setImageResource(R.drawable.ic_flash);
                    iv.setColorFilter(binding.getRoot().getContext().getColor(diffColor));
                } else {
                    iv.setImageResource(R.drawable.ic_flash);
                    iv.setColorFilter(binding.getRoot().getContext().getColor(R.color.hf_outline));
                }
                int size = (int) (16 * binding.getRoot().getContext().getResources().getDisplayMetrics().density);
                iv.setLayoutParams(new ViewGroup.LayoutParams(size, size));
                binding.llDifficulty.addView(iv);
            }
        }
    }
}
