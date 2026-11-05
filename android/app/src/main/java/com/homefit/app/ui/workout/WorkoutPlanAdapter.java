package com.homefit.app.ui.workout;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.homefit.app.R;
import com.homefit.app.data.local.entity.CategoryEntity;
import com.homefit.app.databinding.ItemWorkoutPlanBinding;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class WorkoutPlanAdapter extends ListAdapter<WorkoutPlanAdapter.Plan, WorkoutPlanAdapter.Holder> {

    public static class Plan {
        public String id;
        public CategoryEntity category;
        public String levelName;
        public int level; // 1, 2, 3
        public int minutes;
        public int totalExercises;
        public String imageUrl;
        
        public Plan(String id, CategoryEntity category, String levelName, int level, int minutes, int totalExercises, String imageUrl) {
            this.id = id;
            this.category = category;
            this.levelName = levelName;
            this.level = level;
            this.minutes = minutes;
            this.totalExercises = totalExercises;
            this.imageUrl = imageUrl;
        }
    }

    public static List<Plan> generateFakePlans(CategoryEntity category) {
        List<Plan> plans = new ArrayList<>();
        plans.add(new Plan(category.id + "_1", category, "Người bắt đầu", 1, 14, 16, category.iconUrl));
        plans.add(new Plan(category.id + "_2", category, "Trung bình", 2, 22, 21, category.iconUrl));
        plans.add(new Plan(category.id + "_3", category, "Nâng cao", 3, 26, 21, category.iconUrl));
        return plans;
    }

    public interface OnPlanClick {
        void onClick(@NonNull CategoryEntity category);
    }

    private static final DiffUtil.ItemCallback<Plan> DIFF = new DiffUtil.ItemCallback<Plan>() {
        @Override
        public boolean areItemsTheSame(@NonNull Plan a, @NonNull Plan b) {
            return a.id.equals(b.id);
        }
        @Override
        public boolean areContentsTheSame(@NonNull Plan a, @NonNull Plan b) {
            return a.levelName.equals(b.levelName) && Objects.equals(a.imageUrl, b.imageUrl);
        }
    };

    private final OnPlanClick listener;

    public WorkoutPlanAdapter(@NonNull OnPlanClick listener) {
        super(DIFF);
        this.listener = listener;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new Holder(ItemWorkoutPlanBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        holder.bind(getItem(position), listener);
    }

    static class Holder extends RecyclerView.ViewHolder {
        private final ItemWorkoutPlanBinding binding;

        Holder(@NonNull ItemWorkoutPlanBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(@NonNull Plan plan, @NonNull OnPlanClick listener) {
            binding.tvPlanTitle.setText(plan.category.name + " " + plan.levelName);
            binding.tvPlanDesc.setText(plan.minutes + " phút • " + plan.totalExercises + " Bài tập");
            
            // Add lightning icons based on level
            binding.llDifficulty.removeAllViews();
            for (int i = 0; i < 3; i++) {
                ImageView icon = new ImageView(itemView.getContext());
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                params.setMarginEnd(4);
                icon.setLayoutParams(params);
                
                if (i < plan.level) {
                    icon.setImageResource(R.drawable.ic_lightning);
                } else {
                    icon.setImageResource(R.drawable.ic_lightning_outline);
                }
                binding.llDifficulty.addView(icon);
            }

            Glide.with(binding.ivPlanImage)
                .load(plan.imageUrl)
                .placeholder(com.homefit.app.ui.library.CategoryIcons.forSlug(plan.category.slug))
                .into(binding.ivPlanImage);

            binding.getRoot().setOnClickListener(v -> listener.onClick(plan.category));
        }
    }
}
