package com.homefit.app.ui.common;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavDestination;
import androidx.navigation.fragment.NavHostFragment;

import com.homefit.app.databinding.FragmentPlaceholderBinding;

/**
 * Màn TẠM cho các tab chưa có màn thật (W2-TV6-02). Hiển thị nhãn của điểm đến hiện tại
 * và mẫu thành phần giao diện dùng chung để kiểm tra theme sáng/tối.
 * Xóa file này cùng fragment_placeholder.xml khi cả 4 tab đã có màn thật.
 */
public class PlaceholderFragment extends Fragment {

    private FragmentPlaceholderBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentPlaceholderBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        NavDestination destination = NavHostFragment.findNavController(this).getCurrentDestination();
        CharSequence label = destination != null ? destination.getLabel() : null;
        binding.tvTitle.setText(label);
    }

    @Override
    public void onDestroyView() {
        binding = null;
        super.onDestroyView();
    }
}
