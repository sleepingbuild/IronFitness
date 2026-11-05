package com.homefit.app.ui.workout;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.homefit.app.R;
import com.homefit.app.data.local.entity.CategoryEntity;
import com.homefit.app.databinding.FragmentWorkoutHomeBinding;
import com.homefit.app.ui.library.CategoryViewModel;
import com.homefit.app.ui.library.LibraryArgs;
import com.homefit.app.ui.library.LibraryState;
import com.homefit.app.util.ErrorMessages;

public class HomeWorkoutFragment extends Fragment {

    private FragmentWorkoutHomeBinding binding;
    private CategoryViewModel viewModel;
    private WorkoutPlanAdapter planAdapter;
    private CategoryChipAdapter chipAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentWorkoutHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(CategoryViewModel.class);

        chipAdapter = new CategoryChipAdapter(category -> {
            chipAdapter.setSelectedCategoryId(category.id);
            planAdapter.submitList(WorkoutPlanAdapter.generateFakePlans(category));
        });
        
        binding.rvCategoryChips.setLayoutManager(new LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false));
        binding.rvCategoryChips.setAdapter(chipAdapter);

        planAdapter = new WorkoutPlanAdapter(this::openCategory);
        binding.rvPlans.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvPlans.setAdapter(planAdapter);

        viewModel.getCategories().observe(getViewLifecycleOwner(), categories -> {
            if (categories != null && !categories.isEmpty()) {
                chipAdapter.submitList(categories);
                
                CategoryEntity defaultCategory = categories.get(0);
                chipAdapter.setSelectedCategoryId(defaultCategory.id);
                planAdapter.submitList(WorkoutPlanAdapter.generateFakePlans(defaultCategory));
            }
        });
        
        viewModel.getState().observe(getViewLifecycleOwner(), this::render);
        binding.stateView.btnStateAction.setOnClickListener(v -> viewModel.refresh());
    }
    
    private void render(@Nullable LibraryState state) {
        if (state == null) {
            return;
        }
        binding.progress.setVisibility(state.kind == LibraryState.Kind.LOADING ? View.VISIBLE : View.GONE);
        binding.rvPlans.setVisibility(state.kind == LibraryState.Kind.CONTENT ? View.VISIBLE : View.GONE);
        boolean showMessage = state.kind == LibraryState.Kind.EMPTY || state.kind == LibraryState.Kind.ERROR;
        binding.stateView.getRoot().setVisibility(showMessage ? View.VISIBLE : View.GONE);
        if (state.kind == LibraryState.Kind.ERROR && state.error != null) {
            binding.stateView.tvStateMessage.setText(ErrorMessages.toText(requireContext(), state.error));
        } else if (state.kind == LibraryState.Kind.EMPTY) {
            binding.stateView.tvStateMessage.setText(R.string.library_empty_categories);
        }
    }

    private void openCategory(@NonNull CategoryEntity category) {
        Bundle args = new Bundle();
        args.putLong(LibraryArgs.CATEGORY_ID, category.id);
        args.putString(LibraryArgs.CATEGORY_NAME, category.name);
        NavHostFragment.findNavController(this).navigate(R.id.action_category_to_exerciseList, args);
    }

    @Override
    public void onDestroyView() {
        binding.rvPlans.setAdapter(null);
        binding.rvCategoryChips.setAdapter(null);
        binding = null;
        super.onDestroyView();
    }
}
