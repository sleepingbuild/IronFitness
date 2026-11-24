package com.homefit.app.ui.library;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.homefit.app.data.local.dto.LocalExerciseGroupDto.LocalWorkoutDto;
import com.homefit.app.databinding.ItemExerciseLocalBinding;
import com.homefit.app.ui.common.MediaLoader;

import java.util.ArrayList;
import java.util.List;

public class ExerciseListAdapter extends RecyclerView.Adapter<ExerciseListAdapter.ViewHolder> {

    private final List<LocalWorkoutDto> items = new ArrayList<>();

    public void submitList(List<LocalWorkoutDto> list) {
        items.clear();
        if (list != null) {
            items.addAll(list);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemExerciseLocalBinding binding = ItemExerciseLocalBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemExerciseLocalBinding binding;

        public ViewHolder(ItemExerciseLocalBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(LocalWorkoutDto dto) {
            binding.tvName.setText(com.homefit.app.util.LocalizationHelper.getLocalizedExerciseName(binding.getRoot().getContext(), dto));
            binding.tvReps.setText(com.homefit.app.util.LocalizationHelper.getLocalizedRepsSets(binding.getRoot().getContext(), dto.reps_sets));

            // Always show 2D image thumb, never default dumbbell icon
            binding.ivThumb.setVisibility(View.VISIBLE);
            binding.ivIcon.setVisibility(View.GONE);

            String mediaUrl = dto.mediaUrl;
            if (mediaUrl != null && !mediaUrl.trim().isEmpty()) {
                loadThumb(dto.name, mediaUrl);
            } else {
                com.homefit.app.util.AppExecutors.diskIO().execute(() -> {
                    var list = com.homefit.app.data.local.AppDatabase.getInstance(binding.getRoot().getContext()).exerciseDao().getAllNow();
                    String foundUrl = "";
                    String normDto = dto.name != null ? dto.name.toLowerCase(java.util.Locale.US).replaceAll("[^a-zA-Z0-9]", "") : "";
                    for (var e : list) {
                        if (e.name != null && e.name.equalsIgnoreCase(dto.name)) {
                            foundUrl = e.mediaUrl;
                            break;
                        }
                        String normE = e.name != null ? e.name.toLowerCase(java.util.Locale.US).replaceAll("[^a-zA-Z0-9]", "") : "";
                        if (!normE.isEmpty() && (normDto.contains(normE) || normE.contains(normDto))) {
                            foundUrl = e.mediaUrl;
                            break;
                        }
                    }
                    final String url = foundUrl;
                    com.homefit.app.util.AppExecutors.mainThread().execute(() -> loadThumb(dto.name, url));
                });
            }
        }

        private void loadThumb(String exerciseName, String url) {
            String fullUrl = MediaLoader.resolveExerciseMediaUrl(exerciseName, url);
            binding.ivThumb.setVisibility(View.VISIBLE);
            binding.ivIcon.setVisibility(View.GONE);

            Glide.with(binding.ivThumb)
                    .load(fullUrl)
                    .fitCenter()
                    .listener(new com.bumptech.glide.request.RequestListener<android.graphics.drawable.Drawable>() {
                        @Override
                        public boolean onLoadFailed(@androidx.annotation.Nullable com.bumptech.glide.load.engine.GlideException e, Object model, com.bumptech.glide.request.target.Target<android.graphics.drawable.Drawable> target, boolean isFirstResource) {
                            // Fallback to guaranteed 2D image URL
                            Glide.with(binding.ivThumb)
                                    .load(MediaLoader.getFallback2dMediaUrl(exerciseName))
                                    .fitCenter()
                                    .into(binding.ivThumb);
                            return true;
                        }

                        @Override
                        public boolean onResourceReady(android.graphics.drawable.Drawable resource, Object model, com.bumptech.glide.request.target.Target<android.graphics.drawable.Drawable> target, com.bumptech.glide.load.DataSource dataSource, boolean isFirstResource) {
                            return false;
                        }
                    })
                    .into(binding.ivThumb);
        }
    }
}
