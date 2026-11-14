package com.homefit.app.ui.workout;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
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
                if (diffStr.equalsIgnoreCase("Easy")) duration = (int) (count * 1.0);
                else if (diffStr.equalsIgnoreCase("Medium")) duration = (int) (count * 1.5);
                else if (diffStr.equalsIgnoreCase("Hard")) duration = (int) (count * 2.0);
            }
            
            String title = dto.group_name + " " + diffStr;
            binding.tvName.setText(title);

            int mainBlockCount = count;
            if (count > 0 && dto.duration_min > 0) {
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

            String coverUrl = dto.coverImageUrl;
            if (coverUrl == null || coverUrl.trim().isEmpty()) {
                coverUrl = getDefaultCoverForGroup(dto.group);
            }

            if (coverUrl != null && !coverUrl.trim().isEmpty()) {
                String fullUrl = com.homefit.app.ui.common.MediaLoader.resolveUrl(coverUrl);
                Glide.with(binding.ivCoverImage)
                        .load(fullUrl)
                        .centerCrop()
                        .listener(new com.bumptech.glide.request.RequestListener<android.graphics.drawable.Drawable>() {
                            @Override
                            public boolean onLoadFailed(@androidx.annotation.Nullable com.bumptech.glide.load.engine.GlideException e, Object model, com.bumptech.glide.request.target.Target<android.graphics.drawable.Drawable> target, boolean isFirstResource) {
                                binding.ivCoverImage.setVisibility(View.GONE);
                                binding.ivIcon.setVisibility(View.VISIBLE);
                                return false;
                            }

                            @Override
                            public boolean onResourceReady(android.graphics.drawable.Drawable resource, Object model, com.bumptech.glide.request.target.Target<android.graphics.drawable.Drawable> target, com.bumptech.glide.load.DataSource dataSource, boolean isFirstResource) {
                                binding.ivCoverImage.setVisibility(View.VISIBLE);
                                binding.ivIcon.setVisibility(View.GONE);
                                return false;
                            }
                        })
                        .into(binding.ivCoverImage);
            } else {
                binding.ivCoverImage.setVisibility(View.GONE);
                binding.ivIcon.setVisibility(View.VISIBLE);
            }

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

        private String getDefaultCoverForGroup(String group) {
            if (group == null) return "https://images.unsplash.com/photo-1517838277536-f5f99be501cd?w=800";
            String g = group.toLowerCase();
            if (g.contains("bung")) return "https://images.unsplash.com/photo-1571019614242-c5c5dee9f50b?w=800";
            if (g.contains("nguc")) return "https://images.unsplash.com/photo-1581009146145-b5ef050c2e1e?w=800";
            if (g.contains("tay")) return "https://images.unsplash.com/photo-1583454110551-21f2fa2afe61?w=800";
            if (g.contains("chan")) return "https://images.unsplash.com/photo-1434596922112-19c563067271?w=800";
            if (g.contains("back") || g.contains("lung")) return "https://images.unsplash.com/photo-1605296867304-46d5465a13f1?w=800";
            if (g.contains("tabata")) return "https://images.unsplash.com/photo-1601422407692-ec4eeec1d9b3?w=800";
            if (g.contains("stretch")) return "https://images.unsplash.com/photo-1544367567-0f2fcb009e0b?w=800";
            return "https://images.unsplash.com/photo-1517838277536-f5f99be501cd?w=800";
        }
    }
}
