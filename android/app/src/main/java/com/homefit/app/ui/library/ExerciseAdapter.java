package com.homefit.app.ui.library;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.homefit.app.data.local.entity.ExerciseEntity;
import com.homefit.app.databinding.ItemExerciseBinding;
import com.homefit.app.ui.common.MediaLoader;

import java.util.Objects;

/** Danh sách bài tập: ảnh nhỏ, tên, thời gian hoặc số lần kèm nghỉ. */
public class ExerciseAdapter extends ListAdapter<ExerciseEntity, ExerciseAdapter.Holder> {

    public interface OnExerciseClick {
        void onClick(@NonNull ExerciseEntity exercise);
    }

    private static final DiffUtil.ItemCallback<ExerciseEntity> DIFF = new DiffUtil.ItemCallback<ExerciseEntity>() {
        @Override
        public boolean areItemsTheSame(@NonNull ExerciseEntity a, @NonNull ExerciseEntity b) {
            return a.id == b.id;
        }

        @Override
        public boolean areContentsTheSame(@NonNull ExerciseEntity a, @NonNull ExerciseEntity b) {
            return a.categoryId == b.categoryId && a.name.equals(b.name)
                    && Objects.equals(a.description, b.description)
                    && a.mediaUrl.equals(b.mediaUrl) && a.mediaType.equals(b.mediaType)
                    && Objects.equals(a.durationSec, b.durationSec) && Objects.equals(a.reps, b.reps)
                    && a.restSec == b.restSec && a.sortOrder == b.sortOrder && a.updatedAt == b.updatedAt;
        }
    };

    private final OnExerciseClick listener;

    public ExerciseAdapter(@NonNull OnExerciseClick listener) {
        super(DIFF);
        this.listener = listener;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new Holder(ItemExerciseBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        holder.bind(getItem(position), listener);
    }

    static class Holder extends RecyclerView.ViewHolder {

        private final ItemExerciseBinding binding;

        Holder(@NonNull ItemExerciseBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(@NonNull ExerciseEntity exercise, @NonNull OnExerciseClick listener) {
            binding.tvName.setText(exercise.name);
            binding.tvMeta.setText(ExerciseText.meta(binding.getRoot().getContext(), exercise));
            MediaLoader.loadThumbnail(binding.ivThumb, exercise.mediaUrl, exercise.mediaType);
            binding.getRoot().setOnClickListener(v -> listener.onClick(exercise));
        }
    }
}
