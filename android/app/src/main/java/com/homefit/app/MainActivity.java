package com.homefit.app;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
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
import com.homefit.app.ui.common.BannerAdHelper;
import com.homefit.app.ui.common.Event;
import com.homefit.app.util.NetworkUtils;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Activity duy nhất chứa NavHost và thanh điều hướng dưới.
 * Hỗ trợ chế độ Offline mượt mà: hiển thị thông báo "Đang ở chế độ ngoại tuyến (Offline)" khi mất kết nối.
 */
public class MainActivity extends AppCompatActivity {

    /** Các điểm đến cấp cao nhất (có thanh điều hướng dưới). */
    private final Set<Integer> topLevelDestinations = new HashSet<>(Arrays.asList(
            R.id.homeWorkoutFragment,
            R.id.discoverFragment,
            R.id.reportFragment,
            R.id.settingsFragment));

    /** Các điểm đến thuộc luồng đăng nhập, không cần kiểm tra phiên. */
    private final Set<Integer> authDestinations = new HashSet<>(Arrays.asList(
            R.id.splashFragment,
            R.id.loginFragment,
            R.id.registerFragment));

    private ActivityMainBinding binding;
    private NavController navController;
    private MainViewModel viewModel;
    private final BannerAdHelper bannerAdHelper = new BannerAdHelper();

    private int systemBarBottom;
    private int imeBottom;
    private boolean onTopLevelDestination;

    private Snackbar offlineSnackbar;
    private ConnectivityManager.NetworkCallback networkCallback;

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
        binding.bottomNav.setOnItemReselectedListener(item ->
                navController.popBackStack(item.getItemId(), false));
        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            onTopLevelDestination = topLevelDestinations.contains(destination.getId());
            syncBottomNavSelection(destination);
            updateChrome();
            checkOfflineMode();
            if (onTopLevelDestination && !viewModel.isLoggedIn()) {
                binding.mainRoot.post(() -> goToLogin(true));
            }
        });

        viewModel.getProfile().observe(this, profile ->
                AdsManager.setAdsEnabled(profile == null || !profile.isPremium));
        viewModel.getSessionExpired().observe(this, this::onSessionExpired);

        binding.adContainer.setVisibility(android.view.View.GONE);
        setupNetworkMonitoring();
    }

    private void checkOfflineMode() {
        if (binding == null) return;
        if (!NetworkUtils.isNetworkAvailable(this)) {
            if (offlineSnackbar == null || !offlineSnackbar.isShown()) {
                offlineSnackbar = Snackbar.make(binding.mainRoot, "Đang ở chế độ ngoại tuyến (Offline)", Snackbar.LENGTH_INDEFINITE);
                offlineSnackbar.setAction("Đã hiểu", v -> offlineSnackbar.dismiss());
                offlineSnackbar.show();
            }
        } else {
            if (offlineSnackbar != null && offlineSnackbar.isShown()) {
                offlineSnackbar.dismiss();
            }
        }
    }

    private void setupNetworkMonitoring() {
        try {
            ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            if (cm != null) {
                networkCallback = new ConnectivityManager.NetworkCallback() {
                    @Override
                    public void onAvailable(@NonNull Network network) {
                        runOnUiThread(() -> checkOfflineMode());
                    }

                    @Override
                    public void onLost(@NonNull Network network) {
                        runOnUiThread(() -> checkOfflineMode());
                    }
                };
                cm.registerDefaultNetworkCallback(networkCallback);
            }
        } catch (Exception ignored) {}
    }

    private void unregisterNetworkMonitoring() {
        if (networkCallback != null) {
            try {
                ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                if (cm != null) {
                    cm.unregisterNetworkCallback(networkCallback);
                }
            } catch (Exception ignored) {}
        }
    }

    private boolean onTabSelected(@NonNull MenuItem item) {
        NavOptions options = new NavOptions.Builder()
                .setLaunchSingleTop(true)
                .setRestoreState(true)
                .setPopUpTo(R.id.homeWorkoutFragment, false, true)
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
        if (event != null && event.getContentIfNotHandled() != null && NetworkUtils.isNetworkAvailable(this)) {
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
    protected void onResume() {
        super.onResume();
        bannerAdHelper.onResume();
        checkOfflineMode();
        new com.homefit.app.data.repository.ExerciseRepository(this).refresh();
    }

    @Override
    protected void onPause() {
        bannerAdHelper.onPause();
        super.onPause();
    }

    @Override
    public boolean onSupportNavigateUp() {
        return navController.navigateUp() || super.onSupportNavigateUp();
    }

    @Override
    protected void onDestroy() {
        unregisterNetworkMonitoring();
        bannerAdHelper.destroy();
        binding = null;
        super.onDestroy();
    }
}
