package com.homefit.app;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.NavOptions;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.material.snackbar.Snackbar;
import com.homefit.app.ads.AdsManager;
import com.homefit.app.databinding.ActivityMainBinding;
import com.homefit.app.ui.common.Event;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Activity duy nhất chứa NavHost và thanh điều hướng dưới (W2-TV6-02, mở rộng ở W2-TV6-03).
 *
 * Insets: Activity tự xử lý thanh trạng thái, thanh điều hướng hệ thống và bàn phím,
 * rồi chặn không cho truyền xuống fragment. Fragment không cần tự cộng khoảng cách.
 * Thanh điều hướng dưới chỉ hiện ở 4 tab chính và ẩn khi bàn phím đang mở.
 *
 * Phiên đăng nhập: khi vào tab mà không còn token, hoặc server báo hết phiên, đưa về Đăng nhập.
 * Quảng cáo: bật/tắt theo cờ Premium của hồ sơ.
 */
public class MainActivity extends AppCompatActivity {

    /** Các điểm đến cấp cao nhất (có thanh điều hướng dưới). */
    private final Set<Integer> topLevelDestinations = new HashSet<>(Arrays.asList(
            R.id.workoutFragment,
            R.id.historyFragment,
            R.id.profileFragment,
            R.id.settingsFragment));

    /** Các điểm đến thuộc luồng đăng nhập, không cần kiểm tra phiên. */
    private final Set<Integer> authDestinations = new HashSet<>(Arrays.asList(
            R.id.splashFragment,
            R.id.loginFragment,
            R.id.registerFragment));

    private ActivityMainBinding binding;
    private NavController navController;
    private MainViewModel viewModel;

    private int systemBarBottom;
    private int imeBottom;
    private boolean onTopLevelDestination;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        viewModel = new ViewModelProvider(this).get(MainViewModel.class);

        ViewCompat.setOnApplyWindowInsetsListener(binding.mainRoot, (v, insets) -> {
            Insets bars = insets.getInsets(
                    WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.displayCutout());
            Insets ime = insets.getInsets(WindowInsetsCompat.Type.ime());
            v.setPadding(bars.left, bars.top, bars.right, 0);
            systemBarBottom = bars.bottom;
            imeBottom = ime.bottom;
            updateChrome();
            return WindowInsetsCompat.CONSUMED;
        });

        Fragment host = getSupportFragmentManager().findFragmentById(R.id.navHostFragment);
        if (!(host instanceof NavHostFragment)) {
            throw new IllegalStateException("Thiếu NavHostFragment trong activity_main");
        }
        navController = ((NavHostFragment) host).getNavController();

        binding.bottomNav.setOnItemSelectedListener(this::onTabSelected);
        binding.bottomNav.setOnItemReselectedListener(item -> {
            // Chọn lại tab đang mở: giữ nguyên màn hình.
        });
        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            onTopLevelDestination = topLevelDestinations.contains(destination.getId());
            syncBottomNavSelection(destination);
            updateChrome();
            if (onTopLevelDestination && !viewModel.isLoggedIn()) {
                binding.mainRoot.post(() -> goToLogin(true));
            }
        });

        viewModel.getProfile().observe(this, profile ->
                AdsManager.setAdsEnabled(profile == null || !profile.isPremium));
        viewModel.getSessionExpired().observe(this, this::onSessionExpired);
    }

    /**
     * Chuyển tab giống NavigationUI nhưng lấy tab Tập luyện làm gốc, vì điểm vào của đồ thị là Splash.
     * Mỗi tab giữ trạng thái riêng khi chuyển qua lại.
     */
    private boolean onTabSelected(@NonNull MenuItem item) {
        NavOptions options = new NavOptions.Builder()
                .setLaunchSingleTop(true)
                .setRestoreState(true)
                .setPopUpTo(R.id.workoutFragment, false, true)
                .build();
        try {
            navController.navigate(item.getItemId(), null, options);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private void syncBottomNavSelection(NavDestination destination) {
        MenuItem item = binding.bottomNav.getMenu().findItem(destination.getId());
        if (item != null && !item.isChecked()) {
            item.setChecked(true);
        }
    }

    private void onSessionExpired(@Nullable Event<Boolean> event) {
        if (event != null && event.getContentIfNotHandled() != null) {
            goToLogin(true);
        }
    }

    private void goToLogin(boolean sessionExpired) {
        if (binding == null) {
            return;
        }
        NavDestination current = navController.getCurrentDestination();
        if (current != null && authDestinations.contains(current.getId())) {
            return;
        }
        navController.navigate(R.id.action_global_login);
        if (sessionExpired) {
            Snackbar.make(binding.mainRoot, R.string.error_unauthorized, Snackbar.LENGTH_LONG).show();
        }
    }

    /**
     * Hiện/ẩn thanh điều hướng dưới và đặt khoảng đệm đáy cho nội dung:
     * khi thanh dưới hiện, nó tự đệm đúng bằng thanh điều hướng hệ thống;
     * khi ẩn, vùng nội dung đệm bằng max(thanh hệ thống, bàn phím).
     */
    private void updateChrome() {
        if (binding == null) {
            return;
        }
        boolean imeVisible = imeBottom > 0;
        boolean showNav = onTopLevelDestination && !imeVisible;
        binding.bottomNav.setVisibility(showNav ? View.VISIBLE : View.GONE);
        binding.bottomNav.setPadding(0, 0, 0, systemBarBottom);
        int contentBottom = showNav ? 0 : Math.max(systemBarBottom, imeBottom);
        binding.navHostFragment.setPadding(0, 0, 0, contentBottom);
    }

    @Override
    public boolean onSupportNavigateUp() {
        return navController.navigateUp() || super.onSupportNavigateUp();
    }

    @Override
    protected void onDestroy() {
        binding = null;
        super.onDestroy();
    }
}
