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

import com.homefit.app.R;
import com.homefit.app.data.local.dto.LocalExerciseGroupDto;
import com.homefit.app.databinding.FragmentFilteredGroupsBinding;

public class FilteredGroupsFragment extends Fragment {

    private FragmentFilteredGroupsBinding binding;
    private FilteredGroupsViewModel viewModel;
    private HomeWorkoutAdapter adapter;
    private String filterType;
    private String title;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentFilteredGroupsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(FilteredGroupsViewModel.class);

        Bundle args = getArguments();
        if (args != null) {
            filterType = args.getString("filterType", "");
            title = args.getString("title", "Danh sách bài tập");
        }

        binding.tvTitle.setText(title);
        binding.btnBack.setOnClickListener(v -> NavHostFragment.findNavController(this).navigateUp());

        adapter = new HomeWorkoutAdapter(this::onGroupClicked);
        binding.rvWorkouts.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvWorkouts.setAdapter(adapter);

        viewModel.getFilteredGroups(filterType).observe(getViewLifecycleOwner(), groups -> {
            adapter.submitList(groups);
            if (groups == null || groups.isEmpty()) {
                binding.tvEmpty.setVisibility(View.VISIBLE);
            } else {
                binding.tvEmpty.setVisibility(View.GONE);
            }
        });
    }

    private void onGroupClicked(LocalExerciseGroupDto group) {
        Bundle args = new Bundle();
        args.putString("groupKey", group.group);
        args.putString("difficulty", group.difficulty);
        NavHostFragment.findNavController(this).navigate(R.id.action_filteredGroups_to_exerciseList, args);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
