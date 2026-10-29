package com.homefit.app;

import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.homefit.app.databinding.ActivityMainBinding;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Activity duy nhất chứa NavHost và thanh điều hướng dưới (W2-TV6-02).
 *
 * Insets: Activity tự xử lý thanh trạng thái, thanh điều hướng hệ thống và bàn phím,
 * rồi chặn không cho truyền xuống fragment. Fragment không cần tự cộng khoảng cách.
 * Thanh điều hướng dưới chỉ hiện ở 4 tab chính và ẩn khi bàn phím đang mở.
 */
public class MainActivity extends AppCompatActivity {

    /** Các điểm đến cấp cao nhất (có thanh điều hướng dưới). */
    private final Set<Integer> topLevelDestinations = new HashSet<>(Arrays.asList(
            R.id.workoutFragment,
            R.id.historyFragment,
            R.id.profileFragment,
            R.id.settingsFragment));

    private ActivityMainBinding binding;
    private NavController navController;

    private int systemBarBottom;
    private int imeBottom;
    private boolean onTopLevelDestination = true;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

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
        NavigationUI.setupWithNavController(binding.bottomNav, navController);
        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            onTopLevelDestination = topLevelDestinations.contains(destination.getId());
            updateChrome();
        });
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
