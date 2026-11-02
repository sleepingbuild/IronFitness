package com.homefit.app.ui.history;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.color.MaterialColors;
import com.homefit.app.R;
import com.homefit.app.data.local.entity.WorkoutLogEntity;
import com.homefit.app.databinding.ItemWorkoutLogBinding;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Adapter hiển thị danh sách buổi tập của ngày đang chọn trên Calendar.
 */
public class WorkoutLogAdapter extends ListAdapter<WorkoutLogEntity, WorkoutLogAdapter.LogViewHolder> {

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    private Map<Long, String> categoryNames = Collections.emptyMap();

    public WorkoutLogAdapter() {
        super(DIFF_CALLBACK);
    }

    public void setCategoryNames(@NonNull Map<Long, String> map) {
        this.categoryNames = new HashMap<>(map);
        notifyDataSetChanged();
    }

    private static final DiffUtil.ItemCallback<WorkoutLogEntity> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<WorkoutLogEntity>() {
                @Override
                public boolean areItemsTheSame(@NonNull WorkoutLogEntity oldItem,
                                               @NonNull WorkoutLogEntity newItem) {
                    return oldItem.localId == newItem.localId;
                }

                @Override
                public boolean areContentsTheSame(@NonNull WorkoutLogEntity oldItem,
                                                  @NonNull WorkoutLogEntity newItem) {
                    return oldItem.completedAt == newItem.completedAt
                            && oldItem.totalSeconds == newItem.totalSeconds
                            && oldItem.exercisesDone == newItem.exercisesDone
                            && oldItem.synced == newItem.synced;
                }
            };

    @NonNull
    @Override
    public LogViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemWorkoutLogBinding binding = ItemWorkoutLogBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new LogViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull LogViewHolder holder, int position) {
        holder.bind(getItem(position), categoryNames);
    }

    static class LogViewHolder extends RecyclerView.ViewHolder {

        private final ItemWorkoutLogBinding binding;

        LogViewHolder(@NonNull ItemWorkoutLogBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(@NonNull WorkoutLogEntity item, @NonNull Map<Long, String> categoryNames) {
            Context ctx = binding.getRoot().getContext();

            String catName = item.categoryId != null ? categoryNames.get(item.categoryId) : null;
            if (catName == null || catName.trim().isEmpty()) {
                catName = ctx.getString(R.string.history_category_fallback);
            }
            binding.tvLogCategory.setText(catName);

            String durationText = formatDuration(ctx, item.totalSeconds);
            binding.tvLogDetails.setText(ctx.getString(
                    R.string.history_session_meta_format,
                    item.exercisesDone,
                    durationText));

            String timeStr = Instant.ofEpochMilli(item.completedAt)
                    .atZone(ZoneId.systemDefault())
                    .toLocalTime()
                    .format(TIME_FORMATTER);
            binding.tvLogTime.setText(timeStr);

            if (item.synced) {
                binding.tvLogSyncStatus.setText(R.string.history_status_synced);
                int primary = MaterialColors.getColor(binding.getRoot(), androidx.appcompat.R.attr.colorPrimary);
                binding.tvLogSyncStatus.setTextColor(primary);
            } else {
                binding.tvLogSyncStatus.setText(R.string.history_status_pending);
                int variant = MaterialColors.getColor(binding.getRoot(), com.google.android.material.R.attr.colorOnSurfaceVariant);
                binding.tvLogSyncStatus.setTextColor(variant);
            }
        }

        private static String formatDuration(@NonNull Context ctx, int totalSeconds) {
            int mins = totalSeconds / 60;
            int secs = totalSeconds % 60;
            if (mins > 0 && secs > 0) {
                return ctx.getString(R.string.history_duration_min_sec, mins, secs);
            } else if (mins > 0) {
                return ctx.getString(R.string.history_duration_min_only, mins);
            } else {
                return ctx.getString(R.string.history_duration_sec_only, Math.max(0, secs));
            }
        }
    }
}