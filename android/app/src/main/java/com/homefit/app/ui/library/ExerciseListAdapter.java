package com.homefit.app.ui.library;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.homefit.app.data.local.dto.LocalExerciseGroupDto.LocalWorkoutDto;
import com.homefit.app.databinding.ItemExerciseLocalBinding;

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
            binding.tvName.setText(dto.name);
            binding.tvReps.setText(dto.reps_sets);
        }
    }
}
