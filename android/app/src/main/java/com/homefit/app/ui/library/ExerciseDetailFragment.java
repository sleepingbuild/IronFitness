package com.homefit.app.ui.library;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.homefit.app.R;
import com.homefit.app.data.local.entity.ExerciseEntity;
import com.homefit.app.databinding.FragmentExerciseDetailBinding;
import com.homefit.app.ui.common.MediaLoader;
import com.homefit.app.util.Resource;

/**
 * Chi tiết bài tập (W2-TV6-04): GIF minh họa, thời gian hoặc số lần, nghỉ, mô tả,
 * nút "Bắt đầu buổi tập" cho nhóm cơ của bài. Không có quảng cáo ở màn này.
 */
public class ExerciseDetailFragment extends Fragment {

    private FragmentExerciseDetailBinding binding;
    private long categoryId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentExerciseDetailBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Bundle args = requireArguments();
        categoryId = args.getLong(LibraryArgs.CATEGORY_ID);
        long exerciseId = args.getLong(LibraryArgs.EXERCISE_ID);

        ExerciseDetailViewModel viewModel = new ViewModelProvider(this).get(ExerciseDetailViewModel.class);
        viewModel.init(categoryId, exerciseId);

        binding.btnBack.setOnClickListener(v -> NavHostFragment.findNavController(this).navigateUp());
        binding.btnStart.setOnClickListener(v -> startWorkout());
        binding.stateView.btnStateAction.setVisibility(View.GONE);
        viewModel.getExercise().observe(getViewLifecycleOwner(), this::render);
    }

    private void render(@Nullable Resource<ExerciseEntity> resource) {
        if (resource == null) {
            return;
        }
        binding.progress.setVisibility(resource.isLoading() ? View.VISIBLE : View.GONE);
        boolean success = resource.isSuccess() && resource.data != null;
        binding.content.setVisibility(success ? View.VISIBLE : View.GONE);
        binding.btnStart.setEnabled(success);
        boolean notFound = resource.isError();
        binding.stateView.getRoot().setVisibility(notFound ? View.VISIBLE : View.GONE);
        if (notFound) {
            binding.stateView.tvStateMessage.setText(R.string.exercise_not_found);
        }
        if (success) {
            bind(resource.data);
        }
    }

    private void bind(@NonNull ExerciseEntity exercise) {
        binding.tvName.setText(exercise.name);
        binding.ivMedia.setContentDescription(getString(R.string.exercise_media_description, exercise.name));
        MediaLoader.loadFull(binding.ivMedia, exercise.mediaUrl, exercise.mediaType);
        binding.tvVideoNotice.setVisibility(MediaLoader.isVideo(exercise.mediaType) ? View.VISIBLE : View.GONE);

        boolean byDuration = exercise.durationSec != null;
        binding.tvWorkLabel.setText(byDuration
                ? R.string.exercise_label_work_duration
                : R.string.exercise_label_work_reps);
        binding.tvWorkValue.setText(ExerciseText.work(requireContext(), exercise));
        binding.tvRestValue.setText(getString(R.string.exercise_work_duration, exercise.restSec));

        String description = exercise.description;
        boolean hasDescription = description != null && !description.trim().isEmpty();
        binding.tvDescription.setText(hasDescription ? description : getString(R.string.exercise_no_description));
    }

    private void startWorkout() {
        Bundle args = new Bundle();
        args.putLong(LibraryArgs.CATEGORY_ID, categoryId);
        NavHostFragment.findNavController(this).navigate(R.id.action_detail_to_player, args);
    }

    @Override
    public void onDestroyView() {
        binding = null;
        super.onDestroyView();
    }
}
