package com.homefit.app.ui.library;

import android.content.res.Configuration;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.GridLayoutManager;

import com.homefit.app.R;
import com.homefit.app.data.local.entity.CategoryEntity;
import com.homefit.app.databinding.FragmentCategoryBinding;
import com.homefit.app.ui.common.BannerAdHelper;
import com.homefit.app.util.ErrorMessages;

/**
 * Tab Tập luyện: lưới các nhóm cơ (W2-TV6-04). Có đủ trạng thái tải, rỗng, lỗi (nút Thử lại)
 * và banner AdMob ở cuối màn (W2-TV6-07).
 */
public class CategoryFragment extends Fragment {

    private FragmentCategoryBinding binding;
    private CategoryViewModel viewModel;
    private CategoryAdapter adapter;
    private final BannerAdHelper banner = new BannerAdHelper();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentCategoryBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(CategoryViewModel.class);

        int columns = getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE ? 4 : 2;
        adapter = new CategoryAdapter(this::openCategory);
        binding.rvCategories.setLayoutManager(new GridLayoutManager(requireContext(), columns));
        binding.rvCategories.setAdapter(adapter);
        binding.stateView.btnStateAction.setOnClickListener(v -> viewModel.refresh());

        viewModel.getCategories().observe(getViewLifecycleOwner(), adapter::submitList);
        viewModel.getState().observe(getViewLifecycleOwner(), this::render);
        banner.attach(binding.adContainer);
    }

    private void render(@Nullable LibraryState state) {
        if (state == null) {
            return;
        }
        binding.progress.setVisibility(state.kind == LibraryState.Kind.LOADING ? View.VISIBLE : View.GONE);
        binding.rvCategories.setVisibility(state.kind == LibraryState.Kind.CONTENT ? View.VISIBLE : View.GONE);
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
        binding.rvCategories.setAdapter(null);
        binding = null;
        super.onDestroyView();
    }
}
