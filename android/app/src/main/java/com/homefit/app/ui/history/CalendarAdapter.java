package com.homefit.app.ui.history;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.color.MaterialColors;
import com.homefit.app.R;
import com.homefit.app.databinding.ItemCalendarDayBinding;

import java.time.LocalDate;

/**
 * Adapter hiển thị lưới 7 cột các ngày trong tháng.
 */
public class CalendarAdapter extends ListAdapter<CalendarDayCell, CalendarAdapter.DayViewHolder> {

    public interface OnDayClickListener {
        void onDayClick(@NonNull LocalDate date);
    }

    private final OnDayClickListener listener;

    public CalendarAdapter(@NonNull OnDayClickListener listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
    }

    private static final DiffUtil.ItemCallback<CalendarDayCell> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<CalendarDayCell>() {
                @Override
                public boolean areItemsTheSame(@NonNull CalendarDayCell oldItem,
                                               @NonNull CalendarDayCell newItem) {
                    if (oldItem.date == null || newItem.date == null) {
                        return oldItem == newItem;
                    }
                    return oldItem.date.equals(newItem.date);
                }

                @Override
                public boolean areContentsTheSame(@NonNull CalendarDayCell oldItem,
                                                  @NonNull CalendarDayCell newItem) {
                    return oldItem.equals(newItem);
                }
            };

    @NonNull
    @Override
    public DayViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemCalendarDayBinding binding = ItemCalendarDayBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new DayViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull DayViewHolder holder, int position) {
        holder.bind(getItem(position), listener);
    }

    static class DayViewHolder extends RecyclerView.ViewHolder {

        private final ItemCalendarDayBinding binding;

        DayViewHolder(@NonNull ItemCalendarDayBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(@NonNull CalendarDayCell cell, @NonNull OnDayClickListener listener) {
            if (cell.date == null) {
                binding.tvDayNumber.setText("");
                binding.viewWorkoutDot.setVisibility(View.INVISIBLE);
                binding.dayCellContainer.setBackground(null);
                binding.dayRoot.setClickable(false);
                binding.dayRoot.setOnClickListener(null);
                return;
            }

            Context ctx = binding.getRoot().getContext();
            binding.tvDayNumber.setText(String.valueOf(cell.date.getDayOfMonth()));
            binding.viewWorkoutDot.setVisibility(cell.hasWorkout ? View.VISIBLE : View.INVISIBLE);

            if (cell.isSelected) {
                binding.dayCellContainer.setBackgroundResource(R.drawable.bg_calendar_day_selected);
                int onPrimary = MaterialColors.getColor(binding.getRoot(), com.google.android.material.R.attr.colorOnPrimary);
                binding.tvDayNumber.setTextColor(onPrimary);
            } else if (cell.isToday) {
                binding.dayCellContainer.setBackgroundResource(R.drawable.bg_calendar_day_today);
                int primary = MaterialColors.getColor(binding.getRoot(), androidx.appcompat.R.attr.colorPrimary);
                binding.tvDayNumber.setTextColor(primary);
            } else {
                binding.dayCellContainer.setBackground(null);
                int onSurface = MaterialColors.getColor(binding.getRoot(), com.google.android.material.R.attr.colorOnSurface);
                binding.tvDayNumber.setTextColor(onSurface);
            }

            binding.dayRoot.setContentDescription(ctx.getString(
                    R.string.history_selected_date_format,
                    cell.date.getDayOfMonth() + "/" + cell.date.getMonthValue()));
            binding.dayRoot.setClickable(true);
            binding.dayRoot.setOnClickListener(v -> listener.onDayClick(cell.date));
        }
    }
}