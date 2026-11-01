package com.homefit.app.ui.library;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.homefit.app.R;
import com.homefit.app.data.local.entity.ExerciseEntity;
import com.homefit.app.databinding.FragmentExerciseListBinding;
import com.homefit.app.ui.common.BannerAdHelper;
import com.homefit.app.util.ErrorMessages;

/**
 * Danh sách bài tập của một nhóm cơ (W2-TV6-04) kèm nút "Bắt đầu buổi tập"
 * và banner AdMob ở cuối màn (W2-TV6-07).
 */
public class ExerciseListFragment extends Fragment {

    private FragmentExerciseListBinding binding;
    private ExerciseListViewModel viewModel;
    private ExerciseAdapter adapter;
    private long categoryId;
    private final BannerAdHelper banner = new BannerAdHelper();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentExerciseListBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Bundle args = requireArguments();
        categoryId = args.getLong(LibraryArgs.CATEGORY_ID);
        binding.tvTitle.setText(args.getString(LibraryArgs.CATEGORY_NAME, ""));

        viewModel = new ViewModelProvider(this).get(ExerciseListViewModel.class);
        viewModel.init(categoryId);

        adapter = new ExerciseAdapter(this::openDetail);
        binding.rvExercises.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvExercises.setAdapter(adapter);
        binding.btnBack.setOnClickListener(v -> NavHostFragment.findNavController(this).navigateUp());
        binding.btnStart.setOnClickListener(v -> startWorkout());
        binding.stateView.btnStateAction.setOnClickListener(v -> viewModel.refresh());

        viewModel.getExercises().observe(getViewLifecycleOwner(), list -> {
            adapter.submitList(list);
            binding.btnStart.setEnabled(list != null && !list.isEmpty());
        });
        viewModel.getState().observe(getViewLifecycleOwner(), this::render);
        banner.attach(binding.adContainer);
    }

    private void render(@Nullable LibraryState state) {
        if (state == null) {
            return;
        }
        binding.progress.setVisibility(state.kind == LibraryState.Kind.LOADING ? View.VISIBLE : View.GONE);
        binding.rvExercises.setVisibility(state.kind == LibraryState.Kind.CONTENT ? View.VISIBLE : View.GONE);
        boolean showMessage = state.kind == LibraryState.Kind.EMPTY || state.kind == LibraryState.Kind.ERROR;
        binding.stateView.getRoot().setVisibility(showMessage ? View.VISIBLE : View.GONE);
        if (state.kind == LibraryState.Kind.ERROR && state.error != null) {
            binding.stateView.tvStateMessage.setText(ErrorMessages.toText(requireContext(), state.error));
        } else if (state.kind == LibraryState.Kind.EMPTY) {
            binding.stateView.tvStateMessage.setText(R.string.library_empty_exercises);
        }
    }

    private void openDetail(@NonNull ExerciseEntity exercise) {
        Bundle args = new Bundle();
        args.putLong(LibraryArgs.CATEGORY_ID, categoryId);
        args.putLong(LibraryArgs.EXERCISE_ID, exercise.id);
        NavHostFragment.findNavController(this).navigate(R.id.action_exerciseList_to_detail, args);
    }

    private void startWorkout() {
        NavController navController = NavHostFragment.findNavController(this);
        Bundle args = new Bundle();
        args.putLong(LibraryArgs.CATEGORY_ID, categoryId);
        navController.navigate(R.id.action_exerciseList_to_player, args);
    }

    @Override
    public void onResume() {
        super.onResume();
        banner.onResume();
    }

    @Override
    public void onPause() {
        banner.onPause();
        super.onPause();
    }

    @Override
    public void onDestroyView() {
        banner.destroy();
        binding.rvExercises.setAdapter(null);
        binding = null;
        super.onDestroyView();
    }
}
