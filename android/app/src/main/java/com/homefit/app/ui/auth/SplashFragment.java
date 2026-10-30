package com.homefit.app.ui.auth;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.homefit.app.R;
import com.homefit.app.databinding.FragmentSplashBinding;

/**
 * Màn khởi động: hiện thương hiệu khoảng 0,8 giây rồi vào tab Tập luyện (đã đăng nhập)
 * hoặc màn Đăng nhập. Việc điều hướng chỉ chạy khi màn đang ở trạng thái STARTED,
 * tránh lỗi khi ứng dụng bị đưa xuống nền.
 */
public class SplashFragment extends Fragment {

    private static final long MIN_DISPLAY_MS = 800L;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable navigateRunnable = this::navigateNext;

    private FragmentSplashBinding binding;
    private SplashViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentSplashBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(SplashViewModel.class);
    }

    @Override
    public void onStart() {
        super.onStart();
        handler.postDelayed(navigateRunnable, MIN_DISPLAY_MS);
    }

    @Override
    public void onStop() {
        handler.removeCallbacks(navigateRunnable);
        super.onStop();
    }

    private void navigateNext() {
        if (!isAdded() || binding == null) {
            return;
        }
        NavController navController = NavHostFragment.findNavController(this);
        if (navController.getCurrentDestination() == null
                || navController.getCurrentDestination().getId() != R.id.splashFragment) {
            return;
        }
        navController.navigate(viewModel.isLoggedIn()
                ? R.id.action_splash_to_workout
                : R.id.action_splash_to_login);
    }

    @Override
    public void onDestroyView() {
        binding = null;
        super.onDestroyView();
    }
}
